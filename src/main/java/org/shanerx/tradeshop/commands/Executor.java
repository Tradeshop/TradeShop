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

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.enumys.*;
import org.shanerx.tradeshop.objects.Shop;
import org.shanerx.tradeshop.objects.ShopUser;
import org.shanerx.tradeshop.utils.Utils;

import java.util.Arrays;

public class Executor extends Utils implements CommandExecutor {

	private TradeShop plugin;

	public Executor(TradeShop instance) {
		plugin = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(Message.INVALID_ARGUMENTS.getPrefixed());
			return true;

		} else if (args.length == 1) {

		} else if (args.length == 2) {
			if (Arrays.asList("addmanager", "removemanager", "addmember", "removemember").contains(args[0].toLowerCase())) { //TODO Fix these to work with the new systems
				if (!(sender instanceof Player)) {
					sender.sendMessage(Message.PLAYER_ONLY_COMMAND.getPrefixed());
					return true;

				}

				Player p = (Player) sender;
				Block b = p.getTargetBlock(null, Setting.MAX_EDIT_DISTANCE.getInt());
				Sign s = null;

				if (b != null && plugin.getListManager().isInventory(b.getType())) {
					s = findShopSign(b);
				} else if (ShopType.isShop(b)) {
					s = (Sign) b.getState();
				} else if (!plugin.getListManager().isInventory(b.getType()) || findShopSign(b) == null) {
					p.sendMessage(Message.NO_SIGHTED_SHOP.getPrefixed());
					return true;
				}

				OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
				if (!target.hasPlayedBefore()) {
					p.sendMessage(Message.PLAYER_NOT_FOUND.getPrefixed());
					return true;
				}

				Shop shop = Shop.loadShop(s);

				switch (args[0].toLowerCase()) {

					case "addmanager":
						shop.addManager(new ShopUser(target, ShopRole.MANAGER));
						if (!shop.getMembersUUID().contains(target.getUniqueId())) {
							shop.removeMember(new ShopUser(target, ShopRole.MEMBER));
						}
						break;
					case "removemanager":
						shop.removeManager(new ShopUser(target, ShopRole.MANAGER));
						break;
					case "addmember":
						if (!shop.getManagersUUID().contains(target.getUniqueId())) {
							shop.addMember(new ShopUser(target, ShopRole.MEMBER));
						}
						break;
					case "removemember":
						shop.removeMember(new ShopUser(target, ShopRole.MEMBER));
						break;
				}

				shop.saveShop();
				shop.updateSign();
				p.sendMessage(Message.UPDATED_SHOP_MEMBERS.getPrefixed());
				return true;

			} else if (args[0].equalsIgnoreCase("addItem")) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(Message.PLAYER_ONLY_COMMAND.getPrefixed());
					return true;
				}

				if (!sender.hasPermission(Permissions.ADMIN.getPerm())) {
					sender.sendMessage(Message.NO_COMMAND_PERMISSION.getPrefixed());
					return true;
				}

				Player p = (Player) sender;
				String name = args[1];

				ItemStack itm = p.getInventory().getItemInMainHand();

				if (itm.getType().equals(Material.AIR) || itm.getType() == null) {
					p.sendMessage(colorize("&cYou must ne holding an item to create a custom item."));
					return true;
				}

				plugin.getCustomItemManager().addItem(name, itm);
				p.sendMessage(colorize("&a" + name + " has been added to the custom items."));
				return true;

			} else if (args[0].equalsIgnoreCase("removeItem")) {
				if (!sender.hasPermission(Permissions.ADMIN.getPerm())) {
					sender.sendMessage(Message.NO_COMMAND_PERMISSION.getPrefixed());
					return true;
				}

				String name = args[1];

				plugin.getCustomItemManager().removeItem(name);
				sender.sendMessage(colorize("&a" + name + " has been removed from the custom items."));
				return true;

			} else if (args[0].equalsIgnoreCase("getCI") || args[0].equalsIgnoreCase("items")) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(Message.PLAYER_ONLY_COMMAND.getPrefixed());
					return true;
				}

				if (!sender.hasPermission(Permissions.ADMIN.getPerm())) {
					sender.sendMessage(Message.NO_COMMAND_PERMISSION.getPrefixed());
					return true;
				}

				String name = args[1];

				ItemStack itm = plugin.getCustomItemManager().getItem(name);
				if (itm == null) {
					sender.sendMessage(colorize("&c" + name + " could not be found."));
				} else {
					((Player) sender).getInventory().addItem(itm);
					sender.sendMessage(colorize("&a" + name + " has been given to you."));
				}
				return true;
			}
		}

		sender.sendMessage(Message.INVALID_ARGUMENTS.getPrefixed());
		return true;
	}
}
