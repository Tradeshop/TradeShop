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

package org.shanerx.tradeshop.object;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.enums.ShopRole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Shop {

    User owner;
    ArrayList<User> managers = new ArrayList<>();
    ArrayList<User> members = new ArrayList<>();
    Material inventoryMat;
    Location inventoryLoc, shopLoc;
    ItemStack sellItem, buyItem;

    public Shop(Location shopLocation, Location invLoc, Material invMat, User owner, List<User> managers, List<User> members, ItemStack sellItem, ItemStack buyItem) {
        shopLoc = shopLocation;
        this.owner = owner;
        inventoryLoc = invLoc;
        inventoryMat = invMat;
        this.managers.addAll(managers);
        this.members.addAll(members);
    }

    public Shop() {

    }

    public Location getInventoryLoc() {
        return inventoryLoc;
    }

    public void setInventoryLoc(Location newLoc) {
        inventoryLoc = newLoc;
    }

    public void setInventoryMat(Material newMat) {
        inventoryMat = newMat;
    }

    public Location getShopLoc() {
        return shopLoc;
    }

    public Material getInventoryMat() {
        return inventoryMat;
    }

    public ItemStack getBuyItem() {
        return buyItem;
    }

    public ItemStack getSellItem() {
        return sellItem;
    }

    public void setSellItem(ItemStack newItem) {
        sellItem = newItem;
    }

    public void setBuyItem(ItemStack newItem) {
        buyItem = newItem;
    }

    public void addManager(User newManager) {
        managers.add(newManager);
    }

    public void removeManager(User oldManager) {
        managers.remove(oldManager);
    }

    public void addMember(User newMember) {
        members.add(newMember);
    }

    public void removeMember(User oldMember) {
        members.remove(oldMember);
    }

    public String[] getManagersUUIDs() {
        String[] uuids = new String[managers.size()];
        for (User user : managers) {
            uuids[managers.indexOf(user)] = user.getUUID().toString();
        }

        return uuids;
    }

    public String[] getMembersUUIDs() {
        String[] uuids = new String[members.size()];
        for (User user : members) {
            uuids[members.indexOf(user)] = user.getUUID().toString();
        }

        return uuids;
    }

    private static ArrayList<User> managersFromUUIDs(String[] uuids) {
        ArrayList<User> managers = new ArrayList<>();
        for (String str : uuids) {
            managers.add(new User(Bukkit.getPlayer(UUID.fromString(str)), ShopRole.MANAGER));
        }

        return managers;
    }

    private static ArrayList<User> membersFromUUIDs(String[] uuids) {
        ArrayList<User> members = new ArrayList<>();
        for (String str : uuids) {
            members.add(new User(Bukkit.getPlayer(UUID.fromString(str)), ShopRole.MEMBER));
        }

        return members;
    }

    private String serializeLocation(Location loc) {
        String div = "_";
        String world = loc.getWorld().getName();
        int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();

        return "l" + div + world + div + x + div + y + div + z;
    }

    private static Location deserializeLocation(String loc) {
        String div = "_";
        if (loc.startsWith("l")) {
            String locA[] = loc.split(div);
            World world = Bukkit.getWorld(locA[1]);
            int x = Integer.parseInt(locA[2]), y = Integer.parseInt(locA[3]), z = Integer.parseInt(locA[4]);

            return new Location(world, x, y, z);
        }

        return null;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();

        map.put("location", serializeLocation(shopLoc));
        map.put("inventory", inventoryMat.toString());
        map.put("inventory-location", serializeLocation(inventoryLoc));
        map.put("owner", owner.getUUID().toString());
        map.put("managers", getManagersUUIDs());
        map.put("members", getMembersUUIDs());
        map.put("sell-item", sellItem.serialize());
        map.put("buy-item", buyItem.serialize());

        return map;
    }

    public static Shop deserialize(Map<String, Object> map) {
        Location shopLoc = deserializeLocation((String) map.get("location")),
                invLoc = deserializeLocation((String) map.get("inventory-location"));
        Material invMat = Material.valueOf(map.get("inventory").toString());
        User owner = new User(Bukkit.getPlayer(UUID.fromString(map.get("owner").toString())), ShopRole.OWNER);
        ArrayList<User> managers = managersFromUUIDs((String[]) map.get("managers"));
        ArrayList<User> members = membersFromUUIDs((String[]) map.get("members"));

        return new Shop(shopLoc, invLoc, invMat, owner, managers, members,
                ItemStack.deserialize((Map<String, Object>) map.get("sell-item")),
                ItemStack.deserialize((Map<String, Object>) map.get("buy-item")));
    }
}
