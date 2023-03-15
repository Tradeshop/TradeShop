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

package org.shanerx.tradeshop.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.PluginDescriptionFile;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.data.config.Message;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.framework.events.PlayerShopCreateEvent;
import org.shanerx.tradeshop.item.ShopItemSide;
import org.shanerx.tradeshop.item.ShopItemStack;
import org.shanerx.tradeshop.player.ShopRole;
import org.shanerx.tradeshop.player.ShopUser;
import org.shanerx.tradeshop.shop.ExchangeStatus;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shop.ShopChest;
import org.shanerx.tradeshop.shop.ShopType;
import org.shanerx.tradeshop.shoplocation.ShopLocation;
import org.shanerx.tradeshop.utils.debug.DebugLevels;
import org.shanerx.tradeshop.utils.objects.Tuple;
import org.shanerx.tradeshop.utils.relativedirection.LocationOffset;
import org.shanerx.tradeshop.utils.relativedirection.RelativeDirection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * This class contains a bunch of utility methods that
 * are used in almost every class of the plugin. It was
 * designed with the DRY concept in mind.
 */
public class Utils {

	private final UUID KOPUUID = UUID.fromString("daf79be7-bc1d-47d3-9896-f97b8d4cea7d");
	private final UUID LORIUUID = UUID.fromString("e296bc43-2972-4111-9843-48fc32302fd4");
	private final TradeShop PLUGIN = TradeShop.getPlugin();
	protected PluginDescriptionFile pdf = PLUGIN.getDescription();

	public Utils() {
	}

	public UUID[] getMakers() {
		return new UUID[]{KOPUUID, LORIUUID};
	}

	/**
	 * Returns the plugin name.
	 *
	 * @return the name.
	 */
	protected String getPluginName() {
		return pdf.getName();
	}

	/**
	 * Returns the plugin's version.
	 *
	 * @return the version
	 */
	public String getVersion() {
		return pdf.getVersion();
	}

	/**
	 * Returns a list of authors.
	 *
	 * @return the authors
	 */
	public List<String> getAuthors() {
		return pdf.getAuthors();
	}

	/**
	 * Returns the website of the plugin.
	 *
	 * @return the website
	 */
	public String getWebsite() {
		return pdf.getWebsite();
	}

