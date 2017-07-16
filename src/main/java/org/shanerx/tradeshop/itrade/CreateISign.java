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

public class CreateISign extends Utils implements Listener {

	TradeShop plugin;
	
	public CreateISign(TradeShop instance) {
		plugin = instance;
	}
	
	@SuppressWarnings("deprecation")
    @EventHandler
	public void onSignChange(SignChangeEvent event) throws InterruptedException {
	//	BlockState state = event.getBlock().getState();
		Player player =  event.getPlayer();
        Sign s = (Sign) event.getBlock().getState();
        if (! (event.getLine(0).equalsIgnoreCase("[iTrade]")) ) {
			return;
        }
        int x = event.getBlock().getLocation().getBlockX();
        int y = event.getBlock().getLocation().getBlockY();
        int z = event.getBlock().getLocation().getBlockZ();
        String world = event.getBlock().getLocation().getWorld().getName();
    
        final int CHEST_ID =  plugin.getServer().getWorld(world).getBlockTypeIdAt(x, y - 1, z);
        if (! player.hasPermission("tradeshop.create.infinite") ) {
        	s.setLine(0, "");
        	s.update();
	    	s.setLine(1, "");
        	s.update();
        	s.setLine(2, "");
        	s.update();
        	s.setLine(3, "");
        	s.update();
        	player.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.config.getString("no-ts-create-permission")));
        	return;
        }
        if ( CHEST_ID != 54 ) {
        	event.setLine(0, ChatColor.DARK_RED + "[iTrade]");
        	event.setLine(1, "");
        	event.setLine(2, "");
        	event.setLine(3, "");
        	player.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.config.getString("no-chest")));
        	return;
        }
boolean signIsValid = true; // If this is true, the information on the sign is valid!
        
        String line1 = event.getLine(1);
        String line2 = event.getLine(2);
        
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
        
        try 
        {
            amount1 = Integer.parseInt(info1[0]);
            amount2 = Integer.parseInt(info2[0]);
            
            if(isInt(info1[1]))
                item_name1 = Material.getMaterial(Integer.parseInt(info1[1])).name();
            else
                item_name1 = info1[1].toUpperCase();
            
            item1 = new ItemStack(Material.getMaterial(item_name1), amount1);

            if(isInt(info2[1]))
                item_name2 = Material.getMaterial(Integer.parseInt(info2[1])).name();
            else
                item_name2 = info2[1].toUpperCase();
            
            item2 = new ItemStack(Material.getMaterial(item_name2), amount2);
            
        } catch (Exception e) {
            signIsValid = false;
        }
        
        if ( signIsValid == false ) {
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.config.getString("invalid-sign")));
            event.setLine(0, ChatColor.DARK_RED + "[iTrade]");
            event.setLine(1, "");
            event.setLine(2, "");
            event.setLine(3, "");
            return;
        }
        
        String player_name = event.getPlayer().getName();
        event.setLine(3, player_name);
        event.setLine(0, ChatColor.DARK_GREEN + "[iTrade]");
        event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + plugin.config.getString("successful-setup")));
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

