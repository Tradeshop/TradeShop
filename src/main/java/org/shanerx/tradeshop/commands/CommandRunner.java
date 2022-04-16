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
import org.shanerx.tradeshop.enumys.DebugLevels;
import org.shanerx.tradeshop.enumys.PermStatus;
import org.shanerx.tradeshop.enumys.Permissions;
import org.shanerx.tradeshop.enumys.ShopRole;
import org.shanerx.tradeshop.enumys.ShopStatus;
import org.shanerx.tradeshop.enumys.ShopType;
import org.shanerx.tradeshop.framework.ShopChange;
import org.shanerx.tradeshop.framework.events.PlayerShopChangeEvent;
import org.shanerx.tradeshop.framework.events.PlayerShopCloseEvent;
import org.shanerx.tradeshop.framework.events.PlayerShopCreateEvent;
import org.shanerx.tradeshop.framework.events.PlayerShopOpenEvent;
import org.shanerx.tradeshop.framework.events.TradeShopReloadEvent;
import org.shanerx.tradeshop.objects.IllegalItemList;
import org.shanerx.tradeshop.objects.PlayerSetting;
import org.shanerx.tradeshop.objects.Shop;
import org.shanerx.tradeshop.objects.ShopChest;
import org.shanerx.tradeshop.objects.ShopItemStack;
import org.shanerx.tradeshop.objects.ShopLocation;
import org.shanerx.tradeshop.objects.ShopUser;
import org.shanerx.tradeshop.utils.ObjectHolder;
import org.shanerx.tradeshop.utils.Tuple;
import org.shanerx.tradeshop.utils.Utils;
import org.shanerx.tradeshop.utils.config.Message;
import org.shanerx.tradeshop.utils.config.Setting;
import org.shanerx.tradeshop.utils.config.Variable;
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
				sb.append(plugin.getMessageManager().colour(String.format("&b/ts %s  &f %s\n", c.getFirstName(), c.getDescription())));
			}
		}

		sb.append("\n ");
		command.sendMessage(colorize(sb.toString()));
	}

	public void usage(String subcmd) {
		Commands cmd = Commands.getType(subcmd);
		if (cmd == null) {
			command.sendMessage(plugin.getMessageManager().colour(String.format("&4Cannot find usages for &c%s&r", subcmd)));
			return;
		}
		command.sendMessage(plugin.getMessageManager().colour(String.format("&6Showing help for &c%s&r\n&bUsage:&e %s \n&bAliases: %s\n&bDescription:&e %s", subcmd, cmd.getUsage(), cmd.getAliases(), cmd.getDescription())));
	}

	/**
	 * Sends the sender the bug message
	 */
	public void bugs() {
		command.sendMessage("\n&a[&eTradeShop&a] \n&2To report any bugs to the author, either send a PM on &cSpigot &2- &egoo.gl/s6Jk23 &2or open an issue on &cGitHub &2-&e goo.gl/X4qqyg\n");
	}

	/**
	 * Sends the sender the setup message
	 */
	public void setup() {
		Message.SETUP_HELP.sendMessage(pSender, new Tuple<>(Variable.HEADER.toString(), Setting.TRADESHOP_HEADER.getString()));
	}

	/**
	 * Reloads the plugin and sends success message
	 */
	public void reload() {
		plugin.getLanguage().reload();
		plugin.getSettingManager().reload();
		plugin.getMessageManager().reload();
		plugin.getListManager().reload();
		plugin.getDebugger().reload();
		try {
			plugin.getDataStorage().reload(DataType.valueOf(Setting.DATA_STORAGE_TYPE.getString().toUpperCase()));
		} catch (IllegalArgumentException iae) {
			debugger.log("Config value for data storage set to an invalid value: " + Setting.DATA_STORAGE_TYPE.getString(), DebugLevels.DATA_ERROR);
			debugger.log("TradeShop will now disable...", DebugLevels.DATA_ERROR);
			plugin.getServer().getPluginManager().disablePlugin(plugin);
			return;
		}

		command.sendMessage(Setting.MESSAGE_PREFIX.getString() + "&6The configuration files have been reloaded!");
		Bukkit.getPluginManager().callEvent(new TradeShopReloadEvent(plugin, command.getSender()));
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
			sb.append(String.format("&b[&f%d&b]    &2- &f%s\n", counter, itm.getCleanItemName()));
			counter++;
		}

		Message.SHOP_ITEM_LIST.sendMessage(pSender, new Tuple<>(Variable.TYPE.toString(), "products"), new Tuple<>(Variable.LIST.toString(), sb.toString()));
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
			sb.append(String.format("&b[&f%d&b]    &2- &f%s\n", counter, itm.getCleanItemName()));
			counter++;
		}

		Message.SHOP_ITEM_LIST.sendMessage(pSender, new Tuple<>(Variable.TYPE.toString(), "costs"), new Tuple<>(Variable.LIST.toString(), sb.toString()));
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
			Message.INVALID_ARGUMENTS.sendMessage(pSender);
			return;
		}

		if (!(shop.getOwner().getUUID().equals(pSender.getUniqueId())
				|| shop.getManagersUUID().contains(pSender.getUniqueId())
				|| (Setting.UNLIMITED_ADMIN.getBoolean() && Permissions.isAdminEnabled(pSender)))) {
			Message.NO_SHOP_PERMISSION.sendMessage(pSender);
			return;
		}

		PlayerShopChangeEvent changeEvent = new PlayerShopChangeEvent(pSender, shop, ShopChange.REMOVE_PRODUCT, new ObjectHolder<Integer>(index));
		Bukkit.getPluginManager().callEvent(changeEvent);
		if (changeEvent.isCancelled()) return;

		if (shop.removeProduct(index))
			Message.ITEM_REMOVED.sendMessage(pSender);
		else
			Message.ITEM_NOT_REMOVED.sendMessage(pSender);
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
			Message.INVALID_ARGUMENTS.sendMessage(pSender);
			return;
		}

		if (!(shop.getOwner().getUUID().equals(pSender.getUniqueId())
				|| shop.getManagersUUID().contains(pSender.getUniqueId())
				|| (Setting.UNLIMITED_ADMIN.getBoolean() && Permissions.isAdminEnabled(pSender)))) {
			Message.NO_SHOP_PERMISSION.sendMessage(pSender);
			return;
		}

		PlayerShopChangeEvent changeEvent = new PlayerShopChangeEvent(pSender, shop, ShopChange.REMOVE_COST, new ObjectHolder<Integer>(index));
		Bukkit.getPluginManager().callEvent(changeEvent);
		if (changeEvent.isCancelled()) return;

		if (shop.removeCost(index))
			Message.ITEM_REMOVED.sendMessage(pSender);
		else
			Message.ITEM_NOT_REMOVED.sendMessage(pSender);
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

		if (!(shop.getOwner().getUUID().equals(pSender.getUniqueId())
				|| shop.getManagersUUID().contains(pSender.getUniqueId())
				|| (Setting.UNLIMITED_ADMIN.getBoolean() && Permissions.isAdminEnabled(pSender)))) {
			Message.NO_SHOP_PERMISSION.sendMessage(pSender);
			return;
		}

		ItemStack itemInHand;

		if (mat == null) {
			itemInHand = pSender.getInventory().getItemInMainHand().clone();
		} else {
			itemInHand = new ItemStack(mat, 1);
		}

		if (itemInHand.getType() == Material.AIR) {
			Message.HELD_EMPTY.sendMessage(pSender);
			return;
		}

		if (isIllegal(IllegalItemList.TradeItemType.PRODUCT, itemInHand.getType())) {
			Message.ILLEGAL_ITEM.sendMessage(pSender);
			return;
		}

		if (!(shop.getShopType().isITrade() && shop.getInventoryLocation() == null) && itemInHand.getType().toString().endsWith("SHULKER_BOX") && shop.getInventoryLocation().getBlock().getType().toString().endsWith("SHULKER_BOX")) {
			Message.NO_SHULKER_COST.sendMessage(pSender);
			return;
		}

		if (amount > 0) {
			itemInHand.setAmount(amount);
		}

		if (Math.ceil((double) itemInHand.getAmount() / (double) itemInHand.getMaxStackSize()) > Setting.MAX_ITEMS_PER_TRADE_SIDE.getInt()) {
			Message.TOO_MANY_ITEMS.sendMessage(pSender, new Tuple<>(Variable.SIDE.toString(), "product"));
			return;
		}

		PlayerShopChangeEvent changeEvent = new PlayerShopChangeEvent(pSender, shop, ShopChange.SET_PRODUCT, new ObjectHolder<ItemStack>(itemInHand));
		Bukkit.getPluginManager().callEvent(changeEvent);
		if (changeEvent.isCancelled()) return;

		shop.setProduct(itemInHand);

		Message.ITEM_ADDED.sendMessage(pSender);
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

		if (!(shop.getOwner().getUUID().equals(pSender.getUniqueId())
				|| shop.getManagersUUID().contains(pSender.getUniqueId())
				|| (Setting.UNLIMITED_ADMIN.getBoolean() && Permissions.isAdminEnabled(pSender)))) {
			Message.NO_SHOP_PERMISSION.sendMessage(pSender);
			return;
		}

		ItemStack itemInHand;

		if (mat == null) {
			itemInHand = pSender.getInventory().getItemInMainHand().clone();
		} else {
			itemInHand = new ItemStack(mat, 1);
		}

		if (itemInHand.getType() == Material.AIR) {
			Message.HELD_EMPTY.sendMessage(pSender);
			return;
		}

		if (isIllegal(IllegalItemList.TradeItemType.PRODUCT, itemInHand.getType())) {
			Message.ILLEGAL_ITEM.sendMessage(pSender);
			return;
		}

		if (!(shop.getShopType().isITrade() && shop.getInventoryLocation() == null) && itemInHand.getType().toString().endsWith("SHULKER_BOX") && shop.getInventoryLocation().getBlock().getType().toString().endsWith("SHULKER_BOX")) {
			Message.NO_SHULKER_COST.sendMessage(pSender);
			return;
		}

		if (amount > 0) {
			itemInHand.setAmount(amount);
		}

		if (shop.getProduct().size() + Math.ceil((double) itemInHand.getAmount() / (double) itemInHand.getMaxStackSize()) > Setting.MAX_ITEMS_PER_TRADE_SIDE.getInt()) {
			Message.TOO_MANY_ITEMS.sendMessage(pSender, new Tuple<>(Variable.SIDE.toString(), "product"));
			return;
		}

		PlayerShopChangeEvent changeEvent = new PlayerShopChangeEvent(pSender, shop, ShopChange.ADD_PRODUCT, new ObjectHolder<ItemStack>(itemInHand));
		Bukkit.getPluginManager().callEvent(changeEvent);
		if (changeEvent.isCancelled()) return;

		shop.addProduct(itemInHand);

		Message.ITEM_ADDED.sendMessage(pSender);
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

		if (!(shop.getOwner().getUUID().equals(pSender.getUniqueId())
				|| shop.getManagersUUID().contains(pSender.getUniqueId())
				|| (Setting.UNLIMITED_ADMIN.getBoolean() && Permissions.isAdminEnabled(pSender)))) {
			Message.NO_SHOP_PERMISSION.sendMessage(pSender);
			return;
		}

		ItemStack costItem;

		if (mat == null) {
			costItem = pSender.getInventory().getItemInMainHand().clone();
		} else {
			costItem = new ItemStack(mat, 1);
		}

		if (costItem.getType() == Material.AIR) {
			Message.HELD_EMPTY.sendMessage(pSender);
			return;
		}

		if (isIllegal(IllegalItemList.TradeItemType.COST, costItem.getType())) {
			Message.ILLEGAL_ITEM.sendMessage(pSender);
			return;
		}

		if (!(shop.getShopType().isITrade() && shop.getInventoryLocation() == null) && costItem.getType().toString().endsWith("SHULKER_BOX") && shop.getInventoryLocation().getBlock().getType().toString().endsWith("SHULKER_BOX")) {
			Message.NO_SHULKER_COST.sendMessage(pSender);
			return;
		}

		if (amount > 0) {
			costItem.setAmount(amount);
		}

		if (Math.ceil((double) costItem.getAmount() / (double) costItem.getMaxStackSize()) > Setting.MAX_ITEMS_PER_TRADE_SIDE.getInt()) {
			Message.TOO_MANY_ITEMS.sendMessage(pSender, new Tuple<>(Variable.SIDE.toString(), "cost"));
			return;
		}

		PlayerShopChangeEvent changeEvent = new PlayerShopChangeEvent(pSender, shop, ShopChange.SET_COST, new ObjectHolder<ItemStack>(costItem));
		Bukkit.getPluginManager().callEvent(changeEvent);
		if (changeEvent.isCancelled()) return;

		shop.setCost(costItem);

		Message.ITEM_ADDED.sendMessage(pSender);
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

		if (!(shop.getOwner().getUUID().equals(pSender.getUniqueId())
				|| shop.getManagersUUID().contains(pSender.getUniqueId())
				|| (Setting.UNLIMITED_ADMIN.getBoolean() && Permissions.isAdminEnabled(pSender)))) {
			Message.NO_SHOP_PERMISSION.sendMessage(pSender);
			return;
		}

		ItemStack itemInHand;

		if (mat == null) {
			itemInHand = pSender.getInventory().getItemInMainHand().clone();
		} else {
			itemInHand = new ItemStack(mat, 1);
		}

		if (itemInHand.getType() == Material.AIR) {
			Message.HELD_EMPTY.sendMessage(pSender);
			return;
		}

		if (isIllegal(IllegalItemList.TradeItemType.COST, itemInHand.getType())) {
			Message.ILLEGAL_ITEM.sendMessage(pSender);
			return;
		}

		if (!(shop.getShopType().isITrade() && shop.getInventoryLocation() == null) && itemInHand.getType().toString().endsWith("SHULKER_BOX") && shop.getInventoryLocation().getBlock().getType().toString().endsWith("SHULKER_BOX")) {
			Message.NO_SHULKER_COST.sendMessage(pSender);
			return;
		}

		if (amount > 0) {
			itemInHand.setAmount(amount);
		}

		if (shop.getCost().size() + Math.ceil((double) itemInHand.getAmount() / (double) itemInHand.getMaxStackSize()) > Setting.MAX_ITEMS_PER_TRADE_SIDE.getInt()) {
			Message.TOO_MANY_ITEMS.sendMessage(pSender, new Tuple<>(Variable.SIDE.toString(), "cost"));
			return;
		}

		PlayerShopChangeEvent changeEvent = new PlayerShopChangeEvent(pSender, shop, ShopChange.ADD_COST, new ObjectHolder<ItemStack>(itemInHand));
		Bukkit.getPluginManager().callEvent(changeEvent);
		if (changeEvent.isCancelled()) return;

		shop.addCost(itemInHand);

		Message.ITEM_ADDED.sendMessage(pSender);
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
				Permissions.isAdminEnabled(pSender))) {
			Message.NO_SHOP_PERMISSION.sendMessage(pSender);
			return;
		}

		PlayerShopOpenEvent event = new PlayerShopOpenEvent(pSender, shop);
		if (event.isCancelled()) return;

        ShopStatus status = shop.setOpen();

        switch (status) {
			case OPEN:
				Message.CHANGE_OPEN.sendMessage(pSender);
				break;
			case INCOMPLETE:
				if (shop.isMissingItems())
					Message.MISSING_ITEM.sendMessage(pSender);
				else if (shop.getChestAsSC() == null)
					Message.MISSING_CHEST.sendMessage(pSender);
				break;
			case OUT_OF_STOCK:
				Message.SHOP_EMPTY.sendMessage(pSender);
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
				Permissions.isAdminEnabled(pSender))) {
			Message.NO_SHOP_PERMISSION.sendMessage(pSender);
			return;
		}

		PlayerShopCloseEvent event = new PlayerShopCloseEvent(pSender, shop);
		if (event.isCancelled()) return;

		shop.setStatus(ShopStatus.CLOSED);
		shop.updateSign();
		shop.saveShop();

		Message.CHANGE_CLOSED.sendMessage(pSender);
	}

	/**
	 * Switches the shop type between BiTrade and Trade
	 */
	public void switchShop() {
		Shop shop = findShop();

		if (shop == null)
			return;

		if (!Permissions.hasPermission(pSender, Permissions.EDIT)) {
			Message.NO_COMMAND_PERMISSION.sendMessage(pSender);
			return;
		}

		switch (shop.getShopType()) {
			case TRADE:
				if (!Permissions.hasPermission(pSender, Permissions.CREATEBI)) {
					Message.NO_COMMAND_PERMISSION.sendMessage(pSender);
					return;
				}
			case BITRADE:
				if (!Permissions.hasPermission(pSender, Permissions.CREATE)) {
					Message.NO_COMMAND_PERMISSION.sendMessage(pSender);
					return;
				}
		}

		if (!(shop.getOwner().getUUID().equals(pSender.getUniqueId())
				|| shop.getManagersUUID().contains(pSender.getUniqueId())
				|| (Setting.UNLIMITED_ADMIN.getBoolean() && Permissions.isAdminEnabled(pSender)))) {
			Message.NO_SHOP_PERMISSION.sendMessage(pSender);
			return;
		}

		shop.switchType();

		Message.SHOP_TYPE_SWITCHED.sendMessage(pSender, new Tuple<>(Variable.NEW_TYPE.toString(), shop.getShopType().toHeader()));
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
			Message.WHO_MESSAGE.sendMessage(pSender,
					new Tuple<>(Variable.OWNER.toString(), Setting.ITRADESHOP_OWNER.getString()),
					new Tuple<>(Variable.MANAGERS.toString(), "None"),
					new Tuple<>(Variable.MEMBERS.toString(), "None"));
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
		Message.WHO_MESSAGE.sendMessage(pSender,
				new Tuple<>(Variable.OWNER.toString(), owner),
				new Tuple<>(Variable.MANAGERS.toString(), managers.toString()),
				new Tuple<>(Variable.MEMBERS.toString(), members.toString()));
	}

	/**
	 * Adds the specified player to the shop as a manager
	 */
	public void addManager() {
		Shop shop = findShop();

		if (shop == null)
			return;

		if (!shop.getOwner().getUUID().equals(pSender.getUniqueId())
				|| (Setting.UNLIMITED_ADMIN.getBoolean() && Permissions.isAdminEnabled(pSender))) {
			Message.NO_SHOP_PERMISSION.sendMessage(pSender);
			return;
		}

		OfflinePlayer target = Bukkit.getOfflinePlayer(command.getArgAt(1));
		if (!target.hasPlayedBefore()) {
			Message.PLAYER_NOT_FOUND.sendMessage(pSender);
			return;
		}

		if (shop.getUsersUUID().contains(target.getUniqueId())) {
			Message.UNSUCCESSFUL_SHOP_MEMBERS.sendMessage(pSender);
			return;
		}

		PlayerShopChangeEvent changeEvent = new PlayerShopChangeEvent(pSender, shop, ShopChange.ADD_MANAGER, new ObjectHolder<OfflinePlayer>(target));
		Bukkit.getPluginManager().callEvent(changeEvent);
		if (changeEvent.isCancelled()) return;

		shop.addManager(target.getUniqueId());

		Message.UPDATED_SHOP_MEMBERS.sendMessage(pSender);
	}

	/**
	 * Removes the specified player from the shop if they currently are a manager
	 */
	public void removeUser() {
		Shop shop = findShop();

		if (shop == null)
			return;

		if (!shop.getOwner().getUUID().equals(pSender.getUniqueId())
				|| (Setting.UNLIMITED_ADMIN.getBoolean() && Permissions.isAdminEnabled(pSender))) {
			Message.NO_SHOP_PERMISSION.sendMessage(pSender);
			return;
		}

		OfflinePlayer target = Bukkit.getOfflinePlayer(command.getArgAt(1));
		if (!target.hasPlayedBefore()) {
			Message.PLAYER_NOT_FOUND.sendMessage(pSender);
			return;
		}

		PlayerShopChangeEvent changeEvent = new PlayerShopChangeEvent(pSender, shop, ShopChange.REMOVE_USER, new ObjectHolder<OfflinePlayer>(target));
		Bukkit.getPluginManager().callEvent(changeEvent);
		if (changeEvent.isCancelled()) return;

		if (!shop.removeUser(target.getUniqueId())) {
			Message.UNSUCCESSFUL_SHOP_MEMBERS.sendMessage(pSender);
			return;
		}

		Message.UPDATED_SHOP_MEMBERS.sendMessage(pSender);
	}

	/**
	 * Adds the specified player to the shop as a member
	 */
	public void addMember() {
		Shop shop = findShop();

		if (shop == null)
			return;

		if (!shop.getOwner().getUUID().equals(pSender.getUniqueId())
				|| (Setting.UNLIMITED_ADMIN.getBoolean() && Permissions.isAdminEnabled(pSender))) {
			Message.NO_SHOP_PERMISSION.sendMessage(pSender);
			return;
		}

		OfflinePlayer target = Bukkit.getOfflinePlayer(command.getArgAt(1));
		if (!target.hasPlayedBefore()) {
			Message.PLAYER_NOT_FOUND.sendMessage(pSender);
			return;
		}


		if (shop.getUsersUUID().contains(target.getUniqueId())) {
			Message.UNSUCCESSFUL_SHOP_MEMBERS.sendMessage(pSender);
			return;
		}

		PlayerShopChangeEvent changeEvent = new PlayerShopChangeEvent(pSender, shop, ShopChange.ADD_MEMBER, new ObjectHolder<OfflinePlayer>(target));
		Bukkit.getPluginManager().callEvent(changeEvent);
		if (changeEvent.isCancelled()) return;

		shop.addMember(target.getUniqueId());

		Message.UPDATED_SHOP_MEMBERS.sendMessage(pSender);
	}

	/**
	 * Changes the players trade multiplier for current login
	 */
	public void multi() {
        if (!Setting.ALLOW_MULTI_TRADE.getBoolean()) {
			Message.FEATURE_DISABLED.sendMessage(pSender);
			return;
		}

        PlayerSetting playerSetting = plugin.getDataStorage().loadPlayer(pSender.getUniqueId());

		if (command.argsSize() == 1) {
			Message.MULTI_AMOUNT.sendMessage(pSender, new Tuple<>(Variable.AMOUNT.toString(), String.valueOf(playerSetting.getMulti())));
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

			Message.MULTI_UPDATE.sendMessage(pSender, new Tuple<>(Variable.AMOUNT.toString(), String.valueOf(amount)));
		}
	}

	/**
	 * Changes the players with the ADMIN permission to toggle whether it is enabled for them
	 */
	public void toggleAdmin() {
		PlayerSetting playerSetting = plugin.getDataStorage().loadPlayer(pSender.getUniqueId());

		playerSetting.setAdminEnabled(!playerSetting.isAdminEnabled());
		plugin.getDataStorage().savePlayer(playerSetting);

		Message.ADMIN_TOGGLED.sendMessage(pSender, new Tuple<>(Variable.STATE.toString(), playerSetting.isAdminEnabled() ? "enabled" : "disabled"));
	}

	/**
	 * Shows players their current admin mode or changes with optional variable
	 */
	public void admin() {
		PlayerSetting playerSetting = plugin.getDataStorage().loadPlayer(pSender.getUniqueId());
		boolean initialValue = playerSetting.isAdminEnabled();

		if (command.hasArgAt(1)) {

			switch (command.getArgAt(1).toLowerCase()) {
				case "true":
				case "t":
					playerSetting.setAdminEnabled(true);
					break;
				case "false":
				case "f":
					playerSetting.setAdminEnabled(false);
					break;
			}

			if (initialValue != playerSetting.isAdminEnabled())
				plugin.getDataStorage().savePlayer(playerSetting);
		}

		Message.ADMIN_TOGGLED.sendMessage(pSender, new Tuple<>(Variable.STATE.toString(), playerSetting.isAdminEnabled() ? "enabled" : "disabled"));
	}

	/**
	 * Toggles the join status message for the player
	 */
	public void toggleStatus() {
		if (!Setting.ALLOW_TOGGLE_STATUS.getBoolean()) {
			Message.FEATURE_DISABLED.sendMessage(pSender);
			return;
		}

		PlayerSetting playerSetting = plugin.getDataStorage().loadPlayer(pSender.getUniqueId());
		playerSetting.setShowInvolvedStatus(!playerSetting.showInvolvedStatus());
		plugin.getDataStorage().savePlayer(playerSetting);
		Message.TOGGLED_STATUS.sendMessage(pSender, new Tuple<>(Variable.STATUS.toString(), playerSetting.showInvolvedStatus() ? "on" : "off"));
	}

	/**
	 * Create a regular shop from a sign in front of the player
	 */
	public void createTrade() {
		Sign sign = findSign();

		if (sign == null)
			return;

		createShop(sign, ShopType.TRADE);
	}

	/**
	 * Create a BiTrade shop from a sign in front of the player
	 */
	public void createBiTrade() {
		Sign sign = findSign();

		if (sign == null)
			return;

		createShop(sign, ShopType.BITRADE);
	}

	/**
	 * Create a iTrade shop from a sign in front of the player
	 */
	public void createITrade() {
		Sign sign = findSign();

		if (sign == null)
			return;

		createShop(sign, ShopType.ITRADE);
	}


	/**
	 * Create a shop from a non-shop sign in front of the player
	 *
	 * @param shopSign sign to make into a shop
	 * @param shopType type of shop to make
	 */
	private void createShop(Sign shopSign, ShopType shopType) {
		if (ShopType.isShop(shopSign)) {
			Message.EXISTING_SHOP.sendMessage(pSender);
			return;
		}

		ShopUser owner = new ShopUser(pSender, ShopRole.OWNER);

		if (!checkShopChest(shopSign.getBlock()) && !shopType.isITrade()) {
			Message.NO_CHEST.sendMessage(pSender);
			return;
		}

		if (Setting.MAX_SHOPS_PER_CHUNK.getInt() <= plugin.getDataStorage().getShopCountInChunk(shopSign.getChunk())) {
			Message.TOO_MANY_CHESTS.sendMessage(pSender);
			return;
		}

		ShopChest shopChest;
		Shop shop;
		Block chest = findShopChest(shopSign.getBlock());

		if (!shopType.isITrade()) {
			if (ShopChest.isShopChest(chest)) {
				shopChest = new ShopChest(chest.getLocation());
			} else {
				shopChest = new ShopChest(chest, pSender.getUniqueId(), shopSign.getLocation());
			}

			if (shopChest.hasOwner() && !shopChest.getOwner().equals(owner.getUUID())) {
				Message.NO_SHOP_PERMISSION.sendMessage(pSender);
				return;
			}

			if (shopChest.hasShopSign() && !shopChest.getShopSign().getLocation().equals(shopSign.getLocation())) {
				Message.EXISTING_SHOP.sendMessage(pSender);
				return;
			}

			shop = new Shop(new Tuple<>(shopSign.getLocation(), shopChest.getChest().getLocation()), shopType, owner);
			shopChest.setName();


			if (shopChest.isEmpty() && shop.hasProduct()) {
				Message.EMPTY_TS_ON_SETUP.sendMessage(pSender);
			}
		} else {
			shop = new Shop(shopSign.getLocation(), shopType, owner);
		}

		PlayerShopCreateEvent shopCreateEvent = new PlayerShopCreateEvent(pSender, shop);
		Bukkit.getPluginManager().callEvent(shopCreateEvent);
		if (shopCreateEvent.isCancelled()) {
			return;
		}

		shopSign.setLine(0, shopType.toHeader());
		shopSign.update();

		shop.saveShop();

		Message.SUCCESSFUL_SETUP.sendMessage(pSender);
	}

	/**
	 * Changes/Sets the players permission level if internal permissions is enabled
	 */
	public void playerLevel() {
		if (Bukkit.getOfflinePlayer(command.getArgAt(1)).hasPlayedBefore()) {
			PlayerSetting playerSetting = plugin.getDataStorage().loadPlayer(Bukkit.getOfflinePlayer(command.getArgAt(1)).getUniqueId());
			if (command.argsSize() == 2) {
				Message.VIEW_PLAYER_LEVEL.sendMessage(pSender,
						new Tuple<>(Variable.PLAYER.toString(), Bukkit.getOfflinePlayer(command.getArgAt(1)).getName()),
						new Tuple<>(Variable.LEVEL.toString(), playerSetting.getType() + ""));
			} else {
				if (isInt(command.getArgAt(2))) {
					int newLevel = Integer.parseInt(command.getArgAt(2));

					playerSetting.setType(newLevel);
					plugin.getDataStorage().savePlayer(playerSetting);

					Message.SET_PLAYER_LEVEL.sendMessage(pSender,
							new Tuple<>(Variable.PLAYER.toString(), Bukkit.getOfflinePlayer(command.getArgAt(1)).getName()),
							new Tuple<>(Variable.LEVEL.toString(), playerSetting.getType() + ""));
				} else {
					Message.INVALID_ARGUMENTS.sendMessage(pSender);
				}
			}
		} else {
			Message.PLAYER_NOT_FOUND.sendMessage(pSender);
		}
	}

	/**
	 * Shows the player the status of all shops they are involved with or the specified player is involved with
	 */
	public void status() {
		if (command.hasArgAt(1)) {
			if (!Permissions.isAdminEnabled(pSender)) {
				Message.NO_COMMAND_PERMISSION.sendMessage(pSender);
				return;
			}
			if (Bukkit.getOfflinePlayer(command.getArgAt(1)).hasPlayedBefore()) {
				plugin.getDataStorage().loadPlayer(Bukkit.getOfflinePlayer(command.getArgAt(1)).getUniqueId())
						.getInvolvedStatusesInventory().show(pSender.getPlayer());
			} else {
				Message.PLAYER_NOT_FOUND.sendMessage(pSender);
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
			Message.PLAYER_ONLY_COMMAND.sendMessage(pSender);
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
			Message.NO_SIGHTED_SHOP.sendMessage(pSender);
			return null;
		}
	}

	/**
	 * Returns the Sign the player is looking at
	 *
	 * @return null if Sign is not found, Sign object if it is
	 */
	protected Sign findSign() {
		if (pSender == null) {
			Message.PLAYER_ONLY_COMMAND.sendMessage(pSender);
			return null;
		}

		Block b = pSender.getTargetBlockExact(Setting.MAX_EDIT_DISTANCE.getInt());
		try {
			if (b == null)
				throw new NoSuchFieldException();

			if (plugin.getSigns().getSignTypes().contains(b.getType())) {
				return (Sign) b.getState();

			} else
				throw new NoSuchFieldException();

		} catch (NoSuchFieldException ex) {
			Message.NO_SIGN_FOUND.sendMessage(pSender);
			return null;
		}
	}
}