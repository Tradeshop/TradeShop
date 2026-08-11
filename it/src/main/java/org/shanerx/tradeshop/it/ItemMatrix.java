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

import de.leonhard.storage.Json;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.shanerx.tradeshop.data.storage.DataStorage;
import org.shanerx.tradeshop.data.storage.DataType;
import org.shanerx.tradeshop.item.ShopItemSide;
import org.shanerx.tradeshop.item.ShopItemStack;
import org.shanerx.tradeshop.item.ShopItemStackSettingKeys;
import org.shanerx.tradeshop.player.ShopRole;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shop.ShopStatus;
import org.shanerx.tradeshop.shoplocation.ShopLocation;
import org.shanerx.tradeshop.utils.gsonprocessing.GsonProcessor;
import org.shanerx.tradeshop.utils.simplix.serializers.ConfSerSerializer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * The item-metadata matrix, at the tier where it means something.
 *
 * <h2>Why every one of these is here and not in {@code src/test}</h2>
 * MockBukkit is not a weaker CraftBukkit for items; it is a <em>different
 * relation</em>, and the three ways it differs are exactly the three things these
 * rows are about. Each was measured on {@code MockBukkit-v1.21:3.133.2} rather
 * than assumed:
 *
 * <ul>
 *   <li><b>Nested containers do not exist.</b> There is no {@code BlockStateMetaMock}
 *       in the jar, and {@code Bukkit.getItemFactory().getItemMeta(SHULKER_BOX)}
 *       answers a plain {@code ItemMetaMock}. The cast at
 *       {@code ShopItemStack.java:314-315} therefore throws
 *       {@code ClassCastException}, is caught at {@code :331}, and returns
 *       {@code false} - so every tier-1 shulker non-match would pass without
 *       executing one line of the comparison.</li>
 *   <li><b>{@code hasItemMeta()} is inverted for a plain item.</b>
 *       {@code ItemStackMock.hasItemMeta()} is {@code meta != null &&
 *       !ItemFactoryMock.equals(meta, null)}, and {@code ItemFactoryMock.equals}
 *       is {@code Objects.equals}, so a bare {@code new ItemStack(Material.X)}
 *       reports {@code true} there and {@code false} on a real server. That single
 *       bit is what {@code useMeta} ({@code ShopItemStack.java:304}) is computed
 *       from, so the five {@code useMeta} rows below <em>cannot fail</em> at tier 1
 *       - measured, not argued: they pass there, against a comparator that never
 *       took the branch they are about.</li>
 *   <li><b>{@code ItemMetaMock.serialize()} is not {@code CraftMetaItem.serialize()}</b>
 *       - different keys, no {@code "v"}, no DataFixerUpper. Every round trip is
 *       therefore tier 2, without exception.</li>
 * </ul>
 *
 * <h2>Rows written to fail</h2>
 * The five {@code useMeta} rows pin one defect (D2), and the round trips pin the
 * persistence defect W4 phase 1 fixes. They are red here on purpose and are
 * expected to go green after those fixes, without being edited.
 */
final class ItemMatrix {

    private ItemMatrix() {
    }

    /**
     * The site coordinates these rows use. Reserved through {@link SiteAllocator}
     * rather than agreed by comment - see that class for why a hand-typed range
     * stopped being trustworthy.
     */
    private static final SiteAllocator.Reservation SITE = SiteAllocator.reserve("ItemMatrix", 2);

    /**
     * The player the save-and-reload row's shop is shared with, so that at least
     * one ordinary shop in this harness has somebody other than its owner on it.
     */
    private static final UUID SHARED_WITH = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

