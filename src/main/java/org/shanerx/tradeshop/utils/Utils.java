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

package org.shanerx.tradeshop.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Nameable;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.PluginDescriptionFile;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.enums.Message;
import org.shanerx.tradeshop.enums.Potions;
import org.shanerx.tradeshop.enums.Setting;
import org.shanerx.tradeshop.enums.ShopType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;


/**
 * This class contains a bunch of utility methods that
 * are used in almost every class of the plugin. It was
 * designed with the DRY concept in mind.
 */
public class Utils {

	protected final PluginDescriptionFile pdf = Bukkit.getPluginManager().getPlugin("TradeShop").getDescription();
	protected final String PREFIX = "&a[&eTradeShop&a] ";

	protected final TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");

	private final UUID KOPUUID = UUID.fromString("daf79be7-bc1d-47d3-9896-f97b8d4cea7d");
	private final UUID LORIUUID = UUID.fromString("e296bc43-2972-4111-9843-48fc32302fd4");

	public UUID[] getMakers() {
		return new UUID[]{KOPUUID, LORIUUID};
	}

	/**
	 * Returns the plugin name.
	 *
	 * @return the name.
	 */
	protected String getPluginName() {
		return pdf.getName();
	}

	/**
	 * Returns the plugin's version.
	 *
	 * @return the version
	 */
	public String getVersion() {
		return pdf.getVersion();
	}

	/**
	 * Returns a list of authors.
	 *
	 * @return the authors
	 */
	public List<String> getAuthors() {
		return pdf.getAuthors();
	}

	/**
	 * Returns the website of the plugin.
	 *
	 * @return the website
	 */
	public String getWebsite() {
		return pdf.getWebsite();
	}

	/**
	 * Returns the prefix of the plugin.
	 *
	 * @return the prefix
	 */
	public String getPrefix() {
		return PREFIX;
	}

	/**
	 * Checks whether or not the block entered is a {@code Trade} sign.
	 *
	 * @return true if it is
	 */
	public boolean isTradeShopSign(Block b) {
		if (!isSign(b)) {
			return false;
		}
		Sign sign = (Sign) b.getState();
		return ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase(ShopType.TRADE.toHeader());
	}


	/**
	 * Checks whether or not the block entered is a {@code BiTrade} sign.
	 *
	 * @return true if it is
	 */
	public boolean isBiTradeShopSign(Block b) {
		if (!isSign(b)) {
			return false;
		}
		Sign sign = (Sign) b.getState();
		return ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase(ShopType.BITRADE.toHeader());
	}

	/**
	 * Checks whether or not the block entered is a {@code iTrade} sign.
	 *
	 * @return true if it is
	 */
	public boolean isInfiniteTradeShopSign(Block b) {
		if (!isSign(b)) {
			return false;
		}
		Sign sign = (Sign) b.getState();
		return ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase(ShopType.ITRADE.toHeader());
	}

	/**
	 * Returns true if it is a TradeShop (Regardless of its type).
	 *
	 * @param b the sign block
	 * @return true if it is a TradeShop.
	 */
	public boolean isShopSign(Block b) {
		return isTradeShopSign(b) || isInfiniteTradeShopSign(b) || isBiTradeShopSign(b);
	}

	/**
	 * Returns true if it is a sign (not necessarily a TradeSign).
	 *
	 * @param b the sign block
	 * @return true if it is a sign.
	 */
	public boolean isSign(Block b) {
		return b != null && (b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN);
	}

