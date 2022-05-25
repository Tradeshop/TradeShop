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

package org.shanerx.tradeshop.shop.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.shanerx.tradeshop.data.config.Message;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.data.config.Variable;
import org.shanerx.tradeshop.framework.events.PlayerPreTradeEvent;
import org.shanerx.tradeshop.framework.events.PlayerPrepareTradeEvent;
import org.shanerx.tradeshop.framework.events.PlayerSuccessfulTradeEvent;
import org.shanerx.tradeshop.item.ShopItemSide;
import org.shanerx.tradeshop.player.Permissions;
import org.shanerx.tradeshop.shop.ExchangeStatus;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shop.ShopType;
import org.shanerx.tradeshop.shoplocation.ShopLocation;
import org.shanerx.tradeshop.utils.Utils;
import org.shanerx.tradeshop.utils.objects.Tuple;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

        shop = PLUGIN.getDataStorage().loadShopFromSign(new ShopLocation(s.getLocation()));

        if (shop == null) {
            s.setLine(0, "");
            s.setLine(1, "");
            s.setLine(2, "");
            s.setLine(3, "");
            s.update();
            return;
        }

        if (Permissions.hasPermission(buyer, Permissions.PREVENT_TRADE) && !buyer.isOp()) {
            Message.NO_TRADE_PERMISSION.sendMessage(buyer);
            return;
        }

        if (shop.getShopType() != ShopType.BITRADE && e.getAction() == Action.LEFT_CLICK_BLOCK) {
            return;
        }

        if (!shop.getShopType().equals(ShopType.ITRADE) && shop.getUsersUUID().contains(buyer.getUniqueId()) && !Setting.ALLOW_USER_PURCHASING.getBoolean()) {
            Message.SELF_OWNED.sendMessage(buyer);
            return;
        }

        PlayerPreTradeEvent preEvent = new PlayerPreTradeEvent(e.getPlayer(), shop.getSideList(ShopItemSide.COST), shop.getSideList(ShopItemSide.PRODUCT), shop, e.getClickedBlock(), e.getBlockFace());
        Bukkit.getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) return;

        boolean doBiTradeAlternate = shop.getShopType() == ShopType.BITRADE && e.getAction() == Action.LEFT_CLICK_BLOCK;

        chestState = shop.getStorage();
        if (!shop.getShopType().equals(ShopType.ITRADE) && chestState == null) {
            Message.MISSING_CHEST.sendMessage(buyer);
            shop.updateStatus();
            return;
        }

        if (!(shop.isSideValid(ShopItemSide.PRODUCT) && shop.isSideValid(ShopItemSide.COST))) {
            Message.ILLEGAL_ITEM.sendMessage(buyer);
            return;
        }

        int multiplier = 1;

        if (buyer.isSneaking() && Setting.ALLOW_MULTI_TRADE.getBoolean()) {
            multiplier = PLUGIN.getDataStorage().loadPlayer(buyer.getUniqueId()).getMulti();
        }

        switch (shop.getStatus()) {
            case CLOSED:
                Message.SHOP_CLOSED.sendMessage(buyer);
                return;
            case INCOMPLETE:
                Message.SHOP_EMPTY.sendMessage(buyer);
            case OUT_OF_STOCK:
                if (shop.getShopType() == ShopType.ITRADE) {
                    break;
                }

                List<ItemStack> searchResult = getItems(shop.getChestAsSC().getInventory().getStorageContents(), shop.getSideList(ShopItemSide.PRODUCT, doBiTradeAlternate), multiplier);
                Message.SHOP_INSUFFICIENT_ITEMS.sendItemMultiLineMessage(buyer, Collections.singletonMap(Variable.MISSING_ITEMS, searchResult));
                return;
            case OPEN:
                break;
        }

        PlayerPrepareTradeEvent event = new PlayerPrepareTradeEvent(e.getPlayer(), shop.getSideList(ShopItemSide.COST), shop.getSideList(ShopItemSide.PRODUCT), shop, e.getClickedBlock(), e.getBlockFace());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        e.setCancelled(true);
        shop = PLUGIN.getDataStorage().loadShopFromSign(new ShopLocation(s.getLocation()));

        Tuple<ExchangeStatus, List<ItemStack>> canExchangeResult = canExchangeAll(shop, buyer.getInventory(), multiplier, e.getAction());

        switch (canExchangeResult.getLeft()) {
            case SHOP_NO_PRODUCT:
                Message.SHOP_INSUFFICIENT_ITEMS.sendItemMultiLineMessage(buyer, Collections.singletonMap(Variable.MISSING_ITEMS, canExchangeResult.getRight()));
                return;
            case PLAYER_NO_COST:
                Message.INSUFFICIENT_ITEMS.sendItemMultiLineMessage(buyer, Collections.singletonMap(Variable.MISSING_ITEMS, canExchangeResult.getRight()));
                return;
            case SHOP_NO_SPACE:
                Message.SHOP_FULL.sendMessage(buyer);
                return;
            case PLAYER_NO_SPACE:
                Message.PLAYER_FULL.sendMessage(buyer);
                return;
            case NOT_TRADE:
                e.setCancelled(false);
                return;
        }

        String owner = shop.getOwner().getName();

        if (owner == null)
            owner = "-Unknown-";

        Tuple<List<ItemStack>, List<ItemStack>> tradeReturn = tradeAllItems(shop, multiplier, e, buyer);
        Map<Variable, List<ItemStack>> tradedItems = new HashMap<>();

        tradedItems.put(Variable.GIVEN_LINES, tradeReturn.getRight());
        tradedItems.put(Variable.RECEIVED_LINES, tradeReturn.getLeft());

        if (tradeReturn.getLeft().get(0) != null && tradeReturn.getRight().get(0) != null) {
            Message.ON_TRADE.sendItemMultiLineMessage(buyer, tradedItems,
                    new Tuple<>(Variable.SELLER.toString(), shop.getShopType().isITrade() ? Setting.ITRADESHOP_OWNER.getString() : owner));
        } else {
            Message.FAILED_TRADE.sendMessage(buyer);
        }

        shop.updateSign();
        shop.saveShop();
    }

    private Tuple<List<ItemStack>, List<ItemStack>> tradeAllItems(Shop shop, int multiplier, PlayerInteractEvent event, Player buyer) {
        Action action = event.getAction();
        List<ItemStack> costItems = createBadList(), productItems = createBadList(); // Start with Bad lists so that in the event of failure at least one list has to have null at 0
        Inventory shopInventory = shop.hasStorage() ? shop.getChestAsSC().getInventory() : null;
        Inventory playerInventory = buyer.getInventory();

        if (shop.getShopType() == ShopType.ITRADE && action.equals(Action.RIGHT_CLICK_BLOCK)) { //ITrade trade

            if (shop.hasSide(ShopItemSide.COST)) {
                costItems = getItems(playerInventory.getStorageContents(), shop.getSideList(ShopItemSide.COST), multiplier);
                if (costItems.get(0) == null) {
                    ItemStack item = costItems.get(1);
                    Message.INSUFFICIENT_ITEMS.sendItemMultiLineMessage(buyer, Collections.singletonMap(Variable.MISSING_ITEMS, costItems));
                    return new Tuple<>(productItems, costItems);
                }

                for (ItemStack item : costItems) {
                    playerInventory.removeItem(item);
                }
            } else {
                costItems.clear();

                //Create Free item and set amount/name
                ItemStack noCostITradeItem = new ItemStack(Material.STICK, Setting.ITRADESHOP_NO_COST_AMOUNT.getInt());
                ItemMeta noCostITradeMeta = noCostITradeItem.getItemMeta();
                noCostITradeMeta.setDisplayName(Setting.ITRADESHOP_NO_COST_TEXT.getString());
                noCostITradeItem.setItemMeta(noCostITradeMeta);

                costItems.add(noCostITradeItem);
            }

            Inventory iTradeVirtualInventory = Bukkit.createInventory(null, Math.min((int) (Math.ceil(shop.getSideList(ShopItemSide.PRODUCT).size() / 9.0) * 9) * multiplier, 54));
            while (iTradeVirtualInventory.firstEmpty() != -1) {
                for (ItemStack item : shop.getSideItemStacks(ShopItemSide.PRODUCT)) {
                    item.setAmount(item.getMaxStackSize());
                    iTradeVirtualInventory.addItem(item);
                }
            }

            productItems = getItems(iTradeVirtualInventory.getStorageContents(), shop.getSideList(ShopItemSide.PRODUCT), multiplier);

            for (ItemStack item : productItems) {
                playerInventory.addItem(item);
            }

            Bukkit.getPluginManager().callEvent(new PlayerSuccessfulTradeEvent(buyer, costItems, productItems, shop, event.getClickedBlock(), event.getBlockFace()));
            PLUGIN.getMetricsManager().addTrade();
            return new Tuple<>(productItems, costItems); //Successfully completed trade
        } else if (shop.getShopType() == ShopType.BITRADE && action == Action.LEFT_CLICK_BLOCK) { //BiTrade Reversed Trade

            //Method to find Cost items in player inventory and add to cost array
            costItems = getItems(playerInventory.getStorageContents(), shop.getSideList(ShopItemSide.PRODUCT), multiplier); //Reverse BiTrade, Product is Cost
            if (costItems.get(0) == null) {
                ItemStack item = costItems.get(1);
                Message.INSUFFICIENT_ITEMS.sendItemMultiLineMessage(buyer, Collections.singletonMap(Variable.MISSING_ITEMS, costItems));
                return new Tuple<>(productItems, costItems);
            }

            //Method to find Product items in shop inventory and add to product array
            productItems = getItems(shopInventory.getStorageContents(), shop.getSideList(ShopItemSide.COST), multiplier); //Reverse BiTrade, Cost is Product
            if (productItems.get(0) == null) {
                ItemStack item = productItems.get(1);
                shop.updateStatus();
                Message.SHOP_INSUFFICIENT_ITEMS.sendItemMultiLineMessage(buyer, Collections.singletonMap(Variable.MISSING_ITEMS, productItems));
                return new Tuple<>(productItems, costItems);
            }
        } else if (action.equals(Action.RIGHT_CLICK_BLOCK)) { // Normal Trade

            //Method to find Cost items in player inventory and add to cost array
            costItems = getItems(playerInventory.getStorageContents(), shop.getSideList(ShopItemSide.COST), multiplier);
            if (costItems.get(0) == null) {
                ItemStack item = costItems.get(1);
                Message.INSUFFICIENT_ITEMS.sendItemMultiLineMessage(buyer, Collections.singletonMap(Variable.MISSING_ITEMS, costItems));
                return new Tuple<>(productItems, costItems);
            }

            //Method to find Product items in shop inventory and add to product array
            productItems = getItems(shopInventory.getStorageContents(), shop.getSideList(ShopItemSide.PRODUCT), multiplier);
            if (productItems.get(0) == null) {
                ItemStack item = productItems.get(1);
                shop.updateStatus();
                Message.SHOP_INSUFFICIENT_ITEMS.sendItemMultiLineMessage(buyer, Collections.singletonMap(Variable.MISSING_ITEMS, productItems));
                return new Tuple<>(productItems, costItems);
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

            Bukkit.getPluginManager().callEvent(new PlayerSuccessfulTradeEvent(buyer, costItems, productItems, shop, event.getClickedBlock(), event.getBlockFace()));
            PLUGIN.getMetricsManager().addTrade();
            return new Tuple<>(productItems, costItems); //Successfully completed trade
        } else {
            return new Tuple<>(productItems, costItems);
        }
    }
}