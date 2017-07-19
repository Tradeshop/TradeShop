package org.shanerx.tradeshop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.List;

public class Utils {

	protected final String VERSION = Bukkit.getPluginManager().getPlugin("TradeShop").getDescription().getVersion();
	protected final PluginDescriptionFile pdf = Bukkit.getPluginManager().getPlugin("TradeShop").getDescription();
	protected final String PREFIX = "&a[&eTradeShop&a] ";
	
	public String getPluginName() {
		return pdf.getName();
	}
	
	public String getVersion() {
		return pdf.getVersion();
	}
	
	public List<String> getAuthors() {
		return pdf.getAuthors();
	}
	
	public String getWebsite() {
		return pdf.getWebsite();
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
	
	public boolean canFit(Inventory inv, ItemStack itm, int amt) {
		int count = 0;
		if (inv.firstEmpty() >= 0)
			return true;
		for (ItemStack i : inv.getContents()) {
			if (i != null) {
				if (i.getType() == itm.getType() && i.getData() == itm.getData() && i.getDurability() == itm.getDurability() && i.getItemMeta() == itm.getItemMeta()) {
					count += i.getAmount();
				}
			}
		}
		while (count >= itm.getMaxStackSize()) {
			count -= itm.getMaxStackSize();
		}
		if (count == 0) {
			return false;
		} else {
			return count + amt <= itm.getMaxStackSize();
		}
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