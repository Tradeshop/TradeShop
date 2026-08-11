/*
 * Copyright (c) 2016-2026
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *                http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.shanerx.tradeshop.it;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.shanerx.tradeshop.data.storage.DataStorage;
import org.shanerx.tradeshop.data.storage.DataType;
import org.shanerx.tradeshop.item.ShopItemSide;
import org.shanerx.tradeshop.item.ShopItemStack;
import org.shanerx.tradeshop.item.ShopItemStackSettingKeys;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shop.ShopStatus;
import org.shanerx.tradeshop.shop.ShopType;
import org.shanerx.tradeshop.shoplocation.ShopLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Tier 3: a real client does what a person does, and this class watches it.
 *
 * <h2>The division of labour</h2>
 * <b>The bot acts. This class asserts.</b> Everything a player performs - placing
 * the chest, placing the sign, <em>typing the sign text</em>, right-clicking to
 * trade, clicking container slots - arrives here as a real packet handled by the
 * real server, driven from {@code it-client/play.js} over the Minecraft protocol.
 * Nothing in this file performs a player's action, and in particular nothing here
 * writes a sign: that is the single behaviour every lower tier has to fake, and
 * faking it here would leave the harness asserting its own simulation at the exact
 * point TradeShop's mechanic starts.
 *
 * <p>The bot reports only what it did, by running {@code /itstep <name>}. Each
 * step name is declared up front, in order, and running it makes this class read
 * the world and decide. So there is one assertion language and one place to read a
 * failure, and the JavaScript half stays thin.
 *
 * <h2>Sequencing, and why it is not a sleep</h2>
 * The tier-2 half of this plugin shuts the server down when its scenarios end. A
 * bot needs the server alive, and needs to have connected first. So in client
 * mode the plugin does not shut down when tier 2 finishes: it calls
 * {@link #begin} instead, which prepares the site and prints
 * {@value #ARMED_MARKER} to the console. The runner waits for that line before it
 * launches the bot - a state the runner can observe, not an interval it guesses -
 * and the server then stays up until the bot's final {@code /itstep finish}, or
 * until the deadline below expires.
 *
 * <h2>A bot that never arrives</h2>
 * The vacuous pass this whole project treats as the worst possible output is a
 * server that boots, runs nothing and exits 0. So the deadline does not let the
 * run off: every declared step the client never reached is recorded as a
 * <em>failure</em>, by name, and the runner exits non-zero on it. There is no
 * path through this class that turns a missing client into a smaller suite.
 */
final class ClientPhase implements Listener, CommandExecutor {

    /**
     * The line the runner waits for before it launches the bot. Changing this text
     * means changing {@code ci/integration.sh} in the same commit.
     */
    static final String ARMED_MARKER = "tier 3 armed, waiting for a client";

    /**
     * The two accounts the bot logs in as. Shared with {@code it-client/play.js}
     * by name and nothing else; every coordinate the bot needs is told to it at
     * join time, so this is the only constant that has to agree in two languages.
     */
    static final String OWNER_BOT = "TSOwner";
    static final String BUYER_BOT = "TSBuyer";

    /**
     * Declared before anything runs, and in the order the bot must call them.
     *
     * <p>Declared up front because "the suite ran fewer scenarios than it has" is
     * the failure a report written by the run itself cannot catch.
     */
    static final List<String> STEPS = List.of(
            "aRealClientIsLoggedIn",
            "aClientTypedSignEditCreatesACompleteShop",
            "aClientStockedShopReportsItselfOpen",
            "aRealPlayerSessionTradesWithTheShop",
            "theEditGuiOpensAndItsClicksReachTheShop",
            "theWhatGuiShowsWhatTheShopTrades",
            // Before the step below rather than after it, because this one needs the
            // shop still selling diamonds out of a stocked chest.
            "aGuiToggledComparisonChangesWhatTheShopAccepts",
            // Last on purpose: it replaces the shop's product, so every step above
            // it would be asserting a shop this one has already changed.
            "aHeldComplexItemBecomesTheProductAndRenders");

    /** The step name that is not a step: it closes the run. */
    private static final String FINISH = "finish";

    /** The complex item the owner bot is given, and the only one in this tier. */
    private static final String PRODUCT_NAME = "Kingsblade";
    private static final String PRODUCT_LORE = "Forged at the end of the world";
    private static final ItemStack COMPLEX_PRODUCT = complexProduct();

