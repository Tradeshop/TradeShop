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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.data.type.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.data.storage.DataStorage;
import org.shanerx.tradeshop.data.storage.DataType;
import org.shanerx.tradeshop.item.ShopItemSide;
import org.shanerx.tradeshop.item.ShopItemStack;
import org.shanerx.tradeshop.player.ShopRole;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shop.ShopChest;
import org.shanerx.tradeshop.shoplocation.ShopLocation;
import org.shanerx.tradeshop.utils.Utils;
import org.shanerx.tradeshop.utils.objects.ObjectHolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * The rows for the defects a code review reported and nobody ever ran.
 *
 * <h2>Why every one of them is here and not in {@code src/test}</h2>
 * Each turns on something MockBukkit does not have, rather than on something it
 * does differently:
 *
 * <ul>
 *   <li><b>Config repair</b> is a real {@code config.yml} on disk, read back through
 *       {@code ConfigManager.reload()} - the same call {@code TradeShop.onEnable}
 *       makes at {@code TradeShop.java:140}. The defect is what the repair does
 *       to a file, so a test with no file proves nothing about it.</li>
 *   <li><b>Chest linkage</b> is a linkage map written to and read from a real JSON file,
 *       keyed by a world a real server named.</li>
 *   <li><b>Five-slot storage</b> is {@code Bukkit.createInventory}'s own size rule - CraftBukkit
 *       refuses anything that is not a multiple of nine - applied to the real slot
 *       count of a real hopper.</li>
 *   <li><b>Double chests</b> needs a real double chest. MockBukkit has no
 *       {@code DoubleChest}, so {@code ShopChest.isDoubleChest} is false there and
 *       the branch under test is never entered at all.</li>
 *   <li><b>The cost side read off disk</b> is a real JSON file whose modification time
 *       moves while the loader is still reading it. Nothing about it exists without a
 *       plugin data folder and a storage layer that reloads itself.</li>
 * </ul>
 *
 * <p>Every row asserts what an operator is entitled to, so a red row is the
 * defect and a green row is the fix. None of them is written to pass today.
 */
final class DefectRows {

    private DefectRows() {
    }

    /**
     * These rows' patch of the world. Reserved through {@link SiteAllocator}
     * rather than agreed by comment - see that class for why a hand-typed range
     * stopped being trustworthy.
     */
    private static final SiteAllocator.Reservation SITE = SiteAllocator.reserve("DefectRows", 10);

    /**
     * The two players a shop is shared with. Fixed rather than random so that a
     * red row names the same value the shop file on disk does.
     */
    private static final UUID MANAGER = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final UUID MEMBER = UUID.fromString("66666666-7777-8888-9999-aaaaaaaaaaaa");

