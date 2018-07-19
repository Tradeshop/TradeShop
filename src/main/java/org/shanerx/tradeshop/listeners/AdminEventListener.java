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

import org.bukkit.Bukkit;
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
import org.shanerx.tradeshop.utils.ShopManager;
import org.shanerx.tradeshop.utils.Utils;

public class AdminEventListener extends Utils implements Listener {

	private TradeShop plugin;
	private ShopManager shopUtils;

	public AdminEventListener(TradeShop instance) {
		plugin = instance;
		shopUtils = new ShopManager();
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();

		if (isSign(block)) {
			Sign s = (Sign) block.getState();

			if (!isShopSign(s.getBlock())) {
				return;

			} else if (player.hasPermission(Permissions.ADMIN.getPerm())) {
				return;

			} else if (findShopChest(block) != null && shopUtils.getShopOwners(s).contains(Bukkit.getOfflinePlayer(event.getPlayer().getUniqueId()))) {
				return;

			}
			event.setCancelled(true);
			player.sendMessage(Message.NO_TS_DESTROY.getPrefixed());

		} else if (plugin.getListManager().isInventory(block.getType())) {
			if (player.hasPermission(Permissions.ADMIN.getPerm())) {
				shopUtils.resetInvName(block.getState());
				return;
			}

			Sign s;
			try {
				s = findShopSign(block);
				if (s == null)
					throw new Exception();
			} catch (Exception e) {
				shopUtils.resetInvName(block.getState());
				return;
			}

			if (!isShopSign(s.getBlock())) {
				shopUtils.resetInvName(block.getState());
				return;

			} else if (shopUtils.getShopOwners(s).contains(Bukkit.getOfflinePlayer(event.getPlayer().getUniqueId()))) {
				shopUtils.resetInvName(block.getState());
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

		if (e.getPlayer().hasPermission(Permissions.ADMIN.getPerm())) {
			//Do nothing

		} else if (isShopSign(s.getBlock())) {
			if (!shopUtils.getShopUsers(block).contains(Bukkit.getOfflinePlayer(e.getPlayer().getUniqueId()))) {
				e.getPlayer().sendMessage(Message.NO_TS_OPEN.getPrefixed());
				e.setCancelled(true);
			}
		}
	}
}

