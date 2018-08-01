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

package org.shanerx.tradeshop.listeners;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.enumys.Message;
import org.shanerx.tradeshop.enumys.Permissions;
import org.shanerx.tradeshop.enumys.ShopType;
import org.shanerx.tradeshop.objects.Shop;
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

			if (player.hasPermission(Permissions.ADMIN.getPerm())) {
				return;

			} else if (shop.getChest() != null && event.getPlayer().getUniqueId().equals(shop.getOwner())) {
				return;

			}
			event.setCancelled(true);
			player.sendMessage(Message.NO_TS_DESTROY.getPrefixed());

		} else if (plugin.getListManager().isInventory(block.getType())) {
			if (player.hasPermission(Permissions.ADMIN.getPerm())) {
				//shopUtils.resetInvName(block.getState()); TODO reset inventory name on break
				return;
			}

			Sign s;
			try {
				s = findShopSign(block);
				if (s == null)
					throw new Exception();
			} catch (Exception e) {
				//shopUtils.resetInvName(block.getState()); TODO reset inventory name on break
				return;
			}

			if (!ShopType.isShop(s)) {
				//shopUtils.resetInvName(block.getState()); TODO reset inventory name on break
				return;
			}

			Shop shop = Shop.loadShop(s);

			if (event.getPlayer().getUniqueId().equals(shop.getOwner())) {
				//shopUtils.resetInvName(block.getState()); TODO reset inventory name on break
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


		Sign s;
		try {
			s = findShopSign(block);
			if (s == null)
				throw new Exception();
		} catch (Exception ex) {
			return;
		}

		Shop shop = Shop.loadShop(s);

		if (!e.getPlayer().hasPermission(Permissions.ADMIN.getPerm()) && isShopSign(s.getBlock())) {
			if (!shop.getUsersUUID().contains(e.getPlayer().getUniqueId())) {
				e.getPlayer().sendMessage(Message.NO_TS_OPEN.getPrefixed());
				e.setCancelled(true);
			}
		}
	}
}

