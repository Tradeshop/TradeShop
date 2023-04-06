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

package org.shanerx.tradeshop.commands.commandrunners;

import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiStateElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.commands.CommandPass;
import org.shanerx.tradeshop.data.config.Message;
import org.shanerx.tradeshop.item.ShopItemSide;
import org.shanerx.tradeshop.item.ShopItemStack;
import org.shanerx.tradeshop.player.Permissions;
import org.shanerx.tradeshop.player.ShopRole;
import org.shanerx.tradeshop.player.ShopUser;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shop.ShopSettingKeys;
import org.shanerx.tradeshop.utils.objects.ObjectHolder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of GUICommand for the `edit` command
 *
 * @since 2.3.0
 */
public class EditSubCommand extends GUISubCommand {

    private Shop shop;
    private InventoryGui mainMenu,
            userEdit,
            costEdit,
            productEdit,
            settingEdit;


    public EditSubCommand(TradeShop instance, CommandPass command) {
        super(instance, command);
    }

    /**
     * Opens a GUI allowing the player to edit the shop
     */
    public void edit() {
        shop = ShopUser.findObservedShop(command.getPlayerSender());

        if (shop == null)
            return;

        if (!(shop.getUsersUUID(ShopRole.MANAGER, ShopRole.OWNER).contains(command.getPlayerSender().getUniqueId())
                || Permissions.isAdminEnabled(command.getPlayerSender()))) {
            command.sendMessage(Message.NO_SHOP_PERMISSION.getPrefixed());
            return;
        }

        mainMenu = new InventoryGui(plugin, "Edit Menu-" + shop.getShopLocationAsSL().serialize(), MENU_LAYOUT);

        mainMenu.setFiller(new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1));

        // ShopUser edit menu, currently can only change/remove. Adding only available through commands
        mainMenu.addElement(editUserMenu('a'));

        mainMenu.addElement(editCostMenu('b'));

        mainMenu.addElement(editProductMenu('c'));

        mainMenu.addElement(editSettingsMenu('d'));

