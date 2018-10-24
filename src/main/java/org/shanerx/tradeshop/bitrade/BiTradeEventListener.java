/*
 *     Copyright (c) 2016-2017 SparklingComet @ http://shanerx.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * NOTICE: All modifications made by others to the source code belong
 * to the respective contributor. No contributor should be held liable for
 * any damages of any kind, whether be material or moral, which were
 * caused by their contribution(s) to the project. See the full License for more information
 */

package org.shanerx.tradeshop.bitrade;

import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.Message;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.Utils;
import org.shanerx.tradeshop.trade.TradeEventListener;

import java.util.Objects;

@SuppressWarnings("unused")
public class BiTradeEventListener extends Utils implements Listener {

    private TradeShop plugin;

    public BiTradeEventListener(TradeShop instance) {
        plugin = instance;
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onBlockInteract(PlayerInteractEvent e) {

        Player buyer = e.getPlayer();

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {

            if (!isBiTradeShopSign(e.getClickedBlock())) {
                return;
            }
            Sign s = (Sign) e.getClickedBlock().getState();
            BlockState chestState;

            try {
                chestState = findShopChest(s.getBlock()).getState();
            } catch (NullPointerException npe) {
                buyer.sendMessage(colorize(getPrefix() + plugin.getMessages().getString("missing-shop")));
                return;
            }

            if (getShopUsers(chestState.getBlock()).contains(Bukkit.getOfflinePlayer(buyer.getName()))) {
                buyer.sendMessage(colorize(getPrefix() + plugin.getMessages().getString("self-owned")));
                return;
            }
            e.setCancelled(true);

            Inventory chestInventory = ((InventoryHolder) chestState).getInventory();
            Inventory playerInventory = buyer.getInventory();

            String line1 = s.getLine(1);
            String line2 = s.getLine(2);
            String[] info1 = line1.split(" ");
            String[] info2 = line2.split(" ");


            int amount1 = Integer.parseInt(info1[0]);
            int amount2 = Integer.parseInt(info2[0]);

            if (buyer.isSneaking()) {
                if (!buyer.isOnGround() && plugin.getSettings().getBoolean("allow-quad-trade")) {
                    amount1 = amount1 * 4;
                    amount2 = amount2 * 4;
                } else if (plugin.getSettings().getBoolean("allow-double-trade")) {
                    amount1 += amount1;
                    amount2 += amount2;
                }
            }

            int durability1 = 0;
            int durability2 = 0;
            if (line1.split(":").length > 1) {
                durability1 = Integer.parseInt(info1[1].split(":")[1]);
                info1[1] = info1[1].split(":")[0];
            }
            if (line2.split(":").length > 1) {
                durability2 = Integer.parseInt(info2[1].split(":")[1]);
                info2[1] = info2[1].split(":")[0];
            }

            String item_name1, item_name2;
            ItemStack item1 = null, item2 = null;

            try {
                item1 = isValidType(info1[1], durability1, amount1);
                item2 = isValidType(info2[1], durability2, amount2);
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }

            if (isBlacklisted(e, item1, item2)) return;

            if (Objects.requireNonNull(item1).hasItemMeta() && item1.getItemMeta().hasDisplayName()) {
                item_name1 = item1.getItemMeta().getDisplayName();
            } else {
                item_name1 = info1[1];
            }

            if (Objects.requireNonNull(item2).hasItemMeta() && item2.getItemMeta().hasDisplayName()) {
                item_name2 = item2.getItemMeta().getDisplayName();
            } else {
                item_name2 = info2[1];
            }

            if (containsLessThan(playerInventory, item2)) {
                buyer.sendMessage(colorize(getPrefix() + plugin.getMessages().getString("insufficient-items")
                        .replace("{ITEM}", item_name2.toLowerCase()).replace("{AMOUNT}", String.valueOf(amount2))));
                return;
            }

            if (containsLessThan(chestInventory, item1)) {
                buyer.sendMessage(colorize(getPrefix() + plugin.getMessages().getString("shop-empty")
                        .replace("{ITEM}", item_name1.toLowerCase()).replace("{AMOUNT}", String.valueOf(amount1))));
                return;
            }

            if (canNotExchange(chestInventory, item1, item2)) {
                buyer.sendMessage(colorize(getPrefix() + plugin.getMessages().getString("shop-full")
                        .replace("{ITEM}", item_name1.toLowerCase()).replace("{AMOUNT}", String.valueOf(amount1))));
                return;
            }

            if (canNotExchange(playerInventory, item2, item1)) {
                buyer.sendMessage(colorize(getPrefix() + plugin.getMessages().getString("player-full")
                        .replace("{ITEM}", item_name2.toLowerCase()).replace("{AMOUNT}", String.valueOf(amount2))));
                return;
            }

            int count = amount1, removed;
            item1 = TradeEventListener.moveToPlayer(chestInventory, playerInventory, item1, count);

            count = amount2;
            TradeEventListener.moveToChest(chestInventory, playerInventory, item1, item2, count);

            String message = Message.ON_TRADE.toString()
                    .replace("{AMOUNT1}", String.valueOf(amount1))
                    .replace("{AMOUNT2}", String.valueOf(amount2))
                    .replace("{ITEM1}", item_name1.toLowerCase())
                    .replace("{ITEM2}", item_name2.toLowerCase())
                    .replace("{SELLER}", s.getLine(3));
            buyer.sendMessage(colorize(getPrefix() + message));

        } else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {

            if (!isBiTradeShopSign(e.getClickedBlock())) {
                return;
            }

            Sign s = (Sign) e.getClickedBlock().getState();
            BlockState chestState;

            try {
                chestState = findShopChest(s.getBlock()).getState();
            } catch (NullPointerException npe) {
                buyer.sendMessage(colorize(getPrefix() + Message.MISSING_SHOP));
                return;
            }

            if (getShopUsers(chestState.getBlock()).contains(Bukkit.getOfflinePlayer(buyer.getName()))) {

                if (getShopOwners(chestState.getBlock()).contains(Bukkit.getOfflinePlayer(buyer.getName()))) {
                    return;
                }

                buyer.sendMessage(colorize(getPrefix() + Message.SELF_OWNED));
                e.setCancelled(true);
                return;
            }
            e.setCancelled(true);

            Inventory chestInventory = ((InventoryHolder) chestState).getInventory();
            Inventory playerInventory = buyer.getInventory();

            String line2 = s.getLine(2);
            String line1 = s.getLine(1);
            String[] info2 = line2.split(" ");
            String[] info1 = line1.split(" ");


            int amount2 = Integer.parseInt(info2[0]);
            int amount1 = Integer.parseInt(info1[0]);

            if (buyer.isSneaking()) {
                if (!buyer.isOnGround() && plugin.getSettings().getBoolean("allow-quad-trade")) {
                    amount2 = amount2 * 4;
                    amount1 = amount1 * 4;
                } else if (plugin.getSettings().getBoolean("allow-double-trade")) {
                    amount2 += amount2;
                    amount1 += amount1;
                }
            }

            int durability2 = 0;
            int durability1 = 0;
            if (line2.split(":").length > 1) {
                durability2 = Integer.parseInt(info2[1].split(":")[1]);
                info2[1] = info2[1].split(":")[0];
            }
            if (line1.split(":").length > 1) {
                durability1 = Integer.parseInt(info1[1].split(":")[1]);
                info1[1] = info1[1].split(":")[0];
            }

            String item_name1, item_name2;
            ItemStack item1 = null, item2 = null;

            try {
                item1 = isValidType(info1[1], durability1, amount1);
                item2 = isValidType(info2[1], durability2, amount2);
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }

            if (isBlacklisted(e, item1, item2)) return;

            if (Objects.requireNonNull(item1).hasItemMeta() && item1.getItemMeta().hasDisplayName()) {
                item_name1 = item1.getItemMeta().getDisplayName();
            } else {
                item_name1 = info1[1];
            }

            if (Objects.requireNonNull(item2).hasItemMeta() && item2.getItemMeta().hasDisplayName()) {
                item_name2 = item2.getItemMeta().getDisplayName();
            } else {
                item_name2 = info2[1];
            }

            if (containsLessThan(playerInventory, item1)) {
                buyer.sendMessage(colorize(getPrefix() + Message.INSUFFICIENT_ITEMS.toString()
                        .replace("{ITEM}", item_name1.toLowerCase()).replace("{AMOUNT}", String.valueOf(amount1))));
                return;
            }

            if (containsLessThan(chestInventory, item2)) {
                buyer.sendMessage(colorize(getPrefix() + Message.SHOP_EMPTY.toString()
                        .replace("{ITEM}", item_name2.toLowerCase()).replace("{AMOUNT}", String.valueOf(amount2))));
                return;
            }

            if (canNotExchange(chestInventory, item2, item1)) {
                buyer.sendMessage(colorize(getPrefix() + Message.SHOP_FULL.toString()
                        .replace("{ITEM}", item_name2.toLowerCase()).replace("{AMOUNT}", String.valueOf(amount2))));
                return;
            }

            if (canNotExchange(playerInventory, item1, item2)) {
                buyer.sendMessage(colorize(getPrefix() + Message.PLAYER_FULL.toString()
                        .replace("{ITEM}", item_name1.toLowerCase()).replace("{AMOUNT}", String.valueOf(amount1))));
                return;
            }

            int count = amount1, removed;
            item1 = TradeEventListener.moveToPlayer(chestInventory, playerInventory, item1, count);

            count = amount2;
            TradeEventListener.moveToChest(chestInventory, playerInventory, item1, item2, count);
            String message = Message.ON_TRADE.toString()
                    .replace("{AMOUNT2}", String.valueOf(amount1))
                    .replace("{AMOUNT1}", String.valueOf(amount2))
                    .replace("{ITEM2}", item_name1.toLowerCase())
                    .replace("{ITEM1}", item_name2.toLowerCase())
                    .replace("{SELLER}", s.getLine(3));
            buyer.sendMessage(colorize(getPrefix() + message));
        }
    }

    private boolean isBlacklisted(PlayerInteractEvent e, ItemStack item1, ItemStack item2) {
        if (item1 == null || item2 == null) {
            failedTrade(e, Message.BUY_FAILED_SIGN);
            return true;
        } else if (isBlacklistItem(item1) || isBlacklistItem(item2)) {
            failedTrade(e, Message.ILLEGAL_ITEM);
            return true;
        }
        return false;
    }
}