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
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.BlockFace;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * A chest with a TradeShop sign on it, on a real server, and the moves a player
 * would make to use it.
 *
 * <p>Same vocabulary as the tier-1 harness on purpose - {@code createShop},
 * {@code stockShop}, {@code closeChestAsOwner}, {@code rightClickSign},
 * {@code countInChest} - so that a divergence between the two tiers reads as a
 * divergence rather than as two unrelated tests.
 *
 * <h2>Isolation</h2>
 * Every scenario gets its own patch of the world, a thousand blocks from the
 * last. TradeShop keys every shop, chest linkage and protection entry by world
 * name plus coordinates, so coordinates nobody has used are a namespace nobody
 * has written to - which matters more here than in tier 1, because here the data
 * store is a real file that survives the scenario.
 *
 * <h2>Threads</h2>
 * Scenarios run off the main thread so that they can wait for the server without
 * stopping it, and every call that touches the world is marshalled back onto the
 * main thread. Waiting is the point: TradeShop reads stock on
 * {@code InventoryCloseEvent} and does disk work on the main thread, so an
 * assertion can genuinely arrive before the plugin has caught up.
 */
final class RealShop {

    private static final int CHEST_Y = 0;

    private final Plugin plugin;
    private final int index;

    private Block chestBlock;
    private Block signBlock;
    private Player owner;

    /**
     * Where a scenario's ground comes from. There is no table here any more:
     * the register this javadoc used to carry was a list every suite had to be
     * edited into by hand, and the two defects below are both what happened
     * when one was not. {@link SiteAllocator} hands out the ranges instead, so
     * the agreement is kept by the runtime rather than by this comment.
     *
     * <p><b>A site is a namespace, and two rows on one site is not a near miss.</b>
     * TradeShop keys every shop, chest linkage and protection entry by world name
     * plus coordinates, and the world outlives the scenario. The second row to
     * arrive finds whatever the first left: {@code IntegrationPlugin}'s sign-break
     * control and {@code ConfigAndMetricsRows}' chunk-count row both took 41, the
     * control left a sign standing that still read as a shop, and
     * {@code Utils.createShop:515} refused the second row's shop with
     * "existing-shop" - a red row whose message pointed at the counting code,
     * which was correct all along. {@code IssueRows} and {@code SettingToggleRows}
     * separately both declared 30, each with a comment naming the other suites it
     * had checked against, and neither comment named the other.
     *
     * <p>A new suite reserves its own range from {@link SiteAllocator} and
     * addresses it by offset. Nothing here can stop a suite passing a literal
     * instead - Java cannot refuse an {@code int} for where it came from - but a
     * suite that does ask can no longer be handed ground another suite holds.
     *
     * @param index the scenario's patch of the world, at x = {@code index * 1000}.
     *              Every suite gets this from a {@link SiteAllocator.Reservation}
     *              rather than choosing it by hand - see {@link SiteAllocator}
     *              for why a hand-agreed range was not enough - except the
     *              scenarios declared directly in {@code IntegrationPlugin},
     *              which predate that class and are reserved there as one
     *              block instead.
     * @throws IllegalArgumentException if {@code index} is
     *              {@link SiteAllocator#TIER_3_CLIENT}: that site belongs to
     *              {@link ClientPhase}, which builds it directly rather than
     *              through this class, and a scene left there would leave
     *              blocks the client's {@code getHighestBlockYAt} would trip
     *              over
     */
    RealShop(Plugin plugin, int index) {
        if (index == SiteAllocator.TIER_3_CLIENT) {
            throw new IllegalArgumentException("site " + index + " is reserved for tier 3's client ("
                    + "ClientPhase, x=" + (SiteAllocator.TIER_3_CLIENT * 1000) + "); a scene built there "
                    + "would leave blocks behind that move the client's site out from under the bot when "
                    + "it next asks getHighestBlockYAt where the ground is");
        }
        this.plugin = plugin;
        this.index = index;
    }

