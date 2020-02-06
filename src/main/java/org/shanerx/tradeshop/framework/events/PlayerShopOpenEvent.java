package org.shanerx.tradeshop.framework.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.shanerx.tradeshop.objects.Shop;

public class PlayerShopOpenEvent extends PlayerEvent implements Cancellable {
	
	private static final HandlerList handlers = new HandlerList();
	private Shop shop;
	private boolean cancelled;
	
	public PlayerShopOpenEvent(Player buyer, Shop s) {
		super(buyer);
		this.shop = s;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public Shop getShop() {
		return shop;
	}
	
	public Player getBuyer() {
		return super.getPlayer();
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}
