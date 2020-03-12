package org.shanerx.tradeshop.framework.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.shanerx.tradeshop.objects.Shop;

/**
 * This class represents the event being fired  upon shop creation. It implements {@link org.bukkit.event.Cancellable},
 * which makes it possible to cancel the event.
 */
public class PlayerShopCreateEvent extends PlayerEvent implements Cancellable {
	
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private Shop shop;
	
	/**
	 * Constructor for the object.
	 * @param p The {@link org.bukkit.entity.Player} object which represents the creator of the shop.
	 * @param shop The {@link Shop} object representing the shop which is being created.
	 */
	public PlayerShopCreateEvent(Player p, Shop shop) {
		super(p);
		this.shop = shop;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
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
	
	/**
	 * Returns the {@link Shop} object representing the player shop this event is about.
	 * @return the shop.
	 */
	public Shop getShop() {
		return shop;
	}
}
