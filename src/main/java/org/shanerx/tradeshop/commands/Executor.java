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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.enumys.Message;
import org.shanerx.tradeshop.enumys.Permissions;
import org.shanerx.tradeshop.enumys.Setting;
import org.shanerx.tradeshop.enumys.ShopRole;
import org.shanerx.tradeshop.enumys.ShopType;
import org.shanerx.tradeshop.objects.Shop;
import org.shanerx.tradeshop.objects.ShopUser;
import org.shanerx.tradeshop.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

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
			if (args[0].equalsIgnoreCase("help")) {
				if (!sender.hasPermission(Permissions.HELP.getPerm())) {
					sender.sendMessage(Message.NO_COMMAND_PERMISSION.getPrefixed());
					return true;
				}

				StringBuilder sb = new StringBuilder();
				String msg;

				sb.append("\n&2")
						.append(getPluginName())
						.append(" ")
						.append(getVersion())
						.append(" by ").append(pdf.getAuthors().get(0)).append(" & ").append(pdf.getAuthors().get(1))
						.append("\n\n&6/tradeshop help &c - Display help message\n");

				if (sender.hasPermission(Permissions.CREATE.getPerm())) {
					sb.append("&6/tradeshop setup &c - Display TradeShop setup tutorial\n")
							.append("&6/tradeshop item &c - Shows helpful information on item held by player\n");
				}

				sb.append("&6/tradeshop bugs &c - Report bugs\n")
						.append("&6/tradeshop addowner|removeowner [target] &c - Add/Remove another owner to your shop\n")
						.append("&6/tradeshop addmember|removemember [target] &c - Add/Remove a collaborator to your shop\n");

				if (sender.hasPermission(Permissions.EDIT.getPerm())) {
					sb.append("&6/tradeshop addproduct &c - Add item to your shop\n")
							.append("&6/tradeshop addcost &c - Change cost of your shop\n")
							.append("&6/tradeshop open &c - Open shop\n")
							.append("&6/tradeshop close &c -Close shop\n");
				}

				if (sender.hasPermission(Permissions.WHO.getPerm())) {
					sb.append("&6/tradeshop who &c - Shows members shop being looked at\n");
				}


				if (sender.hasPermission(Permissions.ADMIN.getPerm())) {
					sb.append("\n&6/tradeshop addItem [item name] &c - Adds custom items to config")
							.append("\n&6/tradeshop removeItem [item name] &c - Removes custom items to config")
							.append("\n&6/tradeshop getcustomitems &c - shows all custom items")
							.append("\n&6/tradeshop reload &c - Reloads plugin configuration files\n");
				}

				msg = sb.toString();

				sender.sendMessage(colorize(msg));
				return true;

			} else if (args[0].equalsIgnoreCase("bugs")) {
				sender.sendMessage(colorize("\n&2To report any bugs to the author, either send a PM on"
						+ " &cSpigot &2- &egoo.gl/s6Jk23 &2or open an issue on &cGitHub &2-&e goo.gl/X4qqyg\n"));
				return true;

			} else if (args[0].equalsIgnoreCase("setup")) {
				if (!sender.hasPermission(Permissions.CREATE.getPerm())) {
					sender.sendMessage(Message.NO_COMMAND_PERMISSION.getPrefixed());
					return true;
				}
				sender.sendMessage(Message.SETUP_HELP.getPrefixed());
				return true;

			} else if (args[0].equalsIgnoreCase("reload")) {
				if (!sender.hasPermission(Permissions.ADMIN.getPerm())) {
					sender.sendMessage(Message.NO_COMMAND_PERMISSION.getPrefixed());
					return true;

				}
				plugin.reloadConfig();
				sender.sendMessage(colorize(getPrefix() + "&6The configuration files have been reloaded!"));
				return true;

			} else if (args[0].equalsIgnoreCase("addProduct")) { //TODO add removeProduct
				if (!(sender instanceof Player)) {
					sender.sendMessage(Message.PLAYER_ONLY_COMMAND.getPrefixed());
					return true;
				}

				if (!sender.hasPermission(Permissions.EDIT.getPerm())) {
					sender.sendMessage(Message.NO_COMMAND_PERMISSION.getPrefixed());
					return true;
				}

				Player p = (Player) sender;
				Block b = p.getTargetBlock(null, Setting.MAX_EDIT_DISTANCE.getInt());

				if (b == null || b.getType() == Material.AIR) {
					p.sendMessage(Message.NO_SIGHTED_SHOP.getPrefixed());
					return true;

				}

				Shop shop;
				int amount = 0; //TODO add support for text entries
				String mat = "";

				if (ShopType.isShop(b)) {
					shop = Shop.loadShop((Sign) b.getState());
				} else if (plugin.getListManager().isInventory(b.getType()) &&
						((InventoryHolder) b.getState()).getInventory().getName().contains("$ ^Sign:l_")) {
					String loc = ((InventoryHolder) b.getState()).getInventory().getName().split("$ ^")[1];
					loc.replace("Sign:", "");
					shop = Shop.loadShop(loc);
				} else {
					p.sendMessage(Message.NO_SIGHTED_SHOP.getPrefixed());
					return true;
				}


				ItemStack itemInHand = p.getInventory().getItemInMainHand();

				if (itemInHand == null || itemInHand.getType() == Material.AIR) {
					sender.sendMessage(Message.HELD_EMPTY.getPrefixed());
					return true;
				}

				if (!isValidType(itemInHand.getType().toString())) {
					sender.sendMessage(Message.ILLEGAL_ITEM.getPrefixed());
					return true;
				}

				shop.setProduct(itemInHand);
				shop.saveShop();
				shop.updateSign();

				sender.sendMessage(Message.ITEM_ADDED.getPrefixed());
				return true;

			} else if (args[0].equalsIgnoreCase("addCost")) { //TODO Add removeCost
				if (!(sender instanceof Player)) {
					sender.sendMessage(Message.PLAYER_ONLY_COMMAND.getPrefixed());
					return true;
				}

				if (!sender.hasPermission(Permissions.EDIT.getPerm())) {
					sender.sendMessage(Message.NO_COMMAND_PERMISSION.getPrefixed());
					return true;
				}

				Player p = (Player) sender;
				Block b = p.getTargetBlock(null, Setting.MAX_EDIT_DISTANCE.getInt());

				if (b == null || b.getType() == Material.AIR) {
					p.sendMessage(Message.NO_SIGHTED_SHOP.getPrefixed());
					return true;

				}

				Shop shop;
				int amount = 0; //TODO add support for text entries
				String mat = "";

				if (ShopType.isShop(b)) {
					shop = Shop.loadShop((Sign) b.getState());
				} else if (plugin.getListManager().isInventory(b.getType()) &&
						((InventoryHolder) b.getState()).getInventory().getName().contains("$ ^Sign:l_")) {
					String loc = ((InventoryHolder) b.getState()).getInventory().getName().split("$ ^")[0];
					loc.replace("Sign:", "");
					shop = Shop.loadShop(loc);
				} else {
					p.sendMessage(Message.NO_SIGHTED_SHOP.getPrefixed());
					return true;
				}

				if (!(shop.getOwner().getUUID().equals(p.getUniqueId()) || shop.getManagersUUID().contains(p.getUniqueId()))) {
					p.sendMessage(Message.NO_EDIT.getPrefixed());
					return true;
				}


				ItemStack itemInHand = p.getInventory().getItemInMainHand();

				if (itemInHand == null || itemInHand.getType() == Material.AIR) {
					sender.sendMessage(Message.HELD_EMPTY.getPrefixed());
					return true;
				}

				if (!isValidType(itemInHand.getType().toString())) {
					sender.sendMessage(Message.ILLEGAL_ITEM.getPrefixed());
					return true;
				}

				shop.setCost(itemInHand);
				shop.saveShop();
				shop.updateSign();

				sender.sendMessage(Message.ITEM_ADDED.getPrefixed());
				return true;

			} else if (args[0].equalsIgnoreCase("open")) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(Message.PLAYER_ONLY_COMMAND.getPrefixed());
					return true;
				}

				if (!sender.hasPermission(Permissions.EDIT.getPerm())) {
					sender.sendMessage(Message.NO_COMMAND_PERMISSION.getPrefixed());
					return true;
				}

				Player p = (Player) sender;
				Block b = p.getTargetBlock(null, Setting.MAX_EDIT_DISTANCE.getInt());

				if (b == null || b.getType() == Material.AIR) {
					p.sendMessage(Message.NO_SIGHTED_SHOP.getPrefixed());
					return true;

				}

				Shop shop;

				if (ShopType.isShop(b)) {
					shop = Shop.loadShop((Sign) b.getState());
				} else if (plugin.getListManager().isInventory(b.getType()) &&
						((InventoryHolder) b.getState()).getInventory().getName().contains("$ ^Sign:l_")) {
					String loc = ((InventoryHolder) b.getState()).getInventory().getName().split("$ ^")[0];
					loc.replace("Sign:", "");
					shop = Shop.loadShop(loc);
				} else {
					p.sendMessage(Message.NO_SIGHTED_SHOP.getPrefixed());
					return true;
				}

				if (!(shop.getOwner().getUUID().equals(p.getUniqueId()) || shop.getManagersUUID().contains(p.getUniqueId()))) {
					p.sendMessage(Message.NO_EDIT.getPrefixed());
					return true;
				}

				if (shop.missingItems()) {
					p.sendMessage(Message.MISSING_ITEM.getPrefixed());
					return true;
				}

				boolean opened = shop.setOpen();
				shop.saveShop();

				if (opened) {
					sender.sendMessage(Message.CHANGE_OPEN.getPrefixed());
				} else {
					sender.sendMessage(Message.MISSING_ITEM.getPrefixed());
				}
				return true;

			} else if (args[0].equalsIgnoreCase("close")) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(Message.PLAYER_ONLY_COMMAND.getPrefixed());
					return true;
				}

				if (!sender.hasPermission(Permissions.EDIT.getPerm())) {
					sender.sendMessage(Message.NO_COMMAND_PERMISSION.getPrefixed());
					return true;
				}

				Player p = (Player) sender;
				Block b = p.getTargetBlock(null, Setting.MAX_EDIT_DISTANCE.getInt());

				if (b == null || b.getType() == Material.AIR) {
					p.sendMessage(Message.NO_SIGHTED_SHOP.getPrefixed());
					return true;

				}

				Shop shop;

				if (ShopType.isShop(b)) {
					shop = Shop.loadShop((Sign) b.getState());
				} else if (plugin.getListManager().isInventory(b.getType()) &&
						((InventoryHolder) b.getState()).getInventory().getName().contains("$ ^Sign:l_")) {
					String loc = ((InventoryHolder) b.getState()).getInventory().getName().split("$ ^")[0];
					loc.replace("Sign:", "");
					shop = Shop.loadShop(loc);
				} else {
					p.sendMessage(Message.NO_SIGHTED_SHOP.getPrefixed());
					return true;
				}

				if (!(shop.getOwner().getUUID().equals(p.getUniqueId()) || shop.getManagersUUID().contains(p.getUniqueId()))) {
					p.sendMessage(Message.NO_EDIT.getPrefixed());
					return true;
				}

				shop.setClosed();
				shop.saveShop();
				shop.updateSign();

				sender.sendMessage(Message.CHANGE_CLOSED.getPrefixed());
				return true;

			} else if (args[0].equalsIgnoreCase("switch")) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(Message.PLAYER_ONLY_COMMAND.getPrefixed());
					return true;
				}

				if (!sender.hasPermission(Permissions.EDIT.getPerm())) {
					sender.sendMessage(Message.NO_COMMAND_PERMISSION.getPrefixed());
					return true;
				}

				Player p = (Player) sender;
				Block b = p.getTargetBlock(null, Setting.MAX_EDIT_DISTANCE.getInt());

				if (b == null || b.getType() == Material.AIR) {
					p.sendMessage(Message.NO_SIGHTED_SHOP.getPrefixed());
					return true;

				}

				Shop shop;

				if (ShopType.isShop(b)) {
					shop = Shop.loadShop((Sign) b.getState());
				} else if (plugin.getListManager().isInventory(b.getType()) &&
						((InventoryHolder) b.getState()).getInventory().getName().contains("$ ^Sign:l_")) {
					String loc = ((InventoryHolder) b.getState()).getInventory().getName().split("$ ^")[0];
					loc.replace("Sign:", "");
					shop = Shop.loadShop(loc);
				} else {
					p.sendMessage(Message.NO_SIGHTED_SHOP.getPrefixed());
					return true;
				}

				if (!(shop.getOwner().getUUID().equals(p.getUniqueId()) || shop.getManagersUUID().contains(p.getUniqueId()))) {
					p.sendMessage(Message.NO_EDIT.getPrefixed());
					return true;
				}

				shop.switchType();

				sender.sendMessage(Message.SHOP_TYPE_SWITCHED.getPrefixed().replace("%newtype%", shop.getShopType().toHeader()));
				return true;

			} else if (args[0].equalsIgnoreCase("what")) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(Message.PLAYER_ONLY_COMMAND.getPrefixed());
					return true;
				}

				if (!sender.hasPermission(Permissions.EDIT.getPerm())) {
					sender.sendMessage(Message.NO_COMMAND_PERMISSION.getPrefixed());
					return true;
				}

				Player p = (Player) sender;
				Block b = p.getTargetBlock(null, Setting.MAX_EDIT_DISTANCE.getInt());

				if (b == null || b.getType() == Material.AIR) {
					p.sendMessage(Message.NO_SIGHTED_SHOP.getPrefixed());
					return true;

				}

				Shop shop;

				if (ShopType.isShop(b)) {
					shop = Shop.loadShop((Sign) b.getState());
				} else if (plugin.getListManager().isInventory(b.getType()) &&
						((InventoryHolder) b.getState()).getInventory().getName().contains("$ ^Sign:l_")) {
					String loc = ((InventoryHolder) b.getState()).getInventory().getName().split("$ ^")[0];
					loc.replace("Sign:", "");
					shop = Shop.loadShop(loc);
				} else {
					p.sendMessage(Message.NO_SIGHTED_SHOP.getPrefixed());
					return true;
				}

				if (!(shop.getOwner().getUUID().equals(p.getUniqueId()) || shop.getManagersUUID().contains(p.getUniqueId()))) {
					p.sendMessage(Message.NO_EDIT.getPrefixed());
					return true;
				}

				Inventory shopContents = Bukkit.createInventory(null, 18, colorize(Bukkit.getOfflinePlayer(shop.getOwner().getUUID()).getName() + "'s Shop                                 "));

				ItemStack costLabel = new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1),
						productLabel = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE, 1),
						blankLabel = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1);

				ItemMeta costMeta = costLabel.getItemMeta(),
						productMeta = productLabel.getItemMeta(),
						blankMeta = blankLabel.getItemMeta();

				ArrayList<String> costLore = new ArrayList<>();
				costLore.add("This is the item");
				costLore.add("you give to make");
				costLore.add("the trade.");

				ArrayList<String> productLore = new ArrayList<>();
				productLore.add("This is the item");
				productLore.add("the you receive");
				productLore.add("from the trade.");

				costMeta.setDisplayName("Cost");
				costMeta.setLore(costLore);

				productMeta.setDisplayName("Product");
				productMeta.setLore(productLore);

				blankMeta.setDisplayName(" ");

				costLabel.setItemMeta(costMeta);
				productLabel.setItemMeta(productMeta);
				blankLabel.setItemMeta(blankMeta);

				shopContents.setItem(2, costLabel);
				shopContents.setItem(11, shop.getProduct());
				shopContents.setItem(6, productLabel);
				shopContents.setItem(15, shop.getCost());
				shopContents.setItem(0, blankLabel);
				shopContents.setItem(1, blankLabel);
				shopContents.setItem(3, blankLabel);
				shopContents.setItem(4, blankLabel);
				shopContents.setItem(5, blankLabel);
				shopContents.setItem(7, blankLabel);
				shopContents.setItem(8, blankLabel);
				shopContents.setItem(9, blankLabel);
				shopContents.setItem(10, blankLabel);
				shopContents.setItem(12, blankLabel);
				shopContents.setItem(13, blankLabel);
				shopContents.setItem(14, blankLabel);
				shopContents.setItem(16, blankLabel);
				shopContents.setItem(17, blankLabel);

				p.openInventory(shopContents);
				return true;

			} else if (args[0].equalsIgnoreCase("who")) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(Message.PLAYER_ONLY_COMMAND.getPrefixed());
					return true;

				} else if (!sender.hasPermission(Permissions.WHO.getPerm())) {
					sender.sendMessage(Message.NO_COMMAND_PERMISSION.getPrefixed());
					return true;
				}

				Player p = (Player) sender;
				String owner = "";
				StringBuilder managers = new StringBuilder();
				StringBuilder members = new StringBuilder();
				Shop shop;
				Block b;
				Sign s;

				try {
					b = p.getTargetBlock(null, Setting.MAX_EDIT_DISTANCE.getInt());

					if (b == null || b.getType() == Material.AIR)
						throw new NoSuchFieldException();

					if (ShopType.isShop(b)) {
						s = (Sign) b.getState();
						shop = Shop.loadShop(s);

						if (shop.getShopType().isITrade()) {
							p.sendMessage(Message.WHO_MESSAGE.getPrefixed()
									.replace("{OWNER}", Setting.ITRADESHOP_OWNER.getString())
									.replace("{MANAGERS}", "None")
									.replace("{MEMBERS}", "None"));
							return true;
						}

					} else if (plugin.getListManager().isInventory(b.getType()) &&
							((InventoryHolder) b.getState()).getInventory().getName().contains("$ ^Sign:l_")) {
						String loc = ((InventoryHolder) b.getState()).getInventory().getName().split("$ ^")[1];
						shop = Shop.loadShop(loc);
						s = shop.getShopSign();

						if (shop.getShopType().isITrade()) {
							p.sendMessage(Message.WHO_MESSAGE.getPrefixed()
									.replace("{OWNER}", Setting.ITRADESHOP_OWNER.getString())
									.replace("{MANAGERS}", "None")
									.replace("{MEMBERS}", "None"));
							return true;
						}
					} else
						throw new NoSuchFieldException();

					if (shop.getOwner() != null)
						owner = shop.getOwner().getName();

					if (shop.getManagers().size() > 0) {
						for (ShopUser usr : shop.getManagers()) {
							if (managers.toString().equals(""))
								managers = new StringBuilder(usr.getName());
							else
								managers.append(", ").append(usr.getName());
						}
					}

					if (shop.getMembers().size() > 0) {
						for (ShopUser usr : shop.getMembers()) {
							if (members.toString().equals(""))
								members = new StringBuilder(usr.getName());
							else
								members.append(", ").append(usr.getName());
						}
					}

					if (managers.toString().equals("")) {
						managers = new StringBuilder("None");
					}
					if (members.toString().equals("")) {
						members = new StringBuilder("None");
					}
					p.sendMessage(Message.WHO_MESSAGE.getPrefixed()
							.replace("{OWNER}", owner)
							.replace("{MANAGERS}", managers.toString())
							.replace("{MEMBERS}", members.toString()));
					return true;
				} catch (NoSuchFieldException e) {
					p.sendMessage(Message.NO_SIGHTED_SHOP.getPrefixed());
					return true;
				}

			} else if (args[0].equalsIgnoreCase("getCustomItems")) {
				if (!sender.hasPermission(Permissions.ADMIN.getPerm())) {
					sender.sendMessage(Message.NO_COMMAND_PERMISSION.getPrefixed());
					return true;
				}
				Set<String> items = plugin.getCustomItemManager().getItems();
				StringBuilder sb = new StringBuilder();
				sender.sendMessage(colorize("&aCurrent custom items:"));
				for (String s : items) {
					sb.append("-" + s + "  ");
				}

				sender.sendMessage(sb.toString());
				return true;

			}
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
