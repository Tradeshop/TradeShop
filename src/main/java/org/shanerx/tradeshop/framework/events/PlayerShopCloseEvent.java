package org.shanerx.tradeshop.framework.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.shanerx.tradeshop.objects.Shop;

/**
 * This class represents the event which is fired when the owner of a shop closes the shop.
 * Since it implements {@link org.bukkit.event.Cancellable}, it is possible to cancel the event.
 */
public class PlayerShopCloseEvent extends PlayerEvent implements Cancellable {
	
	private static final HandlerList handlers = new HandlerList();
	private Shop shop;
	private boolean cancelled;
	
	/**
	 * Constructor for the object.
	 * @param buyer The {@link org.bukkit.entity.Player} object representing the player who is performing the action.
	 * @param s The {@link Shop} object which represents the player shop which is being closed.
	 */
	public PlayerShopCloseEvent(Player buyer, Shop s) {
		super(buyer);
		this.shop = s;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	/**
	 * Returns the {@link Shop} object representing the player shop this event is about.
	 * @return the shop.
	 */
	public Shop getShop() {
		return shop;
	}
	
	/**
	 * Returns whether or not the event has been cancelled.
	 * @return true if the event is being cancelled.
	 */
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	/**
	 * Choose whether or not to cancel the event.
	 * @param cancelled true if the event should be cancelled.
	 */
	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}
