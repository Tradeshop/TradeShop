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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.item.IllegalItemList;
import org.shanerx.tradeshop.item.NonObtainableMaterials;
import org.shanerx.tradeshop.item.ShopItemSide;
import org.shanerx.tradeshop.shop.ShopStorage;
import org.shanerx.tradeshop.utils.debug.DebugLevels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class ListManager extends Utils {

	private final IllegalItemList globalList = new IllegalItemList(IllegalItemList.ListType.DISABLED, new ArrayList<>());
	private final IllegalItemList costList = new IllegalItemList(IllegalItemList.ListType.DISABLED, new ArrayList<>());
	private final IllegalItemList productList = new IllegalItemList(IllegalItemList.ListType.DISABLED, new ArrayList<>());

	private final ArrayList<BlockFace> directions = new ArrayList<>();
	private final ArrayList<ShopStorage.Storages> inventories = new ArrayList<>();
	private final ArrayList<String> gameMats = new ArrayList<>();
	private final ArrayList<String> addOnMats = new ArrayList<>();

	private Cache<Location, Boolean> skippableHoppers; //second data type doesn't matter and isn't used, boolean chosen as it is the smallest


	public ListManager() {
		reload();
		setGameMatList();
	}

	private void initSkippableHoppers() {
		skippableHoppers = CacheBuilder.newBuilder()
				.maximumSize(100000)
				.expireAfterAccess(1000, TimeUnit.MILLISECONDS)
				.build();
	}

	public Boolean canSkipHopper(Location location) {
		if (skippableHoppers.getIfPresent(location) == null)
			return null;
		else
			return skippableHoppers.getIfPresent(location);
	}

	public void addSkippableHopper(Location location, boolean shouldBlock) {
		skippableHoppers.put(location, shouldBlock);
	}


	public ArrayList<BlockFace> getDirections() {
		return directions;
	}

	public ArrayList<ShopStorage.Storages> getInventories() {
		return inventories;
	}

	public IllegalItemList getGlobalList() {
		return globalList;
	}

	public IllegalItemList getCostList() {
		return costList;
	}

	public IllegalItemList getProductList() {
		return productList;
	}

	public ArrayList<String> getGameMats() {
		return gameMats;
	}

	public boolean isIllegal(ShopItemSide side, Material mat) {
		if (globalList.isIllegal(mat))
			return true;

		return side.equals(ShopItemSide.COST) ? costList.isIllegal(mat) : productList.isIllegal(mat);
	}

	public boolean isDirection(BlockFace face) {
		return directions.contains(face);
	}

	public boolean isInventory(Block block) {
		//Get blocks Material and strip all non-alpha chars
		Material blockMaterial = block.getType();
		Boolean found = false;

		debugger.log("isInventory Block Material: " + blockMaterial.name(), DebugLevels.LIST_MANAGER);

        //For each ShopStorage.Storages in inventories, check if their block list contains the block material. end loop if true.
        for (ShopStorage.Storages storage : inventories) {
            if (storage.getTypeMaterials().contains(blockMaterial)) {
                found = true;
                break;
            }
        }

		debugger.log("isInventory Block Material found: " + found, DebugLevels.LIST_MANAGER);
        return found;
	}

	public void reload() {
        //Reloads any lists that need reloading
		updateIllegalLists();
		updateDirections();
        updateInventoryMats();
		setGameMatList();
		initSkippableHoppers();
	}

	public void clearManager() {
		// Clears all lists, Only use if plugin is shutting down
		inventories.clear();
		globalList.clear();
		costList.clear();
		productList.clear();
		directions.clear();
		addOnMats.clear();
		gameMats.clear();
	}

	private void updateIllegalLists() {
		//Clears list before regenerating
		globalList.clear();
		costList.clear();
		productList.clear();

		globalList.setType(Setting.GLOBAL_ILLEGAL_ITEMS_TYPE.getString());
		costList.setType(Setting.COST_ILLEGAL_ITEMS_TYPE.getString());
		productList.setType(Setting.PRODUCT_ILLEGAL_ITEMS_TYPE.getString());

		if (globalList.getType().equals(IllegalItemList.ListType.DISABLED))
			globalList.setType(IllegalItemList.ListType.BLACKLIST);

		debugger.log("Loading GLOBAL Illegal Item List with mode:  " + globalList.getType(), DebugLevels.ILLEGAL_ITEMS_LIST);
		if (globalList.getType().equals(IllegalItemList.ListType.BLACKLIST)) {
			// Add non-removable blacklist items
			globalList.add(Material.AIR);
			globalList.add(Material.CAVE_AIR);
			globalList.add(Material.VOID_AIR);
		} else if (globalList.getType().equals(IllegalItemList.ListType.WHITELIST)) {
			globalList.remove(Material.AIR);
			globalList.remove(Material.CAVE_AIR);
			globalList.remove(Material.VOID_AIR);
		}

		for (String str : Setting.GLOBAL_ILLEGAL_ITEMS_LIST.getStringList()) {
			globalList.checkAndAdd(str);
		}

		debugger.log("Loading COST Illegal Item List with mode:  " + costList.getType(), DebugLevels.ILLEGAL_ITEMS_LIST);
		for (String str : Setting.COST_ILLEGAL_ITEMS_LIST.getStringList()) {
			costList.checkAndAdd(str);
		}

		debugger.log("Loading PRODUCT Illegal Item List with mode:  " + productList.getType(), DebugLevels.ILLEGAL_ITEMS_LIST);
		for (String str : Setting.PRODUCT_ILLEGAL_ITEMS_LIST.getStringList()) {
			productList.checkAndAdd(str);
		}
	}

    private void setGameMatList() {
		gameMats.clear();

		//Adds each Material from Minecraft to a list for command tab complete
		for (Material mat : Material.values()) {
			// Only add the material if it isn't BlackListed
			if (!globalList.isIllegal(mat)) {
				gameMats.add(mat.toString());
			}
		}

		// Remove all non obtainable materials that we have found
		for (NonObtainableMaterials mat : NonObtainableMaterials.values()) {
			gameMats.remove(mat.toString());
		}

		//Adds any strings that have been added the the AddOnMats list to the autocomplete list
		gameMats.addAll(addOnMats);
	}

	private void updateDirections() {
		directions.clear();
		ArrayList<BlockFace> allowed = new ArrayList<>(Arrays.asList(BlockFace.DOWN, BlockFace.WEST, BlockFace.SOUTH, BlockFace.EAST, BlockFace.NORTH, BlockFace.UP));

		for (String str : Setting.ALLOWED_DIRECTIONS.getStringList()) {
			if (allowed.contains(BlockFace.valueOf(str)))
				directions.add(BlockFace.valueOf(str));
		}
	}

    private void updateInventoryMats() {
		//Clears the list before updating
		inventories.clear();

		debugger.log("Inventory Materials from Config:", DebugLevels.STARTUP);
		debugger.log("Config String | Status | Matching Type", DebugLevels.STARTUP);

		//For each String in the Allowed shops config setting, check if it is a valid inventory and add the ShopStorage.Storages object to the list
		for (String str : Setting.ALLOWED_SHOPS.getStringList()) {
			String logMsg = "- " + str;
			String storageName = PLUGIN.getStorages().isValidInventory(str);
			if (storageName.length() > 0) {
				ShopStorage.Storages storage = PLUGIN.getStorages().getValidInventory(storageName);
				inventories.add(storage);
				logMsg += " | Valid | " + storage.name();
			} else {
				logMsg += " | InValid";
			}

			debugger.log(logMsg, DebugLevels.STARTUP);
		}
	}
}