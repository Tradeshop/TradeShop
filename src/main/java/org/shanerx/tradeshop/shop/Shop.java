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

package org.shanerx.tradeshop.shop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.item.ShopItemSide;
import org.shanerx.tradeshop.item.ShopItemStack;
import org.shanerx.tradeshop.player.PlayerSetting;
import org.shanerx.tradeshop.player.ShopRole;
import org.shanerx.tradeshop.player.ShopUser;
import org.shanerx.tradeshop.shoplocation.ShopChunk;
import org.shanerx.tradeshop.shoplocation.ShopLocation;
import org.shanerx.tradeshop.utils.Utils;
import org.shanerx.tradeshop.utils.gsonprocessing.GsonProcessor;
import org.shanerx.tradeshop.utils.objects.ObjectHolder;
import org.shanerx.tradeshop.utils.objects.Tuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Shop {

	private ShopUser owner;
	private Set<UUID> managers, members;
	private ShopType shopType;
	private final ShopLocation shopLoc;
	private List<ShopItemStack> product, cost;
	private ShopLocation chestLoc;
	private transient Inventory storageInv;
	private transient Utils utils = new Utils();
	private ShopStatus status = ShopStatus.INCOMPLETE;

	private Map<ShopSettingKeys, ObjectHolder<?>> shopSettings;

	private int availableTrades = 0;

	/**
	 * Creates a Shop object
	 *
	 * @param locations    Location of shop sign and chest as Tuple, left = Sign location, right = inventory location
	 * @param owner        Owner of the shop as a ShopUser
	 * @param shopType     Type of the shop as ShopType
	 * @param productItems Product items to go into the shop
	 * @param costItems    Cost items to go into the shop
	 * @param players      Users to be added to the shop as Tuple, left = Managers, right = Members
	 */
	public Shop(Tuple<Location, Location> locations, ShopType shopType, ShopUser owner, Tuple<Set<UUID>, Set<UUID>> players, List<ShopItemStack> productItems, List<ShopItemStack> costItems) {
		shopLoc = new ShopLocation(locations.getLeft());
		this.owner = owner;

		if (locations.getRight() != null) {
			chestLoc = new ShopLocation(locations.getRight());
			utils.PLUGIN.getDataStorage().addChestLinkage(chestLoc, shopLoc);
		}

		this.shopType = shopType;

		managers = players.getLeft() == null ? new HashSet<>() : players.getLeft();
		members = players.getRight() == null ? new HashSet<>() : players.getRight();

		product = productItems != null ? new ArrayList<>(productItems) : new ArrayList<>();
		cost = costItems != null ? new ArrayList<>(costItems) : new ArrayList<>();

		product.removeIf(shopItemStack -> shopItemStack.getItemStack() == null);
		cost.removeIf(shopItemStack -> shopItemStack.getItemStack() == null);

		fixAfterLoad();
	}

	/**
	 * Creates a Shop object
	 *
	 * @param locations Location of shop sign and chest as Tuple, left = Sign location, right = inventory location
	 * @param owner     Owner of the shop as a ShopUser
	 * @param shopType  Type of the shop as ShopType
	 * @param items     Items to go into the shop as Tuple, left = Product, right = Cost
	 * @param players   Users to be added to the shop as Tuple, left = Managers, right = Members
	 */
	public Shop(Tuple<Location, Location> locations, ShopType shopType, ShopUser owner, Tuple<Set<UUID>, Set<UUID>> players, Tuple<ItemStack, ItemStack> items) {
		this(locations, shopType, owner, players, Collections.singletonList(new ShopItemStack(items.getLeft())), Collections.singletonList(new ShopItemStack(items.getRight())));
	}

	/**
	 * Creates a Shop object
	 *
	 * @param locations Location of shop sign and chest as Tuple, left = Sign location, right = inventory location
	 * @param owner     Owner of the shop as a ShopUser
	 * @param shopType  Type of the shop as ShopType
	 */
	public Shop(Tuple<Location, Location> locations, ShopType shopType, ShopUser owner) {
		this(locations, shopType, owner, new Tuple<>(null, null), new Tuple<>(null, null));
	}

	/**
	 * Creates a Shop object
	 *
	 * @param location Location of shop sign
	 * @param owner    Owner of the shop as a ShopUser
	 * @param shopType Type of the shop as ShopType
	 */
	public Shop(Location location, ShopType shopType, ShopUser owner) {
		this(new Tuple<>(location, null), shopType, owner, new Tuple<>(null, null), new Tuple<>(null, null));
	}

	/**

	 * Loads a shop from file and returns the Shop object
	 *
	 * @param loc Location of the shop sign
	 * @return The shop from file
	 */
	public static Shop loadShop(ShopLocation loc) {
		return new Utils().PLUGIN.getDataStorage().loadShopFromSign(loc);
	}

	/**
	 * Loads the shop from file
	 *
	 * @param s Shop sign to load from
	 * @return Shop Object
	 */
	public static Shop loadShop(Sign s) {
		return loadShop(new ShopLocation(s.getLocation()));
	}

	/**
	 * Sets the storageInventory
	 */
	public void setStorageInventory() {
		if (getStorage() != null && getStorage() instanceof Container)
			storageInv = ((Container) getStorage()).getInventory();
		else
			storageInv = null;
	}

	/**
	 * Returns the storage block as a ShopChest
	 *
	 * @return Shop storage as ShopChest
	 */
	public ShopChest getChestAsSC() {
        try {
            return new ShopChest(chestLoc.getLocation());
        } catch (NullPointerException ex) {
            return null;
        }
	}

	/**
	 * Returns location of shops inventory
	 *
	 * @return inventory location as Location
	 */
	public Location getInventoryLocation() {
		return chestLoc != null ? chestLoc.getLocation() : null;
	}

	/**
	 * Sets the inventory location
	 *
	 * @param newLoc new location to set
	 */
	public void setInventoryLocation(Location newLoc) {
		chestLoc = new ShopLocation(newLoc);
		utils.PLUGIN.getDataStorage().addChestLinkage(chestLoc, shopLoc);
	}

	/**
	 * Returns the location of the shop sign
	 *
	 * @return Location of the shops sign
	 */
	public Location getShopLocation() {
        return getShopLocationAsSL().getLocation();
	}

	/**
	 * Returns the type of the shop
	 *
	 * @return list of managers as ShopUser
	 */
	public ShopType getShopType() {
		return shopType;
	}

	/**
	 * Sets the shops type
	 *
	 * @param newType new type as ShopType
	 */
	public void setShopType(ShopType newType) {
		if (shopType != newType)
			setShopSettings(newType);

		shopType = newType;
	}

	/**
	 * Returns the amount of trades the shop could do when last accessed
	 *
	 * @return amount of trades the shop can do
	 */
	public int getAvailableTrades() {
		return availableTrades;
	}

	/**
	 * Returns the shop signs location as a ShopLocation
	 *
	 * @return Shop sign's Location as ShopLocation
	 */
	public ShopLocation getShopLocationAsSL() {
		return shopLoc;
	}

	/**
	 * Returns the shop inventories location as a ShopLocation
	 *
	 * @return Shop inventory's Location as ShopLocation
	 */
	public ShopLocation getInventoryLocationAsSL() {
		return chestLoc;
	}

	/**
	 * Fixes values and objects after loading or creating a Shop
	 */
	public void fixAfterLoad() {
		if (utils == null)
			utils = new Utils();

		shopLoc.stringToWorld();
		if (!getShopType().isITrade() && chestLoc != null) {
			chestLoc.stringToWorld();
			cost.removeIf(item -> item.getItemStack().getType().toString().endsWith("SHULKER_BOX") && getInventoryLocation().getBlock().getType().toString().endsWith("SHULKER_BOX"));
			utils.PLUGIN.getDataStorage().addChestLinkage(chestLoc, shopLoc);
		}

		setShopSettings();

		fixSide(ShopItemSide.COST);
		fixSide(ShopItemSide.PRODUCT);

		if (getShopSign() != null)
			updateSign();
	}

	/**
	 * Generates a semi readable output of the shop object for debug output
	 *
	 * @return Return String for debug output
	 */
	public String toDebug() {
		StringBuilder sb = new StringBuilder();
		sb.append("Shop Debug: \n");
		sb.append("Shop Chunk: ").append(new ShopChunk(shopLoc.getChunk()).serialize()).append("\n");
		sb.append("Sign Location: ").append(shopLoc.serialize()).append("\n");
		sb.append("Shop Type: ").append((isMissingItems() ? Setting.SHOP_INCOMPLETE_COLOUR : Setting.SHOP_GOOD_COLOUR).getString() + shopType.toHeader()).append("\n");
		sb.append("Shop Status: ").append(status.getLine()).append("\n");
		sb.append("Storage Location: ").append(hasStorage() ? getInventoryLocationAsSL().serialize() : "N/A").append("\n");
		sb.append("Storage Type: ").append(hasStorage() ? getStorage().getType().toString() : "N/A").append("\n");
		sb.append("Owner: ").append(owner.getName()).append(" | ").append(owner.getUUID()).append("\n");
		sb.append("Managers: ").append(managers.isEmpty() ? "N/A" : managers.size()).append("\n");
		if (!managers.isEmpty())
			getUsers(ShopRole.MANAGER).forEach(manager -> sb.append("          ").append(manager.getName()).append(" | ").append(manager.getUUID()).append("\n"));
		sb.append("Members: ").append(members.isEmpty() ? "N/A" : members.size()).append("\n");
		if (!members.isEmpty())
			getUsers(ShopRole.MEMBER).forEach(member -> sb.append("         ").append(member.getName()).append(" | ").append(member.getUUID()).append("\n"));
		sb.append("Products: ").append(product.isEmpty() ? "N/A" : product.size()).append("\n");
		if (!product.isEmpty())
			product.forEach(productItem -> sb.append("          ").append(productItem.toConsoleText()).append("\n"));
		sb.append("Costs: ").append(cost.isEmpty() ? "N/A" : cost.size()).append("\n");
		if (!cost.isEmpty())
			cost.forEach(costItem -> sb.append("       ").append(costItem.toConsoleText()).append("\n"));

		return utils.colorize(sb.toString());
	}

	/**
	 * Saves the shop to file
	 */
	public void saveShop() {
		updateFullTradeCount();
		utils.PLUGIN.getDataStorage().saveShop(this);
		updateUserFiles();
	}

	/**
	 * Saves the shop to file if passed boolean is true
	 *
	 * @param shouldSave true if save should proceed
	 */
	public void saveShop(boolean shouldSave) {
		if (shouldSave) saveShop();
	}

	/**
	 * Returns the shops sign as a Sign
	 *
	 * @return Shop sign as Sign
	 */
	public Sign getShopSign() {
		Block b = getShopLocation().getBlock();
		Sign s = null;

		if (ShopType.isShop(b)) {
			s = (Sign) b.getState();
		}
		return s;
	}

	/**
	 * Updates the text on the shops sign
	 */
	public void updateSign() {
		Sign s = getShopSign();

		if (s == null)
			return;

		String[] signLines = updateSignLines(Setting.SHOP_SIGN_DEFAULT_COLOURS.getMappedString(Signs.match(s.getType()).name()));

		for (int i = 0; i < 4; i++) {
			s.setLine(i, signLines[i]);
		}

		s.update();
	}

	/**
	 * Updates the text on the shops sign using the events sign if it matches location
	 */
	public void updateSign(SignChangeEvent event) {
		if (event == null || !event.getBlock().getLocation().equals(getShopLocation()))
			return;

		String[] signLines = updateSignLines(Setting.SHOP_SIGN_DEFAULT_COLOURS.getMappedString(Signs.match(event.getBlock().getType()).name()));

		for (int i = 0; i < 4; i++) {
			event.setLine(i, signLines[i]);
		}
	}

	/**
	 * Updates the text on the shops sign using the passed sign if it matches location
	 */
	public void updateSign(Sign sign) {
		if (sign == null || !sign.getLocation().equals(getShopLocation()))
			return;

		String[] signLines = updateSignLines(Setting.SHOP_SIGN_DEFAULT_COLOURS.getMappedString(Signs.match(sign.getType()).name()));

		for (int i = 0; i < 4; i++) {
			sign.setLine(i, signLines[i]);
		}

		sign.update();
	}

	/**
	 * Updates the text for the shop signs
	 *
	 * @return String array containing updated sign lines to be set
	 */
	private String[] updateSignLines(String defaultColour) {
		String[] signLines = new String[4];

		signLines[0] = utils.colorize((isMissingItems() ? Setting.SHOP_INCOMPLETE_COLOUR : Setting.SHOP_GOOD_COLOUR).getString() + shopType.toHeader());

		if (product.isEmpty()) {
			signLines[1] = "";
		} else if (product.size() == 1) {
			signLines[1] = itemLineFormatter(defaultColour, String.valueOf(product.get(0).getItemStack().getAmount()), " ", product.get(0).getCleanItemName());
		} else {
			signLines[1] = itemLineFormatter(defaultColour, Setting.MULTIPLE_ITEMS_ON_SIGN.getString().replace("%amount%", ""));
		}

		if (cost.isEmpty()) {
			signLines[2] = "";
		} else if (cost.size() == 1) {
			signLines[2] = itemLineFormatter(defaultColour, String.valueOf(cost.get(0).getItemStack().getAmount()), " ", cost.get(0).getCleanItemName());
		} else {
			signLines[2] = itemLineFormatter(defaultColour, Setting.MULTIPLE_ITEMS_ON_SIGN.getString().replace("%amount%", ""));
		}

		updateStatus();

		signLines[3] = status.getLine();

		for (int i = 0; i < 4; i++) {
			if (signLines[i] == null)
				signLines[i] = " ";
		}

		return signLines;
	}

	private String itemLineFormatter(String defaultColour, String... strings) {
		StringBuilder sb = new StringBuilder();

		for (String s : strings) {
			sb.append(s);
		}

		String colorFix = ChatColor.stripColor(utils.colorize(sb.toString()));

		return utils.colorize(defaultColour + colorFix.substring(0, Math.min(colorFix.length(), 15)));
	}

	/**
	 * Returns the shops inventory as a BlockState
	 *
	 * @return shops inventory as BlockState
	 */
	public BlockState getStorage() {
		try {
			return getInventoryLocation().getBlock().getState();
		} catch (NullPointerException npe) {
			return null;
		}
	}

	/**
	 * Removes the shops inventory from the shop
	 */
	public void removeStorage() {
		if (hasStorage()) {
			utils.PLUGIN.getDataStorage().removeChestLinkage(chestLoc);
			chestLoc = null;
		}
	}

	/**
	 * Returns if the shops inventory exists
	 *
	 * @return shops inventory as BlockState
	 */
	public boolean hasStorage() {
		return getStorage() != null;
	}

	/**
	 * Returns the shops status as ShopStatus
	 *
	 * @return Shops status as ShopStatus
	 */
	public ShopStatus getStatus() {
		return status;
	}

	/**
	 * Sets the shop to open if the shop has all necessary information to make a trade
	 *
	 * @return true if shop opened
	 */
	public ShopStatus setOpen() {
		setStatus(ShopStatus.OPEN);
		updateStatus();

		saveShop();
		updateSign();
		return status;
	}

	/**
	 * Automatically updates a shops status if it is not CLOSED
	 */
	public void updateStatus() {
		if (!status.equals(ShopStatus.CLOSED)) {
			if (!isMissingItems() && (chestLoc != null || shopType.isITrade())) {
				if (getAvailableTrades() > 0)
					setStatus(ShopStatus.OPEN);
				else
					setStatus(ShopStatus.OUT_OF_STOCK);
			} else {
				setStatus(ShopStatus.INCOMPLETE);
			}
		}
    }

	/**
	 * Sets the shops status to closed
	 */
    public void setStatus(ShopStatus newStatus) {
        status = newStatus;
	}

	/**
	 * Removes this shop from file
	 */
    public void remove() {
        purgeFromUserFiles();
		utils.PLUGIN.getDataStorage().removeShop(this);
    }

	/**
	 * Checks if shop is open
	 *
	 * @return true if open
	 */
    public boolean isTradeable() {
        return status.isTradingAllowed();
	}

	/**
	 * Switches the type of the shop between 'Trade' and 'BiTrade'
	 */
	public void switchType() {
		if (shopType == ShopType.TRADE)
			setShopType(ShopType.BITRADE);
		else if (shopType == ShopType.BITRADE)
			setShopType(ShopType.TRADE);

		saveShop();
		updateSign();
	}

	/**
	 * Checks if all Items in the list are valid for trade
	 *
	 * @param side  Which side of the trade should be checked for illegal items
	 * @param items List<ShopItemStack> to check
	 * @return true if all products are valid
	 */
	private boolean areItemsValid(ShopItemSide side, List<ShopItemStack> items) {
		for (ShopItemStack iS : items) {
			if (utils.isIllegal(side, iS.getItemStack().getType()))
				return false;
		}

		return true;
	}

	/**
	 * Updates the number of trades the shop can make
	 */
	public void updateFullTradeCount() {
		if (getShopType().equals(ShopType.ITRADE) && hasSide(ShopItemSide.PRODUCT)) {
			availableTrades = 999;
			return;
		}

		if (!hasStorage() || !hasSide(ShopItemSide.PRODUCT)) {
			availableTrades = 0;
			return;
		}

		Inventory shopInventory = getChestAsSC().getInventory();

		int count = countItems(getSideList(ShopItemSide.PRODUCT), shopInventory.getStorageContents());

		if (count == 0 && getShopType().equals(ShopType.BITRADE)) {
			count = countItems(getSideList(ShopItemSide.COST), shopInventory.getStorageContents());
		}

		availableTrades = count;
	}

	private int countItems(List<ShopItemStack> countItems, ItemStack[] storageContents) {
		Inventory storage = Bukkit.createInventory(null, storageContents.length);
		storage.setContents(storageContents);

		int totalCount = 0, currentCount = 0;

		for (ShopItemStack item : countItems) {
			totalCount += item.getItemStack().getAmount();
			int traded;
			for (ItemStack storageItem : storage.getStorageContents()) {
				if (storageItem != null && item.isSimilar(storageItem)) {
					traded = Math.min(storageItem.getAmount(), item.getItemStack().getMaxStackSize());

					storageItem.setAmount(traded);
					storage.removeItem(storageItem);
					currentCount += traded;
				}
			}
		}

		return (currentCount == 0 || totalCount == 0) ? 0 : currentCount / totalCount;
	}


	//region Setting Management - Methods for managing a shops settings
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Sets the Shops settings based on the ShopType and server defaults.
	 */
	public void setShopSettings() {
		setShopSettings(shopType);
	}

	/**
	 * Retrieves the specified setting if it is usable on the shop
	 */
	public ObjectHolder<?> getShopSetting(ShopSettingKeys settingKey) {
		return settingKey.isUsable(shopType) ? shopSettings.get(settingKey) : null;
	}

	/**
	 * Retrieves the specified setting if it is usable on the shop
	 */
	public boolean hasShopSetting(ShopSettingKeys settingKey) {
		return settingKey.isUsable(shopType) && shopSettings.containsKey(settingKey);
	}

	/**
	 * Retrieves the specified setting if it is usable on the shop
	 */
	public Map<ShopSettingKeys, ObjectHolder<?>> getShopSettings() {
		return shopSettings;
	}

	/**
	 * Sets the Shops settings based on the passed ShopType
	 */
	public void setShopSettings(ShopType type) {
		if (shopSettings == null)
			shopSettings = new HashMap<>();

		for (ShopSettingKeys settingKey : ShopSettingKeys.values()) {
			if (shopSettings.containsKey(settingKey) && !settingKey.isUsable(type)) {
				shopSettings.remove(settingKey);
			} else {
				shopSettings.putIfAbsent(settingKey, settingKey.getDefaultValue(type));
			}

			if (!settingKey.isUserEditable(type) && shopSettings.get(settingKey) != settingKey.getDefaultValue(type)) {
				shopSettings.put(settingKey, settingKey.getDefaultValue(type));
			}
		}
	}

	/**
	 * Retrieves the specified setting if it is usable on the shop
	 */
	public void setShopSettings(Map<ShopSettingKeys, ObjectHolder<?>> newSettings) {
		if (newSettings.size() > 0)
			new Utils().PLUGIN.getListManager().removeSkippableShop(getShopLocation());

		for (ShopSettingKeys settingKey : newSettings.keySet()) {
			if (settingKey.isUsable(shopType) && newSettings.get(settingKey) != null) {
				shopSettings.put(settingKey, newSettings.get(settingKey));
			}
		}
	}

	//------------------------------------------------------------------------------------------------------------------
	//endregion

	//region User Management - Methods for adding/deleting/updating/viewing a shops users
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Returns a list of all users for the shop based on specified roles.
	 *
	 * @param roles role to get users with
	 * @return list of ShopUsers.
	 */
	public List<ShopUser> getUsers(ShopRole... roles) {
		return getUsersExcluding(Collections.emptyList(), roles);
	}

	/**
	 * Returns a list of all users for the shop based on specified roles. Will exclude any users in the list.
	 *
	 * @param excludedPlayers player UUIDs to exclude from the results
	 * @param roles           role to get users with
	 * @return list of ShopUsers.
	 */
	public List<ShopUser> getUsersExcluding(List<UUID> excludedPlayers, ShopRole... roles) {
		List<ShopUser> users = new ArrayList<>();
		for (ShopRole role : roles) {
			switch (role) {
				case MEMBER:
					members.forEach(uuid -> {
						if (!excludedPlayers.contains(uuid)) {
							users.add(new ShopUser(uuid, role));
						}
					});
					break;
				case MANAGER:
					managers.forEach(uuid -> {
						if (!excludedPlayers.contains(uuid)) {
							users.add(new ShopUser(uuid, role));
						}
					});
					break;
				case OWNER:
					if (!excludedPlayers.contains(owner.getUUID()))
						users.add(owner);
					break;
			}
		}

		return users;
	}

	/**
	 * Returns a list of all user names for the shop based on specified roles.
	 *
	 * @param roles role to get users with
	 * @return List of specified users names
	 */
	public List<String> getUserNames(ShopRole... roles) {
		return getUsers(roles).stream().map(ShopUser::getName).collect(Collectors.toList());
	}

	/**
	 * Returns a list of all user UUIDs for the shop based on specified roles.
	 *
	 * @param roles role to get users with
	 * @return List of specified users names
	 */
	public List<UUID> getUsersUUID(ShopRole... roles) {
		List<UUID> users = new ArrayList<>();
		for (ShopRole role : roles) {
			switch (role) {
				case MEMBER:
					users.addAll(members);
					break;
				case MANAGER:
					users.addAll(managers);
					break;
				case OWNER:
					users.add(owner.getUUID());
					break;
			}
		}

		return users;
	}

	/**
	 * Returns true if the shop has users of the specified role
	 *
	 * @param role role to check for users
	 * @return true if users exist
	 */
	public boolean hasUsers(ShopRole role) {
		return !getUsers(role).isEmpty();
	}

	/**
	 * Returns the shops owner
	 *
	 * @return ShopUser object of owner
	 */
	public ShopUser getOwner() {
		return owner;
	}

	/**
	 * Sets the owner (don't know if this will ever be used)
	 *
	 * @param owner The new owner of the shop
	 */
	public void setOwner(ShopUser owner) {
		this.owner = owner;
	}

	/**
	 * Sets a user to the shop with the specified role after removing it if it was already on the shop
	 *
	 * @param newUser the player to be set
	 * @param role    role to set thge player to
	 * @return true if player has been set
	 */
	public boolean setUser(UUID newUser, ShopRole role) {
		removeUser(newUser);
		return addUser(newUser, role);
	}

	/**
	 * Adds a user to the shop with the specified role
	 *
	 * @param newUser the player to be added
	 * @param role    role to set thge player to
	 * @return true if player has been added
	 */
	public boolean addUser(UUID newUser, ShopRole role) {
		if (getUsers(ShopRole.MANAGER, ShopRole.MEMBER).size() >= Setting.MAX_SHOP_USERS.getInt())
			return false;

		if (!getUsersUUID(ShopRole.MANAGER, ShopRole.MEMBER).contains(newUser)) {
			switch (role) {
				case MANAGER:
					saveShop(managers.add(newUser));
					return true;
				case MEMBER:
					saveShop(members.add(newUser));
					return true;
			}
		}

		return false;
	}

	/**
	 * Updates all shop users
	 */
	public void updateShopUsers(Set<ShopUser> updatedUserSet) {
		for (ShopUser user : updatedUserSet) {
			removeUser(user.getUUID());
			addUser(user.getUUID(), user.getRole());
		}
		saveShop();
	}

	/**
	 * Returns the ShopRole of the supplied UUID
	 *
	 * @param uuidToCheck uuid to check for role of
	 * @return the ShopRole of the supplied UUID
	 */
	public ShopRole checkRole(UUID uuidToCheck) {
		if (owner.getUUID().equals(uuidToCheck)) {
			return ShopRole.OWNER;
		} else if (managers.contains(uuidToCheck)) {
			return ShopRole.MANAGER;
		} else if (members.contains(uuidToCheck)) {
			return ShopRole.MEMBER;
		} else {
			return ShopRole.SHOPPER;
		}
	}

	/**
	 * Removes a user from the shop
	 *
	 * @param oldUser the UUID of the player to be removed
	 * @return true if user was removed
	 */
	public boolean removeUser(UUID oldUser) {
		boolean ret = false;
		if (getUsersUUID(ShopRole.MANAGER).contains(oldUser)) {
			managers.remove(oldUser);
			ret = true;
		}

		if (getUsersUUID(ShopRole.MEMBER).contains(oldUser)) {
			members.remove(oldUser);
			ret = true;
		}

		saveShop(ret);

		return ret;
	}

	/**
	 * Sets the a list of users to a role
	 *
	 * @param users the managers to be set to the shop
	 */
	public boolean setUsers(Set<UUID> users, ShopRole role) {
		boolean ret = false;
		switch (role) {
			case MANAGER:
				users.forEach(this::removeUser);
				managers = users;
				ret = true;
			case MEMBER:
				users.forEach(this::removeUser);
				members = users;
				ret = true;
		}

		saveShop(ret);

		return ret;
	}

	/**
	 * Updates the saved player data for all users
	 */
	private void updateUserFiles() {
		TradeShop plugin = new Utils().PLUGIN;
		for (UUID user : getUsersUUID(ShopRole.OWNER, ShopRole.MANAGER, ShopRole.MEMBER)) {
			PlayerSetting playerSetting = plugin.getDataStorage().loadPlayer(user);
			playerSetting.updateShop(this);
			plugin.getDataStorage().savePlayer(playerSetting);
		}
	}

	/**
	 * Removes this shop from all users
	 */
	private void purgeFromUserFiles() {
		TradeShop plugin = new Utils().PLUGIN;
		for (UUID user : getUsersUUID(ShopRole.OWNER, ShopRole.MANAGER, ShopRole.MEMBER)) {
			PlayerSetting playerSetting = plugin.getDataStorage().loadPlayer(user);
			playerSetting.removeShop(this);
			plugin.getDataStorage().savePlayer(playerSetting);
		}
	}


	//------------------------------------------------------------------------------------------------------------------
	//endregion

	//region Item Management - Methods for adding/deleting/updating/viewing Cost and Product lists
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Checks if shop has a bad side and fixes if necessary
	 */
	public void fixSide(ShopItemSide side) {
		List<ShopItemStack> ogItems = (side.equals(ShopItemSide.PRODUCT) ? product : cost);

		Set<Material> matSet = new HashSet<>();

		ogItems.forEach((item) -> matSet.add(item.getItemStack().getType()));


		if (ogItems.size() > 1 && ogItems.size() != matSet.size()) {
			List<ShopItemStack> scrapList = new ArrayList<>(ogItems);
			(side.equals(ShopItemSide.PRODUCT) ? product : cost).clear();
			(side.equals(ShopItemSide.PRODUCT) ? product : cost).clear();

			ogItems.forEach((item) -> {
				while (scrapList.contains(item)) {
					addSideItem(side, item);
					scrapList.remove(scrapList.lastIndexOf(item));
				}
			});

			updateFullTradeCount();
			updateSign();
			saveShop();
		}
	}

	/**
	 * Checks if shop has items on specified side
	 *
	 * @return True if side != null
	 */
	public boolean hasSide(ShopItemSide side) {
		return getSide(side).size() > 0;
	}

	/**
	 * Checks if shop has necessary items to make a trade
	 *
	 * @return true if items are missing
	 */
	public boolean isMissingItems() {
		return isNoCost() ? product.isEmpty() : product.isEmpty() || cost.isEmpty();
	}

	/**
	 * Checks if the shop is set to not use cost
	 *
	 * @return true if cost is not needed
	 */
	public boolean isNoCost() {
		if (!ShopSettingKeys.NO_COST.isUsable(getShopType())) return false;

		Boolean noCost = shopSettings.get(ShopSettingKeys.NO_COST).asBoolean();
		return noCost != null ? noCost : false;
	}

	/**
	 * Returns the ItemStacks on specified side
	 *
	 * @param side Side to get ItemStacks form
	 * @return ItemStack List
	 */
	public List<ItemStack> getSideItemStacks(ShopItemSide side) {
		List<ItemStack> ret = new ArrayList<>();
		for (ShopItemStack itm : getSide(side))
			ret.add(itm.getItemStack().clone());
		return ret;
	}

	/**
	 * Returns the appropriate list for the side of the trade for object internal use
	 *
	 * @return Side ShopItemStack List
	 */
	private List<ShopItemStack> getSide(ShopItemSide side) {
		return side.equals(ShopItemSide.PRODUCT) ? product : cost;
	}

	/**
	 * Returns a list of the items on the shops specified side
	 *
	 * @param side Side to get items for
	 * @return Side ShopItemStack List
	 */
	public List<ShopItemStack> getSideList(ShopItemSide side) {
		return getSideList(side, false);
	}

	/**
	 * Returns a list of the items on the shops specified side, or the opposite side it doBiTradeAlternate is true
	 *
	 * @param side               Side to get items for
	 * @param doBiTradeAlternate Should side be flipped(for biTrade + Left Click)
	 * @return Side ShopItemStack List
	 */
	public List<ShopItemStack> getSideList(ShopItemSide side, boolean doBiTradeAlternate) {
		List<ShopItemStack> ret = new ArrayList<>();
		for (ShopItemStack itm : getSide(doBiTradeAlternate ? side.getReverse() : side))
			ret.add(itm.clone());
		return ret;
	}

	/**
	 * Removes the item at the index of the specified side
	 *
	 * @param side  side of the trade to remove item from
	 * @param index index of item to remove
	 * @return true if Item is removed from the specified Side
	 */
	public boolean removeSideIndex(ShopItemSide side, int index) {
		if (getSide(side).size() > index) {
			getSide(side).remove(index);

			saveShop();
			updateSign();
			return true;
		}

		return false;
	}

	/**
	 * Sets the Item for the specified side of the trade
	 *
	 * @param side    Side of the trade to set
	 * @param newItem ItemStack to be set
	 */
	public void setSideItems(ShopItemSide side, ItemStack newItem) {
		if (utils.isIllegal(side, newItem.getType()))
			return;

		getSide(side).clear();

		addSideItem(side, newItem);
	}

	/**
	 * Adds more items to the specified side
	 *
	 * @param side        Side to add items to
	 * @param newShopItem ShopItemStack to be added
	 */
	public void addSideItem(ShopItemSide side, ShopItemStack newShopItem) {
		if (utils.isIllegal(side, newShopItem.getItemStack().getType()))
			return;

		int toRemoveShopItemStack = -1;


		for (int i = 0; i < getSide(side).size(); i++) {
			if (getSideList(side).get(i).getItemStack().getType().equals(newShopItem.getItemStack().getType()) && getSideList(side).get(i).isSimilar(newShopItem.getItemStack())) {
				toRemoveShopItemStack = i;
				newShopItem = getSideList(side).get(i).clone();
				newShopItem.setAmount(getSideList(side).get(i).getAmount() + newShopItem.getItemStack().getAmount());
				break;
			}
		}

		if (toRemoveShopItemStack > -1)
			getSide(side).remove(toRemoveShopItemStack);

		getSide(side).add(newShopItem);
		//*/


		if (!getShopType().isITrade() && chestLoc != null)
			getSide(side).removeIf(item -> item.getItemStack().getType().toString().endsWith("SHULKER_BOX") && getInventoryLocation().getBlock().getType().toString().endsWith("SHULKER_BOX"));

		saveShop();
		updateSign();
	}

	/**
	 * Adds more items to the specified side
	 *
	 * @param side    Side to add items to
	 * @param newItem ItemStack to be added
	 */
	public void addSideItem(ShopItemSide side, ItemStack newItem) {
		addSideItem(side, new ShopItemStack(newItem));
	}

	/**
	 * Checks if the shop has sufficient stock to make a trade on specified side
	 */
	public boolean hasSideStock(ShopItemSide side) {
		return !shopType.isITrade() && hasSide(side) && getChestAsSC() != null && getChestAsSC().hasStock(getSideList(side));
	}

	/**
	 * Checks if all Items on the specified side are valid for trade
	 *
	 * @param side Side to check
	 * @return true if all Items are valid
	 */
	public boolean isSideValid(ShopItemSide side) {
		return areItemsValid(side, getSideList(side));
	}

	/**
	 * Updates list on specified side
	 *
	 * @param side            Side to be updated
	 * @param updatedItemList list to set updatedItemList to
	 */
	public void updateSide(ShopItemSide side, List<ShopItemStack> updatedItemList) {
		if (!getShopType().isITrade() && chestLoc != null)
			updatedItemList.removeIf(item -> item.getItemStack().getType().toString().endsWith("SHULKER_BOX") && getInventoryLocation().getBlock().getType().toString().endsWith("SHULKER_BOX"));

		if (side.equals(ShopItemSide.PRODUCT)) product = updatedItemList;
		else cost = updatedItemList;

		saveShop();
		updateSign();
	}

	/**
	 * Updates item on specified side at index
	 *
	 * @param side        Side to be updated
	 * @param updatedItem Item to be updated at the specified index and side
	 * @param index       index of the item t be updated
	 */
	public void updateSideItem(ShopItemSide side, ShopItemStack updatedItem, int index) {
		if (!getShopType().isITrade() &&
				chestLoc != null &&
				updatedItem.getItemStack().getType().toString().endsWith("SHULKER_BOX") &&
				getInventoryLocation().getBlock().getType().toString().endsWith("SHULKER_BOX"))
			return;

		(side.equals(ShopItemSide.PRODUCT) ? product : cost).set(index, updatedItem);

		saveShop();
		updateSign();
	}

	//------------------------------------------------------------------------------------------------------------------
	//endregion
}