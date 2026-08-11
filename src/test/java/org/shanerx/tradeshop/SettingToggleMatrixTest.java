package org.shanerx.tradeshop;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.harness.Items;
import org.shanerx.tradeshop.harness.MockBukkitEnvironment;
import org.shanerx.tradeshop.item.ShopItemStack;
import org.shanerx.tradeshop.item.ShopItemStackSettingKeys;
import org.shanerx.tradeshop.utils.objects.ObjectHolder;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every per-item comparison setting, switched off and back on, and the one
 * question that catches a toggle wired to the wrong thing.
 *
 * <h2>What a row states</h2>
 * For a setting {@code S} and the attribute it is named after, each row states
 * four things about an item that differs from the shop's in exactly that
 * attribute and nothing else:
 *
 * <ol>
 *   <li><b>gated on</b> - with every setting at its server default, the item is
 *       refused;</li>
 *   <li><b>toggled on</b> - with {@code S} written onto the item explicitly, it is
 *       still refused, so the row is about the setting and not about the
 *       default;</li>
 *   <li><b>gated off</b> - with {@code S} off, the item is accepted, and an
 *       identical item still is too;</li>
 *   <li><b>independence</b> - with {@code S} off, an item differing in some
 *       <em>other</em> setting's attribute is <em>still refused</em>.</li>
 * </ol>
 *
 * The fourth is the one that earns its keep. The first three pass for a setting
 * that quietly disables half the comparator along with its own check, and that is
 * not hypothetical here: one {@code useMeta} condition was found switching off
 * seven of the fifteen checks, and it was a duplication exploit for as long as it
 * existed. Stating "with the lore check off, a differently enchanted item is still
 * refused" is what makes that shape fail.
 *
 * <h2>Why the settings are written and not clicked</h2>
 * {@code ShopItemStack.setShopSettings} is what the GUI's own state elements call
 * ({@code GUISubCommand.java:248,256}), so driving it directly exercises the same
 * write. What the GUI adds on top - that a click reaches that call, that Save
 * persists it, and that a trade changes - is one row at tier 3, where a real
 * client can click.
 *
 * <h2>One thing that would make every row here a lie</h2>
 * {@code ShopItemStack.java:287} honours a per-item override only while
 * {@code key.isUserEditable()}, and {@code ShopItemStackSettingKeys.java:126}
 * reads that out of {@code config.yml} with {@code getBoolean}, which answers
 * {@code false} for a key the file does not contain. A suite that did not check
 * this would write settings that were then ignored and pass on the defaults
 * alone. {@link #everyPerItemSettingIsUserEditableSoAnOverrideCanTakeEffect()}
 * checks it, and {@link #anOperatorCanLockASettingAgainstPerItemOverrides()}
 * states the other half: what happens when it is false.
 *
 * <h2>What is deliberately not here</h2>
 * {@code COMPARE_SHULKER_INVENTORY}. There is no {@code BlockStateMetaMock} in
 * MockBukkit, so {@code getItemMeta(SHULKER_BOX)} answers a plain
 * {@code ItemMetaMock}, the cast at {@code ShopItemStack.java:390-391} throws,
 * {@code :407} catches it and returns {@code false} - a non-match reached without
 * executing one line of the comparison, which would make a row here pass while
 * proving nothing. It is in {@code it/SettingToggleRows.java} instead.
 */
@ExtendWith(MockBukkitEnvironment.class)
class SettingToggleMatrixTest {

    /**
     * One setting, the attribute it is named after, and an item differing in that
     * attribute alone.
     *
     * <p>A supplier rather than an item: {@code isSimilar} answers {@code false}
     * for two references to the same object ({@code ShopItemStack.java:348}), and
     * every row builds both sides several times.
     */
    private record Row(ShopItemStackSettingKeys key, String attribute, Supplier<ItemStack> differing) {
    }

    // ------------------------------------------------------------------
    // The preconditions that make every row below mean something
    // ------------------------------------------------------------------

    @Test
    void everyPerItemSettingIsUserEditableSoAnOverrideCanTakeEffect() {
        for (ShopItemStackSettingKeys key : ShopItemStackSettingKeys.values()) {
            assertTrue(key.isUserEditable(),
                    key + " is not user-editable on this config, so ShopItemStack.java:287 would drop "
                            + "every per-item override of it and this suite would be asserting the "
                            + "server default fifteen times over");
            assertFalse(key.getDefaultValue().isNull(),
                    key + " has no server-wide default, and every caller reads that holder as a boolean "
                            + "or an int");
        }

        // The defaults the whole matrix leans on: "everything else on" is the
        // absence of an override, so it is only true while the file says so.
        assertEquals(1, ShopItemStackSettingKeys.COMPARE_DURABILITY.getDefaultValue().asInteger(),
                "COMPARE_DURABILITY defaults to '==', which is what 'gated on' means for it");
        for (ShopItemStackSettingKeys key : ShopItemStackSettingKeys.values()) {
            if (key == ShopItemStackSettingKeys.COMPARE_DURABILITY) continue;
            assertTrue(key.getDefaultValue().asBoolean(), key + " should default to on");
        }
    }

    /**
     * The operator's half of the same switch, stated because it is the reason a
     * per-item override can be silently ignored.
     *
     * <p>{@code user-editable: false} in {@code shop-per-item-settings} is how an
     * operator pins a comparison server-wide, and {@code ShopItemStack.java:287}
     * enforces it by refusing to read the item's own value. The value written onto
     * the item is not lost - it is not consulted - so the shop keeps comparing
     * names whatever the item says.
     */
    @Test
    void anOperatorCanLockASettingAgainstPerItemOverrides() {
        String subKey = ShopItemStackSettingKeys.COMPARE_NAME.getConfigName() + ".user-editable";
        Object wasEditable = Setting.SHOP_PER_ITEM_SETTINGS.getMappedObject(subKey);

        try {
            Setting.SHOP_PER_ITEM_SETTINGS.setMappedValue(subKey, false);
            assertFalse(ShopItemStackSettingKeys.COMPARE_NAME.isUserEditable(),
                    "precondition: the operator has locked COMPARE_NAME");

            ShopItemStack locked = withSetting(sword(), ShopItemStackSettingKeys.COMPARE_NAME, false);
            assertTrue(locked.getShopSetting(ShopItemStackSettingKeys.COMPARE_NAME).asBoolean(),
                    "a locked setting answers the server default, not the value on the item");
            assertFalse(locked.isSimilar(sword(meta -> meta.setDisplayName("Queensblade"))),
                    "so the name is still compared, and the shop still refuses a renamed item");
        } finally {
            Setting.SHOP_PER_ITEM_SETTINGS.setMappedValue(subKey, wasEditable);
        }

        assertTrue(ShopItemStackSettingKeys.COMPARE_NAME.isUserEditable(),
                "the lock must be lifted again, or every later row in this JVM is asserting the "
                        + "server default");
    }

    /**
     * The third state of the same switch: the config has no answer at all.
     *
     * <p>{@code shop-per-item-settings.<key>} has two children and they are read by
     * two methods that treat a hole differently.
     * {@code ShopItemStackSettingKeys.getDefaultValue():116-119} falls back to the
     * value compiled into the enum, because a hole there used to throw inside the
     * trade gate. {@code isUserEditable():125-127} hands straight to
     * {@code Setting.getMappedBoolean}, and {@code getBoolean} answers {@code false}
     * for a key that is not there.
     *
     * <p>{@code false} is not "no answer" for this key. It is
     * {@code user-editable: false}, which is a deliberate decision an operator makes
     * and the one thing that makes {@code ShopItemStack.java:287} throw away a shop
     * owner's per-item override. So a hole does not degrade to the default, it
     * quietly enacts the opposite of it: every per-item setting on the server stops
     * being editable, every override already written onto an item stops being
     * consulted, and nothing is logged. Two comparisons that were switched off go
     * back on, and the shop starts refusing items it used to accept.
     *
     * <p>The hole is punched in memory rather than in a file, for the same reason
     * {@code it/DefectRows.aMissingPerItemSettingDoesNotThrowInsideTheTradeGate}
     * does: this is about the guard, not about the boot-time repair, and the two
     * have to be able to fail and be fixed independently.
     */
    @Test
    void aPerItemSettingMissingFromTheConfigIsStillEditableRatherThanLocked() {
        String subKey = ShopItemStackSettingKeys.COMPARE_LORE.getConfigName() + ".user-editable";
        Object wasEditable = Setting.SHOP_PER_ITEM_SETTINGS.getMappedObject(subKey);

        try {
            Setting.SHOP_PER_ITEM_SETTINGS.setMappedValue(subKey, null);
            assertTrue(Setting.SHOP_PER_ITEM_SETTINGS.getMappedObject(subKey) == null,
                    "precondition: the config has no answer for whether COMPARE_LORE is editable");

            assertTrue(ShopItemStackSettingKeys.COMPARE_LORE.isUserEditable(),
                    "a per-item setting the config says nothing about must keep the plugin's own "
                            + "default, which is editable - ShopItemStackSettingKeys.java:125-127 reads "
                            + "getBoolean on a missing key and gets false, which is the value an "
                            + "operator writes to LOCK the setting");

            ShopItemStack loreOff = withSetting(sword(), ShopItemStackSettingKeys.COMPARE_LORE, false);
            assertFalse(loreOff.getShopSetting(ShopItemStackSettingKeys.COMPARE_LORE).asBoolean(),
                    "so an override written onto an item is still consulted");
            assertTrue(loreOff.isSimilar(sword(meta -> meta.setLore(List.of("first line", "changed")))),
                    "and the shop still accepts the item its owner said it should - a hole in the "
                            + "config must not switch a comparison back on behind their back");
        } finally {
            Setting.SHOP_PER_ITEM_SETTINGS.setMappedValue(subKey, wasEditable);
        }

        assertTrue(ShopItemStackSettingKeys.COMPARE_LORE.isUserEditable(),
                "the hole must be filled again, or every later row in this JVM is asserting the "
                        + "server default");
    }

    // ------------------------------------------------------------------
    // The families. One base item, and one variant per attribute it carries.
    // ------------------------------------------------------------------

    /**
     * A sword carrying eight of the fifteen comparable attributes at once.
     *
     * <p>Eight on one item is the point: the independence question is only
     * answerable when the other seven attributes are present to be compared, and
     * an item that carried one at a time would make every cross-check vacuous.
     */
    @Test
    void aSwordTogglesEightComparisonsIndependently() {
        matrix("a sword", SettingToggleMatrixTest::sword, List.of(
                new Row(ShopItemStackSettingKeys.COMPARE_DURABILITY, "damage",
                        () -> sword(meta -> ((Damageable) meta).setDamage(12))),
                new Row(ShopItemStackSettingKeys.COMPARE_ENCHANTMENTS, "enchantment level",
                        () -> sword(meta -> meta.addEnchant(Enchantment.SHARPNESS, 2, true))),
                new Row(ShopItemStackSettingKeys.COMPARE_NAME, "display name",
                        () -> sword(meta -> meta.setDisplayName("Queensblade"))),
                new Row(ShopItemStackSettingKeys.COMPARE_LORE, "lore",
                        () -> sword(meta -> meta.setLore(List.of("first line", "changed")))),
                new Row(ShopItemStackSettingKeys.COMPARE_CUSTOM_MODEL_DATA, "custom model data",
                        () -> sword(meta -> meta.setCustomModelData(8))),
                new Row(ShopItemStackSettingKeys.COMPARE_ITEM_FLAGS, "item flags",
                        () -> sword(meta -> meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES))),
                new Row(ShopItemStackSettingKeys.COMPARE_UNBREAKABLE, "unbreakable",
                        () -> sword(meta -> meta.setUnbreakable(false))),
                new Row(ShopItemStackSettingKeys.COMPARE_ATTRIBUTE_MODIFIER, "attribute modifiers",
                        () -> sword(3.0, meta -> {
                        }))));
    }

    /**
     * {@code COMPARE_DURABILITY} is the one setting that is not a boolean:
     * {@code -1} off, {@code 0} '&lt;=', {@code 1} '==', {@code 2} '&gt;='
     * ({@code ShopItemStackSettingKeys.java:43}).
     *
     * <p>The three comparing modes are stated in
     * {@code ItemMetadataCompareTest.theThreeDamageComparisonModesAcceptAndRefuseAsConfigured}
     * and are not repeated. What belongs here is the part the matrix asks of every
     * other setting: that the two inequality modes gate <em>damage only</em>. A
     * mode that also loosened the name or the enchantment check would still pass
     * every assertion in that test.
     */
    @Test
    void theInequalityDamageModesStillCompareEverythingElse() {
        for (int mode : new int[]{0, 2}) {
            ShopItemStack shop = withSetting(sword(), ShopItemStackSettingKeys.COMPARE_DURABILITY, mode);

            assertTrue(shop.isSimilar(sword()),
                    "damage mode " + mode + " should accept an item identical in every attribute");
            assertFalse(shop.isSimilar(sword(meta -> meta.setDisplayName("Queensblade"))),
                    "damage mode " + mode + " must not stop the display name from being compared");
            assertFalse(shop.isSimilar(sword(meta -> meta.addEnchant(Enchantment.SHARPNESS, 2, true))),
                    "damage mode " + mode + " must not stop the enchantments from being compared");
            assertFalse(shop.isSimilar(sword(meta -> meta.setLore(List.of("first line", "changed")))),
                    "damage mode " + mode + " must not stop the lore from being compared");
        }
    }

    /**
     * A written book. {@code COMPARE_NAME} takes the book branch and compares the
     * title rather than the display name ({@code ShopItemStack.java:513-519}), so
     * "name" means something different here than it does on the sword - which is
     * exactly why the book needs its own family rather than a row in the sword's.
     */
    @Test
    void aWrittenBookTogglesTitleAuthorPagesAndLoreIndependently() {
        matrix("a written book", () -> ledger(book -> {
        }), List.of(
                new Row(ShopItemStackSettingKeys.COMPARE_NAME, "title",
                        () -> ledger(book -> book.setTitle("Ledgers"))),
                new Row(ShopItemStackSettingKeys.COMPARE_BOOK_AUTHOR, "author",
                        () -> ledger(book -> book.setAuthor("Someone else"))),
                new Row(ShopItemStackSettingKeys.COMPARE_BOOK_PAGES, "pages",
                        () -> ledger(book -> book.setPages(List.of("one", "three")))),
                new Row(ShopItemStackSettingKeys.COMPARE_LORE, "lore",
                        () -> ledger(book -> book.setLore(List.of("ink", "vellum"))))));

        // A page added and a page taken away are the same setting and not the same
        // difference: the comparator compares the lists whole, so a shorter list
        // and a longer one both have to be refused.
        ShopItemStack shop = new ShopItemStack(ledger(book -> {
        }));
        assertFalse(shop.isSimilar(ledger(book -> book.setPages(List.of("one")))),
                "a book with a page missing is a different book");
        assertFalse(shop.isSimilar(ledger(book -> book.setPages(List.of("one", "two", "three")))),
                "a book with an extra page is a different book");
    }

    /** Flight duration and effects are two settings on one item, so each has to gate only its own. */
    @Test
    void aFireworkRocketTogglesDurationAndEffectsIndependently() {
        matrix("a firework rocket", () -> rocket(2, Color.RED, "Skylight"), List.of(
                new Row(ShopItemStackSettingKeys.COMPARE_FIREWORK_DURATION, "flight duration",
                        () -> rocket(1, Color.RED, "Skylight")),
                new Row(ShopItemStackSettingKeys.COMPARE_FIREWORK_EFFECTS, "effects",
                        () -> rocket(2, Color.GREEN, "Skylight")),
                new Row(ShopItemStackSettingKeys.COMPARE_NAME, "display name",
                        () -> rocket(2, Color.RED, "Nightfall"))));
    }

    /**
     * A potion. One setting covers both halves of what a potion is - the base type
     * and the effects brewed into it - so both are stated against it, and the
     * cross-check is that turning it off does not also stop the potion being
     * compared as an ordinary item.
     */
    @Test
    void aPotionTogglesItsEffectsIndependently() {
        matrix("a potion", () -> draught(PotionType.STRENGTH, "Draught", List.of("warm", "bitter")), List.of(
                new Row(ShopItemStackSettingKeys.COMPARE_POTION_EFFECTS, "base potion type",
                        () -> draught(PotionType.HARMING, "Draught", List.of("warm", "bitter"))),
                new Row(ShopItemStackSettingKeys.COMPARE_NAME, "display name",
                        () -> draught(PotionType.STRENGTH, "Tincture", List.of("warm", "bitter"))),
                new Row(ShopItemStackSettingKeys.COMPARE_LORE, "lore",
                        () -> draught(PotionType.STRENGTH, "Draught", List.of("warm", "sweet")))));

        // The other half of the same setting: an effect brewed in on top of the
        // base type. Since 1.20.5 the extended and upgraded variants are base types
        // of their own, so those go through the check above; a custom effect does
        // not.
        Supplier<ItemStack> plain = () -> draught(PotionType.WATER, "Draught", List.of("warm"));
        Supplier<ItemStack> poisoned = () -> {
            ItemStack stack = plain.get();
            ItemMeta meta = stack.getItemMeta();
            ((PotionMeta) meta).addCustomEffect(new PotionEffect(PotionEffectType.POISON, 600, 1), true);
            stack.setItemMeta(meta);
            return stack;
        };

        assertFalse(new ShopItemStack(plain.get()).isSimilar(poisoned.get()),
                "a water bottle with an effect brewed into it is not a water bottle");
        assertTrue(withSetting(plain.get(), ShopItemStackSettingKeys.COMPARE_POTION_EFFECTS, false)
                        .isSimilar(poisoned.get()),
                "and with COMPARE_POTION_EFFECTS off the brewed-in effect stops mattering");
        assertFalse(withSetting(plain.get(), ShopItemStackSettingKeys.COMPARE_POTION_EFFECTS, false)
                        .isSimilar(draught(PotionType.WATER, "Tincture", List.of("warm"))),
                "but the name is still compared, so COMPARE_POTION_EFFECTS gates only the potion");
    }

    /**
     * A bundle. Its contents differ by <em>material</em> here rather than by
     * metadata, and that is a limit of the tier rather than of the setting:
     * {@code ItemStackMock.equals} answers true for a plain diamond against a named
     * one - measured - and the comparator compares contents with
     * {@code List.remove}. A bundle whose contents differ only in metadata is
     * asserted in {@code it/ItemMatrix.aBundleIsComparedByItsContents}, on a server
     * whose {@code equals} reads the metadata.
     */
    @Test
    void aBundleTogglesItsContentsIndependently() {
        matrix("a bundle", () -> kit(Material.EMERALD, "Kit"), List.of(
                new Row(ShopItemStackSettingKeys.COMPARE_BUNDLE_INVENTORY, "contents",
                        () -> kit(Material.REDSTONE, "Kit")),
                new Row(ShopItemStackSettingKeys.COMPARE_NAME, "display name",
                        () -> kit(Material.EMERALD, "Satchel"))));
    }

    // ------------------------------------------------------------------
    // The matrix itself
    // ------------------------------------------------------------------

    private static void matrix(String family, Supplier<ItemStack> base, List<Row> rows) {
        assertTrue(new ShopItemStack(base.get()).isSimilar(base.get()),
                family + ": an item identical in every attribute is accepted, which is the "
                        + "precondition every refusal below is measured against");

        for (Row row : rows) {
            String on = family + ": with " + row.key() + " on, ";
            String off = family + ": with " + row.key() + " off, ";

            assertFalse(new ShopItemStack(base.get()).isSimilar(row.differing().get()),
                    on + "a different " + row.attribute() + " must be refused");

            assertFalse(withSetting(base.get(), row.key(), onValue(row.key()))
                            .isSimilar(row.differing().get()),
                    family + ": " + row.key() + " written onto the item explicitly must refuse a "
                            + "different " + row.attribute() + " too, or the row above was only "
                            + "asserting the server default");

            assertTrue(withSetting(base.get(), row.key(), offValue(row.key())).isSimilar(base.get()),
                    off + "an identical item must still be accepted");

            assertTrue(withSetting(base.get(), row.key(), offValue(row.key()))
                            .isSimilar(row.differing().get()),
                    off + "the " + row.attribute() + " stops mattering");

            for (Row other : rows) {
                if (other.key() == row.key()) {
                    continue;
                }
                assertFalse(withSetting(base.get(), row.key(), offValue(row.key()))
                                .isSimilar(other.differing().get()),
                        off + "a different " + other.attribute() + " must STILL be refused by "
                                + other.key() + ". A setting that switches off more than its own "
                                + "comparison is a duplication exploit, and it is the failure this "
                                + "assertion exists for");
            }
        }
    }

    private static ShopItemStack withSetting(ItemStack stack, ShopItemStackSettingKeys key, Object value) {
        ShopItemStack item = new ShopItemStack(stack);
        item.setShopSettings(key, new ObjectHolder<>(value));
        return item;
    }

    /** {@code COMPARE_DURABILITY} is an int with four states; everything else is a boolean. */
    private static Object offValue(ShopItemStackSettingKeys key) {
        return key == ShopItemStackSettingKeys.COMPARE_DURABILITY ? Integer.valueOf(-1) : Boolean.FALSE;
    }

    private static Object onValue(ShopItemStackSettingKeys key) {
        return key == ShopItemStackSettingKeys.COMPARE_DURABILITY ? Integer.valueOf(1) : Boolean.TRUE;
    }

    // ------------------------------------------------------------------
    // Fixtures
    // ------------------------------------------------------------------

    private static ItemStack sword() {
        return sword(2.0, meta -> {
        });
    }

    private static ItemStack sword(Consumer<ItemMeta> change) {
        return sword(2.0, change);
    }

    /**
     * The loaded sword, with the attack modifier's amount as a parameter.
     *
     * <p>A parameter rather than an edit, because {@code addAttributeModifier}
     * appends: a variant built by adding a second modifier would differ by
     * <em>having two</em> rather than by carrying a different one, and the row
     * would stop being about one attribute.
     */
    private static ItemStack sword(double attackDamage, Consumer<ItemMeta> change) {
        return Items.with(Material.DIAMOND_SWORD, meta -> {
            meta.setDisplayName("Kingsblade");
            meta.setLore(List.of("first line", "second line"));
            meta.setCustomModelData(7);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.setUnbreakable(true);
            meta.addEnchant(Enchantment.SHARPNESS, 3, true);
            ((Damageable) meta).setDamage(11);
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, Items.attackDamage(attackDamage));
            change.accept(meta);
        });
    }

    private static ItemStack ledger(Consumer<BookMeta> change) {
        return Items.with(Material.WRITTEN_BOOK, meta -> {
            BookMeta book = (BookMeta) meta;
            // The legacy String API on purpose: BookMetaMock aborts every Adventure
            // Component accessor, this build counts an abort as a failure, and the
            // comparator calls the String API anyway.
            book.setTitle("Ledger");
            book.setAuthor("Pie");
            book.setPages(List.of("one", "two"));
            book.setLore(List.of("ink", "paper"));
            change.accept(book);
        });
    }

    private static ItemStack rocket(int power, Color colour, String name) {
        return Items.with(Material.FIREWORK_ROCKET, meta -> {
            FireworkMeta firework = (FireworkMeta) meta;
            firework.setPower(power);
            firework.addEffect(Items.burst(colour));
            firework.setDisplayName(name);
        });
    }

    private static ItemStack draught(PotionType base, String name, List<String> lore) {
        return Items.with(Material.POTION, meta -> {
            ((PotionMeta) meta).setBasePotionType(base);
            meta.setDisplayName(name);
            meta.setLore(lore);
        });
    }

    /** A bundle holding a diamond and one other material, under a display name. */
    private static ItemStack kit(Material second, String name) {
        ItemStack stack = Items.bundle(new ItemStack(Material.DIAMOND), new ItemStack(second));
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        stack.setItemMeta(meta);
        return stack;
    }
}
