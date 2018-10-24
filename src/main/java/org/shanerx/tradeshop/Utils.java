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

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.*;


/**
 * This class contains a bunch of utility methods that
 * are used in almost every class of the plugin. It was
 * designed with the DRY concept in mind.
 */
public class Utils {

    protected final PluginDescriptionFile pdf = Bukkit.getPluginManager().getPlugin("TradeShop").getDescription();
    protected final String PREFIX = "&a[&eTradeShop&a] ";

    protected final TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");

    private final Permission PHELP = new Permission("tradeshop.help");
    private final Permission PCREATE = new Permission("tradeshop.create");
    private final Permission PADMIN = new Permission("tradeshop.admin");
    private final Permission PCREATEI = new Permission("tradeshop.create.infinite");
    private final Permission PCREATEBI = new Permission("tradeshop.create.bi");
    private final Permission PWHO = new Permission("tradeshop.who");

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
        return ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase(ShopType.TRADE.header());
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
        return ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase(ShopType.BITRADE.header());
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
        return ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase(ShopType.ITRADE.header());
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
        return b != null && (b.getType() == Material.SIGN || b.getType() == Material.WALL_SIGN);
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
     * @param itm2 the second item
     * @return true if it args are equal.
     */
    public boolean itemCheck(ItemStack itm1, ItemStack itm2) {
        int check = 0;

        if (itm1.getType().equals(itm2.getType())) {
            check++;
        }

        //noinspection deprecation
        if (itm1.getDurability() == itm2.getDurability()) {
            check++;
        }

        if (itm1.getData().equals(itm2.getData())) {
            check++;
        }

        if (itm1.hasItemMeta() && itm2.hasItemMeta()) {
            if (itm1.getItemMeta().equals(itm2.getItemMeta())) {
                check++;
            }
        } else if (!(itm1.hasItemMeta() && itm2.hasItemMeta())) {
            check++;
        }

        return check == 4;
    }

