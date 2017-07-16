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

public class Trade extends Utils implements Listener {

	TradeShop plugin;
	
	public Trade(TradeShop instance) {
		plugin = instance;
	}
	
	
	@SuppressWarnings("deprecation")
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
	        
	        String item_name1, item_name2;
	        
	        if(isInt(info1[1]))
                item_name1 = Material.getMaterial(Integer.parseInt(info1[1])).name();
            else
                item_name1 = info1[1].toUpperCase();
	        
	        if(isInt(info2[1]))
                item_name2 = Material.getMaterial(Integer.parseInt(info2[1])).name();
            else
                item_name2 = info2[1].toUpperCase();
	        
			ItemStack item1 = new ItemStack(Material.getMaterial(item_name1), amount1); // What the player gets
	        ItemStack item2 = new ItemStack(Material.getMaterial(item_name2), amount2); // What the player pays
	        boolean item1check = false, item2check = false;
	        
	        if (!containsAtLeast(playerInventory, item2.getType(), amount2)) {
	        	buyer.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.config.getString("insufficient-items")
	        			.replace("{ITEM}", item_name2.toLowerCase()).replace("{AMOUNT}", String.valueOf(amount2))));
	        	return;
	        } else {
	            for(ItemStack i : playerInventory.getContents())
	            {
	                if(i != null)
	                {
    	                if(i.getType() == item2.getType())
    	                {
    	                    if(i.getAmount() >= amount2)
    	                    {
    	                        buyer.sendMessage(i.getAmount() + "");
    	                        item2.setData(i.getData());
                	            item2.setDurability(i.getDurability());
                	            item2.setItemMeta(i.getItemMeta());
                	            item2check = true;
                	            break;
    	                    }
    	                }
	                }
	            }
	        }
	        
	        if (!containsAtLeast(chestInventory, item1.getType(), amount1)) {
	            buyer.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.config.getString("shop-empty")
	        			.replace("{ITEM}", item_name1.toLowerCase()).replace("{AMOUNT}", String.valueOf(amount1))));
	        	return;
	        } else  {
	            for(ItemStack i : chestInventory.getContents())
                {
	                if(i != null)
	                {
	                    if(i.getType() == item1.getType())
                        {
                            if(i.getAmount() >= amount1)
                            {
                                item1.setData(i.getData());
                                item1.setDurability(i.getDurability());
                                item1.setItemMeta(i.getItemMeta());
                                item1check = true;
                                break;
                            }
                        }
	                }
                }
	        }
	        
	        if(item1check && item2check)
	        {
                playerInventory.removeItem(item2);
                chestInventory.removeItem(item1);
                chestInventory.addItem(item2);
                playerInventory.addItem(item1);
	        }
	        else if(!item1check)
	        {
	            buyer.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.config.getString("shop-full-amount")
                .replace("{ITEM}", item_name1.toLowerCase()).replace("{AMOUNT}", String.valueOf(amount1)))); 
	            return;
	        }
            else if(!item2check)
            {
                buyer.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.config.getString("full-amount")
                .replace("{ITEM}", item_name2.toLowerCase()).replace("{AMOUNT}", String.valueOf(amount2)))); 
                return;
            }
	        
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
			
			try {
			    String line1 = s.getLine(1);
		        String line2 = s.getLine(2);
		        String[] info1 = line1.split(" ");
		        String[] info2 = line2.split(" ");
		        int amount1 = Integer.parseInt(info1[0]);
		        int amount2 = Integer.parseInt(info2[0]);
		        String item_name1 = info1[1].toUpperCase();
		        String item_name2 = info2[1].toUpperCase();
		        
	        	buyer.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.config.getString("confirm-trade").replace("{AMOUNT1}", String.valueOf(amount1)).replace("{AMOUNT2}", String.valueOf(amount2)).replace("{ITEM1}", item_name1.toLowerCase()).replace("{ITEM2}", item_name2.toLowerCase())));
			} catch (Exception ex) {
				return;
			}

		}
		
	}
	
	public static boolean containsAtLeast(Inventory inv, Material mat, int amt)
    {
	    int count = 0;
        for(ItemStack itm : inv.getContents())
        {
            if(itm != null)
                if(itm.getType() == mat)
                {
                    count += itm.getAmount();
                }
                
            if(count >= amt)
                return true;
        }
        
        return false;
    }

    //checks to see if a string is an integer, IDK if i use this in this plugin but its here
    public static boolean isInt(String str)
    {
        try{
            Integer.parseInt(str);
        }catch(Exception e){
            return false;
        }
        
        return true;
    }
}