    // ------------------------------------------------------------------
    // Setup
    // ------------------------------------------------------------------

    void placeChestAndSign() {
        placeChestAndSign(Material.OAK_SIGN);
    }

    /**
     * As {@link #placeChestAndSign()}, on a sign of the caller's choosing.
     *
     * <p>The wood and the mounting are the whole point of some scenarios: this
     * server is newer than the one tier 1 mocks, so it is the only place a pale
     * oak sign or a hanging sign exists at all.
     */
    void placeChestAndSign(Material signMaterial) {
        placeStorageAndSign(Material.CHEST, signMaterial);
    }

    /**
     * As {@link #placeChestAndSign()}, on a storage block of the caller's
     * choosing.
     *
     * <p>{@code ShopStorage.Storages} permits ten block types and they do not all
     * hold the same number of slots - a hopper and a brewing stand hold five
     * rather than a multiple of nine. Which of them a shop can actually be built
     * on is a property of the running server's blocks, so it is asked here rather
     * than assumed.
     *
     * <p>Both blocks are cleared to air first, and that is load-bearing rather
     * than tidy. {@code CraftBlock.setTypeAndData} only drops the old block
     * entity when the block itself changes - {@code blockData.getBlock() !=
     * old.getBlock()} - so setting {@code OAK_SIGN} over an {@code OAK_SIGN} that
     * is already there leaves its text exactly where it was, and setting
     * {@code CHEST} over a {@code CHEST} leaves that chest's contents and
     * persistent data. A scene that started on ground another row had used was
     * therefore starting on that row's sign, still reading as a shop. Placing air
     * changes the block, so the entity goes.
     */
    void placeStorageAndSign(Material storageMaterial, Material signMaterial) {
        run(() -> {
            World world = Bukkit.getWorlds().get(0);
            int x = index * 1000;

            chestBlock = world.getBlockAt(x, CHEST_Y, 0);
            signBlock = world.getBlockAt(x, CHEST_Y + 1, 0);

            chestBlock.setType(Material.AIR, false);
            signBlock.setType(Material.AIR, false);

            // Physics off: a standing sign with nothing solid under it pops off
            // as an item the moment the server ticks the block, and the chest is
            // placed in the same breath. A hanging sign with nothing above it
            // would go the same way, which is the other reason this is false.
            chestBlock.setType(storageMaterial, false);
            signBlock.setType(signMaterial, false);

            owner = HarnessPlayer.create("owner" + index, chestBlock.getLocation().add(0.5, 1, 1.5), signBlock);
        });
    }

    // ------------------------------------------------------------------
    // The three flows
    // ------------------------------------------------------------------

    /**
     * Creates a shop the way a player creates one from the chat bar.
     *
     * <p>Three commands, because that is what it takes: {@code /tradeshop create}
     * makes a shop with no items in it - {@code INCOMPLETE} - and
     * {@code setProduct} and {@code setCost} fill the two sides. Each runs
     * TradeShop's whole
     * {@code CommandCaller -> CommandType -> SubCommand -> runner} chain; see
     * {@link #dispatch} for the one piece of server plumbing that is stepped
     * around, and why.
     *
     * <p>This path, unlike a sign edit, writes the sign itself:
     * {@code Utils.createShop} with no {@code SignChangeEvent} to decorate calls
     * {@code Shop.updateSign(Sign)}, which calls {@code sign.update()} on the
     * real block. Every later sign update follows the same route. So the sign
     * assertions in these scenarios read what TradeShop actually wrote to the
     * world, with nothing copied back by the harness.
     */
    void createShopByCommand(String product, String cost) {
        dispatch("create");
        dispatch(("setProduct " + product).split(" "));
        dispatch(("setCost " + cost).split(" "));
    }

