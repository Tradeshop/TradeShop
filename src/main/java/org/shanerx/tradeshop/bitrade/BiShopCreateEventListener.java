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

package org.shanerx.tradeshop.bitrade;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.Utils;

public class BiShopCreateEventListener extends Utils implements Listener {
	
	private TradeShop plugin;
	
	public BiShopCreateEventListener(TradeShop instance) {
		plugin = instance;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGH)
	public void onSignChange(SignChangeEvent event) throws InterruptedException {
		Player player = event.getPlayer();
		Sign s = (Sign) event.getBlock().getState();
		
		if (!event.getLine(0).equalsIgnoreCase("[BiTrade]")) {
			return;
			
		} else if (!player.hasPermission(getCreateBiPerm())) {
			s.setLine(0, "");
			s.update();
			s.setLine(1, "");
			s.update();
			s.setLine(2, "");
			s.update();
			s.setLine(3, "");
			s.update();
			player.sendMessage(colorize(getPrefix() + plugin.getMessages().getString("no-ts-create-permission")));
			return;
		}
		Block chest;
		
		try {
			chest = findShopChest(s.getBlock());
		} catch (Exception e) {
			event.setLine(0, ChatColor.DARK_RED + "[BiTrade]");
			event.setLine(1, "");
			event.setLine(2, "");
			event.setLine(3, "");
			player.sendMessage(colorize(getPrefix() + plugin.getMessages().getString("no-chest")));
			return;
		}
		
		
		if (!plugin.getAllowedInventories().contains(chest.getType())) {
			event.setLine(0, ChatColor.DARK_RED + "[BiTrade]");
			event.setLine(1, "");
			event.setLine(2, "");
			event.setLine(3, "");
			player.sendMessage(colorize(getPrefix() + plugin.getMessages().getString("no-chest")));
			return;
		}
		
		boolean signIsValid = true;
		
		String line1 = event.getLine(1);
		String line2 = event.getLine(2);
		
		if (!line1.contains(" ") || !line2.contains(" ")) {
			signIsValid = false;
		}
		
		String[] info1 = line1.split(" ");
		String[] info2 = line2.split(" ");
		
		if (info1.length != 2 || info2.length != 2) {
			signIsValid = false;
		}
		
		
		int durability1 = 0;
		@SuppressWarnings("unused")
		int durability2 = 0;
		if (line1.split(":").length > 1) {
			durability1 = Integer.parseInt(info1[1].split(":")[1]);
			info1[1] = info1[1].split(":")[0];
		}
		if (line2.split(":").length > 1) {
			durability2 = Integer.parseInt(info2[1].split(":")[1]);
			info2[1] = info2[1].split(":")[0];
		}
		
		int amount1 = 0;
		int amount2 = 0;
		String item_name1 = null;
		String item_name2 = null;
		ItemStack item1;
		@SuppressWarnings("unused")
		ItemStack item2;
		
		try {
			amount1 = Integer.parseInt(info1[0]);
			amount2 = Integer.parseInt(info2[0]);
			
			if (isInt(info1[1]))
				item_name1 = Material.getMaterial(Integer.parseInt(info1[1])).name();
			else
				item_name1 = info1[1].toUpperCase();
			
			item1 = new ItemStack(Material.getMaterial(item_name1), amount1);
			
			if (isInt(info2[1]))
				item_name2 = Material.getMaterial(Integer.parseInt(info2[1])).name();
			else
				item_name2 = info2[1].toUpperCase();
			
			item2 = new ItemStack(Material.getMaterial(item_name2), amount2);
			
		} catch (Exception e) {
			signIsValid = false;
		}
		
		if (!signIsValid) {
			event.getPlayer().sendMessage(colorize(getPrefix() + plugin.getMessages().getString("invalid-sign")));
			event.setLine(0, ChatColor.DARK_RED + "[BiTrade]");
			event.setLine(1, "");
			event.setLine(2, "");
			event.setLine(3, "");
			return;
			
		}
		Inventory chestInventory = ((InventoryHolder) chest.getState()).getInventory();
		item1 = new ItemStack(Material.getMaterial(item_name1), amount1);
		item1.setDurability((short) durability1);
		event.setLine(0, ChatColor.DARK_GREEN + "[BiTrade]");
		setName((InventoryHolder) chest.getState(), "o:" + player.getName());
		
		if (chestInventory.containsAtLeast(item1, amount1)) {
			event.setLine(0, ChatColor.DARK_GREEN + "[BiTrade]");
			event.getPlayer().sendMessage(colorize(getPrefix() + plugin.getMessages().getString("successful-setup")));
			return;
		}
		event.getPlayer().sendMessage(colorize(getPrefix() + plugin.getMessages().getString("empty-ts-on-setup")));
	}
}