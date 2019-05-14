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

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.enumys.Commands;
import org.shanerx.tradeshop.enumys.Message;
import org.shanerx.tradeshop.enumys.Setting;
import org.shanerx.tradeshop.enumys.ShopRole;
import org.shanerx.tradeshop.enumys.ShopType;
import org.shanerx.tradeshop.framework.CustomCommandHandler;
import org.shanerx.tradeshop.framework.TradeCommand;
import org.shanerx.tradeshop.objects.Shop;
import org.shanerx.tradeshop.objects.ShopChest;
import org.shanerx.tradeshop.objects.ShopUser;
import org.shanerx.tradeshop.utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;

public class CommandRunner extends Utils {

	private TradeShop plugin;
	private CommandPass command;
	private Player pSender;

	public CommandRunner(TradeShop instance, CommandPass command) {
		this.plugin = instance;
		this.command = command;

		if (command.getSender() instanceof Player) {
			pSender = (Player) command.getSender();
		}
	}

	/**
	 * Colors and sends the string to the sender
	 *
	 * @param message message to send to the sender
	 */
	public void sendMessage(String message) {
		command.getSender().sendMessage(colorize(message));
	}

	/**
	 * Builds and sends the sender the help message
	 */
	public void help() {
		if (command.argsSize() == 2) {
			usage(command.getArgAt(1));
			return;
		}

		StringBuilder sb = new StringBuilder();

		sb.append("\n&2")
				.append(getPluginName())
				.append(" ")
				.append(getVersion())
				.append(" by ").append(pdf.getAuthors().get(0)).append(" & ").append(pdf.getAuthors().get(1))
				.append("\n\n&b/tradeshop &f &f Display help message\n");

		for (Commands c : Commands.values()) {
			if (pSender.hasPermission(c.getPerm().getPerm())) {
				sb.append(Message.colour(String.format("&b/%s &f &f %s\n", c.getFirstName(), c.getDescription())));
			}
		}

		Iterator<String> iter = CustomCommandHandler.getInstance().iter();
		while (iter.hasNext()) {
			String cmdName = iter.next();
			TradeCommand cmd = CustomCommandHandler.getInstance().getExecutable(cmdName);
			if (pSender.hasPermission(cmd.getPermission())) {
				sb.append(Message.colour(String.format("&b/%s &f &f %s\n", cmdName, cmd.getDescription())));
			}
		}

		sendMessage(colorize(sb.append("\n").toString()));
	}

	public void usage(String subcmd) {
		CustomCommandHandler handler = CustomCommandHandler.getInstance();
		if (handler.isAvailable(subcmd)) {
			sendMessage(Message.INVALID_SUBCOMMAND.getPrefixed());
			return;
		} else if (handler.isNativeCommand(subcmd)) {
			Commands cmd = Commands.getType(subcmd);
			sendMessage(Message.colour(String.format("&6Showing help for &c%s&r\n&bUsage: %s \n&bAliases: %s\n&bDescription:&e %s", subcmd, cmd.getUsage(), cmd.getAliases(), cmd.getDescription())));
			return;
		}

		TradeCommand cmd = handler.getExecutable(subcmd);
		sendMessage(Message.colour(String.format("&6Showing help for &c%s&r\n&bUsage: %s \n&bAliases: %s\n&bDescription:&e %s\n &f[%s]", subcmd, cmd.getUsage(), cmd.getAliases(), cmd.getDescription(), cmd.getPlugin().getName())));
		return;
	}

	/**
	 * Sends the sender the bug message
	 */
	public void bugs() {
		sendMessage("\n&2To report any bugs to the author, either send a PM on &cSpigot &2- &egoo.gl/s6Jk23 &2or open an issue on &cGitHub &2-&e goo.gl/X4qqyg\n");
	}

	/**
	 * Sends the sender the setup message
	 */
	public void setup() {
		sendMessage(Message.SETUP_HELP.getPrefixed());
	}

