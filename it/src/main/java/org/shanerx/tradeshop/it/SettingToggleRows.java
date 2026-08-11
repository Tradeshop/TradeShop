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

import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.shanerx.tradeshop.data.storage.DataStorage;
import org.shanerx.tradeshop.data.storage.DataType;
import org.shanerx.tradeshop.item.ShopItemSide;
import org.shanerx.tradeshop.item.ShopItemStack;
import org.shanerx.tradeshop.item.ShopItemStackSettingKeys;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shoplocation.ShopLocation;
import org.shanerx.tradeshop.utils.objects.ObjectHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * The two per-item toggles a mock cannot answer honestly.
 *
 * <p>Every other setting is switched off and back on in
 * {@code src/test/.../SettingToggleMatrixTest}, with the same four assertions per
 * row and the same independence cross-check. Only these two are here, each for a
 * reason that is about the mock rather than about the setting:
 *
 * <ul>
 *   <li><b>{@code COMPARE_SHULKER_INVENTORY}</b> - MockBukkit ships no
 *       {@code BlockStateMetaMock}, so {@code getItemMeta(SHULKER_BOX)} answers a
 *       plain {@code ItemMetaMock}, the cast at
 *       {@code ShopItemStack.java:390-391} throws, {@code :407} catches it and
 *       returns {@code false}. A tier-1 row would see a refusal without one line
 *       of the comparison having run, and would go on passing with the setting
 *       toggled either way.</li>
 *   <li><b>A toggle that has to survive a restart</b> - the per-item settings
 *       are written into a real shop file and read back by a
 *       {@code DataStorage} that has never seen the shop. Nothing about that
 *       exists without a plugin data folder, and a toggle that only holds until
 *       the server stops is not a toggle a shop owner has.</li>
 * </ul>
 */
final class SettingToggleRows {

    private SettingToggleRows() {
    }

    /**
     * This suite's patch of the world. Reserved through {@link SiteAllocator}
     * rather than agreed by comment - this suite and {@code IssueRows} both
     * once wrote a comment exactly like this one claiming 30, and neither
     * comment named the other. Both landing on the same site is the collision
     * {@link SiteAllocator} exists to make impossible.
     */
    private static final SiteAllocator.Reservation SITE = SiteAllocator.reserve("SettingToggleRows", 1);

