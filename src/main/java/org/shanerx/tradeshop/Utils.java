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

package org.shanerx.tradeshop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Nameable;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Utils {
	
	protected final String VERSION = Bukkit.getPluginManager().getPlugin("TradeShop").getDescription().getVersion();
	protected final PluginDescriptionFile pdf = Bukkit.getPluginManager().getPlugin("TradeShop").getDescription();
	protected final String PREFIX = "&a[&eTradeShop&a] ";
	
	protected final Permission PHELP = new Permission("tradeshop.help");
	protected final Permission PCREATE = new Permission("tradeshop.create");
	protected final Permission PADMIN = new Permission("tradeshop.admin");
	protected final Permission PCREATEI = new Permission("tradeshop.create.infinite");
	protected final Permission PCREATEBI = new Permission("tradeshop.create.bi");
	
	protected final TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
	
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
	
	public Permission getHelpPerm() {
		return PHELP;
	}
	
	public Permission getCreatePerm() {
		return PCREATE;
	}
	
	public Permission getCreateBiPerm() {
		return PCREATEBI;
	}
	
	public Permission getAdminPerm() {
		return PADMIN;
	}
	
	public Permission getCreateIPerm() {
		return PCREATEI;
	}
	
	public boolean isTradeShopSign(Block b) {
		if (!isSign(b)) {
			return false;
		}
		Sign sign = (Sign) b.getState();
		if (!ChatColor.stripColor(sign.getLine(0)).equals("[Trade]")) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean isBiTradeShopSign(Block b) {
		if (!isSign(b)) {
			return false;
		}
		Sign sign = (Sign) b.getState();
		if (!ChatColor.stripColor(sign.getLine(0)).equals("[BiTrade]")) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean isInfiniteTradeShopSign(Block b) {
		if (!isSign(b)) {
			return false;
		}
		Sign sign = (Sign) b.getState();
		if (!ChatColor.stripColor(sign.getLine(0)).equals("[iTrade]")) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean isShopSign(Block b) {
		return isTradeShopSign(b) || isInfiniteTradeShopSign(b) || isBiTradeShopSign(b);
	}
	
	public boolean isSign(Block b) {
		if (b == null)
			return false;
		return b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN;
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
		int count = 0, empty = 0;
		for (ItemStack i : inv.getContents()) {
			if (i != null) {
				if (i.getType() == itm.getType() && i.getData() == itm.getData() && i.getDurability() == itm.getDurability() && i.getItemMeta() == itm.getItemMeta()) {
					count += i.getAmount();
				}
			} else
				empty += itm.getMaxStackSize();
		}
		
		return empty + (count % itm.getMaxStackSize()) >= amt;
	}
	
	public boolean canExchange(Inventory inv, ItemStack itmOut, int amtOut, ItemStack itmIn, int amtIn) {
		
		int count = 0, slots = 0, empty = 0, removed = 0;
		
		for (ItemStack i : inv.getContents()) {
			if (i != null) {
				if (i.getType() == itmIn.getType() && i.getDurability() == itmIn.getDurability()) {
					count += i.getAmount();
					slots++;
				} else if (i.getType() == itmOut.getType() && i.getDurability() == itmOut.getDurability() && amtOut != removed) {
					
					if (i.getAmount() > amtOut - removed) {
						removed = amtOut;
					} else if (i.getAmount() == amtOut - removed) {
						removed = amtOut;
						empty += itmIn.getMaxStackSize();
					} else if (i.getAmount() < amtOut - removed) {
						removed += i.getAmount();
						empty += itmIn.getMaxStackSize();
					}
				}
			} else
				empty += itmIn.getMaxStackSize();
		}
		
		return empty + ((slots * itmIn.getMaxStackSize()) - count) >= amtIn;
		
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
	
	public boolean containsAtLeast(Inventory inv, Material mat, short durability, int amt) {
		int count = 0;
		for (ItemStack itm : inv.getContents()) {
			if (itm != null) {
				if (itm.getType() == mat && itm.getDurability() == durability) {
					count += itm.getAmount();
				}
			}
		}
		return count >= amt;
	}
	
	public String colorize(String msg) {
		msg = ChatColor.translateAlternateColorCodes('&', msg);
		return msg;
	}
	
	public Sign findShopSign(Block chest) {
		ArrayList<BlockFace> faces = plugin.getAllowedDirections();
		Collections.reverse(faces);
		
		for (BlockFace face : faces) {
			Block relative = chest.getRelative(face);
			if (isSign(relative))
				if (isShopSign(relative))
					return (Sign) chest.getRelative(face).getState();
		}
		
		
		return null;
		
	}
	
	public Block findShopChest(Block sign) {
		ArrayList<Material> invs = plugin.getAllowedInventories();
		ArrayList<BlockFace> faces = plugin.getAllowedDirections();
		
		for (BlockFace face : faces) {
			Block relative = sign.getRelative(face);
			if (relative != null)
				if (invs.contains(relative.getType()))
					return sign.getRelative(face);
		}
		return null;
	}
	
	public List<OfflinePlayer> getShopOwners(Block b) {
		TradeShop ts = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
		if (!ts.getAllowedInventories().contains(b.getType())) {
			return null;
		}
		
		List<OfflinePlayer> owners = new ArrayList<>();
		Inventory inv = ((InventoryHolder) b.getState()).getInventory();
		String names = inv.getName();
		for (String m : names.split(";")) {
			if (m.startsWith("o:")) {
				owners.add(Bukkit.getOfflinePlayer(m.substring(2)));
			}
		}
		return owners;
	}
	
	public List<OfflinePlayer> getShopMembers(Block b) {
		TradeShop ts = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
		if (!ts.getAllowedInventories().contains(b.getType())) {
			return null;
		}
		
		List<OfflinePlayer> members = new ArrayList<>();
		Inventory inv = ((InventoryHolder) b.getState()).getInventory();
		String names = inv.getName();
		for (String m : names.split(";")) {
			members.add(Bukkit.getOfflinePlayer(m.substring(2)));
		}
		return members;
	}
	
	public List<OfflinePlayer> getShopOwners(Sign s) {
		Chest c = (Chest) findShopChest(s.getBlock());
		if (c == null) {
			return null;
		}
		return getShopOwners(c.getBlock());
	}
	
	public List<OfflinePlayer> getShopMembers(Sign s) {
		Chest c = (Chest) findShopChest(s.getBlock());
		if (c == null) {
			return null;
		}
		return getShopMembers(c.getBlock());
	}
	
	public void setName(InventoryHolder ih, String title) {
		if (ih instanceof Nameable) {
			((Nameable) ih).setCustomName(title);
		}
	}
}