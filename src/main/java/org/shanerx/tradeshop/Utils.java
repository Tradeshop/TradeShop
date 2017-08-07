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
import org.bukkit.Material;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * This class contains a bunch of utility methods that
 * are used in almost every class of the plugin. It was
 * designed with the DRY concept in mind.
 */
public class Utils {

    protected final String VERSION = Bukkit.getPluginManager().getPlugin("TradeShop").getDescription().getVersion();
    protected final PluginDescriptionFile pdf = Bukkit.getPluginManager().getPlugin("TradeShop").getDescription();
    protected final String PREFIX = "&a[&eTradeShop&a] ";
  
    protected final TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
  
    private final Permission PHELP = new Permission("tradeshop.help");
    private final Permission PCREATE = new Permission("tradeshop.create");
    private final Permission PADMIN = new Permission("tradeshop.admin");
    private final Permission PCREATEI = new Permission("tradeshop.create.infinite");
    private final Permission PCREATEBI = new Permission("tradeshop.create.bi");
    private final Permission PWHO = new Permission("tradeshop.who");
  
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
    public String getPluginName() {
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
     * Returns the Help permission.
     *
     * @return help
     */
    public Permission getHelpPerm() {
        return PHELP;
    }

    /**
     * Returns the Who permission.
     *
     * @return who
     */
    public Permission getWhoPerm() {
        return PWHO;
    }

    /**
     * Returns the normal {@code [Trade]} sign create permission.
     *
     * @return the Trade create permission
     */
    public Permission getCreatePerm() {
        return PCREATE;
    }

    /**
     * Returns the {@code [iTrade]} sign create permission.
     *
     * @return the iTrade create permission
     */
    public Permission getCreateIPerm() {
        return PCREATEI;
    }

    /**
     * Returns the {@code [BiTrade]} sign create permission.
     *
     * @return the BiTrade create permission
     */
    public Permission getCreateBiPerm() {
        return PCREATEBI;
    }

    /**
     * Returns the TradeShop admin destroy permission.
     *
     * @return the Trade create permission
     */
    public Permission getAdminPerm() {
        return PADMIN;
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
        return ChatColor.stripColor(sign.getLine(0)).equals("[Trade]");
    }


    /**
     * Checks whether or not the block entered is a {@code iTrade} sign.
     *
     * @return true if it is
     */
    public boolean isBiTradeShopSign(Block b) {
        if (!isSign(b)) {
            return false;
        }
        Sign sign = (Sign) b.getState();
        return ChatColor.stripColor(sign.getLine(0)).equals("[BiTrade]");
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
        return ChatColor.stripColor(sign.getLine(0)).equals("[iTrade]");
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
     * Checks whether or not a certain ItemStack can fit inside an inventory.
     *
     * @param inv the Inventory the item should be placed into
     * @param itm the ItemStack
     * @param amt the amount
     * @return true if the Inventory has enough space for the ItemStack.
     */
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

    /**
     * Checks whether a trade can take place.
     *
     * @param inv    the Inventory object representing the inventory that is subject to the transaction.
     * @param itmOut the ItemStack that is being given away
     * @param amtOut the amount of that ItemStack
     * @param itmIn  the ItemStack that is being received
     * @param amtIn  the amount of that ItemStack
     * @return true if the exchange may take place.
     */
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

    /**
     * Checks whether the an inventory contains at least a certain amount of a certain material inside a specified inventory.
     *
     * @param inv the Inventory object
     * @param mat the Material constant
     * @param amt the amount
     * @return true if the condition is met.
     */
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

    /**
     * Checks whether the an inventory contains at least a certain amount of a certain material inside a specified inventory.
     * <br>
     * This works with the ItemStack's durability, which represents how much a tool is broken or, in case of a block, the block data.
     *
     * @param inv the Inventory object
     * @param mat the Material constant
     * @param amt the amount
     * @return true if the condition is met.
     */
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

/**
     * This function wraps up Bukkit's method {@code ChatColor.translateAlternateColorCodes('&', msg)}.
     * <br>
     * Used for shortening purposes and follows the DRY concept.
     *
     * @param msg
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

 /**
     * Finds the TradeShop chest, dropper, dispenser, hopper or shulker box (1.9+) linked to a sign.
     *
     * @param sign the TradeShop sign
     * @return the shop's inventory holder block.
     */
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

    /**
     * Returns all the owners of a TradeShop, including the one on the last line of the sign.
     *
     * @param b the inventory holder block
     * @return all the owners.
     */
    public List<OfflinePlayer> getShopOwners(Block b) {
        if (!plugin.getAllowedInventories().contains(b.getType())) {
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
        Sign s = findShopSign(b);
        if (s.getLine(3) == null || s.getLine(3).equals("")) {
            if (owners.size() > 0) {
                s.setLine(3, owners.get(0).getName());
                s.update();
            }
            return owners;
        } else if (!owners.contains(Bukkit.getOfflinePlayer(s.getLine(3)))) {
            owners.add(Bukkit.getOfflinePlayer(s.getLine(3)));
            addOwner(s, Bukkit.getOfflinePlayer(s.getLine(3)));
        }
        return owners;
    }

    /**
     * Returns all the owners of a TradeShop, including the one on the last line of the sign.
     *
     * @param b the inventory holder block
     * @return all the members.
     */
    public List<OfflinePlayer> getShopMembers(Block b) {
        if (!plugin.getAllowedInventories().contains(b.getType())) {
            return null;
        }

        List<OfflinePlayer> members = new ArrayList<>();
        Inventory inv = ((InventoryHolder) b.getState()).getInventory();
        String names = inv.getName();
        for (String m : names.split(";")) {
            if (m.startsWith("m:")) {
                members.add(Bukkit.getOfflinePlayer(m.substring(2)));
            }
        }
        Sign s = findShopSign(b);
        if (s.getLine(3) == null || s.getLine(3).equals("")) {
            if (members.size() > 0) {
                s.setLine(3, members.get(0).getName());
                s.update();
            }
            return members;
        } else if (!members.contains(Bukkit.getOfflinePlayer(s.getLine(3)))) {
            members.add(Bukkit.getOfflinePlayer(s.getLine(3)));
            if (getShopOwners(s).size() == 0) {
                addOwner(s, Bukkit.getOfflinePlayer(s.getLine(3)));
            }
        }
        return members;
    }

    /**
     * Returns all the members <b><em>(including the owners)</em></b> of a TradeShop, including the one on the last line of the sign.
     *
     * @param b the inventory holder block
     * @return all the members.
     */
    public List<OfflinePlayer> getShopUsers(Block b) {
        if (!plugin.getAllowedInventories().contains(b.getType())) {
            return null;
        }

        List<OfflinePlayer> users = new ArrayList<>();
        Inventory inv = ((InventoryHolder) b.getState()).getInventory();
        String names = inv.getName();
        for (String m : names.split(";")) {
            users.add(Bukkit.getOfflinePlayer(m.substring(2)));
        }

        Sign s = findShopSign(b);
        if (s.getLine(3) == null || s.getLine(3).equals("")) {
            if (users.size() > 0) {
                s.setLine(3, users.get(0).getName());
                s.update();
            }
            return users;
        } else if (!users.contains(Bukkit.getOfflinePlayer(s.getLine(3)))) {
            users.add(Bukkit.getOfflinePlayer(s.getLine(3)));
            addOwner(s, Bukkit.getOfflinePlayer(s.getLine(3)));
        }
        return users;
    }

    /**
     * Returns all the owners of a TradeShop, including the one on the last line of the sign.
     *
     * @param s the TradeShop sign
     * @return all the owners.
     */
    public List<OfflinePlayer> getShopOwners(Sign s) {
        Chest c = (Chest) findShopChest(s.getBlock()).getState();
        if (c == null) {
            return null;
        }
        return getShopOwners(c.getBlock());
    }

    /**
     * Returns all the members of a TradeShop.
     *
     * @param s the TradeShop sign
     * @return all the members.
     */
    public List<OfflinePlayer> getShopMembers(Sign s) {
        Chest c = (Chest) findShopChest(s.getBlock()).getState();
        if (c == null) {
            return null;
        }
        return getShopMembers(c.getBlock());
    }

    /**
     * Returns all the users <b><em>(including the owners)</em></b> of a TradeShop, including the one on the last line of the sign.
     *
     * @param s the TradeShop sign
     * @return all the members.
     */
    public List<OfflinePlayer> getShopUsers(Sign s) {
        Chest c = (Chest) findShopChest(s.getBlock()).getState();
        if (c == null) {
            return null;
        }
        return getShopUsers(c.getBlock());
    }

    /**
     * Adds a player to the members list of a TradeShop.
     * <br>
     * The target player is not required to be online at the time of the operation.
     *
     * @param b the inventory holder block
     * @param p the OfflinePlayer object.
     * @return true if succesfull
     */
    public boolean addMember(Block b, OfflinePlayer p) {
        if (getShopUsers(b).size() >= plugin.getSettings().getInt("max-shop-users")) {
            return false;
        }

        List<OfflinePlayer> members = getShopMembers(b);
        if (!members.contains(p)) {
            members.add(p);
            if (getShopOwners(b).contains(p)) {
                removeOwner(b, p);
            }
        } else {
            return false;
        }
        StringBuilder sb = new StringBuilder();
        List<String> all = Arrays.asList(((InventoryHolder) b.getState()).getInventory().getName().split(";"));
        members.forEach(m -> sb.append(all.contains("o:" + m.getName()) ? "o:" : "m:").append(m.getName()).append(';'));
        setName((InventoryHolder) b.getState(), sb.toString().substring(0, sb.toString().length()));
        return true;
    }

    /**
     * Adds a player to the members list of a TradeShop.
     * <br>
     * The target player is not required to be online at the time of the operation.
     *
     * @param s the TradeShop sign
     * @param p the OfflinePlayer object.
     * @return true if succesfull
     */
    public boolean addMember(Sign s, OfflinePlayer p) {
        return addMember(findShopChest(s.getBlock()), p);
    }

    /**
     * Removes a player from the members list of a TradeShop.
     * <br>
     * The target player is not required to be online at the time of the operation.
     *
     * @param b the inventory holder block
     * @param p the OfflinePlayer object.
     */
    public void removeMember(Block b, OfflinePlayer p) {
        List<OfflinePlayer> members = getShopMembers(b);
        members.remove(p);
        StringBuilder sb = new StringBuilder();
        List<String> all = Arrays.asList(((InventoryHolder) b.getState()).getInventory().getName().split(";"));
        members.forEach(m -> sb.append(all.contains("o:" + m.getName()) ? "o:" : "m:").append(m.getName()).append(';'));
        setName((InventoryHolder) b.getState(), sb.toString().substring(0, sb.toString().length()));
    }

    /**
     * Removes a player from the members list of a TradeShop.
     * <br>
     * The target player is not required to be online at the time of the operation.
     *
     * @param s the TradeShop sign
     * @param p the OfflinePlayer object.
     */
    public void removeMember(Sign s, OfflinePlayer p) {
        removeMember(findShopChest(s.getBlock()), p);
    }

    /**
     * Adds a player to the owners list of a TradeShop.
     * <br>
     * The target player is not required to be online at the time of the operation.
     *
     * @param b the inventory holder block
     * @param p the OfflinePlayer object.
     * @return true if succesfull
     */
    public boolean addOwner(Block b, OfflinePlayer p) {
        if (getShopUsers(b).size() >= plugin.getSettings().getInt("max-shop-users")) {
            return false;
        }

        List<OfflinePlayer> owners = getShopMembers(b);
        if (!owners.contains(p)) {
            owners.add(p);
            if (getShopMembers(b).contains(p)) {
                removeMember(b, p);
            }
        } else {
            return false;
        }

        owners.add(p);
        StringBuilder sb = new StringBuilder();
        List<String> all = Arrays.asList(((InventoryHolder) b.getState()).getInventory().getName().split(";"));
        owners.forEach(m -> sb.append(all.contains("m:" + m.getName()) ? "m:" : "o:").append(m.getName()).append(';'));
        setName((InventoryHolder) b.getState(), sb.toString().substring(0, sb.toString().length()));
        return true;
    }

    /**
     * Adds a player to the owners list of a TradeShop.
     * <br>
     * The target player is not required to be online at the time of the operation.
     *
     * @param s the TradeShop sign
     * @param p the OfflinePlayer object.
     * @return true if succesfull
     */
    public boolean addOwner(Sign s, OfflinePlayer p) {
        return addOwner(findShopChest(s.getBlock()), p);
    }

    /**
     * Removes a player from the owners list of a TradeShop.
     * <br>
     * The target player is not required to be online at the time of the operation.
     *
     * @param b the inventory holder block
     * @param p the OfflinePlayer object.
     */
    public void removeOwner(Block b, OfflinePlayer p) {
        List<OfflinePlayer> members = getShopMembers(b);
        members.remove(p);
        StringBuilder sb = new StringBuilder();
        List<String> all = Arrays.asList(((InventoryHolder) b.getState()).getInventory().getName().split(";"));
        members.forEach(m -> sb.append(all.contains("m:" + m.getName()) ? "m:" : "o:").append(m.getName()).append(';'));
        setName((InventoryHolder) b.getState(), sb.toString().substring(0, sb.toString().length()));
    }

    /**
     * Removes a player from the owners list of a TradeShop.
     * <br>
     * The target player is not required to be online at the time of the operation.
     *
     * @param s the TradeShop sign
     * @param p the OfflinePlayer object.
     */
    public void removeOwner(Sign s, OfflinePlayer p) {
        removeOwner(findShopChest(s.getBlock()), p);
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
