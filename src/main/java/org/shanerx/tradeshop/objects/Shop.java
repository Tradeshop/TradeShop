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

package org.shanerx.tradeshop.objects;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.enumys.ShopRole;
import org.shanerx.tradeshop.enumys.ShopStatus;
import org.shanerx.tradeshop.enumys.ShopType;
import org.shanerx.tradeshop.utils.ItemSerializer;
import org.shanerx.tradeshop.utils.JsonConfiguration;
import org.shanerx.tradeshop.utils.Tuple;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class Shop implements Serializable {

    private ShopUser owner;
    private List<ShopUser> managers, members;
	private ShopType shopType;
	private ShopLocation shopLoc, chestLoc;
	private transient ItemStack sellItem, buyItem;
	private String sellItemB64, buyItemB64;
	private ShopStatus status = ShopStatus.CLOSED;

	public Shop(Tuple<Location, Location> locations, ShopType shopType, ShopUser owner, Tuple<List<ShopUser>, List<ShopUser>> players, Tuple<ItemStack, ItemStack> items) {
		shopLoc = new ShopLocation(locations.getLeft());
		this.owner = owner;
		chestLoc = new ShopLocation(locations.getRight());
		this.shopType = shopType;
		managers = players.getLeft();
		members = players.getRight();
		sellItem = items.getLeft();
		buyItem = items.getRight();

		sellItemB64 = ItemSerializer.itemStackArrayToBase64(sellItem);
		buyItemB64 = ItemSerializer.itemStackArrayToBase64(buyItem);
	}

	public Shop(Tuple<Location, Location> locations, ShopType shopType, ShopUser owner) {
		shopLoc = new ShopLocation(locations.getLeft());
		this.owner = owner;
		chestLoc = new ShopLocation(locations.getRight());
		this.shopType = shopType;
		managers = Collections.emptyList();
		members = Collections.emptyList();
		sellItemB64 = "";
		buyItemB64 = "";
	}

	public static Shop deserialize(String serialized) {
		Shop shop = new Gson().fromJson(serialized, Shop.class);
		shop.itemsFromB64();

		return shop;
	}

    public void setOwner(ShopUser owner) {
        this.owner = owner;
    }

    public ShopUser getOwner() {
        return owner;
    }

    public void setManagers(List<ShopUser> managers) {
        this.managers = managers;
    }

    public List<ShopUser> getManagers() {
        return managers;
    }

    public void setMembers(List<ShopUser> members) {
        this.members = members;
    }

	public static Shop loadShop(ShopLocation loc) {
		return loadShop(loc.serialize());
	}

	public List<ShopUser> getUsers() {
		List<ShopUser> users = new ArrayList<>();
		users.add(owner);
		users.addAll(managers);
		users.addAll(members);
		return users;
	}

	public ShopChest getChestAsSC() {
		return new ShopChest(chestLoc.getLocation());
	}

    public List<ShopUser> getMembers() {
        return members;
    }

	public static Shop loadShop(String serializedShopLocation) {
		ShopLocation sl = ShopLocation.deserialize(serializedShopLocation);
		JsonConfiguration json = new JsonConfiguration(sl.getLocation().getChunk());

		return json.loadShop(sl);
	}

    public void addManager(ShopUser newManager) {
        managers.add(newManager);
		saveShop();
    }

    public void removeManager(ShopUser oldManager) {
        managers.remove(oldManager);
		saveShop();
    }

    public void addMember(ShopUser newMember) {
        members.add(newMember);
		saveShop();
    }

	public List<UUID> getManagersUUID() {
        return managers.stream().map(ShopUser::getUUID).collect(Collectors.toList());
    }

	public List<UUID> getMembersUUID() {
        return members.stream().map(ShopUser::getUUID).collect(Collectors.toList());
    }

	private List<ShopUser> managersFromUUIDs(String... uuids) {
		List<ShopUser> managers = new ArrayList<>();
        for (String str : uuids) {
            managers.add(new ShopUser(Bukkit.getPlayer(UUID.fromString(str)), ShopRole.MANAGER));
        }

		return managers;
	}

	private List<ShopUser> membersFromUUIDs(String... uuids) {
		List<ShopUser> members = new ArrayList<>();
        for (String str : uuids) {
            members.add(new ShopUser(Bukkit.getPlayer(UUID.fromString(str)), ShopRole.MEMBER));
        }

		return members;
	}

	public Location getInventoryLocation() {
		return chestLoc.getLocation();
	}

	public void setChestLocation(Location newLoc) {
		chestLoc = new ShopLocation(newLoc);
	}

	public Location getShopLocation() {
		return shopLoc.getLocation();
	}

	public ShopType getShopType() {
		return shopType;
	}

	public void setBuyItem(ItemStack newItem) {
		buyItem = newItem;
		buyItemB64 = ItemSerializer.itemStackArrayToBase64(buyItem);
	}

	public ItemStack getBuyItem() {
		return buyItem;
	}

	public void setSellItem(ItemStack newItem) {
		sellItem = newItem;
		sellItemB64 = ItemSerializer.itemStackArrayToBase64(sellItem);
	}

	public ItemStack getSellItem() {
		return sellItem;
	}

	public void setShopType(ShopType newType) {
		shopType = newType;
	}

	private String serializeLocation(Location loc) {
		return new Gson().toJson(loc);
	}

	private static Location deserializeLocation(String loc) {
		return new Gson().fromJson(loc, Location.class);
	}

	public String serialize() {
		return new Gson().toJson(this);
	}

	public void itemsFromB64() {
		if (sellItemB64.length() > 0 && sellItem == null) {
			try {
				sellItem = ItemSerializer.itemStackArrayFromBase64(sellItemB64);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		if (buyItemB64.length() > 0 && buyItem == null) {
			try {
				buyItem = ItemSerializer.itemStackArrayFromBase64(buyItemB64);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public ShopLocation getShopLocationAsSL() {
		return shopLoc;
	}

	public ShopLocation getInventoryLocationAsSL() {
		return chestLoc;
	}

	public void fixAfterLoad() {
		itemsFromB64();
		shopLoc.stringToWorld();
		chestLoc.stringToWorld();
	}

	public static Shop loadShop(Sign s) {
		JsonConfiguration json = new JsonConfiguration(s.getLocation().getChunk());

		return json.loadShop(new ShopLocation(s.getLocation()));
	}

	public void removeMember(ShopUser oldMember) {
		members.remove(oldMember);
		saveShop();
	}

	public List<UUID> getUsersUUID() {
		return getUsers().stream().map(ShopUser::getUUID).collect(Collectors.toList());
	}

	public void saveShop() {
		JsonConfiguration json = new JsonConfiguration(shopLoc.getLocation().getChunk());

		json.saveShop(this);
	}

	public Sign getShopSign() {
		Block b = getShopLocation().getBlock();
		Sign s = null;

		if (ShopType.isShop(b)) {
			s = (Sign) b.getState();
		}
		return s;
	}

	public void updateSign() {
		Sign s = getShopSign();

		if (!missingItems()) {
			s.setLine(0, ChatColor.DARK_GREEN + shopType.toHeader());
		} else {
			s.setLine(0, ChatColor.GRAY + shopType.toHeader());
		}

		if (sellItem != null) {
			s.setLine(1, sellItem.getAmount() + " " + sellItem.getType());
		}

		if (buyItem != null) {
			s.setLine(2, buyItem.getAmount() + " " + buyItem.getType());
		}

		s.setLine(3, status.getLine());
		s.update();
	}

	public BlockState getChest() {
		try {
			return getInventoryLocation().getBlock().getState();
		} catch (NullPointerException npe) {
			return null;
		}
	}

	public ShopStatus getStatus() {
		return status;
	}

	public boolean setOpen() {
		boolean ret;

		if (!missingItems()) {
			status = ShopStatus.OPEN;
			ret = true;
		} else {
			status = ShopStatus.CLOSED;
			ret = false;
		}

		updateSign();
		return ret;
	}

	public boolean missingItems() {
		return sellItem == null || buyItem == null;
	}

	public void setClosed() {
		status = ShopStatus.CLOSED;
		updateSign();
	}

	public void remove() {
		JsonConfiguration json = new JsonConfiguration(shopLoc.getLocation().getChunk());

		json.removeShop(shopLoc);
	}

	public boolean isOpen() {
		return status == ShopStatus.OPEN;
	}

	public void switchType() {
		if (shopType == ShopType.TRADE)
			setShopType(ShopType.BITRADE);
		else if (shopType == ShopType.BITRADE)
			setShopType(ShopType.TRADE);

		updateSign();
		saveShop();
	}
}
