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

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.commands.commandrunners.AdminSubCommand;
import org.shanerx.tradeshop.commands.commandrunners.BasicTextSubCommand;
import org.shanerx.tradeshop.commands.commandrunners.CreateSubCommand;
import org.shanerx.tradeshop.commands.commandrunners.EditSubCommand;
import org.shanerx.tradeshop.commands.commandrunners.GeneralPlayerSubCommand;
import org.shanerx.tradeshop.commands.commandrunners.ShopFindSubCommand;
import org.shanerx.tradeshop.commands.commandrunners.ShopItemSubCommand;
import org.shanerx.tradeshop.commands.commandrunners.ShopSubCommand;
import org.shanerx.tradeshop.commands.commandrunners.ShopUserSubCommand;
import org.shanerx.tradeshop.commands.commandrunners.WhatSubCommand;
import org.shanerx.tradeshop.data.config.Message;
import org.shanerx.tradeshop.framework.ShopChange;
import org.shanerx.tradeshop.item.ShopItemSide;
import org.shanerx.tradeshop.player.ShopRole;
import org.shanerx.tradeshop.shop.ShopType;

/**
 * This class is used for calling command methods from CommandRunner
 * as well as doing initial checks for necessary arguments,
 * permissions, and sender type
 **/
public class CommandCaller implements CommandExecutor {

	private final TradeShop plugin;

	public CommandCaller(TradeShop instance) {
		plugin = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		CommandPass cmdPass = new CommandPass(sender, cmd, label, args);
		Commands command = Commands.getType(cmdPass.getArgAt(0));

		if (!cmdPass.hasArgs() || command == null) {
			Message.INVALID_ARGUMENTS.sendMessage(sender);
			return true;

		}

		switch (command.checkPerm(sender)) {
			case NO_PERM:
				Message.NO_COMMAND_PERMISSION.sendMessage(sender);
				return true;
			case PLAYER_ONLY:
				Message.PLAYER_ONLY_COMMAND.sendMessage(sender);
				return true;
		}

		if (command.getMinArgs() > args.length || command.getMaxArgs() < args.length) {
			Message.INVALID_ARGUMENTS.sendMessage(sender);
			return true;
		}

		switch (command) {
			case HELP:
				new BasicTextSubCommand(plugin, cmdPass).help();
				break;
			case BUGS:
				new BasicTextSubCommand(plugin, cmdPass).bugs();
				break;
			case SETUP:
				new BasicTextSubCommand(plugin, cmdPass).setup();
				break;
			case RELOAD:
				new AdminSubCommand(plugin, cmdPass).reload();
				break;
			case ADD_PRODUCT:
				new ShopItemSubCommand(plugin, cmdPass, ShopItemSide.PRODUCT).addSide();
				break;
			case ADD_COST:
				new ShopItemSubCommand(plugin, cmdPass, ShopItemSide.COST).addSide();
				break;
			case OPEN:
				new ShopSubCommand(plugin, cmdPass).open();
				break;
			case CLOSE:
				new ShopSubCommand(plugin, cmdPass).close();
				break;
			case SWITCH:
				new ShopSubCommand(plugin, cmdPass).switchShop();
				break;
			case WHAT:
				new WhatSubCommand(plugin, cmdPass).what();
				break;
			case WHO:
				new ShopUserSubCommand(plugin, cmdPass).who();
				break;
			case REMOVE_USER:
				new ShopUserSubCommand(plugin, cmdPass).editUser(ShopRole.SHOPPER, ShopChange.REMOVE_USER);
				break;
			case ADD_MANAGER:
				new ShopUserSubCommand(plugin, cmdPass).editUser(ShopRole.MANAGER, ShopChange.ADD_MANAGER);
				break;
			case ADD_MEMBER:
				new ShopUserSubCommand(plugin, cmdPass).editUser(ShopRole.MEMBER, ShopChange.ADD_MEMBER);
				break;
			case SET_MANAGER:
				new ShopUserSubCommand(plugin, cmdPass).editUser(ShopRole.MANAGER, ShopChange.SET_MANAGER);
				break;
			case SET_MEMBER:
				new ShopUserSubCommand(plugin, cmdPass).editUser(ShopRole.MEMBER, ShopChange.SET_MEMBER);
				break;
			case MULTI:
				new GeneralPlayerSubCommand(plugin, cmdPass).multi();
				break;
			case SET_PRODUCT:
				new ShopItemSubCommand(plugin, cmdPass, ShopItemSide.PRODUCT).setSide();
				break;
			case SET_COST:
				new ShopItemSubCommand(plugin, cmdPass, ShopItemSide.COST).setSide();
				break;
			case LIST_PRODUCT:
				new ShopItemSubCommand(plugin, cmdPass, ShopItemSide.PRODUCT).listSide();
				break;
			case LIST_COST:
				new ShopItemSubCommand(plugin, cmdPass, ShopItemSide.COST).listSide();
				break;
			case REMOVE_PRODUCT:
				new ShopItemSubCommand(plugin, cmdPass, ShopItemSide.PRODUCT).removeSide();
				break;
			case REMOVE_COST:
				new ShopItemSubCommand(plugin, cmdPass, ShopItemSide.COST).removeSide();
				break;
			case STATUS:
				new GeneralPlayerSubCommand(plugin, cmdPass).status();
				break;
			case EDIT:
				new EditSubCommand(plugin, cmdPass).edit();
				break;
			case TOGGLE_STATUS:
				new GeneralPlayerSubCommand(plugin, cmdPass).toggleStatus();
				break;
			case FIND:
				new ShopFindSubCommand(plugin, cmdPass).find();
				break;
			case CREATE_TRADE:
				new CreateSubCommand(plugin, cmdPass).createShop(ShopType.TRADE);
				break;
			case CREATE_BITRADE:
				new CreateSubCommand(plugin, cmdPass).createShop(ShopType.BITRADE);
				break;
			case CREATE_ITRADE:
				new CreateSubCommand(plugin, cmdPass).createShop(ShopType.ITRADE);
				break;
			case TOGGLE_ADMIN:
				new AdminSubCommand(plugin, cmdPass).toggleAdmin();
				break;
			case ADMIN:
				new AdminSubCommand(plugin, cmdPass).admin();
				break;
		}

		return true;
	}
}
