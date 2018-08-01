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
import org.bukkit.inventory.InventoryHolder;
import org.shanerx.tradeshop.TradeShop;

import java.util.UUID;

public class ShopChest {

	private transient static TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
	private ShopLocation shopSign;
	private Location loc;
	private Block chest;
	private UUID owner;
	private String sep1 = "\\& \\^", sep2 = ":";

	public ShopChest(Location chestLoc, Location shopLoc) {
		this.loc = chestLoc;
		this.shopSign = new ShopLocation(shopLoc);

		getBlock();
		loadFromName();
	}

	public ShopChest(Block chest, UUID owner, Location sign) {
		this.loc = chest.getLocation();
		this.owner = owner;
		this.shopSign = new ShopLocation(sign);
		this.chest = chest;
	}

	public void getBlock() {
		if (loc.getBlock() != null && plugin.getListManager().getInventories().contains(loc.getBlock().getType())) {
			chest = loc.getBlock();
		}
	}

	public void loadFromName() {
		if (chest != null &&
				((Nameable) chest.getState()).getCustomName() != null &&
				((Nameable) chest.getState()).getCustomName().contains("Sign:l_")) {
			String name[] = ((Nameable) chest.getState()).getCustomName().split(sep1);
			shopSign = ShopLocation.deserialize(name[0].split(sep2)[1]);
			owner = UUID.fromString(name[1].split(sep2)[1]);
		}
	}

	public String getName() {
		StringBuilder sb = new StringBuilder();
		sb.append("Sign:");
		sb.append(shopSign.serialize()); //TODO NPE from here 86-76
		sb.append("$ ^Owner:");
		sb.append(owner.toString());

		return sb.toString();
	}

	public void setName() {
		BlockState bs = chest.getState();
		if (bs instanceof InventoryHolder && bs instanceof Nameable) {
			((Nameable) bs).setCustomName(getName()); //TODO NPE 86-76
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
