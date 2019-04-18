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

package org.shanerx.tradeshop.objects;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.enumys.ShopRole;
import org.shanerx.tradeshop.enumys.ShopStatus;
import org.shanerx.tradeshop.enumys.ShopType;
import org.shanerx.tradeshop.utils.ItemSerializer;
import org.shanerx.tradeshop.utils.JsonConfiguration;
import org.shanerx.tradeshop.utils.Tuple;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class Shop implements Serializable {

    private ShopUser owner;
    private List<ShopUser> managers, members;
	private ShopType shopType;
	private ShopLocation shopLoc, chestLoc;
	private transient ItemStack product, cost;
	private String productB64, costB64;
	private ShopStatus status = ShopStatus.CLOSED;

	/**
	 * Creates a Shop object
	 *
	 * @param locations Location of shop sign and chest as Tuple, left = Sign location, right = inventory location
	 * @param owner     Owner of the shop as a ShopUser
	 * @param shopType  Type of the shop as ShopType
	 * @param items     Items to go into the shop as Tuple, left = Product, right = Cost
	 * @param players   Users to be added to the shop as Tuple, left = Managers, right = Members
	 */
	public Shop(Tuple<Location, Location> locations, ShopType shopType, ShopUser owner, Tuple<List<ShopUser>, List<ShopUser>> players, Tuple<ItemStack, ItemStack> items) {
		shopLoc = new ShopLocation(locations.getLeft());
		this.owner = owner;
		chestLoc = new ShopLocation(locations.getRight());
		this.shopType = shopType;
		managers = players.getLeft();
		members = players.getRight();
		product = items.getLeft();
		cost = items.getRight();

		productB64 = ItemSerializer.itemStackArrayToBase64(product);
		costB64 = ItemSerializer.itemStackArrayToBase64(cost);
	}

	/**
	 * Creates a Shop object
	 *
	 * @param locations Location of shop sign and chest as Tuple, left = Sign location, right = inventory location
	 * @param owner Owner of the shop as a ShopUser
	 * @param shopType Type of the shop as ShopType
	 */
	public Shop(Tuple<Location, Location> locations, ShopType shopType, ShopUser owner) {
		shopLoc = new ShopLocation(locations.getLeft());
		this.owner = owner;
		chestLoc = new ShopLocation(locations.getRight());
		this.shopType = shopType;
		managers = Collections.emptyList();
		members = Collections.emptyList();
		productB64 = "";
		costB64 = "";
	}

	/**
	 * Deserializes the object to Json using Gson
	 *
	 * @return Shop object from file
	 */
	public static Shop deserialize(String serialized) {
		Shop shop = new Gson().fromJson(serialized, Shop.class);
		shop.itemsFromB64();

		return shop;
	}

	/**
	 * Loads a shop from file and returns the Shop object
	 *
	 * @param loc Location of the shop sign
	 * @return The shop from file
	 */
	public static Shop loadShop(ShopLocation loc) {
		return loadShop(loc.serialize());
	}

	/**
	 * Retrieves the Shop object based on a serialized ShopLocation of the sign
	 *
	 * @param serializedShopLocation ShopLocation in serialized string
	 * @return Shop object from file
	 */
	public static Shop loadShop(String serializedShopLocation) {
		return loadShop(ShopLocation.deserialize(serializedShopLocation));
	}

	/**
	 * Loads the shop from file
	 *
	 * @param s Shop sign to load from
	 * @return Shop Object
	 */
	public static Shop loadShop(Sign s) {
		JsonConfiguration json = new JsonConfiguration(s.getLocation().getChunk());

		return json.loadShop(new ShopLocation(s.getLocation()));
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
	 * Returns the managers
	 *
	 * @return List of managers as ShopUser
	 */
	public List<ShopUser> getManagers() {
		return managers;
	}

	/**
	 * Sets the managers
	 *
	 * @param managers the managers to be set to the shop
	 */
	public void setManagers(List<ShopUser> managers) {
		this.managers = managers;
	}

	/**
	 * Returns a list of all users for the shop, including; owners, managers, and members.
	 *
	 * @return the sign.
	 */
	public List<ShopUser> getUsers() {
		List<ShopUser> users = new ArrayList<>();
		users.add(owner);
		users.addAll(managers);
		users.addAll(members);
		return users;
	}

	/**
	 * Returns the storage block as a ShopChest
	 *
	 * @return Shop storage as ShopChest
	 */
	public ShopChest getChestAsSC() {
		return new ShopChest(chestLoc.getLocation());
	}

	/**
	 * Gets the members of the shop as ShopUser
	 *
	 * @return List of members as ShopUser
	 */
	public List<ShopUser> getMembers() {
		return members;
	}

	/**
	 * Sets the members
	 *
	 * @param members the members to be set to the shop
	 */
	public void setMembers(List<ShopUser> members) {
		this.members = members;
	}

	/**
	 * Adds a manager to the shop
	 *
	 * @param newManager the player to be added as a shopUser object
	 * @return true if player has been added
	 */
	public boolean addManager(ShopUser newManager) {
		if (!getUsersUUID().contains(newManager.getUUID())) {
			managers.add(newManager);
			saveShop();
			return true;
		}
		return false;
	}

	/**
	 * Removes a manager from the shop
	 *
	 * @param oldManager the player to be removes as a shopUser object
	 */
	public void removeManager(ShopUser oldManager) {
		managers.remove(oldManager);
		saveShop();
	}

	/**
	 * Adds a member to the shop
	 *
	 * @param newMember the player to be added as a shopUser object
	 * @return true if player has been added
	 */
	public boolean addMember(ShopUser newMember) {
		if (!getUsersUUID().contains(newMember.getUUID())) {
			members.add(newMember);
			saveShop();
			return true;
		}
		return false;
    }

	/**
	 * Returns a list of managers uuid
	 *
	 * @return List of all managers uuid
	 */
	public List<UUID> getManagersUUID() {
		return managers.stream().map(ShopUser::getUUID).collect(Collectors.toList());
	}

	/**
	 * Returns a list of all members uuid
	 *
	 * @return list of member uuid
	 */
	public List<UUID> getMembersUUID() {
		return members.stream().map(ShopUser::getUUID).collect(Collectors.toList());
	}

	/**
	 * Fills the manager list from uuid values in file
	 *
	 * @param uuids uuid list from file
	 * @return list of managers as ShopUser
	 */
	private List<ShopUser> managersFromUUIDs(String... uuids) {
		List<ShopUser> managers = new ArrayList<>();
		for (String str : uuids) {
			managers.add(new ShopUser(Bukkit.getPlayer(UUID.fromString(str)), ShopRole.MANAGER));
		}

		return managers;
	}

	/**
	 * Fills the member list from uuid values in file
	 *
	 * @param uuids uuid list from file
	 * @return list of members as ShopUser
	 */
	private List<ShopUser> membersFromUUIDs(String... uuids) {
		List<ShopUser> members = new ArrayList<>();
		for (String str : uuids) {
			members.add(new ShopUser(Bukkit.getPlayer(UUID.fromString(str)), ShopRole.MEMBER));
		}

		return members;
	}

	/**
	 * Returns location of shops inventory
	 *
	 * @return inventory location as Location
	 */
	public Location getInventoryLocation() {
		return chestLoc.getLocation();
	}

	/**
	 * Sets the inventory location
	 *
	 * @param newLoc new location to set
	 */
	public void setInventoryLocation(Location newLoc) {
		chestLoc = new ShopLocation(newLoc);
	}

	/**
	 * Returns the location of the shop sign
	 *
	 * @return Location of the shops sign
	 */
	public Location getShopLocation() {
		return shopLoc.getLocation();
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
	 * returns the cost item
	 *
	 * @return Cost item
	 */
	public ItemStack getCost() {
		return cost;
	}

	/**
	 * Sets the cost item
	 *
	 * @param newItem ItemStack to be set
	 */
	public void setCost(ItemStack newItem) {
		cost = newItem;
		costB64 = ItemSerializer.itemStackArrayToBase64(cost);
	}

	/**
	 * Returns the amount of the product item
	 *
	 * @return int amount of product item
	 */
	public int getProductAmt() {
		return product.getAmount();
	}

	/**
	 * Sets the product items amount
	 *
	 * @param amount int to be set to the product items amount
	 */
	public void setProductAmt(int amount) {
		product.setAmount(amount);
	}

	/**
	 * Returns the amount of the cost item
	 *
	 * @return int amount of cost item
	 */
	public int getCostAmt() {
		return cost.getAmount();
	}

	/**
	 * Sets the cost items amount
	 *
	 * @param amount int to be set to the cost items amount
	 */
	public void setCostAmt(int amount) {
		cost.setAmount(amount);
	}

	/**
	 * Checks if shop has product
	 *
	 * @return True if product != null
	 */
	public boolean hasProduct() {
		return product != null;
	}

	/**
	 * Checks if shop has cost
	 *
	 * @return True if cost != null
	 */
	public boolean hasCost() {
		return cost != null;
	}

	/**
	 * Sets the product item
	 *
	 * @param newItem item to be set to product
	 */
	public void setProduct(ItemStack newItem) {
		product = newItem;
		productB64 = ItemSerializer.itemStackArrayToBase64(product);
	}

	/**
	 * Returns the product item
	 *
	 * @return Product ItemStack
	 */
	public ItemStack getProduct() {
		return product;
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
	 * Converts Base64 String to itemstack values
	 */
	public void itemsFromB64() {
		if (productB64.length() > 0 && product == null) {
			try {
				product = ItemSerializer.itemStackArrayFromBase64(productB64);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		if (costB64.length() > 0 && cost == null) {
			try {
				cost = ItemSerializer.itemStackArrayFromBase64(costB64);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
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
		itemsFromB64();
		shopLoc.stringToWorld();
		chestLoc.stringToWorld();
	}

	/**
	 * Remove a member from the shop
	 *
	 * @param oldMember Member to be removed
	 */
	public void removeMember(ShopUser oldMember) {
		members.remove(oldMember);
		saveShop();
	}

	/**
	 * Gets all users of shop as UUID List
	 *
	 * @return List of all users as UUIDs
	 */
	public List<UUID> getUsersUUID() {
		return getUsers().stream().map(ShopUser::getUUID).collect(Collectors.toList());
	}

	/**
	 * Saves the shop too file
	 */
	public void saveShop() {
		JsonConfiguration json = new JsonConfiguration(shopLoc.getLocation().getChunk());

		json.saveShop(this);
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

		if (!missingItems()) {
			s.setLine(0, ChatColor.DARK_GREEN + shopType.toHeader());
		} else {
			s.setLine(0, ChatColor.GRAY + shopType.toHeader());
		}

		if (product != null) {
			if (product.hasItemMeta() && product.getItemMeta().hasDisplayName())
				s.setLine(1, product.getAmount() + " " + product.getItemMeta().getDisplayName());
			else
				s.setLine(1, product.getAmount() + " " + product.getType().toString());
		}

		if (cost != null) {
			if(cost.hasItemMeta() && cost.getItemMeta().hasDisplayName())
				s.setLine(1, cost.getAmount() + " " + cost.getItemMeta().getDisplayName());
			else
				s.setLine(2, cost.getAmount() + " " + cost.getType().toString());
		}

		s.setLine(3, status.getLine());
		s.update();
	}

	/**
	 * Returns the shops inventory as a BlockState
	 *
	 * @return shops inventory as BlockState
	 */
	public BlockState getInventory() {
		try {
			return getInventoryLocation().getBlock().getState();
		} catch (NullPointerException npe) {
			return null;
		}
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
	public boolean setOpen() {
		boolean ret;

		if (!missingItems()) {
			status = ShopStatus.OPEN;
			ret = true;
		} else {
			status = ShopStatus.CLOSED;
			ret = false;
		}

		updateSign();
		return ret;
	}

	/**
	 * Checks if shop has necessary items to make a trade
	 *
	 * @return true if items are missing
	 */
	public boolean missingItems() {
		return product == null || cost == null;
	}

	/**
	 * Sets the shops status to closed
	 */
	public void setClosed() {
		status = ShopStatus.CLOSED;
		updateSign();
	}

	/**
	 * Removes this shop from file
	 */
	public void remove() {
		JsonConfiguration json = new JsonConfiguration(shopLoc.getLocation().getChunk());

		json.removeShop(shopLoc);
	}

	/**
	 * Checks if shop is open
	 *
	 * @return true if open
	 */
	public boolean isOpen() {
		return status == ShopStatus.OPEN;
	}

	/**
	 * Switches the type of the shop between 'Trade' and 'BiTrade'
	 */
	public void switchType() {
		if (shopType == ShopType.TRADE)
			setShopType(ShopType.BITRADE);
		else if (shopType == ShopType.BITRADE)
			setShopType(ShopType.TRADE);

		updateSign();
		saveShop();
	}
}