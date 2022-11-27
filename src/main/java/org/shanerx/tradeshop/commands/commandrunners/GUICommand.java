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

import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.GuiStateElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.commands.CommandPass;
import org.shanerx.tradeshop.item.ShopItemSide;
import org.shanerx.tradeshop.item.ShopItemStack;
import org.shanerx.tradeshop.item.ShopItemStackSettingKeys;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.utils.objects.ObjectHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of CommandRunner for plugin commands that generate GUI screens
 *
 * @since 2.6.0
 */
public class GUICommand extends CommandRunner {

    //region Util Variables
    //------------------------------------------------------------------------------------------------------------------


    protected final String[] MENU_LAYOUT = {"ad bc"},
            EDIT_LAYOUT = {"aggggggga", "ap c s na"},
            ITEM_LAYOUT = {"u ggggggg", "j hhhhhhh", "ap cbs na"},
            SETTING_LAYOUT = {"ggggggggg", "hhhhhhhhh", "ap cbs na"},
            WHAT_MENU = {"141125333", "1qqq2eee3", "11p123n33"};
    protected List<ShopItemStack> costItems = new ArrayList<>(),
            productItems = new ArrayList<>();
    protected List<Boolean> costItemsRemoval = new ArrayList<>(),
            productItemsRemoval = new ArrayList<>();


    //------------------------------------------------------------------------------------------------------------------
    //endregion

    public GUICommand(TradeShop instance, CommandPass command) {
        super(instance, command);
    }


    //region Util Methods
    //------------------------------------------------------------------------------------------------------------------

