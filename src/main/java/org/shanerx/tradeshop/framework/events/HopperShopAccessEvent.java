package org.shanerx.tradeshop.framework.events;

import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.objects.Shop;

/**
 * This event is is fired when a hopper attempts to modify the contents of a shop's inventory.
 * While the event does not implement {@link org.bukkit.event.Cancellable}, it actually is possible to
 * modify the outcome by using {@link HopperShopAccessEvent#setForbidden(boolean)}.
 * Note: This method does NOT work like {@link org.bukkit.event.Cancellable#setCancelled(boolean)}
 */
public class HopperShopAccessEvent extends InventoryMoveItemEvent {

	public enum HopperDirection {
		FROM_HOPPER,
		TO_HOPPER
	}
	
	private static final HandlerList handlers = new HandlerList();
	private Shop shop;
	private boolean isForbidden;
	private HopperDirection dir;
	
	/**
	 * Constructor for the object.
	 * @param s The {@link Shop} object representing the Shop in question.
	 * @param source The source {@link org.bukkit.inventory.Inventory} object.
	 * @param destination The source {@link org.bukkit.inventory.Inventory} object.
	 * @param itm The {@link org.bukkit.inventory.ItemStack} object representing the item in transaction.
	 * @param isForbidden Whether or not the trade is meant to happen.
	 */
	public HopperShopAccessEvent(Shop s, Inventory source, Inventory destination, ItemStack itm, boolean isForbidden, HopperDirection dir) {
		super(source, itm, destination, false);
		this.isForbidden = isForbidden;
		this.dir = dir;
	}
	
	/**
	 * Returns the {@link Shop} object representing the player shop this event is about.
	 * @return the shop.
	 */
	public Shop getShop() {
		return shop;
	}
	
	/**
	 * Whether or not to forbid the item from making the transition. The default value is the one set in the plugin configuration.
	 * @return `true` if the transition is being blocked. 
	 */
	public boolean isForbidden() {
		return isForbidden;
	}
	
	/**
	 * Set whether or not to block the transition from happening.
	 * @param forbidden `true` if the transition must be blocked.
	 */
	public void setForbidden(boolean forbidden) {
		isForbidden = forbidden;
	}

	public HopperDirection getItemDirection() {
		return dir;
	}
}
