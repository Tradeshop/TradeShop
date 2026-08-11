'use strict'

// Tier 3: a real client does what a person does.
//
// THIS FILE ASSERTS NOTHING ABOUT SERVER STATE. It connects over the Minecraft
// protocol, performs a player's actions - place the chest, place the sign, TYPE
// the sign text, right-click to trade, click container slots - and reports what
// it did by running `/itstep <name>`. The in-server plugin under it/ reads the
// world and decides pass or fail.
//
// That split is the design and not a convenience. One assertion language means
// one place to read a failure, and it keeps this half thin enough that having a
// second language in the project stays a nuisance rather than a second codebase.
// If you find yourself wanting to check a block from here, the check belongs in
// it/ClientPhase.java.
//
// Everything this script needs to know about the world is told to it by the
// plugin at join time - the site coordinates and the shop header - so TradeShop
// stays the single source of truth for its own text. The only constants shared
// across the language boundary are the two account names.
//
// Run it the way ci/integration.sh does:
//   TS_IT_PORT=25599 node play.js
//
// Timeouts here are failures and never retries: login, chunk load and window
// open are all timing, and a harness that retries past a real defect is worse
// than no harness.

const mineflayer = require('mineflayer')
const { Vec3 } = require('vec3')

const HOST = process.env.TS_IT_HOST || '127.0.0.1'
const PORT = Number(process.env.TS_IT_PORT || 25599)
const VERSION = process.env.TS_IT_VERSION || '1.21.11'

// Must match it/ClientPhase.java.
const OWNER = 'TSOwner'
const BUYER = 'TSBuyer'

// Generous, because a cold runner that is merely slow must not be reported as a
// bug. Shorter than the plugin's own deadline, so that a stuck client is
// reported by the client rather than by the server timing out on it.
const STEP_TIMEOUT = Number(process.env.TS_IT_STEP_TIMEOUT || 45000)
const WHOLE_RUN_TIMEOUT = Number(process.env.TS_IT_RUN_TIMEOUT || 150000)

const started = Date.now()
const say = (...parts) => console.log('[client %ss] %s', ((Date.now() - started) / 1000).toFixed(1), parts.join(' '))

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

/** A failure the run should stop on, as opposed to a crash. */
class Failed extends Error {}

// ---------------------------------------------------------------------------
// Waiting, always with a deadline
// ---------------------------------------------------------------------------

async function until (what, predicate, timeout = STEP_TIMEOUT) {
  const deadline = Date.now() + timeout
  for (;;) {
    const value = predicate()
    if (value) return value
    if (Date.now() > deadline) throw new Failed(`timed out after ${timeout}ms waiting for ${what}`)
    await sleep(100)
  }
}

/**
 * The next window the server opens, armed BEFORE the action that opens it.
 *
 * de.themoep:inventorygui replaces its own window on every screen change, so
 * "wait for a window" has to mean "wait for the next one" or the current one
 * answers immediately and the click chain runs against a stale view.
 */
function nextWindow (bot, what, timeout = STEP_TIMEOUT) {
  const waiting = new Promise((resolve, reject) => {
    const timer = setTimeout(() => {
      bot.removeListener('windowOpen', onOpen)
      reject(new Failed(`timed out after ${timeout}ms waiting for the server to open ${what}`))
    }, timeout)
    const onOpen = (window) => {
      clearTimeout(timer)
      resolve(window)
    }
    bot.once('windowOpen', onOpen)
  })

  // Nobody awaits this until the action that opens the window has been
  // performed, so if that action throws first nobody ever will. The no-op
  // handler keeps that from surfacing as an unhandled rejection that buries the
  // real error under a second one; `await waiting` still sees the rejection,
  // because it attaches its own handler to the same promise.
  waiting.catch(() => {})
  return waiting
}

// ---------------------------------------------------------------------------
// A session
// ---------------------------------------------------------------------------

/** Every session opened, so a failure can still release the server. */
const sessions = []

