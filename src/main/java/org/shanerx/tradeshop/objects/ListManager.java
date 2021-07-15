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
import org.shanerx.tradeshop.enumys.DebugLevels;
import org.shanerx.tradeshop.enumys.Setting;
import org.shanerx.tradeshop.enumys.ShopStorage;
import org.shanerx.tradeshop.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings("unused")
public class ListManager extends Utils {

	private final ArrayList<Material> blacklist = new ArrayList<>();
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

	public ArrayList<Material> getBlacklist() {
		return blacklist;
	}

	public ArrayList<String> getGameMats() {
		return gameMats;
	}

	public boolean isBlacklisted(Material mat) {
		return blacklist.contains(mat);
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
		updateBlacklist();
		updateDirections();
        updateInventoryMats();
        setGameMatList();
	}

	public void clearManager() {
        // Clears all lists, Only use if plugin is shutting down
		inventories.clear();
		blacklist.clear();
		directions.clear();
        addOnMats.clear();
        gameMats.clear();
		plugin.getDataStorage().clearChestLinkages();
	}

	private void updateBlacklist() {
		//Clears list before regenerating
		blacklist.clear();

		//Gets the Material object for each sting in the config Blacklist and adds it to the Blacklist
		//If the string is not a Material, ignores it
		for (String str : Setting.getItemBlackList()) {
			Material mat = Material.matchMaterial(str);
			if (mat != null)
				blacklist.add(mat);
		}
	}

    private void setGameMatList() {
		gameMats.clear();

		//Adds each Material from Minecraft to a list for command tab complete
		for (Material mat : Material.values()) {
			gameMats.add(mat.toString());
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