    /**
     * Checks whether a trade can take place.
     *
     * @param inv    the Inventory object representing the inventory that is subject to the transaction.
     * @param itmOut the ItemStack that is being given away
     * @param itmIn  the ItemStack that is being received
     * @return true if the exchange may take place.
     */
    public boolean canNotExchange(Inventory inv, ItemStack itmOut, ItemStack itmIn) {
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
        return empty + ((slots * itmIn.getMaxStackSize()) - count) < amtIn;
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
        e.setLine(0, ChatColor.DARK_RED + shop.header());
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
        ArrayList<String> illegalItems = plugin.getIllegalItems();
        Set<String> customItemSet = plugin.getCustomItemSet();
        String matLower = mat.toLowerCase();
        ItemStack blacklist = getBlackListItem();

        if (isInt(mat)) {
            return null;
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
                    ItemStack temp = plugin.getCustomItem(mat);
                    if (!plugin.getSettings().getBoolean("allow-custom-illegal-items")) {
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

        //noinspection deprecation
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
    public boolean containsLessThan(Inventory inv, ItemStack item) {
        int count = 0;
        for (ItemStack itm : inv.getContents()) {
            if (itm != null) {
                if (itemCheck(item, itm)) {
                    count += itm.getAmount();
                }
            }
        }
        return count < item.getAmount();
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
        ArrayList<BlockFace> faces = plugin.getAllowedDirections();
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
        ArrayList<Material> invs = plugin.getAllowedInventories();
        ArrayList<BlockFace> faces = plugin.getAllowedDirections();

        for (BlockFace face : faces) {
            Block relative = sign.getRelative(face);
            if (relative != null)
                if (invs.contains(relative.getType()))
                    return relative;
        }

        return null;
    }

    /**
     * Returns all the owners of a TradeShop, including the one on the last line of the sign.
     *
     * @param b the inventory holder block
     * @return all the owners.
     */
    @SuppressWarnings("deprecation")
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
        try {
            if (s != null && s.getLine(3).equals("")) {
                if (owners.size() > 0) {
                    s.setLine(3, owners.get(0).getName());
                    s.update();
                    return owners;
                } else {
                    return null;
                }
            } else {
                assert s != null;
                if (!owners.contains(Bukkit.getOfflinePlayer(Objects.requireNonNull(s).getLine(3)))) {
                    owners.add(Bukkit.getOfflinePlayer(s.getLine(3)));
                    changeInvName(b.getState(), readInvName(b.getState()), Collections.singletonList(plugin.getServer().getOfflinePlayer(s.getLine(3))), Collections.emptyList());
                }
            }
        } catch (NullPointerException ignored) {
        }
        return owners;
    }

    /**
     * Returns all the owners of a TradeShop, including the one on the last line of the sign.
     *
     * @param block the inventory holder block
     * @return all the members.
     */
    @SuppressWarnings("deprecation")
    public List<OfflinePlayer> getShopMembers(Block block) {
        BlockState b = block.getState();

        if (!plugin.getAllowedInventories().contains(b.getType())) {
            return null;
        }

        List<OfflinePlayer> members = new ArrayList<>();
        Inventory inv = ((InventoryHolder) b).getInventory();
        String names = inv.getName();
        for (String m : names.split(";")) {
            if (m.startsWith("m:")) {
                members.add(Bukkit.getOfflinePlayer(m.substring(2)));
            }
        }
        Sign s = findShopSign(b.getBlock());
        try {
            if (s.getLines().length != 4 || s.getLine(3).equals("")) {
                if (members.size() > 0) {
                    s.setLine(3, members.get(0).getName());
                    s.update();
                } else {
                    return null;
                }
                return members;
            } else if (getShopOwners(s).size() == 0 || !getShopOwners(s).contains(Bukkit.getOfflinePlayer(s.getLine(3)))) {
                changeInvName(b, readInvName(b), Collections.singletonList(plugin.getServer().getOfflinePlayer(s.getLine(3))), members);
            }
        } catch (NullPointerException ignored) {
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
        if (getShopOwners(b) != null)
            users.addAll(getShopOwners(b));
        if (getShopMembers(b) != null)
            users.addAll(getShopMembers(b));

        if (users.size() == 0)
            return null;

        return users;
    }

    /**
     * Returns all the owners of a TradeShop, including the one on the last line of the sign.
     *
     * @param s the TradeShop sign
     * @return all the owners.
     */
    public List<OfflinePlayer> getShopOwners(Sign s) {
        BlockState c = findShopChest(s.getBlock()).getState();
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
        BlockState c = findShopChest(s.getBlock()).getState();
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
        BlockState c = findShopChest(s.getBlock()).getState();
        if (c == null) {
            return null;
        }
        return getShopUsers(c.getBlock());
    }

    /**
     * Sets the name of the inventory
     *
     * @param state   blockState to change the name of
     * @param name    original name of inventory, null to use generic name
     * @param owners  List of inventory owners
     * @param members List of inventory members
     */
    public void changeInvName(BlockState state, String name, List<OfflinePlayer> owners, List<OfflinePlayer> members) {
        StringBuilder sb = new StringBuilder();
        if (name == null || name.equalsIgnoreCase("")) {
            name = "";
        }
        sb.append(name).append(" <");
        owners.forEach(o -> sb.append("o:").append(o.getName()).append(";"));
        members.forEach(m -> sb.append("m:").append(m.getName()).append(";"));
        sb.append(">");
        setName((InventoryHolder) state, sb.toString());
    }

    /**
     * Reads the name of the inventory
     *
     * @param state blockState to change the name of
     * @return Name of inventory
     */
    public String readInvName(BlockState state) {
        if (!plugin.getAllowedInventories().contains(state.getType())) {
            return null;
        }

        Inventory inv = ((InventoryHolder) state).getInventory();

        if (((Nameable) state).getCustomName() == null) {
            return "";
        }

        String[] names = inv.getName().split(" <");

        if (names[0] == null || names[0].equalsIgnoreCase("")) {
            return "";
        } else {
            return names[0];
        }

    }

    /**
     * Resets the name of the inventory
     *
     * @param state blockState to change the name of
     */
    public void resetInvName(BlockState state) {
        Inventory inv = ((InventoryHolder) state).getInventory();
        String name = inv.getName();

        if (name.startsWith("o:")) {
            name = "";
        }

        String[] names = name.split(" <");

        while (names[0].endsWith(" ")) {
            names[0] = names[0].substring(0, name.length() - 2);
        }

        setName(((InventoryHolder) state), names[0]);

    }

    /**
     * Adds a player to the members list of a TradeShop.
     * <br>
     * The target player is not required to be online at the time of the operation.
     *
     * @param b the inventory holder block
     * @param p the OfflinePlayer object.
     * @return true if successful
     */
    public boolean addMember(Block b, OfflinePlayer p) {
        if (getShopUsers(b).size() >= plugin.getSettings().getInt("max-shop-users")) {
            return false;
        }

        List<OfflinePlayer> owners = getShopOwners(b);
        List<OfflinePlayer> members = getShopMembers(b);
        if (!members.contains(p)) {
            members.add(p);
            owners.remove(p);
        } else {
            return false;
        }

        changeInvName(b.getState(), readInvName(b.getState()), owners, members);
        return true;
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
        List<OfflinePlayer> owners = getShopOwners(b);
        List<OfflinePlayer> members = getShopMembers(b);
        members.remove(p);

        changeInvName(b.getState(), readInvName(b.getState()), owners, members);
    }

    /**
     * Adds a player to the owners list of a TradeShop.
     * <br>
     * The target player is not required to be online at the time of the operation.
     *
     * @param b the inventory holder block
     * @param p the OfflinePlayer object.
     * @return true if successful
     */
    public boolean addOwner(Block b, OfflinePlayer p) {
        if (getShopUsers(b).size() >= plugin.getSettings().getInt("max-shop-users")) {
            return false;
        }

        List<OfflinePlayer> owners = getShopOwners(b);
        List<OfflinePlayer> members = getShopMembers(b);
        if (!owners.contains(p)) {
            owners.add(p);
            members.remove(p);
        } else {
            return false;
        }

        changeInvName(b.getState(), readInvName(b.getState()), owners, members);
        return true;
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
        List<OfflinePlayer> owners = getShopOwners(b);
        List<OfflinePlayer> members = getShopMembers(b);
        owners.remove(p);

        changeInvName(b.getState(), readInvName(b.getState()), owners, members);
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
