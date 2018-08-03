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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Nameable;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.TradeShop;

import java.util.UUID;

public class ShopChest {

	//TODO make rename both halfs of a double chest

	private transient static TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
	private ShopLocation shopSign;
	private Location loc;
	private Block chest;
	private UUID owner;
	private String sep1 = "\\$ \\^", sep2 = ":";

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
				plugin.getListManager().getInventories().contains(checking.getType()) &&
				((Nameable) checking.getState()).getCustomName() != null &&
				((Nameable) checking.getState()).getCustomName().contains("$ ^Sign:l_");
	}

	private void getBlock() {
		if (loc.getBlock() != null && plugin.getListManager().getInventories().contains(loc.getBlock().getType())) {
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

	public void loadFromName() {
		if (chest != null &&
				((Nameable) chest.getState()).getCustomName() != null &&
				((Nameable) chest.getState()).getCustomName().contains("$ ^Sign:l_")) {
			String name[] = ((Nameable) chest.getState()).getCustomName().split(sep1);
			shopSign = ShopLocation.deserialize(name[1].split(sep2)[1]);
			owner = UUID.fromString(name[2].split(sep2)[1]);
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
			sb.append(((Nameable) chest.getState()).getCustomName().replaceAll(sep1, ""));
		}
		sb.append("$ ^Sign:");
		sb.append(shopSign.serialize());
		sb.append("$ ^Owner:");
		sb.append(owner.toString());

		return sb.toString();
	}

	public void resetName() {
		BlockState bs = chest.getState();
		if (bs instanceof InventoryHolder && bs instanceof Nameable) {
			((Nameable) bs).setCustomName(((Nameable) chest.getState()).getCustomName().split(sep1)[0]);
			bs.update();
		}
	}

	public void setName() {
		BlockState bs = chest.getState();
		if (bs instanceof InventoryHolder && bs instanceof Nameable) {
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

	public boolean hasShop() {
		return shopSign != null;
	}
}
