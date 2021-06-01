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

import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.enumys.*;
import org.shanerx.tradeshop.framework.ShopChange;
import org.shanerx.tradeshop.framework.events.PlayerShopChangeEvent;
import org.shanerx.tradeshop.framework.events.PlayerShopCloseEvent;
import org.shanerx.tradeshop.framework.events.PlayerShopOpenEvent;
import org.shanerx.tradeshop.objects.*;
import org.shanerx.tradeshop.utils.ObjectHolder;
import org.shanerx.tradeshop.utils.Utils;
import org.shanerx.tradeshop.utils.data.DataType;

public class CommandRunner extends Utils {

	protected final TradeShop plugin;
	protected final CommandPass command;
	protected Player pSender;

	protected final GuiPageElement PREV_BUTTON = new GuiPageElement('p', new ItemStack(Material.POTION), GuiPageElement.PageAction.PREVIOUS, "Go to previous page (%prevpage%)"),
			NEXT_BUTTON = new GuiPageElement('n', new ItemStack(Material.SPLASH_POTION), GuiPageElement.PageAction.NEXT, "Go to next page (%nextpage%)");
	protected final StaticGuiElement CANCEL_BUTTON = new StaticGuiElement('c', new ItemStack(Material.END_CRYSTAL), click3 -> {
		InventoryGui.goBack(pSender);
		return true;
	}, "Cancel Changes"),
			BACK_BUTTON = new StaticGuiElement('b', new ItemStack(Material.END_CRYSTAL), click3 -> {
				InventoryGui.goBack(pSender);
				return true;
			}, "Back");
	protected final String[] MENU_LAYOUT = {"a b c"},
			EDIT_LAYOUT = {"aggggggga", "ap c s na"},
			ITEM_LAYOUT = {"aggggggga", "aggggggga", "a  cbs  a"},
			WHAT_MENU = {"141125333", "1aaa2bbb3", "11p123n33"};

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
			if (c.checkPerm(command.getSender()) == PermStatus.GOOD) {
				sb.append(Message.colour(String.format("&b/ts %s  &f %s\n", c.getFirstName(), c.getDescription())));
			}
		}

		sb.append("\n ");
		sendMessage(colorize(sb.toString()));
	}

	public void usage(String subcmd) {
		Commands cmd = Commands.getType(subcmd);
		if (cmd == null) {
			sendMessage(Message.colour(String.format("&4Cannot find usages for &c%s&r", subcmd)));
			return;
		}
		sendMessage(Message.colour(String.format("&6Showing help for &c%s&r\n&bUsage:&e %s \n&bAliases: %s\n&bDescription:&e %s", subcmd, cmd.getUsage(), cmd.getAliases(), cmd.getDescription())));
	}

	/**
	 * Sends the sender the bug message
	 */
	public void bugs() {
		sendMessage("\n&a[&eTradeShop&a] \n&2To report any bugs to the author, either send a PM on &cSpigot &2- &egoo.gl/s6Jk23 &2or open an issue on &cGitHub &2-&e goo.gl/X4qqyg\n");
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
		plugin.getListManager().reload();
		Message.reload();
		Setting.reload();
		plugin.getDebugger().reload();
		try {
			plugin.getDataStorage().reload(DataType.valueOf(Setting.DATA_STORAGE_TYPE.getString().toUpperCase()));
		} catch (IllegalArgumentException iae) {
			debugger.log("Config value for data storage set to an invalid value: " + Setting.DATA_STORAGE_TYPE.getString(), DebugLevels.DATA_ERROR);
			debugger.log("TradeShop will now disable...", DebugLevels.DATA_ERROR);
			plugin.getServer().getPluginManager().disablePlugin(plugin);
			return;
		}
		sendMessage(Setting.MESSAGE_PREFIX.getString() + "&6The configuration files have been reloaded!");
	}

	/**
	 * Lists products with their index
	 */
	public void listProduct() {
		Shop shop = findShop();

		if (shop == null)
			return;

		StringBuilder sb = new StringBuilder();
		int counter = 1;

        for (ShopItemStack itm : shop.getProduct()) {
            sb.append(String.format("&b[&f%d&b]    &2- &f%s\n", counter, itm.getItemStack().hasItemMeta() && itm.getItemStack().getItemMeta().hasDisplayName() ? itm.getItemStack().getItemMeta().getDisplayName() : itm.getItemStack().getType().toString()));
			counter++;
		}

		sendMessage(Message.SHOP_ITEM_LIST.getPrefixed().replaceAll("%type%", "products").replaceAll("%list%", sb.toString()));
	}

	/**
	 * Lists costs with their index
	 */
	public void listCost() {
		Shop shop = findShop();

		if (shop == null)
			return;

		StringBuilder sb = new StringBuilder();
		int counter = 1;

        for (ShopItemStack itm : shop.getCost()) {
            sb.append(String.format("&b[&f%d&b]    &2- &f%s\n", counter, itm.getItemStack().hasItemMeta() && itm.getItemStack().getItemMeta().hasDisplayName() ? itm.getItemStack().getItemMeta().getDisplayName() : itm.getItemStack().getType().toString()));
			counter++;
		}

		sendMessage(Message.SHOP_ITEM_LIST.getPrefixed().replaceAll("%type%", "products").replaceAll("%list%", sb.toString()));
	}

	/**
	 * Removes product at index
	 */
	public void removeProduct() {
		Shop shop = findShop();

		if (shop == null)
			return;

		int index = 0;

		if (isInt(command.getArgAt(1))) {
			index = Integer.parseInt(command.getArgAt(1)) - 1;
		} else {
			sendMessage(Message.INVALID_ARGUMENTS.getPrefixed());
			return;
		}

		if (!(shop.getOwner().getUUID().equals(pSender.getUniqueId()) || shop.getManagersUUID().contains(pSender.getUniqueId()))) {
			sendMessage(Message.NO_SHOP_PERMISSION.getPrefixed());
			return;
		}
		
		PlayerShopChangeEvent changeEvent = new PlayerShopChangeEvent(pSender, shop, ShopChange.REMOVE_PRODUCT, new ObjectHolder<Integer>(index));
		Bukkit.getPluginManager().callEvent(changeEvent);
		if (changeEvent.isCancelled()) return;
		
		if (shop.removeProduct(index))
			sendMessage(Message.ITEM_REMOVED.getPrefixed());
		else
			sendMessage(Message.ITEM_NOT_REMOVED.getPrefixed());
	}

	/**
	 * Removes cost at index
	 */
	public void removeCost() {
		Shop shop = findShop();

		if (shop == null)
			return;

		int index;

		if (isInt(command.getArgAt(1))) {
			index = Integer.parseInt(command.getArgAt(1)) - 1;
		} else {
			sendMessage(Message.INVALID_ARGUMENTS.getPrefixed());
			return;
		}

		if (!(shop.getOwner().getUUID().equals(pSender.getUniqueId()) || shop.getManagersUUID().contains(pSender.getUniqueId()))) {
			sendMessage(Message.NO_SHOP_PERMISSION.getPrefixed());
			return;
		}
		
		PlayerShopChangeEvent changeEvent = new PlayerShopChangeEvent(pSender, shop, ShopChange.REMOVE_COST, new ObjectHolder<Integer>(index));
		Bukkit.getPluginManager().callEvent(changeEvent);
		if (changeEvent.isCancelled()) return;
		
		if (shop.removeCost(index))
			sendMessage(Message.ITEM_REMOVED.getPrefixed());
		else
			sendMessage(Message.ITEM_NOT_REMOVED.getPrefixed());
	}

	/**
	 * Sets a product to a Shop
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
	public void setProduct() {
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

		if (!isValidType(itemInHand.getType())) {
			sendMessage(Message.ILLEGAL_ITEM.getPrefixed());
			return;
		}

		if (amount > 0) {
			itemInHand.setAmount(amount);
		}

		if (Math.ceil((double) itemInHand.getAmount() / (double) itemInHand.getMaxStackSize()) > Setting.MAX_ITEMS_PER_TRADE_SIDE.getInt()) {
			sendMessage(Message.TOO_MANY_ITEMS.getPrefixed().replaceAll("%side%", "products"));
			return;
		}
		
		PlayerShopChangeEvent changeEvent = new PlayerShopChangeEvent(pSender, shop, ShopChange.SET_PRODUCT, new ObjectHolder<ItemStack>(itemInHand));
		Bukkit.getPluginManager().callEvent(changeEvent);
		if (changeEvent.isCancelled()) return;
		
		shop.setProduct(itemInHand);

		sendMessage(Message.ITEM_ADDED.getPrefixed());
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

		if (!isValidType(itemInHand.getType())) {
			sendMessage(Message.ILLEGAL_ITEM.getPrefixed());
			return;
		}

		if (amount > 0) {
			itemInHand.setAmount(amount);
		}

		if (shop.getProduct().size() + Math.ceil((double) itemInHand.getAmount() / (double) itemInHand.getMaxStackSize()) > Setting.MAX_ITEMS_PER_TRADE_SIDE.getInt()) {
			sendMessage(Message.TOO_MANY_ITEMS.getPrefixed().replaceAll("%side%", "products"));
			return;
		}
		
		PlayerShopChangeEvent changeEvent = new PlayerShopChangeEvent(pSender, shop, ShopChange.ADD_PRODUCT, new ObjectHolder<ItemStack>(itemInHand));
		Bukkit.getPluginManager().callEvent(changeEvent);
		if (changeEvent.isCancelled()) return;
		
		shop.addProduct(itemInHand);

		sendMessage(Message.ITEM_ADDED.getPrefixed());
	}

	/**
	 * Sets the item to the shop
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
	public void setCost() {
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

		if (!isValidType(itemInHand.getType())) {
			sendMessage(Message.ILLEGAL_ITEM.getPrefixed());
			return;
		}

		if (itemInHand.getType().toString().endsWith("SHULKER_BOX") && shop.getInventoryLocation().getBlock().getType().toString().endsWith("SHULKER_BOX")) {
			sendMessage(Message.NO_SHULKER_COST.getPrefixed());
			return;
		}

		if (amount > 0) {
			itemInHand.setAmount(amount);
		}

		if (Math.ceil((double) itemInHand.getAmount() / (double) itemInHand.getMaxStackSize()) > Setting.MAX_ITEMS_PER_TRADE_SIDE.getInt()) {
			sendMessage(Message.TOO_MANY_ITEMS.getPrefixed().replaceAll("%side%", "costs"));
			return;
		}
		
		PlayerShopChangeEvent changeEvent = new PlayerShopChangeEvent(pSender, shop, ShopChange.SET_COST, new ObjectHolder<ItemStack>(itemInHand));
		Bukkit.getPluginManager().callEvent(changeEvent);
		if (changeEvent.isCancelled()) return;
		
		shop.setCost(itemInHand);

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

		if (!isValidType(itemInHand.getType())) {
			sendMessage(Message.ILLEGAL_ITEM.getPrefixed());
			return;
		}

		if (itemInHand.getType().toString().endsWith("SHULKER_BOX") && shop.getInventoryLocation().getBlock().getType().toString().endsWith("SHULKER_BOX")) {
			sendMessage(Message.NO_SHULKER_COST.getPrefixed());
			return;
		}

		if (amount > 0) {
			itemInHand.setAmount(amount);
		}

		if (shop.getCost().size() + Math.ceil((double) itemInHand.getAmount() / (double) itemInHand.getMaxStackSize()) > Setting.MAX_ITEMS_PER_TRADE_SIDE.getInt()) {
			sendMessage(Message.TOO_MANY_ITEMS.getPrefixed().replaceAll("%side%", "costs"));
			return;
		}
		
		PlayerShopChangeEvent changeEvent = new PlayerShopChangeEvent(pSender, shop, ShopChange.ADD_COST, new ObjectHolder<ItemStack>(itemInHand));
		Bukkit.getPluginManager().callEvent(changeEvent);
		if (changeEvent.isCancelled()) return;
		
		shop.addCost(itemInHand);

		sendMessage(Message.ITEM_ADDED.getPrefixed());
	}

	/**
	 * Sets the shop to the open status allowing trades to happen
	 */
	public void open() {
		Shop shop = findShop();

		if (shop == null)
			return;

		if (!(shop.getOwner().getUUID().equals(pSender.getUniqueId()) ||
				shop.getManagersUUID().contains(pSender.getUniqueId()) ||
				Permissions.hasPermission(pSender, Permissions.ADMIN))) {
			sendMessage(Message.NO_EDIT.getPrefixed());
			return;
		}
		
		PlayerShopOpenEvent event = new PlayerShopOpenEvent(pSender, shop);
		if (event.isCancelled()) return;

        ShopStatus status = shop.setOpen();

        switch (status) {
            case OPEN:
                sendMessage(Message.CHANGE_OPEN.getPrefixed());
                break;
            case INCOMPLETE:
                if (shop.isMissingItems())
                    sendMessage(Message.MISSING_ITEM.getPrefixed());
                else if (shop.getChestAsSC() == null)
                    sendMessage(Message.MISSING_CHEST.getPrefixed());
                break;
            case OUT_OF_STOCK:
                sendMessage(Message.SHOP_EMPTY.getPrefixed());
                break;
		}
	}

	/**
	 * Sets the shop to the close status preventing trades from happen
	 */
	public void close() {
		Shop shop = findShop();

		if (shop == null)
			return;

		if (!(shop.getOwner().getUUID().equals(pSender.getUniqueId()) ||
				shop.getManagersUUID().contains(pSender.getUniqueId()) ||
				Permissions.hasPermission(pSender, Permissions.ADMIN))) {
			sendMessage(Message.NO_EDIT.getPrefixed());
			return;
		}
		
		PlayerShopCloseEvent event = new PlayerShopCloseEvent(pSender, shop);
		if (event.isCancelled()) return;

        shop.setStatus(ShopStatus.CLOSED);
        shop.updateSign();
		shop.saveShop();

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

		if (shop == null)
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

		if (shop.getUsersUUID().contains(target.getUniqueId())) {
			sendMessage(Message.UNSUCCESSFUL_SHOP_MEMBERS.getPrefixed());
			return;
		}
		
		PlayerShopChangeEvent changeEvent = new PlayerShopChangeEvent(pSender, shop, ShopChange.ADD_MANAGER, new ObjectHolder<OfflinePlayer>(target));
		Bukkit.getPluginManager().callEvent(changeEvent);
		if (changeEvent.isCancelled()) return;
		
		shop.addManager(target.getUniqueId());

		sendMessage(Message.UPDATED_SHOP_MEMBERS.getPrefixed());
	}

	/**
	 * Removes the specified player from the shop if they currently are a manager
	 */
	public void removeUser() {
		Shop shop = findShop();

		if (shop == null)
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
		
		PlayerShopChangeEvent changeEvent = new PlayerShopChangeEvent(pSender, shop, ShopChange.REMOVE_USER, new ObjectHolder<OfflinePlayer>(target));
		Bukkit.getPluginManager().callEvent(changeEvent);
		if (changeEvent.isCancelled()) return;
		
		if (!shop.removeUser(target.getUniqueId())) {
			sendMessage(Message.UNSUCCESSFUL_SHOP_MEMBERS.getPrefixed());
			return;
		}
		
		sendMessage(Message.UPDATED_SHOP_MEMBERS.getPrefixed());
	}

	/**
	 * Adds the specified player to the shop as a member
	 */
	public void addMember() {
		Shop shop = findShop();

		if (shop == null)
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


		if (shop.getUsersUUID().contains(target.getUniqueId())) {
			sendMessage(Message.UNSUCCESSFUL_SHOP_MEMBERS.getPrefixed());
			return;
		}

		PlayerShopChangeEvent changeEvent = new PlayerShopChangeEvent(pSender, shop, ShopChange.ADD_MEMBER, new ObjectHolder<OfflinePlayer>(target));
		Bukkit.getPluginManager().callEvent(changeEvent);
		if (changeEvent.isCancelled()) return;

		shop.addMember(target.getUniqueId());

		sendMessage(Message.UPDATED_SHOP_MEMBERS.getPrefixed());
	}

	/**
	 * Changes the players trade multiplier for current login
	 */
	public void multi() {
        if (!Setting.ALLOW_MULTI_TRADE.getBoolean()) {
            sendMessage(Message.FEATURE_DISABLED.getPrefixed());
            return;
        }

        PlayerSetting playerSetting = plugin.getDataStorage().loadPlayer(pSender.getUniqueId());

		if (command.argsSize() == 1) {
			sendMessage(Message.MULTI_AMOUNT.getPrefixed().replaceAll("%amount%", String.valueOf(playerSetting.getMulti())));
		} else {
            int amount = Setting.MULTI_TRADE_DEFAULT.getInt();

			if (isInt(command.getArgAt(1)))
				amount = Integer.parseInt(command.getArgAt(1));

			if (amount < 2)
				amount = 2;
			else if (amount > Setting.MULTI_TRADE_MAX.getInt())
				amount = Setting.MULTI_TRADE_MAX.getInt();

			playerSetting.setMulti(amount);
			plugin.getDataStorage().savePlayer(playerSetting);

			sendMessage(Message.MULTI_UPDATE.getPrefixed().replaceAll("%amount%", String.valueOf(amount)));
		}
	}

	public void toggleStatus() {
		if (!Setting.ALLOW_TOGGLE_STATUS.getBoolean()) {
			sendMessage(Message.FEATURE_DISABLED.getPrefixed());
			return;
		}

		PlayerSetting playerSetting = plugin.getDataStorage().loadPlayer(pSender.getUniqueId());
		playerSetting.setShowInvolvedStatus(!playerSetting.showInvolvedStatus());
		plugin.getDataStorage().savePlayer(playerSetting);
		sendMessage(Message.TOGGLED_STATUS.getPrefixed().replace("%status%", playerSetting.showInvolvedStatus() ? "on" : "off"));
	}

	/**
	 * Changes/Sets the players permission level if internal permissions is enabled
	 */
	public void playerLevel() {
		if (Bukkit.getOfflinePlayer(command.getArgAt(1)).hasPlayedBefore()) {
			PlayerSetting playerSetting = plugin.getDataStorage().loadPlayer(Bukkit.getOfflinePlayer(command.getArgAt(1)).getUniqueId());
			if (command.argsSize() == 2) {
				sendMessage(Message.VIEW_PLAYER_LEVEL.getMessage()
						.replace("%player%", Bukkit.getOfflinePlayer(command.getArgAt(1)).getName())
						.replace("%level%", playerSetting.getType() + ""));
			} else {
				if (isInt(command.getArgAt(2))) {
					int newLevel = Integer.parseInt(command.getArgAt(2));

					playerSetting.setType(newLevel);
					plugin.getDataStorage().savePlayer(playerSetting);

					sendMessage(Message.SET_PLAYER_LEVEL.getMessage()
							.replace("%player%", Bukkit.getOfflinePlayer(command.getArgAt(1)).getName())
							.replace("%level%", playerSetting.getType() + ""));
				} else {
					sendMessage(Message.INVALID_ARGUMENTS.getMessage());
				}
			}
		} else {
			sendMessage(Message.PLAYER_NOT_FOUND.getMessage());
		}
	}

	/**
	 * Changes/Sets the players permission level if internal permissions is enabled
	 */
	public void status() {
		if (command.hasArgAt(1)) {
			if (!Permissions.hasPermission(pSender, Permissions.ADMIN)) {
				Message.NO_COMMAND_PERMISSION.sendMessage(pSender);
				return;
			}
			if (Bukkit.getOfflinePlayer(command.getArgAt(1)).hasPlayedBefore()) {
				plugin.getDataStorage().loadPlayer(Bukkit.getOfflinePlayer(command.getArgAt(1)).getUniqueId())
						.getInvolvedStatusesInventory().show(pSender.getPlayer());
			} else {
				sendMessage(Message.PLAYER_NOT_FOUND.getMessage());
			}
		} else {
			plugin.getDataStorage().loadPlayer(pSender.getUniqueId()).getInvolvedStatusesInventory().show(pSender.getPlayer());
		}
	}

	/**
	 * Returns the Shop the player is looking at
	 *
	 * @return null if Shop is not found, Shop object if it is
	 */
	protected Shop findShop() {
		if (pSender == null) {
			sendMessage(Message.PLAYER_ONLY_COMMAND.getPrefixed());
			return null;
		}

		Block b = pSender.getTargetBlockExact(Setting.MAX_EDIT_DISTANCE.getInt());
		try {
			if (b == null)
				throw new NoSuchFieldException();

			if (ShopType.isShop(b)) {
				return Shop.loadShop((Sign) b.getState());

            } else if (ShopChest.isShopChest(b)) {
				if (plugin.getDataStorage().getChestLinkage(new ShopLocation(b.getLocation())) != null)
					return plugin.getDataStorage().loadShopFromStorage(new ShopLocation(b.getLocation()));

				return Shop.loadShop(new ShopChest(b.getLocation()).getShopSign());

			} else
				throw new NoSuchFieldException();

		} catch (NoSuchFieldException ex) {
			sendMessage(Message.NO_SIGHTED_SHOP.getPrefixed());
			return null;
		}
	}
}