function connect (username) {
  return new Promise((resolve, reject) => {
    const bot = mineflayer.createBot({
      host: HOST,
      port: PORT,
      username,
      auth: 'offline',
      version: VERSION,
      checkTimeoutInterval: 60000
    })

    bot.harness = { replies: [], site: null, ended: null }
    sessions.push(bot)

    bot.on('message', (message) => {
      const text = message.toString()
      if (!text.startsWith('TS-IT ')) return
      say(username, '<-', text)
      bot.harness.replies.push(text)

      const site = /^TS-IT SITE (-?\d+) (-?\d+) (-?\d+) (.+)$/.exec(text)
      if (site) {
        bot.harness.site = {
          chest: new Vec3(Number(site[1]), Number(site[2]), Number(site[3])),
          header: site[4]
        }
      }
    })

    bot.on('error', (e) => reject(new Failed(`${username}: ${e.message || e}`)))
    bot.on('kicked', (reason) => reject(new Failed(`${username} was kicked: ${JSON.stringify(reason)}`)))
    bot.on('end', (reason) => { bot.harness.ended = reason || 'ended' })
    bot.once('spawn', () => resolve(bot))
  })
}

/**
 * Tells the server the world has finished loading.
 *
 * Minecraft 1.21.2 added a serverbound `player_loaded` packet, and the server
 * refuses gameplay packets - block placement, block interaction, container clicks
 * - from a client that has not sent it. Mineflayer 4.37.1 never sends it: the
 * packet is in `minecraft-data`'s protocol for 1.21.11 and appears nowhere in
 * mineflayer or minecraft-protocol.
 *
 * MEASURED, because the symptom is invisible from both ends and cost an hour: the
 * server drops the placement with no console line, no event, and no reply, and the
 * client just sees a block that never changed. Adding this line is what turned
 * every action in this file from silently ignored into performed.
 *
 * This is not a workaround for the harness. It is a packet a real client sends,
 * sent at the point a real client sends it.
 */
function announceLoaded (bot) {
  bot._client.write('player_loaded', {})
}

/**
 * Runs one declared step: tells the harness what was just done, and waits for
 * its verdict.
 *
 * The verdict is not this script's opinion. It is read only to decide whether
 * carrying on would produce a cascade of failures on top of one real one.
 */
async function step (bot, name) {
  say('--> /itstep', name)
  bot.chat('/itstep ' + name)

  const prefix = 'TS-IT STEP ' + name + ' '
  const reply = await until(`the harness to judge ${name}`,
    () => bot.harness.replies.find((r) => r.startsWith(prefix) || r.startsWith('TS-IT ERROR')))

  if (reply.startsWith('TS-IT ERROR')) throw new Failed(reply)
  if (reply !== prefix + 'PASS') throw new Failed(reply)
}

// ---------------------------------------------------------------------------
// A player's actions
// ---------------------------------------------------------------------------

const centre = (pos) => pos.offset(0.5, 0.5, 0.5)

/**
 * Turns to face a block and lets the server catch up.
 *
 * Measured here, and it is the whole reason this helper exists: a look packet
 * sent in the same breath as a place packet arrives at a server that is still
 * holding the old rotation, so its reach and ray-trace checks fail and the
 * placement is dropped with nothing logged anywhere. Waiting a few real ticks -
 * not a wall-clock sleep - is waiting for the server to have processed the turn.
 */
async function face (bot, pos) {
  await bot.lookAt(centre(pos), true)
  await bot.waitForTicks(5)
}

async function hold (bot, itemName) {
  const item = bot.registry.itemsByName[itemName]
  if (!item) throw new Failed(`this Minecraft version has no item called ${itemName}`)
  await bot.equip(item.id, 'hand')
}

/**
 * Places the block in hand against the top face of `on`.
 *
 * A sign placed on a chest has to be a sneaking place: a plain right-click on a
 * chest opens the chest, which is the server behaving correctly and not a bug in
 * the harness.
 */
async function placeOnTopOf (bot, on, { sneak = false } = {}) {
  const target = on.offset(0, 1, 0)
  await face(bot, target)
  say('placing against', bot.blockAt(on) ? bot.blockAt(on).name : 'nothing', 'at', on.toString(),
    '| holding', bot.heldItem ? bot.heldItem.name + 'x' + bot.heldItem.count : 'nothing',
    'in hotbar slot', String(bot.quickBarSlot),
    '| standing at', bot.entity.position.toString(),
    'reach', bot.entity.position.offset(0, 1.62, 0).distanceTo(centre(on)).toFixed(2),
    '| sneak', String(sneak))
  if (sneak) {
    bot.setControlState('sneak', true)
    await bot.waitForTicks(5)
  }
  try {
    await bot.placeBlock(bot.blockAt(on), new Vec3(0, 1, 0))
  } finally {
    if (sneak) bot.setControlState('sneak', false)
  }
  say('placed', bot.blockAt(target) ? bot.blockAt(target).name : 'nothing', 'at', target.toString())
}

