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

package org.shanerx.tradeshop.framework.events;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.shop.Shop;

/**
 * This class represents the event being fired  when someone who has the required permissions attempts to open a shop.
 * It implements {@link org.bukkit.event.Cancellable}, which makes it possible to cancel the event.
 */
public class PlayerShopInventoryOpenEvent extends PlayerInteractEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final Shop shop;
	private boolean cancelled;
	
	/**
	 * Constructor for the object.
	 * @param p The {@link org.bukkit.entity.Player} object which represents the player who opens the shop inventory.
	 * @param s The {@link Shop} object which represents the shop the player is attempting to access.
	 * @param action The {@link org.bukkit.event.block.Action} enum entry which represents the action which was performed.
	 * @param itm The {@link org.bukkit.inventory.ItemStack} object representing the item the player holds in their hands.
	 * @param chest The chest (or other inventory-equipped block) which holds the shop inventory.
	 * @param chestFace The face of the block which was clicked.
	 */
	public PlayerShopInventoryOpenEvent(Player p, Shop s, Action action, ItemStack itm, Block chest, BlockFace chestFace) {
		super(p, action, itm, chest, chestFace);
		this.shop = s;
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