    protected StaticGuiElement itemSettingMenu(Shop shop, int index, ShopItemSide side, boolean editable) {
        ShopItemStack item = (side.equals(ShopItemSide.COST) ? costItems : productItems).get(index);
        ItemStack tempStack = item.getItemStack().clone();
        ItemMeta tempMeta = tempStack.getItemMeta();
        List<String> newLore = new ArrayList<>();
        newLore.add(colorize("&8Amount &7» &f" + item.getAmount()));

        if (tempMeta != null && tempMeta.hasLore()) {
            newLore.add("");
            newLore.addAll(tempMeta.getLore());
        }

        tempMeta.setLore(newLore);
        tempStack.setItemMeta(tempMeta);

        return new StaticGuiElement('e', tempStack, click2 -> {
            InventoryGui itemEdit = new InventoryGui(plugin, (editable ? "Edit " : "View ") + ((side.equals(ShopItemSide.COST) ? "Cost Item" : "Product Item")), ITEM_LAYOUT);
            GuiElementGroup itemGroup = new GuiElementGroup('g'),
                    settingGroup = new GuiElementGroup('h');

            // Add Cancel button when editable and Back button when not - Goes to previous screen without saving changes
            itemEdit.addElement(getBackButton(editable));

            // Add Save button only when editable - Saves and Goes to previous screen
            if (editable) itemEdit.addElement(new StaticGuiElement('s', new ItemStack(Material.ANVIL), click3 -> {
                shop.updateSideItem(side, item, index);
                InventoryGui.goBack(pSender);
                return true;
            }, "Save Changes"));

            itemEdit.addElement(new StaticGuiElement('a', new ItemStack(Material.YELLOW_STAINED_GLASS_PANE), " "));
            itemEdit.addElement(getNextButton());
            itemEdit.addElement(getPrevButton());

            itemEdit.addElement(new StaticGuiElement('u', item.getItemStack()));
            if (editable) {
                itemEdit.addElement(new GuiStateElement('j',
                        (side.equals(ShopItemSide.COST) ? costItemsRemoval : productItemsRemoval).get(index) + "",
                        new GuiStateElement.State(change -> (side.equals(ShopItemSide.COST) ? costItemsRemoval : productItemsRemoval).set(index, true),
                                "true",
                                getBooleanItem(false),
                                "Item Removed",
                                "Click to RE-ADD!"),
                        new GuiStateElement.State(change -> (side.equals(ShopItemSide.COST) ? costItemsRemoval : productItemsRemoval).set(index, false),
                                "false",
                                getBooleanItem(true),
                                "Item Valid",
                                "Click to REMOVE!")

                ));
            } else {
                itemEdit.addElement(new StaticGuiElement('j', getBooleanItem(true), "Item Valid", " "));
            }


            //Add new item settings below
            if (item.getItemStack().getItemMeta() instanceof Damageable) {
                itemGroup.addElement(settingDisplayItem(ShopItemStackSettingKeys.COMPARE_DURABILITY, item));
                settingGroup.addElement(numericalOption(ShopItemStackSettingKeys.COMPARE_DURABILITY, item, editable));
            }

            if (tempMeta instanceof BookMeta) {
                itemGroup.addElement(settingDisplayItem(ShopItemStackSettingKeys.COMPARE_BOOK_AUTHOR, item));
                settingGroup.addElement(booleanOption(ShopItemStackSettingKeys.COMPARE_BOOK_AUTHOR, item, editable));
                itemGroup.addElement(settingDisplayItem(ShopItemStackSettingKeys.COMPARE_BOOK_PAGES, item));
                settingGroup.addElement(booleanOption(ShopItemStackSettingKeys.COMPARE_BOOK_PAGES, item, editable));
            }

            if (tempMeta instanceof FireworkMeta) {
                itemGroup.addElement(settingDisplayItem(ShopItemStackSettingKeys.COMPARE_FIREWORK_DURATION, item));
                settingGroup.addElement(booleanOption(ShopItemStackSettingKeys.COMPARE_FIREWORK_DURATION, item, editable));
                itemGroup.addElement(settingDisplayItem(ShopItemStackSettingKeys.COMPARE_FIREWORK_EFFECTS, item));
                settingGroup.addElement(booleanOption(ShopItemStackSettingKeys.COMPARE_FIREWORK_EFFECTS, item, editable));
            }

            if (tempStack.getType().toString().endsWith("SHULKER_BOX")) {
                itemGroup.addElement(settingDisplayItem(ShopItemStackSettingKeys.COMPARE_SHULKER_INVENTORY, item));
                settingGroup.addElement(booleanOption(ShopItemStackSettingKeys.COMPARE_SHULKER_INVENTORY, item, editable));
            }

            if (plugin.getVersion().isAtLeast(1, 17) && tempStack.getType().equals(Material.BUNDLE)) {
                itemGroup.addElement(settingDisplayItem(ShopItemStackSettingKeys.COMPARE_BUNDLE_INVENTORY, item));
                settingGroup.addElement(booleanOption(ShopItemStackSettingKeys.COMPARE_BUNDLE_INVENTORY, item, editable));
            }

            itemGroup.addElement(settingDisplayItem(ShopItemStackSettingKeys.COMPARE_NAME, item));
            settingGroup.addElement(booleanOption(ShopItemStackSettingKeys.COMPARE_NAME, item, editable));

            itemGroup.addElement(settingDisplayItem(ShopItemStackSettingKeys.COMPARE_LORE, item));
            settingGroup.addElement(booleanOption(ShopItemStackSettingKeys.COMPARE_LORE, item, editable));

            itemGroup.addElement(settingDisplayItem(ShopItemStackSettingKeys.COMPARE_UNBREAKABLE, item));
            settingGroup.addElement(booleanOption(ShopItemStackSettingKeys.COMPARE_UNBREAKABLE, item, editable));

            itemGroup.addElement(settingDisplayItem(ShopItemStackSettingKeys.COMPARE_ENCHANTMENTS, item));
            settingGroup.addElement(booleanOption(ShopItemStackSettingKeys.COMPARE_ENCHANTMENTS, item, editable));

            itemGroup.addElement(settingDisplayItem(ShopItemStackSettingKeys.COMPARE_ITEM_FLAGS, item));
            settingGroup.addElement(booleanOption(ShopItemStackSettingKeys.COMPARE_ITEM_FLAGS, item, editable));

            itemGroup.addElement(settingDisplayItem(ShopItemStackSettingKeys.COMPARE_CUSTOM_MODEL_DATA, item));
            settingGroup.addElement(booleanOption(ShopItemStackSettingKeys.COMPARE_CUSTOM_MODEL_DATA, item, editable));

            itemGroup.addElement(settingDisplayItem(ShopItemStackSettingKeys.COMPARE_ATTRIBUTE_MODIFIER, item));
            settingGroup.addElement(booleanOption(ShopItemStackSettingKeys.COMPARE_ATTRIBUTE_MODIFIER, item, editable));


            itemEdit.addElement(itemGroup);
            itemEdit.addElement(settingGroup);
            itemEdit.show(pSender);
            return true;
        });
    }

