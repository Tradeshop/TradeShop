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
import org.bukkit.Nameable;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.shanerx.tradeshop.enumys.Setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShopManager extends Utils {

	/**
	 * Returns all the owners of a TradeShop, including the one on the last line of the sign.
	 *
	 * @param b the inventory holder block
	 * @return all the owners.
	 */
	public List<OfflinePlayer> getShopOwners(Block b) {
		if (!plugin.getListManager().isInventory(b.getType())) {
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
			} else if (!owners.contains(Bukkit.getOfflinePlayer(s.getLine(3)))) {
				owners.add(Bukkit.getOfflinePlayer(s.getLine(3)));
				changeInvName(b.getState(), readInvName(b.getState()), Collections.singletonList(plugin.getServer().getOfflinePlayer(s.getLine(3))), Collections.emptyList());
			}
		} catch (NullPointerException npe) {
		}
		return owners;
	}

	/**
	 * Returns all the owners of a TradeShop, including the one on the last line of the sign.
	 *
	 * @param block the inventory holder block
	 * @return all the members.
	 */
	public List<OfflinePlayer> getShopMembers(Block block) {
		BlockState b = block.getState();

		if (!plugin.getListManager().isInventory(b.getType())) {
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
		} catch (NullPointerException npe) {
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
		if (!plugin.getListManager().isInventory(b.getType())) {
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
		sb.append(name + " <");
		owners.forEach(o -> sb.append("o:" + o.getName() + ";"));
		members.forEach(m -> sb.append("m:" + m.getName() + ";"));
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
		if (!plugin.getListManager().isInventory(state.getType())) {
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
		String[] temp = name.split(";");

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
		if (getShopUsers(b).size() >= Setting.MAX_SHOP_USERS.getInt()) {
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
	 * Adds a player to the members list of a TradeShop.
	 * <br>
	 * The target player is not required to be online at the time of the operation.
	 *
	 * @param s the TradeShop sign
	 * @param p the OfflinePlayer object.
	 * @return true if successful
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
		List<OfflinePlayer> owners = getShopOwners(b);
		List<OfflinePlayer> members = getShopMembers(b);
		members.remove(p);

		changeInvName(b.getState(), readInvName(b.getState()), owners, members);
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
	 * @return true if successful
	 */
	public boolean addOwner(Block b, OfflinePlayer p) {
		if (getShopUsers(b).size() >= Setting.MAX_SHOP_USERS.getInt()) {
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
	 * Adds a player to the owners list of a TradeShop.
	 * <br>
	 * The target player is not required to be online at the time of the operation.
	 *
	 * @param s the TradeShop sign
	 * @param p the OfflinePlayer object.
	 * @return true if successful
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
		List<OfflinePlayer> owners = getShopOwners(b);
		List<OfflinePlayer> members = getShopMembers(b);
		owners.remove(p);

		changeInvName(b.getState(), readInvName(b.getState()), owners, members);
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