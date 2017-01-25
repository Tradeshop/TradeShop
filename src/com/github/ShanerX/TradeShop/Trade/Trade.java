package com.github.ShanerX.TradeShop.Trade;

import java.awt.Event;

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
import org.bukkit.inventory.meta.ItemMeta;

import com.github.ShanerX.TradeShop.TradeShop;
import com.github.ShanerX.TradeShop.Utils;

public class Trade extends Utils implements Listener {

	TradeShop plugin;
	
	public Trade(TradeShop instance) {
		plugin = instance;
	}
	
	
	@EventHandler
	public void onBlockInteract(PlayerInteractEvent e) {
		
		Player buyer = e.getPlayer();
		
		if ( e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			
			Sign s;
			
			try {
				
				s = (Sign) e.getClickedBlock().getState();
			} catch (Exception ex) {
				return;
			}
			
			if (! "[Trade]".equalsIgnoreCase(ChatColor.stripColor(s.getLine(0))))  {
				return;
			}
			
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
	        String item_name1 = info1[1].toUpperCase();
	        String item_name2 = info2[1].toUpperCase();
	        
			ItemStack item1 = new ItemStack(Enum.valueOf(Material.class, item_name1), amount1); // What the player gets
	        ItemStack item2 = new ItemStack(Enum.valueOf(Material.class, item_name2), amount2); // What the player pays
	        
	        if (!playerInventory.contains(Enum.valueOf(Material.class, item_name2))) {
	        	buyer.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.config.getString("insufficient-items")
	        			.replace("{ITEM}", item_name2.toLowerCase()).replace("{AMOUNT}", String.valueOf(amount2))));
	        	return;
	        }
	        
	        if (!chestInventory.contains(Enum.valueOf(Material.class, item_name1))) {
	        	buyer.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.config.getString("shop-empty")
	        			.replace("{ITEM}", item_name1.toLowerCase()).replace("{AMOUNT}", String.valueOf(amount1))));
	
	//	        s.setLine(0, ChatColor.RED + "[Trade]");
	        	return;
	        }
	        
	 //      s.setLine(0, ChatColor.GREEN + "[Trade]");
	 //      e.getClickedBlock().getState().update();
	        for (ItemStack i : playerInventory.getContents()) {
	        	if (i == null) {
	        		continue;
	        	}
	        	if (i.getType() != item2.getType()) {
	        		continue;
	        	}
	        	item2.setItemMeta(i.getItemMeta());
	        	break;
	        }
	        
	        for (ItemStack i : chestInventory.getContents()) {
	        	if (i == null) {
	        		continue;
	        	}
	        	if (i.getType() != item1.getType()) {
	        		continue;
	        	}
	        	item1.setItemMeta(i.getItemMeta());
	        	break;
	        }
	        
	        playerInventory.addItem(item1);
	        playerInventory.removeItem(item2);
	        chestInventory.addItem(item2);
	        chestInventory.removeItem(item1);	        
	        
	        String message = plugin.config.getString("on-trade").replace("{AMOUNT1}", String.valueOf(amount1)).replace("{AMOUNT2}", String.valueOf(amount2)).replace("{ITEM1}", item_name1.toLowerCase()).replace("{ITEM2}", item_name2.toLowerCase()).replace("{SELLER}", s.getLine(3));
	        
	        buyer.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + message));
	        return;
		}
		
		else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
			Sign s;
			try {
				s = (Sign) e.getClickedBlock().getState();
			} catch (Exception ex) {
				return;
			}
			if (! "[Trade]".equalsIgnoreCase(ChatColor.stripColor(s.getLine(0)))) {
				return;
			}
		    String line1 = s.getLine(1);
	        String line2 = s.getLine(2);
	        String[] info1 = line1.split(" ");
	        String[] info2 = line2.split(" ");
	        int amount1 = Integer.parseInt(info2[0]);
	        int amount2 = Integer.parseInt(info2[0]);
	        String item_name1 = info1[1].toUpperCase();
	        String item_name2 = info2[1].toUpperCase();
	        
        	buyer.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.config.getString("confirm-trade").replace("{AMOUNT1}", String.valueOf(amount1)).replace("{AMOUNT2}", String.valueOf(amount2)).replace("{ITEM1}", item_name1.toLowerCase()).replace("{ITEM2}", item_name2.toLowerCase())));
		}
	}
}
