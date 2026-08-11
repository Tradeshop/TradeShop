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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSignOpenEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredListener;
import org.shanerx.tradeshop.item.ShopItemSide;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shop.ShopStatus;
import org.shanerx.tradeshop.shop.ShopType;
import org.shanerx.tradeshop.shoplocation.ShopLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * The rows for defects reported on the upstream tracker.
 *
 * <h2>Why they are here and not in {@code src/test}</h2>
 * Each turns on something a mock does not have:
 *
 * <ul>
 *   <li><b>#160</b> is a {@code BlockState} for air. The whole defect is that
 *       {@code getBlock().getState()} answers a live object for a block that is
 *       not there, so a mock that answered null - or threw - would hide it. The
 *       repair a shop owner takes runs inside a real {@code BlockPlaceEvent}
 *       listener as well.</li>
 *   <li><b>#152</b> is about which guard is doing the work, and the guard it
 *       replaced was a listener for a Paper-only event. Which events a running
 *       server actually delivers, and which handlers are registered for them,
 *       cannot be asked anywhere a real server is not running.</li>
 * </ul>
 *
 * <h2>What is not claimed</h2>
 * There is no Spigot server in this harness and BuildTools is out of scope, so
 * nothing below proves how Spigot behaves. What the #152 rows prove is
 * TradeShop's own decision: that both guards on a shop sign are taken through
 * APIs every server this plugin supports has - {@code PlayerInteractEvent} for
 * the block interaction and {@code org.bukkit.event.player.PlayerSignOpenEvent}
 * for the editor - rather than through Paper's fork-only event.
 */
final class IssueRows {

    private IssueRows() {
    }

    /**
     * These rows' patch of the world. Reserved through {@link SiteAllocator}
     * rather than agreed by comment - this suite and {@code SettingToggleRows}
     * both once wrote a comment exactly like this one claiming 30, and neither
     * comment named the other.
     */
    private static final SiteAllocator.Reservation SITE = SiteAllocator.reserve("IssueRows", 10);

