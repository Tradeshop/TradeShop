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

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.commands.CommandPass;
import org.shanerx.tradeshop.data.config.Message;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.data.config.Variable;
import org.shanerx.tradeshop.framework.ShopChange;
import org.shanerx.tradeshop.framework.events.PlayerShopChangeEvent;
import org.shanerx.tradeshop.player.Permissions;
import org.shanerx.tradeshop.player.ShopRole;
import org.shanerx.tradeshop.player.ShopUser;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shoplocation.ShopLocation;
import org.shanerx.tradeshop.utils.objects.ObjectHolder;
import org.shanerx.tradeshop.utils.objects.Tuple;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of CommandRunner for commands that view/change shop users
 *
 * @since 2.6.0
 */
public class ShopUserSubCommand extends SubCommand {

    private OfflinePlayer target;

    public ShopUserSubCommand(TradeShop instance, CommandPass command) {
        super(instance, command);
    }

    /**
     * Tells the player who the Owner/Managers/Members that are on the shop are
     */
    public void who() {
        Shop shop = ShopUser.findObservedShop(command.getPlayerSender());

        if (shop == null)
            return;

        if (shop.getShopType().isITrade()) {
            Message.WHO_MESSAGE.sendMessage(command.getPlayerSender(),
                    new Tuple<>(Variable.OWNER.toString(), Setting.ITRADESHOP_OWNER.getString()),
                    new Tuple<>(Variable.MANAGERS.toString(), "None"),
                    new Tuple<>(Variable.MEMBERS.toString(), "None"));
            return;
        }

        String owner = String.join(", ", shop.getUserNames(ShopRole.OWNER));
        String managers = String.join(", ", shop.getUserNames(ShopRole.MANAGER));
        String members = String.join(", ", shop.getUserNames(ShopRole.MEMBER));

        Message.WHO_MESSAGE.sendMessage(command.getPlayerSender(),
                new Tuple<>(Variable.OWNER.toString(), owner.length() < 3 ? "None" : owner),
                new Tuple<>(Variable.MANAGERS.toString(), managers.length() < 3 ? "None" : managers),
                new Tuple<>(Variable.MEMBERS.toString(), members.length() < 3 ? "None" : members));
    }

