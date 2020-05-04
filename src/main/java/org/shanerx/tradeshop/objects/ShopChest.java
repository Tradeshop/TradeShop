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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Nameable;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ShopChest extends Utils {

	private transient static TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
	private ShopLocation shopSign;
	private Location loc;
	private Block chest;
	private UUID owner;
	private String sectionSeparator = "\\$ \\^", titleSeparator = ":";

	public ShopChest(Location chestLoc) {
		this.loc = chestLoc;

		getBlock();
		loadFromName();
	}

	public ShopChest(Block chest, UUID owner, Location sign) {
		this.loc = chest.getLocation();
		this.owner = owner;
		this.shopSign = new ShopLocation(sign);
		this.chest = chest;
	}

	public static boolean isShopChest(Block checking) {
		return checking != null &&
				plugin.getListManager().isInventory(checking) &&
				((Nameable) checking.getState()).getCustomName() != null &&
				((Nameable) checking.getState()).getCustomName().contains("$ ^Sign:l_");
	}

	public static Block getOtherHalfOfDoubleChest(Block chest) {
		if (chest.getType() != Material.CHEST || chest.getType() != Material.TRAPPED_CHEST) {
			return null;
		}
		ArrayList<BlockFace> flatFaces = new ArrayList<>(Arrays.asList(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST));

		for (BlockFace face : flatFaces) {
			Block adjoining = chest.getRelative(face);

			if (adjoining.getType() == chest.getType()) {
				return adjoining;
			}
		}

		return null;
	}

	public static boolean isDoubleChest(Block chest) {
		return getOtherHalfOfDoubleChest(chest) != null;
	}

	private void getBlock() {
		if (loc.getBlock() != null && plugin.getListManager().isInventory(loc.getBlock())) {
			chest = loc.getBlock();
		}
	}

	public BlockState getBlockState() {
		return chest.getState();
	}

	public Inventory getInventory() {
		BlockState bs = chest.getState();
		if (bs instanceof InventoryHolder) {
			return ((InventoryHolder) bs).getInventory();
		}

		return null;
	}

    public boolean hasStock(List<ItemStack> product) {
        return getItems(getInventory(), product, 1).get(0) != null;
    }

	public void loadFromName() {
		if (chest != null &&
				((Nameable) chest.getState()).getCustomName() != null &&
				((Nameable) chest.getState()).getCustomName().contains("$ ^Sign:l_")) {
			String[] name = ((Nameable) chest.getState()).getCustomName().split(sectionSeparator);
			shopSign = ShopLocation.deserialize(name[1].split(titleSeparator)[1]);
			owner = UUID.fromString(name[2].split(titleSeparator)[1]);
		}
	}

	public boolean isEmpty() {
		Inventory inv = getInventory();
		if (inv == null) {
			return true;
		}

		for (ItemStack i : inv.getStorageContents()) {
			if (i != null) {
				return false;
			}
		}

		return true;
	}

	public String getName() {
		StringBuilder sb = new StringBuilder();
		if (((Nameable) chest.getState()).getCustomName() != null) {
			sb.append(((Nameable) chest.getState()).getCustomName().replaceAll(sectionSeparator, ""));
		}
		sb.append("$ ^Sign:");
		sb.append(shopSign.serialize());
		sb.append("$ ^Owner:");
		sb.append(owner.toString());

		return sb.toString();
	}

	public void resetName() {
		if (chest != null) {
			BlockState bs = chest.getState();
			if (bs instanceof Nameable && ((Nameable) bs).getCustomName() != null
					&& ((Nameable) bs).getCustomName().contains("$ ^Sign:l_")) {
				((Nameable) bs).setCustomName(((Nameable) bs).getCustomName().split(sectionSeparator)[0]);

				if (isDoubleChest(chest)) {
					BlockState dblSide = getOtherHalfOfDoubleChest(chest).getState();
					((Nameable) dblSide).setCustomName(
							((Nameable) dblSide).getCustomName().split(sectionSeparator)[0]);

					dblSide.update();
				}

				bs.update();
			}
		}
	}

	public void setName() {
		BlockState bs = chest.getState();
		if (bs instanceof Nameable) {
			((Nameable) bs).setCustomName(getName());

			if (isDoubleChest(chest)) {
				BlockState dblSide = getOtherHalfOfDoubleChest(chest).getState();
				((Nameable) dblSide).setCustomName(getName());

				dblSide.update();
			}

			bs.update();
		}
	}

	public void setEventName(BlockPlaceEvent event) {
		BlockState bs = event.getBlockPlaced().getState();
		if (bs instanceof Nameable) {
			((Nameable) bs).setCustomName(getName());

			bs.update();
		}
	}

	public void setSign(ShopLocation newSign) {
		shopSign = newSign;
	}

	public UUID getOwner() {
		return owner;
	}

	public void setOwner(UUID uuid) {
		owner = uuid;
	}

	public Block getChest() {
		return chest;
	}

	public ShopLocation getShopSign() {
		return shopSign;
	}

	public boolean hasOwner() {
		return owner != null;
	}

	public boolean hasShopSign() {
		return shopSign != null;
	}

	public Shop getShop() {
		if (hasShopSign()) {
			Shop shop = Shop.loadShop(getShopSign());
		}

		return null;
	}
}
