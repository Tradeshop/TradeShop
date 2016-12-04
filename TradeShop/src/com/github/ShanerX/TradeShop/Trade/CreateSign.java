package com.github.ShanerX.TradeShop.Trade;

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
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.ShanerX.TradeShop.TradeShop;

public class CreateSign implements Listener {

	TradeShop plugin;
	
	public CreateSign(TradeShop instance) {
		plugin = instance;
	}
	
	@EventHandler
	public void onSignChange(SignChangeEvent event) throws InterruptedException {
	//	BlockState state = event.getBlock().getState();
		Player player =  event.getPlayer();
        Sign s = (Sign) event.getBlock().getState();
        if (! (event.getLine(0).equalsIgnoreCase("[Trade]")) ) {
			return;
        }
        int x = event.getBlock().getLocation().getBlockX();
        int y = event.getBlock().getLocation().getBlockY();
        int z = event.getBlock().getLocation().getBlockZ();
        String world = event.getBlock().getLocation().getWorld().getName();
    
        @SuppressWarnings("deprecation")
        final int CHEST_ID =  plugin.getServer().getWorld(world).getBlockTypeIdAt(x, y - 1, z);
        if (! player.hasPermission("tradeshop.create") ) {
        	s.setLine(0, "");
        	s.update();
	    	s.setLine(1, "");
        	s.update();
        	s.setLine(2, "");
        	s.update();
        	s.setLine(3, "");
        	s.update();
        	player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a[&eTradeShop&a] &cYou don't have permission to create TradeShops!"));
        	return;
        }
        if ( CHEST_ID != 54 ) {
        	event.setLine(0, ChatColor.DARK_RED + "[Trade]");
        	event.setLine(1, "");
        	event.setLine(2, "");
        	event.setLine(3, "");
        	player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a[&eTradeShop&a] &cYou need to put a chest under the sign!"));
        	return;
        }
        boolean signIsValid = true; // If this is true, the information on the sign is valid!
        
        String line1 = event.getLine(1);
        String line2 = event.getLine(2);
        String line3 = event.getLine(3);
        
        if ( !line1.contains(" ") || !line2.contains(" ") ) {
        	signIsValid = false;
        }
        
        String[] info1 = line1.split(" ");
        String[] info2 = line2.split(" ");
        
        if ( info1.length != 2 || info2.length != 2 ) {
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
        	item_name1 = info1[1].toUpperCase();
        	item1 = new ItemStack(Enum.valueOf(Material.class, item_name1), amount1);
        	item_name2 = info2[1].toUpperCase();
        	item2 = new ItemStack(Enum.valueOf(Material.class, item_name2), amount2);
        	
        } catch (Exception e) {
        	signIsValid = false;
        }
        
        if ( signIsValid == false ) {
        	event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&a[&eTradeShop&a] &cInvalid sign format!"));
        	event.setLine(0, ChatColor.DARK_RED + "[Trade]");
	    	event.setLine(1, "");
	    	event.setLine(2, "");
        	event.setLine(3, "");
        	return;
        }
        if ( line3.equals(event.getPlayer().getName()) ) {
        	return;
        }
        
        String player_name = event.getPlayer().getName();
        event.setLine(3, player_name);
        
		BlockState chestState = Bukkit.getServer().getWorld(world).getBlockAt(new Location(event.getBlock().getWorld(), x, y - 1, z)).getState();
		Chest chest = (Chest) chestState;
		Inventory chestInventory = chest.getInventory();
		
		event.setLine(0, ChatColor.DARK_GREEN + "[Trade]");
		
		if (chestInventory.contains(Enum.valueOf(Material.class, item_name1))) {
			event.setLine(0, ChatColor.DARK_GREEN + "[Trade]");
	    	event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&a[&eTradeShop&a] &aYou have sucessfully setup a TradeShop!"));
	    	return;
		}

    	event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&a[&eTradeShop&a] &cTradeShop empty, please remember to fill it!"));
	}
}
