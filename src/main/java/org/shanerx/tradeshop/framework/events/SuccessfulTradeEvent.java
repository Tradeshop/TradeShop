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

/**
 * This class represents the event which is fired when a player attempts to perform a transaction with a shop.
 * Note: This event is ONLY fired when all the necessary conditions for the transaction are met, and it is fired JUST BEFORE it happens.
 * This makes it possible to cancel the event moments before the trade takes place, by using {@link org.bukkit.event.Cancellable}.
 */
public class SuccessfulTradeEvent extends PlayerInteractEvent {

	private static final HandlerList handlers = new HandlerList();
	private Shop shop;
	private List<ItemStack> product;
	private List<ItemStack> cost;
	private Block clickedBlock;
	
	/**
	 * Constructor for the object.
	 * @param who The {@link org.bukkit.entity.Player} object representing the player who is attempting the trade.
	 * @param cost The object representing the items which are being traded.
	 * @param product The object representing the items being traded for.
	 * @param shop The object representing the shop at which the trade takes place.
	 * @param clickedBlock The {@link org.bukkit.block.Block} that was clicked, ie. the sign.
	 * @param clickedFace  The {@link org.bukkit.block.BlockFace} object representing the face of the block that was clicked.
	 */
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
	
	/**
	 * Returns the {@link Shop} object representing the player shop this event is about.
	 * @return the shop.
	 */
	public Shop getShop() {
		return shop;
	}
	
	/**
	 * The items that are being bought from the shop by the player.
	 * @return A {@link java.util.List} which contains the {@link org.bukkit.inventory.ItemStack} objects which represent the items.
	 */
	public List<ItemStack> getProduct() {
		return product;
	}
	
	/**
	 * The items that are being paid to the shop by the player.
	 * @return A {@link java.util.List} which contains the {@link org.bukkit.inventory.ItemStack} objects which represent the items.
	 */
	public List<ItemStack> getCost() {
		return cost;
	}
}