    /**
     * Runs {@code /tradeshop <args>} as the shop's owner.
     *
     * <p>Straight at the {@link PluginCommand}, not through
     * {@code Bukkit.dispatchCommand}. Paper routes chat commands through its
     * Brigadier dispatcher, which turns the sender into a vanilla command
     * listener first and refuses anything that is not a connected player:
     * "Cannot make HarnessPlayer(owner3) a vanilla command listener". That
     * wrapper is the server's dispatch plumbing; everything this scenario is
     * about is downstream of it. Calling the plugin command directly runs the
     * whole of TradeShop's own chain - permission check, {@code CommandCaller},
     * {@code CommandType} lookup, {@code SubCommand}, runner - and nothing of
     * it is stubbed.
     *
     * <p>Package-visible so that a scenario can run one subcommand rather than
     * the three {@link #createShopByCommand} runs: {@code create} on its own is
     * how a shop is left INCOMPLETE, and there is no other way to reach that
     * state without reaching into the shop object and setting it.
     */
    void dispatch(String... args) {
        PluginCommand command = Bukkit.getPluginCommand("tradeshop");
        if (command == null) {
            throw new AssertionError("the server does not know /tradeshop, so TradeShop did not "
                    + "register its command");
        }
        run(() -> command.execute(owner, "tradeshop", args));
    }

    /**
     * Line 1 is the product the shop gives, line 2 the cost it takes.
     *
     * <p>The sign block is left blank and only the event carries the text, which
     * is the state a real server is in while the event runs. CraftBukkit's
     * packet handler builds the new sign text from the packet, fires
     * {@link SignChangeEvent} with it, and writes it onto the block
     * <em>afterwards</em>, only if nothing cancelled the event. The block still
     * holds its old contents while every listener runs.
     *
     * <p>Getting this backwards is not a detail: writing the header onto the
     * block first makes {@code ShopProtectionListener} - which cancels an edit
     * to a block that is already a shop sign - cancel the very edit that was
     * meant to create the shop, and no shop is created at all.
     *
     * <p>Nothing here copies the event's finished lines back onto the block.
     * That is the step the tier-1 harness has to perform by hand, and leaving it
     * out is what makes the later assertions about sign contents mean something:
     * anything on this sign got there because TradeShop wrote it through the
     * server.
     */
    void createShop(String header, String product, String cost) {
        run(() -> Bukkit.getPluginManager().callEvent(
                new SignChangeEvent(signBlock, owner, new String[]{header, product, cost, ""})));
    }

    /**
     * As {@link #createShop}, on the side of the sign the caller names.
     *
     * <p>A sign has had two sides since 1.20 and {@link SignChangeEvent} has
     * carried which one is being edited for just as long. The three-argument
     * constructor {@link #createShop} uses is the pre-1.20 one and answers
     * {@link Side#FRONT} for {@code getSide()}, so it cannot express a back-side
     * edit at all - which is exactly the edit a player makes by walking round a
     * sign and typing on the other face.
     *
     * <p>Callers pass {@link Side#FRONT} explicitly rather than reusing
     * {@link #createShop} when the point of the scenario is the side: a control
     * that differs from its subject in the constructor as well as in the
     * argument is not a control.
     */
    void createShopOn(Side side, String header, String product, String cost) {
        run(() -> Bukkit.getPluginManager().callEvent(
                new SignChangeEvent(signBlock, owner, new String[]{header, product, cost, ""}, side)));
    }

    /**
     * The owner puts the product in the chest.
     *
     * <p>No {@code update()} afterwards, and that is not an omission. A
     * {@code BlockState} obtained from a placed block is a snapshot of the
     * block's data but hands out the <em>live</em> inventory, so the items are
     * already in the chest by the time {@code addItem} returns - and calling
     * {@code update()} would write the stale snapshot back over them, emptying
     * the chest again. The tier-1 harness calls {@code update()} because
     * MockBukkit's block states behave the other way round.
     */
    void stockShop(ItemStack stock) {
        run(() -> ((Container) chestBlock.getState()).getInventory().addItem(stock));
    }