    static List<IntegrationPlugin.Scenario> rows(IntegrationPlugin plugin) {
        List<IntegrationPlugin.Scenario> rows = new ArrayList<>();

        // ------------------------------------------------------------------
        // COMPARE_SHULKER_INVENTORY, both directions and the cross-check.
        // ------------------------------------------------------------------
        rows.add(new IntegrationPlugin.Scenario("compareShulkerInventoryGatesOnlyTheBoxContents", () -> {
            Assert.that(ShopItemStackSettingKeys.COMPARE_SHULKER_INVENTORY.isUserEditable(),
                    "precondition: the setting is user-editable, or ShopItemStack.java:287 drops the "
                            + "per-item override and every assertion below is about the server default");
            Assert.that(ShopItemStackSettingKeys.COMPARE_SHULKER_INVENTORY.getDefaultValue().asBoolean(),
                    "precondition: the server-wide default is on, which is what 'gated on' means here");
            Assert.that(ShopItemStackSettingKeys.COMPARE_NAME.getDefaultValue().asBoolean(),
                    "precondition: the name comparison is on, and it is the other half of the "
                            + "cross-check below");

            Assert.that(kit("Kit", "Pie's").getItemMeta() instanceof BlockStateMeta,
                    "precondition: a real server gives a shulker box a BlockStateMeta. This is the cast "
                            + "MockBukkit throws on, and the whole reason this row is not at tier 1");

            // Gated on, at the server default.
            Assert.that(!new ShopItemStack(kit("Kit", "Pie's")).isSimilar(kit("Kit", "Someone else's")),
                    "with COMPARE_SHULKER_INVENTORY on, a box whose contents differ must be refused");

            // Gated on, written onto the item, so the row is about the setting and
            // not about the default.
            Assert.that(!toggled(kit("Kit", "Pie's"), ShopItemStackSettingKeys.COMPARE_SHULKER_INVENTORY, true)
                            .isSimilar(kit("Kit", "Someone else's")),
                    "and with the setting written onto the item explicitly it must be refused too");

            // Gated off.
            Assert.that(toggled(kit("Kit", "Pie's"), ShopItemStackSettingKeys.COMPARE_SHULKER_INVENTORY, false)
                            .isSimilar(kit("Kit", "Pie's")),
                    "with COMPARE_SHULKER_INVENTORY off an identical box must still be accepted");
            Assert.that(toggled(kit("Kit", "Pie's"), ShopItemStackSettingKeys.COMPARE_SHULKER_INVENTORY, false)
                            .isSimilar(kit("Kit", "Someone else's")),
                    "and with it off the contents of the box stop mattering");

            // Independence, both ways round. Turning the box's contents off must
            // not turn the box's own name off with it, and vice versa.
            Assert.that(!toggled(kit("Kit", "Pie's"), ShopItemStackSettingKeys.COMPARE_SHULKER_INVENTORY, false)
                            .isSimilar(kit("Satchel", "Pie's")),
                    "with COMPARE_SHULKER_INVENTORY off, a box with a different NAME must still be "
                            + "refused by COMPARE_NAME - a setting that switches off more than its own "
                            + "comparison is a duplication exploit");
            Assert.that(!toggled(kit("Kit", "Pie's"), ShopItemStackSettingKeys.COMPARE_NAME, false)
                            .isSimilar(kit("Satchel", "Someone else's")),
                    "and with COMPARE_NAME off, a box whose CONTENTS differ must still be refused by "
                            + "COMPARE_SHULKER_INVENTORY");
            Assert.that(toggled(kit("Kit", "Pie's"), ShopItemStackSettingKeys.COMPARE_NAME, false)
                            .isSimilar(kit("Satchel", "Pie's")),
                    "while a box differing only in its name is accepted, which is what makes the two "
                            + "assertions above about independence rather than about refusing everything");
        }));

        // ------------------------------------------------------------------
        // A toggle that outlives the process that made it.
        // ------------------------------------------------------------------
        rows.add(new IntegrationPlugin.Scenario("aPerItemToggleSurvivesASaveAndReload", () -> {
            RealShop scene = new RealShop(plugin, SITE.at(0));
            scene.placeChestAndSign();
            scene.createShopByCommand("1 DIAMOND", "1 EMERALD");

            ShopLocation where = scene.get(() -> new ShopLocation(scene.signBlock().getLocation()));

            scene.run(() -> {
                Shop shop = Shop.loadShop(where);
                ShopItemStack cost = shop.getSideList(ShopItemSide.COST).get(0);
                cost.setShopSettings(ShopItemStackSettingKeys.COMPARE_NAME, new ObjectHolder<>(false));
                shop.updateSideItem(ShopItemSide.COST, cost, 0, true);
            });

            // A brand new DataStorage has an empty shopCache, so this read comes off
            // the file the plugin just wrote - the same path a restart takes.
            Shop reloaded = scene.get(() -> new DataStorage(DataType.FLATFILE).loadShopFromSign(where));
            Assert.that(reloaded != null, "the shop should still be on disk after the toggle was saved");

            List<ShopItemStack> cost = reloaded.getSideList(ShopItemSide.COST);
            Assert.equal(1, cost.size(), "the cost side should come back from disk");
            ShopItemStack reloadedCost = cost.get(0);

            Assert.that(!reloadedCost.getShopSetting(ShopItemStackSettingKeys.COMPARE_NAME).asBoolean(),
                    "a per-item setting an owner turned off must still be off after the shop is read "
                            + "back off disk. It is written by ShopItemStack.serialize under its config "
                            + "name and read back by deserialize:250-260");
            Assert.that(reloadedCost.getShopSetting(ShopItemStackSettingKeys.COMPARE_LORE).asBoolean(),
                    "and the settings nobody touched must come back on rather than being dragged down "
                            + "with it");
            Assert.equal(1, reloadedCost.getShopSetting(ShopItemStackSettingKeys.COMPARE_DURABILITY).asInteger(),
                    "including the one that is an int rather than a boolean");

            // What the setting is for, after the trip through the file. Asserting the
            // stored value alone would not say whether the comparator still reads it.
            Assert.that(reloadedCost.isSimilar(named(Material.EMERALD, "Someone else's")),
                    "and the reloaded item must actually compare that way: with COMPARE_NAME off it "
                            + "accepts a renamed emerald");
            Assert.that(!reloadedCost.isSimilar(lored(Material.EMERALD, "not the same emerald")),
                    "while still refusing one carrying lore, so the reload restored one setting and "
                            + "not the whole map");
        }));

        return rows;
    }

    // ------------------------------------------------------------------
    // Builders
    // ------------------------------------------------------------------

    /** A named shulker box holding a named diamond and a book, both of which are compared. */
    private static ItemStack kit(String boxName, String diamondName) {
        return ItemMatrix.with(Material.SHULKER_BOX, meta -> {
            BlockStateMeta block = (BlockStateMeta) meta;
            ShulkerBox inside = (ShulkerBox) block.getBlockState();
            inside.getInventory().setItem(0, ItemMatrix.named(Material.DIAMOND, diamondName));
            inside.getInventory().setItem(1, ItemMatrix.book("Ledger", "Pie", "one", "two"));
            block.setBlockState(inside);
            meta.setDisplayName(boxName);
        });
    }

    private static ItemStack named(Material material, String name) {
        return ItemMatrix.named(material, name);
    }

    private static ItemStack lored(Material material, String lore) {
        return ItemMatrix.with(material, meta -> meta.setLore(List.of(lore)));
    }

    private static ShopItemStack toggled(ItemStack stack, ShopItemStackSettingKeys key, Object value) {
        ShopItemStack item = new ShopItemStack(stack);
        item.setShopSettings(key, new ObjectHolder<>(value));
        return item;
    }
}
