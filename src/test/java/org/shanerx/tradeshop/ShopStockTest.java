package org.shanerx.tradeshop;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.shanerx.tradeshop.harness.ShopScenario;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shop.ShopStatus;
import org.shanerx.tradeshop.shoplocation.ShopLocation;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Stocking a shop: the owner puts the product in the chest and shuts the lid, and
 * the sign is supposed to notice.
 */
class ShopStockTest extends ShopScenario {

    @Test
    void closingTheChestAfterStockingItOpensTheShop() {
        createShop("1 DIAMOND", "1 EMERALD");
        Shop shop = Shop.loadShop(new ShopLocation(signBlock.getLocation()));
        assertEquals(ShopStatus.OUT_OF_STOCK, shop.getStatus(), "precondition: the shop starts empty");

        stockShop(new ItemStack(Material.DIAMOND, 10));
        closeChestAsOwner();

        Shop reloaded = Shop.loadShop(new ShopLocation(signBlock.getLocation()));
        assertEquals(ShopStatus.OPEN, reloaded.getStatus(), "a stocked shop should be open");
        assertEquals(10, reloaded.getAvailableTrades(),
                "ten diamonds at one per trade is ten trades");
    }

    @Test
    void stockDoesNotLeakBetweenShopsInDifferentWorlds() {
        createShop("1 DIAMOND", "1 EMERALD");
        stockShop(new ItemStack(Material.DIAMOND, 3));
        closeChestAsOwner();

        Shop shop = Shop.loadShop(new ShopLocation(signBlock.getLocation()));
        assertEquals(3, shop.getAvailableTrades(),
                "this shop's count must come from this shop's chest, not one an earlier test filled");
        assertEquals(3, countInChest(Material.DIAMOND));
    }
}
