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

import com.google.gson.Gson;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import org.shanerx.tradeshop.shoplocation.ShopLocation;
import org.shanerx.tradeshop.utils.Utils;
import org.shanerx.tradeshop.utils.objects.Tuple;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Shop implements Serializable {

	private ShopUser owner;
	private List<UUID> managers, members;
	private ShopType shopType;
	private final ShopLocation shopLoc;
	private List<ShopItemStack> product, cost;
	private ShopLocation chestLoc;
	private transient SignChangeEvent signChangeEvent;
	private transient Inventory storageInv;
	private transient Utils utils = new Utils();
	private ShopStatus status = ShopStatus.INCOMPLETE;

	private int availableTrades = 0;

	/**
	 * Creates a Shop object
	 *
	 * @param locations Location of shop sign and chest as Tuple, left = Sign location, right = inventory location
	 * @param owner     Owner of the shop as a ShopUser
	 * @param shopType  Type of the shop as ShopType
	 * @param items     Items to go into the shop as Tuple, left = Product, right = Cost
	 * @param players   Users to be added to the shop as Tuple, left = Managers, right = Members
	 */
	public Shop(Tuple<Location, Location> locations, ShopType shopType, ShopUser owner, Tuple<List<UUID>, List<UUID>> players, Tuple<ItemStack, ItemStack> items) {
		shopLoc = new ShopLocation(locations.getLeft());
		this.owner = owner;

		if (locations.getRight() != null) {
			chestLoc = new ShopLocation(locations.getRight());
			utils.PLUGIN.getDataStorage().addChestLinkage(chestLoc, shopLoc);
		}

		this.shopType = shopType;

		managers = players.getLeft() == null ? Collections.emptyList() : players.getLeft();
		members = players.getRight() == null ? Collections.emptyList() : players.getRight();

		product = new ArrayList<>();
		cost = new ArrayList<>();

		if (items.getLeft() != null) product.add(new ShopItemStack(items.getLeft()));
		if (items.getRight() != null) cost.add(new ShopItemStack(items.getRight()));

		fixAfterLoad();
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
	 * Deserializes the object to Json using Gson
	 *
	 * @param serialized Shop GSON to be deserialized
	 *
	 * @return Shop object from file
	 */
	public static Shop deserialize(String serialized) {
		Shop shop = new Gson().fromJson(serialized, Shop.class);
		shop.fixAfterLoad();

		return shop;
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
	 * Adds the sign change event to the shop to be used during sign update
	 *
	 * @param event The SignChangeEvent to hold
	 */
	public void setEvent(SignChangeEvent event) {
		this.signChangeEvent = event;
	}

	/**
	 * Removes the SignChangeEvent from the shop(Should be done before leaving the event)
	 */
	public void removeEvent() {
		this.signChangeEvent = null;
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
	 * Serializes the object to Json using Gson
	 *
	 * @return serialized string
	 */
	public String serialize() {
		return new Gson().toJson(this);
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
	 * Fixes values that cannot be serialized after loading
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

		if (getShopSign() != null)
			updateSign();
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
		if (signChangeEvent != null) {
			updateSign(signChangeEvent);
			removeEvent();
		} else {
			Sign s = getShopSign();

			if (s != null) {
				String[] signLines = updateSignLines();

				for (int i = 0; i < 4; i++) {
					if (signLines[i] != null)
						s.setLine(i, signLines[i]);
				}

				s.update();
			}
		}
	}

	/**
     * Updates the text on the shops sign during SignChangeEvent
	 *
	 * @param signEvent SignEvent to update the sign for
	 */
	public void updateSign(SignChangeEvent signEvent) {
        String[] signLines = updateSignLines();

        for (int i = 0; i < 4; i++) {
            signEvent.setLine(i, signLines[i]);
		}
	}

    /**
     * Updates the text for the shop signs
     *
     * @return String array containing updated sign lines to be set
     */
    private String[] updateSignLines() {
		String[] signLines = new String[4];

        if (isMissingItems()) {
            signLines[0] = utils.colorize(Setting.SHOP_INCOMPLETE_COLOUR.getString() + shopType.toHeader());
        } else {
            signLines[0] = utils.colorize(Setting.SHOP_GOOD_COLOUR.getString() + shopType.toHeader());
        }

		if (product.isEmpty()) {
			signLines[1] = "";
		} else if (product.size() == 1) {
			StringBuilder sb = new StringBuilder();

			ShopItemStack item = product.get(0);

			sb.append(item.getItemStack().getAmount());
			sb.append(" ");

			sb.append(item.getCleanItemName());

			signLines[1] = sb.substring(0, Math.min(sb.length(), 15));

        } else {
			signLines[1] = Setting.MULTIPLE_ITEMS_ON_SIGN.getString().replace("%amount%", "");
        }

		if (cost.isEmpty()) {
			signLines[2] = "";
		} else if (cost.size() == 1) {
			StringBuilder sb = new StringBuilder();

			ShopItemStack item = cost.get(0);

			sb.append(item.getItemStack().getAmount());
			sb.append(" ");

			sb.append(item.getCleanItemName());

			signLines[2] = sb.substring(0, Math.min(sb.length(), 15));
		} else {
			signLines[2] = Setting.MULTIPLE_ITEMS_ON_SIGN.getString();
		}

		signLines[1] = ChatColor.stripColor(signLines[1]);
		signLines[2] = ChatColor.stripColor(signLines[2]);

		updateStatus();

		signLines[3] = status.getLine();

		return signLines;
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
				if (shopType.isITrade() || hasSideStock(ShopItemSide.PRODUCT) || (shopType.isBiTrade() && hasSideStock(ShopItemSide.COST)))
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
		if (!hasStorage() || !hasSide(ShopItemSide.PRODUCT)) {
			availableTrades = 0;
			return;
		}

		Inventory shopInventory = hasStorage() ? getChestAsSC().getInventory() : null;

		Inventory clone = Bukkit.createInventory(null, shopInventory.getStorageContents().length);
		clone.setContents(shopInventory.getStorageContents());
		int totalCount = 0, currentCount = 0;

		for (ShopItemStack item : getSideList(ShopItemSide.PRODUCT)) {
			totalCount += item.getItemStack().getAmount();
			int traded;
			for (ItemStack storageItem : clone.getStorageContents()) {
				if (storageItem != null && item.isSimilar(storageItem)) {
					traded = Math.min(storageItem.getAmount(), item.getItemStack().getMaxStackSize());

					storageItem.setAmount(traded);
					clone.removeItem(storageItem);
					currentCount += traded;
				}
			}
		}

		availableTrades = currentCount == 0 || totalCount == 0 ? 0 : currentCount / totalCount;
	}

	//region User Management - Methods for adding/deleting/updating/viewing a shops users
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Returns a list of all users for the shop based on specified roles.
	 *
	 * @param roles role to get users with
	 * @return list of ShopUsers.
	 */
	public List<ShopUser> getUsers(ShopRole... roles) {
		List<ShopUser> users = new ArrayList<>();
		for (ShopRole role : roles) {
			switch (role) {
				case MEMBER:
					members.forEach(uuid -> users.add(new ShopUser(uuid, ShopRole.MEMBER)));
					break;
				case MANAGER:
					members.forEach(uuid -> users.add(new ShopUser(uuid, ShopRole.MANAGER)));
					break;
				case OWNER:
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
	 * Adds a user to the shop with the specified role
	 *
	 * @param newUser the player to be added as a shopUser object
	 * @param role    role to set thge player to
	 * @return true if player has been added
	 */
	public boolean addUser(UUID newUser, ShopRole role) {
		boolean ret = false;
		if (!getUsersUUID().contains(newUser)) {
			switch (role) {
				case MANAGER:
					managers.add(newUser);
					ret = true;
				case MEMBER:
					members.add(newUser);
					ret = true;
			}
		}

		if (ret) saveShop();

		return ret;
	}

	/**
	 * Updates all shop users
	 */
	public void updateShopUsers(Set<ShopUser> updatedUserSet) {
		for (ShopUser user : updatedUserSet) {
			removeUser(user.getUUID());
			switch (user.getRole()) {
				case MANAGER:
					managers.add(user.getUUID());
					break;
				case MEMBER:
					members.add(user.getUUID());
					break;
				default:
					break;
			}
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

		saveShop();

		return ret;
	}

	/**
	 * Sets the a list of users to a role
	 *
	 * @param users the managers to be set to the shop
	 */
	public boolean setUsers(List<UUID> users, ShopRole role) {
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

		if (ret) saveShop();

		return ret;
	}

	/**
	 * Updates the saved player data for all users
	 */
	private void updateUserFiles() {
		TradeShop plugin = new Utils().PLUGIN;
		for (UUID user : getUsersUUID()) {
			PlayerSetting playerSetting = plugin.getDataStorage().loadPlayer(user);
			playerSetting.updateShops(this);
			plugin.getDataStorage().savePlayer(playerSetting);
		}
	}

	/**
	 * Removes this shop from all users
	 */
	private void purgeFromUserFiles() {
		TradeShop plugin = new Utils().PLUGIN;
		for (UUID user : getUsersUUID()) {
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
		return shopType.equals(ShopType.ITRADE) ? product.isEmpty() : product.isEmpty() || cost.isEmpty();
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
	 * @param side    Side to add items to
	 * @param newItem ItemStack to be added
	 */
	public void addSideItem(ShopItemSide side, ItemStack newItem) {
		if (utils.isIllegal(side, newItem.getType()))
			return;

		///* Added stacks are not separated and are added ontop of existing similar stacks
		ShopItemStack toAddShopItemStack = new ShopItemStack(newItem);
		int toRemoveShopItemStack = -1;


		for (int i = 0; i < getSide(side).size(); i++) {
			if (getSideList(side).get(i).getItemStack().getType().equals(newItem.getType()) && getSideList(side).get(i).isSimilar(newItem)) {
				toRemoveShopItemStack = i;
				toAddShopItemStack = getSideList(side).get(i).clone();
				toAddShopItemStack.setAmount(getSideList(side).get(i).getAmount() + newItem.getAmount());
				break;
			}
		}

		if (toRemoveShopItemStack > -1)
			getSide(side).remove(toRemoveShopItemStack);

		getSide(side).add(toAddShopItemStack);
		//*/


		if (!getShopType().isITrade() && chestLoc != null)
			getSide(side).removeIf(item -> item.getItemStack().getType().toString().endsWith("SHULKER_BOX") && getInventoryLocation().getBlock().getType().toString().endsWith("SHULKER_BOX"));

		saveShop();
		updateSign();
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
	 * @param updatedCostList list to set as new CostList
	 */
	public void updateSide(ShopItemSide side, List<ShopItemStack> updatedCostList) {
		if (!getShopType().isITrade() && chestLoc != null)
			updatedCostList.removeIf(item -> item.getItemStack().getType().toString().endsWith("SHULKER_BOX") && getInventoryLocation().getBlock().getType().toString().endsWith("SHULKER_BOX"));
		if (side.equals(ShopItemSide.PRODUCT)) {
			product = updatedCostList;
		} else {
			cost = updatedCostList;
		}

		saveShop();
		updateSign();
	}

	//------------------------------------------------------------------------------------------------------------------
	//endregion
}