        mainMenu.show(command.getPlayerSender());
    }

    private GuiElement editSettingsMenu(char slotChar) {
        return new StaticGuiElement(slotChar, new ItemStack(Material.CRAFTING_TABLE), click -> {
            Map<ShopSettingKeys, ObjectHolder<?>> changedSettings = new HashMap<>();

            settingEdit = new InventoryGui(plugin, "Edit Shop Settings", SETTING_LAYOUT);
            GuiElementGroup viewGroup = new GuiElementGroup('g'),
                    changeGroup = new GuiElementGroup('h');

            shop.getShopSettings().forEach((key, value) -> {
                viewGroup.addElement(new StaticGuiElement('e', key.getDisplayItem(), key.makeReadable(), value.toString()));

                ObjectHolder<?> state = shop.getShopSetting(key);
                if (!(state == null || state.getObject() == null)) {
                    if (state.isBoolean()) {
                        if (key.isUserEditable(shop.getShopType())) {
                            changeGroup.addElement(new GuiStateElement('e',
                                    state.asBoolean().toString(),
                                    new GuiStateElement.State(change -> {
                                        if (!state.asBoolean())
                                            changedSettings.put(key, new ObjectHolder<>(true));
                                        else
                                            changedSettings.remove(key);
                                    },
                                            "true",
                                            getBooleanItem(true),
                                            key.makeReadable(),
                                            "State: True"
                                    ),
                                    new GuiStateElement.State(change -> {
                                        if (state.asBoolean())
                                            changedSettings.put(key, new ObjectHolder<>(false));
                                        else
                                            changedSettings.remove(key);
                                    },
                                            "false",
                                            getBooleanItem(false),
                                            key.makeReadable(),
                                            "State: False"
                                    )));
                        } else {
                            changeGroup.addElement(new StaticGuiElement('e', getBooleanItem(state.asBoolean()), key.makeReadable(), value.toString()));
                        }


                    } // Add else-if later if other type settings are added...
                }
            });

            // Add Cancel Button - Cancels changes and Goes to previous screen
            settingEdit.addElement(getBackButton(true));

            // Add Save Button - Saves and Goes to previous menu
            settingEdit.addElement(new StaticGuiElement('s', new ItemStack(Material.ANVIL), click3 -> {
                shop.setShopSettings(changedSettings);
                shop.saveShop(changedSettings.size() > 0);
                InventoryGui.goBack(command.getPlayerSender());
                return true;
            }, "Save Changes"));

            settingEdit.addElement(new StaticGuiElement('a', new ItemStack(Material.YELLOW_STAINED_GLASS_PANE), " "));

            settingEdit.addElements(getNextButton(), getPrevButton(), viewGroup, changeGroup);

            settingEdit.show(command.getPlayerSender());
            return true;
        }, colorize("&eEdit Shop Settings"));
    }

    private GuiElement editUserMenu(char slotChar) {
        return new StaticGuiElement(slotChar, new ItemStack(Material.PLAYER_HEAD), click -> {
            userEdit = new InventoryGui(plugin, "Edit Users", EDIT_LAYOUT);
            Set<ShopUser> shopUsers = new HashSet<>();
            GuiElementGroup userGroup = new GuiElementGroup('g');

            // Previous page
            userEdit.addElement(getPrevButton());

            // Next page
            userEdit.addElement(getNextButton());

            // Cancel and Back
            userEdit.addElement(getBackButton(true));

            userEdit.addElement(new StaticGuiElement('a', new ItemStack(Material.BLUE_STAINED_GLASS_PANE), " "));

            // Owner added separately as it is not editable
            userGroup.addElement(new StaticGuiElement('e', shop.getOwner().getHead(), shop.getOwner().getName(), "Position: " + shop.getOwner().getRole().toString()));

            if (shop.getOwner().getUUID().equals(command.getPlayerSender().getUniqueId())) {

                // Save and Back
                userEdit.addElement(new StaticGuiElement('s', new ItemStack(Material.ANVIL), click3 -> {
                    shop.updateShopUsers(shopUsers);
                    InventoryGui.goBack(command.getPlayerSender());
                    return true;
                }, "Save Changes"));

                for (ShopUser user : shop.getUsers(ShopRole.MANAGER, ShopRole.MEMBER)) {
                    shopUsers.add(user);
                    userGroup.addElement(new GuiStateElement('e',
                            user.getRole().toString(),
                            new GuiStateElement.State(change -> {
                                shopUsers.remove(user);
                                user.setRole(ShopRole.MANAGER);
                                shopUsers.add(user);
                            },
                                    "MANAGER",
                                    user.getHead(),
                                    user.getName(),
                                    "Position: MANAGER",
                                    "Click to cycle the player to Member."),
                            new GuiStateElement.State(change -> {
                                shopUsers.remove(user);
                                user.setRole(ShopRole.MEMBER);
                                shopUsers.add(user);
                            },
                                    "MEMBER",
                                    user.getHead(),
                                    user.getName(),
                                    "Position: MEMBER",
                                    "Click to cycle the player to Removed."),
                            new GuiStateElement.State(change -> {
                                shopUsers.remove(user);
                                user.setRole(ShopRole.SHOPPER);
                                shopUsers.add(user);
                            },
                                    "SHOPPER",
                                    new ItemStack(Material.BARRIER),
                                    user.getName(),
                                    "Position: NONE",
                                    "Click to cycle the player to Manager.")
                    ));

                }
            } else {
                for (ShopUser user : shop.getUsers(ShopRole.MANAGER, ShopRole.MEMBER)) {
                    userGroup.addElement(new StaticGuiElement('e', user.getHead(), user.getName(), "Position: " + user.getRole().toString()));
                }
            }
            userEdit.addElement(userGroup);
            userEdit.show(command.getPlayerSender());
            return true;
        }, colorize("&eEdit Shop Users"));
    }

    private GuiElement editCostMenu(char slotChar) {
        return new StaticGuiElement(slotChar, new ItemStack(Material.GOLD_NUGGET), click -> {
            costEdit = new InventoryGui(plugin, "Edit Costs", EDIT_LAYOUT);
            if (costItems.isEmpty()) {
                for (ShopItemStack item : shop.getSideList(ShopItemSide.COST)) {
                    costItems.add(item.clone());
                    costItemsRemoval.add(false);
                }
            }
            GuiElementGroup costGroup = new GuiElementGroup('g');

            // Previous page
            costEdit.addElement(getPrevButton());

            // Next page
            costEdit.addElement(getNextButton());

            // Cancel and Back
            costEdit.addElement(getBackButton(true));

            // Save and Back
            costEdit.addElement(new StaticGuiElement('s', new ItemStack(Material.ANVIL), click3 -> {
                for (int i = costItems.size() - 1; i >= 0; i--) {
                    if (costItemsRemoval.get(i))
                        costItems.remove(i);
                }
                shop.updateSide(ShopItemSide.COST, costItems);
                shop.saveShop();
                InventoryGui.goBack(command.getPlayerSender());
                return true;
            }, "Save Changes"));

            costEdit.addElement(new StaticGuiElement('a', new ItemStack(Material.YELLOW_STAINED_GLASS_PANE), " "));

            for (int i = 0; i < costItems.size(); i++) {
                costGroup.addElement(itemSettingMenu(shop, i, ShopItemSide.COST, true));
            }

            costEdit.addElement(costGroup);
            costEdit.show(command.getPlayerSender());
            return true;
        }, colorize("&eEdit Shop Costs"));
    }

    private GuiElement editProductMenu(char slotChar) {
        return new StaticGuiElement(slotChar, new ItemStack(Material.GRASS_BLOCK), click -> {
            productEdit = new InventoryGui(plugin, "Edit Products", EDIT_LAYOUT);
            if (productItems.isEmpty()) {
                for (ShopItemStack item : shop.getSideList(ShopItemSide.PRODUCT)) {
                    productItems.add(item.clone());
                    productItemsRemoval.add(false);
                }
            }
            GuiElementGroup productGroup = new GuiElementGroup('g');

            // Previous page
            productEdit.addElement(getPrevButton());

            // Next page
            productEdit.addElement(getNextButton());

            // Cancel and Back
            productEdit.addElement(getBackButton(true));

            // Save and Back
            productEdit.addElement(new StaticGuiElement('s', new ItemStack(Material.ANVIL), click3 -> {
                for (int i = productItems.size() - 1; i >= 0; i--) {
                    if (productItemsRemoval.get(i))
                        productItems.remove(i);
                }
                shop.updateSide(ShopItemSide.PRODUCT, productItems);
                shop.saveShop();
                InventoryGui.goBack(command.getPlayerSender());
                return true;
            }, "Save Changes"));

            productEdit.addElement(new StaticGuiElement('a', new ItemStack(Material.YELLOW_STAINED_GLASS_PANE), " "));

            for (int i = 0; i < productItems.size(); i++) {
                productGroup.addElement(itemSettingMenu(shop, i, ShopItemSide.PRODUCT, true));
            }

            productEdit.addElement(productGroup);
            productEdit.show(command.getPlayerSender());
            return true;
        }, colorize("&eEdit Shop Products"));
    }
}