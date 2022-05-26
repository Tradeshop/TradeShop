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
import org.shanerx.tradeshop.utils.objects.ObjectHolder;
import org.shanerx.tradeshop.utils.objects.Tuple;

/**
 * Implementation of CommandRunner for commands that view/change shop users
 *
 * @since 2.6.0
 */
public class ShopUserCommand extends CommandRunner {

    public ShopUserCommand(TradeShop instance, CommandPass command) {
        super(instance, command);
    }

    /**
     * Tells the player who the Owner/Managers/Members that are on the shop are
     */
    public void who() {
        String owner = "";
        StringBuilder managers = new StringBuilder();
        StringBuilder members = new StringBuilder();
        Shop shop = findShop();

        if (shop == null)
            return;

        if (shop.getShopType().isITrade()) {
            Message.WHO_MESSAGE.sendMessage(pSender,
                    new Tuple<>(Variable.OWNER.toString(), Setting.ITRADESHOP_OWNER.getString()),
                    new Tuple<>(Variable.MANAGERS.toString(), "None"),
                    new Tuple<>(Variable.MEMBERS.toString(), "None"));
            return;
        }

        if (shop.getOwner() != null)
            owner = shop.getOwner().getName();

        if (shop.hasUsers(ShopRole.MANAGER)) {
            for (ShopUser usr : shop.getUsers(ShopRole.MANAGER)) {
                if (managers.toString().equals(""))
                    managers = new StringBuilder(usr.getName());
                else
                    managers.append(", ").append(usr.getName());
            }
        }

        if (shop.hasUsers(ShopRole.MEMBER)) {
            for (ShopUser usr : shop.getUsers(ShopRole.MEMBER)) {
                if (members.toString().equals(""))
                    members = new StringBuilder(usr.getName());
                else
                    members.append(", ").append(usr.getName());
            }
        }

        if (managers.toString().equals("")) {
            managers = new StringBuilder("None");
        }
        if (members.toString().equals("")) {
            members = new StringBuilder("None");
        }
        Message.WHO_MESSAGE.sendMessage(pSender,
                new Tuple<>(Variable.OWNER.toString(), owner),
                new Tuple<>(Variable.MANAGERS.toString(), managers.toString()),
                new Tuple<>(Variable.MEMBERS.toString(), members.toString()));
    }

    /**
     * Adds the specified player to the shop as a manager
     */
    public void addManager() {
        Shop shop = findShop();

        if (shop == null)
            return;

        if (!shop.getOwner().getUUID().equals(pSender.getUniqueId())
                || (Setting.UNLIMITED_ADMIN.getBoolean() && Permissions.isAdminEnabled(pSender))) {
            Message.NO_SHOP_PERMISSION.sendMessage(pSender);
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(command.getArgAt(1));
        if (!target.hasPlayedBefore()) {
            Message.PLAYER_NOT_FOUND.sendMessage(pSender);
            return;
        }

        if (shop.getUsersUUID().contains(target.getUniqueId())) {
            Message.UNSUCCESSFUL_SHOP_MEMBERS.sendMessage(pSender);
            return;
        }

        PlayerShopChangeEvent changeEvent = new PlayerShopChangeEvent(pSender, shop, ShopChange.ADD_MANAGER, new ObjectHolder<OfflinePlayer>(target));
        Bukkit.getPluginManager().callEvent(changeEvent);
        if (changeEvent.isCancelled()) return;

        shop.addUser(target.getUniqueId(), ShopRole.MANAGER);

        Message.UPDATED_SHOP_MEMBERS.sendMessage(pSender);
    }

    /**
     * Removes the specified player from the shop if they currently are a manager
     */
    public void removeUser() {
        Shop shop = findShop();

        if (shop == null)
            return;

        if (!shop.getOwner().getUUID().equals(pSender.getUniqueId())
                || (Setting.UNLIMITED_ADMIN.getBoolean() && Permissions.isAdminEnabled(pSender))) {
            Message.NO_SHOP_PERMISSION.sendMessage(pSender);
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(command.getArgAt(1));
        if (!target.hasPlayedBefore()) {
            Message.PLAYER_NOT_FOUND.sendMessage(pSender);
            return;
        }

        PlayerShopChangeEvent changeEvent = new PlayerShopChangeEvent(pSender, shop, ShopChange.REMOVE_USER, new ObjectHolder<OfflinePlayer>(target));
        Bukkit.getPluginManager().callEvent(changeEvent);
        if (changeEvent.isCancelled()) return;

        if (!shop.removeUser(target.getUniqueId())) {
            Message.UNSUCCESSFUL_SHOP_MEMBERS.sendMessage(pSender);
            return;
        }

        Message.UPDATED_SHOP_MEMBERS.sendMessage(pSender);
    }

    /**
     * Adds the specified player to the shop as a member
     */
    public void addMember() {
        Shop shop = findShop();

        if (shop == null)
            return;

        if (!shop.getOwner().getUUID().equals(pSender.getUniqueId())
                || (Setting.UNLIMITED_ADMIN.getBoolean() && Permissions.isAdminEnabled(pSender))) {
            Message.NO_SHOP_PERMISSION.sendMessage(pSender);
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(command.getArgAt(1));
        if (!target.hasPlayedBefore()) {
            Message.PLAYER_NOT_FOUND.sendMessage(pSender);
            return;
        }


        if (shop.getUsersUUID().contains(target.getUniqueId())) {
            Message.UNSUCCESSFUL_SHOP_MEMBERS.sendMessage(pSender);
            return;
        }

        PlayerShopChangeEvent changeEvent = new PlayerShopChangeEvent(pSender, shop, ShopChange.ADD_MEMBER, new ObjectHolder<OfflinePlayer>(target));
        Bukkit.getPluginManager().callEvent(changeEvent);
        if (changeEvent.isCancelled()) return;

        shop.addUser(target.getUniqueId(), ShopRole.MEMBER);

        Message.UPDATED_SHOP_MEMBERS.sendMessage(pSender);
    }
}
