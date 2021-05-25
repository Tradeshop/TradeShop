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

import com.google.common.collect.Iterables;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiStateElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.enumys.Message;
import org.shanerx.tradeshop.enumys.Permissions;
import org.shanerx.tradeshop.enumys.ShopRole;
import org.shanerx.tradeshop.objects.Shop;
import org.shanerx.tradeshop.objects.ShopItemStack;
import org.shanerx.tradeshop.objects.ShopUser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EditCommand extends CommandRunner {

    private Shop shop;
    private InventoryGui mainMenu,
            userEdit,
            costEdit,
            productEdit;
    private List<ShopItemStack> costItems,
            productItems;
    private List<Boolean> costItemsRemoval,
            productItemsRemoval;
    public final ItemStack TRUE_ITEM = new ItemStack(Material.EMERALD_BLOCK);
    public final ItemStack FALSE_ITEM = new ItemStack(Material.REDSTONE_BLOCK);


    public EditCommand(TradeShop instance, CommandPass command) {
        super(instance, command);
    }

    /**
     * Opens a GUI allowing the player to edit the shop
     */
    public void edit() {
        shop = findShop();

        if (shop == null)
            return;

        if (!(shop.getOwner().getUUID().equals(pSender.getUniqueId()) ||
                shop.getManagersUUID().contains(pSender.getUniqueId()) ||
                Permissions.hasPermission(pSender, Permissions.ADMIN))) {
            sendMessage(Message.NO_EDIT.getPrefixed());
            return;
        }

        mainMenu = new InventoryGui(plugin, "Edit Menu-" + shop.getShopLocationAsSL().serialize(), MENU_LAYOUT);

        mainMenu.setFiller(new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1));

        // ShopUser edit menu, currently can only change/remove. Adding only available through commands
        mainMenu.addElement(new StaticGuiElement('a', new ItemStack(Material.PLAYER_HEAD), click -> {
            userEdit = new InventoryGui(plugin, "Edit Users", EDIT_LAYOUT);
            Set<ShopUser> shopUsers = new HashSet<>();
            GuiElementGroup userGroup = new GuiElementGroup('g');

            // Previous page
            userEdit.addElement(PREV_BUTTON);

            // Next page
            userEdit.addElement(NEXT_BUTTON);

            // Cancel and Back
            userEdit.addElement(CANCEL_BUTTON);

            userEdit.addElement(new StaticGuiElement('a', new ItemStack(Material.BLUE_STAINED_GLASS_PANE), " "));

            // Owner added separately as it is not editable
            userGroup.addElement(new StaticGuiElement('e', shop.getOwner().getHead(), shop.getOwner().getName(), "Position: " + shop.getOwner().getRole().toString()));

            if (shop.getOwner().getUUID().equals(pSender.getUniqueId())) {

                // Save and Back
                userEdit.addElement(new StaticGuiElement('s', new ItemStack(Material.ANVIL), click3 -> {
                    shop.updateShopUsers(shopUsers);
                    InventoryGui.goBack(pSender);
                    return true;
                }, "Save Changes"));

                for (ShopUser user : Iterables.concat(shop.getManagers(), shop.getMembers())) {
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
                                    "Click here to change to Member."),
                            new GuiStateElement.State(change -> {
                                shopUsers.remove(user);
                                user.setRole(ShopRole.MEMBER);
                                shopUsers.add(user);
                            },
                                    "MEMBER",
                                    user.getHead(),
                                    user.getName(),
                                    "Position: MEMBER",
                                    "Click here to remove this player."),
                            new GuiStateElement.State(change -> {
                                shopUsers.remove(user);
                                user.setRole(ShopRole.SHOPPER);
                                shopUsers.add(user);
                            },
                                    "SHOPPER",
                                    new ItemStack(Material.BARRIER),
                                    user.getName(),
                                    "Position: NONE",
                                    "Click here to add the player as a Manager.")
                    ));

                }
            } else {
                for (ShopUser user : Iterables.concat(shop.getManagers(), shop.getMembers())) {
                    userGroup.addElement(new StaticGuiElement('e', user.getHead(), user.getName(), "Position: " + user.getRole().toString()));
                }
            }
            userEdit.addElement(userGroup);
            userEdit.show(pSender);
            return true;
        }, "Edit Shop Users"));

        mainMenu.addElement(new StaticGuiElement('b', new ItemStack(Material.GOLD_NUGGET), click -> {
            costEdit = new InventoryGui(plugin, "Edit Costs", EDIT_LAYOUT);
            costItems = new ArrayList<>();
            costItemsRemoval = new ArrayList<>();
            for (ShopItemStack item : shop.getCost()) {
                costItems.add(item.clone());
                costItemsRemoval.add(false);
            }
            GuiElementGroup costGroup = new GuiElementGroup('g');

            // Previous page
            costEdit.addElement(PREV_BUTTON);

            // Next page
            costEdit.addElement(NEXT_BUTTON);

            // Cancel and Back
            costEdit.addElement(CANCEL_BUTTON);

            // Save and Back
            costEdit.addElement(new StaticGuiElement('s', new ItemStack(Material.ANVIL), click3 -> {
                for (int i = costItems.size() - 1; i >= 0; i--) {
                    if (costItemsRemoval.get(i))
                        costItems.remove(i);
                }
                shop.updateCost(costItems);
                InventoryGui.goBack(pSender);
                return true;
            }, "Save Changes"));

            costEdit.addElement(new StaticGuiElement('a', new ItemStack(Material.YELLOW_STAINED_GLASS_PANE), " "));

            for (int i = 0; i < costItems.size(); i++) {
                costGroup.addElement(shopItemEditMenu(i, true));
            }

            costEdit.addElement(costGroup);
            costEdit.show(pSender);
            return true;
        }, "Edit Shop Costs"));

        mainMenu.addElement(new StaticGuiElement('c', new ItemStack(Material.GRASS_BLOCK), click -> {
            productEdit = new InventoryGui(plugin, "Edit Products", EDIT_LAYOUT);
            productItems = new ArrayList<>();
            productItemsRemoval = new ArrayList<>();
            for (ShopItemStack item : shop.getProduct()) {
                productItems.add(item.clone());
                productItemsRemoval.add(false);
            }
            GuiElementGroup productGroup = new GuiElementGroup('g');

            // Previous page
            productEdit.addElement(PREV_BUTTON);

            // Next page
            productEdit.addElement(NEXT_BUTTON);

            // Cancel and Back
            productEdit.addElement(CANCEL_BUTTON);

            // Save and Back
            productEdit.addElement(new StaticGuiElement('s', new ItemStack(Material.ANVIL), click3 -> {
                for (int i = productItems.size() - 1; i >= 0; i--) {
                    if (productItemsRemoval.get(i))
                        productItems.remove(i);
                }
                shop.updateProduct(productItems);
                InventoryGui.goBack(pSender);
                return true;
            }, "Save Changes"));

            productEdit.addElement(new StaticGuiElement('a', new ItemStack(Material.YELLOW_STAINED_GLASS_PANE), " "));

            for (int i = 0; i < productItems.size(); i++) {
                productGroup.addElement(shopItemEditMenu(i, false));
            }

            productEdit.addElement(productGroup);
            productEdit.show(pSender);
            return true;
        }, "Edit Shop Products"));

        mainMenu.show(pSender);
    }

    private StaticGuiElement shopItemEditMenu(int index, boolean isCost) {
        return new StaticGuiElement('e', (isCost ? costItems : productItems).get(index).getItemStack(), click2 -> {
            ShopItemStack item = (isCost ? costItems : productItems).get(index),
                    tempItem = item.clone();
            InventoryGui itemEdit = new InventoryGui(plugin, "Edit Cost Item", ITEM_LAYOUT);
            GuiElementGroup itemGroup = new GuiElementGroup('g');

            // Cancel and Back
            itemEdit.addElement(CANCEL_BUTTON);

            // Save and Back
            itemEdit.addElement(new StaticGuiElement('s', new ItemStack(Material.ANVIL), click3 -> {
                (isCost ? costItems : productItems).set((isCost ? costItems : productItems).indexOf(item), tempItem);
                InventoryGui.goBack(pSender);
                return true;
            }, "Save Changes"));

            itemEdit.addElement(new StaticGuiElement('a', new ItemStack(Material.YELLOW_STAINED_GLASS_PANE), " "));

            itemGroup.addElement(new GuiStateElement('e',
                    (isCost ? costItemsRemoval : productItemsRemoval).get(index) + "",
                    new GuiStateElement.State(change -> (isCost ? costItemsRemoval : productItemsRemoval).set(index, true),
                            "true",
                            new ItemStack(Material.BARRIER),
                            item.getItemName()),
                    new GuiStateElement.State(change -> (isCost ? costItemsRemoval : productItemsRemoval).set(index, false),
                            "false",
                            item.getItemStack())

            ));

            //Add new item settings below
            try {
                if (item.getItemStack().getItemMeta() instanceof Damageable) {
                    itemGroup.addElement(numericalOption("CompareDurability", tempItem, new ItemStack[]{
                            FALSE_ITEM, new ItemStack(Material.IRON_BLOCK), TRUE_ITEM, new ItemStack(Material.GOLD_BLOCK)
                    }));
                }

                if (tempItem.getItemStack().getItemMeta() instanceof BookMeta) {
                    itemGroup.addElement(booleanOption("CompareBookAuthor", tempItem, TRUE_ITEM, FALSE_ITEM));
                    itemGroup.addElement(booleanOption("CompareBookPages", tempItem, TRUE_ITEM, FALSE_ITEM));
                }

                if (tempItem.getItemStack().getType().toString().endsWith("SHULKER_BOX")) {
                    itemGroup.addElement(booleanOption("CompareShulkerInventory", tempItem, TRUE_ITEM, FALSE_ITEM));
                }

                itemGroup.addElement(booleanOption("CompareName", tempItem, TRUE_ITEM, FALSE_ITEM));
                itemGroup.addElement(booleanOption("CompareLore", tempItem, TRUE_ITEM, FALSE_ITEM));
                itemGroup.addElement(booleanOption("CompareUnbreakable", tempItem, TRUE_ITEM, FALSE_ITEM));
                itemGroup.addElement(booleanOption("CompareEnchantments", tempItem, TRUE_ITEM, FALSE_ITEM));
                itemGroup.addElement(booleanOption("CompareItemFlags", tempItem, TRUE_ITEM, FALSE_ITEM));
                itemGroup.addElement(booleanOption("CompareCustomModelData", tempItem, TRUE_ITEM, FALSE_ITEM));
                itemGroup.addElement(booleanOption("CompareAttributeModifier", tempItem, TRUE_ITEM, FALSE_ITEM));
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }

            itemEdit.addElement(itemGroup);
            itemEdit.show(pSender);
            return true;
        });
    }

    private GuiStateElement numericalOption(String setting, ShopItemStack tempItem, ItemStack[] indexedTempItem) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        String readableSetting = makeStringReadable(setting);
        Method isSetting = ShopItemStack.class.getMethod("is" + setting, Integer.class);
        Method setSetting = ShopItemStack.class.getMethod("set" + setting, Void.class);

        return new GuiStateElement('e',
                isSetting.invoke(tempItem, (Object) null) + "",
                new GuiStateElement.State(change -> {
                    try {
                        setSetting.invoke(tempItem, 2);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                },
                        "2",
                        indexedTempItem[3],
                        readableSetting,
                        "State: >="),
                new GuiStateElement.State(change -> {
                    try {
                        setSetting.invoke(tempItem, -1);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                },
                        "-1",
                        indexedTempItem[0],
                        readableSetting,
                        "State: False"),
                new GuiStateElement.State(change -> {
                    try {
                        setSetting.invoke(tempItem, 0);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                },
                        "0",
                        indexedTempItem[1],
                        readableSetting,
                        "State: <="),
                new GuiStateElement.State(change -> {
                    try {
                        setSetting.invoke(tempItem, 1);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                },
                        "1",
                        indexedTempItem[2],
                        readableSetting,
                        "State: =="

                ));
    }

    private GuiStateElement booleanOption(String setting, ShopItemStack tempItem, ItemStack trueTempItem, ItemStack falseTempItem) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String readableSetting = makeStringReadable(setting);
        Method isSetting = ShopItemStack.class.getMethod("is" + setting, Boolean.class);
        Method setSetting = ShopItemStack.class.getMethod("set" + setting, Void.class);


        return new GuiStateElement('e',
                isSetting.invoke(tempItem, (Object) null) + "",
                new GuiStateElement.State(change -> {
                    try {
                        setSetting.invoke(tempItem, true);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                },
                        "true",
                        trueTempItem,
                        readableSetting,
                        "State: True"),
                new GuiStateElement.State(change -> {
                    try {
                        setSetting.invoke(tempItem, false);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                },
                        "false",
                        falseTempItem,
                        readableSetting,
                        "State: False"
                ));
    }

    private String makeStringReadable(String toFix) {
        StringBuilder ret = new StringBuilder();

        //Adds a space before each capital letter
        for (int i = 0; i < toFix.length(); i++) {
            char ch = toFix.charAt(i);
            if (ch >= 'A' && ch <= 'Z' && i > 0)
                ret.append(" ").append(ch);
            else
                ret.append(ch);
        }

        //removes any Leading/Trailing spaces and returns
        return ret.toString().trim();
    }
}