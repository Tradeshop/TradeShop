/*
 *
 *                         Copyright (c) 2016-2023
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
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.shop.Shop;

import java.util.List;

/**
 * This class represents the event which is fired when a player attempts to perform a transaction with a shop.
 * Note: This event is ONLY fired when all the necessary conditions for the transaction are met, and it is fired JUST BEFORE it happens.
 * This makes it possible to cancel the event moments before the trade takes place, by using {@link org.bukkit.event.Cancellable}.
 */
public class PlayerSuccessfulTradeEvent extends PlayerInteractEvent {

    private static final HandlerList handlers = new HandlerList();
    private final Shop shop;
    private final List<ItemStack> product;
    private final List<ItemStack> cost;
    private final Block clickedBlock;

    /**
     * Constructor for the object.
     *
     * @param who          The {@link org.bukkit.entity.Player} object representing the player who is attempting the trade.
     * @param cost         The object representing the items which are being traded.
     * @param product      The object representing the items being traded for.
     * @param shop         The object representing the shop at which the trade takes place.
     * @param clickedBlock The {@link org.bukkit.block.Block} that was clicked, ie. the sign.
     * @param clickedFace  The {@link org.bukkit.block.BlockFace} object representing the face of the block that was clicked.
     */
    public PlayerSuccessfulTradeEvent(Player who, List<ItemStack> cost, List<ItemStack> product, Shop shop, Block clickedBlock, BlockFace clickedFace) {
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
     *
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
