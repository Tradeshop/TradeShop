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

package org.shanerx.tradeshop.trade;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.Utils;

public class TradeEventListener extends Utils implements Listener {
	
	private TradeShop plugin;
	
	public TradeEventListener(TradeShop instance) {
		plugin = instance;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onBlockInteract(PlayerInteractEvent e) {
		
		Player buyer = e.getPlayer();
		
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			
			if (!isTradeShopSign(e.getClickedBlock())) {
				return;
			}
			Sign s = (Sign) e.getClickedBlock().getState();
			
			int x = e.getClickedBlock().getLocation().getBlockX();
			int y = e.getClickedBlock().getLocation().getBlockY();
			int z = e.getClickedBlock().getLocation().getBlockZ();
			String world = e.getClickedBlock().getLocation().getWorld().getName();
			
			BlockState chestState = Bukkit.getServer().getWorld(world).getBlockAt(new Location(e.getClickedBlock().getWorld(), x, y - 1, z)).getState();
			Chest chest = (Chest) chestState;
			
			Inventory chestInventory = chest.getInventory();
			Inventory playerInventory = buyer.getInventory();
			
			String line1 = s.getLine(1);
			String line2 = s.getLine(2);
			String[] info1 = line1.split(" ");
			String[] info2 = line2.split(" ");
			
			
			int amount1 = Integer.parseInt(info1[0]);
			int amount2 = Integer.parseInt(info2[0]);
			
			int durability1 = 0;
			int durability2 = 0;
			if (line1.split(":").length > 1) {
				durability1 = Integer.parseInt(info1[1].split(":")[1]);
				info1[1] = info1[1].split(":")[0];
			}
			if (line2.split(":").length > 1) {
				durability2 = Integer.parseInt(info2[1].split(":")[1]);
				info2[1] = info2[1].split(":")[0];
			}
			
			String item_name1, item_name2;
			
			if (isInt(info1[1]))
				item_name1 = Material.getMaterial(Integer.parseInt(info1[1])).name();
			else
				item_name1 = info1[1].toUpperCase();
			
			if (isInt(info2[1]))
				item_name2 = Material.getMaterial(Integer.parseInt(info2[1])).name();
			else
				item_name2 = info2[1].toUpperCase();
			
			ItemStack item1 = new ItemStack(Material.getMaterial(item_name1), amount1); // What the player gets
			ItemStack item2 = new ItemStack(Material.getMaterial(item_name2), amount2); // What the player pays
			
			boolean item1check = false;
			boolean item2check = false;
			
			if (!containsAtLeast(playerInventory, item2.getType(), amount2)) {
				buyer.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.getMessages().getString("insufficient-items")
						.replace("{ITEM}", item_name2.toLowerCase()).replace("{AMOUNT}", String.valueOf(amount2))));
				return;
			} else {
				for (ItemStack i : playerInventory.getContents()) {
					if (i != null) {
						if (i.getType() == item2.getType() && i.getDurability() == durability2) {
							if (i.getAmount() >= amount2) {
								item2.setData(i.getData());
								item2.setDurability((short)durability2);
								item2.setItemMeta(i.getItemMeta());
								item2check = true;
								break;
							}
						}
					}
				}
			}
			
			if (!containsAtLeast(chestInventory, item1.getType(), amount1)) {
				buyer.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.getMessages().getString("shop-empty")
						.replace("{ITEM}", item_name1.toLowerCase()).replace("{AMOUNT}", String.valueOf(amount1))));
				return;
			} else {
				for (ItemStack i : chestInventory.getContents()) {
					if (i != null) {
						if (i.getType() == item1.getType() && i.getDurability() == durability1) {
							if (i.getAmount() >= amount1) {
								item1.setData(i.getData());
								item1.setDurability((short)durability1);
								item1.setItemMeta(i.getItemMeta());
								item1check = true;
								break;
							}
						}
					}
				}
			}
			
			if (!canFit(chestInventory, item1, amount1)) {
                buyer.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.getMessages().getString("shop-full")
                        .replace("{ITEM}", item_name1.toLowerCase()).replace("{AMOUNT}", String.valueOf(amount1))));
                return;
            }
			
			if (!canFit(playerInventory, item2, amount2)) {
                buyer.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.getMessages().getString("player-full")
                        .replace("{ITEM}", item_name2.toLowerCase()).replace("{AMOUNT}", String.valueOf(amount2))));
                return;
            }
            
			if (item1check && item2check) {
				playerInventory.removeItem(item2);
				chestInventory.removeItem(item1);
				chestInventory.addItem(item2);
				playerInventory.addItem(item1);
				
			} else if (!item1check) {
				buyer.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.getMessages().getString("shop-full-amount")
						.replace("{ITEM}", item_name1.toLowerCase()).replace("{AMOUNT}", String.valueOf(amount1))));
				return;
				
			} else if (!item2check) {
				buyer.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.getMessages().getString("full-amount")
						.replace("{ITEM}", item_name2.toLowerCase()).replace("{AMOUNT}", String.valueOf(amount2))));
				return;
				
			}
			String message = plugin.getMessages().getString("on-trade").replace("{AMOUNT1}", String.valueOf(amount1)).replace("{AMOUNT2}", String.valueOf(amount2)).replace("{ITEM1}", item_name1.toLowerCase()).replace("{ITEM2}", item_name2.toLowerCase()).replace("{SELLER}", s.getLine(3));
			buyer.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + message));
			
		} else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
			
			if (!isTradeShopSign(e.getClickedBlock())) {
				return;
			}
			Sign s = (Sign) e.getClickedBlock().getState();
			
			try {
				String line1 = s.getLine(1);
				String line2 = s.getLine(2);
				String[] info1 = line1.split(" ");
				String[] info2 = line2.split(" ");
				int amount1 = Integer.parseInt(info1[0]);
				int amount2 = Integer.parseInt(info2[0]);
				String item_name1 = info1[1].toUpperCase();
				String item_name2 = info2[1].toUpperCase();
				
				buyer.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.getMessages().getString("confirm-trade").replace("{AMOUNT1}", String.valueOf(amount1)).replace("{AMOUNT2}", String.valueOf(amount2)).replace("{ITEM1}", item_name1.toLowerCase()).replace("{ITEM2}", item_name2.toLowerCase())));
			} catch (Exception ex) {
			}
		}
	}
}