/**
 * Clicks a slot by the icon in it, the way a person finds a button.
 *
 * Failures are reported and not thrown: whether a click landed is a question
 * about the server, and it/ClientPhase.java answers it from the windows the
 * server actually opened. A click this script could not send shows up there as a
 * missing screen, with the reason printed here next to it.
 */
async function clickIcon (bot, itemName, what) {
  const window = bot.currentWindow
  if (!window) throw new Failed(`no window is open, so ${what} cannot be clicked`)

  const slot = window.slots.findIndex((item, index) =>
    index < window.inventoryStart && item && item.name === itemName)
  if (slot < 0) {
    throw new Failed(`no ${itemName} in the open window, so ${what} is not on screen. Slots: ` +
      window.slots.slice(0, window.inventoryStart).map((i) => (i ? i.name : '-')).join(','))
  }

  say('clicking', itemName, 'in slot', String(slot), '-', what)
  try {
    await bot.clickWindow(slot, 0, 0)
  } catch (e) {
    say('clickWindow rejected (the harness decides whether the click landed):', e.message || String(e))
  }
}

/**
 * Clicks the toggle that belongs to a named setting icon, one row below it.
 *
 * This is the only slot arithmetic in the harness, and it is here rather than
 * spread across the run for that reason. GUISubCommand's ITEM_LAYOUT is
 *
 *     "u ggggggg"     the setting's icon      - slots 2..8
 *     "j hhhhhhh"     the toggle for it       - slots 11..17
 *     "ap cbs na"
 *
 * so the two groups are filled in step with each other and the toggle for a
 * setting sits exactly nine slots below the icon that names it. Finding the icon
 * by item rather than by position is what keeps the rest of it robust: which
 * settings an item has depends on the item, so the icon's own slot moves.
 */
async function clickToggleUnder (bot, iconName, what) {
  const window = bot.currentWindow
  if (!window) throw new Failed(`no window is open, so the ${what} toggle cannot be clicked`)

  const icon = window.slots.findIndex((item, index) =>
    index < window.inventoryStart && item && item.name === iconName)
  if (icon < 0) {
    throw new Failed(`no ${iconName} in the open window, so ${what} is not on this page. Slots: ` +
      window.slots.slice(0, window.inventoryStart).map((i) => (i ? i.name : '-')).join(','))
  }

  const toggle = icon + 9
  const under = window.slots[toggle]
  say('clicking the', what, 'toggle in slot', String(toggle),
    '- it is', under ? under.name : 'empty', 'under the', iconName, 'in slot', String(icon))
  try {
    await bot.clickWindow(toggle, 0, 0)
  } catch (e) {
    say('clickWindow rejected (the harness decides whether the click landed):', e.message || String(e))
  }
}

// ---------------------------------------------------------------------------
// The run
// ---------------------------------------------------------------------------

