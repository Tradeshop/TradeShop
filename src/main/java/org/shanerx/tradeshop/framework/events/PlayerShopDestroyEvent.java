package org.shanerx.tradeshop.framework.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.shanerx.tradeshop.objects.Shop;

public class PlayerShopDestroyEvent extends PlayerEvent implements Cancellable {
	
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private Shop shop;
	
	public PlayerShopDestroyEvent(Player p, Shop shop) {
		super(p);
		this.shop = shop;
	}
	
	@Override
	public HandlerList getHandlers() {
		return null;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	public Shop getShop() {
		return shop;
	}
}
