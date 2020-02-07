package org.shanerx.tradeshop.framework.events;

import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.objects.Shop;

public class HopperShopAccessEvent extends InventoryMoveItemEvent {
	
	private static final HandlerList handlers = new HandlerList();
	private Shop shop;
	private boolean isForbidden;
	
	public HopperShopAccessEvent(Shop s, Inventory source, Inventory destination, ItemStack itm, boolean isForbidden) {
		super(source, itm, destination, false);
		this.isForbidden = isForbidden;
	}
	
	public Shop getShop() {
		return shop;
	}
	
	public boolean isForbidden() {
		return isForbidden;
	}
	
	public void setForbidden(boolean forbidden) {
		isForbidden = forbidden;
	}
}
