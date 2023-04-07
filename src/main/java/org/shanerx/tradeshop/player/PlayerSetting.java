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

package org.shanerx.tradeshop.player;

import com.google.common.collect.Sets;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shoplocation.IllegalWorldException;
import org.shanerx.tradeshop.shoplocation.ShopLocation;
import org.shanerx.tradeshop.utils.Utils;
import org.shanerx.tradeshop.utils.gsonprocessing.GsonProcessor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerSetting implements Serializable {

    private transient UUID uuid;
    private final String uuidString;
    private final Set<String> ownedShops;
    private final Set<String> staffShops;
    private boolean showInvolvedStatus, adminEnabled = true;

    private int multi = Setting.MULTI_TRADE_DEFAULT.getInt();

    private final transient TradeShop PLUGIN = TradeShop.getPlugin();

    private transient Utils utils = new Utils();

    public PlayerSetting(UUID playerUUID, Map<String, Integer> data) {
        this.uuid = playerUUID;
        this.uuidString = uuid.toString();

        if (data.containsKey("multi")) multi = data.get("multi");

        ownedShops = Sets.newHashSet();
        staffShops = Sets.newHashSet();

        load();
    }

    public PlayerSetting(UUID playerUUID) {
        this.uuid = playerUUID;
        this.uuidString = uuid.toString();

        ownedShops = Sets.newHashSet();
        staffShops = Sets.newHashSet();

        load();
    }

    public static PlayerSetting deserialize(String serialized) {
        PlayerSetting playerSetting = new GsonProcessor().fromJson(serialized, PlayerSetting.class);
        playerSetting.load();
        return playerSetting;
    }

    public boolean isAdminEnabled() {
        return adminEnabled;
    }

    public void setAdminEnabled(boolean adminEnabled) {
        this.adminEnabled = adminEnabled;
    }

    public int getMulti() {
        return multi;
    }

    public void setMulti(int multi) {
        this.multi = multi;
    }

    public Set<String> getOwnedShops() {
        return ownedShops;
    }

    public void addShop(Shop shop) {
        if (shop.getOwner().getUUID().equals(uuid) &&
                !ownedShops.contains(shop.getShopLocationAsSL().serialize()))
            ownedShops.add(shop.getShopLocationAsSL().serialize());
        else if (shop.getUsersUUID(ShopRole.MANAGER, ShopRole.MEMBER).contains(uuid) &&
                !ownedShops.contains(shop.getShopLocationAsSL().serialize()))
            staffShops.add(shop.getShopLocationAsSL().serialize());
    }

    public void removeShop(Shop shop) {
        ownedShops.remove(shop.getShopLocationAsSL().serialize());
        staffShops.remove(shop.getShopLocationAsSL().serialize());
    }

    public void removeShop(String shop) {
        ownedShops.remove(shop);
        staffShops.remove(shop);
    }

    public void updateShop(Shop shop) {
        if (!shop.getUsersUUID(ShopRole.OWNER, ShopRole.MANAGER, ShopRole.MEMBER).contains(uuid))
            removeShop(shop);
        else
            addShop(shop);

    }

    public UUID getUuid() {
        return uuid;
    }

    public Set<String> getStaffShops() {
        return staffShops;
    }

    public void load() {
        if (uuid == null) uuid = UUID.fromString(uuidString);
        if (multi > Setting.MULTI_TRADE_MAX.getInt()) multi = Setting.MULTI_TRADE_MAX.getInt();
        utils = new Utils();
    }

    public String serialize() {
        return new GsonProcessor().toJson(this);
    }

    public String getInvolvedStatusesString() {
        Set<String> nullShops = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        sb.append("&eStatus of your shops: \n");
        sb.append("&eShop Role &f| &eType &f| &eAvailable Trades &f| &eLocation &f| &eInventory Status\n&b");
        if (getOwnedShops().size() > 0) {
            getOwnedShops().forEach(s -> {
                try {
                    Shop shop = TradeShop.getPlugin().getDataStorage().loadShopFromSign(ShopLocation.deserialize(s));
                    if (shop == null) {
                        nullShops.add(s);
                    } else if (shop.checkRole(uuid) != ShopRole.SHOPPER) {
                        sb.append(shop.checkRole(uuid).toString()).append(" &f|&a ");
                        sb.append(shop.getShopType().toString()).append(" &f|&b ");
                        sb.append(shop.getAvailableTrades()).append(" &f|&d ");
                        sb.append(s).append(" &f| ");
                        sb.append(shop.getStatus().getLine()).append("\n&b");
                    }
                } catch (IllegalWorldException ignored) {
                    //Prevents IllegalWorldException when a player has shops in a world that is not loaded, They are not removed in case the world is loaded again...
                }
            });
        }
        if (getStaffShops().size() > 0) {
            getStaffShops().forEach(s -> {
                try {
                    Shop shop = PLUGIN.getDataStorage().loadShopFromSign(ShopLocation.deserialize(s));
                    if (shop == null) {
                        nullShops.add(s);
                    } else if (shop.checkRole(uuid) != ShopRole.SHOPPER) {
                        sb.append(shop.checkRole(uuid).toString()).append(" &f|&a ");
                        sb.append(shop.getShopType().toString()).append(" &f|&b ");
                        sb.append(shop.getAvailableTrades()).append(" &f|&d ");
                        sb.append(s).append(" &f| ");
                        sb.append(shop.getStatus().getLine()).append("\n&b");
                    }
                } catch (IllegalWorldException ignored) {
                    //Prevents IllegalWorldException when a player has shops in a world that is not loaded, They are not removed in case the world is loaded again...
                }
            });
        }

        nullShops.forEach(this::removeShop);

        sb.deleteCharAt(sb.lastIndexOf("\n"));
        return utils.colorize(sb.toString());
    }

    public InventoryGui getInvolvedStatusesInventory() {
        Set<String> nullShops = new HashSet<>();
        InventoryGui gui = new InventoryGui(PLUGIN, Bukkit.getOfflinePlayer(uuid).getName() + "'s Shops", new String[]{"ggggggggg", "ggggggggg", " fp   ln "});
        GuiElementGroup group = new GuiElementGroup('g');
        if (getOwnedShops().size() > 0) {
            getOwnedShops().forEach(s -> {
                try {
                    Shop shop = PLUGIN.getDataStorage().loadShopFromSign(ShopLocation.deserialize(s));
                    if (shop == null) {
                        nullShops.add(s);
                    } else if (shop.checkRole(uuid) != ShopRole.SHOPPER) {
                        group.addElement(new StaticGuiElement('e',
                                new ItemStack(shop.getInventoryLocation() != null ?
                                        shop.getInventoryLocation().getBlock().getType() :
                                        Material.getMaterial(shop.getShopLocation().getBlock().getType().toString().replaceAll("WALL_", ""))),
                                Math.min(shop.getAvailableTrades(), 64),
                                click -> {
                                    return true; //Prevents clicking the item from doing anything, required parameter when using amount
                                },
                                utils.colorize("&d" + s),
                                utils.colorize("&a" + shop.getShopType().toString()),
                                utils.colorize("&b" + shop.checkRole(uuid).toString()),
                                utils.colorize("&bAvailable Trades: " + shop.getAvailableTrades()),
                                utils.colorize(shop.getStatus().getLine())));
                    }
                } catch (IllegalWorldException ignored) {
                    //Prevents IllegalWorldException when a player has shops in a world that is not loaded, They are not removed in case the world is loaded again...
                }
            });
        }
        if (getStaffShops().size() > 0) {
            getStaffShops().forEach(s -> {
                try {
                    Shop shop = PLUGIN.getDataStorage().loadShopFromSign(ShopLocation.deserialize(s));
                    if (shop == null) {
                        nullShops.add(s);
                    } else if (shop.checkRole(uuid) != ShopRole.SHOPPER) {
                        group.addElement(new StaticGuiElement('e',
                                new ItemStack(shop.getInventoryLocation() != null ?
                                        shop.getInventoryLocation().getBlock().getType() :
                                        Material.getMaterial(shop.getShopLocation().getBlock().getType().toString().replaceAll("WALL_", ""))),
                                Math.min(shop.getAvailableTrades(), 64),
                                click -> {
                                    return true; //Prevents clicking the item from doing anything, required parameter when using amount
                                },
                                utils.colorize("&d" + s),
                                utils.colorize("&a" + shop.getShopType().toString()),
                                utils.colorize("&b" + shop.checkRole(uuid).toString()),
                                utils.colorize("&bAvailable Trades: " + shop.getAvailableTrades()),
                                utils.colorize(shop.getStatus().getLine())));
                    }
                } catch (IllegalWorldException ignored) {
                    //Prevents IllegalWorldException when a player has shops in a world that is not loaded, They are not removed in case the world is loaded again...
                }
            });
        }

        nullShops.forEach(this::removeShop);

        gui.addElement(group);

        // First page
        gui.addElement(new GuiPageElement('f', new ItemStack(Material.STICK), GuiPageElement.PageAction.FIRST, "Go to first page (current: %page%)"));

        // Previous page
        gui.addElement(new GuiPageElement('p', new ItemStack(Material.POTION), GuiPageElement.PageAction.PREVIOUS, "Go to previous page (%prevpage%)"));

        // Next page
        gui.addElement(new GuiPageElement('n', new ItemStack(Material.SPLASH_POTION), GuiPageElement.PageAction.NEXT, "Go to next page (%nextpage%)"));

        // Last page
        gui.addElement(new GuiPageElement('l', new ItemStack(Material.ARROW), GuiPageElement.PageAction.LAST, "Go to last page (%pages%)"));

        //Blank Item
        gui.setFiller(new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1));

        return gui;
    }

    public boolean showInvolvedStatus() {
        return showInvolvedStatus;
    }

    public void setShowInvolvedStatus(boolean showInvolvedStatus) {
        this.showInvolvedStatus = showInvolvedStatus;
    }
}
