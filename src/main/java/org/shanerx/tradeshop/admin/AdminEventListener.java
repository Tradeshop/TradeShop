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

package org.shanerx.tradeshop.admin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.Utils;

public class AdminEventListener extends Utils implements Listener {
	
	private TradeShop plugin;
	
	public AdminEventListener(TradeShop instance) {
		
		plugin = instance;
		
	}
	
	@SuppressWarnings("unused")
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		
		if (event.getBlock().getType() == Material.SIGN_POST || event.getBlock().getType() == Material.WALL_SIGN) {
			Sign s = (Sign) event.getBlock().getState();
			
			if (!("[iTrade]".equalsIgnoreCase(ChatColor.stripColor(s.getLine(0))) && "[Trade]".equalsIgnoreCase(ChatColor.stripColor(s.getLine(0))) && player.hasPermission(getAdminPerm()))) {
				return;
			}
			
			try {
				String[] signInfo1 = s.getLine(1).split(" ");
				String[] signInfo2 = s.getLine(2).split(" ");
				int amount1 = Integer.parseInt(signInfo1[0]);
				int amount2 = Integer.parseInt(signInfo2[0]);
				String item_name1 = signInfo1[1].toUpperCase();
				ItemStack item1 = new ItemStack(Enum.valueOf(Material.class, item_name1), amount1);
				String item_name2 = signInfo2[1].toUpperCase();
				ItemStack item2 = new ItemStack(Enum.valueOf(Material.class, item_name2), amount2);
				
			} catch (Exception e) {
				return;
			}
			
			String[] lines = s.getLines();
			if (!lines[3].equalsIgnoreCase(player.getName())) {
				event.setCancelled(true);
			}
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.getMessages().getString("no-ts-destroy")));
			return;
		}
		
		if (plugin.getAllowedInventories().contains(block.getType())) {
			if (player.hasPermission(getAdminPerm())) {
				return;
			}
			
			Sign s;
			try {
				s = (Sign) event.getBlock().getRelative(0, +1, 0).getState();
			} catch (Exception ex) {
				return;
			}
			
			try {
				String[] signInfo1 = s.getLine(1).split(" ");
				String[] signInfo2 = s.getLine(2).split(" ");
				int amount1 = Integer.parseInt(signInfo1[0]);
				int amount2 = Integer.parseInt(signInfo2[0]);
				String item_name1 = signInfo1[1].toUpperCase();
				ItemStack item1 = new ItemStack(Enum.valueOf(Material.class, item_name1), amount1);
				String item_name2 = signInfo2[1].toUpperCase();
				ItemStack item2 = new ItemStack(Enum.valueOf(Material.class, item_name2), amount2);
				
			} catch (Exception e) {
				return;
			}
			
			if (s.getLine(3) == null || s.getLine(3).equals(""))
				return;
			if (s.getLine(3).equalsIgnoreCase(player.getName())) {
				return;
			}
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.getMessages().getString("no-ts-destroy")));
			event.setCancelled(true);
		}
	}
	
	@SuppressWarnings("unused")
	@EventHandler
	public void onChestOpen(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		
		Block block = e.getClickedBlock();
		
		if (!(plugin.getAllowedInventories().contains(block.getType()) && e.getPlayer().hasPermission(getAdminPerm()))) {
			return;
		}
		
		Sign s;
		try {
			s = (Sign) block.getRelative(0, +1, 0).getState();
			
		} catch (Exception ex) {
			return;
		}
		
		
		try {
			String[] signInfo1 = s.getLine(1).split(" ");
			String[] signInfo2 = s.getLine(2).split(" ");
			int amount1 = Integer.parseInt(signInfo1[0]);
			int amount2 = Integer.parseInt(signInfo2[0]);
			String item_name1 = signInfo1[1].toUpperCase();
			ItemStack item1 = new ItemStack(Enum.valueOf(Material.class, item_name1), amount1);
			String item_name2 = signInfo2[1].toUpperCase();
			ItemStack item2 = new ItemStack(Enum.valueOf(Material.class, item_name2), amount2);
			
		} catch (Exception ex) {
			return;
		}
		
		if (s.getLine(3) == null || s.getLine(3).equals(""))
			return;
		if (s.getLine(3).equalsIgnoreCase(e.getPlayer().getName())) {
			return;
		}
		
		e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.getMessages().getString("no-ts-open")));
		e.setCancelled(true);
	}
}
