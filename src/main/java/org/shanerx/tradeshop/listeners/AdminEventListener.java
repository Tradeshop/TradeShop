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
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.enumys.Message;
import org.shanerx.tradeshop.enumys.Permissions;
import org.shanerx.tradeshop.enumys.ShopType;
import org.shanerx.tradeshop.objects.Shop;
import org.shanerx.tradeshop.objects.ShopChest;
import org.shanerx.tradeshop.utils.Utils;

public class AdminEventListener extends Utils implements Listener {

	private TradeShop plugin;

	public AdminEventListener(TradeShop instance) {
		plugin = instance;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();

		if (ShopType.isShop(block)) {
			Shop shop = Shop.loadShop((Sign) block.getState());

			if (shop == null)
				return;

			if (player.hasPermission(Permissions.ADMIN.getPerm()) || event.getPlayer().getUniqueId().equals(shop.getOwner().getUUID())) {

				if (shop.getStorage() != null)
					new ShopChest(shop.getStorage().getLocation()).resetName();

				shop.remove();
				return;
			}
			event.setCancelled(true);
			player.sendMessage(Message.NO_TS_DESTROY.getPrefixed());

		} else if (plugin.getListManager().isInventory(block.getType())) {
			BlockState bs = block.getState();
			if (!(bs instanceof Nameable && ((Nameable) bs).getCustomName() != null
					&& ((Nameable) bs).getCustomName().contains("$ ^Sign:l_"))) {
				return;
			}

			if (player.hasPermission(Permissions.ADMIN.getPerm())) {
				new ShopChest(block.getLocation()).resetName();
				return;
			}

			Sign s = findShopSign(block);
			if (s == null) {
				new ShopChest(block.getLocation()).resetName();
				return;
			}

			if (!ShopType.isShop(s)) {
				new ShopChest(block.getLocation()).resetName();
				return;
			}

			Shop shop = Shop.loadShop(s);

			if (event.getPlayer().getUniqueId().equals(shop.getOwner().getUUID())) {
				if (!ShopChest.isDoubleChest(block)) {
					new ShopChest(shop.getInventoryLocation()).resetName();
					shop.removeStorage();
					shop.setClosed();
					shop.saveShop();
				} else {
					if (bs instanceof Nameable && ((Nameable) bs).getCustomName() != null
							&& ((Nameable) bs).getCustomName().contains("$ ^Sign:l_")) {
						((Nameable) bs).setCustomName(((Nameable) bs).getCustomName().split("\\$ \\^")[0]);

						bs.update();
					}
				}
				return;
			}

			event.setCancelled(true);
			player.sendMessage(Message.NO_TS_DESTROY.getPrefixed());
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onChestOpen(PlayerInteractEvent e) {
		Block block = e.getClickedBlock();

		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;

		} else if (!plugin.getListManager().isInventory(block.getType())) {
			return;
		}


		Sign s = findShopSign(block);
		if (s == null) {
			return;
		}

		if (!e.getPlayer().hasPermission(Permissions.ADMIN.getPerm()) && ShopType.isShop(s.getBlock())) {
			Shop shop = Shop.loadShop(s);
			if (!shop.getUsersUUID().contains(e.getPlayer().getUniqueId())) {
				e.getPlayer().sendMessage(Message.NO_TS_OPEN.getPrefixed());
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockPlace(BlockPlaceEvent event) {
		Block block = event.getBlock();

		if (!plugin.getListManager().getInventories().contains(block.getType()))
			return;

		if (!ShopChest.isDoubleChest(block)) {
			Sign shopSign = findShopSign(block);

			if (shopSign == null)
				return;

			Shop shop = Shop.loadShop(shopSign);

			if (shop.getShopType().isITrade())
				return;

			new ShopChest(block, shop.getOwner().getUUID(), shopSign.getLocation()).setEventName(event);
			shop.setInventoryLocation(block.getLocation());
			shop.saveShop();

		} else {
			Block otherhalf = ShopChest.getOtherHalfOfDoubleChest(block);
			Player p = event.getPlayer();

			if (!ShopChest.isShopChest(otherhalf)) {
				return;
			}

			ShopChest shopOtherHalf = new ShopChest(otherhalf.getLocation());

			if (shopOtherHalf.hasOwner() && !shopOtherHalf.getOwner().equals(p.getUniqueId())) {
				event.setCancelled(true);
				return;
			}

			new ShopChest(block, shopOtherHalf.getOwner(), shopOtherHalf.getShopSign().getLocation()).setEventName(event);
		}
		return;
	}
}

