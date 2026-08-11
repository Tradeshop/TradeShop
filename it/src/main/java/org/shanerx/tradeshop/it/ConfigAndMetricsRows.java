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
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shop.ShopChest;
import org.shanerx.tradeshop.shop.ShopStatus;
import org.shanerx.tradeshop.shoplocation.ShopLocation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * The rows for what a config save does to an operator's file, what the storage
 * layer says when it is asked how many shops it is holding, and the two storage
 * defects beside them.
 *
 * <h2>Why every one of them is here and not in {@code src/test}</h2>
 * <ul>
 *   <li><b>The config save</b> is a real {@code config.yml} an operator has
 *       edited, rewritten by the same {@code ConfigManager.reload()} that
 *       {@code TradeShop.onEnable} calls at {@code TradeShop.java:139}. The defect
 *       is what a boot does to a file, so a test with no file proves nothing about
 *       it.</li>
 *   <li><b>The counts and the search</b> read a real chunk file back through the
 *       storage library the plugin shades. What they get wrong is the shape of the
 *       document that library hands back, which only exists once a shop has been
 *       serialised to disk and parsed again.</li>
 *   <li><b>The double chest</b> needs a real double chest. MockBukkit has no
 *       {@code DoubleChest}, so {@code ShopChest.isDoubleChest} answers false there
 *       and the branch under test is never entered at all.</li>
 *   <li><b>The stored status</b> is what {@code /tradeshop find} reads: a shop
 *       parsed back out of the store rather than the live object the plugin is
 *       holding. That needs a store.</li>
 * </ul>
 *
 * <p>Every row asserts what an operator is entitled to, so a red row is the
 * defect and a green row is the fix. None of them was written to pass today.
 */
final class ConfigAndMetricsRows {

    private ConfigAndMetricsRows() {
    }

    /**
     * These rows' patch of the world. Reserved through {@link SiteAllocator}
     * rather than agreed by comment - see that class for why a hand-typed
     * range stopped being trustworthy.
     */
    private static final SiteAllocator.Reservation SITE = SiteAllocator.reserve("ConfigAndMetricsRows", 7);

