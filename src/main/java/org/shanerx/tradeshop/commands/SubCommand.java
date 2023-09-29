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

package org.shanerx.tradeshop.commands;

import com.google.common.collect.Lists;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.commands.commandrunners.*;
import org.shanerx.tradeshop.data.config.Message;
import org.shanerx.tradeshop.framework.ShopChange;
import org.shanerx.tradeshop.item.ShopItemSide;
import org.shanerx.tradeshop.player.ShopRole;
import org.shanerx.tradeshop.shop.ShopType;
import org.shanerx.tradeshop.utils.Utils;

import java.util.ArrayList;

public abstract class SubCommand extends Utils {

    protected final TradeShop plugin;

    protected final CommandSender sender;
    protected final ArrayList<String> args;

    public SubCommand(TradeShop instance, CommandSender sender, String[] args) {
        this.plugin = instance;
        this.sender = sender;
        this.args = Lists.newArrayList(args);
    }

    public static void runSubCommand(CommandType commandType, CommandSender sender, String[] args) {
        if (commandType == null) {
            throw new IllegalArgumentException("commandType cannot be null!");
        }
        TradeShop plugin = TradeShop.getPlugin();

        switch (commandType) {
            case HELP:
                new BasicTextSubCommand(plugin, sender, args).help();
                break;
            case BUGS:
                new BasicTextSubCommand(plugin, sender, args).bugs();
                break;
            case SETUP:
                new BasicTextSubCommand(plugin, sender, args).setup();
                break;
            case RELOAD:
                new AdminSubCommand(plugin, sender, args).reload();
                break;
            case ADD_PRODUCT:
                new ShopItemSubCommand(plugin, sender, args, ShopItemSide.PRODUCT).addSide();
                break;
            case ADD_COST:
                new ShopItemSubCommand(plugin, sender, args, ShopItemSide.COST).addSide();
                break;
            case OPEN:
                new ShopSubCommand(plugin, sender, args).open();
                break;
            case CLOSE:
                new ShopSubCommand(plugin, sender, args).close();
                break;
            case SWITCH:
                new ShopSubCommand(plugin, sender, args).switchShop();
                break;
            case WHAT:
                new WhatSubCommand(plugin, sender, args).what();
                break;
            case WHO:
                new ShopUserSubCommand(plugin, sender, args).who();
                break;
            case REMOVE_USER:
                new ShopUserSubCommand(plugin, sender, args).editUser(ShopRole.SHOPPER, ShopChange.REMOVE_USER);
                break;
            case ADD_MANAGER:
                new ShopUserSubCommand(plugin, sender, args).editUser(ShopRole.MANAGER, ShopChange.ADD_MANAGER);
                break;
            case ADD_MEMBER:
                new ShopUserSubCommand(plugin, sender, args).editUser(ShopRole.MEMBER, ShopChange.ADD_MEMBER);
                break;
            case SET_MANAGER:
                new ShopUserSubCommand(plugin, sender,  args).editUser(ShopRole.MANAGER, ShopChange.SET_MANAGER);
                break;
            case SET_MEMBER:
                new ShopUserSubCommand(plugin, sender, args).editUser(ShopRole.MEMBER, ShopChange.SET_MEMBER);
                break;
            case MULTI:
                new GeneralPlayerSubCommand(plugin, sender, args).multi();
                break;
            case SET_PRODUCT:
                new ShopItemSubCommand(plugin, sender, args, ShopItemSide.PRODUCT).setSide();
                break;
            case SET_COST:
                new ShopItemSubCommand(plugin, sender, args, ShopItemSide.COST).setSide();
                break;
            case LIST_PRODUCT:
                new ShopItemSubCommand(plugin, sender, args, ShopItemSide.PRODUCT).listSide();
                break;
            case LIST_COST:
                new ShopItemSubCommand(plugin, sender, args, ShopItemSide.COST).listSide();
                break;
            case REMOVE_PRODUCT:
                new ShopItemSubCommand(plugin, sender, args, ShopItemSide.PRODUCT).removeSide();
                break;
            case REMOVE_COST:
                new ShopItemSubCommand(plugin, sender, args, ShopItemSide.COST).removeSide();
                break;
            case STATUS:
                new GeneralPlayerSubCommand(plugin, sender, args).status();
                break;
            case EDIT:
                new EditSubCommand(plugin, sender, args).edit();
                break;
            case TOGGLE_STATUS:
                new GeneralPlayerSubCommand(plugin, sender, args).toggleStatus();
                break;
            case FIND:
                new ShopFindSubCommand(plugin, sender, args).find();
                break;
            case CREATE_TRADE:
                new CreateSubCommand(plugin, sender, args).createShop(ShopType.TRADE);
                break;
            case CREATE_BITRADE:
                new CreateSubCommand(plugin, sender, args).createShop(ShopType.BITRADE);
                break;
            case CREATE_ITRADE:
                new CreateSubCommand(plugin, sender, args).createShop(ShopType.ITRADE);
                break;
            case TOGGLE_ADMIN:
                new AdminSubCommand(plugin, sender, args).toggleAdmin();
                break;
            case ADMIN:
                new AdminSubCommand(plugin, sender, args).admin();
                break;
            case METRICS:
                new AdminSubCommand(plugin, sender, args).metrics();
                break;
        }
    }

    public CommandSender getSender() {
        return sender;
    }

    public boolean isSenderPlayer() {
        return getSender() instanceof Player;
    }

    public Player getPlayerSender() {
        return isSenderPlayer() ? (Player) getSender() : null;
    }

    public int argsSize() {
        return args.size();
    }

    public boolean hasArgAt(int index) {
        return index < argsSize();
    }

    public String getArgAt(int index) {
        if (hasArgAt(index)) {
            return args.get(index);
        } else {
            return null;
        }
    }

    public static String getArgAt(String args[], int index) {
        if (index < args.length) {
            return args[index];
        }
        return null;
    }

    public ArrayList<String> getArgs() {
        return args;
    }

    public boolean hasArgs() {
        return argsSize() > 0;
    }

    /**
     * Colors and sends the string to the sender
     *
     * @param message message to send to the sender
     */
    public void sendMessage(String message) {
        getSender().sendMessage((new Utils()).colorize(message));
    }
}