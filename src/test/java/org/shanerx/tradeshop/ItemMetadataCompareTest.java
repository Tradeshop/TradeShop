package org.shanerx.tradeshop;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.shanerx.tradeshop.harness.Items;
import org.shanerx.tradeshop.harness.MockBukkitEnvironment;
import org.shanerx.tradeshop.item.ShopItemStack;
import org.shanerx.tradeshop.item.ShopItemStackSettingKeys;
import org.shanerx.tradeshop.utils.objects.ObjectHolder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The trade gate, item by item: {@code ShopItemStack.isSimilar}.
 *
 * <h2>Why the comparator is called directly and not through a trade</h2>
 * {@code ShopTradeListener:153 -> Utils.canExchangeAll -> Utils.getItems:667}
 * reaches this method, and it is the only comparison on the trade path.
 * Everything downstream of it - {@code Inventory.removeItem}, {@code addItem} -
 * runs against {@code ItemStackMock.isSimilar}, which compares the item type and
 * nothing else. A tier-1 row driven through a whole trade would therefore be
 * asserting the mock's equality, not the plugin's, so the rows here call the gate.
 *
 * <h2>The assertion that matters is the negative one</h2>
 * A comparator that is too loose is a duplication exploit: it lets a buyer pay
 * with something cheaper than the shop asked for. So every row states a
 * <em>near</em>-identical item and asserts it is refused - one page different, one
 * enchantment level, one lore line - and only then states the identical one and
 * asserts it is accepted.
 *
 * <h2>Two rows here are written to fail</h2>
 * {@link #potionsAreComparedByMaterialAlone()} and
 * {@link #aShopSellingPlainRocketsRefusesDecoratedOnes()} assert the behaviour a
 * shop owner is entitled to and do not get it. They are red on today's code on
 * purpose: a test that names a defect outlives a findings row, and a test edited
 * to match what it observed asserts nothing.
 *
 * <h2>What is deliberately not here</h2>
 * Every round trip and every nested container is tier 2, without exception.
 * MockBukkit is not a weaker CraftBukkit in those places, it is a different
 * relation: {@code ItemMetaMock.serialize()} is not {@code CraftMetaItem.serialize()}
 * - different keys, no {@code "v"}, no DataFixerUpper - and there is no
 * {@code BlockStateMetaMock} at all, so the shulker cast at
 * {@code ShopItemStack.java:314-315} throws {@code ClassCastException}, is caught
 * at {@code :331} and returns {@code false}. A tier-1 shulker non-match would pass
 * without executing one line of the comparison.
 */
@ExtendWith(MockBukkitEnvironment.class)
class ItemMetadataCompareTest {

    // ------------------------------------------------------------------
    // Books
    // ------------------------------------------------------------------

    /**
     * A written book is identified by what is written in it.
     *
     * <p>Three separate settings decide this - {@code COMPARE_NAME} takes the book
     * branch and compares the title ({@code :437-443}), {@code COMPARE_BOOK_AUTHOR}
     * the author ({@code :455}), {@code COMPARE_BOOK_PAGES} the pages ({@code :472})
     * - so one row per book is not enough to say the branch works; each difference
     * is stated on its own.
     */
    @Test
    void booksAreComparedByTitleAuthorAndPages() {
        ShopItemStack shop = shopItem(Items.book("Ledger", "Pie", "one", "two"));

        assertTrue(shop.isSimilar(Items.book("Ledger", "Pie", "one", "two")),
                "the same book should be accepted");

        assertFalse(shop.isSimilar(Items.book("Ledgers", "Pie", "one", "two")),
                "a different title is a different book");
        assertFalse(shop.isSimilar(Items.book("Ledger", "Someone else", "one", "two")),
                "a different author is a different book");
        assertFalse(shop.isSimilar(Items.book("Ledger", "Pie", "one", "three")),
                "one page different is a different book, and this is the assertion a duplication "
                        + "exploit turns on");
        assertFalse(shop.isSimilar(Items.book("Ledger", "Pie", "one")),
                "a book with a page missing is a different book");
        assertFalse(shop.isSimilar(Items.book("Ledger", "Pie", "one", "two", "three")),
                "a book with an extra page is a different book");
    }

    // ------------------------------------------------------------------
    // Potions - written to fail, D1
    // ------------------------------------------------------------------

    /**
     * WRITTEN TO FAIL. Every potion of a form shares one {@link Material}, and
     * {@code ShopItemStack.isSimilar} has no {@code PotionMeta} branch at all - the
     * word "potion" does not appear in the file. Base type, extended/upgraded,
     * custom effects and colour are all invisible to it.
     *
     * <p>So a shop whose cost is a Potion of Strength accepts a Potion of Harming,
     * and a shop selling an extended potion accepts the base one. That is a
     * duplication exploit priced in whatever the shop asked for, not a cosmetic
     * bug, and it is asserted here as refused because refused is what it should be.
     */
    @Test
    void potionsAreComparedByMaterialAlone() {
        ShopItemStack strength = shopItem(Items.potion(PotionType.STRENGTH));

        assertFalse(strength.isSimilar(Items.potion(PotionType.HARMING)),
                "a shop asking for Strength must not accept Harming - D1, potions are compared by "
                        + "material only and every potion is Material.POTION");
        assertFalse(strength.isSimilar(Items.potion(PotionType.LONG_STRENGTH)),
                "an extended potion is not the base potion - D1");
        assertFalse(strength.isSimilar(Items.potion(PotionType.STRONG_STRENGTH)),
                "an upgraded potion is not the base potion - D1");

        ShopItemStack plainWater = shopItem(Items.potion(PotionType.WATER));
        assertFalse(plainWater.isSimilar(
                        Items.potionWithCustomEffect(PotionType.WATER, PotionEffectType.POISON)),
                "a water bottle with a custom effect brewed into it is not a water bottle - D1");
    }

    // ------------------------------------------------------------------
    // Fireworks
    // ------------------------------------------------------------------

    /**
     * Flight duration and the effects, in the order they were added.
     *
     * <p>Ordering is asserted because the comparator walks the two effect lists by
     * index ({@code :539-545}) rather than as sets, so two rockets carrying the same
     * two effects in the other order are a non-match. That is the behaviour; the row
     * pins it so a later change to set semantics is a decision rather than a
     * surprise.
     */
    @Test
    void fireworksComparePowerEffectsAndTheirOrder() {
        ShopItemStack shop = shopItem(Items.rocket(2, Items.burst(Color.RED), Items.burst(Color.BLUE)));

        assertTrue(shop.isSimilar(Items.rocket(2, Items.burst(Color.RED), Items.burst(Color.BLUE))),
                "the same rocket should be accepted");

        assertFalse(shop.isSimilar(Items.rocket(1, Items.burst(Color.RED), Items.burst(Color.BLUE))),
                "a shorter flight duration is a different rocket");
        assertFalse(shop.isSimilar(Items.rocket(2, Items.burst(Color.RED), Items.burst(Color.GREEN))),
                "one effect of a different colour is a different rocket");
        assertFalse(shop.isSimilar(Items.rocket(2, Items.burst(Color.BLUE), Items.burst(Color.RED))),
                "the effects are compared in order, so the same two effects swapped are a non-match");
        assertFalse(shop.isSimilar(Items.rocket(2, Items.burst(Color.RED))),
                "a rocket with one of the two effects missing is a different rocket");
    }

    /**
     * WRITTEN TO FAIL. {@code ShopItemStack.java:534} only enters the effect
     * comparison {@code if (fireworkMeta.hasEffects())} - that is, only when the
     * <em>shop's</em> rocket has effects. The buyer's is never examined on its own.
     *
     * <p>A shop selling plain rockets therefore accepts decorated ones, and the
     * shop is paid the cheaper item. The opposite direction is asserted too, because
     * it works: it is the one-sidedness that is the defect, not the check.
     */
    @Test
    void aShopSellingPlainRocketsRefusesDecoratedOnes() {
        ShopItemStack decorated = shopItem(Items.rocket(1, Items.burst(Color.RED)));
        assertFalse(decorated.isSimilar(Items.rocket(1)),
                "precondition: a shop asking for a decorated rocket already refuses a plain one");

        ShopItemStack plain = shopItem(Items.rocket(1));
        assertFalse(plain.isSimilar(Items.rocket(1, Items.burst(Color.RED))),
                "a shop dealing in plain rockets must not accept a decorated one - D3, the effect "
                        + "comparison at :534 only runs when the SHOP's rocket has effects");
    }

    // ------------------------------------------------------------------
    // Enchantments
    // ------------------------------------------------------------------

    /**
     * The level is part of the enchantment, and an enchanted book stores its
     * enchantments somewhere else.
     *
     * <p>Both arms of {@code :402-430} are stated here because they are genuinely
     * different code: an {@code EnchantmentStorageMeta} on both sides compares
     * {@code getStoredEnchants()}, anything else compares {@code getEnchants()}. An
     * enchanted book's own {@code getEnchants()} is empty, so a row that only
     * exercised the second arm would call two different books equal.
     */
    @Test
    void enchantmentLevelsAndEnchantedBookStorageAreCompared() {
        ShopItemStack sharpThree = shopItem(Items.enchanted(Material.DIAMOND_SWORD, Enchantment.SHARPNESS, 3));

        assertTrue(sharpThree.isSimilar(Items.enchanted(Material.DIAMOND_SWORD, Enchantment.SHARPNESS, 3)),
                "the same enchantment at the same level should be accepted");
        assertFalse(sharpThree.isSimilar(Items.enchanted(Material.DIAMOND_SWORD, Enchantment.SHARPNESS, 2)),
                "one level lower is a cheaper item and must be refused");
        assertFalse(sharpThree.isSimilar(Items.enchanted(Material.DIAMOND_SWORD, Enchantment.EFFICIENCY, 3)),
                "a different enchantment at the same level is a different item");
        assertFalse(sharpThree.isSimilar(Items.with(Material.DIAMOND_SWORD, meta -> {
                    meta.addEnchant(Enchantment.SHARPNESS, 3, true);
                    meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                })),
                "an extra enchantment is a different item");

        ShopItemStack storedThree = shopItem(Items.enchantedBook(Enchantment.SHARPNESS, 3));

        assertTrue(storedThree.isSimilar(Items.enchantedBook(Enchantment.SHARPNESS, 3)),
                "the same enchanted book should be accepted");
        assertFalse(storedThree.isSimilar(Items.enchantedBook(Enchantment.SHARPNESS, 2)),
                "an enchanted book storing a lower level is a cheaper book");
        assertFalse(storedThree.isSimilar(Items.enchantedBook(Enchantment.EFFICIENCY, 3)),
                "an enchanted book storing a different enchantment is a different book");

        // "...against an enchanted book storing NOTHING" is deliberately absent
        // here and asserted at tier 2 instead. An empty book has no metadata on a
        // real server, which puts that pair on the useMeta path (D2) rather than on
        // this branch - and the mock, which reports a blank meta as present, would
        // have shown it passing.
    }

    // ------------------------------------------------------------------
    // The six-attribute sweep
    // ------------------------------------------------------------------

    /**
     * One item carrying six attributes, and six buyers each differing in exactly
     * one of them.
     *
     * <p>Stated as one difference at a time on purpose: an item that differs in
     * several is refused by whichever check runs first, so it says nothing about the
     * other five.
     */
    @Test
    void aNearIdenticalItemIsRefusedOnEachOfSixAttributes() {
        ShopItemStack shop = shopItem(loaded(0));

        assertTrue(shop.isSimilar(loaded(0)), "an item identical in all six attributes is accepted");

        assertFalse(shop.isSimilar(edit(loaded(0), meta -> meta.setDisplayName("Queensblade"))),
                "a different display name is a different item");
        assertFalse(shop.isSimilar(edit(loaded(0), meta -> meta.setLore(java.util.List.of("first line", "changed")))),
                "one lore line different is a different item");
        assertFalse(shop.isSimilar(edit(loaded(0), meta -> meta.setCustomModelData(8))),
                "different custom model data is a different item");
        assertFalse(shop.isSimilar(edit(loaded(0), meta -> meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES))),
                "an extra item flag is a different item");
        assertFalse(shop.isSimilar(edit(loaded(0), meta -> meta.setUnbreakable(false))),
                "a breakable copy of an unbreakable item is a different item");
        assertFalse(shop.isSimilar(loaded(11)),
                "one point of damage different is a different item under the default '==' mode");
    }

    /**
     * {@code COMPARE_DURABILITY} is not a boolean: {@code -1} is off, {@code 0} is
     * "the buyer's item may be no less damaged", {@code 1} is exact, {@code 2} is
     * "no more damaged" ({@code ShopItemStackSettingKeys.java:43}).
     *
     * <p>The two inequality modes are the ones worth stating, because their sense is
     * easy to invert and the failure would be silent: a shop that meant "any sword
     * at least this good" would quietly accept a nearly-broken one.
     */
    @Test
    void theThreeDamageComparisonModesAcceptAndRefuseAsConfigured() {
        assertTrue(damageMode(1, 10).isSimilar(damaged(10)), "'==' accepts the same damage");
        assertFalse(damageMode(1, 10).isSimilar(damaged(9)), "'==' refuses a less damaged item");
        assertFalse(damageMode(1, 10).isSimilar(damaged(11)), "'==' refuses a more damaged item");

        assertTrue(damageMode(0, 10).isSimilar(damaged(11)),
                "'<=' accepts an item at least as damaged as the shop's");
        assertTrue(damageMode(0, 10).isSimilar(damaged(10)), "'<=' accepts equal damage");
        assertFalse(damageMode(0, 10).isSimilar(damaged(9)),
                "'<=' refuses an item less damaged than the shop's");

        assertTrue(damageMode(2, 10).isSimilar(damaged(9)),
                "'>=' accepts an item no more damaged than the shop's");
        assertTrue(damageMode(2, 10).isSimilar(damaged(10)), "'>=' accepts equal damage");
        assertFalse(damageMode(2, 10).isSimilar(damaged(11)),
                "'>=' refuses an item more damaged than the shop's");

        assertTrue(damageMode(-1, 10).isSimilar(damaged(500)),
                "with the comparison off, damage stops mattering entirely");
    }

    // ------------------------------------------------------------------
    // Fixtures
    // ------------------------------------------------------------------

    private static ShopItemStack shopItem(ItemStack stack) {
        return new ShopItemStack(stack);
    }

    /** A sword carrying all six of the swept attributes, damaged by {@code damage}. */
    private static ItemStack loaded(int damage) {
        return Items.with(Material.DIAMOND_SWORD, meta -> {
            meta.setDisplayName("Kingsblade");
            meta.setLore(java.util.List.of("first line", "second line"));
            meta.setCustomModelData(7);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.setUnbreakable(true);
            ((Damageable) meta).setDamage(damage);
        });
    }

    private static ItemStack edit(ItemStack stack, java.util.function.Consumer<org.bukkit.inventory.meta.ItemMeta> change) {
        org.bukkit.inventory.meta.ItemMeta meta = stack.getItemMeta();
        change.accept(meta);
        stack.setItemMeta(meta);
        return stack;
    }

    private static ItemStack damaged(int damage) {
        return Items.damaged(Material.DIAMOND_SWORD, damage);
    }

    /** A shop item damaged by {@code damage}, comparing durability in {@code mode}. */
    private static ShopItemStack damageMode(int mode, int damage) {
        ShopItemStack item = shopItem(damaged(damage));
        item.setShopSettings(ShopItemStackSettingKeys.COMPARE_DURABILITY, new ObjectHolder<>(mode));
        return item;
    }
}
