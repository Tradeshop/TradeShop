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
		CommandType commandType = CommandType.getType(args[0]);

		if (args.length == 0 || commandType == null) {
			Message.INVALID_ARGUMENTS.sendMessage(sender);
			return true;
		}

		switch (commandType.checkPerm(sender)) {
			case NO_PERM:
				Message.NO_COMMAND_PERMISSION.sendMessage(sender);
				return true;
			case PLAYER_ONLY:
				Message.PLAYER_ONLY_COMMAND.sendMessage(sender);
				return true;
		}

		if (commandType.getMinArgs() > args.length || commandType.getMaxArgs() < args.length) {
			Message.INVALID_ARGUMENTS.sendMessage(sender);
			return true;
		}

		SubCommand.runSubCommand(commandType, sender, args);

		return true;
	}
}
