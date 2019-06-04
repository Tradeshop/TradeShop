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
import org.shanerx.tradeshop.framework.PlayerTradeEvent;
import org.shanerx.tradeshop.objects.Shop;
import org.shanerx.tradeshop.objects.ShopLocation;
import org.shanerx.tradeshop.utils.JsonConfiguration;
import org.shanerx.tradeshop.utils.Utils;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class ShopTradeListener extends Utils implements Listener {

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onBlockInteract(PlayerInteractEvent e) {

		Player buyer = e.getPlayer();
		Shop shop;
		Sign s;
		BlockState chestState;

		if (ShopType.isShop(e.getClickedBlock())) {
			s = (Sign) e.getClickedBlock().getState();
		} else {
			return;
		}

		JsonConfiguration json = new JsonConfiguration(s.getChunk());
		shop = json.loadShop(new ShopLocation(s.getLocation()));

		if (shop == null)
			return;

		if (!shop.getShopType().equals(ShopType.ITRADE) && shop.getUsersUUID().contains(buyer.getUniqueId())) {
			buyer.sendMessage(Message.SELF_OWNED.getPrefixed());
			return;
		}

		if (!shop.isOpen()) {
			buyer.sendMessage(Message.SHOP_CLOSED.getPrefixed());
			return;
		}

		chestState = shop.getStorage();
		if (!shop.getShopType().equals(ShopType.ITRADE) && chestState == null) {
			buyer.sendMessage(Message.MISSING_CHEST.getPrefixed());
			shop.setClosed();
			return;
		}

		Inventory chestInventory;

		try {
			chestInventory = ((InventoryHolder) chestState).getInventory();
		} catch (NullPointerException npe) {
			chestInventory = null;
		}

		Inventory playerInventory = buyer.getInventory();
		List<ItemStack> product = shop.getProduct();
		List<ItemStack> cost = shop.getCost();

		int multiplier = 1;

		e.setCancelled(true);

		if (buyer.isSneaking() && Setting.ALLOW_MULTI_TRADE.getBoolean()) {
			JsonConfiguration pJson = new JsonConfiguration(buyer.getUniqueId());
			Map<String, Integer> data = pJson.loadPlayer();
			multiplier = data.get("multi");

		}

		String productName, costName;
		int amountCost = 0, amountProd = 0;

		if (!(shop.areProductsValid() && shop.areCostsValid())) {
			buyer.sendMessage(Message.ILLEGAL_ITEM.getPrefixed());
			return;
		}

		if (product.size() == 1) {
			if (product.get(0).hasItemMeta() && product.get(0).getItemMeta().hasDisplayName())
				productName = product.get(0).getItemMeta().getDisplayName();
			else productName = product.get(0).getType().toString();

			amountProd = product.get(0).getAmount();
		} else {
			productName = "Various";
			for (ItemStack iS : product) {
				amountProd += iS.getAmount();
			}
		}
		if (cost.size() == 1) {
			if (cost.get(0).hasItemMeta() && cost.get(0).getItemMeta().hasDisplayName())
				costName = cost.get(0).getItemMeta().getDisplayName();
			else costName = cost.get(0).getType().toString();

			amountCost = cost.get(0).getAmount();
		} else {
			costName = "Various";
			for (ItemStack iS : cost) {
				amountCost += iS.getAmount();
			}
		}

		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {

			if (!checkInventory(playerInventory, cost, multiplier)) {
				buyer.sendMessage(Message.INSUFFICIENT_ITEMS.getPrefixed()
						.replace("{ITEM}", costName.toLowerCase()).replace("{AMOUNT}", String.valueOf(amountCost)));
				return;
			}

			if (!canExchangeAll(playerInventory, cost, product, multiplier)) {
				buyer.sendMessage(Message.PLAYER_FULL.getPrefixed()
						.replace("{ITEM}", costName.toLowerCase()).replace("{AMOUNT}", String.valueOf(amountCost)));
				return;
			}

			if (chestInventory != null) {
				if (!checkInventory(chestInventory, product, multiplier)) {
					buyer.sendMessage(Message.SHOP_EMPTY.getPrefixed()
							.replace("{ITEM}", productName.toLowerCase())
							.replace("{AMOUNT}", String.valueOf(amountProd)));

					if (!checkInventory(chestInventory, product, 1))
						shop.setClosed();

					return;
				}


				if (!canExchangeAll(chestInventory, product, cost, multiplier)) {
					buyer.sendMessage(Message.SHOP_FULL.getPrefixed()
							.replace("{ITEM}", productName.toLowerCase()).replace("{AMOUNT}", String.valueOf(amountProd)));
					return;
				}
			}

			int count, removed;
			if (!shop.getShopType().equals(ShopType.ITRADE)) {
				for (ItemStack iS : product) {
					count = iS.getAmount();
					while (count > 0) {
						boolean resetItem = false;
						ItemStack temp = chestInventory.getItem(chestInventory.first(iS.getType())),
								dupitm1 = iS.clone();

						if (count > iS.getMaxStackSize()) {
							removed = iS.getMaxStackSize();
						} else {
							removed = count;
						}

						if (removed > temp.getAmount()) {
							removed = temp.getAmount();
						}

						iS.setAmount(removed);
						if (!iS.hasItemMeta() && temp.hasItemMeta()) {
							iS.setItemMeta(temp.getItemMeta());
							iS.setData(temp.getData());
							resetItem = true;
						}

						chestInventory.removeItem(iS);
						playerInventory.addItem(iS);

						if (resetItem) {
							iS = dupitm1;
						}

						count -= removed;
					}
				}

				for (ItemStack iS : cost) {
					count = iS.getAmount();
					while (count > 0) {
						boolean resetItem = false;
						ItemStack temp = playerInventory.getItem(playerInventory.first(iS.getType())),
								dupitm1 = iS.clone();
						if (count > iS.getMaxStackSize())
							removed = iS.getMaxStackSize();
						else
							removed = count;

						if (removed > temp.getAmount())
							removed = temp.getAmount();

						iS.setAmount(removed);
						if (!iS.hasItemMeta() && temp.hasItemMeta()) {
							iS.setItemMeta(temp.getItemMeta());
							iS.setData(temp.getData());
							resetItem = true;
						}

						playerInventory.removeItem(iS);
						chestInventory.addItem(iS);

						if (resetItem) {
							iS = dupitm1;
						}

						count -= removed;
					}
				}
			} else {

				for (ItemStack iS : product) {
					count = iS.getAmount();
					while (count > 0) {
						if (count > iS.getMaxStackSize()) {
							removed = iS.getMaxStackSize();
						} else {
							removed = count;
						}

						iS.setAmount(removed);
						playerInventory.addItem(iS);
						count -= removed;
					}
				}

				for (ItemStack iS : cost) {
					count = iS.getAmount();
					while (count > 0) {
						if (count > iS.getMaxStackSize())
							removed = iS.getMaxStackSize();
						else
							removed = count;

						iS.setAmount(removed);
						playerInventory.removeItem(iS);
						count -= removed;
					}
				}
			}

			buyer.sendMessage(Message.ON_TRADE.getPrefixed()
					.replace("{AMOUNT1}", String.valueOf(amountProd))
					.replace("{AMOUNT2}", String.valueOf(amountCost))
					.replace("{ITEM1}", productName.toLowerCase())
					.replace("{ITEM2}", costName.toLowerCase())
					.replace("{SELLER}", shop.getOwner().getPlayer().getName()));

			Bukkit.getPluginManager().callEvent(new PlayerTradeEvent(e.getPlayer(), cost, product, shop, e.getClickedBlock(), e.getBlockFace()));

		} else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {

			if (!checkInventory(playerInventory, cost, multiplier)) {
				buyer.sendMessage(Message.INSUFFICIENT_ITEMS.getPrefixed()
						.replace("{ITEM}", costName.toLowerCase()).replace("{AMOUNT}", String.valueOf(amountCost)));
				return;
			}

			if (!canExchangeAll(playerInventory, cost, product, multiplier)) {
				buyer.sendMessage(Message.PLAYER_FULL.getPrefixed()
						.replace("{ITEM}", costName.toLowerCase()).replace("{AMOUNT}", String.valueOf(amountCost)));
				return;
			}

			if (chestInventory != null) {
				if (!checkInventory(chestInventory, product, multiplier)) {
					buyer.sendMessage(Message.SHOP_EMPTY.getPrefixed()
							.replace("{ITEM}", productName.toLowerCase())
							.replace("{AMOUNT}", String.valueOf(amountProd)));

					if (!checkInventory(chestInventory, product, 1))
						shop.setClosed();

					return;
				}


				if (!canExchangeAll(chestInventory, product, cost, multiplier)) {
					buyer.sendMessage(Message.SHOP_FULL.getPrefixed()
							.replace("{ITEM}", productName.toLowerCase()).replace("{AMOUNT}", String.valueOf(amountProd)));
					return;
				}
			}

			int count, removed;
			if (!shop.getShopType().equals(ShopType.ITRADE)) {
				for (ItemStack iS : product) {
					count = iS.getAmount();
					while (count > 0) {
						boolean resetItem = false;
						ItemStack temp = chestInventory.getItem(chestInventory.first(iS.getType())),
								dupitm1 = iS.clone();

						if (count > iS.getMaxStackSize()) {
							removed = iS.getMaxStackSize();
						} else {
							removed = count;
						}

						if (removed > temp.getAmount()) {
							removed = temp.getAmount();
						}

						iS.setAmount(removed);
						if (!iS.hasItemMeta() && temp.hasItemMeta()) {
							iS.setItemMeta(temp.getItemMeta());
							iS.setData(temp.getData());
							resetItem = true;
						}

						playerInventory.removeItem(iS);
						chestInventory.addItem(iS);

						if (resetItem) {
							iS = dupitm1;
						}

						count -= removed;
					}
				}

				for (ItemStack iS : cost) {
					count = iS.getAmount();
					while (count > 0) {
						boolean resetItem = false;
						ItemStack temp = playerInventory.getItem(playerInventory.first(iS.getType())),
								dupitm1 = iS.clone();
						if (count > iS.getMaxStackSize())
							removed = iS.getMaxStackSize();
						else
							removed = count;

						if (removed > temp.getAmount())
							removed = temp.getAmount();

						iS.setAmount(removed);
						if (!iS.hasItemMeta() && temp.hasItemMeta()) {
							iS.setItemMeta(temp.getItemMeta());
							iS.setData(temp.getData());
							resetItem = true;
						}

						chestInventory.removeItem(iS);
						playerInventory.addItem(iS);

						if (resetItem) {
							iS = dupitm1;
						}

						count -= removed;
					}
				}
			}

			buyer.sendMessage(Message.ON_TRADE.getPrefixed()
					.replace("{AMOUNT2}", String.valueOf(amountCost))
					.replace("{AMOUNT1}", String.valueOf(amountProd))
					.replace("{ITEM2}", costName.toLowerCase())
					.replace("{ITEM1}", productName.toLowerCase())
					.replace("{SELLER}", shop.getOwner().getPlayer().getName()));

			Bukkit.getPluginManager().callEvent(new PlayerTradeEvent(e.getPlayer(), cost, product, shop, e.getClickedBlock(), e.getBlockFace()));
		}
	}
}
