package org.shanerx.tradeshop.framework.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.shanerx.tradeshop.framework.ShopChange;
import org.shanerx.tradeshop.objects.Shop;
import org.shanerx.tradeshop.utils.ObjectHolder;

/**
 * This class represents a {@link org.bukkit.event.Cancellable} event, which gets fired when someone is about to
 * modify shop information such as product, cost, managers and members.
 */
public class PlayerShopChangeEvent extends PlayerEvent implements Cancellable {
	
	private static final HandlerList handlers = new HandlerList();
	private Shop shop;
	private ObjectHolder<?> what;
	private boolean cancelled;
	
	/**
	 * Constructor for the object.
	 * @param player The {@link org.bukkit.entity.Player} object representing the player who is attempting the change.
	 * @param s The {@link Shop} object representing the shop.
	 * @param change The {@link ShopChange} enum entry which represents the kind of action which caused the event to be fired.
	 * @param what The data representing the object the change is about, wrapped inside an {@link ObjectHolder} for polymorphism purposes.
	 */
	public PlayerShopChangeEvent(Player player, Shop s, ShopChange change, ObjectHolder<?> what) {
		super(player);
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
	
	/**
	 * Returns the {@link ObjectHolder} instance which wraps the object representing the data which is being changed.
	 * This can be of type {@link org.bukkit.entity.Player}, {@link org.bukkit.inventory.ItemStack} or {@link java.lang.Integer}.
	 * @return the object the change is about.
	 */
	public ObjectHolder<?> getWhat() {
		return what;
	}
}
