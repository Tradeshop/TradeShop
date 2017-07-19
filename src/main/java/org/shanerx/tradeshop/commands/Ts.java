package org.shanerx.tradeshop.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.Utils;

public class Ts extends Utils implements CommandExecutor {
	
	private TradeShop plugin;
	
	public Ts(TradeShop instance) {
		plugin = instance;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.getConfig().getString("invalid-arguments")));
			return true;
		
		}
		else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("help")) {
				if (!sender.hasPermission("tradeshop.help")) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.getConfig().getString("no-command-permission")));
					return true;
				}
				
				String line1 = "\n";
				String line2 = "&2" + getPluginName() + " " + getVersion() + " by " + getAuthor() + "\n";
				String line3 = "\n";
				String line4 = "\n";
				String line5 = "&6/tradeshop help &c - Display help message\n";
				String line7 = "&6/tradeshop setup &c - Display TradeShop setup tutorial\n";
				String line8 = "&6/tradeshop bugs &c - Report bugs\n \n";
				
				if (sender.hasPermission("tradeshop.admin")) {
					String helpMsg = line1 + line2 + line3 + line4 + line5 + line7 + line8;
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
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.getConfig().getString("setup-help")));
				return true;
				
			} else if (args[0].equalsIgnoreCase("reload")) {
				if (!sender.hasPermission("tradeshop.admin")) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.getConfig().getString("no-command-permission")));
					return true;
					
				}
				plugin.reloadConfig();
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + "&6The configuration files have been reloaded!"));
				return true;
			}
		}
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.getConfig().getString("invalid-arguments")));
		return true;
	}
}