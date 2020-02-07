package org.shanerx.tradeshop.framework.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.shanerx.tradeshop.framework.ShopChange;
import org.shanerx.tradeshop.objects.Shop;
import org.shanerx.tradeshop.utils.ObjectHolder;

public class PlayerShopChangeEvent extends PlayerEvent implements Cancellable {
	
	private static final HandlerList handlers = new HandlerList();
	private Shop shop;
	private ObjectHolder<?> what;
	private boolean cancelled;
	
	public PlayerShopChangeEvent(Player player, Shop s, ShopChange change, ObjectHolder<?> what) {
		super(player);
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
	
	public ObjectHolder<?> getWhat() {
		return what;
	}
}