async function play () {
  const owner = await connect(OWNER)
  say(OWNER, 'spawned; negotiated version', owner._client.version,
    '(protocol', String(owner.registry.version.version) + ')', 'server', owner.game.serverBrand)

  const site = await until('the harness to say where the site is', () => owner.harness.site)
  const chestPos = site.chest
  const groundPos = chestPos.offset(0, -1, 0)
  const signPos = chestPos.offset(0, 1, 0)
  say('site: ground', groundPos.toString(), 'chest', chestPos.toString(), 'sign', signPos.toString(),
    'header', site.header)

  await owner.waitForChunksToLoad()
  await until('the ground under the site to load',
    () => owner.blockAt(groundPos) && owner.blockAt(groundPos).name !== 'air')
  announceLoaded(owner)
  await owner.waitForTicks(5)
  say('inventory:', owner.inventory.items().map((i) => i.name + 'x' + i.count).join(', ') || 'empty')

  await step(owner, 'aRealClientIsLoggedIn')

  // ---- A shop created entirely over the protocol. -------------------------
  await hold(owner, 'chest')
  await placeOnTopOf(owner, groundPos)

  await hold(owner, 'oak_sign')
  await placeOnTopOf(owner, chestPos, { sneak: true })

  const sign = await until('the sign the client placed to appear',
    () => { const b = owner.blockAt(signPos); return b && b.name.endsWith('sign') ? b : null })

  // The line this whole tier exists for. Nothing server-side writes these; the
  // server's own packet handler does, from this packet.
  say('typing the sign:', [site.header, '1 DIAMOND', '1 EMERALD'].join(' | '))
  owner.updateSign(sign, `${site.header}\n1 DIAMOND\n1 EMERALD\n`)
  // No wait is needed for correctness and none is used as one: the sign packet
  // and the /itstep packet travel the same connection in that order, so the
  // server has finished the edit before it reads the step. The ticks are for the
  // console to be readable in order, not for the assertion to be true.
  await owner.waitForTicks(5)

  await step(owner, 'aClientTypedSignEditCreatesACompleteShop')

  // ---- Stocking it, with real container clicks. ---------------------------
  await face(owner, chestPos)
  const chest = await owner.openContainer(owner.blockAt(chestPos))
  say('chest window open, depositing ten diamonds')
  await chest.deposit(owner.registry.itemsByName.diamond.id, null, 10)
  chest.close()
  await owner.waitForTicks(5)

  await step(owner, 'aClientStockedShopReportsItselfOpen')

  // ---- A trade performed by a second real session. ------------------------
  const buyer = await connect(BUYER)
  say(BUYER, 'spawned')
  await buyer.waitForChunksToLoad()
  announceLoaded(buyer)
  await until('the buyer to see the shop sign',
    () => { const b = buyer.blockAt(signPos); return b && b.name.endsWith('sign') })

  await face(buyer, signPos)
  say('right-clicking the sign as', BUYER)
  await buyer.activateBlock(buyer.blockAt(signPos))
  // This step is reported over the OWNER's connection, so the packet ordering
  // that covers every other step does not cover this one - two connections have
  // no order between them. The harness polls for the buyer's diamond instead.
  await buyer.waitForTicks(5)

  await step(owner, 'aRealPlayerSessionTradesWithTheShop')

  // ---- The inventorygui paths, driven by real window clicks. --------------
  await face(owner, signPos)
  let opening = nextWindow(owner, 'the edit menu')
  owner.chat('/tradeshop edit')
  await opening

  opening = nextWindow(owner, 'the shop settings screen')
  await clickIcon(owner, 'crafting_table', 'Edit Shop Settings')
  await opening

  opening = nextWindow(owner, 'the edit menu again, via goBack')
  await clickIcon(owner, 'anvil', 'Save Changes')
  await opening
  owner.closeWindow(owner.currentWindow)
  await owner.waitForTicks(5)

  await step(owner, 'theEditGuiOpensAndItsClicksReachTheShop')

  await face(owner, signPos)
  opening = nextWindow(owner, 'the what screen')
  owner.chat('/tradeshop what')
  await opening

  opening = nextWindow(owner, 'the product item view')
  await clickIcon(owner, 'diamond', 'the product item')
  await opening
  owner.closeWindow(owner.currentWindow)
  await owner.waitForTicks(5)

  await step(owner, 'theWhatGuiShowsWhatTheShopTrades')

  // ---- A comparison switched off with a click, and the trade it decides. ---
  //
  // The shop is made to ask for the one named emerald on this server. The buyer
  // is carrying plain ones, so the two differ in exactly one attribute and one
  // setting decides the trade. Then: try to pay (must be refused), turn Compare
  // Name off through the edit GUI, try again (must go through). Nothing here
  // checks either outcome - it/ClientPhase.java reads them off the inventories on
  // either side of TradeShop's own handler.
  await hold(owner, 'emerald')
  await face(owner, signPos)
  say('typing /tradeshop setCost while holding',
    owner.heldItem ? owner.heldItem.name + ' x' + owner.heldItem.count : 'nothing')
  owner.chat('/tradeshop setCost')
  await owner.waitForTicks(10)

  await face(buyer, signPos)
  say('right-clicking the sign as', BUYER, '- the shop wants a named emerald and this one is plain')
  await buyer.activateBlock(buyer.blockAt(signPos))
  await buyer.waitForTicks(10)

  await face(owner, signPos)
  opening = nextWindow(owner, 'the edit menu')
  owner.chat('/tradeshop edit')
  await opening

  opening = nextWindow(owner, 'the cost list')
  await clickIcon(owner, 'gold_nugget', 'Edit Shop Costs')
  await opening

  opening = nextWindow(owner, "the cost item's settings")
  await clickIcon(owner, 'emerald', 'the cost item')
  await opening

  // No window is awaited here: a GuiStateElement redraws the screen it is on
  // rather than opening a new one, so waiting for one would time out on a click
  // that landed.
  await clickToggleUnder(owner, 'name_tag', 'Compare Name')
  await owner.waitForTicks(5)

  opening = nextWindow(owner, 'the cost list again, via goBack')
  await clickIcon(owner, 'anvil', 'Save Changes')
  await opening
  owner.closeWindow(owner.currentWindow)
  await owner.waitForTicks(10)

  await face(buyer, signPos)
  say('right-clicking the sign as', BUYER, 'again - same emerald, one setting different')
  await buyer.activateBlock(buyer.blockAt(signPos))
  await buyer.waitForTicks(10)

  await step(owner, 'aGuiToggledComparisonChangesWhatTheShopAccepts')

  // ---- A complex item, out of a real hand and back onto a real screen. -----
  //
  // The only row of the item-metadata matrix that belongs at this tier. The
  // sword the harness handed over at join has a name, a lore line and an
  // enchantment on it; holding it and typing /tradeshop setProduct is the path
  // an item takes through the network codec into the shop, and the what screen
  // afterwards is it coming back out as something a client can be shown.
  await hold(owner, 'diamond_sword')
  await face(owner, signPos)
  say('typing /tradeshop setProduct while holding',
    owner.heldItem ? owner.heldItem.name : 'nothing')
  owner.chat('/tradeshop setProduct')
  await owner.waitForTicks(10)

  opening = nextWindow(owner, 'the what screen, a second time')
  owner.chat('/tradeshop what')
  await opening

  opening = nextWindow(owner, 'the view of the complex product item')
  await clickIcon(owner, 'diamond_sword', 'the complex product item')
  await opening
  owner.closeWindow(owner.currentWindow)
  await owner.waitForTicks(5)

  await step(owner, 'aHeldComplexItemBecomesTheProductAndRenders')

  return owner
}