    static List<IntegrationPlugin.Scenario> rows(IntegrationPlugin plugin) {
        List<IntegrationPlugin.Scenario> rows = new ArrayList<>();

        // ------------------------------------------------------------------
        // Nested containers. Tier 1 cannot reach these at all.
        // ------------------------------------------------------------------

        rows.add(new IntegrationPlugin.Scenario("aShulkerBoxIsComparedByItsContents", () -> {
            ItemStack shopBox = shulker(book("Ledger", "Pie", "one", "two"), named(Material.DIAMOND, "Pie's"));
            Assert.that(shopBox.getItemMeta() instanceof BlockStateMeta,
                    "precondition: a real server gives a shulker box a BlockStateMeta - this is the "
                            + "cast that throws under MockBukkit, and the whole reason this row is here");

            ShopItemStack shop = new ShopItemStack(shopBox);

            Assert.that(shop.isSimilar(shulker(book("Ledger", "Pie", "one", "two"), named(Material.DIAMOND, "Pie's"))),
                    "a shulker box holding the same two items should be accepted");
            Assert.that(!shop.isSimilar(shulker(book("Ledger", "Pie", "one", "three"), named(Material.DIAMOND, "Pie's"))),
                    "one page different in the book INSIDE the box is a different box");
            Assert.that(!shop.isSimilar(shulker(book("Ledger", "Pie", "one", "two"))),
                    "a box missing one of the two items is a different box");
            Assert.that(!shop.isSimilar(shulker(book("Ledger", "Pie", "one", "two"),
                            named(Material.DIAMOND, "Pie's"), named(Material.EMERALD, "extra"))),
                    "a box with an extra item in it is a different box");
            Assert.that(!shop.isSimilar(new ItemStack(Material.SHULKER_BOX)),
                    "an empty box is not a full one");
        }));

        rows.add(new IntegrationPlugin.Scenario("aBundleIsComparedByItsContents", () -> {
            ItemStack shopBundle = bundle(named(Material.DIAMOND, "Pie's"), new ItemStack(Material.EMERALD, 3));
            Assert.that(shopBundle.getItemMeta() instanceof BundleMeta,
                    "precondition: a bundle carries a BundleMeta");

            ShopItemStack shop = new ShopItemStack(shopBundle);

            Assert.that(shop.isSimilar(bundle(named(Material.DIAMOND, "Pie's"), new ItemStack(Material.EMERALD, 3))),
                    "a bundle holding the same two stacks should be accepted");
            Assert.that(!shop.isSimilar(bundle(named(Material.DIAMOND, "Someone else's"), new ItemStack(Material.EMERALD, 3))),
                    "a differently named item inside the bundle is a different bundle");
            Assert.that(!shop.isSimilar(bundle(named(Material.DIAMOND, "Pie's"), new ItemStack(Material.EMERALD, 2))),
                    "one fewer emerald inside the bundle is a different bundle");
            Assert.that(!shop.isSimilar(new ItemStack(Material.BUNDLE)),
                    "an empty bundle is not a full one");
        }));

        // ------------------------------------------------------------------
        // The useMeta hole. Five rows, one defect: ShopItemStack.java:304.
        //
        // useMeta is false whenever the two sides disagree about HAVING metadata,
        // and seven of the fifteen checks are gated on it - durability,
        // enchantments, lore, custom model data, item flags, unbreakable and
        // attribute modifiers. So a buyer holding a PLAIN item of the right
        // material skips all seven, and only the display-name check still runs.
        //
        // Each row is one attribute, because an item differing in several would be
        // refused by whichever check ran first and would say nothing about the
        // others. None of them sets a display name on the shop's item: that check
        // is NOT gated by useMeta, and setting one would make every row pass for a
        // reason that has nothing to do with the defect.
        // ------------------------------------------------------------------

        rows.add(useMetaRow(plugin, "aPlainItemMustNotPayForAnEnchantedOne", "enchantments",
                with(Material.DIAMOND_SWORD, meta -> meta.addEnchant(Enchantment.SHARPNESS, 5, true))));

        rows.add(useMetaRow(plugin, "aPlainItemMustNotPayForALoredOne", "lore",
                with(Material.DIAMOND_SWORD, meta -> meta.setLore(Arrays.asList("Forged at the", "end of the world")))));

        rows.add(useMetaRow(plugin, "aPlainItemMustNotPayForADamagedOne", "durability",
                with(Material.DIAMOND_SWORD, meta -> ((Damageable) meta).setDamage(1200))));

        rows.add(useMetaRow(plugin, "aPlainItemMustNotPayForAnUnbreakableOne", "unbreakable",
                with(Material.DIAMOND_SWORD, meta -> meta.setUnbreakable(true))));

        rows.add(useMetaRow(plugin, "aPlainItemMustNotPayForOneWithCustomModelData", "custom model data",
                with(Material.DIAMOND_SWORD, meta -> meta.setCustomModelData(4711))));

        // ------------------------------------------------------------------
        // Round trips, through the server's own serializer.
        //
        // ConfSerSerializer.serialize / deserializeItemStack is the exact pair
        // ShopItemStack.serialize() and ShopItemStack.deserialize() use for the
        // item itself, so this is the plugin's real save-and-load of an item with
        // nothing simulated - on a build with a real CraftMetaItem.serialize() and
        // a real DataFixerUpper behind it.
        // ------------------------------------------------------------------

        rows.add(new IntegrationPlugin.Scenario("aNamedLoredEnchantedItemSurvivesTheSerializer", () -> {
            ItemStack original = with(Material.DIAMOND_SWORD, meta -> {
                meta.setDisplayName("Kingsblade");
                meta.setLore(Arrays.asList("first line", "second line"));
                meta.addEnchant(Enchantment.SHARPNESS, 3, true);
                meta.setCustomModelData(7);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                meta.setUnbreakable(true);
                ((Damageable) meta).setDamage(11);
            });

            ItemStack loaded = roundTrip(original);

            Assert.equal(Material.DIAMOND_SWORD, loaded.getType(), "the material should survive a load");
            Assert.that(loaded.hasItemMeta(),
                    "the metadata should survive a load at all - ConfSerSerializer.toMap flattens ItemMeta "
                            + "into a plain Map without Bukkit's '==' type key, and ItemStack.deserialize "
                            + "then skips anything failing instanceof ItemMeta");
            ItemMeta meta = loaded.getItemMeta();
            Assert.equal("Kingsblade", meta.getDisplayName(), "the display name should survive a load");
            Assert.equal(Arrays.asList("first line", "second line"), meta.getLore(),
                    "the lore should survive a load");
            Assert.equal(3, meta.getEnchantLevel(Enchantment.SHARPNESS),
                    "the enchantment and its level should survive a load");
            Assert.equal(7, meta.getCustomModelData(), "the custom model data should survive a load");
            Assert.that(meta.getItemFlags().contains(ItemFlag.HIDE_ENCHANTS),
                    "the item flags should survive a load");
            Assert.that(meta.isUnbreakable(), "unbreakable should survive a load");
            Assert.equal(11, ((Damageable) meta).getDamage(), "the damage should survive a load");
        }));

        rows.add(new IntegrationPlugin.Scenario("aWrittenBookSurvivesTheSerializer", () -> {
            ItemStack loaded = roundTrip(book("Ledger", "Pie", "one", "two"));

            Assert.equal(Material.WRITTEN_BOOK, loaded.getType(), "the material should survive a load");
            Assert.that(loaded.getItemMeta() instanceof BookMeta,
                    "a written book should still be a written book after a load, was "
                            + loaded.getItemMeta().getClass().getSimpleName());
            BookMeta meta = (BookMeta) loaded.getItemMeta();
            Assert.equal("Ledger", meta.getTitle(), "the title should survive a load");
            Assert.equal("Pie", meta.getAuthor(), "the author should survive a load");
            Assert.equal(Arrays.asList("one", "two"), meta.getPages(), "the pages should survive a load");
        }));

        rows.add(new IntegrationPlugin.Scenario("aFireworkRocketSurvivesTheSerializer", () -> {
            ItemStack loaded = roundTrip(rocket(2, burst(Color.RED), burst(Color.BLUE)));

            Assert.that(loaded.getItemMeta() instanceof FireworkMeta,
                    "a rocket should still carry FireworkMeta after a load");
            FireworkMeta meta = (FireworkMeta) loaded.getItemMeta();
            Assert.equal(2, meta.getPower(), "the flight duration should survive a load");
            Assert.equal(2, meta.getEffects().size(), "both effects should survive a load");
            Assert.equal(burst(Color.RED), meta.getEffects().get(0),
                    "the first effect should survive a load unchanged");
            Assert.equal(burst(Color.BLUE), meta.getEffects().get(1),
                    "the second effect should survive a load unchanged, in the same position - the "
                            + "comparator walks the two lists by index");
        }));

        rows.add(new IntegrationPlugin.Scenario("aPotionSurvivesTheSerializer", () -> {
            ItemStack original = with(Material.POTION, meta -> {
                PotionMeta potion = (PotionMeta) meta;
                potion.setBasePotionType(PotionType.STRENGTH);
                potion.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 600, 1), true);
            });

            ItemStack loaded = roundTrip(original);

            Assert.that(loaded.getItemMeta() instanceof PotionMeta,
                    "a potion should still carry PotionMeta after a load");
            PotionMeta meta = (PotionMeta) loaded.getItemMeta();
            Assert.equal(PotionType.STRENGTH, meta.getBasePotionType(),
                    "the base potion type should survive a load. isSimilar never reads it (D1), which is "
                            + "why nothing else in this suite would notice it being lost");
            Assert.that(meta.hasCustomEffect(PotionEffectType.POISON),
                    "a brewed-in custom effect should survive a load");
        }));

        rows.add(new IntegrationPlugin.Scenario("aShulkerBoxSurvivesTheSerializerWithItsContents", () -> {
            ItemStack loaded = roundTrip(shulker(book("Ledger", "Pie", "one", "two"), named(Material.DIAMOND, "Pie's")));

            Assert.that(loaded.getItemMeta() instanceof BlockStateMeta,
                    "a shulker box should still carry a BlockStateMeta after a load");
            ItemStack[] contents = ((ShulkerBox) ((BlockStateMeta) loaded.getItemMeta()).getBlockState())
                    .getInventory().getContents();
            List<ItemStack> held = new ArrayList<>();
            for (ItemStack stack : contents) {
                if (stack != null) {
                    held.add(stack);
                }
            }
            Assert.equal(2, held.size(), "both items inside the box should survive a load");
            Assert.that(held.get(0).getItemMeta() instanceof BookMeta,
                    "the book inside the box should still be a book after a load");
            Assert.equal("Ledger", ((BookMeta) held.get(0).getItemMeta()).getTitle(),
                    "the title of the book INSIDE the box should survive a load");
        }));

        // ------------------------------------------------------------------
        // A shop written to disk and read back by a storage that has never seen
        // it. This is the restart, without restarting: DataStorage serves a live
        // shop from shopCache, so a shop that has just been saved answers from
        // memory and hides everything below.
        // ------------------------------------------------------------------

        rows.add(new IntegrationPlugin.Scenario("aShopWithAComplexItemSurvivesASaveAndReload", () -> {
            RealShop scene = new RealShop(plugin, SITE.at(0));
            scene.placeChestAndSign();
            scene.createShopByCommand("1 DIAMOND", "1 EMERALD");

            ItemStack complex = with(Material.DIAMOND_SWORD, meta -> {
                meta.setDisplayName("Kingsblade");
                meta.setLore(Arrays.asList("first line", "second line"));
                meta.addEnchant(Enchantment.SHARPNESS, 3, true);
            });

            ShopLocation where = scene.get(() -> new ShopLocation(scene.signBlock().getLocation()));
            scene.run(() -> {
                Shop shop = Shop.loadShop(where);
                shop.setSideItems(ShopItemSide.PRODUCT, complex);
                // An ORDINARY row's shop carries a manager, and that is the point of
                // it being here rather than only in the rows about shop users. Every
                // shop this harness built was owned by one player and shared with
                // nobody, so a non-empty `managers` list was never once written to a
                // file or read back from one - and a defect that made every shared
                // shop unloadable sat under two units about shop loading without a
                // single row going red. A fixture set that is uniformly the simple
                // case is a suite that only tests the simple case.
                shop.addUser(SHARED_WITH, ShopRole.MANAGER);
                shop.saveShop();
            });

            // A brand new DataStorage has an empty shopCache, so this read comes off
            // the file the plugin just wrote - the same path a restart takes. Its
            // constructor runs validate() first, exactly as a starting server does.
            Shop reloaded;
            try {
                reloaded = scene.get(() -> new DataStorage(DataType.FLATFILE).loadShopFromSign(where));
            } catch (Throwable t) {
                throw new AssertionError("the shop file the plugin just wrote cannot be read back at all. "
                        + "A shop whose item carries a display name must survive a restart, and the read "
                        + "threw instead: " + t, t);
            }

            Assert.that(reloaded != null, "the shop should still be on disk after being saved");
            List<ShopItemStack> product = reloaded.getSideList(ShopItemSide.PRODUCT);
            Assert.equal(1, product.size(), "the product side should hold exactly one item after a reload");

            ItemStack loaded = product.get(0).getItemStack();
            Assert.that(loaded != null, "the product item should come back from disk at all");
            Assert.equal(Material.DIAMOND_SWORD, loaded.getType(), "the product material should survive a reload");
            Assert.that(loaded.hasItemMeta(), "the product's metadata should survive a reload");
            Assert.equal("Kingsblade", loaded.getItemMeta().getDisplayName(),
                    "the name a shop advertises should survive a reload");
            Assert.equal(Arrays.asList("first line", "second line"), loaded.getItemMeta().getLore(),
                    "the lore should survive a reload");
            Assert.equal(3, loaded.getItemMeta().getEnchantLevel(Enchantment.SHARPNESS),
                    "the enchantment should survive a reload");
            Assert.that(reloaded.getUsersUUID(ShopRole.MANAGER).contains(SHARED_WITH),
                    "and the manager this shop was shared with should survive it too");
        }));

        // ------------------------------------------------------------------
        // The two encodings a shop file can already be in. A fix to the third one
        // has to keep reading these, so they are asserted before the fix rather
        // than after it.
        // ------------------------------------------------------------------

        rows.add(new IntegrationPlugin.Scenario("theLegacyEncodingsStillLoadTheirMetadata", () -> {
            ItemStack original = with(Material.DIAMOND_SWORD, meta -> {
                meta.setDisplayName("Kingsblade");
                meta.addEnchant(Enchantment.SHARPNESS, 3, true);
            });

            ShopItemStack fromB64 = new ShopItemStack(base64Of(original));
            Assert.that(fromB64.getItemStack() != null,
                    "the itemStackB64 encoding should still decode to an item");
            Assert.equal(Material.DIAMOND_SWORD, fromB64.getItemStack().getType(),
                    "the material should survive the base64 encoding");
            Assert.that(fromB64.getItemStack().hasItemMeta(),
                    "the metadata should survive the base64 encoding - this is the encoding a migration "
                            + "has to keep reading");
            Assert.equal("Kingsblade", fromB64.getItemStack().getItemMeta().getDisplayName(),
                    "the display name should survive the base64 encoding");
            Assert.equal(3, fromB64.getItemStack().getItemMeta().getEnchantLevel(Enchantment.SHARPNESS),
                    "the enchantment should survive the base64 encoding");

            ItemStack fromJson;
            try {
                fromJson = GsonProcessor.fromJsonToItemStack(GsonProcessor.itemStackToJson(original));
            } catch (com.bergerkiller.bukkit.common.config.JsonSerializer.JsonSyntaxException e) {
                throw new AssertionError("the JSON itemStackString encoding no longer parses its own "
                        + "output: " + e, e);
            }
            Assert.that(fromJson != null, "the JSON itemStackString encoding should still decode to an item");
            Assert.equal(Material.DIAMOND_SWORD, fromJson.getType(),
                    "the material should survive the JSON encoding");
            Assert.that(fromJson.hasItemMeta(), "the metadata should survive the JSON encoding");
            Assert.equal("Kingsblade", fromJson.getItemMeta().getDisplayName(),
                    "the display name should survive the JSON encoding");
        }));

        // ------------------------------------------------------------------
        // A shop file written before data components existed, loaded off disk by
        // the storage that has to keep reading it.
        //
        // The fixture is a real artefact, not a hand-written approximation: a
        // 1.20.4-era chunk file whose item carries its metadata as a nested "meta"
        // map. ConfSerSerializer.toMap flattens such a map and drops Bukkit's "=="
        // type key, and ItemStack.deserialize applies "meta" only when it is
        // already an instanceof ItemMeta - so before the reader was taught to
        // rebuild it, this sword came back stripped of its enchantment, its name
        // and its lore, and the shop quietly started selling a plain sword.
        // ------------------------------------------------------------------

        rows.add(new IntegrationPlugin.Scenario("aPreComponentShopFileKeepsItsItemMetadata", () -> {
            File legacy = new File(plugin.getDataFolder(), "legacy-shop-pre-components.json");
            Assert.that(legacy.getParentFile().isDirectory() || legacy.getParentFile().mkdirs(),
                    "the harness should have a folder to drop the legacy fixture into");

            try (InputStream fixture = plugin.getResource("legacy/shop-pre-components.json")) {
                Assert.that(fixture != null, "the legacy fixture should be on the harness classpath");
                Files.copy(fixture, legacy.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new AssertionError("could not place the legacy fixture this row is about: " + e, e);
            }

            // Read with the storage layer the plugin reads shop files with, then
            // through the item reader the plugin loads stored items with. Nothing here
            // stands in for either: Json is what JsonShopData extends, and
            // ShopItemStack.deserialize(Map) is the exact call Shop.deserialize makes
            // for every item on every side of every shop.
            //
            // The shop is read at item level rather than through DataStorage on
            // purpose. Shop's constructor runs fixAfterLoad() before deserialize has
            // filled in chestLoc, so a Shop-level load asserts the state of the world
            // around 0,1,0 as much as it asserts the migration - and the Shop-level
            // path is already covered, on a file this server wrote itself, by
            // aShopWithAComplexItemSurvivesASaveAndReload above.
            Json stored = new Json(legacy);
            String shopKey = "l::world::0::1::0";
            Assert.that(stored.singleLayerKeySet().contains(shopKey),
                    "the fixture should hold the shop it was captured from, keys were "
                            + stored.singleLayerKeySet());

            List<Map<String, Object>> product = stored.getListParameterized(shopKey + ".product");
            Assert.equal(1, product.size(), "the legacy product side should hold exactly one item");

            ShopItemStack loadedProduct = ShopItemStack.deserialize(product.get(0));
            Assert.that(loadedProduct != null, "the legacy product item should be readable at all");

            ItemStack sword = loadedProduct.getItemStack();
            Assert.that(sword != null, "the legacy product item should come back from disk at all");
            Assert.equal(Material.DIAMOND_SWORD, sword.getType(),
                    "the legacy product material should survive the load");
            Assert.that(sword.hasItemMeta(),
                    "the legacy item's metadata should survive the load. It is stored as a nested "
                            + "\"meta\" map with no Bukkit '==' key, and ItemStack.deserialize skips "
                            + "anything failing instanceof ItemMeta - so without rebuilding it first "
                            + "the sword arrives bare");

            ItemMeta meta = sword.getItemMeta();
            Assert.equal(3, meta.getEnchantLevel(Enchantment.SHARPNESS),
                    "the enchantment a pre-component file stored as DAMAGE_ALL:3 should survive the load");
            Assert.that(meta.hasDisplayName(), "the legacy item should still have a display name");
            Assert.that(meta.getDisplayName().contains("Migration Blade"),
                    "the name the shop advertises should survive the load, was " + meta.getDisplayName());
            Assert.that(meta.hasLore(), "the legacy item should still have lore");
            Assert.equal(2, meta.getLore().size(), "both lore lines should survive the load");
            Assert.that(meta.getLore().get(0).contains("forged before 1.20.5"),
                    "the first lore line should survive the load, was " + meta.getLore().get(0));
            Assert.that(meta.getLore().get(1).contains("data components did not exist"),
                    "the second lore line should survive the load, was " + meta.getLore().get(1));

            // The per-item settings are stored under their config names -
            // "compare-name", not COMPARE_NAME - and the reader used to hand them
            // straight to valueOf. Reading them at all is part of the migration.
            Assert.that(loadedProduct.getShopSetting(ShopItemStackSettingKeys.COMPARE_ENCHANTMENTS).asBoolean(),
                    "the legacy item's per-item settings should be read back under their config names");

            // The cost side carries no metadata at all, and has to survive the same
            // read: a migration that only works on decorated items is not a migration.
            List<Map<String, Object>> cost = stored.getListParameterized(shopKey + ".cost");
            Assert.equal(1, cost.size(), "the legacy cost side should hold exactly one item");
            ItemStack emeralds = ShopItemStack.deserialize(cost.get(0)).getItemStack();
            Assert.equal(Material.EMERALD, emeralds.getType(),
                    "the legacy cost material should survive the load");
            Assert.equal(3, emeralds.getAmount(), "the legacy cost amount should survive the load");
        }));

        // ------------------------------------------------------------------
        // What the comparator's answer costs, in items that moved.
        // ------------------------------------------------------------------

        rows.add(new IntegrationPlugin.Scenario("aRealTradeRefusesABuyerHoldingACheaperItem", () -> {
            RealShop scene = new RealShop(plugin, SITE.at(1));
            scene.placeChestAndSign();
            scene.createShopByCommand("1 DIAMOND", "1 EMERALD");

            ItemStack askedFor = with(Material.DIAMOND_SWORD, meta -> meta.addEnchant(Enchantment.SHARPNESS, 5, true));
            ShopLocation where = scene.get(() -> new ShopLocation(scene.signBlock().getLocation()));
            scene.run(() -> {
                Shop shop = Shop.loadShop(where);
                shop.setSideItems(ShopItemSide.COST, askedFor);
                shop.saveShop();
                shop.updateSign();
            });

            scene.stockShop(new ItemStack(Material.DIAMOND, 10));
            scene.closeChestAsOwner();
            Assert.eventually(15_000, "the shop to be open before anyone trades with it",
                    scene.onServer(() -> Shop.loadShop(where).getStatus() == ShopStatus.OPEN));

            ItemStack plainSword = new ItemStack(Material.DIAMOND_SWORD);
            Assert.that(!plainSword.hasItemMeta(),
                    "precondition: a bare new ItemStack has no metadata on a real server. It reports the "
                            + "opposite under MockBukkit, which is why this row cannot live at tier 1");

            Player buyer = scene.buyerHolding(plainSword);
            scene.rightClickSign(buyer);

            // Give the trade a chance to happen before asserting that it did not.
            // Asserting a negative immediately after an action can pass simply by
            // being early, which is the one way this row could lie.
            try {
                Assert.eventually(3_000, "the trade this row says must not happen",
                        scene.onServer(() -> scene.rawCountOf(buyer, Material.DIAMOND) > 0));
            } catch (AssertionError expectedTimeout) {
                // Good: nothing moved.
            }

            Assert.equal(0, scene.countOf(buyer, Material.DIAMOND),
                    "a shop asking for a Sharpness V sword must not pay out for a plain one - D2, the "
                            + "useMeta gate at ShopItemStack.java:304 disables the enchantment check "
                            + "whenever one side has no metadata at all");
            Assert.equal(1, scene.countOf(buyer, Material.DIAMOND_SWORD),
                    "and the buyer should still be holding the sword they came with");
            Assert.equal(10, scene.countInChest(Material.DIAMOND), "and the shop should keep its stock");
            Assert.equal(0, scene.countInChest(Material.DIAMOND_SWORD),
                    "and should not have been paid with the wrong sword");
        }));

        return rows;
    }

    // ------------------------------------------------------------------
    // The useMeta rows, which differ only in which attribute they carry
    // ------------------------------------------------------------------

    private static IntegrationPlugin.Scenario useMetaRow(IntegrationPlugin plugin, String name,
                                                         String attribute, ItemStack shopItem) {
        return new IntegrationPlugin.Scenario(name, () -> {
            ItemStack plain = new ItemStack(Material.DIAMOND_SWORD);

            Assert.that(!plain.hasItemMeta(),
                    "precondition: a bare new ItemStack has no metadata on a real server. Under MockBukkit "
                            + "it reports that it does - ItemFactoryMock.equals is Objects.equals - which "
                            + "makes useMeta true there and hides this defect entirely");
            Assert.that(shopItem.hasItemMeta(), "precondition: the shop's item does carry metadata");

            Assert.that(!new ShopItemStack(shopItem).isSimilar(plain),
                    "a shop asking for an item with " + attribute + " must not accept a plain one - D2, "
                            + "useMeta at ShopItemStack.java:304 is false whenever the two sides disagree "
                            + "about having metadata, and it gates seven of the fifteen checks");
        });
    }

    // ------------------------------------------------------------------
    // Builders
    // ------------------------------------------------------------------

    static ItemStack with(Material material, Consumer<ItemMeta> edit) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        edit.accept(meta);
        stack.setItemMeta(meta);
        return stack;
    }

    static ItemStack named(Material material, String name) {
        return with(material, meta -> meta.setDisplayName(name));
    }

    static ItemStack book(String title, String author, String... pages) {
        return with(Material.WRITTEN_BOOK, meta -> {
            BookMeta book = (BookMeta) meta;
            book.setTitle(title);
            book.setAuthor(author);
            book.setPages(Arrays.asList(pages));
        });
    }

    private static ItemStack rocket(int power, FireworkEffect... effects) {
        return with(Material.FIREWORK_ROCKET, meta -> {
            FireworkMeta firework = (FireworkMeta) meta;
            firework.setPower(power);
            for (FireworkEffect effect : effects) {
                firework.addEffect(effect);
            }
        });
    }

    private static FireworkEffect burst(Color colour) {
        return FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(colour).build();
    }

    private static ItemStack shulker(ItemStack... contents) {
        return with(Material.SHULKER_BOX, meta -> {
            BlockStateMeta block = (BlockStateMeta) meta;
            ShulkerBox box = (ShulkerBox) block.getBlockState();
            for (int slot = 0; slot < contents.length; slot++) {
                box.getInventory().setItem(slot, contents[slot]);
            }
            block.setBlockState(box);
        });
    }

    private static ItemStack bundle(ItemStack... contents) {
        return with(Material.BUNDLE, meta -> ((BundleMeta) meta).setItems(Arrays.asList(contents)));
    }

    // ------------------------------------------------------------------
    // The round trip itself
    // ------------------------------------------------------------------

    /**
     * An item through the plugin's own save and load, and nothing else.
     *
     * <p>{@code ConfSerSerializer.serialize} is what {@code ShopItemStack.serialize()}
     * writes into {@code itemStackString}, and {@code deserializeItemStack} is what
     * {@code ShopItemStack.deserialize} reads it back with. Nothing here is a
     * stand-in for that pair; it is that pair.
     */
    private static ItemStack roundTrip(ItemStack original) {
        Map<String, Object> written = ConfSerSerializer.serialize(original);
        ItemStack loaded = ConfSerSerializer.deserializeItemStack(written);
        Assert.that(loaded != null, "the item did not come back from the serializer at all. What was "
                + "written was: " + written);
        return loaded;
    }

    /** The oldest of the three on-disk encodings, written the way the plugin reads it. */
    private static String base64Of(ItemStack item) {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             BukkitObjectOutputStream out = new BukkitObjectOutputStream(bytes)) {
            out.writeObject(item);
            out.flush();
            // MIME base64, which is what SnakeYAML's Base64Coder.encodeLines wrote:
            // 76-character lines. That class is a SnakeYAML internal and is absent from
            // Paper 26.2, so the fixture is built with the JDK's own encoder - same
            // wire format, one fewer dependency on a server internal.
            return Base64.getMimeEncoder().encodeToString(bytes.toByteArray());
        } catch (Exception e) {
            throw new AssertionError("could not build the base64 encoding this row is about: " + e, e);
        }
    }
}
