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

import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.shop.Shop;

/**
 * This event is is fired when a hopper attempts to modify the contents of a shop's inventory.
 * While the event does not implement {@link org.bukkit.event.Cancellable}, it actually is possible to
 * modify the outcome by using {@link HopperShopAccessEvent#setForbidden(boolean)}.
 * Note: This method does NOT work like {@link org.bukkit.event.Cancellable#setCancelled(boolean)}
 */
public class HopperShopAccessEvent extends InventoryMoveItemEvent {

	public enum HopperDirection {
		FROM_HOPPER,
		TO_HOPPER
	}
	
	private static final HandlerList handlers = new HandlerList();
	private Shop shop;
	private boolean isForbidden;
	private final HopperDirection dir;

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	/**
	 * Constructor for the object.
	 * @param s The {@link Shop} object representing the Shop in question.
	 * @param source The source {@link org.bukkit.inventory.Inventory} object.
	 * @param destination The source {@link org.bukkit.inventory.Inventory} object.
	 * @param itm The {@link org.bukkit.inventory.ItemStack} object representing the item in transaction.
	 */
	public HopperShopAccessEvent(Shop s, Inventory source, Inventory destination, ItemStack itm, HopperDirection dir) {
		super(source, itm, destination, false);
		this.isForbidden = false;
		this.dir = dir;
	}
	
	/**
	 * Returns the {@link Shop} object representing the player shop this event is about.
	 * @return the shop.
	 */
	public Shop getShop() {
		return shop;
	}
	
	/**
	 * Whether or not to forbid the item from making the transition. The default value is the one set in the plugin configuration.
	 * @return `true` if the transition is being blocked. 
	 */
	public boolean isForbidden() {
		return isForbidden;
	}
	
	/**
	 * Set whether or not to block the transition from happening.
	 * @param forbidden `true` if the transition must be blocked.
	 */
	public void setForbidden(boolean forbidden) {
		isForbidden = forbidden;
	}

	public HopperDirection getItemDirection() {
		return dir;
	}
}
