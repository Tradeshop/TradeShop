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

package org.shanerx.tradeshop.itrade;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.Utils;

public class IShopCreateEventListener extends Utils implements Listener {
	
	private TradeShop plugin;
	
	public IShopCreateEventListener(TradeShop instance) {
		plugin = instance;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onSignChange(SignChangeEvent event) throws InterruptedException {
		//	BlockState state = event.getBlock().getState();
		Player player = event.getPlayer();
		Sign s = (Sign) event.getBlock().getState();
		if (!(event.getLine(0).equalsIgnoreCase("[iTrade]"))) {
			return;
		}

		if (!player.hasPermission(getCreateIPerm())) {
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

		boolean signIsValid = true; // If this is true, the information on the sign is valid!
		
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
		
		if (line1.split(":").length > 1) {
			info1[1] = info1[1].split(":")[0];
		}
		if (line2.split(":").length > 1) {
			info2[1] = info2[1].split(":")[0];
		}
		
		if (info1.length != 2 || info2.length != 2) {
			signIsValid = false;
		}
		
		int amount1 = 0;
		int amount2 = 0;
		String item_name1 = null;
		String item_name2 = null;
		@SuppressWarnings("unused")
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
		
		if (signIsValid == false) {
			event.getPlayer().sendMessage(colorize(getPrefix() + plugin.getMessages().getString("invalid-sign")));
			event.setLine(0, ChatColor.DARK_RED + "[iTrade]");
			event.setLine(1, "");
			event.setLine(2, "");
			event.setLine(3, "");
			return;
		}
		
		String player_name = event.getPlayer().getName();
		event.setLine(3, plugin.getSettings().getString("itrade-shop-name"));
		event.setLine(0, ChatColor.DARK_GREEN + "[iTrade]");
		event.getPlayer().sendMessage(colorize(getPrefix() + plugin.getMessages().getString("successful-setup")));
	}
}