	/**
	 * Reloads the plugin and sends success message
	 */
	public void reload() {
		plugin.reloadConfig();
		sendMessage(getPrefix() + "&6The configuration files have been reloaded!");
	}

	/**
	 * Adds a product to a Shop
	 * <p>
	 * With no variables sent will use the amount and data of the held item for product
	 * </p>
	 * <p>
	 * If the player uses a int in the first variable they can change the amount for the item they are holding
	 * </p>
	 * <p>
	 * With 2 variables used the player can use an amount and material to set the sign instead of a held item
	 * </p>
	 */
	public void addProduct() {
		Shop shop = findShop();

		if (shop == null)
			return;

		int amount = 0;
		Material mat = null;

		if (command.hasArgAt(1) && isInt(command.getArgAt(1))) {
			amount = Integer.parseInt(command.getArgAt(1));
		}

		if (command.hasArgAt(2) && Material.getMaterial(command.getArgAt(2).toUpperCase()) != null) {
			mat = Material.getMaterial(command.getArgAt(2).toUpperCase());
		}

		if (!(shop.getOwner().getUUID().equals(pSender.getUniqueId()) || shop.getManagersUUID().contains(pSender.getUniqueId()))) {
			sendMessage(Message.NO_SHOP_PERMISSION.getPrefixed());
			return;
		}

		ItemStack itemInHand;

		if (mat == null) {
			itemInHand = pSender.getInventory().getItemInMainHand().clone();
		} else {
			itemInHand = new ItemStack(mat, 1);
		}

		if (itemInHand.getType() == Material.AIR) {
			sendMessage(Message.HELD_EMPTY.getPrefixed());
			return;
		}

		if (!isValidType(itemInHand.getType().toString())) {
			sendMessage(Message.ILLEGAL_ITEM.getPrefixed());
			return;
		}

		if (amount > 0) {
			itemInHand.setAmount(amount);
		}

		shop.setProduct(itemInHand);
		shop.saveShop();
		shop.updateSign();

		sendMessage(Message.ITEM_ADDED.getPrefixed());
	}

	/**
	 * Adds a cost to a Shop
	 * <p>
	 * With no variables sent will use the amount and data of the held item for cost
	 * </p>
	 * <p>
	 * If the player uses a int in the first variable they can change the amount for the item they are holding
	 * </p>
	 * <p>
	 * With 2 variables used the player can use an amount and material to set the sign instead of a held item
	 * </p>
	 */
	public void addCost() {
		Shop shop = findShop();

		if(shop == null)
			return;

		int amount = 0;
		Material mat = null;

		if (command.hasArgAt(1) && isInt(command.getArgAt(1))) {
			amount = Integer.parseInt(command.getArgAt(1));
		}

		if (command.hasArgAt(2) && Material.getMaterial(command.getArgAt(2).toUpperCase()) != null) {
			mat = Material.getMaterial(command.getArgAt(2).toUpperCase());
		}

		if (!(shop.getOwner().getUUID().equals(pSender.getUniqueId()) || shop.getManagersUUID().contains(pSender.getUniqueId()))) {
			sendMessage(Message.NO_SHOP_PERMISSION.getPrefixed());
			return;
		}

		ItemStack itemInHand;

		if (mat == null) {
			itemInHand = pSender.getInventory().getItemInMainHand().clone();
		} else {
			itemInHand = new ItemStack(mat, 1);
		}

		if (itemInHand.getType() == Material.AIR) {
			sendMessage(Message.HELD_EMPTY.getPrefixed());
			return;
		}

		if (!isValidType(itemInHand.getType().toString())) {
			sendMessage(Message.ILLEGAL_ITEM.getPrefixed());
			return;
		}

		if (amount > 0) {
			itemInHand.setAmount(amount);
		}

		shop.setCost(itemInHand);
		shop.saveShop();
		shop.updateSign();

		sendMessage(Message.ITEM_ADDED.getPrefixed());
	}

