package com.github.ShanerX.TradeShop.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.ShanerX.TradeShop.TradeShop;
import com.github.ShanerX.TradeShop.Utils;

public class ts extends Utils implements CommandExecutor{

	TradeShop plugin;
	
	public ts(TradeShop instance) {
		plugin = instance;
	}
	
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (label.equalsIgnoreCase("tradeshop") || label.equalsIgnoreCase("ts")) {
			if (args.length == 0) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + " &eTry &6/tradeshop help &eto display help!"));
				return true;
			}
			
			if (args.length == 1) {
				
				if (args[0].equalsIgnoreCase("help")) {
					
					if (! sender.hasPermission("tradeshop.help")) {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + " &aYou do not have permission to execute this command"));
						return true;
					}

				
					String line1 = "\n";
					String line2 = "&2" + getPluginName() + " " + getVersion() + " by " + getAuthor() + "\n";
					String line3 = "\n";
					String line4 = "\n";
					String line5 = "&6/tradeshop help &c - Display help message\n";
		//			String line6 = "&6/tradeshop admin &c - Display admin commands\n";
					String line7 = "&6/tradeshop setup &c - Display TradeShop setup tutorial\n";
					String line8 = "&6/tradeshop bugs &c - Report bugs\n \n";
					
					if (sender.hasPermission("tradeshop.admin")) {
						String helpMsg = line1 + line2 + line3 + line4 + line5 + line7 + line8;
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', helpMsg));
						return true;
					}
					else {
						String helpMsg = line1 + line2 + line3 + line4 + line5 + line7;
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', helpMsg));
						return true;
					}
					
				}
				
	/*			if (args[0].equalsIgnoreCase("admin")) {
					if (!sender.hasPermission("tradeshop.admin")) {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + " &aYou do not have permission to execute this command"));
						return true;
					}
				}
	*/			
				else if (args[0].equalsIgnoreCase("bugs")) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "\n &2To report any bugs to the author, either send a PM on"
							+ " &cSpigot &2- &egoo.gl/s6Jk23 &2or open an issue on &cGitHub &2-&e goo.gl/X4qqyg\n"));
					return true;
				}
				
				else if (args[0].equalsIgnoreCase("setup")) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "\n&2Setting up a TradeShop is easy! Just make sure to follow these steps:"
							+ "\n \nStep 1: &ePlace down a chest."
							+ "\n&2Step 2: &ePlace a sign on top of the chest."
							+ "\n&2Step 3: &eWrite the following on the sign"
							+ "\n&6[Trade]\n<amount> <item_you_sell>\n<amount> <item_you_buy>\n&6&oEmpty line\n"));
					return true;
				}
				
				else if (args[0].equalsIgnoreCase("owner")) {
					if (!sender.hasPermission("tradeshop.admin")) {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + " &aYou do not have permission to execute this command"));
						return true;
					}
				}
				
	/*			else if (args[0].equalsIgnoreCase("bypass")) {
					if (!sender.hasPermission("tradeshop.admin")) {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + " &aYou do not have permission to execute this command"));
						return true;
					}
					if (! (sender instanceof Player)) {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Sorry, but only players can execute this command"));
						return true;
					}
					
					Player admin = (Player)sender;
					if (admins.contains(admin.getName())) {
						admins.remove(admin.getName());
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + " &aYou toggled TradeShop bypass mode to &eoff"));
						return true;
					}
					admins.add(admin.getName());
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + " &aYou toggled TradeShop bypass mode to &eon"));
					return true;
				}
	*/			
			}
			
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + " &eTry &6/tradeshop help &eto display help!"));
			return true;
			
	/*		else if (args.length == 2) {
				
				if(args[0].equalsIgnoreCase("bypass")) {
					Player player = plugin.getServer().getPlayer(args[1]);
					if (admins.contains(player.getName())) {
						admins.remove(player.getName());
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + " &aYou toggled TradeShop bypass mode to &eoff &a for &e" + player.getName()));
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + " &aYou are no longer bypassing TradeShops!"));
						return true;
					}
					admins.add(player.getName());
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + " &aYou toggled TradeShop bypass mode to &eon &a for &e" + player.getName()));
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + " &aYou are now bypassing TradeShops!"));
					return true;
				}
				 
			}*/
		}
		
		
		return true;
	}
}