	/**
	 * Returns true if the number is an {@code int}.
	 *
	 * @param str the string that should be parsed
	 * @return true if it is an {@code int}.
	 */
	public boolean isInt(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Returns true itemStacks are equal excluding amount.
	 *
	 * @param itm1 the first item
	 * @param itm2 the ssecond item
	 * @return true if it args are equal.
	 */
	public boolean itemCheck(ItemStack itm1, ItemStack itm2) {
		int i1 = itm1.getAmount(), i2 = itm2.getAmount();
		ItemMeta temp1 = itm1.getItemMeta();
		MaterialData temp11 = itm1.getData();
		boolean ret = false;
		itm1.setAmount(1);
		itm2.setAmount(1);

		if (!itm1.hasItemMeta() && itm2.hasItemMeta()) {
			itm1.setItemMeta(itm2.getItemMeta());
			itm1.setData(itm2.getData());
		}
		ret = itm1.equals(itm2);

		itm1.setItemMeta(temp1);
		itm1.setData(temp11);
		itm1.setAmount(i1);
		itm2.setAmount(i2);
		return ret;
	}

	/**
	 * Checks whether or not a certain ItemStack can fit inside an inventory.
	 *
	 * @param inv the Inventory the item should be placed into
	 * @param itm the ItemStack
	 * @return true if the Inventory has enough space for the ItemStack.
	 */
	public boolean canFit(Inventory inv, ItemStack itm) {
		int count = 0, empty = 0;
		for (ItemStack i : inv.getContents()) {
			if (i != null) {
				if (itemCheck(itm, i)) {
					count += i.getAmount();
				}
			} else
				empty += itm.getMaxStackSize();
		}
		return empty + (count % itm.getMaxStackSize()) >= itm.getAmount();
	}

	/**
	 * Checks whether a trade can take place.
	 *
	 * @param inv    the Inventory object representing the inventory that is subject to the transaction.
	 * @param itmOut the ItemStack that is being given away
	 * @param itmIn  the ItemStack that is being received
	 * @return true if the exchange may take place.
	 */
	public boolean canExchange(Inventory inv, ItemStack itmOut, ItemStack itmIn) {
		int count = 0,
				slots = 0,
				empty = 0,
				removed = 0,
				amtIn = itmIn.getAmount(),
				amtOut = itmOut.getAmount();

		for (ItemStack i : inv.getContents()) {
			if (i != null) {
				if (itemCheck(itmIn, i)) {
					count += i.getAmount();
					slots++;
				} else if (itemCheck(itmOut, i) && amtOut != removed) {

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

	/**
	 * Serves as reference for blacklist item
	 *
	 * @return returns item for blacklist fail
	 */
	public ItemStack getBlackListItem() {
		ItemStack blacklist = new ItemStack(Material.BEDROCK);
		ItemMeta bm = blacklist.getItemMeta();
		bm.setDisplayName("blacklisted&4&0&4");
		blacklist.setItemMeta(bm);
		return blacklist;
	}

	/**
	 * Sets the event sign to a failed creation sign
	 *
	 * @param e    Event to reset the sign for
	 * @param shop Shoptype enum to get header
	 */
	public void failedSignReset(SignChangeEvent e, ShopType shop) {
		e.setLine(0, ChatColor.DARK_RED + shop.toString());
		e.setLine(1, "");
		e.setLine(2, "");
		e.setLine(3, "");
	}

	/**
	 * Sets the event sign to a failed creation sign
	 *
	 * @param e    event where shop creation failed
	 * @param shop Shoptype enum to get header
	 * @param msg  The enum constant representing the error message
	 */
	public void failedSign(SignChangeEvent e, ShopType shop, Message msg) {
		failedSignReset(e, shop);
		e.getPlayer().sendMessage(colorize(getPrefix() + msg));
	}

	/**
	 * Sets the event sign to a failed creation sign
	 *
	 * @param e   Event to reset the sign for
	 * @param msg The enum constant representing the error message
	 */
	public void failedTrade(PlayerInteractEvent e, Message msg) {
		e.getPlayer().sendMessage(colorize(getPrefix() + msg));
	}

	/**
	 * Checks whether or not it is a valid material or custom item.
	 *
	 * @param mat String to check
	 * @return returns item or null if invalid
	 */
	public ItemStack isValidType(String mat) {
		ArrayList<String> illegalItems = plugin.getListManager().getBlacklist();
		Set<String> customItemSet = plugin.getCustomItemManager().getItems();
		String matLower = mat.toLowerCase();
		ItemStack blacklist = getBlackListItem();

		if (isInt(mat) && Material.getMaterial(Integer.parseInt(mat)) != null) {
			Material temp = Material.getMaterial(Integer.parseInt(mat));
			if (illegalItems.contains(Material.matchMaterial(mat))) {
				return blacklist;
			}

			return new ItemStack(temp, 1);
		}

		if (Material.matchMaterial(mat) != null) {
			Material temp = Material.matchMaterial(mat);
			if (illegalItems.contains(temp.name().toLowerCase())) {
				return blacklist;
			}

			return new ItemStack(temp, 1);
		}

		if (customItemSet.size() > 0) {
			for (String str : customItemSet) {
				if (str.equalsIgnoreCase(mat)) {
					ItemStack temp = plugin.getCustomItemManager().getItem(mat);
					if (!Setting.ALLOW_CUSTOM_ILLEGAL_ITEMS.getBoolean()) {
						if (illegalItems.contains(temp.getType().name().toLowerCase())) {
							return blacklist;
						}
					}

					return temp;
				}
			}
		}

		if (Potions.isType(mat)) {
			ItemStack temp = Potions.valueOf(mat.toUpperCase()).getItem();
			if (illegalItems.contains(matLower)) {
				return null;
			} else if (matLower.contains("p_")) {
				if (illegalItems.contains("potion")) {
					return blacklist;
				}
			} else if (matLower.contains("s_")) {
				if (illegalItems.contains("splash_potion")) {
					return blacklist;
				}
			} else if (matLower.contains("l_")) {
				if (illegalItems.contains("lingering_potion")) {
					return blacklist;
				}
			}

			return temp;
		}

		return null;

	}

	/**
	 * Checks whether or not it is a valid material or custom item.
	 *
	 * @param mat        String to check
	 * @param durability durability to set
	 * @param amount     amount to set
	 * @return returns item or null if invalid
	 */
	public ItemStack isValidType(String mat, int durability, int amount) {
		ItemStack itm = isValidType(mat);

		if (itm == null) {
			return null;
		}

		itm.setDurability((short) durability);
		itm.setAmount(amount);
		return itm;
	}

	/**
	 * Checks whether or not it is a valid material or custom item.
	 *
	 * @param itm Item to check
	 * @return true if item is blacklist item
	 */
	public boolean isBlacklistItem(ItemStack itm) {
		ItemStack blacklist = getBlackListItem();

		if (!itm.hasItemMeta()) {
			return false;
		} else if (!itm.getItemMeta().hasDisplayName()) {
			return false;
		} else return itm.getItemMeta().getDisplayName().equalsIgnoreCase(blacklist.getItemMeta().getDisplayName());
	}

	/**
	 * Checks whether the an inventory contains at least a certain amount of a certain material inside a specified inventory.
	 * <br>
	 * This works with the ItemStack's durability, which represents how much a tool is broken or, in case of a block, the block data.
	 *
	 * @param inv  the Inventory object
	 * @param item the item to be checked
	 * @return true if the condition is met.
	 */
	public boolean containsAtLeast(Inventory inv, ItemStack item) {
		int count = 0;
		for (ItemStack itm : inv.getContents()) {
			if (itm != null) {
				if (itemCheck(item, itm)) {
					count += itm.getAmount();
				}
			}
		}
		return count >= item.getAmount();
	}

	/**
	 * This function wraps up Bukkit's method {@code ChatColor.translateAlternateColorCodes('&', msg)}.
	 * <br>
	 * Used for shortening purposes and follows the DRY concept.
	 *
	 * @param msg string containing Color and formatting codes.
	 * @return the colorized string returned by the above method.
	 */
	public String colorize(String msg) {
		msg = ChatColor.translateAlternateColorCodes('&', msg);
		return msg;
	}

	/**
	 * Finds the TradeShop sign linked to a chest.
	 *
	 * @param chest the block holding the shop's inventory. Can be a chest, a trapped chest, a dropper, a dispenser, a hopper and a shulker box (1.9+).
	 * @return the sign.
	 */
	public Sign findShopSign(Block chest) {
		ArrayList<BlockFace> faces = plugin.getListManager().getDirections();
		Collections.reverse(faces);
		ArrayList<BlockFace> flatFaces = new ArrayList<>(Arrays.asList(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST));
		boolean isDouble = false;
		BlockFace doubleSide = null;

		for (BlockFace face : faces) {
			Block relative = chest.getRelative(face);
			if (isShopSign(relative)) {
				return (Sign) relative.getState();
			} else if (flatFaces.contains(face) && (chest.getType().equals(Material.CHEST) || chest.getType().equals(Material.TRAPPED_CHEST))) {
				if (relative.getType().equals(chest.getType())) {
					isDouble = true;
					doubleSide = face;
				}
			}
		}

		if (isDouble) {
			chest = chest.getRelative(doubleSide);
			for (BlockFace face : faces) {
				Block relative = chest.getRelative(face);
				if (isShopSign(relative)) {
					return (Sign) relative.getState();
				}
			}
		}

		return null;
	}

	/**
	 * Finds the TradeShop chest, dropper, dispenser, hopper or shulker box (1.9+) linked to a sign.
	 *
	 * @param sign the TradeShop sign
	 * @return the shop's inventory holder block.
	 */
	public Block findShopChest(Block sign) {
		ArrayList<Material> invs = plugin.getListManager().getInventories();
		ArrayList<BlockFace> faces = plugin.getListManager().getDirections();

		for (BlockFace face : faces) {
			Block relative = sign.getRelative(face);
			if (relative != null)
				if (invs.contains(relative.getType()))
					return relative;
		}

		return null;
	}

	/**
	 * Checks to see if the shop chest is findable
	 *
	 * @param sign the TradeShop sign
	 * @return the shop's inventory holder block.
	 */
	public boolean checkShopChest(Block sign) {
		return findShopChest(sign) != null;
	}

	/**
	 * Sets the name (title) of an inventory.
	 * <br>
	 * Represents a wrapper method for {@code Nameable#setCustomTitle(title)}
	 * and was written with the DRY concept in mind.
	 *
	 * @param ih    the InventoryHolder object
	 * @param title the new title.
	 */
	public void setName(InventoryHolder ih, String title) {
		if (ih instanceof Nameable) {
			((Nameable) ih).setCustomName(title);
		}
	}
}