    static List<IntegrationPlugin.Scenario> rows(IntegrationPlugin plugin) {
        List<IntegrationPlugin.Scenario> rows = new ArrayList<>();

        // ------------------------------------------------------------------
        // Config repair, first half: an older config.yml that has lost part of a per-item
        // setting is not repaired by a boot.
        //
        // ConfigManager.addKeyValue:207-220 is the repair. For a Map setting it
        // walks the map's OWN keys - compare-durability, compare-name - and asks
        // whether each is present. It never looks inside one. A node that exists
        // but has lost its `default` child is therefore seen as present, nothing
        // is written, addKeyValue answers false, ConfigManager.reload:116 does
        // not save, and the hole survives the boot.
        //
        // It also returns as soon as it writes ONE key, so even the shape it can
        // repair is repaired one key per pass.
        //
        // MEASURED, so that the fix is aimed at the shape that actually breaks: a
        // per-item key that is absent ALTOGETHER is repaired. addKeyValue writes
        // it - as a Map.toString(), at :213, which is its own bug - returns true,
        // and reload() then rewrites the whole file from the enum defaults, which
        // covers the damage. Only the partly-present shape below survives a boot.
        // ------------------------------------------------------------------
        rows.add(new IntegrationPlugin.Scenario("aPerItemSettingThatLostAChildIsRepairedAtBoot", () -> {
            TradeShop tradeShop = tradeShop();
            File config = tradeShop.getSettingManager().getFile();
            File backup = new File(config.getParentFile(), config.getName() + ".w9-backup");
            String settingPath = Setting.SHOP_PER_ITEM_SETTINGS.getPath();

            // A scene only so that the config edits are marshalled onto the
            // server thread the same way every other touch in this tier is.
            RealShop scene = new RealShop(plugin, SITE.at(0));
            scene.placeChestAndSign();

            try {
                Files.copy(config.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // The shape an older file actually has: the setting is there and
                // one of its two children is not, because the child was added
                // after the file was written.
                scene.run(() -> {
                    YamlConfiguration old = YamlConfiguration.loadConfiguration(config);
                    old.set(settingPath + ".compare-durability.default", null);
                    old.set(settingPath + ".compare-name.default", null);
                    save(old, config);
                });

                // Exactly what TradeShop.java:140 does on every boot.
                scene.run(() -> tradeShop.getSettingManager().reload());

                Assert.that(Setting.SHOP_PER_ITEM_SETTINGS.getMappedObject("compare-durability.default") != null,
                        "a boot must repair a per-item setting that has lost a child, or every later read "
                                + "of it answers null - ConfigManager.addKeyValue:207-220 only asks whether "
                                + "the setting's own key is present and never looks inside it");
                Assert.that(Setting.SHOP_PER_ITEM_SETTINGS.getMappedObject("compare-name.default") != null,
                        "and it must repair every one of them rather than stopping at the first - "
                                + "addKeyValue:214 returns as soon as one key is written");
            } catch (IOException e) {
                throw new AssertionError("could not stand up an older config.yml: " + e);
            } finally {
                restore(backup, config);
                scene.run(() -> tradeShop.getSettingManager().reload());
            }
        }));

        // ------------------------------------------------------------------
        // Config repair, second half: whatever the config does, a setting that reads back
        // as null must not take the trade gate down with it.
        //
        // Setting.getMappedObject:299 answers null for a hole,
        // ShopItemStackSettingKeys.getDefaultValue:107 wraps that null in an
        // ObjectHolder, and then:
        //   ObjectHolder.asBoolean:107  -> canBeBoolean:70-71 calls obj.toString()
        //                                  on it, with no null guard at all;
        //   ObjectHolder.asInteger:112  -> answers null, which
        //   ShopItemStack.java:439      -> unboxes into an int.
        //
        // The hole is punched in memory here rather than through a file: this row
        // is about the guard, not about the repair, and the two have to be able to
        // fail and be fixed independently or a fix to one turns the other green
        // without ever having been written.
        // ------------------------------------------------------------------
        rows.add(new IntegrationPlugin.Scenario("aMissingPerItemSettingDoesNotThrowInsideTheTradeGate", () -> {
            RealShop scene = new RealShop(plugin, SITE.at(1));
            scene.placeChestAndSign();
            scene.createShopByCommand("1 DIAMOND", "1 EMERALD");
            scene.stockShop(new ItemStack(Material.DIAMOND, 10));

            Shop shop = scene.get(() -> Shop.loadShop(new ShopLocation(scene.signBlock().getLocation())));
            Assert.that(shop != null, "precondition: a shop to trade with once a setting has gone missing");

            Player buyer = scene.buyerHolding(new ItemStack(Material.EMERALD, 5));

            Object durability = Setting.SHOP_PER_ITEM_SETTINGS.getMappedObject("compare-durability.default");
            Object name = Setting.SHOP_PER_ITEM_SETTINGS.getMappedObject("compare-name.default");

            try {
                // The holder a missing setting produces, on its own, before any
                // shop is involved.
                try {
                    new ObjectHolder<Object>(null).asBoolean();
                } catch (Throwable t) {
                    throw new AssertionError("ObjectHolder.asBoolean() must answer for a setting that is "
                            + "not in the file, and it threw " + rootCause(t) + " - :107 hands straight to "
                            + "canBeBoolean:70-71, which calls obj.toString() with no null guard", t);
                }

                scene.run(() -> {
                    Setting.SHOP_PER_ITEM_SETTINGS.setMappedValue("compare-durability.default", null);
                    Setting.SHOP_PER_ITEM_SETTINGS.setMappedValue("compare-name.default", null);
                });

                // The item comparison, which is what a trade is made of.
                try {
                    scene.run(() -> new ShopItemStack(new ItemStack(Material.DIAMOND))
                            .isSimilar(new ItemStack(Material.DIAMOND)));
                } catch (Throwable t) {
                    throw new AssertionError("comparing two items must survive a per-item setting that is "
                            + "missing from the config, and it threw " + rootCause(t)
                            + " - ShopItemStack.java:439 unboxes ObjectHolder.asInteger():112, which "
                            + "answers null for a setting that is not there", t);
                }

                // And the gate itself, called the way ShopTradeListener calls it,
                // so that the failure is where a shop owner meets it.
                try {
                    scene.run(() -> new Utils().canExchangeAll(shop, buyer.getInventory(), 1,
                            Action.RIGHT_CLICK_BLOCK));
                } catch (Throwable t) {
                    throw new AssertionError("a trade must still be possible when the config is missing a "
                            + "per-item setting, and the gate threw " + rootCause(t), t);
                }
            } finally {
                Sync.run(plugin, () -> {
                    Setting.SHOP_PER_ITEM_SETTINGS.setMappedValue("compare-durability.default", durability);
                    Setting.SHOP_PER_ITEM_SETTINGS.setMappedValue("compare-name.default", name);
                });
            }
        }));

        // ------------------------------------------------------------------
        // The chest-linkage defect. Unlinking a chest never unlinks it.
        //
        // LinkageConfiguration.removeChest:70 calls
        // Map<String, Object>.remove(chestLocation) with a ShopLocation.
        // Map.remove takes Object, so it compiles; no String key is ever equal to
        // a ShopLocation, so it is always a miss. Every other method on that
        // interface keys by chestLocation.toString() - addLinkage:53-56,
        // getLinkedShop:44-45 - and this one does not.
        //
        // The entry therefore outlives the chest. DataStorage.removeChestLinkage:317
        // is the route a player takes by breaking half of a shop's storage
        // (ShopProtectionListener.java:317), and Shop.removeStorage:617 the route
        // taken by unlinking the whole of it.
        // ------------------------------------------------------------------
        rows.add(new IntegrationPlugin.Scenario("unlinkingAChestRemovesItsLinkageEntry", () -> {
            RealShop scene = new RealShop(plugin, SITE.at(2));
            scene.placeChestAndSign();
            scene.createShopByCommand("1 DIAMOND", "1 EMERALD");

            ShopLocation chest = scene.get(() -> new ShopLocation(scene.chestBlock().getLocation()));

            Assert.that(scene.get(() -> tradeShop().getDataStorage().getChestLinkage(chest)) != null,
                    "precondition: creating the shop linked the chest under the sign to it");

            scene.run(() -> tradeShop().getDataStorage().removeChestLinkage(chest));

            Assert.that(scene.get(() -> tradeShop().getDataStorage().getChestLinkage(chest)) == null,
                    "a chest that has been unlinked must stop being linked - "
                            + "LinkageConfiguration.removeChest:70 passes a ShopLocation to a "
                            + "Map<String,String>.remove, which is a legal call that can never match the "
                            + "String key addLinkage:56 wrote");

            // And the block goes on reading as a shop chest for exactly as long as
            // the entry does, which is what the hopper and protection paths key on
            // (ShopChest.isShopChest:75, ShopProtectionListener.java:117).
            Assert.that(!scene.get(() -> ShopChest.isShopChest(scene.chestBlock())),
                    "and an unlinked chest must stop reading as a shop chest to every path that asks");
        }));

        // ------------------------------------------------------------------
        // The five-slot-storage defect. Two of the ten storage types a shop may be built on hold five
        // slots, and five is not a multiple of nine.
        //
        // Shop.java:740 and Utils.java:430 both pass a raw
        // getStorageContents().length to Bukkit.createInventory, which takes a
        // multiple of nine up to 54 and throws otherwise. A hopper and a brewing
        // stand are both five, and ShopStorage.Storages lists both - HOPPER at
        // :94, BREWING_STAND at :89 - so an operator who adds either to
        // allowed-shops gets a shop that cannot count its own stock, let alone
        // sell any. Measured here, from this server:
        // "Size for custom inventory must be a multiple of 9 between 9 and 54
        // slots (got 5)".
        //
        // Utils.java:415 does the same arithmetic to the PLAYER's inventory. That
        // one is 36 slots on this server and therefore safe today; it is named
        // here rather than tested, because a test for it would be asserting the
        // size of a vanilla inventory rather than a decision this plugin makes.
        // ------------------------------------------------------------------
        rows.add(new IntegrationPlugin.Scenario("aShopOnAHopperCanCountItsStockAndTrade", () -> {
            Object allowed = Setting.ALLOWED_SHOPS.getSetting();
            RealShop scene = new RealShop(plugin, SITE.at(3));

            try {
                allowStorage(scene, "HOPPER");
                scene.placeStorageAndSign(Material.HOPPER, Material.OAK_SIGN);

                Assert.equal(5, storageSlots(scene),
                        "precondition: a hopper holds five slots, and Bukkit.createInventory takes a "
                                + "multiple of nine");
                Assert.that(scene.get(() -> tradeShop().getListManager().isInventory(scene.chestBlock())),
                        "precondition: the server was told a hopper may carry a shop");

                try {
                    scene.createShopByCommand("1 DIAMOND", "1 EMERALD");
                } catch (Throwable t) {
                    throw new AssertionError("a shop on a hopper should be created like any other, and "
                            + "creating one threw: " + rootCause(t) + ". Shop.java:740 builds a counting "
                            + "inventory the size of the storage block, and a hopper is five slots", t);
                }

                Shop shop = scene.get(() -> Shop.loadShop(new ShopLocation(scene.signBlock().getLocation())));
                Assert.that(shop != null, "a shop on a hopper should have been stored at the sign");

                scene.stockShop(new ItemStack(Material.DIAMOND, 10));
                scene.run(shop::updateFullTradeCount);
                Assert.equal(10, shop.getAvailableTrades(),
                        "ten diamonds at one per trade is ten trades on a hopper as much as on a chest");

                Player hopperBuyer = scene.buyerHolding(new ItemStack(Material.EMERALD, 5));
                try {
                    scene.run(() -> new Utils().canExchangeAll(shop, hopperBuyer.getInventory(), 1,
                            Action.RIGHT_CLICK_BLOCK));
                } catch (Throwable t) {
                    throw new AssertionError("a buyer must be able to trade with a hopper shop, and the "
                            + "gate threw: " + rootCause(t) + " - Utils.java:430 copies the shop's storage "
                            + "into an inventory of the storage block's own slot count", t);
                }
            } finally {
                restoreAllowedShops(plugin, allowed);
            }
        }));

        rows.add(new IntegrationPlugin.Scenario("aShopOnABrewingStandCanBeCreated", () -> {
            Object allowed = Setting.ALLOWED_SHOPS.getSetting();
            RealShop scene = new RealShop(plugin, SITE.at(4));

            try {
                allowStorage(scene, "BREWING_STAND");
                scene.placeStorageAndSign(Material.BREWING_STAND, Material.OAK_SIGN);

                Assert.equal(5, storageSlots(scene),
                        "precondition: a brewing stand is the second permitted five-slot storage type");
                Assert.that(scene.get(() -> tradeShop().getListManager().isInventory(scene.chestBlock())),
                        "precondition: the server was told a brewing stand may carry a shop");

                try {
                    scene.createShopByCommand("1 DIAMOND", "1 EMERALD");
                } catch (Throwable t) {
                    throw new AssertionError("a shop on a brewing stand should be created like any other, "
                            + "and creating one threw: " + rootCause(t) + ". ShopStorage.Storages:89 "
                            + "permits it and Shop.java:740 cannot count its five slots", t);
                }

                Assert.that(scene.get(() -> Shop.loadShop(new ShopLocation(scene.signBlock().getLocation()))) != null,
                        "a shop on a brewing stand should have been stored at the sign");
            } finally {
                restoreAllowedShops(plugin, allowed);
            }
        }));

        // ------------------------------------------------------------------
        // The double-chest defect. A double chest laid along the Z axis resolves to the wrong half.
        //
        // ShopChest.getOtherHalfOfDoubleChest:101-115. The X branch at :107 asks
        // `check.getX() == floor(chestLoc.getX())`, which is the right question.
        // The Z branch at :109 asks `check.getX() == floor(chestLoc.getZ())` - an
        // X compared against a Z. The two agree only where the chest happens to
        // sit on the diagonal, so one of the two halves always resolves to
        // itself, and which one depends on the coordinates.
        //
        // MEASURED here: the pair at x=25000, z=0 and z=1 has its combined
        // inventory at z=0.5, so floor(z) is 0 while check.getX() is 25000, the
        // comparison is false, and the left half is told its other half is the
        // left half.
        //
        // Only reachable with a real double chest: MockBukkit has no DoubleChest,
        // so isDoubleChest is false there and this method is never entered at all.
        // ------------------------------------------------------------------
        rows.add(new IntegrationPlugin.Scenario("bothHalvesOfAZAxisDoubleChestResolveToEachOther", () -> {
            Block[] halves = doubleChest(plugin, SITE.at(5), false);
            Block left = halves[0], right = halves[1];

            Assert.that(Sync.get(plugin, () -> ShopChest.isDoubleChest(left)),
                    "precondition: the server built one double chest out of the two blocks");

            Assert.equal(right, Sync.get(plugin, () -> ShopChest.getOtherHalfOfDoubleChest(left)),
                    "the other half of the left block is the right block - ShopChest.java:109 compares "
                            + "check.getX() against chestLoc.getZ()");
            Assert.equal(left, Sync.get(plugin, () -> ShopChest.getOtherHalfOfDoubleChest(right)),
                    "and the other half of the right block is the left block");
        }));

        rows.add(new IntegrationPlugin.Scenario("bothHalvesOfAnXAxisDoubleChestResolveToEachOther", () -> {
            Block[] halves = doubleChest(plugin, SITE.at(6), true);
            Block left = halves[0], right = halves[1];

            Assert.that(Sync.get(plugin, () -> ShopChest.isDoubleChest(left)),
                    "precondition: the server built one double chest out of the two blocks");

            // The control, and it is expected to be GREEN. It is here so that a red
            // Z row reads as "the Z branch is wrong" rather than "double chests are
            // broken", and because it answers the second half of the report:
            // ShopChest.java:105 takes two references from
            // getInventory().getLocation(), and if a server handed out the SAME
            // object twice, :107's setX would move the value :107 is still reading.
            // Paper 1.21.11 hands out two, so that half does not reproduce - and
            // this row is what will say so if a future server changes its mind.
            Assert.equal(right, Sync.get(plugin, () -> ShopChest.getOtherHalfOfDoubleChest(left)),
                    "the other half of the left block is the right block");
            Assert.equal(left, Sync.get(plugin, () -> ShopChest.getOtherHalfOfDoubleChest(right)),
                    "and the other half of the right block is the left block");
        }));

        // ------------------------------------------------------------------
        // A shop read back off disk loses its cost side. Same symptom class as
        // "my shop reverted", and it was noticed from a shop file carrying
        // `cost: []` rather than from a report.
        //
        // Shop.deserialize:251-292 walks the keys of a FlatFileSection, which is a
        // LIVE VIEW of the file: SimplixStorage's FlatFile defaults to
        // ReloadSettings.INTELLIGENT, so every get() on it calls reloadIfNeeded()
        // and re-reads the file the moment its mtime moves.
        //
        // The loop moves that mtime itself. `case "product"` at :264 adds each item
        // through Shop.addSideItem, which ends at saveShop:1235 - and the shop it
        // saves is the half-built one, product only. From that write onwards every
        // remaining key is answered out of the file that was just written over the
        // one being read, so `cost` comes back as the empty list that write
        // contained. The damage is then saved again, so it is permanent: the shop
        // reloads INCOMPLETE and stops trading until its owner sets the cost again.
        //
        // MEASURED, on this server, twice: of the fifteen shops a full run leaves on
        // disk, exactly one has `cost: []`, and it is the only one any scenario ever
        // read back off the file rather than out of DataStorage's shopCache.
        //
        // Not reachable at tier 1: MockBukkit has no plugin data folder full of real
        // JSON, and the defect is a file's mtime moving under a reader.
        // ------------------------------------------------------------------
        rows.add(new IntegrationPlugin.Scenario("aShopReadBackFromDiskStillTakesItsCost", () -> {
            RealShop scene = new RealShop(plugin, SITE.at(7));
            scene.placeChestAndSign();
            scene.createShopByCommand("1 DIAMOND", "1 EMERALD");

            ShopLocation where = scene.get(() -> new ShopLocation(scene.signBlock().getLocation()));

            Shop live = scene.get(() -> Shop.loadShop(where));
            Assert.that(live != null, "precondition: the command created a shop");
            Assert.equal(1, live.getSideList(ShopItemSide.COST).size(),
                    "precondition: the shop the running plugin holds takes one emerald");

            // The restart, without restarting. A brand new DataStorage has an empty
            // shopCache, so this read comes off the file - the same path a starting
            // server takes for every shop it has.
            Shop reloaded = scene.get(() -> new DataStorage(DataType.FLATFILE).loadShopFromSign(where));

            Assert.that(reloaded != null, "the shop should still be on disk");
            Assert.equal(1, reloaded.getSideList(ShopItemSide.PRODUCT).size(),
                    "the product side should come back from disk");
            Assert.equal(1, reloaded.getSideList(ShopItemSide.COST).size(),
                    "and so should the cost side - Shop.deserialize adds the product through "
                            + "addSideItem, which saves, and that save rewrites the very file the "
                            + "loop is still reading from");
            Assert.equal(Material.EMERALD,
                    reloaded.getSideList(ShopItemSide.COST).get(0).getItemStack().getType(),
                    "and it should still be the emerald the shop was created to take");
            Assert.that(!reloaded.isMissingItems(),
                    "a shop with both sides must not come back off disk as incomplete");

            // The file, not just the object: the load wrote what it had read, so a
            // second read gets the same answer whatever this one said.
            Assert.equal(1, scene.get(() -> new DataStorage(DataType.FLATFILE).loadShopFromSign(where))
                            .getSideList(ShopItemSide.COST).size(),
                    "and the file must not have been rewritten without the cost side, or the "
                            + "loss outlives the read that caused it");
        }));

        // ------------------------------------------------------------------
        // A shop with anyone on it but its owner cannot be loaded at all.
        //
        // Shop.deserialize read `managers` and `members` with
        // getSerializableList(key, UUID.class), which maps every element of the
        // stored list through SimplixSerializer.deserialize(element, UUID.class).
        // That looks the TARGET class up in a registry TradeShop never registers
        // UUID in, so a list with anything in it threw
        //
        //   de.leonhard.storage.internal.exceptions.SimplixValidationException:
        //   No serializable found for 'UUID'
        //
        // An empty list never enters the mapping function, and every shop this
        // harness built had neither a manager nor a member - which is why two
        // units about shop loading passed over it. The plugin has commands to add
        // both, so every shop anybody has ever shared is a shop the server cannot
        // load: it stops trading, and its owner's evidence is that it "just
        // stopped".
        //
        // Reachable at tier 1 as well and asserted there too. It is here because
        // the two rows below are what an operator actually does - a shop shared
        // from the chat bar, then a restart, then /tradeshop find - and because a
        // real server is where the in-memory read path exists at all.
        // ------------------------------------------------------------------

        rows.add(new IntegrationPlugin.Scenario("aShopWithAManagerComesBackOffDisk", () -> {
            RealShop scene = new RealShop(plugin, SITE.at(8));
            scene.placeChestAndSign();
            scene.createShopByCommand("1 DIAMOND", "1 EMERALD");

            ShopLocation where = scene.get(() -> new ShopLocation(scene.signBlock().getLocation()));

            Assert.that(scene.get(() -> Shop.loadShop(where).addUser(MANAGER, ShopRole.MANAGER)),
                    "precondition: the owner could add a manager to their own shop");

            // The restart, without restarting: a brand new DataStorage has an empty
            // shopCache, so this read comes off the file - the path a starting
            // server takes for every shop it has.
            Shop reloaded;
            try {
                reloaded = scene.get(() -> new DataStorage(DataType.FLATFILE).loadShopFromSign(where));
            } catch (Throwable t) {
                throw new AssertionError("a shop that has been shared with one other player cannot "
                        + "be read back off disk at all, so it is gone the next time the server "
                        + "starts: " + rootCause(t), t);
            }

            Assert.that(reloaded != null, "a shop with a manager should still be on disk");
            Assert.that(reloaded.getUsersUUID(ShopRole.MANAGER).contains(MANAGER),
                    "and the manager the owner added must still be on it");
            Assert.equal(1, reloaded.getSideList(ShopItemSide.PRODUCT).size(),
                    "and the rest of the shop must come back with them");
        }));

        rows.add(new IntegrationPlugin.Scenario("aShopWithAMemberIsFoundByTheChunkSearch", () -> {
            RealShop scene = new RealShop(plugin, SITE.at(9));
            scene.placeChestAndSign();
            scene.createShopByCommand("1 DIAMOND", "1 EMERALD");

            ShopLocation where = scene.get(() -> new ShopLocation(scene.signBlock().getLocation()));

            Assert.that(scene.get(() -> Shop.loadShop(where).addUser(MEMBER, ShopRole.MEMBER)),
                    "precondition: the owner could add a member to their own shop");

            // The OTHER read path, and it holds different objects. The save put the
            // map Shop.serialize built straight into the storage layer's in-memory
            // document, where the list still holds java.util.UUID objects rather
            // than the strings the file holds. Everything reading that chunk is
            // answered out of it until the file is re-read.
            //
            // Save and search inside ONE task, for the reason
            // ConfigAndMetricsRows.searchingAChunkFindsTheShopsInIt gives: a
            // ChunkUnloadEvent landing between them drops the cached chunk data and
            // turns this into a second reading off disk, which is a coin toss the
            // row must not be decided by.
            List<Shop> found;
            try {
                found = Sync.get(plugin, () -> {
                    tradeShop().getDataStorage().saveShop(Shop.loadShop(where));
                    return tradeShop().getDataStorage().getMatchingShopsInChunk(
                            scene.signBlock().getChunk().getChunkSnapshot(), false, null, null);
                });
            } catch (Throwable t) {
                throw new AssertionError("/tradeshop find throws in any chunk holding a shop that "
                        + "has been shared, because the shop is read back out of the storage "
                        + "layer's own in-memory copy of what was just written: " + rootCause(t), t);
            }

            Assert.equal(1, found.size(), "searching the chunk must find the shared shop");
            Assert.that(found.get(0).getUsersUUID(ShopRole.MEMBER).contains(MEMBER),
                    "and it must come back with the member on it");
        }));

        return rows;
    }

    // ------------------------------------------------------------------
    // Scene helpers
    // ------------------------------------------------------------------

    /**
     * Two chest blocks the server has joined into one double chest.
     *
     * <p>Vanilla joins two chests when their facings agree and their halves point
     * at each other: the partner of a LEFT chest is clockwise of its facing and of
     * a RIGHT chest counter-clockwise. A north-facing pair therefore runs along X
     * and an east-facing pair along Z, which is the only reason the facing differs
     * between the two cases.
     *
     * @param alongX true for a pair laid along X, false for one laid along Z
     * @return the LEFT half and the RIGHT half, in that order
     */
    private static Block[] doubleChest(IntegrationPlugin plugin, int site, boolean alongX) {
        return Sync.get(plugin, () -> {
            World world = Bukkit.getWorlds().get(0);
            int x = site * 1000;

            BlockFace facing = alongX ? BlockFace.NORTH : BlockFace.EAST;

            Block left = world.getBlockAt(x, 0, 0);
            Block right = alongX ? world.getBlockAt(x + 1, 0, 0) : world.getBlockAt(x, 0, 1);

            left.setType(Material.CHEST, false);
            right.setType(Material.CHEST, false);

            setHalf(left, facing, Chest.Type.LEFT);
            setHalf(right, facing, Chest.Type.RIGHT);

            // Logged because it is the number the Z branch gets wrong, and reading
            // it out of a run is worth more than deriving it from the source.
            Location inventoryAt = ((Container) left.getState()).getInventory().getLocation();
            Bukkit.getLogger().info("[harness] double chest along " + (alongX ? "X" : "Z")
                    + ": left " + left.getLocation().toVector() + ", right " + right.getLocation().toVector()
                    + ", combined inventory at " + (inventoryAt == null ? "null" : inventoryAt.toVector()));

            return new Block[]{left, right};
        });
    }

    private static void setHalf(Block block, BlockFace facing, Chest.Type half) {
        Chest data = (Chest) block.getBlockData();
        data.setFacing(facing);
        data.setType(half);
        block.setBlockData(data, false);
    }

    /** Adds one storage type to allowed-shops and makes the running plugin notice. */
    private static void allowStorage(RealShop scene, String storage) {
        scene.run(() -> {
            List<String> allowed = new ArrayList<>(Arrays.asList("CHEST", "TRAPPED_CHEST", "SHULKER"));
            allowed.add(storage);
            Setting.ALLOWED_SHOPS.setValue(allowed);
            tradeShop().getListManager().reload();
        });
    }

    private static void restoreAllowedShops(IntegrationPlugin plugin, Object allowed) {
        Sync.run(plugin, () -> {
            Setting.ALLOWED_SHOPS.setValue(allowed);
            tradeShop().getListManager().reload();
        });
    }

    private static int storageSlots(RealShop scene) {
        return scene.get(() -> ((Container) scene.chestBlock().getState())
                .getInventory().getStorageContents().length);
    }

    private static TradeShop tradeShop() {
        return (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
    }

    /**
     * The exception a wrapper was hiding.
     *
     * <p>Bukkit's {@code PluginCommand.execute} rethrows whatever a command threw
     * as a {@code CommandException} whose own message names only the command, so a
     * row that drives a command and reports what it caught would otherwise say
     * nothing at all about the defect.
     */
    private static String rootCause(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause.toString();
    }

    private static void save(YamlConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            throw new AssertionError("could not write " + file + ": " + e);
        }
    }

    /**
     * Puts the real config.yml back.
     *
     * <p>Silent on failure on purpose: this runs in a finally, and an exception
     * thrown here would replace the row's own verdict with a housekeeping error.
     * The restore is asserted by the rows that come after it rather than by this
     * method - a run where it did not happen goes red somewhere downstream.
     */
    private static void restore(File backup, File config) {
        try {
            if (backup.isFile()) {
                Files.copy(backup.toPath(), config.toPath(), StandardCopyOption.REPLACE_EXISTING);
                backup.delete();
            }
        } catch (IOException ignored) {
        }
    }
}
