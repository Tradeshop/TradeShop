package org.shanerx.tradeshop.framework.events;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.objects.Shop;

public class PlayerShopInventoryOpenEvent extends PlayerInteractEvent implements Cancellable {
	
	private static final HandlerList handlers = new HandlerList();
	private Shop shop;
	private boolean cancelled;
	
	public PlayerShopInventoryOpenEvent(Player p, Shop s, Action action, ItemStack itm, Block chest, BlockFace chestFace) {
		super(p, action, itm, chest, chestFace);
		this.shop = s;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public Shop getShop() {
		return shop;
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