    static List<IntegrationPlugin.Scenario> rows(IntegrationPlugin plugin) {
        List<IntegrationPlugin.Scenario> rows = new ArrayList<>();

        // ------------------------------------------------------------------
        // Any boot that writes config.yml throws away every setting whose default is
        // a map, whatever the operator put there.
        //
        // Setting.getFileString:346-348 is the whole of it. A setting whose default
        // is NOT a map is rendered from getSetting() at :350 - the value that was
        // loaded from the file - and one whose default IS a map is rendered from
        // `defaultValue`, the constant compiled into the enum. ConfigManager.save
        // rebuilds the entire file out of those strings (:180-197) and then reloads
        // itself from what it just wrote (:204), so the operator's value is gone
        // from the file and from memory in the same breath.
        //
        // SIX settings are map-valued, not the three with obvious names:
        // shop-per-item-settings, max-shops-per-player, sign-default-colours, and
        // the per-shop settings of all THREE shop types. Every one of them is
        // edited below.
        //
        // A save is not a rare event. ConfigManager.reload:116 saves whenever
        // load() or Setting.upgrade() reports a change, load() -> setDefaults()
        // reports one whenever a single key is missing from the file, and that is
        // the ordinary state of a config.yml written by an older build of the
        // plugin. The boot after an upgrade is therefore the boot that reverts the
        // operator, which is the worst possible timing: it is also the boot where
        // they are least likely to look.
        //
        // max-shops-per-player is the sharpest of the six, because its whole
        // purpose is keys the enum default has never heard of. The default map is
        // {default: -1}; an operator adds one entry per permission group, and
        // ListManager.reload:210 turns each into a tradeshop.limit.* permission. A
        // save writes {default: -1} and every group limit on the server is gone.
        // ------------------------------------------------------------------
        rows.add(new IntegrationPlugin.Scenario("anOperatorsTunedSettingsSurviveABootThatWritesTheFile", () -> {
            TradeShop tradeShop = tradeShop();
            File config = tradeShop.getSettingManager().getFile();
            File backup = new File(config.getParentFile(), config.getName() + ".tuned-backup");

            String perItem = Setting.SHOP_PER_ITEM_SETTINGS.getPath();
            String perPlayer = Setting.MAX_SHOPS_PER_PLAYER.getPath();
            String colours = Setting.SHOP_SIGN_DEFAULT_COLOURS.getPath();
            String trade = Setting.TRADE_PER_SHOP_SETTINGS.getPath();
            String biTrade = Setting.BITRADE_PER_SHOP_SETTINGS.getPath();
            String iTrade = Setting.ITRADE_PER_SHOP_SETTINGS.getPath();

            // A scene only so that the config edits are marshalled onto the server
            // thread the same way every other touch in this tier is.
            RealShop scene = new RealShop(plugin, SITE.at(0));
            scene.placeChestAndSign();

            try {
                Files.copy(config.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);

                scene.run(() -> {
                    YamlConfiguration tuned = YamlConfiguration.loadConfiguration(config);

                    // What an operator does: opens config.yml and changes things.
                    tuned.set(perItem + ".compare-durability.default", 2);
                    tuned.set(perItem + ".compare-name.user-editable", false);
                    tuned.set(perPlayer + ".default", 12);
                    tuned.set(perPlayer + ".vip", 40);
                    tuned.set(colours + ".oak", "&5");
                    tuned.set(trade + ".hopper-export.default", true);
                    tuned.set(biTrade + ".hopper-import.default", true);
                    tuned.set(iTrade + ".no-cost.user-editable", false);

                    // And the hole that makes the next boot write the file at all,
                    // standing in for the key a plugin upgrade adds. Any one
                    // missing key is enough: ConfigManager.addKeyValue writes it,
                    // setDefaults answers true, and reload() saves.
                    tuned.set(Setting.MAX_SHOP_USERS.getPath(), null);

                    save(tuned, config);
                });

                // Exactly what TradeShop.java:139 does on every boot.
                scene.run(() -> tradeShop.getSettingManager().reload());

                Assert.that(Setting.MAX_SHOP_USERS.getInt() > 0,
                        "precondition: the boot repaired the missing key, which is what made it "
                                + "write the file - without that write there is nothing to assert");

                Assert.equal(2, Setting.SHOP_PER_ITEM_SETTINGS.getMappedObject("compare-durability.default"),
                        "an operator's per-item comparison mode must survive a boot that writes the file");
                Assert.equal(false, Setting.SHOP_PER_ITEM_SETTINGS.getMappedObject("compare-name.user-editable"),
                        "and so must a comparison the operator has locked against per-item overrides");
                Assert.equal(12, Setting.MAX_SHOPS_PER_PLAYER.getMappedObject("default"),
                        "and so must the shop limit the operator set");
                Assert.equal(40, Setting.MAX_SHOPS_PER_PLAYER.getMappedObject("vip"),
                        "and so must a per-group limit, which is a key the enum default has never "
                                + "held and which every tradeshop.limit.* permission is built from");
                Assert.equal("&5", Setting.SHOP_SIGN_DEFAULT_COLOURS.getMappedObject("oak"),
                        "and so must the colour the operator chose for oak signs");
                Assert.equal(true, Setting.TRADE_PER_SHOP_SETTINGS.getMappedObject("hopper-export.default"),
                        "and so must a per-shop default for trade shops");
                Assert.equal(true, Setting.BITRADE_PER_SHOP_SETTINGS.getMappedObject("hopper-import.default"),
                        "and so must one for bitrade shops - there are six map settings, not the three "
                                + "with obvious names");
                Assert.equal(false, Setting.ITRADE_PER_SHOP_SETTINGS.getMappedObject("no-cost.user-editable"),
                        "and so must one for itrade shops");

                // The file itself, not only the config the plugin is holding. Those
                // are the same object today - save() reloads from what it wrote -
                // but the operator's complaint is about the file, so the file is
                // what gets read.
                YamlConfiguration onDisk = scene.get(() -> YamlConfiguration.loadConfiguration(config));
                Assert.equal(40, onDisk.get(perPlayer + ".vip"),
                        "and the file on disk must still hold it after the boot, or the next editor "
                                + "to open config.yml finds their work gone");
                Assert.equal("&5", onDisk.get(colours + ".oak"),
                        "and so must the sign colour");
            } catch (IOException e) {
                throw new AssertionError("could not stand up an operator's config.yml: " + e);
            } finally {
                restore(backup, config);
                scene.run(() -> tradeShop.getSettingManager().reload());
            }
        }));

        // ------------------------------------------------------------------
        // A chunk holding one shop reports twenty-two of them.
        //
        // JsonShopData.size:102-103 is keySet().size(), and the keySet() it
        // inherits is the storage library's DEEP one: FileData.keySet walks into
        // every nested map and answers a dotted path per LEAF. A serialised shop is
        // a nested document - owner, shopLoc, chestLoc, both item lists, the
        // per-shop settings - so one shop in a chunk file is twenty-two keys, and
        // the number scales with what a shop happens to carry rather than with how
        // many there are. singleLayerKeySet() is the same library's shallow one and
        // is what this method wanted.
        //
        // What that costs an operator: Utils.java:532 refuses to build a shop once
        // max-shops-per-chunk is reached, comparing the setting against this
        // number. The default is 128, so the real ceiling is about five shops in a
        // chunk, and the sixth player to build in a busy market square is told the
        // chunk is full when it holds five shops.
        // ------------------------------------------------------------------
        rows.add(new IntegrationPlugin.Scenario("theShopCountForAChunkCountsShopsRatherThanTheirFields", () -> {
            RealShop scene = new RealShop(plugin, SITE.at(1));
            scene.placeChestAndSign();

            Assert.equal(0, chunkCount(plugin, scene),
                    "precondition: nothing has been built in this chunk yet");

            scene.createShopByCommand("1 DIAMOND", "1 EMERALD");

            Assert.equal(1, chunkCount(plugin, scene),
                    "a chunk holding one shop holds one shop - JsonShopData.size:102-103 counts "
                            + "the leaves of the whole chunk document instead, so every field of "
                            + "every shop is counted as a shop and max-shops-per-chunk "
                            + "(Utils.java:532) fills up about twenty-two times too early");
        }));

        // ------------------------------------------------------------------
        // The bStats shop counter has always reported zero.
        //
        // DataStorage.getShopCountInWorld:277-298 schedules the counting with
        // runTaskAsynchronously and returns count.get() on the line after the
        // schedule, before the task has been given a thread. The AtomicInteger it
        // reads is the one the task will later fill in, and nothing ever looks at
        // it again.
        //
        // VarManager.startup:89-91 is the only caller: one adjustShops per world at
        // enable, each adding zero. shopCounter therefore starts at zero on every
        // server whatever is on disk, and MetricsManager.java:75 publishes it as
        // the "shop-counter" chart. Shops created and destroyed while the server is
        // up do move it - PlayerShopCreateEvent:53 and PlayerShopDestroyEvent:53 -
        // so the number is not constant, it is just missing every shop that existed
        // before the server started.
        //
        // Asserted as a delta rather than an absolute, because earlier rows have
        // left shops all over this world and the count that matters is the one this
        // row can account for. A delta of two also fails a "fix" that returns any
        // constant, which "greater than zero" would not.
        // ------------------------------------------------------------------
        rows.add(new IntegrationPlugin.Scenario("theShopCountForAWorldCountsTheShopsInIt", () -> {
            World world = Sync.get(plugin, () -> Bukkit.getWorlds().get(0));

            int before = Sync.get(plugin, () -> tradeShop().getDataStorage().getShopCountInWorld(world));

            RealShop first = new RealShop(plugin, SITE.at(2));
            first.placeChestAndSign();
            first.createShopByCommand("1 DIAMOND", "1 EMERALD");

            RealShop second = new RealShop(plugin, SITE.at(3));
            second.placeChestAndSign();
            second.createShopByCommand("1 DIAMOND", "1 EMERALD");

            int after = Sync.get(plugin, () -> tradeShop().getDataStorage().getShopCountInWorld(world));

            Assert.that(after > 0,
                    "a world with shops in it must not count zero of them - "
                            + "DataStorage.getShopCountInWorld:297 returns an AtomicInteger the "
                            + "async task it just scheduled has not touched yet, and that zero is "
                            + "what bStats has been publishing");
            Assert.equal(before + 2, after,
                    "and creating two shops must move the count by exactly two, which is the "
                            + "number VarManager.startup:90 hands to the shop-counter chart");
        }));

        // ------------------------------------------------------------------
        // Searching a chunk for shops throws.
        //
        // JsonShopData.list:95-99 deserialises every key the same deep keySet()
        // answers, so it hands ShopLocation.deserialize paths like
        // "l::world::44000::1::0.status". That splits on "::" into five parts whose
        // last is "0.status", ObjectHolder.asInteger:120 cannot parse it and answers
        // null, and ShopLocation.java:82 unboxes the null:
        //
        //   Cannot invoke "java.lang.Integer.intValue()" because the return value of
        //   "org.shanerx.tradeshop.utils.objects.ObjectHolder.asInteger()" is null
        //
        // measured on this server. DataStorage.getMatchingShopsInChunk:262 is the
        // only caller and ShopUser.findProximityShop:181 is the only caller of that,
        // so /tradeshop find throws for any chunk that holds a shop - which is every
        // chunk it is worth searching.
        //
        // THE SECOND DEFECT ON THE SAME CALL, found because this row was green here
        // and red in CI on the same commit: Shop.serialize:382 hands the storage
        // layer the live Sets the shop holds its managers and members in, and
        // Shop.deserialize:274 reads them back with a cast to List. Which of those
        // two the search hits depends on whether the chunk file has been re-read
        // since the save, so the row could only see it when it lost a race. The
        // second half of this scenario removes the race rather than the assertion.
        // ------------------------------------------------------------------
        rows.add(new IntegrationPlugin.Scenario("searchingAChunkFindsTheShopsInIt", () -> {
            RealShop scene = new RealShop(plugin, SITE.at(4));
            scene.placeChestAndSign();
            scene.createShopByCommand("1 DIAMOND", "1 EMERALD");

            ShopLocation where = scene.get(() -> new ShopLocation(scene.signBlock().getLocation()));

            List<Shop> found = shopsInChunk(plugin, scene);

            Assert.equal(1, found.size(),
                    "searching the chunk a shop was just built in must find that shop and nothing "
                            + "else - JsonShopData.list:95-99 walks the same deep keySet() and asks "
                            + "ShopLocation.deserialize to read a shop location out of every field "
                            + "path in the file, which is what makes /tradeshop find throw");
            Assert.equal(where.toString(), found.get(0).getShopLocationAsSL().toString(),
                    "and the shop it finds is the one that is there");

            // THE SAME SEARCH WITH NO TICK BETWEEN THE SAVE AND IT, and this is the
            // half of the row that is worth having. The search above runs in its own
            // scheduled task, so a ChunkUnloadEvent can land between the shop being
            // written and the chunk being searched - and one that does is a repair:
            // ChunkUnloadListener drops the chunk from DataStorage's per-chunk cache,
            // the next getShopData builds a fresh JsonShopData, and it reads the file
            // off disk instead of the storage layer's in-memory copy of what was just
            // written. Whether that happened was a coin toss, and it decided the row:
            // this branch was green three times running here and red in two of three
            // CI runs OF THE SAME COMMIT.
            //
            // Doing both inside one Sync.get removes the window. Events run on the
            // server thread, a task owns that thread for its whole body, so the cache
            // holds the instance the save wrote through and the search is answered
            // out of memory - which is what a real /tradeshop find does after any
            // trade in a chunk nobody has walked out of.
            List<Shop> withoutAWindow = Sync.get(plugin, () -> {
                Shop live = Shop.loadShop(where);
                tradeShop().getDataStorage().saveShop(live);
                return tradeShop().getDataStorage().getMatchingShopsInChunk(
                        scene.signBlock().getChunk().getChunkSnapshot(), false, null, null);
            });

            Assert.equal(1, withoutAWindow.size(),
                    "searching a chunk in the same tick a shop in it was saved must find that "
                            + "shop - Shop.serialize:382 handed the storage layer the live Set the "
                            + "shop holds its managers in, and Shop.deserialize:274 reads it back "
                            + "with getSerializableList, whose first act is (List) get(key). Off "
                            + "disk that value is an array and the round trip works; before the "
                            + "file is re-read it is still the Set, and /tradeshop find throws "
                            + "ClassCastException: java.util.HashSet cannot be cast to "
                            + "java.util.List");
            Assert.equal(where.toString(), withoutAWindow.get(0).getShopLocationAsSL().toString(),
                    "and it is still the shop that is there");
        }));

        // ------------------------------------------------------------------
        // Unlinking half a double chest leaves the other half linked.
        //
        // LinkageConfiguration.add:59-67 links BOTH halves of a double chest, on
        // purpose: either block has to resolve to the shop, because either is what a
        // player clicks or a hopper feeds. removeChest:78-81 removes ONE key and
        // saves. The two are not each other's inverse, and nothing else ever removes
        // the second entry.
        //
        // What that costs: DataStorage.removeChestLinkage:326 is what a player
        // reaches by breaking half of a shop's storage, and Shop.removeStorage:658
        // by unlinking the whole of it. Either way the surviving half still maps to
        // the shop's sign, so ShopChest.isShopChest answers true for a block that no
        // longer carries a shop - which is the protection path
        // (ShopProtectionListener) and the hopper path both being told the block is
        // still in use.
        // ------------------------------------------------------------------
        rows.add(new IntegrationPlugin.Scenario("unlinkingADoubleChestUnlinksBothOfItsHalves", () -> {
            Block[] halves = doubleChest(plugin, SITE.at(5));
            Block left = halves[0], right = halves[1];

            Assert.that(Sync.get(plugin, () -> ShopChest.isDoubleChest(left)),
                    "precondition: the server joined the two blocks into one double chest");

            ShopLocation shopSign = Sync.get(plugin, () -> {
                Block sign = left.getRelative(BlockFace.UP);
                sign.setType(Material.OAK_SIGN, false);
                return new ShopLocation(sign.getLocation());
            });

            ShopLocation leftLoc = Sync.get(plugin, () -> new ShopLocation(left.getLocation()));
            ShopLocation rightLoc = Sync.get(plugin, () -> new ShopLocation(right.getLocation()));

            Sync.run(plugin, () -> tradeShop().getDataStorage().addChestLinkage(leftLoc, shopSign));

            Assert.that(Sync.get(plugin, () -> tradeShop().getDataStorage().getChestLinkage(leftLoc)) != null,
                    "precondition: linking the chest linked the half it was given");
            Assert.that(Sync.get(plugin, () -> tradeShop().getDataStorage().getChestLinkage(rightLoc)) != null,
                    "precondition: and LinkageConfiguration.add:60-63 linked the other half too, "
                            + "which is the whole reason unlinking has two entries to deal with");

            Sync.run(plugin, () -> tradeShop().getDataStorage().removeChestLinkage(leftLoc));

            Assert.that(Sync.get(plugin, () -> tradeShop().getDataStorage().getChestLinkage(leftLoc)) == null,
                    "unlinking the chest must unlink the half it was given");
            Assert.that(Sync.get(plugin, () -> tradeShop().getDataStorage().getChestLinkage(rightLoc)) == null,
                    "and the other half as well - removeChest:78-81 removes one key where add:59-67 "
                            + "wrote two, so the surviving entry outlives the shop and the block goes "
                            + "on reading as a shop chest to every path that asks");
            Assert.that(!Sync.get(plugin, () -> ShopChest.isShopChest(right)),
                    "and neither block may still read as a shop chest");
        }));

        // ------------------------------------------------------------------
        // A shop's status is written to disk one update out of date.
        //
        // Shop.saveShop:489-499 calls updateFullTradeCount(), then serialises the
        // shop - status and all, Shop.java:388 - and only THEN calls updateSign(),
        // which is the one path that recomputes status
        // (updateSignLines:592 -> updateStatus). So the value in the file is
        // whatever the status was before this save, and the sign the player is
        // looking at disagrees with it.
        //
        // A shop built from the chat bar is the plain case. `create` leaves it
        // INCOMPLETE - the field's initial value, Shop.java:83 - and the setProduct
        // and setCost that follow each write the status they inherited. The shop
        // ends up complete, its sign says so, and the store still says INCOMPLETE.
        //
        // That stored value is not decoration. DataStorage.getMatchingShopsInChunk
        // :262-272 reads shops back with loadASync, which does not recompute
        // anything, and filters on the status it finds; ShopUser.findProximityShop
        // :181 is /tradeshop find. So a shop that is merely out of stock reads as
        // broken to the one command a player uses to go looking for shops, and it
        // stays that way until something re-saves it after a sign update.
        // ------------------------------------------------------------------
        rows.add(new IntegrationPlugin.Scenario("aShopStoresTheStatusItActuallyHas", () -> {
            RealShop scene = new RealShop(plugin, SITE.at(6));
            scene.placeChestAndSign();
            scene.createShopByCommand("1 DIAMOND", "1 EMERALD");

            ShopLocation where = scene.get(() -> new ShopLocation(scene.signBlock().getLocation()));

            Shop live = scene.get(() -> Shop.loadShop(where));
            Assert.that(live != null, "precondition: the command created a shop");
            Assert.that(scene.get(() -> !live.isMissingItems()),
                    "precondition: the shop has both sides, so it is not incomplete");
            Assert.equal(ShopStatus.OUT_OF_STOCK, live.getStatus(),
                    "precondition: an unstocked shop with both sides is out of stock, and that is "
                            + "what its sign says");

            Shop stored = storedShopAt(plugin, scene, where);
            Assert.that(stored != null,
                    "precondition: searching the chunk finds the shop that is in it");
            Assert.equal(ShopStatus.OUT_OF_STOCK, stored.getStatus(),
                    "the status in the store must be the status the shop has - saveShop:495-497 "
                            + "serialises the shop before updateSign() recomputes it, so a shop "
                            + "created from the chat bar is stored as INCOMPLETE and reads as broken "
                            + "to /tradeshop find");

            // The same question after the shop is stocked, which is the state an
            // owner leaves a working shop in.
            scene.stockShop(new ItemStack(Material.DIAMOND, 10));
            scene.closeChestAsOwner();

            Assert.eventually(5000L, "the stocked shop to report itself open",
                    () -> ShopStatus.OPEN.equals(scene.get(() -> Shop.loadShop(where)).getStatus()));

            Assert.equal(ShopStatus.OPEN, storedShopAt(plugin, scene, where).getStatus(),
                    "and an open shop must be stored open, or /tradeshop find never returns it");
        }));

        return rows;
    }

