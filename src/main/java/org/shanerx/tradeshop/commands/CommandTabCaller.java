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
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.enumys.Commands;
import org.shanerx.tradeshop.enumys.Message;
import org.shanerx.tradeshop.enumys.Permissions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandTabCaller implements TabCompleter {

	private TradeShop plugin;
	private CommandPass cmdPass;
	private Commands command;
	private CommandTabCompleter tabCompleter;

	public CommandTabCaller(TradeShop instance) {
		plugin = instance;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		cmdPass = new CommandPass(sender, cmd, label, args);
		command = Commands.getType(cmdPass.getArgAt(0));

		if (command != null) {

			if (!checkPerm()) {
				return null;
			}

			if (command.needsPlayer() && !(sender instanceof Player)) {
				sender.sendMessage(Message.PLAYER_ONLY_COMMAND.getPrefixed());
				return null;
			}

			tabCompleter = new CommandTabCompleter(plugin, cmdPass);

			switch (command) {
				case HELP:
					return tabCompleter.help();
				case ADD_PRODUCT:
					return tabCompleter.addSet();
				case ADD_COST:
					return tabCompleter.addSet();
				case SET_COST:
					return tabCompleter.addSet();
				case SET_PRODUCT:
					return tabCompleter.addSet();
				case ADD_MANAGER:
					return tabCompleter.fillServerPlayer();
				case REMOVE_USER:
					return tabCompleter.fillShopPlayer();
				case ADD_MEMBER:
					return tabCompleter.fillServerPlayer();
				default:
					return Collections.EMPTY_LIST;
			}
		} else {
			if (cmdPass.argsSize() < 2) {
				List<String> subCmds = new ArrayList<>();
				for (Commands cmds : Commands.values()) {
					if (cmds.isPartialName(args[0]))
						subCmds.add(cmds.getFirstName());
				}

				return subCmds;
			}

			return Collections.EMPTY_LIST;
		}
	}


	/**
	 * Checks if the sender has the required permission
	 *
	 * @return true if permission is NONE or sender has permission
	 */
	public boolean checkPerm() {
		if (!cmdPass.getSender().hasPermission(command.getPerm().getPerm()) && !command.getPerm().equals(Permissions.NONE)) {
			cmdPass.getSender().sendMessage(Message.NO_COMMAND_PERMISSION.getPrefixed());
			return false;
		}

		return true;
	}
}
