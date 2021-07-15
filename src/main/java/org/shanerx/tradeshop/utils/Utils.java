/*
 *
 *                         Copyright (c) 2016-2019
 *                SparklingComet @ http://shanerx.org
 *               KillerOfPie @ http://killerofpie.github.io
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *                http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  NOTICE: All modifications made by others to the source code belong
 *  to the respective contributor. No contributor should be held liable for
 *  any damages of any kind, whether be material or moral, which were
 *  caused by their contribution(s) to the project. See the full License for more information.
 *
 */

package org.shanerx.tradeshop.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.PluginDescriptionFile;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.enumys.*;
import org.shanerx.tradeshop.objects.Debug;
import org.shanerx.tradeshop.objects.Shop;
import org.shanerx.tradeshop.objects.ShopItemStack;
import org.shanerx.tradeshop.objects.ShopLocation;

import java.util.*;
import java.util.logging.Level;


/**
 * This class contains a bunch of utility methods that
 * are used in almost every class of the plugin. It was
 * designed with the DRY concept in mind.
 */
public class Utils {

	private final UUID KOPUUID = UUID.fromString("daf79be7-bc1d-47d3-9896-f97b8d4cea7d");
	private final UUID LORIUUID = UUID.fromString("e296bc43-2972-4111-9843-48fc32302fd4");
	public final TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
	protected PluginDescriptionFile pdf = plugin.getDescription();

	public Debug debugger;

