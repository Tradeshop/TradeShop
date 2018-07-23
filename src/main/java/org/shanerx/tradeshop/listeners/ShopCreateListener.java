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

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.InventoryHolder;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.enumys.Message;
import org.shanerx.tradeshop.enumys.ShopRole;
import org.shanerx.tradeshop.enumys.ShopType;
import org.shanerx.tradeshop.objects.Shop;
import org.shanerx.tradeshop.objects.ShopUser;
import org.shanerx.tradeshop.utils.ShopManager;
import org.shanerx.tradeshop.utils.Tuple;
import org.shanerx.tradeshop.utils.Utils;

import java.util.UUID;

@SuppressWarnings("unused")
public class ShopCreateListener extends Utils implements Listener {

	private TradeShop plugin;
	private ShopManager shopUtils;

	public ShopCreateListener(TradeShop instance) {
		plugin = instance;
		shopUtils = new ShopManager();
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		//TODO checking for pre owned chest, add empty chest at setup message

		Sign shopSign = (Sign) event.getBlock().getState();
		shopSign.setLine(0, event.getLine(0));

		if (!ShopType.isShop(shopSign)) {
			return;
		}
		ShopType shopType = ShopType.getType(shopSign);
		Player p = event.getPlayer();
		ShopUser owner = new ShopUser(p, ShopRole.OWNER);

		if (!shopType.checkPerm(p)) {
			failedSign(event, shopType, Message.NO_TS_CREATE_PERMISSION);
		}

		if (!checkShopChest(shopSign.getBlock())) {
			failedSign(event, shopType, Message.NO_CHEST);
			return;
		}

		Block shopChest = findShopChest(shopSign.getBlock());
		Shop shop = new Shop(new Tuple<>(shopSign.getLocation(), shopChest.getLocation()), shopType, owner);

		if (plugin.getListManager().isInventory(shopChest.getType()) &&
				((InventoryHolder) shopChest.getState()).getInventory().getName().contains("$ ^Sign:l_")) {
			String uuid = ((InventoryHolder) shopChest.getState()).getInventory().getName().split("$ ^")[1];
			uuid.replace("Owner:", "");

			if (UUID.fromString(uuid).equals(owner.getUUID())) {
				failedSign(event, shopType, Message.NOT_OWNER);
				return;
			}
		}

		event.setLine(0, ChatColor.GRAY + shopType.toHeader());
		event.setLine(1, "");
		event.setLine(2, "");
		event.setLine(3, shop.getStatus().getLine());

		shop.saveShop();

		setName((InventoryHolder) shopChest.getState(), "$ ^Sign:" + shop.getShopLocationAsSL().serialize() +
				"$ ^Owner:" + owner.getUUID());

		shop.updateSign();
		p.sendMessage(Message.SUCCESSFUL_SETUP.getPrefixed());
		return;
	}
}