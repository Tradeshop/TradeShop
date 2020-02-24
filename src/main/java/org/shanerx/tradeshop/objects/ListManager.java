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
import org.bukkit.block.Container;
import org.shanerx.tradeshop.enumys.Setting;

import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings("unused")
public class ListManager {

	private ArrayList<Material> blacklist = new ArrayList<>();
	private ArrayList<BlockFace> directions = new ArrayList<>();
    private ArrayList<String> inventories = new ArrayList<>();
	private ArrayList<String> gameMats = new ArrayList<>();


	public ListManager() {
		reload();
        setGameMatList();
	}

	public ArrayList<BlockFace> getDirections() {
		return directions;
	}

    public ArrayList<String> getInventories() {
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
        if (!(block instanceof Container))
            return false;

        return inventories.contains(block.getClass().getSimpleName().replaceAll("[\\[\\]]", "").toLowerCase());
	}

	public void reload() {
		updateBlacklist();
		updateDirections();
        updateInventoryMats();
	}

	public void clearManager() {
		inventories.clear();
		blacklist.clear();
		directions.clear();
	}

	private void updateBlacklist() {
		blacklist.clear();
		for (String str : Setting.ILLEGAL_ITEMS.getStringList()) {
			Material mat = Material.matchMaterial(str);
			if (mat != null)
				blacklist.add(mat);
		}
	}

    private void setGameMatList() {
		for (Material mat : Material.values()) {
			gameMats.add(mat.toString());
		}
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
		inventories.clear();

        for (String str : Setting.ALLOWED_SHOPS.getStringList())
            inventories.add(str.toLowerCase());
	}
}