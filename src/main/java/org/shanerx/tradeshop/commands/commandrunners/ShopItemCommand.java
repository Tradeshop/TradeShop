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

package org.shanerx.tradeshop.commands.commandrunners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.commands.CommandPass;
import org.shanerx.tradeshop.data.config.Message;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.data.config.Variable;
import org.shanerx.tradeshop.framework.ShopChange;
import org.shanerx.tradeshop.framework.events.PlayerShopChangeEvent;
import org.shanerx.tradeshop.item.ShopItemSide;
import org.shanerx.tradeshop.item.ShopItemStack;
import org.shanerx.tradeshop.player.Permissions;
import org.shanerx.tradeshop.player.ShopRole;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.utils.objects.ObjectHolder;
import org.shanerx.tradeshop.utils.objects.Tuple;

public class ShopItemCommand extends CommandRunner {

    ShopItemSide side;

    public ShopItemCommand(TradeShop instance, CommandPass command, ShopItemSide side) {
        super(instance, command);
        this.side = side;
    }

    /**
     * Lists items on a shops specified side with their index
     */
    public void listSide() {
        Shop shop = findShop();

        if (shop == null)
            return;

        StringBuilder sb = new StringBuilder();
        int counter = 1;

        for (ShopItemStack itm : shop.getSideList(side)) {
            sb.append(String.format("&b[&f%d&b]    &2- &f%s\n", counter, itm.getCleanItemName()));
            counter++;
        }

        Message.SHOP_ITEM_LIST.sendMessage(pSender, new Tuple<>(Variable.TYPE.toString(), side.toString().toLowerCase() + "s"), new Tuple<>(Variable.LIST.toString(), sb.toString()));
    }

    /**
     * Removes the item at index from the shops specified side
     */
    public void removeSide() {
        Shop shop = findShop();

        if (shop == null)
            return;

        int index = 0;

        if (isInt(command.getArgAt(1))) {
            index = Integer.parseInt(command.getArgAt(1)) - 1;
        } else {
            Message.INVALID_ARGUMENTS.sendMessage(pSender);
            return;
        }

        if (!(shop.getUsersUUID(ShopRole.MANAGER, ShopRole.OWNER).contains(pSender.getUniqueId())
                || (Setting.UNLIMITED_ADMIN.getBoolean() && Permissions.isAdminEnabled(pSender)))) {
            Message.NO_SHOP_PERMISSION.sendMessage(pSender);
            return;
        }

        PlayerShopChangeEvent changeEvent = new PlayerShopChangeEvent(pSender, shop, ShopChange.valueOf("REMOVE_" + side.toString()), new ObjectHolder<Integer>(index));
        Bukkit.getPluginManager().callEvent(changeEvent);
        if (changeEvent.isCancelled()) return;

        if (shop.removeSideIndex(side, index))
            Message.ITEM_REMOVED.sendMessage(pSender);
        else
            Message.ITEM_NOT_REMOVED.sendMessage(pSender);
    }