    private GuiElement numericalOption(ShopItemStackSettingKeys setting, ShopItemStack item, boolean isScreenEditable) {
        ItemStack[] indexedTempItem = new ItemStack[]{getBooleanItem(false), new ItemStack(Material.IRON_BLOCK), getBooleanItem(true), new ItemStack(Material.GOLD_BLOCK)};

        if (setting.isUserEditable() && isScreenEditable) {
            return new GuiStateElement('e',
                    String.valueOf(item.getShopSettingAsInteger(setting)),
                    new GuiStateElement.State(change -> {
                        item.setShopSettings(setting, new ObjectHolder<>(2));
                    },
                            "2",
                            indexedTempItem[3],
                            setting.makeReadable(),
                            item.getStateString(new ObjectHolder<>(2))),

                    new GuiStateElement.State(change -> {
                        item.setShopSettings(setting, new ObjectHolder<>(-1));
                    },
                            "-1",
                            indexedTempItem[0],
                            setting.makeReadable(),
                            item.getStateString(new ObjectHolder<>(-1))),

                    new GuiStateElement.State(change -> {
                        item.setShopSettings(setting, new ObjectHolder<>(0));
                    },
                            "0",
                            indexedTempItem[1],
                            setting.makeReadable(),
                            item.getStateString(new ObjectHolder<>(0))),

                    new GuiStateElement.State(change -> {
                        item.setShopSettings(setting, new ObjectHolder<>(1));
                    },
                            "1",
                            indexedTempItem[2],
                            setting.makeReadable(),
                            item.getStateString(new ObjectHolder<>(1))

                    ));
        }

        return new StaticGuiElement('e', indexedTempItem[item.getShopSettingAsInteger(setting) + 1], setting.makeReadable(), item.getStateString(setting));
    }

    private GuiElement booleanOption(ShopItemStackSettingKeys setting, ShopItemStack item, boolean editable) {
        if (setting.isUserEditable() && editable) {
            return new GuiStateElement('e',
                    String.valueOf(item.getShopSettingAsBoolean(setting)),
                    new GuiStateElement.State(change -> {
                        item.setShopSettings(setting, new ObjectHolder<>(true));
                    },
                            "true",
                            getBooleanItem(true),
                            setting.makeReadable(),
                            item.getStateString(new ObjectHolder<>(true))
                    ),
                    new GuiStateElement.State(change -> {
                        item.setShopSettings(setting, new ObjectHolder<>(false));
                    },
                            "false",
                            getBooleanItem(false),
                            setting.makeReadable(),
                            item.getStateString(new ObjectHolder<>(false))
                    ));
        }

        return new StaticGuiElement('e', getBooleanItem(item.getShopSettingAsBoolean(setting)), setting.makeReadable(), item.getStateString(setting));
    }

    private StaticGuiElement settingDisplayItem(ShopItemStackSettingKeys setting, ShopItemStack tempItem) {
        return new StaticGuiElement('e', setting.getDisplayItem(), setting.makeReadable(), tempItem.getStateString(setting));
    }

    protected GuiElement getPrevButton() {
        return new GuiPageElement('p', new ItemStack(Material.POTION), GuiPageElement.PageAction.PREVIOUS, "Go to previous page (%prevpage%)", " ");
    }

    protected GuiElement getNextButton() {
        return new GuiPageElement('n', new ItemStack(Material.SPLASH_POTION), GuiPageElement.PageAction.NEXT, "Go to next page (%nextpage%)", " ");
    }

    protected GuiElement getBackButton(boolean asCancel) {
        return new StaticGuiElement(asCancel ? 'c' : 'b', new ItemStack(Material.END_CRYSTAL), click3 -> {
            InventoryGui.goBack(pSender);
            return true;
        }, asCancel ? "Cancel Changes" : "Back");
    }

    protected ItemStack getBooleanItem(boolean item) {
        return item ? new ItemStack(Material.EMERALD_BLOCK) : new ItemStack(Material.REDSTONE_BLOCK);
    }

    //------------------------------------------------------------------------------------------------------------------
    //endregion
}
