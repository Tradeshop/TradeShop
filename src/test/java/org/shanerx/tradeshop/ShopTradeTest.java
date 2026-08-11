package org.shanerx.tradeshop;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.shanerx.tradeshop.harness.ShopScenario;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The trade itself: the thing a human would otherwise log in, place a chest,
 * write a sign and click to check. Assertions are on the items that moved, because
 * items moving is what the human was looking at.
 */
class ShopTradeTest extends ShopScenario {

    @Test
    void buyerWithEnoughCostReceivesTheProduct() {
        createShop("1 DIAMOND", "1 EMERALD");
        stockShop(new ItemStack(Material.DIAMOND, 10));
        closeChestAsOwner();

        PlayerMock buyer = buyerHolding(new ItemStack(Material.EMERALD, 5));
        rightClickSign(buyer);

        assertEquals(1, countOf(buyer, Material.DIAMOND), "buyer should have received one diamond");
        assertEquals(4, countOf(buyer, Material.EMERALD), "buyer should have paid one emerald");
        assertEquals(1, countInChest(Material.EMERALD), "the emerald should be in the shop chest");
        assertEquals(9, countInChest(Material.DIAMOND), "the shop should have one fewer diamond");
    }

    @Test
    void buyerWhoCannotPayGetsNothingAndTheShopKeepsItsStock() {
        createShop("1 DIAMOND", "1 EMERALD");
        stockShop(new ItemStack(Material.DIAMOND, 10));
        closeChestAsOwner();

        PlayerMock buyer = buyerHolding(new ItemStack(Material.DIRT, 1));
        rightClickSign(buyer);

        assertEquals(0, countOf(buyer, Material.DIAMOND), "buyer paid nothing so should receive nothing");
        assertEquals(10, countInChest(Material.DIAMOND), "the shop should still hold all ten diamonds");
    }
}
