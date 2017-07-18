package org.shanerx.tradeshop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.util.ArrayList;

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
}