	/**
	 * Sets the shop to the open status allowing trades to happen
	 */
	public void open() {
		Shop shop = findShop();

		if (shop == null)
			return;

		if (!(shop.getOwner().getUUID().equals(pSender.getUniqueId()) || shop.getManagersUUID().contains(pSender.getUniqueId()))) {
			sendMessage(Message.NO_EDIT.getPrefixed());
			return;
		}

		if (shop.missingItems()) {
			sendMessage(Message.MISSING_ITEM.getPrefixed());
			return;
		}

		boolean opened = shop.setOpen();
		shop.saveShop();

		if (opened) {
			sendMessage(Message.CHANGE_OPEN.getPrefixed());
		} else {
			sendMessage(Message.MISSING_ITEM.getPrefixed());
		}
	}

	/**
	 * Sets the shop to the close status preventing trades from happen
	 */
	public void close() {
		Shop shop = findShop();

		if (shop == null)
			return;

		if (!(shop.getOwner().getUUID().equals(pSender.getUniqueId()) || shop.getManagersUUID().contains(pSender.getUniqueId()))) {
			sendMessage(Message.NO_EDIT.getPrefixed());
			return;
		}

		shop.setClosed();
		shop.saveShop();
		shop.updateSign();

		sendMessage(Message.CHANGE_CLOSED.getPrefixed());
	}

	/**
	 * Switches the shop type between BiTrade and Trade
	 */
	public void switchShop() {
		Shop shop = findShop();

		if (shop == null)
			return;

		if (!(shop.getOwner().getUUID().equals(pSender.getUniqueId()) || shop.getManagersUUID().contains(pSender.getUniqueId()))) {
			sendMessage(Message.NO_EDIT.getPrefixed());
			return;
		}

		shop.switchType();

		sendMessage(Message.SHOP_TYPE_SWITCHED.getPrefixed().replace("%newtype%", shop.getShopType().toHeader()));
	}

	/**
	 * Opens a GUI containing the items to be traded at the shop the player is looking at
	 */
	public void what() {
		Shop shop = findShop();

		if (shop == null)
			return;

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

		pSender.openInventory(shopContents);
	}

	/**
	 * Tells the player who the Owner/Managers/Members that are on the shop are
	 */
	public void who() {
		String owner = "";
		StringBuilder managers = new StringBuilder();
		StringBuilder members = new StringBuilder();
		Shop shop = findShop();

		if (shop == null)
			return;

		if (shop.getShopType().isITrade()) {
			sendMessage(Message.WHO_MESSAGE.getPrefixed()
					.replace("{OWNER}", Setting.ITRADESHOP_OWNER.getString())
					.replace("{MANAGERS}", "None")
					.replace("{MEMBERS}", "None"));
			return;
		}

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
		sendMessage(Message.WHO_MESSAGE.getPrefixed()
				.replace("{OWNER}", owner)
				.replace("{MANAGERS}", managers.toString())
				.replace("{MEMBERS}", members.toString()));
	}

	/**
	 * Adds the specified player to the shop as a manager
	 */
	public void addManager() {
		Shop shop = findShop();

		if(shop == null)
			return;

		if (!shop.getOwner().getUUID().equals(pSender.getUniqueId())) {
			sendMessage(Message.NO_SHOP_PERMISSION.getPrefixed());
			return;
		}

		OfflinePlayer target = Bukkit.getOfflinePlayer(command.getArgAt(1));
		if (!target.hasPlayedBefore()) {
			sendMessage(Message.PLAYER_NOT_FOUND.getPrefixed());
			return;
		}

		shop.addManager(new ShopUser(target, ShopRole.MANAGER));
		if (!shop.getMembersUUID().contains(target.getUniqueId())) {
			shop.removeMember(new ShopUser(target, ShopRole.MEMBER));
		}

		shop.saveShop();
		shop.updateSign();
		sendMessage(Message.UPDATED_SHOP_MEMBERS.getPrefixed());
	}