// ---------------------------------------------------------------------------

const hardStop = setTimeout(() => {
  say('FAIL the whole run did not finish within', String(WHOLE_RUN_TIMEOUT) + 'ms')
  process.exit(1)
}, WHOLE_RUN_TIMEOUT)

/**
 * Hands the server back, and waits for it to take it.
 *
 * `/itstep finish` is what makes the harness write its result file and stop the
 * server, so the observable end of this run is the server closing the connection.
 * Waiting for that rather than for a duration matters for one specific reason:
 * exiting the moment after `chat()` can close the socket before the command has
 * been flushed, and the harness would then sit on its deadline having never been
 * told to finish.
 */
async function release (owner, why) {
  say('--> /itstep finish', why || '')
  owner.chat(('/itstep finish ' + (why || '')).trim())
  try {
    await until('the server to close the connection', () => owner.harness.ended, 15000)
    say('the server closed the connection:', owner.harness.ended)
  } catch (e) {
    say('the server did not close the connection:', e.message)
  }
}

play().then(async (owner) => {
  clearTimeout(hardStop)
  await release(owner)
  say('done, every declared step passed')
  process.exit(0)
}).catch(async (e) => {
  clearTimeout(hardStop)
  say('FAIL', e instanceof Failed ? e.message : (e.stack || String(e)))
  // Still hand the server back, so the run fails on the harness's own verdict -
  // with the steps that never ran named in the result file - rather than on a
  // timeout that says nothing about why.
  const owner = sessions.find((b) => b.username === OWNER && !b.harness.ended)
  if (owner) {
    // 180, not 300. MEASURED: a `/itstep finish <reason>` longer than the
    // server's command limit is dropped with
    // "Failed to decode packet 'serverbound/minecraft:chat_command'", the
    // connection is closed, the harness is never told to finish, and the run
    // costs its whole client deadline before reporting anything. A truncated
    // reason is worth more than a lost one - the scenarios say what happened,
    // this only says why the client stopped.
    await release(owner, (e instanceof Failed ? e.message : String(e)).replace(/\s+/g, ' ').slice(0, 180))
  }
  process.exit(1)
})
