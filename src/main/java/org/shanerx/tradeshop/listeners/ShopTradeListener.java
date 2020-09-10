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

package org.shanerx.tradeshop.listeners;

import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.enumys.Message;
import org.shanerx.tradeshop.enumys.Setting;
import org.shanerx.tradeshop.enumys.ShopType;
import org.shanerx.tradeshop.framework.events.PlayerTradeEvent;
import org.shanerx.tradeshop.framework.events.SuccessfulTradeEvent;
import org.shanerx.tradeshop.objects.Shop;
import org.shanerx.tradeshop.objects.ShopItemStack;
import org.shanerx.tradeshop.objects.ShopLocation;
import org.shanerx.tradeshop.utils.JsonConfiguration;
import org.shanerx.tradeshop.utils.Utils;

import java.util.ArrayList;
import java.util.Map;

public class ShopTradeListener extends Utils implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockInteract(PlayerInteractEvent e) {

        if (e.useInteractedBlock().equals(Event.Result.DENY) || e.isCancelled())
            return;

        Player buyer = e.getPlayer();
        Shop shop;
        Sign s;
        BlockState chestState;

        if (ShopType.isShop(e.getClickedBlock())) {
            s = (Sign) e.getClickedBlock().getState();
        } else {
            return;
        }

        JsonConfiguration json = new JsonConfiguration(s.getChunk());
        shop = json.loadShop(new ShopLocation(s.getLocation()));

        if (shop == null) {
            s.setLine(0, "");
            s.setLine(1, "");
            s.setLine(2, "");
            s.setLine(3, "");
            s.update();
            return;
        }

        if (!shop.getShopType().equals(ShopType.ITRADE) && shop.getUsersUUID().contains(buyer.getUniqueId()) && !Setting.ALLOW_USER_PURCHASING.getBoolean()) {
            buyer.sendMessage(Message.SELF_OWNED.getPrefixed());
            return;
        }

        if (!shop.isTradeable()) {
            buyer.sendMessage(Message.SHOP_CLOSED.getPrefixed());
            return;
        }

        chestState = shop.getStorage();
        if (!shop.getShopType().equals(ShopType.ITRADE) && chestState == null) {
            buyer.sendMessage(Message.MISSING_CHEST.getPrefixed());
            shop.updateStatus();
            return;
        }

        if (!(shop.areProductsValid() && shop.areCostsValid())) {
            buyer.sendMessage(Message.ILLEGAL_ITEM.getPrefixed());
            return;
        }

        String productName = "", costName = "";
        int amountCost = 0, amountProduct = 0, multiplier = 1;

        for (ShopItemStack item : shop.getCost()) { //Shop cost list
            //If item has custom name set to tempName, else set material name
            String tempName = item.getItemStack().hasItemMeta() && item.getItemStack().getItemMeta().hasDisplayName() ? item.getItemStack().getItemMeta().getDisplayName() : item.getItemStack().getType().toString();
            costName = costName.isEmpty() ? tempName : Message.VARIOUS_ITEM_TYPE.toString();
            amountCost += item.getItemStack().getAmount();
        }

        for (ShopItemStack item : shop.getProduct()) { //Shop product list
            //If item has custom name set to tempName, else set material name
            String tempName = item.getItemStack().hasItemMeta() && item.getItemStack().getItemMeta().hasDisplayName() ? item.getItemStack().getItemMeta().getDisplayName() : item.getItemStack().getType().toString();
            productName = productName.isEmpty() ? tempName : Message.VARIOUS_ITEM_TYPE.toString();
            amountProduct += item.getItemStack().getAmount();
        }

        if (buyer.isSneaking() && Setting.ALLOW_MULTI_TRADE.getBoolean()) {
            JsonConfiguration pJson = new JsonConfiguration(buyer.getUniqueId());
            Map<String, Integer> data = pJson.loadPlayer();
            multiplier = data.get("multi");

        }

        PlayerTradeEvent event = new PlayerTradeEvent(e.getPlayer(), shop.getCost(), shop.getProduct(), shop, e.getClickedBlock(), e.getBlockFace());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        e.setCancelled(true);
        shop = json.loadShop(new ShopLocation(s.getLocation()));

        switch (canExchangeAll(shop, buyer.getInventory(), multiplier, e.getAction())) {
            case SHOP_NO_PRODUCT:
                buyer.sendMessage(Message.SHOP_EMPTY.getPrefixed()
                        .replace("{ITEM}", costName.toLowerCase()).replace("{AMOUNT}", String.valueOf(amountCost)));
                return;
            case PLAYER_NO_COST:
                buyer.sendMessage(Message.INSUFFICIENT_ITEMS.getPrefixed()
                        .replace("{ITEM}", costName.toLowerCase()).replace("{AMOUNT}", String.valueOf(amountCost)));
                return;
            case SHOP_NO_SPACE:
                buyer.sendMessage(Message.SHOP_FULL.getPrefixed()
                        .replace("{ITEM}", costName.toLowerCase()).replace("{AMOUNT}", String.valueOf(amountCost)));
                return;
            case PLAYER_NO_SPACE:
                buyer.sendMessage(Message.PLAYER_FULL.getPrefixed()
                        .replace("{ITEM}", costName.toLowerCase()).replace("{AMOUNT}", String.valueOf(amountCost)));
                return;
        }