    // ------------------------------------------------------------------
    // Scene helpers
    // ------------------------------------------------------------------

    private static int chunkCount(IntegrationPlugin plugin, RealShop scene) {
        return Sync.get(plugin, () -> tradeShop().getDataStorage()
                .getShopCountInChunk(scene.signBlock().getChunk()));
    }

    /**
     * The shops in the scene's chunk as {@code /tradeshop find} sees them: parsed
     * back out of the store rather than handed over from the live objects the
     * plugin holds.
     *
     * <p>{@code getMatchingShopsInChunk} is the call the command makes, and it goes
     * through {@code loadASync}, which deserialises and fixes up without ever
     * recomputing status. {@code loadShopFromSign} would be the wrong question for
     * the status rows - it runs {@code fixAfterLoad}, which updates the sign and
     * therefore the status, so it repairs the very value they are asking about.
     */
    private static List<Shop> shopsInChunk(IntegrationPlugin plugin, RealShop scene) {
        return Sync.get(plugin, () -> tradeShop().getDataStorage().getMatchingShopsInChunk(
                scene.signBlock().getChunk().getChunkSnapshot(), false, null, null));
    }

    private static Shop storedShopAt(IntegrationPlugin plugin, RealShop scene, ShopLocation where) {
        for (Shop shop : shopsInChunk(plugin, scene)) {
            if (where.toString().equals(shop.getShopLocationAsSL().toString())) {
                return shop;
            }
        }
        return null;
    }

    /**
     * Two chest blocks the server has joined into one double chest, laid along X.
     *
     * <p>Vanilla joins two chests when their facings agree and their halves point at
     * each other: the partner of a LEFT chest is clockwise of its facing and of a
     * RIGHT chest counter-clockwise, so a north-facing pair runs along X.
     *
     * @return the LEFT half and the RIGHT half, in that order
     */
    private static Block[] doubleChest(IntegrationPlugin plugin, int site) {
        return Sync.get(plugin, () -> {
            World world = Bukkit.getWorlds().get(0);
            int x = site * 1000;

            Block left = world.getBlockAt(x, 0, 0);
            Block right = world.getBlockAt(x + 1, 0, 0);

            left.setType(Material.CHEST, false);
            right.setType(Material.CHEST, false);

            setHalf(left, BlockFace.NORTH, Chest.Type.LEFT);
            setHalf(right, BlockFace.NORTH, Chest.Type.RIGHT);

            return new Block[]{left, right};
        });
    }

    private static void setHalf(Block block, BlockFace facing, Chest.Type half) {
        Chest data = (Chest) block.getBlockData();
        data.setFacing(facing);
        data.setType(half);
        block.setBlockData(data, false);
    }

    private static TradeShop tradeShop() {
        return (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
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