    /**
     * Adds or Removes the specified player to/from the shop as the specified role
     */
    public void editUser(ShopRole role, ShopChange change) {
        try {
            boolean applyAllOwned = command.hasArgAt(2) && command.getArgAt(2).length() > 0 && textToBool(command.getArgAt(2));
            Set<Shop> ownedShops = new HashSet<>();
            Map<String, String> updateStatuses = new HashMap<>();

            target = Bukkit.getOfflinePlayer(command.getArgAt(1));

            Shop tempShop = shopUserCommandStart(target, applyAllOwned);

            if (applyAllOwned) {
                for (String location : plugin.getDataStorage().loadPlayer(command.getPlayerSender().getUniqueId()).getOwnedShops()) {
                    ownedShops.add(plugin.getDataStorage().loadShopFromSign(ShopLocation.deserialize(location)));
                }
            } else {
                ownedShops.add(tempShop);
            }

            for (Shop shop : ownedShops) {
                eachOwnedShop:
                {

                    PlayerShopChangeEvent changeEvent = new PlayerShopChangeEvent(command.getPlayerSender(), shop, change, new ObjectHolder<OfflinePlayer>(target));
                    Bukkit.getPluginManager().callEvent(changeEvent);
                    if (changeEvent.isCancelled()) return;

                    boolean success = false;

                    switch (change) {
                        case REMOVE_USER:
                            if (!shop.getUsersUUID(ShopRole.MANAGER, ShopRole.MEMBER).contains(target.getUniqueId())) {
                                updateStatuses.put(shop.getShopLocationAsSL().serialize(), UserOperationStatus.FAILED_MISSING.toString());
                                break eachOwnedShop;
                            }
                            success = shop.removeUser(target.getUniqueId());
                            break;
                        case ADD_MANAGER:
                        case ADD_MEMBER:
                            if (shop.getUsersUUID(ShopRole.MANAGER, ShopRole.MEMBER).contains(target.getUniqueId())) {
                                updateStatuses.put(shop.getShopLocationAsSL().serialize(), UserOperationStatus.FAILED_EXISTING.toString());
                                break eachOwnedShop;
                            } else if (shop.getUsers(ShopRole.MANAGER, ShopRole.MEMBER).size() >= Setting.MAX_SHOP_USERS.getInt()) {
                                updateStatuses.put(shop.getShopLocationAsSL().serialize(), UserOperationStatus.FAILED_CAPACITY.toString());
                                break eachOwnedShop;
                            }
                            success = shop.addUser(target.getUniqueId(), role);
                            break;
                        case SET_MANAGER:
                        case SET_MEMBER:
                            if (shop.getUsersExcluding(Collections.singletonList(target.getUniqueId()), ShopRole.MANAGER, ShopRole.MEMBER).size() >= Setting.MAX_SHOP_USERS.getInt()) {
                                updateStatuses.put(shop.getShopLocationAsSL().serialize(), UserOperationStatus.FAILED_CAPACITY.toString());
                                break eachOwnedShop;
                            }
                            success = shop.setUser(target.getUniqueId(), role);
                            break;
                    }

                    if (success)
                        updateStatuses.put(shop.getShopLocationAsSL().serialize(), UserOperationStatus.SUCCESSFUL.toString());
                    else
                        updateStatuses.put(shop.getShopLocationAsSL().serialize(), UserOperationStatus.FAILED.toString());
                }
            }

            Message.UPDATED_SHOP_USERS.sendUserEditMultiLineMessage(command.getPlayerSender(), Collections.singletonMap(Variable.UPDATED_SHOPS, updateStatuses));

        } catch (UnsupportedOperationException ignored) {
        }
    }


    //region Util Methods
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Checks if targeted player exists and if player is looking at a shop while not targetting all owned shops
     *
     * @param applyAllOwned Is Player targeting all owned shops
     * @return Shop if found or null if not needed; returning null while setting target to null indicates failure, command should respond with an immediate blank return.
     * @throws UnsupportedOperationException if failure
     */
    private Shop shopUserCommandStart(OfflinePlayer target, boolean applyAllOwned) {
        this.target = target;

        if (target == null || !target.hasPlayedBefore()) {
            Message.PLAYER_NOT_FOUND.sendMessage(command.getPlayerSender());
            throw new UnsupportedOperationException();
        }

        if (!applyAllOwned) {
            Shop shop = ShopUser.findObservedShop(command.getPlayerSender());

            if (shop == null) {
                // Message.NO_SIGHTED_SHOP.sendMessage(command.getPlayerSender()); // Message is sent by findShop()
                throw new UnsupportedOperationException();
            }

            if (!shop.getOwner().getUUID().equals(command.getPlayerSender().getUniqueId())
                    || (Setting.UNLIMITED_ADMIN.getBoolean() && Permissions.isAdminEnabled(command.getPlayerSender()))) {
                Message.NO_SHOP_PERMISSION.sendMessage(command.getPlayerSender());
                throw new UnsupportedOperationException();
            }

            return shop;
        }

        return null;
    }


    //------------------------------------------------------------------------------------------------------------------
    //endregion


    enum UserOperationStatus {
        SUCCESSFUL(Message.UPDATED_SHOP_USERS_SUCCESSFUL),
        FAILED(Message.UPDATED_SHOP_USERS_FAILED),
        FAILED_CAPACITY(Message.UPDATED_SHOP_USERS_FAILED_CAPACITY),
        FAILED_EXISTING(Message.UPDATED_SHOP_USERS_FAILED_EXISTING),
        FAILED_MISSING(Message.UPDATED_SHOP_USERS_FAILED_MISSING);

        private final Message text;

        UserOperationStatus(Message text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text.toString();
        }
    }
}