        if (tradeAllItems(shop, multiplier, e.getAction(), buyer)) {
            buyer.sendMessage(Message.ON_TRADE.getPrefixed()
                    .replace("{AMOUNT1}", String.valueOf(amountProduct))
                    .replace("{AMOUNT2}", String.valueOf(amountCost))
                    .replace("{ITEM1}", productName.toLowerCase())
                    .replace("{ITEM2}", costName.toLowerCase())
                    .replace("{SELLER}", shop.getShopType().isITrade() ? Setting.ITRADESHOP_OWNER.getString() : shop.getOwner().getPlayer().getName()));

            Bukkit.getPluginManager().callEvent(new SuccessfulTradeEvent(e.getPlayer(), shop.getCost(), shop.getProduct(), shop, e.getClickedBlock(), e.getBlockFace()));
        }

        shop.updateSign();
    }

    private boolean tradeAllItems(Shop shop, int multiplier, Action action, Player buyer) {
        ArrayList<ItemStack> costItems = new ArrayList<>(), productItems = new ArrayList<>();
        Inventory shopInventory = shop.getChestAsSC().getInventory();
        Inventory playerInventory = buyer.getInventory();

        if (shop.getShopType() == ShopType.ITRADE && action.equals(Action.RIGHT_CLICK_BLOCK)) { //ITrade trade

            //Method to find Cost items in player inventory and add to cost array
            costItems = getItems(playerInventory, shop.getCost(), multiplier);
            if (costItems.get(0) == null) {
                ItemStack item = costItems.get(1);
                buyer.sendMessage(Message.INSUFFICIENT_ITEMS.getPrefixed()
                        .replace("{ITEM}", item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().toString())
                        .replace("{AMOUNT}", String.valueOf(item.getAmount())));
                return false;
            }

            for (ItemStack item : costItems) {
                playerInventory.removeItem(item);
            }

            for (ShopItemStack item : shop.getProduct()) {
                playerInventory.addItem(item.getItemStack());
            }

            return true; //Successfully completed trade
        } else if (shop.getShopType() == ShopType.BITRADE && action == Action.LEFT_CLICK_BLOCK) { //BiTrade Reversed Trade

            //Method to find Cost items in player inventory and add to cost array
            costItems = getItems(playerInventory, shop.getProduct(), multiplier); //Reverse BiTrade, Product is Cost
            if (costItems.get(0) == null) {
                ItemStack item = costItems.get(1);
                buyer.sendMessage(Message.INSUFFICIENT_ITEMS.getPrefixed()
                        .replace("{ITEM}", item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().toString())
                        .replace("{AMOUNT}", String.valueOf(item.getAmount())));
                return false;
            }

            //Method to find Product items in shop inventory and add to product array
            productItems = getItems(shopInventory, shop.getCost(), multiplier); //Reverse BiTrade, Cost is Product
            if (productItems.get(0) == null) {
                ItemStack item = productItems.get(1);
                shop.updateStatus();
                buyer.sendMessage(Message.SHOP_INSUFFICIENT_ITEMS.getPrefixed()
                        .replace("{ITEM}", item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().toString())
                        .replace("{AMOUNT}", String.valueOf(item.getAmount())));
                return false;
            }
        } else if (action.equals(Action.RIGHT_CLICK_BLOCK)) { // Normal Trade

            //Method to find Cost items in player inventory and add to cost array
            costItems = getItems(playerInventory, shop.getCost(), multiplier);
            if (costItems.get(0) == null) {
                ItemStack item = costItems.get(1);
                buyer.sendMessage(Message.INSUFFICIENT_ITEMS.getPrefixed()
                        .replace("{ITEM}", item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().toString())
                        .replace("{AMOUNT}", String.valueOf(item.getAmount())));
                return false;
            }

            //Method to find Product items in shop inventory and add to product array
            productItems = getItems(shopInventory, shop.getProduct(), multiplier);
            if (productItems.get(0) == null) {
                ItemStack item = productItems.get(1);
                shop.updateStatus();
                buyer.sendMessage(Message.SHOP_INSUFFICIENT_ITEMS.getPrefixed()
                        .replace("{ITEM}", item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().toString())
                        .replace("{AMOUNT}", String.valueOf(item.getAmount())));
                return false;
            }

        }

        if (costItems.size() > 0) {
            //For loop to remove cost items from player inventory
            for (ItemStack item : costItems) {
                playerInventory.removeItem(item);
            }

            //For loop to remove product items from shop inventory
            for (ItemStack item : productItems) {
                shopInventory.removeItem(item);
            }

            //For loop to put cost items in shop inventory
            for (ItemStack item : costItems) {
                shopInventory.addItem(item);
            }

            //For loop to put product items in player inventory
            for (ItemStack item : productItems) {
                playerInventory.addItem(item);
            }

            return true; //Successfully completed trade
        } else {
            return false;
        }
    }
}