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

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.enumys.Message;
import org.shanerx.tradeshop.enumys.Setting;
import org.shanerx.tradeshop.enumys.ShopRole;
import org.shanerx.tradeshop.enumys.ShopType;
import org.shanerx.tradeshop.framework.events.PlayerShopCreateEvent;
import org.shanerx.tradeshop.objects.Shop;
import org.shanerx.tradeshop.objects.ShopChest;
import org.shanerx.tradeshop.objects.ShopUser;
import org.shanerx.tradeshop.utils.Tuple;
import org.shanerx.tradeshop.utils.Utils;

@SuppressWarnings("unused")
public class ShopCreateListener extends Utils implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onSignChange(SignChangeEvent event) {

		if (event.isCancelled())
			return;

		Sign shopSign = (Sign) event.getBlock().getState();
		shopSign.setLine(0, event.getLine(0));
		shopSign.setLine(1, event.getLine(1));
		shopSign.setLine(2, event.getLine(2));
		shopSign.setLine(3, event.getLine(3));

		if (!ShopType.isShop(shopSign)) {
			return;
		}
		ShopType shopType = ShopType.getType(shopSign);
		Player p = event.getPlayer();
		ShopUser owner = new ShopUser(p, ShopRole.OWNER);

		if (!shopType.checkPerm(p)) {
			failedSign(event, shopType, Message.NO_TS_CREATE_PERMISSION);
			return;
		}

		if (!checkShopChest(shopSign.getBlock()) && !shopType.isITrade()) {
			failedSign(event, shopType, Message.NO_CHEST);
			return;
		}

		if (Setting.MAX_SHOPS_PER_CHUNK.getInt() <= plugin.getDataStorage().getShopCountInChunk(shopSign.getChunk())) {
			failedSign(event, shopType, Message.TOO_MANY_CHESTS);
			return;
		}

		ShopChest shopChest;
		Shop shop;
		Block chest = findShopChest(event.getBlock());

		if (!shopType.isITrade()) {
			if (ShopChest.isShopChest(chest)) {
				shopChest = new ShopChest(chest.getLocation());
			} else {
				shopChest = new ShopChest(chest, p.getUniqueId(), shopSign.getLocation());
			}

			if (shopChest.hasOwner() && !shopChest.getOwner().equals(owner.getUUID())) {
				failedSign(event, shopType, Message.NO_SHOP_PERMISSION);
				return;
			}

			if (shopChest.hasShopSign() && !shopChest.getShopSign().getLocation().equals(shopSign.getLocation())) {
				failedSign(event, shopType, Message.EXISTING_SHOP);
				return;
			}

			shop = new Shop(new Tuple<>(shopSign.getLocation(), shopChest.getChest().getLocation()), shopType, owner);
			shopChest.setName();


			if (shopChest.isEmpty() && shop.hasProduct()) {
				p.sendMessage(Message.EMPTY_TS_ON_SETUP.getPrefixed());
			}
		} else {
			shop = new Shop(shopSign.getLocation(), shopType, owner);
		}

		shop.setEvent(event);

		ItemStack product = lineCheck(event.getLine(1)),
				cost = lineCheck(event.getLine(2));

		if (product != null && shop.getProduct().isEmpty())
			shop.setProduct(product);

		if (cost != null && shop.getCost().isEmpty())
			shop.setCost(cost);
		
		PlayerShopCreateEvent shopCreateEvent = new PlayerShopCreateEvent(p, shop);
		Bukkit.getPluginManager().callEvent(shopCreateEvent);
		if (shopCreateEvent.isCancelled()) {
			event.setCancelled(true);
			return;
		}

		shop.updateSign(event);
		shop.removeEvent();
		shop.saveShop();

		p.sendMessage(Message.SUCCESSFUL_SETUP.getPrefixed());
	}

	private ItemStack lineCheck(String line) {
		if (line == null || line.equalsIgnoreCase("") || !line.contains(" ") || line.split(" ").length != 2)
			return null;

		String[] info = line.split(" ");

		for (String str : info) {
			if (str == null || str.equalsIgnoreCase(""))
				return null;
		}

		if (!isInt(info[0]) || Material.matchMaterial(info[1]) == null)
			return null;

		ItemStack item = new ItemStack(Material.matchMaterial(info[1]), Integer.parseInt(info[0]));

		if (plugin.getListManager().isBlacklisted(item.getType()))
			return null;

		return item;
	}
}