    /**
     * The owner shuts the chest, which is the moment the plugin re-reads stock.
     */
    void closeChestAsOwner() {
        run(() -> Bukkit.getPluginManager().callEvent(new InventoryCloseEvent(
                HarnessPlayer.viewOf(owner, ((Container) chestBlock.getState()).getInventory()))));
    }

    Player buyerHolding(ItemStack held) {
        return get(() -> {
            Player buyer = HarnessPlayer.create("buyer" + index, signBlock.getLocation().add(0.5, 0, 1.5));
            buyer.getInventory().addItem(held);
            return buyer;
        });
    }

    void rightClickSign(Player player) {
        run(() -> Bukkit.getPluginManager().callEvent(new PlayerInteractEvent(
                player, Action.RIGHT_CLICK_BLOCK, null, signBlock, BlockFace.NORTH)));
    }

    // ------------------------------------------------------------------
    // Reading the world back
    // ------------------------------------------------------------------

    /**
     * The sign's lines as the server holds them, colour stripped.
     *
     * <p>Read from the block, never from the event. The tier-1 harness has to
     * copy a {@code SignChangeEvent}'s finished lines onto the block itself
     * because MockBukkit never does; this reads whatever the server actually
     * stored.
     */
    String[] signLines() {
        return signLines(Side.FRONT);
    }

    /** As {@link #signLines()}, reading the side the caller names. */
    String[] signLines(Side side) {
        return get(() -> {
            String[] raw = ((Sign) signBlock.getState()).getSide(side).getLines();
            String[] clean = new String[raw.length];
            for (int i = 0; i < raw.length; i++) {
                clean[i] = ChatColor.stripColor(raw[i]);
            }
            return clean;
        });
    }

    /** Everything the plugin has said to this scene's owner, colour stripped. */
    java.util.List<String> ownerWasTold() {
        return HarnessPlayer.heardBy(owner);
    }

    int countOf(Player player, Material material) {
        return get(() -> rawCountOf(player, material));
    }

    int countInChest(Material material) {
        return get(() -> rawCountInChest(material));
    }

    /**
     * As {@link #countOf}, but already on the server thread.
     *
     * <p>Separate because {@link #onServer} runs its condition on the main
     * thread: a marshalling call nested inside a marshalled block would be
     * waiting for the thread it is already on.
     */
    int rawCountOf(Player player, Material material) {
        return count(player.getInventory().getContents(), material);
    }

    int rawCountInChest(Material material) {
        return count(((Container) chestBlock.getState()).getInventory().getContents(), material);
    }

    Block signBlock() {
        return signBlock;
    }

    Block chestBlock() {
        return chestBlock;
    }

    /** The chest's live inventory, exactly as {@link #closeChestAsOwner} passes it. */
    org.bukkit.inventory.Inventory chestInventory() {
        return ((Container) chestBlock.getState()).getInventory();
    }

    Player owner() {
        return owner;
    }

    private int count(ItemStack[] contents, Material material) {
        int total = 0;
        for (ItemStack stack : contents) {
            if (stack != null && stack.getType() == material) {
                total += stack.getAmount();
            }
        }
        return total;
    }

    // ------------------------------------------------------------------
    // Main-thread marshalling
    // ------------------------------------------------------------------

    void run(Runnable body) {
        get(() -> {
            body.run();
            return null;
        });
    }

    /**
     * Runs {@code body} on the server's main thread and waits for its answer.
     *
     * <p>Everything in this class goes through here. The scenario thread is not
     * the server thread, and Bukkit will refuse - loudly, which is the right
     * behaviour - any world access from anywhere else.
     */
    <T> T get(Callable<T> body) {
        return Sync.get(plugin, body);
    }

    /** A condition evaluated on the main thread, for {@link Assert#eventually}. */
    java.util.function.BooleanSupplier onServer(Supplier<Boolean> condition) {
        return () -> get(condition::get);
    }
}
