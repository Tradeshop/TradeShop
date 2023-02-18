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

import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.commands.CommandPass;
import org.shanerx.tradeshop.data.config.Message;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.data.config.Variable;
import org.shanerx.tradeshop.framework.events.PlayerShopCloseEvent;
import org.shanerx.tradeshop.framework.events.PlayerShopOpenEvent;
import org.shanerx.tradeshop.player.Permissions;
import org.shanerx.tradeshop.player.ShopRole;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shop.ShopStatus;
import org.shanerx.tradeshop.utils.objects.Tuple;

public class ShopCommand extends CommandRunner {

    public ShopCommand(TradeShop instance, CommandPass command) {
        super(instance, command);
    }

    /**
     * Sets the shop to the open status allowing trades to happen
     */
    public void open() {
        Shop shop = findShop();

        if (shop == null)
            return;

        if (!(shop.getUsersUUID(ShopRole.MANAGER, ShopRole.OWNER).contains(pSender.getUniqueId())
                || Permissions.isAdminEnabled(pSender))) {
            Message.NO_SHOP_PERMISSION.sendMessage(pSender);
            return;
        }

        PlayerShopOpenEvent event = new PlayerShopOpenEvent(pSender, shop);
        if (event.isCancelled()) return;

        ShopStatus status = shop.setOpen();

        switch (status) {
            case OPEN:
                Message.CHANGE_OPEN.sendMessage(pSender);
                break;
            case INCOMPLETE:
                if (shop.isMissingItems())
                    Message.MISSING_ITEM.sendMessage(pSender);
                else if (shop.getChestAsSC() == null)
                    Message.MISSING_CHEST.sendMessage(pSender);
                break;
            case OUT_OF_STOCK:
                Message.SHOP_EMPTY.sendMessage(pSender);
                break;
        }
    }

    /**
     * Sets the shop to the close status preventing trades from happen
     */
    public void close() {
        Shop shop = findShop();

        if (shop == null)
            return;

        if (!(shop.getUsersUUID(ShopRole.MANAGER, ShopRole.OWNER).contains(pSender.getUniqueId())
                || Permissions.isAdminEnabled(pSender))) {
            Message.NO_SHOP_PERMISSION.sendMessage(pSender);
            return;
        }

        PlayerShopCloseEvent event = new PlayerShopCloseEvent(pSender, shop);
        if (event.isCancelled()) return;

        shop.setStatus(ShopStatus.CLOSED);
        shop.updateSign();
        shop.saveShop();

        Message.CHANGE_CLOSED.sendMessage(pSender);
    }

    /**
     * Switches the shop type between BiTrade and Trade
     */
    public void switchShop() {
        Shop shop = findShop();

        if (shop == null)
            return;

        if (!Permissions.hasPermission(pSender, Permissions.EDIT)) {
            Message.NO_COMMAND_PERMISSION.sendMessage(pSender);
            return;
        }

        switch (shop.getShopType()) {
            case TRADE:
                if (!Permissions.hasPermission(pSender, Permissions.CREATEBI)) {
                    Message.NO_COMMAND_PERMISSION.sendMessage(pSender);
                    return;
                }
            case BITRADE:
                if (!Permissions.hasPermission(pSender, Permissions.CREATE)) {
                    Message.NO_COMMAND_PERMISSION.sendMessage(pSender);
                    return;
                }
        }

        if (!(shop.getUsersUUID(ShopRole.MANAGER, ShopRole.OWNER).contains(pSender.getUniqueId())
                || (Setting.UNLIMITED_ADMIN.getBoolean() && Permissions.isAdminEnabled(pSender)))) {
            Message.NO_SHOP_PERMISSION.sendMessage(pSender);
            return;
        }

        shop.switchType();

        Message.SHOP_TYPE_SWITCHED.sendMessage(pSender, new Tuple<>(Variable.NEW_TYPE.toString(), shop.getShopType().toHeader()));
    }
}
