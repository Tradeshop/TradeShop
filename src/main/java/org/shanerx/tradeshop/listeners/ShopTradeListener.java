/*
 *                 Copyright (c) 2016-2017
 *         SparklingComet @ http://shanerx.org
 *      KillerOfPie @ http://killerofpie.github.io
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
 * caused by their contribution(s) to the project. See the full License for more information.
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

			if (ShopType.isShop(e.getClickedBlock())) {
				s = (Sign) e.getClickedBlock().getState();
			} else {
				return;
			}

			JsonConfiguration json = new JsonConfiguration(s.getChunk());
			shop = json.loadShop(new ShopLocation(s.getLocation()));

			chestState = shop.getInventory();
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

			int amountProd = shop.getProduct().getAmount();
			int amountCost = shop.getCost().getAmount();

			e.setCancelled(true);

			if (buyer.isSneaking()) {
				if (!buyer.isOnGround() && Setting.ALLOW_QUAD_TRADE.getBoolean()) {
					amountProd *= 4;
					amountCost *= 4;
				} else if (Setting.ALLOW_DOUBLE_TRADE.getBoolean()) {
					amountProd *= 2;
					amountCost *= 2;
				}
			}

			String productName, costName;
			ItemStack product = shop.getProduct(), cost = shop.getCost();

			if (!(isValidType(product.getType().toString()) && isValidType(cost.getType().toString()))) {
				buyer.sendMessage(Message.ILLEGAL_ITEM.getPrefixed());
				return;
			}

			if (product.hasItemMeta() && product.getItemMeta().hasDisplayName())
				productName = product.getItemMeta().getDisplayName();
			else productName = product.getType().toString();

			if (cost.hasItemMeta() && cost.getItemMeta().hasDisplayName())
				costName = cost.getItemMeta().getDisplayName();
			else costName = cost.getType().toString();

			if (!containsAtLeast(playerInventory, cost)) {
				buyer.sendMessage(Message.INSUFFICIENT_ITEMS.getPrefixed()
						.replace("{ITEM}", costName.toLowerCase()).replace("{AMOUNT}", String.valueOf(amountCost)));
				return;
			}

			if (!canExchange(playerInventory, cost, product)) {
				buyer.sendMessage(Message.PLAYER_FULL.getPrefixed()
						.replace("{ITEM}", costName.toLowerCase()).replace("{AMOUNT}", String.valueOf(amountCost)));
				return;
			}

			if (chestInventory != null) {
				if (!containsAtLeast(chestInventory, product)) {
					buyer.sendMessage(Message.SHOP_EMPTY.getPrefixed()
							.replace("{ITEM}", productName.toLowerCase())
							.replace("{AMOUNT}", String.valueOf(amountProd)));
					return;
				}


				if (!canExchange(chestInventory, product, cost)) {
					buyer.sendMessage(Message.SHOP_FULL.getPrefixed()
							.replace("{ITEM}", productName.toLowerCase()).replace("{AMOUNT}", String.valueOf(amountProd)));
					return;
				}
			}

			int count = amountProd, removed;

			if (!shop.getShopType().equals(ShopType.ITRADE)) {
				while (count > 0) {
					boolean resetItem = false;
					ItemStack temp = chestInventory.getItem(chestInventory.first(product.getType())),
							dupitm1 = product.clone();

					if (count > product.getMaxStackSize()) {
						removed = product.getMaxStackSize();
					} else {
						removed = count;
					}

					if (removed > temp.getAmount()) {
						removed = temp.getAmount();
					}

					product.setAmount(removed);
					if (!product.hasItemMeta() && temp.hasItemMeta()) {
						product.setItemMeta(temp.getItemMeta());
						product.setData(temp.getData());
						resetItem = true;
					}

					chestInventory.removeItem(product);
					playerInventory.addItem(product);

					if (resetItem) {
						product = dupitm1;
					}

					count -= removed;
				}

				count = amountCost;
				while (count > 0) {
					boolean resetItem = false;
					ItemStack temp = playerInventory.getItem(playerInventory.first(cost.getType())),
							dupitm1 = cost.clone();
					if (count > cost.getMaxStackSize())
						removed = cost.getMaxStackSize();
					else
						removed = count;

					if (removed > temp.getAmount())
						removed = temp.getAmount();

					cost.setAmount(removed);
					if (!cost.hasItemMeta() && temp.hasItemMeta()) {
						cost.setItemMeta(temp.getItemMeta());
						cost.setData(temp.getData());
						resetItem = true;
					}

					playerInventory.removeItem(cost);
					chestInventory.addItem(cost);

					if (resetItem) {
						cost = dupitm1;
					}

					count -= removed;
				}
			} else {
				while (count > 0) {
					if (count > product.getMaxStackSize()) {
						removed = product.getMaxStackSize();
					} else {
						removed = count;
					}

					product.setAmount(removed);
					playerInventory.addItem(product);
					count -= removed;
				}

				count = amountCost;
				while (count > 0) {
					if (count > cost.getMaxStackSize())
						removed = cost.getMaxStackSize();
					else
						removed = count;

					cost.setAmount(removed);
					playerInventory.removeItem(cost);
					count -= removed;
				}
			}

            buyer.sendMessage(Message.ON_TRADE.getPrefixed()
                    .replace("{AMOUNT1}", String.valueOf(amountProd))
                    .replace("{AMOUNT2}", String.valueOf(amountCost))
                    .replace("{ITEM1}", productName.toLowerCase())
                    .replace("{ITEM2}", costName.toLowerCase())
                    .replace("{SELLER}", shop.getOwner().getPlayer().getName()));

		} else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {

			if (!ShopType.isShop(e.getClickedBlock())) {
				return;
			}

			s = (Sign) e.getClickedBlock().getState();
			shop = Shop.loadShop(s);

			if (ShopType.getType(s) != ShopType.BITRADE) {
				return;
			}

			chestState = shop.getInventory();
			if (chestState == null) {
				buyer.sendMessage(Message.MISSING_SHOP.getPrefixed());
				return;
			}

			if (shop.getUsersUUID().contains(buyer.getUniqueId())) {
				if (buyer.getUniqueId().equals(shop.getOwner().getUUID())) {
					return;
				}

				buyer.sendMessage(Message.SELF_OWNED.getPrefixed());
				e.setCancelled(true);
				return;
			}

			e.setCancelled(true);

			Inventory chestInventory = ((InventoryHolder) chestState).getInventory();
			Inventory playerInventory = buyer.getInventory();

			int amountProd = shop.getProduct().getAmount();
			int amountCost = shop.getCost().getAmount();

			if (buyer.isSneaking()) {
				if (!buyer.isOnGround() && Setting.ALLOW_QUAD_TRADE.getBoolean()) {
					amountProd *= 4;
					amountCost *= 4;
				} else if (Setting.ALLOW_DOUBLE_TRADE.getBoolean()) {
					amountProd *= 2;
					amountCost *= 2;
				}
			}

			String productName, costName;
			ItemStack product = shop.getProduct(), cost = shop.getCost();

			if (product == null || cost == null) {
				failedTrade(e, Message.BUY_FAILED_SIGN);
				return;
			} else if (isBlacklistItem(product) || isBlacklistItem(cost)) {
				failedTrade(e, Message.ILLEGAL_ITEM);
				return;
			}

			if (product.hasItemMeta() && product.getItemMeta().hasDisplayName()) {
				productName = product.getItemMeta().getDisplayName();
			} else {
                productName = product.getType().toString();
			}

			if (cost.hasItemMeta() && cost.getItemMeta().hasDisplayName()) {
				costName = cost.getItemMeta().getDisplayName();
			} else {
                costName = cost.getType().toString();
			}

			if (!containsAtLeast(playerInventory, product)) {
				buyer.sendMessage(Message.INSUFFICIENT_ITEMS.getPrefixed()
						.replace("{ITEM}", productName.toLowerCase()).replace("{AMOUNT}", String.valueOf(amountProd)));
				return;
			}

			if (!containsAtLeast(chestInventory, cost)) {
				buyer.sendMessage(Message.SHOP_EMPTY.getPrefixed()
						.replace("{ITEM}", costName.toLowerCase()).replace("{AMOUNT}", String.valueOf(amountCost)));
				return;
			}

			if (!canExchange(chestInventory, cost, product)) {
				buyer.sendMessage(Message.SHOP_FULL.getPrefixed()
						.replace("{ITEM}", costName.toLowerCase()).replace("{AMOUNT}", String.valueOf(cost)));
				return;
			}

			if (!canExchange(playerInventory, product, cost)) {
				buyer.sendMessage(Message.PLAYER_FULL.getPrefixed()
						.replace("{ITEM}", productName.toLowerCase()).replace("{AMOUNT}", String.valueOf(amountProd)));
				return;
			}

			int count = amountProd, removed;
			while (count > 0) {
				boolean resetItem = false;
				ItemStack temp = playerInventory.getItem(playerInventory.first(product.getType())),
						dupitm1 = product.clone();
				if (count > product.getMaxStackSize()) {
					removed = product.getMaxStackSize();
				} else {
					removed = count;
				}

				if (removed > temp.getAmount()) {
					removed = temp.getAmount();
				}

				product.setAmount(removed);
				if (!product.hasItemMeta() && temp.hasItemMeta()) {
					product.setItemMeta(temp.getItemMeta());
					product.setData(temp.getData());
					resetItem = true;
				}

				playerInventory.removeItem(product);
				chestInventory.addItem(product);

				if (resetItem) {
					product = dupitm1;
				}

				count -= removed;
			}

			count = amountCost;
			while (count > 0) {
				boolean resetItem = false;
				ItemStack temp = chestInventory.getItem(chestInventory.first(cost.getType())),
						dupitm1 = cost.clone();
				if (count > cost.getMaxStackSize())
					removed = cost.getMaxStackSize();
				else
					removed = count;

				if (removed > temp.getAmount())
					removed = temp.getAmount();

				cost.setAmount(removed);
				if (!cost.hasItemMeta() && temp.hasItemMeta()) {
					cost.setItemMeta(temp.getItemMeta());
					cost.setData(temp.getData());
					resetItem = true;
				}

				chestInventory.removeItem(cost);
				playerInventory.addItem(cost);

				if (resetItem) {
					cost = dupitm1;
				}

				count -= removed;
			}

            buyer.sendMessage(Message.ON_TRADE.getPrefixed()
                    .replace("{AMOUNT2}", String.valueOf(amountCost))
                    .replace("{AMOUNT1}", String.valueOf(amountProd))
                    .replace("{ITEM2}", costName.toLowerCase())
                    .replace("{ITEM1}", productName.toLowerCase())
                    .replace("{SELLER}", shop.getOwner().getPlayer().getName()));
		}
	}
}