    static List<IntegrationPlugin.Scenario> rows(IntegrationPlugin plugin) {
        List<IntegrationPlugin.Scenario> rows = new ArrayList<>();

        // ------------------------------------------------------------------
        // #160, "Error After Removing a Linked Chest".
        //
        // Reported verbatim: create a shop, add cost, add product, break the
        // chest. The next thing that touches the shop throws
        //
        //   NullPointerException: Cannot invoke
        //   "org.bukkit.inventory.Inventory.getStorageContents()"
        //   because "shopInventory" is null
        //
        // Shop.hasStorage:646 is `getStorage() != null` and Shop.getStorage:624
        // is `getInventoryLocation().getBlock().getState()`. AN AIR BLOCK STILL
        // HAS A BLOCKSTATE, so the guard answers true for a chest that is gone,
        // Shop.updateFullTradeCount:741 walks past it, and :746 asks
        // getChestAsSC().getInventory() - which is null, because
        // ShopChest.getBlock:142 assigns its block only when the location holds
        // an inventory, and ShopChest.getInventory:165-172 catches the resulting
        // NPE and answers null. :748 dereferences it.
        //
        // Only reachable with a real world. The whole defect is that
        // getBlock().getState() answers a live object for a block that is not
        // there, so a mock that answered null - or threw - would hide it, and
        // the repair a shop owner takes runs inside a real BlockPlaceEvent.
        //
        // WHAT A SHOP WITH NO STORAGE SHOULD DO is a choice, not a lookup, and
        // the choice asserted below is: it becomes INCOMPLETE, keeps both sides
        // of its trade, and waits for its owner to put a storage block back.
        // Two reasons, both already in the plugin rather than invented here.
        // ShopStatus.INCOMPLETE already means "this shop is missing something it
        // needs", ShopTradeListener:129 already answers it with SHOP_EMPTY and
        // :108-111 already answers a null storage with MISSING_CHEST - none of
        // which is reachable while the guard lies. And
        // ShopProtectionListener.onBlockPlace:409 already holds the repair: an
        // owner placing a storage block under the sign is re-linked to it IF AND
        // ONLY IF !shop.hasStorage(). With the guard always true that branch is
        // dead code, so fixing the guard is what turns the repair back on.
        //
        // Not CLOSED: CLOSED is a thing an owner chooses, updateStatus:683
        // deliberately refuses to move a shop out of it, and a shop that had
        // been repaired would stay shut. Not removed: the shop's item lists are
        // the owner's work, and a vanished block is not consent to destroy them.
        // ------------------------------------------------------------------
        rows.add(new IntegrationPlugin.Scenario("aShopWhoseStorageBlockIsGoneAsksForRepairRatherThanThrowing", () -> {
            RealShop scene = new RealShop(plugin, SITE.at(0));
            scene.placeChestAndSign();
            scene.createShopByCommand("1 DIAMOND", "1 EMERALD");
            scene.stockShop(new ItemStack(Material.DIAMOND, 10));
            scene.closeChestAsOwner();

            ShopLocation where = scene.get(() -> new ShopLocation(scene.signBlock().getLocation()));

            Assert.eventually(15_000, "precondition: the shop is open before its chest goes",
                    scene.onServer(() -> Shop.loadShop(where).getStatus() == ShopStatus.OPEN));

            // The reporter's fourth step, through the server's own listeners.
            // The event fires while the chest is still a chest, which is why the
            // break does not throw; the block is set to air afterwards because
            // that is what the server does next, and it is the state the shop is
            // left pointing at.
            scene.run(() -> {
                Bukkit.getPluginManager().callEvent(new BlockBreakEvent(scene.chestBlock(), scene.owner()));
                scene.chestBlock().setType(Material.AIR, false);
            });

            Shop shop = scene.get(() -> Shop.loadShop(where));
            Assert.that(shop != null, "breaking the chest must not take the shop record with it");

            // The reported exception, at the two calls its stack names. Called
            // rather than driven through an event on purpose: Bukkit's event bus
            // catches what a listener throws and logs it, so an NPE reached that
            // way would fail this run as an unexplained severe console line
            // instead of as this row.
            try {
                scene.run(shop::updateFullTradeCount);
            } catch (Throwable t) {
                throw new AssertionError("counting the trades of a shop whose storage block is gone "
                        + "must not throw, and it threw " + rootCause(t) + " - "
                        + "Shop.updateFullTradeCount:741 asks hasStorage(), which is "
                        + "getStorage():624 != null, and getBlock().getState() answers a BlockState "
                        + "for air as readily as for a chest", t);
            }

            try {
                scene.run(shop::saveShop);
            } catch (Throwable t) {
                throw new AssertionError("and neither must saving it - Shop.saveShop:488 calls "
                        + "updateFullTradeCount:494, which is how every mutation in the plugin "
                        + "reaches it: " + rootCause(t), t);
            }

            Assert.that(!scene.get(shop::hasStorage),
                    "a shop whose storage block has been removed must report that it has none");

            scene.run(shop::updateStatus);
            Assert.equal(ShopStatus.INCOMPLETE, shop.getStatus(),
                    "a shop with nothing to trade out of is incomplete, not out of stock - "
                            + "updateStatus:684 asks only whether chestLoc was ever set");
            Assert.equal(0, shop.getAvailableTrades(), "and it can make no trades at all");

            // The owner's work is not collateral. Both sides survive, so putting
            // a chest back is a repair rather than a rebuild.
            Assert.equal(1, shop.getSideList(ShopItemSide.PRODUCT).size(),
                    "the product side must survive the chest");
            Assert.equal(1, shop.getSideList(ShopItemSide.COST).size(),
                    "and so must the cost side");

            // The repair, taken the way a player takes it: place a chest under
            // the sign.
            scene.run(() -> {
                Block block = scene.chestBlock();
                BlockState replaced = block.getState();
                block.setType(Material.CHEST, false);
                Bukkit.getPluginManager().callEvent(new BlockPlaceEvent(block, replaced,
                        block.getRelative(BlockFace.DOWN), new ItemStack(Material.CHEST),
                        scene.owner(), true, EquipmentSlot.HAND));
            });

            Shop repaired = scene.get(() -> Shop.loadShop(where));
            Assert.that(scene.get(repaired::hasStorage),
                    "an owner who puts a storage block back under the sign must get their shop back "
                            + "- ShopProtectionListener.onBlockPlace:409 re-links it only when the "
                            + "shop says it has no storage, so a guard that always says it has one "
                            + "leaves the shop unrepairable");

            scene.stockShop(new ItemStack(Material.DIAMOND, 10));
            scene.closeChestAsOwner();

            Assert.eventually(15_000, "a repaired and restocked shop to open again",
                    scene.onServer(() -> Shop.loadShop(where).getStatus() == ShopStatus.OPEN));
        }));

        // ------------------------------------------------------------------
        // #152, first half: a shop sign stays editable.
        //
        // There were two guards and neither held. One was
        // PaperShopProtectionListener cancelling PlayerOpenSignEvent - Paper-only
        // API, registered from a version string that had stopped matching Paper,
        // so absent on Spigot AND absent on the server it was written for; the
        // row below the next one is what replaced it. The other is
        // ShopTradeListener:150's e.setCancelled(true), and it sits BELOW every
        // early return in that method: no shop (:88), a storage block that is
        // gone (:108), an illegal item (:114), CLOSED (:126), INCOMPLETE (:129),
        // OUT_OF_STOCK (:132) and a cancelled PlayerPrepareTradeEvent (:148) all
        // return before it. So the protection a player actually gets depends on
        // what the shop happens to be doing at the time.
        //
        // Asserted as the interact event's BLOCK result rather than as
        // isCancelled(), because the block result is what the server acts on: a
        // sign opens its editor out of the block's own interaction, and a DENY
        // there is what stops it. It is also narrower than setCancelled(true),
        // which would deny the item in hand as well and stop a player placing a
        // block against a shop sign.
        //
        // WHAT THIS DOES NOT CLAIM: there is no Spigot server in this harness
        // and BuildTools is out of scope, so nothing here proves how Spigot
        // behaves. What it proves is TradeShop's own decision, taken through
        // PlayerInteractEvent - an API every server this plugin supports has -
        // rather than through the Paper-only event.
        // ------------------------------------------------------------------
        rows.add(new IntegrationPlugin.Scenario("aShopSignIsProtectedFromEditingInEveryShopState", () -> {
            // OUT_OF_STOCK: a complete shop with an empty chest, and the state
            // this was measured leaking in.
            RealShop outOfStock = new RealShop(plugin, SITE.at(1));
            outOfStock.placeChestAndSign();
            outOfStock.createShopByCommand("1 DIAMOND", "1 EMERALD");
            assertState(outOfStock, ShopStatus.OUT_OF_STOCK);
            assertSignDenied(outOfStock, "an out-of-stock shop sign, which ShopTradeListener:132 "
                    + "returns from before the setCancelled at :150");

            // INCOMPLETE: /tradeshop create and nothing else.
            RealShop incomplete = new RealShop(plugin, SITE.at(2));
            incomplete.placeChestAndSign();
            incomplete.dispatch("create");
            assertState(incomplete, ShopStatus.INCOMPLETE);
            assertSignDenied(incomplete, "a shop whose owner has not set its items yet - :129");

            // CLOSED: the owner shut it deliberately.
            RealShop closed = new RealShop(plugin, SITE.at(3));
            closed.placeChestAndSign();
            closed.createShopByCommand("1 DIAMOND", "1 EMERALD");
            Shop closedShop = closed.get(() -> Shop.loadShop(new ShopLocation(closed.signBlock().getLocation())));
            closed.run(() -> {
                closedShop.setStatus(ShopStatus.CLOSED);
                closedShop.saveShop();
            });
            assertState(closed, ShopStatus.CLOSED);
            assertSignDenied(closed, "a shop its owner has closed - :126");

            // A storage block that is gone. #160's guard is what gets the
            // listener as far as :108 rather than throwing on the way, and this
            // is the state it lands in once it does.
            RealShop noChest = new RealShop(plugin, SITE.at(4));
            noChest.placeChestAndSign();
            noChest.createShopByCommand("1 DIAMOND", "1 EMERALD");
            noChest.run(() -> noChest.chestBlock().setType(Material.AIR, false));
            assertSignDenied(noChest, "a shop whose storage block has been removed - :108");

            // OPEN, which is the one state the fallback does cover, so that a
            // red run reads as "these states leak" rather than "signs are
            // unprotected".
            RealShop open = new RealShop(plugin, SITE.at(5));
            open.placeChestAndSign();
            open.createShopByCommand("1 DIAMOND", "1 EMERALD");
            open.stockShop(new ItemStack(Material.DIAMOND, 10));
            open.closeChestAsOwner();
            Assert.eventually(15_000, "precondition: the shop is open",
                    open.onServer(() -> Shop.loadShop(new ShopLocation(open.signBlock().getLocation()))
                            .getStatus() == ShopStatus.OPEN));
            assertSignDenied(open, "an open shop sign");
        }));

        // ------------------------------------------------------------------
        // #152, and the reason it needs care: protecting a sign must not cost
        // the things a player is supposed to be able to do.
        //
        // The trade is the one that would break. ShopTradeListener returns
        // immediately at :69 on a block result of DENY, so a protection handler
        // registered at a lower priority than that listener would silently stop
        // every shop on the server from trading while looking like it had only
        // shut a sign. The click below has to do both: pay the buyer, and leave
        // the sign shut.
        //
        // The GUI and the admin tools are not asserted here because neither is
        // reached by clicking a sign - both are /tradeshop subcommands, and
        // tier 3 drives the edit GUI with a real client - so a deny on a sign
        // interaction cannot touch them.
        // ------------------------------------------------------------------
        rows.add(new IntegrationPlugin.Scenario("protectingAShopSignCostsNeitherTheTradeNorAnOrdinarySign", () -> {
            RealShop scene = new RealShop(plugin, SITE.at(6));
            scene.placeChestAndSign();
            scene.createShopByCommand("1 DIAMOND", "1 EMERALD");
            scene.stockShop(new ItemStack(Material.DIAMOND, 10));
            scene.closeChestAsOwner();

            Assert.eventually(15_000, "precondition: the shop is open before anyone trades with it",
                    scene.onServer(() -> Shop.loadShop(new ShopLocation(scene.signBlock().getLocation()))
                            .getStatus() == ShopStatus.OPEN));

            Player buyer = scene.buyerHolding(new ItemStack(Material.EMERALD, 5));
            Event.Result result = clickSign(scene, buyer);

            Assert.eventually(15_000, "the buyer to be holding a diamond",
                    scene.onServer(() -> scene.rawCountOf(buyer, Material.DIAMOND) == 1));

            Assert.equal(1, scene.countOf(buyer, Material.DIAMOND),
                    "the click that shuts the sign must still be the click that trades");
            Assert.equal(4, scene.countOf(buyer, Material.EMERALD), "and the buyer must still pay");
            Assert.equal(Event.Result.DENY, result, "and the same click must leave the sign shut");

            // An ordinary sign is nobody's shop and has to be left entirely
            // alone, or this protection is a server-wide ban on writing signs.
            RealShop plain = new RealShop(plugin, SITE.at(7));
            plain.placeChestAndSign();
            Assert.that(!plain.get(() -> ShopType.isShop(plain.signBlock())),
                    "precondition: a blank sign is not a shop");
            Assert.equal(Event.Result.ALLOW, clickSign(plain, plain.owner()),
                    "a sign that is not a shop sign must be left exactly as the server found it");
        }));

        // ------------------------------------------------------------------
        // #152, second half: WHICH EVENT refuses the editor.
        //
        // The sign editor was refused by a listener for Paper's
        // io.papermc.paper.event.player.PlayerOpenSignEvent, registered only
        // when getServer().getVersion().toLowerCase().contains("paper") - a
        // string that is the server's own build description and does not name
        // the software, so on Paper 1.21.11 build 132
        // ("1.21.11-132-c5eb079 (MC: 1.21.11)") the listener was not registered
        // at all. Asking the classpath for the class instead of asking the
        // string was the obvious repair, and it was the wrong one.
        //
        // org.bukkit.event.player.PlayerSignOpenEvent is PLAIN BUKKIT. It ships
        // in spigot-api as well as in paper-api, Paper fires it beside its own,
        // and it needs no capability test, no version check and no second
        // listener. That is what this row pins, and it pins it by NAME:
        // a revert to the Paper-only guard would leave a sign editor that is
        // still refused on this Paper server and would sail past a row that only
        // asked whether the editor opened. Every assertion below therefore names
        // the portable event.
        //
        // WHAT THIS DOES NOT CLAIM: there is still no Spigot server in this
        // harness. What is proved here is that TradeShop's guard hangs off an
        // API Spigot has, rather than off one only Paper has.
        //
        // BOTH SIDES, deliberately. getSide() is not consulted by the handler: a
        // shop's lines live on the front, ShopType.getType reads line 0 which is
        // the front, and onSignChange refuses the finished edit for the whole
        // block whichever side it came from - so opening the back would open a
        // screen whose result is thrown away. Both sides are asserted so that a
        // later side test reads as a change of decision rather than as a bug fix.
        // ------------------------------------------------------------------
        rows.add(new IntegrationPlugin.Scenario("aShopSignRefusesItsEditorThroughPlainBukkitApi", () -> {
            Bukkit.getLogger().info("[harness] server name " + Bukkit.getServer().getName()
                    + ", version string " + Bukkit.getServer().getVersion());

            boolean registered = false;
            for (RegisteredListener listener : PlayerSignOpenEvent.getHandlerList().getRegisteredListeners()) {
                if ("TradeShop".equals(listener.getPlugin().getName())) registered = true;
            }

            Assert.that(registered,
                    "TradeShop must listen for org.bukkit.event.player.PlayerSignOpenEvent, which is "
                            + "plain Bukkit API and present on every server this plugin supports. A "
                            + "guard that hangs off Paper's PlayerOpenSignEvent instead is a guard "
                            + "Spigot does not get, and one this server did not get either while it "
                            + "was registered from the version string \""
                            + Bukkit.getServer().getVersion() + "\"");

            RealShop scene = new RealShop(plugin, SITE.at(8));
            scene.placeChestAndSign();
            scene.createShopByCommand("1 DIAMOND", "1 EMERALD");
            Assert.that(scene.get(() -> ShopType.isShop(scene.signBlock())),
                    "precondition: the block reads as a shop sign before its editor is asked for");

            Assert.that(openSign(scene, scene.owner(), Side.FRONT),
                    "a PlayerSignOpenEvent on the front of a shop sign must come back cancelled - "
                            + "that event, not Paper's, is what has to refuse the editor");
            Assert.that(openSign(scene, scene.owner(), Side.BACK),
                    "and so must one on the back: the guard does not read getSide(), because "
                            + "onSignChange refuses the finished edit for the whole block either way "
                            + "and an editor whose result is discarded is not protection");

            // The same event over a sign nobody has made a shop of. Without this
            // the row above is satisfied by a handler that cancels everything.
            RealShop plain = new RealShop(plugin, SITE.at(9));
            plain.placeChestAndSign();
            Assert.that(!plain.get(() -> ShopType.isShop(plain.signBlock())),
                    "precondition: a blank sign is not a shop");
            Assert.that(!openSign(plain, plain.owner(), Side.FRONT),
                    "an ordinary sign must still open its editor, or this protection is a "
                            + "server-wide ban on writing signs");
        }));

        return rows;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Right-clicks the scene's sign and answers what the block result was once
     * every plugin had run.
     *
     * <p>The block result, not {@code isCancelled()}: a fresh
     * {@link PlayerInteractEvent} over a non-null block starts at {@code ALLOW},
     * and {@code isCancelled()} only becomes true once the item in hand has been
     * denied as well. What stops a sign from opening its editor is the block
     * half on its own.
     */
    private static Event.Result clickSign(RealShop scene, Player player) {
        return scene.get(() -> {
            PlayerInteractEvent event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, null,
                    scene.signBlock(), BlockFace.NORTH);
            Bukkit.getPluginManager().callEvent(event);
            return event.useInteractedBlock();
        });
    }