    private static ItemStack complexProduct() {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD, 1);
        ItemMeta meta = sword.getItemMeta();
        meta.setDisplayName(PRODUCT_NAME);
        meta.setLore(List.of(PRODUCT_LORE));
        meta.addEnchant(Enchantment.SHARPNESS, 3, true);
        sword.setItemMeta(meta);
        return sword;
    }

    /**
     * The emerald the owner bot makes its shop ask for, so that the buyer's plain
     * ones are decided by one setting and nothing else.
     *
     * <p>A display name and nothing more: {@code COMPARE_NAME} is the only
     * comparison that separates this from what the buyer is carrying, so the trade
     * flips on that one toggle rather than on whichever check happened to run
     * first.
     */
    private static final String TAGGED_COST_NAME = "Pie's Emerald";

    private static ItemStack taggedCost() {
        ItemStack emerald = new ItemStack(Material.EMERALD, 1);
        ItemMeta meta = emerald.getItemMeta();
        meta.setDisplayName(TAGGED_COST_NAME);
        emerald.setItemMeta(meta);
        return emerald;
    }

    /**
     * A patch of world no tier-2 scenario has touched. {@link SiteAllocator}
     * is what keeps that true now rather than a comment claiming a range -
     * every {@link RealShop}-backed suite gets its sites from it, and
     * {@link SiteAllocator#TIER_3_CLIENT} is the one index it can never hand
     * out, because {@link RealShop} itself refuses to build there. TradeShop
     * keys every shop, chest linkage and protection entry by world name plus
     * coordinates, so an index nobody else holds is an unwritten namespace.
     */
    private static final int SITE_X = SiteAllocator.TIER_3_CLIENT * 1000;
    private static final int SITE_Z = 0;

    /**
     * How long the server waits for the client to finish, in milliseconds.
     *
     * <p>Generous on purpose: login, chunk load, window open and packet
     * round-trips are all timing, and a cold runner that is merely slow must not
     * be reported as a bug. Shortened only by
     * {@code ci/integration.sh} when it is deliberately proving the
     * no-client path, exactly as {@code TS_IT_TIMEOUT} shortens the hang path.
     */
    private static final long DEADLINE_MILLIS =
            Long.getLong("tradeshop.it.clientdeadline", 180_000L);

    private final IntegrationPlugin plugin;

    /** Tier 2's result lines, which this phase appends to and then writes. */
    private List<String> results;

    private volatile boolean armed;
    private volatile boolean finished;
    private volatile int nextStep;
    private volatile String abortedAfter;

    private Block chestBlock;
    private Block signBlock;

    /**
     * Every inventory title the server opened for a player, in order.
     *
     * <p>This is how the {@code inventorygui} paths are asserted without the bot
     * asserting anything: {@code de.themoep:inventorygui} only opens its next
     * screen from inside a click handler, so a title appearing in this list is
     * proof that a real click packet reached that handler. The bot finds the icon
     * and clicks it; the trail says what the server did about it.
     */
    private final List<String> openedTitles = new CopyOnWriteArrayList<>();
    private final List<String> clicks = new CopyOnWriteArrayList<>();

    /**
     * What every right-click on the shop sign did to the clicker's diamonds.
     *
     * <p>One entry per click, taken on either side of TradeShop's own handler:
     * {@code ShopTradeListener.onBlockInteract} runs at {@code LOW} and performs
     * the whole trade inside the event, so a {@code LOWEST} listener sees the
     * inventory before it and a {@code MONITOR} listener sees it after.
     *
     * <p>It exists because "the trade outcome flipped" is a statement about two
     * moments, and a step can only assert at one. Counting items at the end says
     * how many trades happened but not <em>which</em> attempt was refused - and a
     * toggle that broke the trade instead of enabling it would leave the same
     * totals behind. This records the attempts in order, so the row can say the
     * one before the GUI click moved nothing and the one after it moved a diamond.
     */
    private final List<SignClick> signClicks = new CopyOnWriteArrayList<>();

    /** Written by the {@code LOWEST} handler and read by the {@code MONITOR} one, same event. */
    private volatile int diamondsBeforeClick;

    private record SignClick(String player, int before, int after) {
        int moved() {
            return after - before;
        }

        @Override
        public String toString() {
            return player + " " + before + "->" + after;
        }
    }

    ClientPhase(IntegrationPlugin plugin) {
        this.plugin = plugin;
    }

    // ------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------

    /**
     * Wires the listeners and the command at enable time, before any client can
     * connect. The phase is not {@link #armed} yet: a step arriving before tier 2
     * has finished is a bug in the runner's sequencing and is answered as one.
     */
    void wire() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        if (plugin.getCommand("itstep") == null) {
            throw new IllegalStateException("it/plugin.yml does not declare the itstep command, "
                    + "so a bot has no way to report what it did");
        }
        plugin.getCommand("itstep").setExecutor(this);
    }

    /**
     * Called when tier 2 has finished, from the scenario thread.
     *
     * <p>Prepares the site, arms the step machine, prints the marker the runner is
     * waiting for, and starts the deadline. It deliberately does not shut the
     * server down, which is the one difference between a tier-2 run and this one.
     */
    void begin(List<String> tier2Results) {
        this.results = tier2Results;
        Sync.run(plugin, this::prepareSite);
        armed = true;
        plugin.getLogger().info(ARMED_MARKER + " (" + STEPS.size() + " step(s), deadline "
                + (DEADLINE_MILLIS / 1000) + "s, site " + SITE_X + "/" + SITE_Z + ")");
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, this::deadlineExpired,
                Math.max(20L, DEADLINE_MILLIS / 50L));
    }

    /**
     * The site: a chest position and a sign position above it, both empty.
     *
     * <p>The ground is whatever the flat generator put there. This method places
     * <b>no</b> chest and <b>no</b> sign - if it did, the step that asserts the
     * client built the shop would be asserting the harness instead.
     */
    private void prepareSite() {
        World world = Bukkit.getWorlds().get(0);
        world.getChunkAt(SITE_X >> 4, SITE_Z >> 4).load(true);

        int groundY = world.getHighestBlockYAt(SITE_X, SITE_Z);
        chestBlock = world.getBlockAt(SITE_X, groundY + 1, SITE_Z);
        signBlock = world.getBlockAt(SITE_X, groundY + 2, SITE_Z);

        chestBlock.setType(Material.AIR, false);
        signBlock.setType(Material.AIR, false);
    }

    private void deadlineExpired() {
        if (finished) {
            return;
        }
        plugin.getLogger().warning("the client did not finish within " + (DEADLINE_MILLIS / 1000)
                + "s - it reached step " + nextStep + " of " + STEPS.size()
                + ". A timeout is a failure, never a skip.");
        finish("the client never reached this step: it stopped after "
                + nextStep + " of " + STEPS.size() + " within " + (DEADLINE_MILLIS / 1000) + "s");
    }

    /**
     * Writes the result and stops the server, once.
     *
     * <p>Every declared step the client did not reach is recorded as a failure
     * naming why. A run that is missing steps must not be able to look like a
     * smaller suite that passed.
     */
    private synchronized void finish(String whyRemainingFailed) {
        if (finished) {
            return;
        }
        finished = true;

        for (int i = nextStep; i < STEPS.size(); i++) {
            results.add("SCENARIO " + STEPS.get(i) + " FAIL " + whyRemainingFailed);
        }
        if (!clicks.isEmpty()) {
            plugin.getLogger().info("window clicks the client made: " + clicks);
        }
        if (!openedTitles.isEmpty()) {
            plugin.getLogger().info("windows the server opened: " + openedTitles);
        }

        plugin.writeResult(results);
        Bukkit.getScheduler().runTask(plugin, Bukkit::shutdown);
    }

    // ------------------------------------------------------------------
    // The fixture: what the bot is given, as opposed to what it does
    // ------------------------------------------------------------------

    /**
     * A bot arrives, and is put where it can work.
     *
     * <p>Teleporting, opping and handing over items are <em>fixture</em>, the same
     * way the tier-2 scenes hand their buyer five emeralds before it clicks
     * anything. What is under test is what the client then does with them: place,
     * type, click. The site coordinates and the shop header are told to the bot
     * here rather than hard-coded in two languages, so TradeShop stays the single
     * source of truth for its own header.
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!armed) {
            return;
        }

        player.setOp(true);
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().clear();

        Location where;
        if (OWNER_BOT.equals(player.getName())) {
            // One block east of the chest, looking west at it.
            where = new Location(chestBlock.getWorld(), SITE_X + 1.5, chestBlock.getY(), SITE_Z + 0.5, 90f, 0f);
            player.getInventory().addItem(new ItemStack(Material.CHEST, 1));
            player.getInventory().addItem(new ItemStack(Material.OAK_SIGN, 1));
            player.getInventory().addItem(new ItemStack(Material.DIAMOND, 10));
            // The one complex item in this tier. Handing it over is fixture, the
            // same way the ten diamonds are; what is under test is that a client
            // holding it and typing /tradeshop setProduct ends up with a shop that
            // sells exactly this, and that the GUI can draw it. The attributes are
            // chosen to be the ones the network item codec has to carry: a name, a
            // lore line and an enchantment.
            player.getInventory().addItem(COMPLEX_PRODUCT.clone());
            // The one emerald on this server that carries a name. Fixture in the
            // same way the diamonds are; what is under test is that a shop asking
            // for it refuses the buyer's plain ones until a click says otherwise.
            player.getInventory().addItem(taggedCost());
        } else if (BUYER_BOT.equals(player.getName())) {
            // Two blocks south of the sign, looking north at it.
            where = new Location(chestBlock.getWorld(), SITE_X + 0.5, chestBlock.getY(), SITE_Z + 2.5, 0f, 0f);
            player.getInventory().addItem(new ItemStack(Material.EMERALD, 5));
        } else {
            plugin.getLogger().warning("an unexpected client logged in as " + player.getName()
                    + "; this harness only knows " + OWNER_BOT + " and " + BUYER_BOT);
            return;
        }

        player.teleport(where);
        player.sendMessage("TS-IT SITE " + chestBlock.getX() + " " + chestBlock.getY() + " "
                + chestBlock.getZ() + " " + ShopType.TRADE.toHeader());
        plugin.getLogger().info("client " + player.getName() + " joined from "
                + (player.getAddress() == null ? "nowhere" : player.getAddress().toString())
                + ", placed at " + where.toVector() + " holding "
                + describe(player.getInventory().getContents()));
    }

    // ------------------------------------------------------------------
    // The observers
    // ------------------------------------------------------------------

    /**
     * Every block the client tries to place, and whether anything stopped it.
     *
     * <p>Kept because the failure it makes readable is otherwise invisible from
     * both ends: a placement the server declines to apply produces no console line
     * of its own, and the client only sees a block that did not change. With this,
     * "the packet never arrived" and "a listener cancelled it" are different lines
     * in the log instead of the same silence.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(org.bukkit.event.block.BlockPlaceEvent event) {
        plugin.getLogger().info("client " + event.getPlayer().getName() + " placed "
                + event.getBlockPlaced().getType() + " at " + event.getBlockPlaced().getLocation().toVector()
                + " cancelled=" + event.isCancelled());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(org.bukkit.event.player.PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }
        plugin.getLogger().info("client " + event.getPlayer().getName() + " " + event.getAction()
                + " on " + event.getClickedBlock().getType() + " at "
                + event.getClickedBlock().getLocation().toVector()
                + " hand=" + event.getMaterial() + " useInteracted=" + event.useInteractedBlock()
                + " useItem=" + event.useItemInHand());
    }

    /**
     * The clicker's diamonds as they were when the packet arrived, before
     * TradeShop's {@code LOW} handler has had the event.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void beforeShopSignClick(org.bukkit.event.player.PlayerInteractEvent event) {
        if (!isShopSignClick(event)) {
            return;
        }
        diamondsBeforeClick = count(event.getPlayer().getInventory().getContents(), Material.DIAMOND);
    }

    /** And as they are once every listener has run, which is once the trade has or has not happened. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void afterShopSignClick(org.bukkit.event.player.PlayerInteractEvent event) {
        if (!isShopSignClick(event)) {
            return;
        }
        signClicks.add(new SignClick(event.getPlayer().getName(), diamondsBeforeClick,
                count(event.getPlayer().getInventory().getContents(), Material.DIAMOND)));
    }

    /**
     * One record per click, not two.
     *
     * <p>A right-click on a block is offered to each hand in turn, so without the
     * hand test a single click by the buyer would be filed as two attempts and the
     * row below would be counting the server's packet handling rather than the
     * player's actions.
     */
    private boolean isShopSignClick(org.bukkit.event.player.PlayerInteractEvent event) {
        return signBlock != null
                && event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK
                && event.getHand() == org.bukkit.inventory.EquipmentSlot.HAND
                && signBlock.equals(event.getClickedBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryOpen(InventoryOpenEvent event) {
        openedTitles.add(event.getView().getTitle());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        clicks.add(event.getWhoClicked().getName() + " slot " + event.getRawSlot() + " on "
                + (event.getCurrentItem() == null ? "nothing" : event.getCurrentItem().getType().name())
                + " in \"" + event.getView().getTitle() + "\" cancelled=" + event.isCancelled());
    }

    // ------------------------------------------------------------------
    // The step machine
    // ------------------------------------------------------------------

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("TS-IT ERROR only a connected client runs steps");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage("TS-IT ERROR usage: /itstep <name> | /itstep finish [why]");
            return true;
        }
        String name = args[0];

        if (!armed) {
            player.sendMessage("TS-IT ERROR the harness is not armed yet; the runner launched the "
                    + "client before \"" + ARMED_MARKER + "\" appeared");
            return true;
        }
        if (finished) {
            player.sendMessage("TS-IT ERROR the run is already over");
            return true;
        }

        if (FINISH.equals(name)) {
            // The client says why it stopped, because it is the only party that
            // knows whether an action could be performed at all. It still does not
            // get to say whether the behaviour was right - the steps above do that.
            String said = args.length > 1
                    ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length))
                    : "";
            String why;
            if (abortedAfter != null) {
                why = "not attempted: the client aborted after " + abortedAfter + " failed";
            } else if (!said.isEmpty()) {
                why = "not attempted: the client could not perform its actions - " + said;
            } else {
                why = "not attempted: the client stopped before this step and gave no reason";
            }
            player.sendMessage("TS-IT FINISHED");
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> finish(why));
            return true;
        }

        if (nextStep >= STEPS.size()) {
            player.sendMessage("TS-IT ERROR every declared step has run; expected " + FINISH);
            return true;
        }
        String expected = STEPS.get(nextStep);
        if (!expected.equals(name)) {
            // Out of order is a failure and not a re-ordering: the steps build on
            // each other, and a suite that silently runs a different sequence than
            // it declared is the failure this list exists to prevent.
            record(name, "FAIL the client ran steps out of order: expected " + expected);
            abortedAfter = name;
            player.sendMessage("TS-IT STEP " + name + " FAIL out of order, expected " + expected);
            return true;
        }

        // Off the main thread: a step waits for the server, and TradeShop does
        // disk work on the main thread.
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> runStep(player, name));
        return true;
    }

    private void runStep(Player reporter, String name) {
        String verdict;
        try {
            plugin.induceAssertionFailure(name);
            body(name);
            verdict = "PASS";
            plugin.getLogger().info("PASS " + name);
        } catch (Throwable t) {
            verdict = "FAIL " + plugin.describe(t);
            plugin.recordStack(name, t);
        }

        record(name, verdict);
        nextStep++;
        if (verdict.startsWith("FAIL")) {
            abortedAfter = name;
        }
        // The bot needs to know whether to keep going; it is not reading this as
        // an assertion, and it does not get to decide the verdict.
        reporter.sendMessage("TS-IT STEP " + name + " " + verdict);
    }

    private void record(String name, String verdict) {
        results.add("SCENARIO " + name + " " + verdict);
    }

    // ------------------------------------------------------------------
    // The assertions
    // ------------------------------------------------------------------

    private void body(String name) {
        switch (name) {
            case "aRealClientIsLoggedIn" -> aRealClientIsLoggedIn();
            case "aClientTypedSignEditCreatesACompleteShop" -> aClientTypedSignEditCreatesACompleteShop();
            case "aClientStockedShopReportsItselfOpen" -> aClientStockedShopReportsItselfOpen();
            case "aRealPlayerSessionTradesWithTheShop" -> aRealPlayerSessionTradesWithTheShop();
            case "theEditGuiOpensAndItsClicksReachTheShop" -> theEditGuiOpensAndItsClicksReachTheShop();
            case "theWhatGuiShowsWhatTheShopTrades" -> theWhatGuiShowsWhatTheShopTrades();
            case "aGuiToggledComparisonChangesWhatTheShopAccepts" ->
                    aGuiToggledComparisonChangesWhatTheShopAccepts();
            case "aHeldComplexItemBecomesTheProductAndRenders" -> aHeldComplexItemBecomesTheProductAndRenders();
            default -> throw new AssertionError("no body is written for declared step " + name);
        }
    }

    /**
     * The anti-vacuous-pass step: there is genuinely a client here.
     *
     * <p>The tier-2 harness drives a {@code Player} that is a reflective proxy with
     * no socket behind it. This asserts the opposite of that, so that a run where
     * the bot never logged in cannot reach any later step and call it a pass.
     */
    private void aRealClientIsLoggedIn() {
        Player owner = requireOnline(OWNER_BOT);

        Assert.that(owner.getAddress() != null,
                "a real session has a socket address; the tier-2 harness player has none");
        Assert.that(owner.getClass().getName().startsWith("org.bukkit.craftbukkit"),
                "the player must be the server's own, not a proxy over the Player interface - was "
                        + owner.getClass().getName());
        Assert.equal(GameMode.SURVIVAL, Sync.get(plugin, owner::getGameMode),
                "the client trades in survival, where item counts mean something");

        // And the site is untouched, so what the next step finds there was put
        // there by the client.
        Assert.equal(Material.AIR, Sync.get(plugin, () -> chestBlock.getType()),
                "nothing has placed the chest yet");
        Assert.equal(Material.AIR, Sync.get(plugin, () -> signBlock.getType()),
                "nothing has placed the sign yet");
    }

    /**
     * The step this whole tier exists for.
     *
     * <p>The client placed the chest, placed the sign and <b>typed the sign
     * text</b>. A real server applies a sign edit from the packet handler that
     * fired {@code SignChangeEvent}, which is why the tier-2 scenario
     * {@code aSyntheticSignEditLeavesTheBlockBlankSoTheShopNeverOpens} is green:
     * an event fired by a harness is not a packet handler, so the block stays
     * blank and no shop is created. With a client on the other end the write does
     * happen, and every assertion below reads what the server stored.
     */
    private void aClientTypedSignEditCreatesACompleteShop() {
        Assert.equal(Material.CHEST, Sync.get(plugin, () -> chestBlock.getType()),
                "the client should have placed a chest");
        Assert.that(Sync.get(plugin, () -> signBlock.getState() instanceof Sign),
                "the client should have placed a sign above the chest, was "
                        + Sync.get(plugin, () -> signBlock.getType()));

        Assert.eventually(15_000, "TradeShop to store a shop at the sign the client wrote",
                () -> shop() != null);
        Shop shop = shop();

        Assert.equal(ShopType.TRADE, shop.getShopType(), "the header the client typed makes a trade shop");
        Assert.equal(uuidOf(OWNER_BOT), shop.getOwner().getUUID(), "the client that typed the sign owns the shop");
        Assert.equal(Sync.get(plugin, () -> chestBlock.getLocation()), shop.getInventoryLocation(),
                "the shop should be linked to the chest the client placed under the sign");

        // Complete is the point: lines 1 and 2 of a typed sign ARE the two sides,
        // so unlike /tradeshop create this path needs no follow-up command.
        Assert.that(!shop.isMissingItems(),
                "a typed sign sets both sides, so the shop is complete without a setProduct or a setCost");
        assertSide(shop, ShopItemSide.PRODUCT, Material.DIAMOND, 1);
        assertSide(shop, ShopItemSide.COST, Material.EMERALD, 1);
        Assert.equal(ShopStatus.OUT_OF_STOCK, shop.getStatus(),
                "a complete shop with an empty chest is out of stock, not incomplete");

        // Read off the real block. Nothing in this harness writes a sign; these
        // lines are on it because TradeShop decorated the event and the server's
        // packet handler wrote the result back.
        String[] lines = signLines();
        Assert.equal(ShopType.TRADE.toHeader(), lines[0], "line 0 is the shop's header");
        Assert.equal("1 Diamond", lines[1], "line 1 is what the shop gives");
        Assert.equal("1 Emerald", lines[2], "line 2 is what the shop takes");
        Assert.equal(strip(ShopStatus.OUT_OF_STOCK.getLine()), lines[3],
                "line 3 is where a player reads the shop's status");
    }

    /**
     * The client opened the chest, moved its diamonds in slot by slot, and shut
     * the lid. TradeShop recounts stock on the close.
     */
    private void aClientStockedShopReportsItselfOpen() {
        Assert.equal(10, countInChest(Material.DIAMOND),
                "the client should have moved ten diamonds into the chest with real window clicks");
        Assert.equal(0, countOf(requireOnline(OWNER_BOT), Material.DIAMOND),
                "and should no longer be carrying them");

        Assert.eventually(15_000, "a stocked shop to report itself open",
                () -> shop().getStatus() == ShopStatus.OPEN);
        Assert.equal(10, shop().getAvailableTrades(), "ten diamonds at one per trade is ten trades");
        Assert.equal(strip(ShopStatus.OPEN.getLine()), signLines()[3],
                "line 3 should have been rewritten on the block the server stored");
    }

    /** The four movements, off a real session's inventory and a real chest. */
    private void aRealPlayerSessionTradesWithTheShop() {
        Player buyer = requireOnline(BUYER_BOT);

        Assert.eventually(15_000, "the buyer to be holding a diamond",
                () -> countOf(buyer, Material.DIAMOND) == 1);

        Assert.equal(1, countOf(buyer, Material.DIAMOND), "buyer should have received one diamond");
        Assert.equal(4, countOf(buyer, Material.EMERALD), "buyer should have paid one emerald");
        Assert.equal(1, countInChest(Material.EMERALD), "the emerald should be in the shop chest");
        Assert.equal(9, countInChest(Material.DIAMOND), "the shop should have one fewer diamond");
    }

    /**
     * {@code EditSubCommand}, and the {@code inventorygui} machinery underneath it.
     *
     * <p>Every tier below this one can only reach these by calling the handler
     * directly, which is simulating the click. Here the client typed
     * {@code /tradeshop edit} into chat while looking at the sign, and then clicked
     * two icons. The trail is the proof: {@code inventorygui} opens its next screen
     * only from inside a click handler, so "Edit Shop Settings" cannot be in the
     * list unless a real click packet reached {@code GUISubCommand}'s element, and
     * the menu cannot appear a second time unless the Save element's handler ran
     * {@code InventoryGui.goBack}.
     */
    private void theEditGuiOpensAndItsClicksReachTheShop() {
        String menu = "Edit Menu-";
        Assert.that(openedTitles.stream().anyMatch(t -> t.startsWith(menu)),
                "/tradeshop edit typed in chat should have opened the edit menu; the server opened "
                        + openedTitles);
        Assert.that(openedTitles.contains("Edit Shop Settings"),
                "a real click on the settings icon should have opened the settings screen; the server opened "
                        + openedTitles);

        int firstMenu = indexOfStartingWith(menu, 0);
        int settings = openedTitles.indexOf("Edit Shop Settings");
        Assert.that(settings > firstMenu,
                "the settings screen must open after the menu it is reached from, not before");
        Assert.that(indexOfStartingWith(menu, settings) > settings,
                "clicking Save runs InventoryGui.goBack, which reopens the menu - so a second click "
                        + "handler ran too. The server opened " + openedTitles);

        Assert.that(clicks.stream().anyMatch(c -> c.contains(OWNER_BOT) && c.contains("CRAFTING_TABLE")),
                "the settings icon is a crafting table and the client clicked it; clicks were " + clicks);
        Assert.that(clicks.stream().anyMatch(c -> c.contains(OWNER_BOT) && c.contains("ANVIL")),
                "Save is an anvil and the client clicked it; clicks were " + clicks);

        // The shop survived being edited through a GUI, which is the thing a user
        // would notice if it did not.
        Assert.that(shop() != null, "the shop should still be loadable after being edited through the GUI");
        assertSide(shop(), ShopItemSide.PRODUCT, Material.DIAMOND, 1);
        assertSide(shop(), ShopItemSide.COST, Material.EMERALD, 1);
    }

    /** {@code WhatSubCommand}: the read-only view of what a shop trades. */
    private void theWhatGuiShowsWhatTheShopTrades() {
        String title = OWNER_BOT + "'s Shop";
        Assert.that(openedTitles.contains(title),
                "/tradeshop what should have opened a window named \"" + title
                        + "\"; the server opened " + openedTitles);
        Assert.that(openedTitles.contains("View Product Item"),
                "clicking the product in the what screen opens it read-only, so the title says View "
                        + "and not Edit; the server opened " + openedTitles);
        Assert.that(openedTitles.indexOf("View Product Item") > openedTitles.indexOf(title),
                "the item view must open after the what screen it is reached from");
    }

    /**
     * A per-item comparison turned off with a click, and the trade that answer
     * decides.
     *
     * <p>This is the only row in the project where the two halves meet. The tier-1
     * matrix switches all fifteen settings off and back on by calling
     * {@code setShopSettings} - which is the same call the GUI's own state element
     * makes ({@code GUISubCommand.java:256}) - and the tier-2 rows prove one
     * survives a save and a reload. Neither can say that a <em>click</em> reaches
     * that call, that Save writes it to the shop the trade gate reads, or that a
     * shop owner's trade changes as a result. All three need a real client in front
     * of a real inventory, and this is the step that has one.
     *
     * <p>What the client did, in order: made the shop's cost a named emerald,
     * right-clicked the sign as the buyer holding plain ones, opened
     * {@code /tradeshop edit} and clicked through to the cost item's Compare Name
     * toggle, saved, and right-clicked the sign again. Nothing here performed any
     * of it.
     *
     * <p>The flip is read off {@link #signClicks} rather than off the final item
     * counts, because the counts alone cannot say which of the two attempts was the
     * one that moved anything - and a toggle that broke the trade rather than
     * enabling it would leave exactly the same totals.
     */
    private void aGuiToggledComparisonChangesWhatTheShopAccepts() {
        // The precondition that would otherwise make every assertion below vacuous:
        // ShopItemStack.java:287 reads a per-item override only while the config
        // says the setting is user-editable, and ShopItemStackSettingKeys.java:126
        // answers false for a key the file does not carry. A GUI that wrote the
        // toggle perfectly would still change nothing.
        Assert.that(ShopItemStackSettingKeys.COMPARE_NAME.isUserEditable(),
                "COMPARE_NAME is not user-editable on this server's config, so the GUI would not even "
                        + "offer the toggle (GUISubCommand.java:244) and ShopItemStack.java:287 would "
                        + "ignore it if it did");
        Assert.that(ShopItemStackSettingKeys.COMPARE_NAME.getDefaultValue().asBoolean(),
                "the server-wide default for COMPARE_NAME is off, so a false on the item below would "
                        + "say nothing about the click");

        Shop shop = shop();
        Assert.that(shop != null, "the shop should still be loadable after being edited through the GUI");

        List<ShopItemStack> cost = shop.getSideList(ShopItemSide.COST);
        Assert.equal(1, cost.size(), "the cost side should hold exactly one item");
        ItemStack asked = cost.get(0).getItemStack();
        Assert.equal(Material.EMERALD, asked.getType(), "the shop should still be paid in emeralds");
        Assert.that(asked.hasItemMeta() && asked.getItemMeta().hasDisplayName(),
                "the client set the cost from the named emerald in its hand, so the shop's own item "
                        + "carries a name - which is the difference the toggle decides");
        Assert.equal(TAGGED_COST_NAME, asked.getItemMeta().getDisplayName(),
                "and it is the name the harness handed over");

        // 1. The setting the click wrote.
        Assert.that(!cost.get(0).getShopSetting(ShopItemStackSettingKeys.COMPARE_NAME).asBoolean(),
                "a real click on the Compare Name toggle should have reached the GuiStateElement at "
                        + "GUISubCommand.java:255 and left the setting off on the shop's cost item; the "
                        + "windows the server opened were " + openedTitles + " and the clicks were " + clicks);
        Assert.that(cost.get(0).getShopSetting(ShopItemStackSettingKeys.COMPARE_LORE).asBoolean(),
                "and it should have left every other setting alone - a screen that saved the whole map "
                        + "from its own defaults would look identical until one of them mattered");

        // 2. And it is on disk, not only in the shop the running plugin holds. A
        // brand new DataStorage has an empty shopCache, so this read comes off the
        // file the Save button wrote.
        Shop reloaded = Sync.get(plugin, () -> new DataStorage(DataType.FLATFILE)
                .loadShopFromSign(new ShopLocation(signBlock.getLocation())));
        Assert.that(reloaded != null, "the shop the GUI saved should be readable off disk");
        Assert.that(!reloaded.getSideList(ShopItemSide.COST).get(0)
                        .getShopSetting(ShopItemStackSettingKeys.COMPARE_NAME).asBoolean(),
                "and the toggle has to survive the write, or it lasts until the next restart");

        // 3. The click chain, from the windows the server opened rather than from
        // anything the bot said. inventorygui opens its next screen only from inside
        // a click handler, so these titles cannot appear without a real click packet
        // having reached one.
        Assert.that(openedTitles.contains("Edit Costs"),
                "a real click on the cost icon should have opened the cost list; the server opened "
                        + openedTitles);
        Assert.that(openedTitles.contains("Edit Cost Item"),
                "and a click on the emerald in it should have opened that item's settings; the server "
                        + "opened " + openedTitles);
        Assert.that(openedTitles.indexOf("Edit Cost Item") > openedTitles.indexOf("Edit Costs"),
                "the item screen must open after the list it is reached from, not before");
        // A redstone block, not an emerald one, and that is the assertion rather
        // than an accident of it. GuiStateElement flips its state from inside the
        // click handler and redraws the window there and then, so the MONITOR
        // listener that records these lines reads the slot AFTER the swap - it sees
        // what the click produced, not what it found. The toggle was an emerald
        // block (on) when the client clicked it and a redstone block (off) by the
        // time the line was written, which is the flip itself showing up in the
        // trail. Measured on this server; the two blocks are getBooleanItem's own
        // (GUISubCommand.java:288).
        Assert.that(clicks.stream().anyMatch(c -> c.contains(OWNER_BOT)
                        && c.contains("REDSTONE_BLOCK") && c.contains("Edit Cost Item")),
                "the client clicked the Compare Name toggle and the screen should have redrawn it as "
                        + "the off block inside that same click; clicks were " + clicks);
        Assert.that(clicks.stream().anyMatch(c -> c.contains(OWNER_BOT)
                        && c.contains("ANVIL") && c.contains("Edit Cost Item")),
                "and Save on that screen is an anvil, which is what calls Shop.updateSideItem; clicks "
                        + "were " + clicks);

        // 4. The trade, which is the only reason any of the above matters.
        List<SignClick> attempts = signClicks.stream()
                .filter(click -> BUYER_BOT.equals(click.player()))
                .toList();
        Assert.equal(3, attempts.size(),
                "the buyer right-clicked this sign three times in this run - once to trade, once while "
                        + "Compare Name was on, once after it was turned off. Recorded: " + attempts);
        Assert.equal(1, attempts.get(0).moved(),
                "the first click traded, which is what says this recorder works at all");
        Assert.equal(0, attempts.get(1).moved(),
                "the second must have moved nothing: the shop was asking for a named emerald, the buyer "
                        + "held plain ones, and COMPARE_NAME was on. Recorded: " + attempts);
        Assert.equal(1, attempts.get(2).moved(),
                "and the third must have traded, with nothing changed but a click on a toggle. That "
                        + "difference is the whole row. Recorded: " + attempts);

        Player buyer = requireOnline(BUYER_BOT);
        Assert.equal(2, countOf(buyer, Material.DIAMOND),
                "so the buyer leaves with two diamonds from three attempts");
        Assert.equal(3, countOf(buyer, Material.EMERALD), "and has paid two emeralds of its five");
        Assert.equal(2, countInChest(Material.EMERALD), "which are both in the shop's chest");
        Assert.equal(8, countInChest(Material.DIAMOND), "and the shop is two diamonds down");
    }

    /**
     * The one row of the item-metadata matrix that belongs at this tier.
     *
     * <p>Everything about the comparator is settled at tiers 1 and 2, and repeating
     * it here would buy nothing. What only exists here is the <b>path</b>: an item
     * with a name, a lore line and an enchantment travels from a real client's hand,
     * through the network item codec, into {@code ShopItemSubCommand.setSide}'s
     * {@code getItemInMainHand().clone()}, into the shop - and then back out through
     * {@code inventorygui} as an icon a client can be shown. Nothing below this tier
     * has a hand to hold an item in, or a screen to draw it on.
     *
     * <p>The GUI half is asserted from the windows the <em>server</em> opened, not
     * from anything the bot says: {@code inventorygui} only opens its next screen
     * from inside a click handler, so a second "View Product Item" in the trail is
     * proof that a real click packet reached the handler while this item was the
     * product.
     */
    private void aHeldComplexItemBecomesTheProductAndRenders() {
        Shop shop = shop();
        Assert.that(shop != null, "the shop should still be loadable");

        List<ShopItemStack> product = shop.getSideList(ShopItemSide.PRODUCT);
        Assert.equal(1, product.size(), "the product side should hold exactly one item");

        ItemStack held = product.get(0).getItemStack();
        Assert.equal(Material.DIAMOND_SWORD, held.getType(),
                "/tradeshop setProduct with no arguments takes the item in the client's hand");
        Assert.that(held.hasItemMeta(),
                "the metadata should have survived the trip from the client's hand into the shop");
        Assert.equal(PRODUCT_NAME, held.getItemMeta().getDisplayName(),
                "the display name should have survived the trip from the client's hand");
        Assert.equal(List.of(PRODUCT_LORE), held.getItemMeta().getLore(),
                "the lore should have survived the trip from the client's hand");
        Assert.equal(3, held.getItemMeta().getEnchantLevel(Enchantment.SHARPNESS),
                "the enchantment should have survived the trip from the client's hand");

        // And the GUI drew it. Both titles are already in the trail once, from
        // theWhatGuiShowsWhatTheShopTrades, so it is the SECOND "View Product Item"
        // that belongs to this step. The shop's own title is NOT counted: closing
        // the item view runs InventoryGui.goBack and reopens it, so it appears
        // twice per visit rather than once - measured, not assumed.
        String title = OWNER_BOT + "'s Shop";
        Assert.that(openedTitles.contains(title),
                "/tradeshop what should have opened the shop's window; the server opened " + openedTitles);
        Assert.equal(2, occurrences("View Product Item"),
                "clicking the product a second time should have opened it read-only again; the server "
                        + "opened " + openedTitles);
        Assert.that(openedTitles.lastIndexOf("View Product Item") > openedTitles.indexOf(title),
                "the item view must open after the what screen it is reached from");

        // The strongest assertion of the three, and the one that ties the GUI to
        // THIS item: the icon the client found and clicked was a diamond sword,
        // which it only can be if the shop's product is now the sword and the
        // server drew it into the window.
        Assert.that(clicks.stream().anyMatch(c -> c.contains(OWNER_BOT) && c.contains("DIAMOND_SWORD")),
                "the product icon is now the sword and the client clicked it; clicks were " + clicks);
    }

    private int occurrences(String title) {
        return (int) openedTitles.stream().filter(title::equals).count();
    }

    // ------------------------------------------------------------------
    // Reading the world back
    // ------------------------------------------------------------------

    private Shop shop() {
        return Sync.get(plugin, () -> Shop.loadShop(new ShopLocation(signBlock.getLocation())));
    }

    /** The sign's lines as the server holds them, colour stripped. Never the event's. */
    private String[] signLines() {
        return Sync.get(plugin, () -> {
            if (!(signBlock.getState() instanceof Sign sign)) {
                throw new AssertionError("there is no sign at " + signBlock.getLocation()
                        + ", it is " + signBlock.getType());
            }
            String[] raw = sign.getSide(Side.FRONT).getLines();
            String[] clean = new String[raw.length];
            for (int i = 0; i < raw.length; i++) {
                clean[i] = strip(raw[i]);
            }
            return clean;
        });
    }

    private int countInChest(Material material) {
        return Sync.get(plugin, () -> count(((Container) chestBlock.getState()).getInventory().getContents(), material));
    }

    private int countOf(Player player, Material material) {
        return Sync.get(plugin, () -> count(player.getInventory().getContents(), material));
    }

    private static int count(ItemStack[] contents, Material material) {
        int total = 0;
        for (ItemStack stack : contents) {
            if (stack != null && stack.getType() == material) {
                total += stack.getAmount();
            }
        }
        return total;
    }

    private void assertSide(Shop shop, ShopItemSide side, Material material, int amount) {
        List<ShopItemStack> items = shop.getSideList(side);
        Assert.equal(1, items.size(), "the " + side.name().toLowerCase(Locale.ROOT)
                + " side should hold exactly one item");
        Assert.equal(material, items.get(0).getItemStack().getType(),
                "the " + side.name().toLowerCase(Locale.ROOT) + " material comes from the line the client typed");
        Assert.equal(amount, items.get(0).getAmount(),
                "the " + side.name().toLowerCase(Locale.ROOT) + " amount comes from the line the client typed");
    }

    private Player requireOnline(String name) {
        Player player = Sync.get(plugin, () -> Bukkit.getPlayerExact(name));
        Assert.that(player != null, "no client is logged in as " + name + "; online now: "
                + Sync.get(plugin, () -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toList()));
        Assert.that(player.isOnline(), name + " is not online");
        return player;
    }

    /** Offline-mode UUIDs are derived, which is how a bot's name resolves to an owner. */
    private static UUID uuidOf(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private int indexOfStartingWith(String prefix, int from) {
        List<String> snapshot = new ArrayList<>(openedTitles);
        for (int i = from + 1; i < snapshot.size(); i++) {
            if (snapshot.get(i).startsWith(prefix)) {
                return i;
            }
        }
        return -1;
    }

    private static String strip(String coloured) {
        return coloured == null ? null : ChatColor.stripColor(coloured);
    }

    private static String describe(ItemStack[] contents) {
        List<String> held = new ArrayList<>();
        for (ItemStack stack : contents) {
            if (stack != null && stack.getType() != Material.AIR) {
                held.add(stack.getAmount() + "x" + stack.getType().name());
            }
        }
        return held.isEmpty() ? "nothing" : String.join(", ", Collections.unmodifiableList(held));
    }
}