	/**
	 * Returns true if the number is an {@code int}.
	 *
	 * @param str the string that should be parsed
	 * @return true if it is an {@code int}.
	 */
	public boolean isInt(String str) {
		try {
			Integer.parseInt(str);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Returns true itemStacks are equal excluding amount.
	 *
	 * @param itm1 the first item
	 * @param itm2 the ssecond item
	 * @return true if it args are equal.
	 */
	public boolean itemCheck(ItemStack itm1, ItemStack itm2) {
		int i1 = itm1.getAmount(), i2 = itm2.getAmount();
		ItemMeta temp1 = itm1.getItemMeta();
		MaterialData temp11 = itm1.getData();
		boolean ret;
		itm1.setAmount(1);
		itm2.setAmount(1);

		if (!itm1.hasItemMeta() && itm2.hasItemMeta()) {
			itm1.setItemMeta(itm2.getItemMeta());
			itm1.setData(itm2.getData());
		}
		ret = itm1.equals(itm2);

		itm1.setItemMeta(temp1);
		itm1.setData(temp11);
		itm1.setAmount(i1);
		itm2.setAmount(i2);
		return ret;
	}

	/**
	 * Checks whether a trade can take place.
	 *
	 * @param inv    the Inventory object representing the inventory that is subject to the transaction.
	 * @param itmOut the ItemStack that is being given away
	 * @param itmIn  the ItemStack that is being received
	 * @return true if the exchange may take place.
	 */
	public boolean canExchange(Inventory inv, ItemStack itmOut, ItemStack itmIn) {
		int count = 0,
				slots = 0,
				empty = 0,
				removed = 0,
				amtIn = itmIn.getAmount(),
				amtOut = itmOut.getAmount();

		for (ItemStack i : inv.getContents()) {
			if (i != null) {
				if (itemCheck(itmIn, i)) {
					count += i.getAmount();
					slots++;
				} else if (itemCheck(itmOut, i) && amtOut != removed) {

					if (i.getAmount() > amtOut - removed) {
						removed = amtOut;
					} else if (i.getAmount() == amtOut - removed) {
						removed = amtOut;
						empty += itmIn.getMaxStackSize();
					} else if (i.getAmount() < amtOut - removed) {
						removed += i.getAmount();
						empty += itmIn.getMaxStackSize();
					}
				}
			} else
				empty += itmIn.getMaxStackSize();
		}
		return empty + ((slots * itmIn.getMaxStackSize()) - count) >= amtIn;
	}

	/**
	 * Sets the event sign to a failed creation sign
	 *
	 * @param e    Event to reset the sign for
	 * @param shop Shoptype enum to get header
	 */
	public void failedSignReset(SignChangeEvent e, ShopType shop) {
		e.setLine(0, colorize(Setting.SHOP_BAD_COLOUR.getString() + shop.toString()));
		e.setLine(1, "");
		e.setLine(2, "");
		e.setLine(3, "");
	}

	/**
	 * Checks whether or not it is an illegal material.
	 *
	 * @param side What side of the trade the item is on
	 * @param mat  String to check
	 * @return returns true if valid material
	 */
	public boolean isIllegal(ShopItemSide side, Material mat) {
		return PLUGIN.getListManager().isIllegal(side, mat);
	}

	/**
	 * Checks whether the an inventory contains at least a certain amount of a certain material inside a specified inventory.
	 * <br>
	 * This works with the ItemStack's durability, which represents how much a tool is broken or, in case of a block, the block data.
	 *
	 * @param inv  the Inventory object
	 * @param item the item to be checked
	 * @param amount the amount attempting to be traded
	 * @return true if the condition is met.
	 */
	public boolean containsAtLeast(ItemStack[] inv, ItemStack item, int amount) {
		int count = 0;
		for (ItemStack itm : inv) {
			if (itm != null) {
				if (itemCheck(item, itm)) {
					count += itm.getAmount();
				}
			}
		}
		return count >= amount;
	}

	/**
	 * This function wraps up Bukkit's method {@code ChatColor.translateAlternateColorCodes('&', msg)}.
	 * <br>
	 * Used for shortening purposes and follows the DRY concept.
	 *
	 * @param msg string containing Color and formatting codes.
	 * @return the colorized string returned by the above method.
	 */
	public String colorize(String msg) {
		return ChatColor.translateAlternateColorCodes('&', msg);
	}

	/**
	 * Finds the TradeShop sign linked to a chest.
	 *
	 * @param chest the block holding the shop's inventory. Can be a chest, a trapped chest, a dropper, a dispenser, a hopper and a shulker box (1.9+).
	 * @return the sign.
	 */
	public Sign findShopSign(Block chest) {
		//region Linked Sign Check - Load and Check Chest Linkage first, if it is a shop then return Linked Shop
		ShopLocation potentialLocation = PLUGIN.getDataStorage().getChestLinkage(new ShopLocation(chest.getLocation()));
		if (potentialLocation != null && ShopType.isShop(potentialLocation.getLocation().getBlock()))
			return (Sign) potentialLocation.getLocation().getBlock().getState();
		//endregion

		// Directions to check for Sign(Reversed later as the list is built to check from Sign->Chest and this method checks from Chest->Sign)
		ArrayList<RelativeDirection> directions = PLUGIN.getListManager().getDirections();
		// Directions to check for other half of a DoubleChest
		ArrayList<BlockFace> flatFaces = new ArrayList<>(Arrays.asList(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST));

		Block otherHalf = null;

		for (RelativeDirection direction : directions) {
			// Translate Relative Direction and get the Block
			LocationOffset relativeOffset = direction.getTranslatedDirection(RelativeDirection.getDirection(chest.getBlockData()).getOppositeFace());
			Block relative = chest.getRelative(relativeOffset.getXOffset(), relativeOffset.getYOffset(), relativeOffset.getZOffset());

			// Check if it is a shop and return. Add the chest and shop to ChestLinkage, If it is a DoubleChest also add the other half
			if (ShopType.isShop(relative)) {
				PLUGIN.getDataStorage().addChestLinkage(new ShopLocation(chest.getLocation()), new ShopLocation(relative.getLocation()));
				if (ShopChest.isDoubleChest(chest)) {
					PLUGIN.getDataStorage().addChestLinkage(new ShopLocation(ShopChest.getOtherHalfOfDoubleChest(chest).getLocation()), new ShopLocation(relative.getLocation()));
				}
				return (Sign) relative.getState();
				// 	If the relative block wasn't the ShopSign and we are checking a FlatFace, Then check if it is the other half of this Chest
			} else if (flatFaces.stream().anyMatch((face) -> relativeOffset.getXOffset() == face.getModX() ||
					relativeOffset.getYOffset() == face.getModY() ||
					relativeOffset.getZOffset() == face.getModZ()) && (chest.getType().equals(Material.CHEST) || chest.getType().equals(Material.TRAPPED_CHEST))) {
				if (relative.getType().equals(chest.getType()) && ShopChest.isDoubleChest(chest)) {
					// Set Aside for later, We want to check the relative directions on one chest at a time
					otherHalf = relative;
				}
			}
		}

		// Check the relative Directions for the other half of the chest
		if (otherHalf != null) {
			for (RelativeDirection direction2 : directions) {
				LocationOffset relativeOffset2 = direction2.getTranslatedDirection(RelativeDirection.getDirection(otherHalf.getBlockData()).getOppositeFace());
				Block relative2 = chest.getRelative(relativeOffset2.getXOffset(), relativeOffset2.getYOffset(), relativeOffset2.getZOffset());
				if (ShopType.isShop(relative2)) {
					if (ShopType.isShop(relative2)) {
						PLUGIN.getDataStorage().addChestLinkage(new ShopLocation(chest.getLocation()), new ShopLocation(relative2.getLocation()));
						return (Sign) relative2.getState();
					}
				}
			}
		}

		return null;
	}

	/**
	 * Finds the TradeShop chest, dropper, dispenser, hopper or shulker box (1.9+) linked to a sign.
	 *
	 * @param sign the TradeShop sign
	 * @return the shop's inventory holder block.
	 */
	public Block findShopChest(Block sign) {
		// For each Relative Direction listed in the config in order
		for (RelativeDirection direction : PLUGIN.getListManager().getDirections()) {
			// Get the direction the block we want to check is Facing
			BlockFace facing = RelativeDirection.getDirection(sign.getBlockData());
			// Translate the Relative Direction from the Direction the Block is Facing and get the Block on that BlockFace
			LocationOffset relativeOffset = direction.getTranslatedDirection(facing);
			Block relativeBlock = sign.getRelative(relativeOffset.getXOffset(), relativeOffset.getYOffset(), relativeOffset.getZOffset());

			// Check if relativeBlock is an inventory block and return if true
			if (PLUGIN.getListManager().isInventory(relativeBlock)) {
				return relativeBlock;
			}
		}
		return null;
	}

	/**
	 * Checks to see if the shop chest is findable
	 *
	 * @param sign the TradeShop sign
	 * @return whether the sign is linked to a chest.
	 */
	public boolean checkShopChest(Block sign) {
		return findShopChest(sign) != null;
	}

	/**
	 * Returns true if inventory has enough cost to make trade
	 *
	 * @param inv        inventory to check
	 * @param itemList   items to check
	 * @param multiplier multiplier to use for check
	 * @return true if shop has enough cost to make trade
	 */
	public Boolean checkInventory(Inventory inv, List<ShopItemStack> itemList, int multiplier) {
		return getItems(inv.getStorageContents(), itemList, multiplier).get(0) != null;
	}

	/**
	 * Returns a 'bad' list with index 0 being `null`
	 *
	 * @return list with index 0 being `null`
	 */
	public List<ItemStack> createBadList() {
		List<ItemStack> badList = new ArrayList<>();
		badList.add(null);
		return badList;
	}


	/**
	 * Checks whether a trade can take place.
	 *
	 * @param shop       the Shop object the player is trading with
	 * @param playerInv  the storageContents of the player inventory that is subject to the transaction.
	 * @param multiplier the multiplier for the trade
	 * @param action     the action from the event
	 * @return Exchange status with appropriate response
	 */
	public Tuple<ExchangeStatus, List<ItemStack>> canExchangeAll(Shop shop, Inventory playerInv, int multiplier, Action action) {
		if (shop.getShopType() != ShopType.BITRADE && action == Action.LEFT_CLICK_BLOCK) {
			return new Tuple<>(ExchangeStatus.NOT_TRADE, createBadList());
		}

		Inventory playerInventory = Bukkit.createInventory(null, playerInv.getStorageContents().length);
		playerInventory.setContents(playerInv.getStorageContents().clone());

		Inventory shopInventory = null;

		if (shop.getShopType().equals(ShopType.ITRADE)) {
			shopInventory = Bukkit.createInventory(null, Math.min((int) (Math.ceil(shop.getSideList(ShopItemSide.PRODUCT).size() / 9.0) * 9) * multiplier, 54));
			while (shopInventory.firstEmpty() != -1) {
				for (ItemStack item : shop.getSideItemStacks(ShopItemSide.PRODUCT)) {
					item.setAmount(item.getMaxStackSize());
					shopInventory.addItem(item);
				}
			}
		} else {
			Inventory shopInv = shop.getChestAsSC().getInventory();
			shopInventory = Bukkit.createInventory(null, shopInv.getStorageContents().length);
			shopInventory.setContents(shopInv.getStorageContents().clone());
		}

		List<ItemStack> costItems = new ArrayList<>(), productItems;

		boolean isBi = shop.getShopType().equals(ShopType.BITRADE) && action.equals(Action.LEFT_CLICK_BLOCK);

		//Method to find Cost items in player inventory and add to cost array
		if (!shop.isNoCost() || shop.hasSide(ShopItemSide.COST)) {
			costItems = getItems(playerInventory.getStorageContents(), shop.getSideList(ShopItemSide.COST, isBi), multiplier);
			if (costItems.get(0) == null) {
				return new Tuple<>(ExchangeStatus.PLAYER_NO_COST, costItems);
			}
		}

		//Method to find Product items in shop inventory and add to product array
		productItems = getItems(shopInventory.getStorageContents(), shop.getSideList(ShopItemSide.PRODUCT, isBi), multiplier);
		if (productItems.get(0) == null) {
			shop.updateStatus();
			return new Tuple<>(ExchangeStatus.SHOP_NO_PRODUCT, productItems);
		}

		if (costItems.size() > 0) {
			//For loop to remove cost items from player inventory
			for (ItemStack item : costItems) {
				playerInventory.removeItem(item);
			}
		}

		//For loop to remove product items from shop inventory
		for (ItemStack item : productItems) {
			shopInventory.removeItem(item);
		}

		if (!shop.getShopType().isITrade() && costItems.size() > 0) {
			//For loop to put cost items in shop inventory
			for (ItemStack item : costItems) {
				if (!addItemToInventory(shopInventory, item).isEmpty()) {
					return new Tuple<>(ExchangeStatus.SHOP_NO_SPACE, createBadList());
				}
			}
		}

		//For loop to put product items in player inventory
		for (ItemStack item : productItems) {
			if (!addItemToInventory(playerInventory, item).isEmpty()) {
				return new Tuple<>(ExchangeStatus.PLAYER_NO_SPACE, createBadList());
			}
		}

		return new Tuple<>(ExchangeStatus.SUCCESS, createBadList()); //Successfully completed trade
	}

	/**
	 * Create a shop from a non-shop sign in front of the player
	 *
	 * @param shopSign sign to make into a shop
	 * @param creator  player that is creating the shop
	 * @param shopType type of shop to make
	 * @param cost     initial cost item for shop
	 * @param product  initial product item for shop
	 * @param event    SignUpdateEvent if being called from sign creation
	 * @return returns the shop object that was created
	 */
	public Shop createShop(Sign shopSign, Player creator, ShopType shopType, ItemStack cost, ItemStack product, SignChangeEvent event) {
		if (ShopType.isShop(shopSign)) {
			Message.EXISTING_SHOP.sendMessage(creator);
			return null;
		}

		if (!shopType.checkPerm(creator)) {
			Message.NO_TS_CREATE_PERMISSION.sendMessage(creator);
			return null;
		}

		ShopUser owner = new ShopUser(creator, ShopRole.OWNER);

		if (!checkShopChest(shopSign.getBlock()) && !shopType.isITrade()) {
			Message.NO_CHEST.sendMessage(creator);
			return null;
		}

		if (Setting.MAX_SHOPS_PER_CHUNK.getInt() <= PLUGIN.getDataStorage().getShopCountInChunk(shopSign.getChunk())) {
			Message.TOO_MANY_CHESTS.sendMessage(creator);
			return null;
		}

		ShopChest shopChest;
		Shop shop;
		Block chest = findShopChest(shopSign.getBlock());

		if (!shopType.isITrade()) {
			if (ShopChest.isShopChest(chest)) {
				shopChest = new ShopChest(chest.getLocation());
			} else {
				shopChest = new ShopChest(chest, creator.getUniqueId(), shopSign.getLocation());
			}

			if (shopChest.hasOwner() && !shopChest.getOwner().equals(owner.getUUID())) {
				Message.NO_SHOP_PERMISSION.sendMessage(creator);
				return null;
			}

			if (shopChest.hasShopSign() && !shopChest.getShopSign().getLocation().equals(shopSign.getLocation())) {
				Message.EXISTING_SHOP.sendMessage(creator);
				return null;
			}

			shop = new Shop(new Tuple<>(shopSign.getLocation(), shopChest.getChest().getLocation()), shopType, owner);
			shopChest.setName();

			if (cost != null && !shop.hasSide(ShopItemSide.COST))
				shop.setSideItems(ShopItemSide.COST, cost);

			if (product != null && !shop.hasSide(ShopItemSide.PRODUCT))
				shop.setSideItems(ShopItemSide.PRODUCT, product);

			if (shopChest.isEmpty() && shop.hasSide(ShopItemSide.PRODUCT)) {
				Message.EMPTY_TS_ON_SETUP.sendMessage(creator);
			}
		} else {
			shop = new Shop(shopSign.getLocation(), shopType, owner);
		}

		PLUGIN.getDebugger().log("-----Pre-Event-----", DebugLevels.SHOP_CREATION);
		PLUGIN.getDebugger().log(shop.toDebug(), DebugLevels.SHOP_CREATION);


		PlayerShopCreateEvent shopCreateEvent = new PlayerShopCreateEvent(creator, shop);
		Bukkit.getPluginManager().callEvent(shopCreateEvent);
		PLUGIN.getDebugger().log("-----Post-Event-----", DebugLevels.SHOP_CREATION);
		PLUGIN.getDebugger().log(shop.toDebug(), DebugLevels.SHOP_CREATION);

		if (shopCreateEvent.isCancelled()) {
			PLUGIN.getDebugger().log("Creation Failed!", DebugLevels.SHOP_CREATION);
			return null;
		}

		shop.saveShop();

		if (event != null) {
			shop.updateSign(event);
			PLUGIN.getDebugger().log("Event Sign Lines: \n" + event.getLine(0) + "\n" + event.getLine(1) + "\n" + event.getLine(2) + "\n" + event.getLine(3), DebugLevels.SHOP_CREATION);
		} else {
			shop.updateSign(shopSign);
			PLUGIN.getDebugger().log("Sign Lines: \n" + shopSign.getLine(0) + "\n" + shopSign.getLine(1) + "\n" + shopSign.getLine(2) + "\n" + shopSign.getLine(3), DebugLevels.SHOP_CREATION);
		}

		Message.SUCCESSFUL_SETUP.sendMessage(creator);
		PLUGIN.getDebugger().log("Creation Successful!", DebugLevels.SHOP_CREATION);
		return shop;
	}

	/**
	 * Create a shop from a non-shop sign in front of the player
	 *
	 * @param shopSign sign to make into a shop
	 * @param creator  player that is creating the shop
	 * @param shopType type of shop to make
	 * @return returns the shop object that was created
	 */
	public Shop createShop(Sign shopSign, Player creator, ShopType shopType) {
		return createShop(shopSign, creator, shopType, null, null, null);
	}

	/**
	 * Create a shop from a non-shop sign in front of the player
	 *
	 * @param shopSign sign to make into a shop
	 * @param creator  player that is creating the shop
	 * @param shopType type of shop to make
	 * @param cost     initial cost item for shop
	 * @param product  initial product item for shop
	 * @return returns the shop object that was created
	 */
	public Shop createShop(Sign shopSign, Player creator, ShopType shopType, ItemStack cost, ItemStack product) {
		return createShop(shopSign, creator, shopType, cost, product, null);
	}

	/**
	 * Returns an arraylist of the ItemStack Objects to be removed/added, if it could not get enough of any items, it will return index 0 as null, followed by ItemStack Objects that could not be retrieved
	 *
	 * @param storageContents the storage contents of the inventory being checked
	 * @param search          List of ShopItemStack Objects that need to be retrieved
	 * @param multiplier      the multiplier for the trade
	 * @return List of ItemStack Objects to pull from inventory or if not enough; index 0 as null, followed by ItemStack Objects that could not be retrieved
	 */
	public List<ItemStack> getItems(ItemStack[] storageContents, List<ShopItemStack> search, int multiplier) {
		Map<ItemStack, Integer> storage = new HashMap<>(), found = new HashMap<>();
		List<ItemStack> good = new ArrayList<ItemStack>(), bad = createBadList();

		PLUGIN.getDebugger().log("Utils > getItems > Search List: " + search, DebugLevels.TRADE);

		for (ItemStack itemStack : storageContents.clone()) {
			if (itemStack != null) {
				ItemStack tempItemStack = itemStack.clone();
				int amount = tempItemStack.getAmount();
				tempItemStack.setAmount(1);

				if (storage.putIfAbsent(tempItemStack, amount) != null)
					storage.put(tempItemStack, storage.get(tempItemStack) + amount);
			}
		}

		PLUGIN.getDebugger().log("Utils > getItems > Storage List: " + storage, DebugLevels.TRADE);

		int totalCount = 0, currentCount = 0;

		for (ShopItemStack item : search) {
			int count = item.getAmount() * multiplier;
			totalCount += count;

			for (ItemStack storageItem : storage.keySet()) {
				if (item.isSimilar(storageItem)) {
					int taken;
					taken = manyMin(storage.get(storageItem), count);

					if (found.putIfAbsent(item.getItemStack(), taken) != null)
						found.put(item.getItemStack(), storage.get(storageItem) + taken);

					storage.put(storageItem, storage.get(storageItem) - taken);

					ItemStack goodItem = storageItem;
					goodItem.setAmount(taken);
					good.add(goodItem);

					count -= taken;
					currentCount += taken;
				}

				if (count == 0) break;
			}

			if (count > 0) {
				ItemStack badItem = item.getItemStack();
				badItem.setAmount(count);
				bad.add(badItem);
			}
		}

		PLUGIN.getDebugger().log("Utils > getItems > Good List: " + good, DebugLevels.TRADE);
		PLUGIN.getDebugger().log("Utils > getItems > Bad List: " + bad, DebugLevels.TRADE);
		PLUGIN.getDebugger().log("Utils > getItems > Return Status: " + (currentCount != totalCount ? "bad" : "good"), DebugLevels.TRADE);

		return currentCount != totalCount ? bad : good;
	}

	/**
	 * Attempts to add an ItemStack to an Inventory while splitting it with its max stack size as a maximum
	 *
	 * @param inv  Inventory to attempt to add the ItemStack to
	 * @param item ItemStack to be added to the ivnentory
	 * @return smallest integer
	 */
	public Map<Integer, ItemStack> addItemToInventory(Inventory inv, ItemStack item) {
		int maxStack = inv.getMaxStackSize();
		inv.setMaxStackSize(item.getMaxStackSize());
		Map<Integer, ItemStack> result = inv.addItem(item);
		inv.setMaxStackSize(maxStack);
		return result;
	}

	/**
	 * Returns the smallest integer passed to it
	 *
	 * @param values list of integers to compare against each other
	 * @return smallest integer
	 */
	public int manyMin(int... values) {
		int min = values[0];
		for (int i : values) {
			min = Math.min(i, min);
		}

		return min;
	}

	/**
	 * Converts string to boolean based on acceptable responses
	 *
	 * @param check String to convert to boolean
	 * @return true if acceptable string was found
	 */
	public boolean textToBool(String check) {
		switch (check.toLowerCase()) {
			case "true":
			case "t":
			case "yes":
			case "y":
			case "all":
				return true;
			default:
				return false;
		}
	}
}