    /**
     * Asks the server to open the scene's sign for editing, the way a
     * right-click on a written sign does, and answers whether anything refused.
     *
     * <p>{@code Cause.INTERACT} because that is the cause Paper 1.21.11 build 132
     * was measured sending for exactly that click. The event is constructed and
     * fired here rather than driven by a real click for the same reason the rest
     * of this file constructs its events: what is being asserted is TradeShop's
     * decision about this event, and a click routed through the server would
     * also be answered by {@code onShopSignInteract}, which denies the block
     * interaction before the editor is ever asked for.
     */
    @SuppressWarnings("removal")
    private static boolean openSign(RealShop scene, Player player, Side side) {
        return scene.get(() -> {
            PlayerSignOpenEvent event = new PlayerSignOpenEvent(player,
                    (Sign) scene.signBlock().getState(), side, PlayerSignOpenEvent.Cause.INTERACT);
            Bukkit.getPluginManager().callEvent(event);
            return event.isCancelled();
        });
    }

    private static void assertSignDenied(RealShop scene, String what) {
        Assert.that(scene.get(() -> ShopType.isShop(scene.signBlock())),
                "precondition: the block reads as a shop sign before it is clicked (" + what + ")");
        Assert.equal(Event.Result.DENY, clickSign(scene, scene.owner()),
                "a right-click on " + what + " must leave the block interaction denied, or the "
                        + "server goes on to open the sign for editing");
    }

    private static void assertState(RealShop scene, ShopStatus expected) {
        Assert.equal(expected,
                scene.get(() -> Shop.loadShop(new ShopLocation(scene.signBlock().getLocation())).getStatus()),
                "precondition: the shop is " + expected + " before its sign is clicked");
    }

    /**
     * The exception a wrapper was hiding.
     *
     * <p>{@link Sync} reports what the server thread threw as an
     * {@code AssertionError} whose message is the cause's {@code toString()}, so
     * a row that reported what it caught would name the marshalling rather than
     * the defect.
     */
    private static String rootCause(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause.toString();
    }
}
