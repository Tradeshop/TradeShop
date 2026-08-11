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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.player.PlayerSetting;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shop.ShopChest;
import org.shanerx.tradeshop.shoplocation.ShopLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code allow-sign-break}, the setting that reaches a shop's sign before every
 * check that would have protected it.
 *
 * <h2>The defect</h2>
 * {@code ShopProtectionListener.onBlockBreak:283} returns the moment the setting
 * is on, and it returns <em>before</em> {@code shop.remove()}. The sign breaks,
 * the block becomes air, and the record survives with nothing left that can
 * reach it - no trade, no repair, no removal - while it goes on counting against
 * its owner's limit and against {@code MAX_SHOPS_PER_CHUNK} and holding its
 * storage block linked. That is the same orphan the sign-break cleanup closed
 * for the default path, reached through a setting instead: the cleanup sits
 * after this return, so the setting bypasses it.
 *
 * <h2>What the setting takes, and what it must not touch</h2>
 * The break succeeds and the shop is deleted. The record goes and the chest
 * linkage goes with it, and that is the whole of it:
 *
 * <ul>
 *   <li><b>The storage block, and everything in it</b> - untouched. Only the
 *       sign was broken. The chest is a different block, standing and unbroken,
 *       so the server drops nothing out of it and neither may the plugin: it is
 *       left holding every item it held, as an ordinary chest. The rows below
 *       count the stock back out of it after the break, which is the assertion
 *       that would catch a plugin emptying it.</li>
 *   <li><b>The ground</b> - empty. Nothing is dropped on this path at all.</li>
 *   <li><b>The sign</b> - vanilla drops it, because the break is not
 *       cancelled. Nothing here touches it, and {@link #breakSign} clears the
 *       block without a drop, so the item entities these rows count are the
 *       plugin's alone.</li>
 * </ul>
 *
 * <p>The stock is diamonds <em>and</em> gold and the shop's cost is an emerald,
 * so "the chest still holds what it held" is a claim about the whole inventory
 * rather than about the one material the shop traded.
 *
 * <h2>Why tier 2</h2>
 * Every assertion below needs something a mock does not have: a world that can
 * be searched for item entities, a linkage read back out of a real data store,
 * and a setting the running plugin reads through its own config object.
 */
final class AllowSignBreakRows {

    private AllowSignBreakRows() {
    }

    /**
     * These rows' patch of the world. Reserved through {@link SiteAllocator}
     * rather than agreed by comment - see that class for why a hand-typed
     * range stopped being trustworthy.
     */
    private static final SiteAllocator.Reservation SITE = SiteAllocator.reserve("AllowSignBreakRows", 6);

    /** The shop's stock. Neither material is the shop's cost, which is the point. */
    private static final int DIAMONDS = 12;
    private static final int GOLD = 5;

    static List<IntegrationPlugin.Scenario> rows(IntegrationPlugin plugin) {
        List<IntegrationPlugin.Scenario> rows = new ArrayList<>();

        // ------------------------------------------------------------------
        // The subject. RED before the fix.
        // ------------------------------------------------------------------
        rows.add(new IntegrationPlugin.Scenario("aShopWhoseSignBreakIsAllowedIsRemovedAndItsStorageIsLeftAlone", () -> {
            RealShop scene = stockedShop(plugin, SITE.at(0));
            ShopLocation where = locationOf(scene);

            // Not the owner, and not an admin. A harness player answers
            // hasPermission() with true and PlayerSetting.adminEnabled defaults
            // to true, so "a stranger" has to be built rather than assumed - or
            // the row would be about an admin and would say nothing about the
            // setting.
            Player stranger = stranger(scene, "asbStranger" + SITE.at(0));

            withSignBreakAllowed(scene, () -> {
                Assert.that(scene.get(() -> Shop.loadShop(where)) != null,
                        "precondition: the shop is stored before its sign is touched");
                Assert.that(scene.get(() -> ShopChest.isShopChest(scene.chestBlock())),
                        "precondition: and its storage block is linked to it");
                Assert.equal(DIAMONDS, scene.countInChest(Material.DIAMOND),
                        "precondition: the shop's stock is in its storage block");
                Assert.equal(0, totalItems(onTheGround(scene)),
                        "precondition: and nothing is lying on the ground at this site yet");

                BlockBreakEvent broke = breakSign(scene, stranger);

                Assert.that(!broke.isCancelled(),
                        "allow-sign-break means anyone may break a shop's sign, and that is the "
                                + "one thing about this path that was already right");

                Assert.that(scene.get(() -> Shop.loadShop(where)) == null,
                        "with allow-sign-break on, breaking a shop's sign must take the record "
                                + "with it. ShopProtectionListener.onBlockBreak:283 returns on the "
                                + "setting BEFORE shop.remove(), so the sign goes, the block becomes "
                                + "air, and the shop outlives the only block that could ever have "
                                + "found it again - counting against its owner's limit and against "
                                + "the chunk's, and holding its storage block linked, forever");

                Assert.that(!scene.get(() -> ShopChest.isShopChest(scene.chestBlock())),
                        "and the storage block must stop reading as a shop chest: a linkage that "
                                + "outlives its shop is hopper protection and a break refusal "
                                + "enforced on behalf of nothing");

                // The storage block. Nothing came out of it, because nothing
                // broke it.
                Assert.equal(DIAMONDS, scene.countInChest(Material.DIAMOND),
                        "and the storage block keeps its contents. The sign was the block that "
                                + "broke; the chest is standing, unbroken and full, and emptying it "
                                + "is not something the plugin gets to do on the server's behalf");
                Assert.equal(GOLD, scene.countInChest(Material.GOLD_INGOT),
                        "for every slot, not only the ones the shop traded");
                Assert.equal(Material.CHEST, scene.get(() -> scene.chestBlock().getType()),
                        "and the storage block itself is still there - it was never the block "
                                + "being broken, and it does not drop as an item");

                Assert.equal(0, totalItems(onTheGround(scene)),
                        "and nothing is on the ground. Deleting the record is the whole of what "
                                + "this setting does; a shop being removed is not a reason to put "
                                + "one item anywhere");
            });
        }));

        // ------------------------------------------------------------------
        // The same setting, the two players who could already break the sign.
        // RED before the fix, for the same reason as the row above.
        // ------------------------------------------------------------------
        rows.add(new IntegrationPlugin.Scenario("allowSignBreakOnTakesTheOwnersAndAnAdminsShopTheSameWay", () -> {
            RealShop ownersOwn = stockedShop(plugin, SITE.at(1));
            ShopLocation ownersShop = locationOf(ownersOwn);

            withSignBreakAllowed(ownersOwn, () -> {
                BlockBreakEvent broke = breakSign(ownersOwn, ownersOwn.owner());

                Assert.that(!broke.isCancelled(),
                        "an owner breaking their own shop sign is allowed to, and always was");
                Assert.that(ownersOwn.get(() -> Shop.loadShop(ownersShop)) == null,
                        "and with allow-sign-break on the record goes with the sign");
                Assert.equal(DIAMONDS, ownersOwn.countInChest(Material.DIAMOND),
                        "and the stock stays in the chest. The setting is not a permission check "
                                + "and it does not grow one here: an operator who turns it on has "
                                + "chosen one rule for everybody, and that rule leaves the storage "
                                + "block alone whoever swung at the sign");
                Assert.equal(0, totalItems(onTheGround(ownersOwn)),
                        "so nothing is on the ground for the owner's own break either");
            });

            RealShop adminsTarget = stockedShop(plugin, SITE.at(2));
            ShopLocation adminsShop = locationOf(adminsTarget);
            Player admin = admin(adminsTarget, "asbAdmin" + SITE.at(2));

            withSignBreakAllowed(adminsTarget, () -> {
                BlockBreakEvent broke = breakSign(adminsTarget, admin);

                Assert.that(!broke.isCancelled(),
                        "an admin breaking someone else's shop sign is allowed to, and always was");
                Assert.that(adminsTarget.get(() -> Shop.loadShop(adminsShop)) == null,
                        "and the record goes with the sign for an admin too");
                Assert.that(!adminsTarget.get(() -> ShopChest.isShopChest(adminsTarget.chestBlock())),
                        "and the storage block is unlinked");
                Assert.equal(DIAMONDS, adminsTarget.countInChest(Material.DIAMOND),
                        "and the stock stays in the chest, one rule for everybody");
                Assert.equal(0, totalItems(onTheGround(adminsTarget)),
                        "and nothing is on the ground");
            });
        }));

        // ------------------------------------------------------------------
        // The control. GREEN before the fix and after it: with the setting off,
        // every path is the one that shipped.
        // ------------------------------------------------------------------
        rows.add(new IntegrationPlugin.Scenario("allowSignBreakOffLeavesTheRefusalTheOwnerAndTheAdminWhereTheyWere", () -> {
            Assert.that(!Sync.get(plugin, () -> Setting.ALLOW_SIGN_BREAK.getBoolean()),
                    "precondition: allow-sign-break is off, which is the default and the state "
                            + "every assertion below is about");

            // A stranger is refused, and told why.
            RealShop refused = stockedShop(plugin, SITE.at(3));
            ShopLocation refusedShop = locationOf(refused);
            Player stranger = stranger(refused, "asbStranger" + SITE.at(3));

            BlockBreakEvent strangerBreak = refused.get(() -> {
                BlockBreakEvent event = new BlockBreakEvent(refused.signBlock(), stranger);
                Bukkit.getPluginManager().callEvent(event);
                return event;
            });

            Assert.that(strangerBreak.isCancelled(),
                    "with allow-sign-break off, someone who is neither the owner nor an admin "
                            + "must not be able to break a shop's sign");
            Assert.that(HarnessPlayer.heardBy(stranger).stream()
                            .anyMatch(said -> said.toLowerCase().contains("may not destroy")),
                    "and must be told so, which is the whole of what a refused player sees");
            Assert.that(refused.get(() -> Shop.loadShop(refusedShop)) != null,
                    "the shop survives a refused break");
            Assert.that(refused.get(() -> ShopChest.isShopChest(refused.chestBlock())),
                    "and stays linked to its storage block");
            Assert.equal(DIAMONDS, refused.countInChest(Material.DIAMOND),
                    "and keeps its stock, which nothing about a refusal may touch");
            Assert.that(onTheGround(refused).isEmpty(),
                    "and nothing is on the ground");

            // The owner takes their own shop down, and keeps their stock.
            RealShop ownersOwn = stockedShop(plugin, SITE.at(4));
            ShopLocation ownersShop = locationOf(ownersOwn);

            BlockBreakEvent ownerBreak = breakSign(ownersOwn, ownersOwn.owner());

            Assert.that(!ownerBreak.isCancelled(),
                    "an owner breaking their own shop sign is allowed to");
            Assert.that(ownersOwn.get(() -> Shop.loadShop(ownersShop)) == null,
                    "and the shop goes with it, through the branch that was already there");
            Assert.that(!ownersOwn.get(() -> ShopChest.isShopChest(ownersOwn.chestBlock())),
                    "and the storage block is unlinked, as it already was");
            Assert.equal(DIAMONDS, ownersOwn.countInChest(Material.DIAMOND),
                    "and the stock stays in the chest. This is the row that pins the default "
                            + "path where it is: whatever allow-sign-break does, the setting being "
                            + "off must reach exactly the behaviour that shipped");
            Assert.that(onTheGround(ownersOwn).isEmpty(),
                    "so nothing is on the ground either");

            // And an admin taking someone else's down.
            RealShop adminsTarget = stockedShop(plugin, SITE.at(5));
            ShopLocation adminsShop = locationOf(adminsTarget);
            Player admin = admin(adminsTarget, "asbAdmin" + SITE.at(5));

            BlockBreakEvent adminBreak = breakSign(adminsTarget, admin);

            Assert.that(!adminBreak.isCancelled(),
                    "an admin breaking someone else's shop sign is allowed to");
            Assert.that(adminsTarget.get(() -> Shop.loadShop(adminsShop)) == null,
                    "and the shop goes with it");
            Assert.that(!adminsTarget.get(() -> ShopChest.isShopChest(adminsTarget.chestBlock())),
                    "and the storage block is unlinked");
            Assert.equal(DIAMONDS, adminsTarget.countInChest(Material.DIAMOND),
                    "and the stock stays in the chest for an admin's break too");
            Assert.that(onTheGround(adminsTarget).isEmpty(),
                    "and nothing is on the ground");
        }));

        return rows;
    }

    // ------------------------------------------------------------------
    // The scene
    // ------------------------------------------------------------------

    /**
     * A trade shop selling diamonds for emeralds, with diamonds and gold in its
     * chest.
     *
     * <p>The gold is stock the shop does not trade and the emerald is a trade the
     * shop holds no item for. Between them they separate "the contents of the
     * storage block" from "the items named on the sign", so that "the chest still
     * holds what it held" is a claim about the whole inventory.
     */
    private static RealShop stockedShop(IntegrationPlugin plugin, int site) {
        RealShop scene = new RealShop(plugin, site);
        scene.placeChestAndSign();
        floor(scene);
        scene.createShopByCommand("1 DIAMOND", "1 EMERALD");
        scene.stockShop(new ItemStack(Material.DIAMOND, DIAMONDS));
        scene.stockShop(new ItemStack(Material.GOLD_INGOT, GOLD));
        return scene;
    }

    /**
     * Three by three of stone under the chest, so that "nothing is on the ground"
     * is a claim that could fail.
     *
     * <p>{@link RealShop} puts its chest at y=0 in whatever the world generated
     * there, and these sites are open air: measured on Paper 1.21.11 build 132, a
     * stack dropped at the sign was still within three blocks of it when the next
     * assertion read the site and gone by the one after, having fallen the eight
     * or so blocks a second of gravity buys. A row that asserted an empty ground
     * over an empty sky would pass whether the plugin dropped the shop's stock or
     * not, which is the vacuous pass this tier exists to close. With a floor
     * under it, anything the plugin drops is still lying there to be counted.
     */
    private static void floor(RealShop scene) {
        scene.run(() -> {
            Block chest = scene.chestBlock();
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    chest.getRelative(x, -1, z).setType(Material.STONE, false);
                }
            }
        });
    }

    private static ShopLocation locationOf(RealShop scene) {
        return scene.get(() -> new ShopLocation(scene.signBlock().getLocation()));
    }

    /**
     * Breaks the sign the way the server does: the event, then the block.
     *
     * <p>The block is cleared without a drop. On a real server vanilla pops the
     * sign as an item, and nothing in this plugin is involved in that; leaving it
     * out is what makes every item entity these rows find one the plugin put
     * there.
     */
    private static BlockBreakEvent breakSign(RealShop scene, Player breaker) {
        return scene.get(() -> {
            BlockBreakEvent event = new BlockBreakEvent(scene.signBlock(), breaker);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                scene.signBlock().setType(Material.AIR, false);
            }
            return event;
        });
    }

    /**
     * Runs {@code body} with {@code allow-sign-break} on, and puts the setting
     * back whatever happens.
     *
     * <p>Restored in a finally rather than at the end of the body: a row that
     * fails half way through must not leave the setting on for every row after
     * it, or one red row becomes a suite nobody can read.
     */
    private static void withSignBreakAllowed(RealShop scene, Runnable body) {
        Object was = scene.get(Setting.ALLOW_SIGN_BREAK::getSetting);
        scene.run(() -> Setting.ALLOW_SIGN_BREAK.setValue(true));
        try {
            Assert.that(scene.get(() -> Setting.ALLOW_SIGN_BREAK.getBoolean()),
                    "precondition: the running plugin reads allow-sign-break as on, or the row "
                            + "below is about the default path and proves nothing");
            body.run();
        } finally {
            scene.run(() -> Setting.ALLOW_SIGN_BREAK.setValue(was));
        }
    }

    // ------------------------------------------------------------------
    // Players the harness has to build rather than assume
    // ------------------------------------------------------------------

    /**
     * Someone who is neither the shop's owner nor an admin.
     *
     * <p>{@link HarnessPlayer} answers {@code hasPermission} with true and
     * {@code PlayerSetting.adminEnabled} defaults to true, so
     * {@code Permissions.isAdminEnabled} says yes to every harness player unless
     * it is told otherwise. A row that skipped this would be about an admin.
     */
    private static Player stranger(RealShop scene, String name) {
        return withAdmin(scene, name, false);
    }

    /** Someone who is not the owner and has their admin mode switched on. */
    private static Player admin(RealShop scene, String name) {
        return withAdmin(scene, name, true);
    }

    private static Player withAdmin(RealShop scene, String name, boolean adminEnabled) {
        return scene.get(() -> {
            Player player = HarnessPlayer.create(name,
                    scene.signBlock().getLocation().add(0.5, 0, 1.5));
            PlayerSetting setting = tradeShop().getDataStorage().loadPlayer(player.getUniqueId());
            setting.setAdminEnabled(adminEnabled);
            tradeShop().getDataStorage().savePlayer(setting);
            return player;
        });
    }

    // ------------------------------------------------------------------
    // Reading the ground
    // ------------------------------------------------------------------

    /**
     * What is lying at this scene's site, read on the server thread.
     *
     * <p>The chunk is loaded first. Nothing is standing at these sites - the
     * harness player is a proxy, not an entity - so a site's chunk is loaded only
     * while something is touching it, and {@code getNearbyEntities} answers out
     * of loaded chunks alone. A query that did not load it would report an empty
     * ground for a chunk that had merely gone quiet, which is the one way an
     * assertion that the ground is empty could pass without meaning it.
     */
    private static List<ItemStack> onTheGround(RealShop scene) {
        return scene.get(() -> {
            scene.signBlock().getChunk().load();

            List<ItemStack> found = new ArrayList<>();

            for (Entity entity : scene.signBlock().getWorld().getNearbyEntities(
                    scene.signBlock().getLocation().add(0.5, 0.5, 0.5), 3, 4, 3)) {
                if (entity instanceof Item item) {
                    found.add(item.getItemStack());
                }
            }

            return found;
        });
    }

    private static int totalItems(List<ItemStack> stacks) {
        int total = 0;
        for (ItemStack stack : stacks) {
            if (stack != null) {
                total += stack.getAmount();
            }
        }
        return total;
    }

    private static TradeShop tradeShop() {
        return (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
    }
}
