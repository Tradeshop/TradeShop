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

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.shanerx.tradeshop.enumys.Setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class ListManager {

	private ArrayList<String> blacklist = new ArrayList<>();
	private ArrayList<BlockFace> directions = new ArrayList<>();
	private ArrayList<Material> inventories = new ArrayList<>();


	public ListManager() {
		reload();
	}

	public ArrayList<BlockFace> getDirections() {
		return directions;
	}

	public ArrayList<Material> getInventories() {
		return inventories;
	}

	public ArrayList<String> getBlacklist() {
		return blacklist;
	}

	public boolean isBlacklisted(Material mat) {
        return blacklist.contains(mat.toString());
    }

	public boolean isDirection(BlockFace face) {
		return directions.contains(face);
	}

	public boolean isInventory(Material mat) {
		return inventories.contains(mat);
	}


	public void reload() {
		updateBlacklist();
		updateDirections();
		updateMaterials();
	}

    public void clearManager() {
        inventories.clear();
        blacklist.clear();
		directions.clear();
	}

	private void updateBlacklist() {
		blacklist.clear();
		blacklist.addAll(Setting.ILLEGAL_ITEMS.getStringList().stream().map(String::toLowerCase).collect(Collectors.toList()));
	}

	private void updateDirections() {
		directions.clear();
		ArrayList<BlockFace> allowed = new ArrayList<>(Arrays.asList(BlockFace.DOWN, BlockFace.WEST, BlockFace.SOUTH, BlockFace.EAST, BlockFace.NORTH, BlockFace.UP));

		for (String str : Setting.ALLOWED_DIRECTIONS.getStringList()) {
			if (allowed.contains(BlockFace.valueOf(str)))
				directions.add(BlockFace.valueOf(str));
		}
	}

	private void updateMaterials() {
		inventories.clear();
		ArrayList<Material> allowed = new ArrayList<>(Arrays.asList(Material.CHEST, Material.TRAPPED_CHEST, Material.DROPPER, Material.HOPPER, Material.DISPENSER));

		for (String str : Setting.ALLOWED_SHOPS.getStringList()) {
			if (str.equalsIgnoreCase("shulker")) {
				inventories.addAll(Arrays.asList(Material.SHULKER_BOX,
						Material.WHITE_SHULKER_BOX,
						Material.ORANGE_SHULKER_BOX,
						Material.MAGENTA_SHULKER_BOX,
						Material.LIGHT_BLUE_SHULKER_BOX,
						Material.YELLOW_SHULKER_BOX,
						Material.LIME_SHULKER_BOX,
						Material.PINK_SHULKER_BOX,
						Material.GRAY_SHULKER_BOX,
						Material.LIGHT_GRAY_SHULKER_BOX,
						Material.CYAN_SHULKER_BOX,
						Material.PURPLE_SHULKER_BOX,
						Material.BLUE_SHULKER_BOX,
						Material.BROWN_SHULKER_BOX,
						Material.GREEN_SHULKER_BOX,
						Material.RED_SHULKER_BOX,
						Material.BLACK_SHULKER_BOX));
			} else {
				if (allowed.contains(Material.valueOf(str)))
					inventories.add(Material.valueOf(str));

			}
		}
	}
}