package org.shanerx.tradeshop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Utils {

	final String plugin = "TradeShop";
	final String author = "Lori00";
	final String version = Bukkit.getPluginManager().getPlugin("TradeShop").getDescription().getVersion();
	final String website = null;
	
	final String PREFIX = "&a[&eTradeShop&a] ";

	public String getPluginName() {
		return plugin;
	}
	
	public String getVersion() {
		return version;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public String getWebsite() {
		return website;
	}
	
	public String getPrefix() {
		return PREFIX;
	}
	
	public boolean isTradeShopSign(Block b) {
		if (b.getType() != Material.SIGN_POST && b.getType() != Material.WALL_SIGN) {
			return false;
		}
		Sign sign = (Sign) b.getState();
		if (!ChatColor.stripColor(sign.getLine(0)).equals("[Trade]")) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean isInfiniteTradeShopSign(Block b) {
		if (b.getType() != Material.SIGN_POST && b.getType() != Material.WALL_SIGN) {
			return false;
		}
		Sign sign = (Sign) b.getState();
		if (!ChatColor.stripColor(sign.getLine(0)).equals("[iTrade]")) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean isInt(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean canFit(Inventory inv, ItemStack itm, int amt) 
	{
	    int count = 0;
	    if(inv.firstEmpty() >= 0)
	        return true;
	    for (ItemStack i : inv.getContents()) 
	    {
	        if (i != null) {
	            if (i.getType() == itm.getType() && i.getData() == itm.getData() && i.getDurability() == itm.getDurability() && i.getItemMeta() == itm.getItemMeta()) {
	                count += i.getAmount();

	                Bukkit.getServer().broadcastMessage("For Count: " + count);//Debug #Remove
	            }
	        }
	    }

	    while(count >= itm.getMaxStackSize())
	    {
	        Bukkit.getServer().broadcastMessage("While Count: " + count);//Debug #Remove
	        count -= itm.getMaxStackSize();
	    }

        Bukkit.getServer().broadcastMessage("Count + Amt: " + (count + amt) + "    StackSize: " + itm.getMaxStackSize()); //Debug #Remove
        if(count == 0)
            return false;
        else
            return count + amt <= itm.getMaxStackSize();
	}
	
    public boolean containsAtLeast(Inventory inv, Material mat, int amt) {
        int count = 0;
        for (ItemStack itm : inv.getContents()) {
            if (itm != null) {
                if (itm.getType() == mat) {
                    count += itm.getAmount();
                }
            }
        }
        return count >= amt;
    }
    
    
}
