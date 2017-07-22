/*
 *     Copyright (c) 2016-2017 SparklingComet @ http://shanerx.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * NOTICE: All modifications made by others to the source code belong
 * to the respective contributor. No contributor should be held liable for
 * any damages of any kind, whether be material or moral, which were
 * caused by their contribution(s) to the project. See the full License for more information
 */

package org.shanerx.tradeshop.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.Utils;

public class Executor extends Utils implements CommandExecutor {
	
	private TradeShop plugin;
	
	public Executor(TradeShop instance) {
		plugin = instance;
	}
	
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.getMessages().getString("invalid-arguments")));
			return true;
			
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("help")) {
				if (!sender.hasPermission("tradeshop.help")) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.getMessages().getString("no-command-permission")));
					return true;
				}
				
				String line1 = "\n";
				String line2 = "&2" + getPluginName() + " " + getVersion() + " by " + pdf.getAuthors().get(0) + "\n";
				String line3 = "\n";
				String line4 = "\n";
				String line5 = "&6/tradeshop help &c - Display help message\n";
				String line7 = "&6/tradeshop setup &c - Display TradeShop setup tutorial\n";
				String line8 = "&6/tradeshop item &c - Shows helpful iformation on item held by player\n";
				String line9 = "&6/tradeshop bugs &c - Report bugs\n \n";
				
				if (sender.hasPermission("tradeshop.admin")) {
					String helpMsg = line1 + line2 + line3 + line4 + line5 + line7 + line8 + line9;
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', helpMsg));
					return true;
					
				} else {
					String helpMsg = line1 + line2 + line3 + line4 + line5 + line7;
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', helpMsg));
					return true;
					
				}
			} else if (args[0].equalsIgnoreCase("bugs")) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "\n &2To report any bugs to the author, either send a PM on"
						+ " &cSpigot &2- &egoo.gl/s6Jk23 &2or open an issue on &cGitHub &2-&e goo.gl/X4qqyg\n"));
				return true;
				
			} else if (args[0].equalsIgnoreCase("setup")) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.getMessages().getString("setup-help")));
				return true;
				
			} else if (args[0].equalsIgnoreCase("reload")) {
				if (!sender.hasPermission("tradeshop.admin")) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.getMessages().getString("no-command-permission")));
					return true;
					
				}
				plugin.reloadConfig();
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + "&6The configuration files have been reloaded!"));
				return true;
				
			} else if (args[0].equalsIgnoreCase("item")) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(plugin.getMessages().getString("player-only-command"));
					return true;
					
				}
				if (!sender.hasPermission("tradeshop.create")) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.getMessages().getString("no-command-permission")));
					return true;
				}
				
				Player pl = (Player) sender;
				ItemStack itm = pl.getInventory().getItemInMainHand();
				if (itm.getType() != null) {
					String msg = ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.getMessages().getString("held-item"))
							.replace("{MATERIAL}", itm.getType().name())
							.replace("{DURABILITY}", itm.getDurability() + "")
							.replace("{ID}", itm.getTypeId() + "")
							.replace("{AMOUNT}", itm.getAmount() + "");
					sender.sendMessage(msg);
					return true;
					
				} else
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.getMessages().getString("held-empty")));
				return true;
			}
		}
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.getMessages().getString("invalid-arguments")));
		return true;
	}
}