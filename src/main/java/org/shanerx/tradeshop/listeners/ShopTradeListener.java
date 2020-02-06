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
import org.shanerx.tradeshop.framework.events.PlayerTradeEvent;
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

            e.setCancelled(true);

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

			shop = json.loadShop(new ShopLocation(s.getLocation()));
			product = shop.getProduct();
			cost = shop.getCost();

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

			shop = json.loadShop(new ShopLocation(s.getLocation()));
			product = shop.getProduct();
			cost = shop.getCost();
			
			PlayerTradeEvent event = new PlayerTradeEvent(e.getPlayer(), cost, product, shop, e.getClickedBlock(), e.getBlockFace());
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) return;
			
			int count, traded, maxStack;
			if (!shop.getShopType().equals(ShopType.ITRADE)) {
				for (ItemStack iS : product) {
					tradeItems(iS, chestInventory, playerInventory, multiplier);
				}

				for (ItemStack iS : cost) {
					tradeItems(iS, playerInventory, chestInventory, multiplier);
				}
			} else {

				for (ItemStack iS : product) {
					tradeItems(iS, null, playerInventory, multiplier);
				}

				for (ItemStack iS : cost) {
					tradeItems(iS, playerInventory, null, multiplier);
				}
			}

			buyer.sendMessage(Message.ON_TRADE.getPrefixed()
					.replace("{AMOUNT1}", String.valueOf(amountProd))
					.replace("{AMOUNT2}", String.valueOf(amountCost))
					.replace("{ITEM1}", productName.toLowerCase())
					.replace("{ITEM2}", costName.toLowerCase())
					.replace("{SELLER}", !shop.getShopType().isITrade() ? shop.getOwner().getPlayer().getName() : Setting.ITRADESHOP_OWNER.getString()));


		} else if (e.getAction() == Action.LEFT_CLICK_BLOCK && shop.getShopType() == ShopType.BITRADE) {

            e.setCancelled(true);

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
			
			PlayerTradeEvent event = new PlayerTradeEvent(e.getPlayer(), cost, product, shop, e.getClickedBlock(), e.getBlockFace());
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) return;
			
			if (!shop.getShopType().equals(ShopType.ITRADE)) {
				for (ItemStack iS : product) {
					tradeItems(iS, playerInventory, chestInventory, multiplier);
				}

				for (ItemStack iS : cost) {
					tradeItems(iS, chestInventory, playerInventory, multiplier);
				}
			}

			buyer.sendMessage(Message.ON_TRADE.getPrefixed()
					.replace("{AMOUNT2}", String.valueOf(amountCost))
					.replace("{AMOUNT1}", String.valueOf(amountProd))
					.replace("{ITEM2}", costName.toLowerCase())
					.replace("{ITEM1}", productName.toLowerCase())
					.replace("{SELLER}", !shop.getShopType().isITrade() ? shop.getOwner().getPlayer().getName() : Setting.ITRADESHOP_OWNER.getString()));
		}
		
		Bukkit.getPluginManager().callEvent(new PlayerTradeEvent(e.getPlayer(), cost, product, shop, e.getClickedBlock(), e.getBlockFace()));
	}

	private void tradeItems(ItemStack item, Inventory fromInventory, Inventory toInventory, int multiplier) {
		int count = item.getAmount() * multiplier, traded, maxStack;
		boolean isTrade;

		isTrade = fromInventory != null && toInventory != null;

		if (isTrade) {
			while (count > 0) {
				boolean resetItem = false;
				ItemStack temp = fromInventory.getItem(fromInventory.first(item.getType())),
						dupitm1 = item.clone();
				maxStack = dupitm1.getMaxStackSize();

				if (count > maxStack)
					traded = temp.getAmount() < maxStack ? temp.getAmount() : maxStack;
				else
					traded = temp.getAmount() < count ? temp.getAmount() : count;

				dupitm1.setAmount(traded);
				if (!dupitm1.hasItemMeta() && temp.hasItemMeta()) {
					dupitm1.setItemMeta(temp.getItemMeta());
					dupitm1.setData(temp.getData());
				}

				fromInventory.removeItem(dupitm1);
				toInventory.addItem(dupitm1);

				count -= traded;
			}
		} else {
			if (fromInventory == null) {
				toInventory.addItem(item);
			} else if (toInventory == null) {
				while (count > 0) {
					boolean resetItem = false;
					ItemStack temp = fromInventory.getItem(fromInventory.first(item.getType())),
							dupitm1 = item.clone();
					maxStack = dupitm1.getMaxStackSize();

					if (count > maxStack)
						traded = maxStack > temp.getAmount() ? temp.getAmount() : maxStack;
					else
						traded = count > temp.getAmount() ? temp.getAmount() : count;

					dupitm1.setAmount(traded);
					if (!dupitm1.hasItemMeta() && temp.hasItemMeta()) {
						dupitm1.setItemMeta(temp.getItemMeta());
						dupitm1.setData(temp.getData());
					}

					fromInventory.removeItem(dupitm1);

					count -= traded;
				}
			}
		}
	}
}
