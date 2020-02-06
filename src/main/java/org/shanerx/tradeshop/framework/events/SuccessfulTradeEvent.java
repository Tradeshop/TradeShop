package org.shanerx.tradeshop.framework.events;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.objects.Shop;

import java.util.List;

public class SuccessfulTradeEvent extends PlayerInteractEvent {
	
	private static final HandlerList handlers = new HandlerList();
	private Shop shop;
	private List<ItemStack> product;
	private List<ItemStack> cost;
	private Block clickedBlock;
	
	public SuccessfulTradeEvent(Player who, List<ItemStack> cost, List<ItemStack> product, Shop shop, Block clickedBlock, BlockFace clickedFace) {
		super(who, Action.RIGHT_CLICK_BLOCK, null, shop.getShopSign().getBlock(), clickedFace);
		this.shop = shop;
		this.product = product;
		this.cost = cost;
		this.clickedBlock = clickedBlock;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public Shop getShop() {
		return shop;
	}
	
	@Override
	public Block getClickedBlock() {
		return clickedBlock;
	}
	
	public List<ItemStack> getProduct() {
		return product;
	}
	
	public List<ItemStack> getCost() {
		return cost;
	}
}
