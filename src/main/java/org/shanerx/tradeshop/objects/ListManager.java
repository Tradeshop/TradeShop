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

package org.shanerx.tradeshop.objects;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.shanerx.tradeshop.enumys.*;
import org.shanerx.tradeshop.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings("unused")
public class ListManager extends Utils {

	private IllegalItemList globalList;
	private IllegalItemList costList;
	private IllegalItemList productList;

	private final ArrayList<BlockFace> directions = new ArrayList<>();
	private final ArrayList<ShopStorage.Storages> inventories = new ArrayList<>();
	private final ArrayList<String> gameMats = new ArrayList<>();
	private final ArrayList<String> addOnMats = new ArrayList<>();


	public ListManager() {
		reload();
		setGameMatList();
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

	public boolean isIllegal(TradeItemType type, Material mat) {
		return globalList.isIllegal(mat) || type == TradeItemType.COST ? costList.isIllegal(mat) : productList.isIllegal(mat);
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
		plugin.getDataStorage().clearChestLinkages();
	}

	private void updateIllegalLists() {
		//Clears list before regenerating
		globalList.clear();
		costList.clear();
		productList.clear();

		globalList.setType(Setting.GLOBAL_ILLEGAL_ITEMS_TYPE.getString());
		costList.setType(Setting.COST_ILLEGAL_ITEMS_TYPE.getString());
		productList.setType(Setting.PRODUCT_ILLEGAL_ITEMS_TYPE.getString());

		if (globalList.getType().equals(ListType.DISABLED))
			globalList.setType(ListType.BLACKLIST);

		if (globalList.getType().equals(ListType.BLACKLIST)) {
			// Add non-removable blacklist items
			globalList.add(Material.AIR);
			globalList.add(Material.CAVE_AIR);
			globalList.add(Material.VOID_AIR);
		}

		for (String str : Setting.GLOBAL_ILLEGAL_ITEMS_LIST.getStringList()) {
			Material mat = Material.matchMaterial(str);
			if (mat != null) {
				globalList.add(mat);
			} else {
				switch (str.toLowerCase()) {
					case "@banner":
						globalList.add(Material.BLACK_BANNER);
						globalList.add(Material.RED_BANNER);
						globalList.add(Material.GREEN_BANNER);
						globalList.add(Material.BROWN_BANNER);
						globalList.add(Material.BLUE_BANNER);
						globalList.add(Material.PURPLE_BANNER);
						globalList.add(Material.LIGHT_BLUE_BANNER);
						globalList.add(Material.LIGHT_GRAY_BANNER);
						globalList.add(Material.GRAY_BANNER);
						globalList.add(Material.PINK_BANNER);
						globalList.add(Material.LIME_BANNER);
						globalList.add(Material.YELLOW_BANNER);
						globalList.add(Material.CYAN_BANNER);
						globalList.add(Material.MAGENTA_BANNER);
						globalList.add(Material.ORANGE_BANNER);
						globalList.add(Material.WHITE_BANNER);
						globalList.add(Material.BLACK_WALL_BANNER);
						globalList.add(Material.RED_WALL_BANNER);
						globalList.add(Material.GREEN_WALL_BANNER);
						globalList.add(Material.BROWN_WALL_BANNER);
						globalList.add(Material.BLUE_WALL_BANNER);
						globalList.add(Material.PURPLE_WALL_BANNER);
						globalList.add(Material.LIGHT_BLUE_WALL_BANNER);
						globalList.add(Material.LIGHT_GRAY_WALL_BANNER);
						globalList.add(Material.GRAY_WALL_BANNER);
						globalList.add(Material.PINK_WALL_BANNER);
						globalList.add(Material.LIME_WALL_BANNER);
						globalList.add(Material.YELLOW_WALL_BANNER);
						globalList.add(Material.CYAN_WALL_BANNER);
						globalList.add(Material.MAGENTA_WALL_BANNER);
						globalList.add(Material.ORANGE_WALL_BANNER);
						globalList.add(Material.WHITE_WALL_BANNER);
						break;
					case "@candle":
						globalList.add(Material.CANDLE);
						globalList.add(Material.BLACK_CANDLE);
						globalList.add(Material.RED_CANDLE);
						globalList.add(Material.GREEN_CANDLE);
						globalList.add(Material.BROWN_CANDLE);
						globalList.add(Material.BLUE_CANDLE);
						globalList.add(Material.PURPLE_CANDLE);
						globalList.add(Material.LIGHT_BLUE_CANDLE);
						globalList.add(Material.LIGHT_GRAY_CANDLE);
						globalList.add(Material.GRAY_CANDLE);
						globalList.add(Material.PINK_CANDLE);
						globalList.add(Material.LIME_CANDLE);
						globalList.add(Material.YELLOW_CANDLE);
						globalList.add(Material.CYAN_CANDLE);
						globalList.add(Material.MAGENTA_CANDLE);
						globalList.add(Material.ORANGE_CANDLE);
						globalList.add(Material.WHITE_CANDLE);
						break;
					case "@carpet":
						globalList.add(Material.MOSS_CARPET);
						globalList.add(Material.BLACK_CARPET);
						globalList.add(Material.RED_CARPET);
						globalList.add(Material.GREEN_CARPET);
						globalList.add(Material.BROWN_CARPET);
						globalList.add(Material.BLUE_CARPET);
						globalList.add(Material.PURPLE_CARPET);
						globalList.add(Material.LIGHT_BLUE_CARPET);
						globalList.add(Material.LIGHT_GRAY_CARPET);
						globalList.add(Material.GRAY_CARPET);
						globalList.add(Material.PINK_CARPET);
						globalList.add(Material.LIME_CARPET);
						globalList.add(Material.YELLOW_CARPET);
						globalList.add(Material.CYAN_CARPET);
						globalList.add(Material.MAGENTA_CARPET);
						globalList.add(Material.ORANGE_CARPET);
						globalList.add(Material.WHITE_CARPET);
						break;
					case "@concrete":
						globalList.add(Material.BLACK_CONCRETE);
						globalList.add(Material.RED_CONCRETE);
						globalList.add(Material.GREEN_CONCRETE);
						globalList.add(Material.BROWN_CONCRETE);
						globalList.add(Material.BLUE_CONCRETE);
						globalList.add(Material.PURPLE_CONCRETE);
						globalList.add(Material.LIGHT_BLUE_CONCRETE);
						globalList.add(Material.LIGHT_GRAY_CONCRETE);
						globalList.add(Material.GRAY_CONCRETE);
						globalList.add(Material.PINK_CONCRETE);
						globalList.add(Material.LIME_CONCRETE);
						globalList.add(Material.YELLOW_CONCRETE);
						globalList.add(Material.CYAN_CONCRETE);
						globalList.add(Material.MAGENTA_CONCRETE);
						globalList.add(Material.ORANGE_CONCRETE);
						globalList.add(Material.WHITE_CONCRETE);
						break;
					case "@concrete_powder":
						globalList.add(Material.BLACK_CONCRETE_POWDER);
						globalList.add(Material.RED_CONCRETE_POWDER);
						globalList.add(Material.GREEN_CONCRETE_POWDER);
						globalList.add(Material.BROWN_CONCRETE_POWDER);
						globalList.add(Material.BLUE_CONCRETE_POWDER);
						globalList.add(Material.PURPLE_CONCRETE_POWDER);
						globalList.add(Material.LIGHT_BLUE_CONCRETE_POWDER);
						globalList.add(Material.LIGHT_GRAY_CONCRETE_POWDER);
						globalList.add(Material.GRAY_CONCRETE_POWDER);
						globalList.add(Material.PINK_CONCRETE_POWDER);
						globalList.add(Material.LIME_CONCRETE_POWDER);
						globalList.add(Material.YELLOW_CONCRETE_POWDER);
						globalList.add(Material.CYAN_CONCRETE_POWDER);
						globalList.add(Material.MAGENTA_CONCRETE_POWDER);
						globalList.add(Material.ORANGE_CONCRETE_POWDER);
						globalList.add(Material.WHITE_CONCRETE_POWDER);
						break;
					case "@dye":
						globalList.add(Material.BLACK_DYE);
						globalList.add(Material.RED_DYE);
						globalList.add(Material.GREEN_DYE);
						globalList.add(Material.BROWN_DYE);
						globalList.add(Material.BLUE_DYE);
						globalList.add(Material.PURPLE_DYE);
						globalList.add(Material.LIGHT_BLUE_DYE);
						globalList.add(Material.LIGHT_GRAY_DYE);
						globalList.add(Material.GRAY_DYE);
						globalList.add(Material.PINK_DYE);
						globalList.add(Material.LIME_DYE);
						globalList.add(Material.YELLOW_DYE);
						globalList.add(Material.CYAN_DYE);
						globalList.add(Material.MAGENTA_DYE);
						globalList.add(Material.ORANGE_DYE);
						globalList.add(Material.WHITE_DYE);
						break;
					case "@glazed_terracotta":
						globalList.add(Material.BLACK_GLAZED_TERRACOTTA);
						globalList.add(Material.RED_GLAZED_TERRACOTTA);
						globalList.add(Material.GREEN_GLAZED_TERRACOTTA);
						globalList.add(Material.BROWN_GLAZED_TERRACOTTA);
						globalList.add(Material.BLUE_GLAZED_TERRACOTTA);
						globalList.add(Material.PURPLE_GLAZED_TERRACOTTA);
						globalList.add(Material.LIGHT_BLUE_GLAZED_TERRACOTTA);
						globalList.add(Material.LIGHT_GRAY_GLAZED_TERRACOTTA);
						globalList.add(Material.GRAY_GLAZED_TERRACOTTA);
						globalList.add(Material.PINK_GLAZED_TERRACOTTA);
						globalList.add(Material.LIME_GLAZED_TERRACOTTA);
						globalList.add(Material.YELLOW_GLAZED_TERRACOTTA);
						globalList.add(Material.CYAN_GLAZED_TERRACOTTA);
						globalList.add(Material.MAGENTA_GLAZED_TERRACOTTA);
						globalList.add(Material.ORANGE_GLAZED_TERRACOTTA);
						globalList.add(Material.WHITE_GLAZED_TERRACOTTA);
						break;
					case "@shulker_box":
						globalList.add(Material.SHULKER_BOX);
						globalList.add(Material.BLACK_SHULKER_BOX);
						globalList.add(Material.RED_SHULKER_BOX);
						globalList.add(Material.GREEN_SHULKER_BOX);
						globalList.add(Material.BROWN_SHULKER_BOX);
						globalList.add(Material.BLUE_SHULKER_BOX);
						globalList.add(Material.PURPLE_SHULKER_BOX);
						globalList.add(Material.LIGHT_BLUE_SHULKER_BOX);
						globalList.add(Material.LIGHT_GRAY_SHULKER_BOX);
						globalList.add(Material.GRAY_SHULKER_BOX);
						globalList.add(Material.PINK_SHULKER_BOX);
						globalList.add(Material.LIME_SHULKER_BOX);
						globalList.add(Material.YELLOW_SHULKER_BOX);
						globalList.add(Material.CYAN_SHULKER_BOX);
						globalList.add(Material.MAGENTA_SHULKER_BOX);
						globalList.add(Material.ORANGE_SHULKER_BOX);
						globalList.add(Material.WHITE_SHULKER_BOX);
						break;
					case "@stained_glass":
						globalList.add(Material.BLACK_STAINED_GLASS);
						globalList.add(Material.RED_STAINED_GLASS);
						globalList.add(Material.GREEN_STAINED_GLASS);
						globalList.add(Material.BROWN_STAINED_GLASS);
						globalList.add(Material.BLUE_STAINED_GLASS);
						globalList.add(Material.PURPLE_STAINED_GLASS);
						globalList.add(Material.LIGHT_BLUE_STAINED_GLASS);
						globalList.add(Material.LIGHT_GRAY_STAINED_GLASS);
						globalList.add(Material.GRAY_STAINED_GLASS);
						globalList.add(Material.PINK_STAINED_GLASS);
						globalList.add(Material.LIME_STAINED_GLASS);
						globalList.add(Material.YELLOW_STAINED_GLASS);
						globalList.add(Material.CYAN_STAINED_GLASS);
						globalList.add(Material.MAGENTA_STAINED_GLASS);
						globalList.add(Material.ORANGE_STAINED_GLASS);
						globalList.add(Material.WHITE_STAINED_GLASS);
						break;
					case "@stained_glass_pane":
						globalList.add(Material.BLACK_STAINED_GLASS_PANE);
						globalList.add(Material.RED_STAINED_GLASS_PANE);
						globalList.add(Material.GREEN_STAINED_GLASS_PANE);
						globalList.add(Material.BROWN_STAINED_GLASS_PANE);
						globalList.add(Material.BLUE_STAINED_GLASS_PANE);
						globalList.add(Material.PURPLE_STAINED_GLASS_PANE);
						globalList.add(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
						globalList.add(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
						globalList.add(Material.GRAY_STAINED_GLASS_PANE);
						globalList.add(Material.PINK_STAINED_GLASS_PANE);
						globalList.add(Material.LIME_STAINED_GLASS_PANE);
						globalList.add(Material.YELLOW_STAINED_GLASS_PANE);
						globalList.add(Material.CYAN_STAINED_GLASS_PANE);
						globalList.add(Material.MAGENTA_STAINED_GLASS_PANE);
						globalList.add(Material.ORANGE_STAINED_GLASS_PANE);
						globalList.add(Material.WHITE_STAINED_GLASS_PANE);
						break;
					case "@terracotta":
						globalList.add(Material.TERRACOTTA);
						globalList.add(Material.BLACK_TERRACOTTA);
						globalList.add(Material.RED_TERRACOTTA);
						globalList.add(Material.GREEN_TERRACOTTA);
						globalList.add(Material.BROWN_TERRACOTTA);
						globalList.add(Material.BLUE_TERRACOTTA);
						globalList.add(Material.PURPLE_TERRACOTTA);
						globalList.add(Material.LIGHT_BLUE_TERRACOTTA);
						globalList.add(Material.LIGHT_GRAY_TERRACOTTA);
						globalList.add(Material.GRAY_TERRACOTTA);
						globalList.add(Material.PINK_TERRACOTTA);
						globalList.add(Material.LIME_TERRACOTTA);
						globalList.add(Material.YELLOW_TERRACOTTA);
						globalList.add(Material.CYAN_TERRACOTTA);
						globalList.add(Material.MAGENTA_TERRACOTTA);
						globalList.add(Material.ORANGE_TERRACOTTA);
						globalList.add(Material.WHITE_TERRACOTTA);
						break;
					case "@wool":
						globalList.add(Material.BLACK_WOOL);
						globalList.add(Material.RED_WOOL);
						globalList.add(Material.GREEN_WOOL);
						globalList.add(Material.BROWN_WOOL);
						globalList.add(Material.BLUE_WOOL);
						globalList.add(Material.PURPLE_WOOL);
						globalList.add(Material.LIGHT_BLUE_WOOL);
						globalList.add(Material.LIGHT_GRAY_WOOL);
						globalList.add(Material.GRAY_WOOL);
						globalList.add(Material.PINK_WOOL);
						globalList.add(Material.LIME_WOOL);
						globalList.add(Material.YELLOW_WOOL);
						globalList.add(Material.CYAN_WOOL);
						globalList.add(Material.MAGENTA_WOOL);
						globalList.add(Material.ORANGE_WOOL);
						globalList.add(Material.WHITE_WOOL);
						break;
				}
			}
		}

		for (String str : Setting.COST_ILLEGAL_ITEMS_LIST.getStringList()) {
			Material mat = Material.matchMaterial(str);
			if (mat != null)
				costList.add(mat);
		}

		for (String str : Setting.PRODUCT_ILLEGAL_ITEMS_LIST.getStringList()) {
			Material mat = Material.matchMaterial(str);
			if (mat != null)
				productList.add(mat);
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

        log("Inventory Materials from Config:");
        log("Config String | Status | Matching Type");

        //For each String in the Allowed shops config setting, check if it is a valid inventory and add the ShopStorage.Storages object to the list
        for (String str : Setting.ALLOWED_SHOPS.getStringList()) {
            String logMsg = "- " + str;
            String storageName = plugin.getStorages().isValidInventory(str);
            if (storageName.length() > 0) {
                ShopStorage.Storages storage = plugin.getStorages().getValidInventory(storageName);
                inventories.add(storage);
                logMsg += " | Valid | " + storage.name();
            } else {
                logMsg += " | InValid";
            }

            log(logMsg);
        }
	}
}