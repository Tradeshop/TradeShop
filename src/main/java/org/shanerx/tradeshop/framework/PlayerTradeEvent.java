/*
 *
 *                         Copyright (c) 2016-2019
 *                SparklingComet @ http://shanerx.org
 *               KillerOfPie @ http://killerofpie.github.io
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *                http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  NOTICE: All modifications made by others to the source code belong
 *  to the respective contributor. No contributor should be held liable for
 *  any damages of any kind, whether be material or moral, which were
 *  caused by their contribution(s) to the project. See the full License for more information.
 *
 */

package org.shanerx.tradeshop.framework;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.objects.Shop;

import java.util.List;

public class PlayerTradeEvent extends PlayerInteractEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private Shop shop;
	private List<ItemStack> product;
	private List<ItemStack> cost;
	private Block clickedBlock;
	private boolean cancelled;

	public PlayerTradeEvent(Player who, List<ItemStack> cost, List<ItemStack> product, Shop shop, Block clickedBlock, BlockFace clickedFace) {
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
	
	// Event API: Cancellable
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}