	public Utils() {
		debugger = plugin.getDebugger();
	}

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
	 * Returns true if the number is an {@code int}.
	 *
	 * @param str the string that should be parsed
	 * @return true if it is an {@code int}.
	 */
	public boolean isInt(String str) {
		try {
			Integer.parseInt(str);
		} catch (Exception e) {
			return false;
		}
		return true;
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
		boolean ret;
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
	 * Sets the event sign to a failed creation sign
	 *
	 * @param e    Event to reset the sign for
	 * @param shop Shoptype enum to get header
	 */
	public void failedSignReset(SignChangeEvent e, ShopType shop) {
		e.setLine(0, colorize(Setting.SHOP_BAD_COLOUR.getString() + shop.toString()));
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
		e.getPlayer().sendMessage(colorize(Setting.MESSAGE_PREFIX.getString() + msg));
	}

	/**
	 * Sets the event sign to a failed creation sign
	 *
	 * @param e   Event to reset the sign for
	 * @param msg The enum constant representing the error message
	 */
	public void failedTrade(PlayerInteractEvent e, Message msg) {
		e.getPlayer().sendMessage(colorize(Setting.MESSAGE_PREFIX.getString() + msg));
	}

	/**
	 * Checks whether or not it is non-blacklisted material.
	 *
	 * @param mat String to check
	 * @return returns true if valid material
	 */
	public boolean isValidType(Material mat) {
		return !plugin.getListManager().isBlacklisted(mat);
	}

	/**
	 * Checks whether the an inventory contains at least a certain amount of a certain material inside a specified inventory.
	 * <br>
	 * This works with the ItemStack's durability, which represents how much a tool is broken or, in case of a block, the block data.
	 *
	 * @param inv  the Inventory object
	 * @param item the item to be checked
	 * @param amount the amount attempting to be traded
	 * @return true if the condition is met.
	 */
	public boolean containsAtLeast(ItemStack[] inv, ItemStack item, int amount) {
		int count = 0;
		for (ItemStack itm : inv) {
			if (itm != null) {
				if (itemCheck(item, itm)) {
					count += itm.getAmount();
				}
			}
		}
		return count >= amount;
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
		return ChatColor.translateAlternateColorCodes('&', msg);
	}

	/**
	 * Finds the TradeShop sign linked to a chest.
	 *
	 * @param chest the block holding the shop's inventory. Can be a chest, a trapped chest, a dropper, a dispenser, a hopper and a shulker box (1.9+).
	 * @return the sign.
	 */
	public Sign findShopSign(Block chest) {
		ShopLocation potentialLocation = plugin.getDataStorage().getChestLinkage(new ShopLocation(chest.getLocation()));
		if (potentialLocation != null && ShopType.isShop(potentialLocation.getLocation().getBlock()))
			return (Sign) potentialLocation.getLocation().getBlock().getState();

		ArrayList<BlockFace> faces = plugin.getListManager().getDirections();
		Collections.reverse(faces);
		ArrayList<BlockFace> flatFaces = new ArrayList<>(Arrays.asList(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST));
		boolean isDouble = false;
		BlockFace doubleSide = null;

		for (BlockFace face : faces) {
			Block relative = chest.getRelative(face);
			if (ShopType.isShop(relative)) {
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
				if (ShopType.isShop(relative)) {
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
        for (BlockFace face : plugin.getListManager().getDirections()) {
			Block relative = sign.getRelative(face);
            if (plugin.getListManager().isInventory(relative)) {
				return relative;
			}
		}

		return null;
	}

	/**
	 * Checks to see if the shop chest is findable
	 *
	 * @param sign the TradeShop sign
	 * @return whether the sign is linked to a chest.
	 */
	public boolean checkShopChest(Block sign) {
		return findShopChest(sign) != null;
	}

	public void log(String text) {
		Bukkit.getLogger().log(Level.INFO, text);
	}

	/**
	 * Returns true if inventory has enough cost to make trade
	 *
	 * @param inv        inventory to check
	 * @param itemList   items to check
	 * @param multiplier multiplier to use for check
	 * @return true if shop has enough cost to make trade
	 */
	public Boolean checkInventory(Inventory inv, List<ShopItemStack> itemList, int multiplier) {
		return getItems(inv, itemList, multiplier).get(0) != null;
    }
	
	

	/**
	 * Checks whether a trade can take place.
	 *
     * @param shop       the Shop object the player is trading with
     * @param playerInv  the Inventory object representing the inventory that is subject to the transaction.
	 * @param multiplier the multiplier for the trade
	 * @param action     the action from the event
	 * @return Exchange status with appropriate response
	 */
    public ExchangeStatus canExchangeAll(Shop shop, Inventory playerInv, int multiplier, Action action) {
		Inventory playerInventory = Bukkit.createInventory(null, playerInv.getStorageContents().length);
		playerInventory.setContents(playerInv.getStorageContents().clone());

        if (shop.getShopType() != ShopType.BITRADE && action == Action.LEFT_CLICK_BLOCK) {
            return ExchangeStatus.NOT_TRADE;
        }

        Inventory shopInv = null;
        Inventory shopInventory = null;

        if (shop.getShopType() != ShopType.ITRADE) {
            shopInv = shop.getChestAsSC().getInventory();
            shopInventory = Bukkit.createInventory(null, shopInv.getStorageContents().length);
            shopInventory.setContents(shopInv.getStorageContents().clone());
        }

		ArrayList<ItemStack> costItems, productItems;

        if (shop.getShopType() == ShopType.ITRADE) { //ITrade trade

			//Method to find Cost items in player inventory and add to cost array
			costItems = getItems(playerInventory, shop.getCost(), multiplier);

			if (!costItems.isEmpty()) {
				if (costItems.get(0) == null) {
					return ExchangeStatus.PLAYER_NO_COST;
				}

				for (ItemStack item : costItems) {
					playerInventory.removeItem(item);
				}
			}

			for (ShopItemStack item : shop.getProduct()) {
				if (!playerInventory.addItem(item.getItemStack()).isEmpty()) {
                    return ExchangeStatus.PLAYER_NO_SPACE;
                }
            }

            return ExchangeStatus.SUCCESS; //Successfully completed trade
        } else if (shop.getShopType() == ShopType.BITRADE && action == Action.LEFT_CLICK_BLOCK) { //BiTrade Reversed Trade

            //Method to find Cost items in player inventory and add to cost array
            costItems = getItems(playerInventory, shop.getProduct(), multiplier); //Reverse BiTrade, Product is Cost
            if (costItems.get(0) == null) {
                return ExchangeStatus.PLAYER_NO_COST;
            }

            //Method to find Product items in shop inventory and add to product array
            productItems = getItems(shopInventory, shop.getCost(), multiplier); //Reverse BiTrade, Cost is Product
            if (productItems.get(0) == null) {
                shop.updateStatus();
                return ExchangeStatus.SHOP_NO_PRODUCT;
            }
        } else { // Normal Trade

            //Method to find Cost items in player inventory and add to cost array
            costItems = getItems(playerInventory, shop.getCost(), multiplier);
            if (costItems.get(0) == null) {
                return ExchangeStatus.PLAYER_NO_COST;
            }

            //Method to find Product items in shop inventory and add to product array
            productItems = getItems(shopInventory, shop.getProduct(), multiplier);
            if (productItems.get(0) == null) {
                shop.updateStatus();
                return ExchangeStatus.SHOP_NO_PRODUCT;
            }

        }

        //For loop to remove cost items from player inventory
        for (ItemStack item : costItems) {
            playerInventory.removeItem(item);
        }

        //For loop to remove product items from shop inventory
        for (ItemStack item : productItems) {
            shopInventory.removeItem(item);
        }

        //For loop to put cost items in shop inventory
        for (ItemStack item : costItems) {
            if (!shopInventory.addItem(item).isEmpty()) {
                return ExchangeStatus.SHOP_NO_SPACE;
            }
        }

        //For loop to put product items in player inventory
        for (ItemStack item : productItems) {
            if (!playerInventory.addItem(item).isEmpty()) {
                return ExchangeStatus.PLAYER_NO_SPACE;
            }
        }

        return ExchangeStatus.SUCCESS; //Successfully completed trade
	}

    //Returns an arraylist of the itemstacks to be removed/added, if it could not get enough of an item, will return index 0 as null and index 1 as item it could not get enough of
	public ArrayList<ItemStack> getItems(Inventory inventory, List<ShopItemStack> items, int multiplier) {
        Inventory clone = Bukkit.createInventory(null, inventory.getStorageContents().length);
        clone.setContents(inventory.getStorageContents());
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
        int totalCount = 0, currentCount = 0;
        debugger.log("ShopTradeListener > Inventory Type Being Searched: " + inventory.getType().name(), DebugLevels.TRADE);
		debugger.log("ShopTradeListener > Inventory Location Being Searched: " + (inventory.getLocation() != null ? inventory.getLocation().toString() : "null"), DebugLevels.TRADE);

		for (ShopItemStack item : items) {
			totalCount += item.getItemStack().getAmount() * multiplier;
			int count = item.getItemStack().getAmount() * multiplier, traded;

			debugger.log("ShopTradeListener > Item Material Being Searched for: " + item.getItemStack().getType().name(), DebugLevels.TRADE);
			debugger.log("ShopTradeListener > Item count: " + count, DebugLevels.TRADE);

			for (ItemStack storageItem : clone.getStorageContents()) {
				if (storageItem != null && item.isSimilar(storageItem)) {

					traded = Math.min(Math.min(storageItem.getAmount(), item.getItemStack().getMaxStackSize()), count);

					storageItem.setAmount(traded);

					clone.removeItem(storageItem);
					ret.add(storageItem);
					debugger.log("ShopTradeListener > Item traded: " + traded, DebugLevels.TRADE);

					count -= traded;
					currentCount += traded;
					debugger.log("ShopTradeListener > Item new count: " + count, DebugLevels.TRADE);
				}

				if (currentCount >= totalCount) break;
			}

            if (currentCount < totalCount || ret.isEmpty()) {
                debugger.log("ShopTradeListener > TotalCount: " + totalCount, DebugLevels.TRADE);
                debugger.log("ShopTradeListener > CurrentCount: " + currentCount, DebugLevels.TRADE);
                ret.clear();
                ret.add(0, null);
				ret.add(1, item.getItemStack());
				ret.get(1).setAmount(item.getItemStack().getAmount() * multiplier);
            }
        }

        return ret;
    }
}