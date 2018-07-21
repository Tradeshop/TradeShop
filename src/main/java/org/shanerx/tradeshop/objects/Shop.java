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
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.enums.ShopRole;
import org.shanerx.tradeshop.enums.ShopType;
import org.shanerx.tradeshop.utils.JsonConfiguration;
import org.shanerx.tradeshop.utils.Tuple;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class Shop implements Serializable {

    private ShopUser owner;
    private List<ShopUser> managers, members;
	private ShopType shopType;
	private ShopLocation shopLoc, chestLoc;
	private transient ItemStack sellItem, buyItem;
	private Map<String, Object> sellItemMap, buyItemMap;

	public Shop(Tuple<Location, Location> locations, ShopType shopType, ShopUser owner, Tuple<List<ShopUser>, List<ShopUser>> players, Tuple<ItemStack, ItemStack> items) {
		shopLoc = new ShopLocation(locations.getLeft());
		this.owner = owner;
		chestLoc = new ShopLocation(locations.getRight());
		this.shopType = shopType;
		managers = players.getLeft();
		members = players.getRight();
		sellItem = items.getLeft();
		buyItem = items.getRight();

		sellItemMap = sellItem.serialize();
		buyItemMap = buyItem.serialize();
	}

	public Shop(Tuple<Location, Location> locations, ShopType shopType, ShopUser owner) {
		shopLoc = new ShopLocation(locations.getLeft());
		this.owner = owner;
		chestLoc = new ShopLocation(locations.getRight());
		this.shopType = shopType;
		managers = Collections.emptyList();
		members = Collections.emptyList();
		sellItemMap = Collections.emptyMap();
		buyItemMap = Collections.emptyMap();
	}

	public static Shop deserialize(String serialized) {
		Shop shop = new Gson().fromJson(serialized, Shop.class);
		shop.itemFromMap();

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

    public List<ShopUser> getMembers() {
        return members;
    }

    public void addManager(ShopUser newManager) {
        managers.add(newManager);
    }

    public void removeManager(ShopUser oldManager) {
        managers.remove(oldManager);
    }

    public void addMember(ShopUser newMember) {
        members.add(newMember);
    }

    public void removeMember(ShopUser oldMember) {
        members.remove(oldMember);
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
		buyItemMap = buyItem.serialize();
	}

	public ItemStack getBuyItem() {
		return buyItem;
	}

	public void setSellItem(ItemStack newItem) {
		sellItem = newItem;
		sellItemMap = sellItem.serialize();
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

	public void itemFromMap() {
		if (sellItemMap.size() > 0 && sellItem == null) {
			sellItem = ItemStack.deserialize(sellItemMap);
		}

		if (buyItemMap.size() > 0 && buyItem == null) {
			buyItem = ItemStack.deserialize(buyItemMap);
		}
	}

	public ShopLocation getShopLocationAsSL() {
		return shopLoc;
	}

	public ShopLocation getInventoryLocationAsSL() {
		return chestLoc;
	}

	public void fixAfterLoad() {
		itemFromMap();
		shopLoc.stringToWorld();
		chestLoc.stringToWorld();
	}

	public void updateSign() {

	}

	public void saveShop() {
		JsonConfiguration json = new JsonConfiguration(shopLoc.getLocation().getChunk());

		json.saveShop(this);
	}
}