    /**
     * Sets the item for the specified side to a Shop
     * <p>
     * With no variables sent, this will use the amount and data of the held item
     * </p>
     * <p>
     * If the player uses a int in the first variable they can change the amount for the item they are holding
     * </p>
     * <p>
     * With 2 variables used the player can use an amount and material to set the sign instead of a held item
     * </p>
     */
    public void setSide() {
        Shop shop = findShop();

        if (shop == null)
            return;

        int amount = 0;
        Material mat = null;

        if (command.hasArgAt(1) && isInt(command.getArgAt(1))) {
            amount = Integer.parseInt(command.getArgAt(1));
        }

        if (command.hasArgAt(2) && Material.getMaterial(command.getArgAt(2).toUpperCase()) != null) {
            mat = Material.getMaterial(command.getArgAt(2).toUpperCase());
        }

        if (!(shop.getUsersUUID(ShopRole.MANAGER, ShopRole.OWNER).contains(pSender.getUniqueId())
                || (Setting.UNLIMITED_ADMIN.getBoolean() && Permissions.isAdminEnabled(pSender)))) {
            Message.NO_SHOP_PERMISSION.sendMessage(pSender);
            return;
        }

        ItemStack itemInHand;

        if (mat == null) {
            itemInHand = pSender.getInventory().getItemInMainHand().clone();
        } else {
            itemInHand = new ItemStack(mat, 1);
        }

        if (itemInHand.getType() == Material.AIR) {
            Message.HELD_EMPTY.sendMessage(pSender);
            return;
        }

        if (isIllegal(side, itemInHand.getType())) {
            Message.ILLEGAL_ITEM.sendMessage(pSender);
            return;
        }

        if (!(shop.getShopType().isITrade() && shop.getInventoryLocation() == null) && itemInHand.getType().toString().endsWith("SHULKER_BOX") && shop.getInventoryLocation().getBlock().getType().toString().endsWith("SHULKER_BOX")) {
            Message.NO_SHULKER_ITEM.sendMessage(pSender);
            return;
        }

        if (amount > 0) {
            itemInHand.setAmount(amount);
        }

        if (Math.ceil((double) itemInHand.getAmount() / (double) itemInHand.getMaxStackSize()) > Setting.MAX_ITEMS_PER_TRADE_SIDE.getInt()) {
            Message.TOO_MANY_ITEMS.sendMessage(pSender, new Tuple<>(Variable.SIDE.toString(), side.toString().toLowerCase()));
            return;
        }

        PlayerShopChangeEvent changeEvent = new PlayerShopChangeEvent(pSender, shop, ShopChange.valueOf("SET_" + side.toString()), new ObjectHolder<ItemStack>(itemInHand));
        Bukkit.getPluginManager().callEvent(changeEvent);
        if (changeEvent.isCancelled()) return;

        shop.setSideItems(side, itemInHand);

        Message.ITEM_ADDED.sendMessage(pSender);
    }

    /**
     * Adds a item to the specified side of a Shop
     * <p>
     * With no variables sent will use the amount and data of the held item
     * </p>
     * <p>
     * If the player uses a int in the first variable they can change the amount for the item they are holding
     * </p>
     * <p>
     * With 2 variables used the player can use an amount and material to set the sign instead of a held item
     * </p>
     */
    public void addSide() {
        Shop shop = findShop();

        if (shop == null)
            return;

        int amount = 0;
        Material mat = null;

        if (command.hasArgAt(1) && isInt(command.getArgAt(1))) {
            amount = Integer.parseInt(command.getArgAt(1));
        }

        if (command.hasArgAt(2) && Material.getMaterial(command.getArgAt(2).toUpperCase()) != null) {
            mat = Material.getMaterial(command.getArgAt(2).toUpperCase());
        }

        if (!(shop.getUsersUUID(ShopRole.MANAGER, ShopRole.OWNER).contains(pSender.getUniqueId())
                || (Setting.UNLIMITED_ADMIN.getBoolean() && Permissions.isAdminEnabled(pSender)))) {
            Message.NO_SHOP_PERMISSION.sendMessage(pSender);
            return;
        }

        ItemStack itemInHand;

        if (mat == null) {
            itemInHand = pSender.getInventory().getItemInMainHand().clone();
        } else {
            itemInHand = new ItemStack(mat, 1);
        }

        if (itemInHand.getType() == Material.AIR) {
            Message.HELD_EMPTY.sendMessage(pSender);
            return;
        }

        if (isIllegal(side, itemInHand.getType())) {
            Message.ILLEGAL_ITEM.sendMessage(pSender);
            return;
        }

        if (!(shop.getShopType().isITrade() && shop.getInventoryLocation() == null) && itemInHand.getType().toString().endsWith("SHULKER_BOX") && shop.getInventoryLocation().getBlock().getType().toString().endsWith("SHULKER_BOX")) {
            Message.NO_SHULKER_ITEM.sendMessage(pSender);
            return;
        }

        if (amount > 0) {
            itemInHand.setAmount(amount);
        }

        if (shop.getSideList(side).size() + Math.ceil((double) itemInHand.getAmount() / (double) itemInHand.getMaxStackSize()) > Setting.MAX_ITEMS_PER_TRADE_SIDE.getInt()) {
            Message.TOO_MANY_ITEMS.sendMessage(pSender, new Tuple<>(Variable.SIDE.toString(), side.toString().toLowerCase()));
            return;
        }

        PlayerShopChangeEvent changeEvent = new PlayerShopChangeEvent(pSender, shop, ShopChange.valueOf("ADD_" + side.toString()), new ObjectHolder<ItemStack>(itemInHand));
        Bukkit.getPluginManager().callEvent(changeEvent);
        if (changeEvent.isCancelled()) return;

        shop.addSideItem(side, itemInHand);

        Message.ITEM_ADDED.sendMessage(pSender);
    }
}
