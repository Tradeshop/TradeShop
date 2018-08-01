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

import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.enumys.Message;
import org.shanerx.tradeshop.enumys.Setting;
import org.shanerx.tradeshop.enumys.ShopType;
import org.shanerx.tradeshop.objects.Shop;
import org.shanerx.tradeshop.objects.ShopLocation;
import org.shanerx.tradeshop.utils.JsonConfiguration;
import org.shanerx.tradeshop.utils.Utils;

@SuppressWarnings("unused")
public class ShopTradeListener extends Utils implements Listener {

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onBlockInteract(PlayerInteractEvent e) {

		Player buyer = e.getPlayer();
		Shop shop;
		Sign s;
		BlockState chestState;

		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {

			if (isShopSign(e.getClickedBlock())) {
				s = (Sign) e.getClickedBlock().getState();
			} else {
				return;
			}

			JsonConfiguration json = new JsonConfiguration(s.getChunk());
			shop = json.loadShop(new ShopLocation(s.getLocation()));

			chestState = shop.getChest();
			if (!shop.getShopType().equals(ShopType.ITRADE) && chestState == null) {
				buyer.sendMessage(Message.MISSING_SHOP.getPrefixed());
				return;
			}

			if (!shop.getShopType().equals(ShopType.ITRADE) && shop.getUsersUUID().contains(buyer.getUniqueId())) {
				buyer.sendMessage(Message.SELF_OWNED.getPrefixed());
				return;
			}

			if (!shop.isOpen()) {
				buyer.sendMessage(Message.SHOP_CLOSED.getPrefixed());
				return;
			}

			Inventory chestInventory;

			try {
				chestInventory = ((InventoryHolder) chestState).getInventory();
			} catch (NullPointerException npe) {
				chestInventory = null;
			}

			Inventory playerInventory = buyer.getInventory();

			int amount1 = shop.getSellItem().getAmount();
			int amount2 = shop.getBuyItem().getAmount();

			e.setCancelled(true);

			if (buyer.isSneaking()) {
				if (!buyer.isOnGround() && Setting.ALLOW_QUAD_TRADE.getBoolean()) {
					amount1 = amount1 * 4;
					amount2 = amount2 * 4;
				} else if (Setting.ALLOW_DOUBLE_TRADE.getBoolean()) {
					amount1 += amount1;
					amount2 += amount2;
				}
			}

			String item_name1, item_name2;
			ItemStack item1 = shop.getSellItem(), item2 = shop.getBuyItem();

			if (!(isValidType(item1.getType().toString()) && isValidType(item2.getType().toString()))) {
				buyer.sendMessage(Message.ILLEGAL_ITEM.getPrefixed());
				return;
			}

			if (item1.hasItemMeta()) item_name1 = item1.getItemMeta().getDisplayName();
			else item_name1 = item1.getType().toString();

            if (item2.hasItemMeta()) item_name2 = item2.getItemMeta().getDisplayName();
			else item_name2 = item2.getType().toString();

			if (!containsAtLeast(playerInventory, item2)) {
				buyer.sendMessage(Message.INSUFFICIENT_ITEMS.getPrefixed()
						.replace("{ITEM}", item_name2.toLowerCase()).replace("{AMOUNT}", String.valueOf(amount2)));
				return;
			}

			if (!canExchange(playerInventory, item2, item1)) {
				buyer.sendMessage(Message.PLAYER_FULL.getPrefixed()
						.replace("{ITEM}", item_name2.toLowerCase()).replace("{AMOUNT}", String.valueOf(amount2)));
				return;
			}

			if (chestInventory != null) {
				if (!containsAtLeast(chestInventory, item1)) {
					buyer.sendMessage(Message.SHOP_EMPTY.getPrefixed()
							.replace("{ITEM}", item_name1.toLowerCase()).replace("{AMOUNT}", String.valueOf(amount1)));
					return;
				}


				if (!canExchange(chestInventory, item1, item2)) {
					buyer.sendMessage(Message.SHOP_FULL.getPrefixed()
							.replace("{ITEM}", item_name1.toLowerCase()).replace("{AMOUNT}", String.valueOf(amount1)));
					return;
				}
			}

			int count = amount1, removed;

			if (!shop.getShopType().equals(ShopType.ITRADE)) {
				while (count > 0) {
					boolean resetItem = false;
					ItemStack temp = chestInventory.getItem(chestInventory.first(item1.getType())),
							dupitm1 = item1.clone();

					if (count > item1.getMaxStackSize()) {
						removed = item1.getMaxStackSize();
					} else {
						removed = count;
					}

					if (removed > temp.getAmount()) {
						removed = temp.getAmount();
					}

					item1.setAmount(removed);
					if (!item1.hasItemMeta() && temp.hasItemMeta()) {
						item1.setItemMeta(temp.getItemMeta());
						item1.setData(temp.getData());
						resetItem = true;
					}

					chestInventory.removeItem(item1);
					playerInventory.addItem(item1);

					if (resetItem) {
						item1 = dupitm1;
					}

					count -= removed;
				}

				count = amount2;
				while (count > 0) {
					boolean resetItem = false;
					ItemStack temp = chestInventory.getItem(chestInventory.first(item1.getType())),
							dupitm1 = item1.clone();
					if (count > item2.getMaxStackSize())
						removed = item2.getMaxStackSize();
					else
						removed = count;

					if (removed > temp.getAmount())
						removed = temp.getAmount();

					item2.setAmount(removed);
					if (!item1.hasItemMeta() && temp.hasItemMeta()) {
						item1.setItemMeta(temp.getItemMeta());
						item1.setData(temp.getData());
						resetItem = true;
					}

					playerInventory.removeItem(item2);
					chestInventory.addItem(item2);

					if (resetItem) {
						item1 = dupitm1;
					}

					count -= removed;
				}
			} else {
				while (count > 0) {
					if (count > item1.getMaxStackSize()) {
						removed = item1.getMaxStackSize();
					} else {
						removed = count;
					}

					item1.setAmount(removed);
					playerInventory.addItem(item1);
					count -= removed;
				}

				count = amount2;
				while (count > 0) {
					if (count > item2.getMaxStackSize())
						removed = item2.getMaxStackSize();
					else
						removed = count;

					item2.setAmount(removed);
					playerInventory.removeItem(item2);
					count -= removed;
				}
			}

            buyer.sendMessage(Message.ON_TRADE.getPrefixed()
                    .replace("{AMOUNT1}", String.valueOf(amount1))
                    .replace("{AMOUNT2}", String.valueOf(amount2))
                    .replace("{ITEM1}", item_name1.toLowerCase())
                    .replace("{ITEM2}", item_name2.toLowerCase())
                    .replace("{SELLER}", shop.getOwner().getPlayer().getName()));

		} else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {

			if (isShopSign(e.getClickedBlock())) {
				s = (Sign) e.getClickedBlock().getState();
			} else {
				return;
			}

			shop = Shop.loadShop(s);

            if (!isBiTradeShopSign(e.getClickedBlock())) {
				return;
			}

			chestState = shop.getChest();
			if (chestState == null) {
				buyer.sendMessage(Message.MISSING_SHOP.getPrefixed());
				return;
			}

			if (shop.getUsers().contains(buyer.getUniqueId())) {
				buyer.sendMessage(Message.SELF_OWNED.getPrefixed());
				if (buyer.getUniqueId().equals(shop.getOwner().getUUID())) {
					return;
				}

				e.setCancelled(true);
				return;
			}

			e.setCancelled(true);

			Inventory chestInventory = ((InventoryHolder) chestState).getInventory();
			Inventory playerInventory = buyer.getInventory();

            int amount2 = shop.getBuyItem().getAmount();
            int amount1 = shop.getSellItem().getAmount();

			if (buyer.isSneaking()) {
				if (!buyer.isOnGround() && Setting.ALLOW_QUAD_TRADE.getBoolean()) {
					amount2 *= 4;
					amount1 *= 4;
				} else if (Setting.ALLOW_DOUBLE_TRADE.getBoolean()) {
					amount2 += amount2;
					amount1 += amount1;
				}
			}

			String item_name1, item_name2;
			ItemStack item1 = shop.getBuyItem(), item2 = shop.getSellItem();

			if (item1 == null || item2 == null) {
				failedTrade(e, Message.BUY_FAILED_SIGN);
				return;
			} else if (isBlacklistItem(item1) || isBlacklistItem(item2)) {
				failedTrade(e, Message.ILLEGAL_ITEM);
				return;
			}

			if (item1.hasItemMeta() && item1.getItemMeta().hasDisplayName()) {
				item_name1 = item1.getItemMeta().getDisplayName();
			} else {
                item_name1 = item1.getType().toString();
			}

			if (item2.hasItemMeta() && item2.getItemMeta().hasDisplayName()) {
				item_name2 = item2.getItemMeta().getDisplayName();
			} else {
                item_name2 = item2.getType().toString();
			}

			if (!containsAtLeast(playerInventory, item1)) {
				buyer.sendMessage(Message.INSUFFICIENT_ITEMS.getPrefixed()
						.replace("{ITEM}", item_name1.toLowerCase()).replace("{AMOUNT}", String.valueOf(amount1)));
				return;
			}

			if (!containsAtLeast(chestInventory, item2)) {
				buyer.sendMessage(Message.SHOP_EMPTY.getPrefixed()
						.replace("{ITEM}", item_name2.toLowerCase()).replace("{AMOUNT}", String.valueOf(amount2)));
				return;
			}

			if (!canExchange(chestInventory, item2, item1)) {
				buyer.sendMessage(Message.SHOP_FULL.getPrefixed()
						.replace("{ITEM}", item_name2.toLowerCase()).replace("{AMOUNT}", String.valueOf(amount2)));
				return;
			}

			if (!canExchange(playerInventory, item1, item2)) {
				buyer.sendMessage(Message.PLAYER_FULL.getPrefixed()
						.replace("{ITEM}", item_name1.toLowerCase()).replace("{AMOUNT}", String.valueOf(amount1)));
				return;
			}

			int count = amount1, removed;
			while (count > 0) {
				boolean resetItem = false;
				ItemStack temp = chestInventory.getItem(chestInventory.first(item1.getType())),
						dupitm1 = item1.clone();
				if (count > item1.getMaxStackSize()) {
					removed = item1.getMaxStackSize();
				} else {
					removed = count;
				}

				if (removed > temp.getAmount()) {
					removed = temp.getAmount();
				}

				item1.setAmount(removed);
				if (!item1.hasItemMeta() && temp.hasItemMeta()) {
					item1.setItemMeta(temp.getItemMeta());
					item1.setData(temp.getData());
					resetItem = true;
				}

				playerInventory.removeItem(item1);
				chestInventory.addItem(item1);

				if (resetItem) {
					item1 = dupitm1;
				}

				count -= removed;
			}

			count = amount2;
			while (count > 0) {
				boolean resetItem = false;
				ItemStack temp = chestInventory.getItem(chestInventory.first(item1.getType())),
						dupitm1 = item1.clone();
				if (count > item2.getMaxStackSize())
					removed = item2.getMaxStackSize();
				else
					removed = count;

				if (removed > temp.getAmount())
					removed = temp.getAmount();

				item2.setAmount(removed);
				if (!item1.hasItemMeta() && temp.hasItemMeta()) {
					item1.setItemMeta(temp.getItemMeta());
					item1.setData(temp.getData());
					resetItem = true;
				}

				chestInventory.removeItem(item2);
				playerInventory.addItem(item2);

				if (resetItem) {
					item1 = dupitm1;
				}

				count -= removed;
			}

            buyer.sendMessage(Message.ON_TRADE.getPrefixed()
                    .replace("{AMOUNT2}", String.valueOf(amount1))
                    .replace("{AMOUNT1}", String.valueOf(amount2))
                    .replace("{ITEM2}", item_name1.toLowerCase())
                    .replace("{ITEM1}", item_name2.toLowerCase())
                    .replace("{SELLER}", shop.getOwner().getPlayer().getName()));
		}
	}
}
