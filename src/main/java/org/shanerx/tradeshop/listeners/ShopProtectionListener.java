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

package org.shanerx.tradeshop.listeners;

import org.bukkit.Nameable;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.enumys.Setting;
import org.shanerx.tradeshop.enumys.ShopType;
import org.shanerx.tradeshop.objects.Shop;
import org.shanerx.tradeshop.objects.ShopChest;
import org.shanerx.tradeshop.objects.ShopLocation;
import org.shanerx.tradeshop.utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ShopProtectionListener extends Utils implements Listener {

	private TradeShop plugin;

	public ShopProtectionListener(TradeShop instance) {
		plugin = instance;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onInventoryMoveItem(InventoryMoveItemEvent event) {

		if (!(event.getInitiator().getType().equals(InventoryType.HOPPER) &&
				plugin.getListManager().isInventory(event.getSource().getLocation().getBlock().getType()))) {
			return;
		}

		Nameable fromContainer = (Nameable) event.getSource().getLocation().getBlock().getState();

		if (fromContainer.getCustomName() != null && fromContainer.getCustomName().contains("$ ^Sign:l_")) {
			Shop shop = Shop.loadShop(ShopLocation.deserialize(fromContainer.getCustomName().split("\\$ \\^")[1].split(":")[1]));

			event.setCancelled(!Setting.findSetting(shop.getShopType().toString() + "SHOP_HOPPER_EXPORT").getBoolean());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityExplodeItem(EntityExplodeEvent event) {
		List<Block> toRemove = new ArrayList<>();
		for (Iterator<Block> i = event.blockList().iterator(); i.hasNext(); ) {
			Block b = i.next();
			if (ShopChest.isShopChest(b)) {
				Shop shop = Shop.loadShop((new ShopChest(b.getLocation())).getShopSign());
				if (shop != null) {
					if (!Setting.findSetting((shop.getShopType().toString() + "SHOP_EXPLODE").toUpperCase()).getBoolean())
						i.remove();
					else {
						if (shop.getStorage() != null)
							shop.getChestAsSC().resetName();
						shop.remove();
					}

				}

			} else if (ShopType.isShop(b)) {
				if (!Setting.findSetting(ShopType.getType((Sign) b.getState()).toString() + "SHOP_EXPLODE".toUpperCase()).getBoolean()) {
					i.remove();

					if (plugin.getVersion().isBelow(1, 14)) {
						org.bukkit.material.Sign s = (org.bukkit.material.Sign) b.getState().getData();
						toRemove.add(b.getRelative(s.getAttachedFace()));
					} else if (b.getType().toString().contains("WALL_SIGN")) {
						BlockData data = b.getBlockData();
						if (data instanceof Directional)
							toRemove.add(b.getRelative(((Directional) data).getFacing().getOppositeFace()));
					} else {
						toRemove.add(b.getRelative(BlockFace.DOWN));
					}
				} else {
					Shop shop = Shop.loadShop((Sign) b.getState());
					if (shop != null) {

						if (shop.getStorage() != null)
							shop.getChestAsSC().resetName();

						shop.remove();
					}
				}
			}
		}

		event.blockList().removeAll(toRemove);
	}
}

