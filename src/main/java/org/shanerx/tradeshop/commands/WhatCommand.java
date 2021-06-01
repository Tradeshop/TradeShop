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

package org.shanerx.tradeshop.commands;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.enumys.Setting;
import org.shanerx.tradeshop.enumys.ShopItemStackSettingKeys;
import org.shanerx.tradeshop.enumys.ShopType;
import org.shanerx.tradeshop.objects.Shop;
import org.shanerx.tradeshop.objects.ShopItemStack;

public class WhatCommand extends CommandRunner {

    public WhatCommand(TradeShop instance, CommandPass command) {
        super(instance, command);
    }

    /**
     * Opens a GUI containing the items to be traded at the shop the player is looking at
     */
    public void what() {
        Shop shop = findShop();

        if (shop == null)
            return;

        InventoryGui gui = new InventoryGui(plugin, colorize(shop.getShopType() == ShopType.ITRADE ?
                Setting.ITRADESHOP_OWNER.getString() :
                Bukkit.getOfflinePlayer(shop.getOwner().getUUID()).getName() + "'s Shop"),
                WHAT_MENU);

        GuiElementGroup costGroup = new GuiElementGroup('a'), productGroup = new GuiElementGroup('b');

        gui.setFiller(new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1));

        for (ShopItemStack item : shop.getCost()) {
            costGroup.addElement(shopitemViewMenu(item));
        }

        for (ShopItemStack item : shop.getProduct()) {
            productGroup.addElement(shopitemViewMenu(item));
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
        gui.addElement(PREV_BUTTON);
        gui.addElement(NEXT_BUTTON);
        gui.addElement(costGroup);
        gui.addElement(productGroup);

        gui.show(pSender);
    }

    private ItemStack settingItem(boolean state) {
        return state ? new ItemStack(Material.EMERALD_BLOCK) : new ItemStack(Material.REDSTONE_BLOCK);
    }

    private ItemStack settingItem(int state) {
        switch (state) {
            case -1:
                return new ItemStack(Material.REDSTONE_BLOCK);
            case 0:
                return new ItemStack(Material.IRON_BLOCK);
            case 1:
            default:
                return new ItemStack(Material.EMERALD_BLOCK);
            case 2:
                return new ItemStack(Material.GOLD_BLOCK);
        }
    }

    private String stateText(int state) {
        switch (state) {
            case -1:
                return "False";
            case 0:
                return "<=";
            case 1:
            default:
                return "==";
            case 2:
                return ">=";
        }
    }

    private StaticGuiElement shopitemViewMenu(ShopItemStack item) {
        return new StaticGuiElement('e', item.getItemStack(), click2 -> {
            InventoryGui itemView = new InventoryGui(plugin, "Edit Cost Item", ITEM_LAYOUT);
            GuiElementGroup itemGroup = new GuiElementGroup('g');

            itemView.addElement(BACK_BUTTON);

            itemView.addElement(new StaticGuiElement('a', new ItemStack(Material.BLACK_STAINED_GLASS_PANE), " "));

            itemGroup.addElement(new StaticGuiElement('e', item.getItemStack()));

            itemGroup.addElement(new StaticGuiElement('e', settingItem(item.getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_NAME)), "Compare Name", "State: " + item.getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_NAME)));
            itemGroup.addElement(new StaticGuiElement('e', settingItem(item.getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_LORE)), "Compare Lore", "State: " + item.getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_LORE)));

            if (item.getItemStack().getType().toString().endsWith("SHULKER_BOX")) {
                itemGroup.addElement(new StaticGuiElement('e', settingItem(item.getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_SHULKER_INVENTORY)), "Compare Shulker Inventory", "State: " + item.getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_SHULKER_INVENTORY)));
            }

            if (item.getItemStack().getItemMeta() instanceof Damageable) {
                itemGroup.addElement(new StaticGuiElement('e', settingItem(item.getShopSettingAsInteger(ShopItemStackSettingKeys.COMPARE_DURABILITY)), "Compare Durability", "State: " + stateText(item.getShopSettingAsInteger(ShopItemStackSettingKeys.COMPARE_DURABILITY))));
            }

            itemGroup.addElement(new StaticGuiElement('e', settingItem(item.getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_ENCHANTMENTS)), "Compare Enchantments", "State: " + item.getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_ENCHANTMENTS)));

            itemGroup.addElement(new StaticGuiElement('e', settingItem(item.getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_UNBREAKABLE)), "Compare Unbreakable", "State: " + item.getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_UNBREAKABLE)));

            if (item.getItemStack().getItemMeta() instanceof BookMeta) {
                itemGroup.addElement(new StaticGuiElement('e', settingItem(item.getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_BOOK_AUTHOR)), "Compare Book Author", "State: " + item.getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_BOOK_AUTHOR)));
                itemGroup.addElement(new StaticGuiElement('e', settingItem(item.getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_BOOK_PAGES)), "Compare Book Pages", "State: " + item.getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_BOOK_PAGES)));
            }

            itemGroup.addElement(new StaticGuiElement('e', settingItem(item.getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_ITEM_FLAGS)), "Compare Item Flags", "State: " + item.getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_ITEM_FLAGS)));

            itemGroup.addElement(new StaticGuiElement('e', settingItem(item.getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_CUSTOM_MODEL_DATA)), "Compare Custom Model Data", "State: " + item.getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_CUSTOM_MODEL_DATA)));

            itemGroup.addElement(new StaticGuiElement('e', settingItem(item.getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_ATTRIBUTE_MODIFIER)), "Compare Attribute Modifier", "State: " + item.getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_ATTRIBUTE_MODIFIER)));

            itemView.addElement(itemGroup);
            itemView.show(pSender);
            return true;
        });
    }
}