	/**
	 * Removes the specified player from the shop if they currently are a manager
	 */
	public void removeManager() {
		Shop shop = findShop();

		if(shop == null)
			return;

		if (!shop.getOwner().getUUID().equals(pSender.getUniqueId())) {
			sendMessage(Message.NO_SHOP_PERMISSION.getPrefixed());
			return;
		}

		OfflinePlayer target = Bukkit.getOfflinePlayer(command.getArgAt(1));
		if (!target.hasPlayedBefore()) {
			sendMessage(Message.PLAYER_NOT_FOUND.getPrefixed());
			return;
		}

		boolean removed = shop.removeManager(new ShopUser(target, ShopRole.MANAGER));
		if (!removed) {
			sendMessage(Message.UNSUCCESSFUL_SHOP_MEMBERS.getPrefixed());
			return;
		}

		shop.saveShop();
		shop.updateSign();
		sendMessage(Message.UPDATED_SHOP_MEMBERS.getPrefixed());
	}

	/**
	 * Adds the specified player to the shop as a member
	 */
	public void addMember() {
		Shop shop = findShop();

		if(shop == null)
			return;

		if (!shop.getOwner().getUUID().equals(pSender.getUniqueId())) {
			sendMessage(Message.NO_SHOP_PERMISSION.getPrefixed());
			return;
		}

		OfflinePlayer target = Bukkit.getOfflinePlayer(command.getArgAt(1));
		if (!target.hasPlayedBefore()) {
			sendMessage(Message.PLAYER_NOT_FOUND.getPrefixed());
			return;
		}

		if (!shop.getManagersUUID().contains(target.getUniqueId())) {
			shop.addMember(new ShopUser(target, ShopRole.MEMBER));
		}

		shop.saveShop();
		shop.updateSign();
		sendMessage(Message.UPDATED_SHOP_MEMBERS.getPrefixed());
	}

	/**
	 * Removes the specified player from the shop if they are a member
	 */
	public void removeMember() {
		Shop shop = findShop();

		if(shop == null)
			return;

		if (!shop.getOwner().getUUID().equals(pSender.getUniqueId()) || !shop.getManagersUUID().contains(pSender.getUniqueId())) {
			sendMessage(Message.NO_SHOP_PERMISSION.getPrefixed());
			return;
		}

		OfflinePlayer target = Bukkit.getOfflinePlayer(command.getArgAt(1));
		if (!target.hasPlayedBefore()) {
			sendMessage(Message.PLAYER_NOT_FOUND.getPrefixed());
			return;
		}

		boolean removed = shop.removeMember(new ShopUser(target, ShopRole.MEMBER));
		if (!removed) {
			sendMessage(Message.UNSUCCESSFUL_SHOP_MEMBERS.getPrefixed());
			return;
		}


		shop.saveShop();
		shop.updateSign();
		sendMessage(Message.UPDATED_SHOP_MEMBERS.getPrefixed());
	}

	/**
	 * Returns the Shop the player is looking at
	 *
	 * @return null if Shop is not found, Shop object if it is
	 */
	private Shop findShop() {
		Block b = pSender.getTargetBlock(null, Setting.MAX_EDIT_DISTANCE.getInt());

		try {
			if (b.getType() == Material.AIR)
				throw new NoSuchFieldException();

			if (ShopType.isShop(b)) {
				return Shop.loadShop((Sign) b.getState());

			} else if (plugin.getListManager().isInventory(b.getType()) &&
					((InventoryHolder) b.getState()).getInventory().getName().contains("$ ^Sign:l_")) {

				ShopChest shopChest = new ShopChest(b.getLocation());
				return Shop.loadShop(shopChest.getShopSign());
			} else
				throw new NoSuchFieldException();
		} catch (NoSuchFieldException nsfE) {
			sendMessage(Message.NO_SIGHTED_SHOP.getPrefixed());
			return null;
		}
	}
}