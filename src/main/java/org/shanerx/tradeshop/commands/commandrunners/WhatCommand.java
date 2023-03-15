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

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.commands.CommandPass;
import org.shanerx.tradeshop.data.config.Message;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.item.ShopItemSide;
import org.shanerx.tradeshop.item.ShopItemStack;
import org.shanerx.tradeshop.player.Permissions;
import org.shanerx.tradeshop.player.ShopUser;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shop.ShopType;

/**
 * Implementation of GUICommand for the `what` command
 *
 * @since 2.3.0
 */
public class WhatCommand extends GUICommand {

    public WhatCommand(TradeShop instance, CommandPass command) {
        super(instance, command);
    }

    /**
     * Opens a GUI containing the items to be traded at the shop the player is looking at
     */
    public void what() {
        /* Dumb test code TODO: Remove before 2.6.0 release(or not it's not like it affects anything)
        ItemStack src = command.getPlayerSender().getInventory().getItemInMainHand();

        final Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .create();

        final Debug debug = Debug.findDebugger();

        JsonElement srcSer = gson.toJsonTree(src.serialize());
        ConfigurationSerializable obj = ConfigurationSerialization.deserializeObject(gson.fromJson(srcSer, new TypeToken<ItemStack>(){}.getType()));

        debug.log("CSA > S > src: " + src.serialize(), DebugLevels.GSON);
        debug.log("CSA > S > Gson(src): " + srcSer, DebugLevels.GSON);
        debug.log("CSA > S > deSer(src.Ser): " + obj.serialize(), DebugLevels.GSON);
        debug.log("CSA > S > GSON(deSer(src.Ser)): " + gson.toJson(obj), DebugLevels.GSON);

         */

        Shop shop = ShopUser.findObservedShop(command.getPlayerSender());

        if (shop == null)
            return;

        shop.updateFullTradeCount();
        shop.updateSign();

        if (!Permissions.hasPermission(command.getPlayerSender(), Permissions.INFO)) {
            command.sendMessage(Message.NO_COMMAND_PERMISSION.getPrefixed());
            return;
        }

        InventoryGui gui = new InventoryGui(plugin, colorize(shop.getShopType() == ShopType.ITRADE ?
                Setting.ITRADESHOP_OWNER.getString() :
                Bukkit.getOfflinePlayer(shop.getOwner().getUUID()).getName() + "'s Shop"),
                WHAT_MENU);

        GuiElementGroup costGroup = new GuiElementGroup('q'), productGroup = new GuiElementGroup('e');

        gui.setFiller(new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1));

        if (costItems.isEmpty()) {
            for (ShopItemStack item : shop.getSideList(ShopItemSide.COST)) {
                costItems.add(item.clone());
            }
        }

        if (productItems.isEmpty()) {
            for (ShopItemStack item : shop.getSideList(ShopItemSide.PRODUCT)) {
                productItems.add(item.clone());
            }
        }


        for (int i = 0; i < costItems.size(); i++) {
            costGroup.addElement(itemSettingMenu(shop, i, ShopItemSide.COST, false));
        }

        for (int i = 0; i < productItems.size(); i++) {
            productGroup.addElement(itemSettingMenu(shop, i, ShopItemSide.PRODUCT, false));
        }

        gui.addElement(new StaticGuiElement('1', new ItemStack(Material.LIME_STAINED_GLASS_PANE),
                " "));
        gui.addElement(new StaticGuiElement('2', new ItemStack(Material.BLACK_STAINED_GLASS_PANE),
                " "));
        gui.addElement(new StaticGuiElement('3', new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE),
                " "));
        gui.addElement(new StaticGuiElement('4', new ItemStack(Material.GOLD_NUGGET),
                "Cost", "This is the item", "that you give to", "make the trade."));
        gui.addElement(new StaticGuiElement('5', new ItemStack(Material.GRASS_BLOCK),
                "Product", "This is the item", "that you receive", "from the trade."));
        gui.addElement(getPrevButton());
        gui.addElement(getNextButton());
        gui.addElement(costGroup);
        gui.addElement(productGroup);

        gui.show(command.getPlayerSender());

    }
}
