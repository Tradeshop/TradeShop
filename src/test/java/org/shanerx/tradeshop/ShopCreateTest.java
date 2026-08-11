package org.shanerx.tradeshop;

import org.bukkit.ChatColor;
import org.junit.jupiter.api.Test;
import org.shanerx.tradeshop.harness.ShopScenario;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shop.ShopStatus;
import org.shanerx.tradeshop.shop.ShopType;
import org.shanerx.tradeshop.shoplocation.ShopLocation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Writing a shop sign on a chest: the first thing a human tester does, and the
 * gate everything else is behind.
 */
class ShopCreateTest extends ShopScenario {

    @Test
    void writingAShopSignOnAChestCreatesAShopOwnedByTheSigner() {
        createShop("1 DIAMOND", "1 EMERALD");

        Shop shop = Shop.loadShop(new ShopLocation(signBlock.getLocation()));

        assertNotNull(shop, "a shop should have been stored at the sign's location");
        assertEquals(ShopType.TRADE, shop.getShopType());
        assertEquals(owner.getUniqueId(), shop.getOwner().getUUID(), "the signer should own the shop");
        assertEquals(chestBlock.getLocation(), shop.getInventoryLocation(),
                "the shop should be linked to the chest under the sign");
    }

    @Test
    void anEmptyNewShopAdvertisesItselfAsOutOfStock() {
        createShop("1 DIAMOND", "1 EMERALD");

        Shop shop = Shop.loadShop(new ShopLocation(signBlock.getLocation()));

        assertEquals(ShopStatus.OUT_OF_STOCK, shop.getStatus(),
                "nothing has been put in the chest yet");
        assertTrue(ChatColor.stripColor(signLines()[0]).contains(ShopType.TRADE.toHeader()),
                "the header the player typed should survive onto the sign, line 0 was: " + signLines()[0]);
        assertEquals(ChatColor.stripColor(ShopStatus.OUT_OF_STOCK.getLine()),
                ChatColor.stripColor(signLines()[3]),
                "line 3 is where a player reads the shop's status");
    }
}
