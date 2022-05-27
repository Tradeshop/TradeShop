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
import org.shanerx.tradeshop.commands.commandrunners.AdminCommand;
import org.shanerx.tradeshop.commands.commandrunners.BasicTextCommand;
import org.shanerx.tradeshop.commands.commandrunners.CommandRunner;
import org.shanerx.tradeshop.commands.commandrunners.CreateCommand;
import org.shanerx.tradeshop.commands.commandrunners.EditCommand;
import org.shanerx.tradeshop.commands.commandrunners.GeneralPlayerCommand;
import org.shanerx.tradeshop.commands.commandrunners.ShopCommand;
import org.shanerx.tradeshop.commands.commandrunners.ShopItemCommand;
import org.shanerx.tradeshop.commands.commandrunners.ShopUserCommand;
import org.shanerx.tradeshop.commands.commandrunners.WhatCommand;
import org.shanerx.tradeshop.data.config.Message;
import org.shanerx.tradeshop.framework.ShopChange;
import org.shanerx.tradeshop.item.ShopItemSide;
import org.shanerx.tradeshop.player.ShopRole;

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

		CommandRunner cmdRnnr = new CommandRunner(plugin, cmdPass);

		switch (command) {
			case HELP:
				new BasicTextCommand(plugin, cmdPass).help();
				break;
			case BUGS:
				new BasicTextCommand(plugin, cmdPass).bugs();
				break;
			case SETUP:
				new BasicTextCommand(plugin, cmdPass).setup();
				break;
			case RELOAD:
				new AdminCommand(plugin, cmdPass).reload();
				break;
			case ADD_PRODUCT:
				new ShopItemCommand(plugin, cmdPass, ShopItemSide.PRODUCT).addSide();
				break;
			case ADD_COST:
				new ShopItemCommand(plugin, cmdPass, ShopItemSide.COST).addSide();
				break;
			case OPEN:
				new ShopCommand(plugin, cmdPass).open();
				break;
			case CLOSE:
				new ShopCommand(plugin, cmdPass).close();
				break;
			case SWITCH:
				new ShopCommand(plugin, cmdPass).switchShop();
				break;
			case WHAT:
				new WhatCommand(plugin, cmdPass).what();
				break;
			case WHO:
				new ShopUserCommand(plugin, cmdPass).who();
				break;
			case REMOVE_USER:
				new ShopUserCommand(plugin, cmdPass).editUser(ShopRole.SHOPPER, ShopChange.REMOVE_USER);
				break;
			case ADD_MANAGER:
				new ShopUserCommand(plugin, cmdPass).editUser(ShopRole.MANAGER, ShopChange.ADD_MANAGER);
				break;
			case ADD_MEMBER:
				new ShopUserCommand(plugin, cmdPass).editUser(ShopRole.MEMBER, ShopChange.ADD_MEMBER);
				break;
			case SET_MANAGER:
				new ShopUserCommand(plugin, cmdPass).editUser(ShopRole.MANAGER, ShopChange.SET_MANAGER);
				break;
			case SET_MEMBER:
				new ShopUserCommand(plugin, cmdPass).editUser(ShopRole.MEMBER, ShopChange.SET_MEMBER);
				break;
			case MULTI:
				new GeneralPlayerCommand(plugin, cmdPass).multi();
				break;
			case SET_PRODUCT:
				new ShopItemCommand(plugin, cmdPass, ShopItemSide.PRODUCT).setSide();
				break;
			case SET_COST:
				new ShopItemCommand(plugin, cmdPass, ShopItemSide.COST).setSide();
				break;
			case LIST_PRODUCT:
				new ShopItemCommand(plugin, cmdPass, ShopItemSide.PRODUCT).listSide();
				break;
			case LIST_COST:
				new ShopItemCommand(plugin, cmdPass, ShopItemSide.COST).listSide();
				break;
			case REMOVE_PRODUCT:
				new ShopItemCommand(plugin, cmdPass, ShopItemSide.PRODUCT).removeSide();
				break;
			case REMOVE_COST:
				new ShopItemCommand(plugin, cmdPass, ShopItemSide.COST).removeSide();
				break;
			case PLAYER_LEVEL:
				new AdminCommand(plugin, cmdPass).playerLevel();
				break;
			case STATUS:
				new GeneralPlayerCommand(plugin, cmdPass).status();
				break;
			case EDIT:
				new EditCommand(plugin, cmdPass).edit();
				break;
			case TOGGLE_STATUS:
				new GeneralPlayerCommand(plugin, cmdPass).toggleStatus();
				break;
			case CREATE_TRADE:
				new CreateCommand(plugin, cmdPass).createTrade();
				break;
			case CREATE_BITRADE:
				new CreateCommand(plugin, cmdPass).createBiTrade();
				break;
			case CREATE_ITRADE:
				new CreateCommand(plugin, cmdPass).createITrade();
				break;
			case TOGGLE_ADMIN:
				new AdminCommand(plugin, cmdPass).toggleAdmin();
				break;
			case ADMIN:
				new AdminCommand(plugin, cmdPass).admin();
				break;
		}

		return true;
	}
}
