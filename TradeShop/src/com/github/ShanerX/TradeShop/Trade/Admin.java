package com.github.ShanerX.TradeShop.Trade;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.github.ShanerX.TradeShop.TradeShop;

public class Admin implements Listener {
	
	TradeShop plugin;
	
	public Admin(TradeShop instance) {
		plugin = instance;
	}
	
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		BlockState state = event.getBlock().getState();

		if ( event.getBlock().getType() == Material.SIGN_POST || event.getBlock().getType() == Material.WALL_SIGN ) {	
			Sign s = (Sign) event.getBlock().getState();
			
			if (! "[Trade]".equalsIgnoreCase(ChatColor.stripColor(s.getLine(0)))) {
				return;
			}

			if ( player.hasPermission("tradeshop.admin") ) {
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
            if (! lines[3].equalsIgnoreCase(player.getName()) ) {
            	event.setCancelled(true);
            }
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a[&eTradeShop&a] &cYou may not destroy that TradeShop"));
			return;
		}
		if ( state instanceof Chest ) {	
			if ( player.hasPermission("tradeshop.admin") ) {
				return;
			}
            int x = event.getBlock().getLocation().getBlockX();
            int y = event.getBlock().getLocation().getBlockY();
            int z = event.getBlock().getLocation().getBlockZ();
            String world = event.getBlock().getLocation().getWorld().getName();
			int sign =  plugin.getServer().getWorld(world).getBlockTypeIdAt(x, y + 1, z);
            if ( sign != 323 ) {
            }
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a[&eTradeShop&a] &cYou may not destroy that TradeShop"));
        	event.setCancelled(true);
		}
		
	}
	
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onChestOpen(PlayerInteractEvent e) {
		if ( e.getAction() != Action.RIGHT_CLICK_BLOCK ) {
			return;
		}
		
		BlockState blockState = e.getClickedBlock().getState();
		
		if (! (blockState instanceof Chest) ) {
			return;
		}
		
		if ( e.getPlayer().hasPermission("tradeshop.admin") ) {
			return;
		}
		
        int x = e.getClickedBlock().getLocation().getBlockX();
        int y = e.getClickedBlock().getLocation().getBlockY();
        int z = e.getClickedBlock().getLocation().getBlockZ();
        String world = e.getClickedBlock().getLocation().getWorld().getName();
		int sign =  plugin.getServer().getWorld(world).getBlockTypeIdAt(x, y + 1, z);
        if ( sign != 323 ) {
        }
		e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&a[&eTradeShop&a] &cThat TradeShop does not belong to you"));
    	e.setCancelled(true);
	}
}
