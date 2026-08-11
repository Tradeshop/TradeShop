/*
 *
 *                         Copyright (c) 2016-2023
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

package org.shanerx.tradeshop.utils.management;

import com.bergerkiller.bukkit.common.utils.ItemUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.item.IllegalItemList;
import org.shanerx.tradeshop.item.ShopItemSide;
import org.shanerx.tradeshop.shop.ShopStorage;
import org.shanerx.tradeshop.utils.debug.DebugLevels;
import org.shanerx.tradeshop.utils.objects.ObjectHolder;
import org.shanerx.tradeshop.utils.relativedirection.RelativeDirection;

import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class ListManager {

    private final IllegalItemList globalList = new IllegalItemList(IllegalItemList.ListType.DISABLED, new ArrayList<>());
    private final IllegalItemList costList = new IllegalItemList(IllegalItemList.ListType.DISABLED, new ArrayList<>());
    private final IllegalItemList productList = new IllegalItemList(IllegalItemList.ListType.DISABLED, new ArrayList<>());

    private final ArrayList<RelativeDirection> directions = new ArrayList<>();
    private final ArrayList<ShopStorage.Storages> inventories = new ArrayList<>();
    private final ArrayList<String> gameMats = new ArrayList<>();
    private final ArrayList<String> addOnMats = new ArrayList<>();

    private final HashMap<String, Integer> limitPermissions = new HashMap<>();

    private final TradeShop PLUGIN = TradeShop.getPlugin();

    private Cache<Location, Boolean> skippableHoppers, skippableShop;
    private Cache<UUID, Boolean> lockedPlayers;


    public ListManager() {
        reload();
        setGameMatList();
    }

    private void initSkip() {
        skippableHoppers = CacheBuilder.newBuilder()
                .maximumSize(100000)
                .expireAfterAccess(1000, TimeUnit.MILLISECONDS)
                .build();
        skippableShop = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(1500, TimeUnit.MILLISECONDS)
                .build();
    }

    private void initLocker() {
        lockedPlayers = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(Duration.ofMillis(500)).build();
    }

    public boolean lockPlayer(UUID player) {
        if (isPlayerLocked(player)) {
            return false;
        }

        lockedPlayers.put(player, true);
        return true;
    }

    public boolean isPlayerLocked(UUID player) {
        try {
            if (lockedPlayers.getIfPresent(player)) {
                return true;
            }
        } catch (NullPointerException ignored) {
        }

        return false;
    }

    /**
     * Check if a hopper has already been processed and stored in the cache
     *
     * @param location What location to check for
     * @return return null if not in cache(un-unprocessed) or true/false representing whether to block movement
     */
    public Boolean canSkipHopper(Location location) {
        if (skippableHoppers.getIfPresent(location) == null && skippableShop.getIfPresent(location) == null)
            return null;
        else if (skippableHoppers.getIfPresent(location) != null)
            return skippableHoppers.getIfPresent(location);
        else if (skippableShop.getIfPresent(location) != null)
            return skippableShop.getIfPresent(location);

        return null;
    }

    public void addSkippableHopper(Location location, boolean shouldBlock) {
        skippableHoppers.put(location, shouldBlock);
    }

    public void addSkippableShop(Location location, boolean shouldBlock) {
        skippableShop.put(location, shouldBlock);
    }

    public void removeSkippableShop(Location location) {
        skippableShop.invalidate(location);
    }


    public ArrayList<RelativeDirection> getDirections() {
        return directions;
    }

    public ArrayList<ShopStorage.Storages> getInventories() {
        return inventories;
    }

    public IllegalItemList getGlobalList() {
        return globalList;
    }

    public IllegalItemList getCostList() {
        return costList;
    }

    public IllegalItemList getProductList() {
        return productList;
    }

    public HashMap<String, Integer> getLimitPermissions() {
        return limitPermissions;
    }

    public ArrayList<String> getGameMats() {
        return gameMats;
    }

    public boolean isIllegal(ShopItemSide side, Material mat) {
        if (globalList.isIllegal(mat))
            return true;

        return side.equals(ShopItemSide.COST) ? costList.isIllegal(mat) : productList.isIllegal(mat);
    }

    public boolean isInventory(Block block) {
        //Get blocks Material and strip all non-alpha chars
        Material blockMaterial = block.getType();
        Boolean found = false;

        PLUGIN.getDebugger().log("isInventory Block Material: " + blockMaterial.name(), DebugLevels.LIST_MANAGER);

        //For each ShopStorage.Storages in inventories, check if their block list contains the block material. end loop if true.
        for (ShopStorage.Storages storage : inventories) {
            if (storage.getTypeMaterials().contains(blockMaterial)) {
                found = true;
                break;
            }
        }

        PLUGIN.getDebugger().log("isInventory Block Material found: " + found, DebugLevels.LIST_MANAGER);
        return found;
    }

    public void reload() {
        //Reloads and re-process any lists that can be
        updateIllegalLists();
        updateDirections();
        updateInventoryMats();
        setGameMatList();
        initSkip();
        initLocker();
        clearLimitPermissions();
        processLimitPermissions("tradeshop.limit", Setting.MAX_SHOPS_PER_PLAYER.getAsMap());
        registerLimitPermissions();
    }

    public void clearManager() {
        // Clears all lists, Only use if plugin is shutting down
        inventories.clear();
        globalList.clear();
        costList.clear();
        productList.clear();
        directions.clear();
        addOnMats.clear();
        gameMats.clear();
        clearLimitPermissions();
    }

    public void registerLimitPermissions() {
        unRegisterLimitPermissions(limitPermissions);

        PluginManager pm = Bukkit.getPluginManager();
        limitPermissions.forEach((k, v) -> {
            Permission perm = new Permission(k,
                    "Allows creation of " + (v == 0 ? "Zero(Probably an error?)" : v < 0 ? "Unlimited" : v) + " TradeShops.",
                    k.toLowerCase().endsWith("default") ? PermissionDefault.TRUE : PermissionDefault.FALSE);
            pm.addPermission(perm);
            PLUGIN.getDebugger().log("Permission registered: " + perm.getName() + " | State: " + perm.getDefault() + " | Description: " + perm.getDescription(), DebugLevels.STARTUP);
        });
    }

    public void processLimitPermissions(String preFix, Map<String, Object> limitMap) {
        for (Entry<String, Object> entry : limitMap.entrySet()) {
            ObjectHolder<?> vHolder = new ObjectHolder<>(entry.getValue());
            String perm = preFix + "." + entry.getKey();
            if (vHolder.getObject() == null) {
                return;
            }

            if (vHolder.isMap()) {
                processLimitPermissions(perm, vHolder.asMap());
            } else if (vHolder.canBeInteger()) {
                limitPermissions.put(perm, vHolder.asInteger());
            }
        }
    }

    public void clearLimitPermissions() {
        if (!limitPermissions.isEmpty()) {
            unRegisterLimitPermissions(limitPermissions);
            limitPermissions.clear();
        }
    }

    public void unRegisterLimitPermissions(HashMap<String, Integer> permissionList) {
        if (!permissionList.isEmpty()) {
            permissionList.keySet().forEach((k) -> Bukkit.getPluginManager().removePermission(k));
        }
    }

    private void updateIllegalLists() {
        //Clears list before regenerating
        globalList.clear();
        costList.clear();
        productList.clear();

        globalList.setType(Setting.GLOBAL_ILLEGAL_ITEMS_TYPE.getString());
        costList.setType(Setting.COST_ILLEGAL_ITEMS_TYPE.getString());
        productList.setType(Setting.PRODUCT_ILLEGAL_ITEMS_TYPE.getString());

        if (globalList.getType().equals(IllegalItemList.ListType.DISABLED))
            globalList.setType(IllegalItemList.ListType.BLACKLIST);

        PLUGIN.getDebugger().log("Loading GLOBAL Illegal Item List with mode:  " + globalList.getType(), DebugLevels.ILLEGAL_ITEMS_LIST);
        if (globalList.getType().equals(IllegalItemList.ListType.BLACKLIST)) {
            // Add non-removable blacklist items
            globalList.add(Material.AIR);
            globalList.add(Material.CAVE_AIR);
            globalList.add(Material.VOID_AIR);
        } else if (globalList.getType().equals(IllegalItemList.ListType.WHITELIST)) {
            globalList.remove(Material.AIR);
            globalList.remove(Material.CAVE_AIR);
            globalList.remove(Material.VOID_AIR);
        }

        for (String str : Setting.GLOBAL_ILLEGAL_ITEMS_LIST.getStringList()) {
            globalList.checkAndAdd(str);
        }

        PLUGIN.getDebugger().log("Loading COST Illegal Item List with mode:  " + costList.getType(), DebugLevels.ILLEGAL_ITEMS_LIST);
        for (String str : Setting.COST_ILLEGAL_ITEMS_LIST.getStringList()) {
            costList.checkAndAdd(str);
        }

        PLUGIN.getDebugger().log("Loading PRODUCT Illegal Item List with mode:  " + productList.getType(), DebugLevels.ILLEGAL_ITEMS_LIST);
        for (String str : Setting.PRODUCT_ILLEGAL_ITEMS_LIST.getStringList()) {
            productList.checkAndAdd(str);
        }
    }

    private void setGameMatList() {
        gameMats.clear();

        Set<Material> obtainable = obtainableMaterials();

        //Adds each Material from Minecraft that can exist as an item to a list for command tab complete
        for (Material mat : Material.values()) {
            // A shop trades ItemStacks, so a material with no item form cannot be
            // one side of a trade and has no business being offered as one.
            if (!obtainable.contains(mat)) {
                continue;
            }

            // Only add the material if it isn't BlackListed
            if (!globalList.isIllegal(mat)) {
                gameMats.add(mat.toString());
            }
        }

        //Adds any strings that have been added the AddOnMats list to the autocomplete list
        gameMats.addAll(addOnMats);
    }

    /**
     * Every material the running server has an item form for.
     *
     * <p>This used to be the complement of {@code NonObtainableMaterials}, a
     * sixty-nine constant blocklist written by hand. It was stale - its wall-sign
     * entries stopped at {@code WARPED_WALL_SIGN}, so every hanging sign added
     * since 1.20 and every wall sign added since 1.19 was still being offered as
     * something a shop could trade - and it was stale by construction, because a
     * list of what Minecraft has is a list that a Minecraft release invalidates.
     *
     * <p>{@link ItemUtil#getItemTypes()} walks the server's live item registry,
     * which is the authoritative answer and the reason the library dependency
     * exists. It reaches the server's internals to do it, so it cannot be class
     * -initialised where those internals are absent - under MockBukkit the first
     * touch throws {@code NoClassDefFoundError: Could not initialize class
     * com.bergerkiller.bukkit.common.Common}. {@link Material#isItem()} is
     * Bukkit's own answer to the same question and needs nothing bootstrapped,
     * so it is what the tier-1 suite sees.
     */
    private static Set<Material> obtainableMaterials() {
        Set<Material> obtainable = EnumSet.noneOf(Material.class);

        try {
            obtainable.addAll(ItemUtil.getItemTypes());
        } catch (Throwable notBootstrapped) {
            // An Error, not an Exception - see above. Falling through to Bukkit
            // rather than propagating, because a tab-complete list is not worth
            // refusing to enable over.
        }

        if (obtainable.isEmpty()) {
            for (Material mat : Material.values()) {
                // Legacy materials are the pre-1.13 name table. They are in the
                // enum, they are not in any registry, and the blocklist never
                // excluded them - so four hundred and sixty-three of them were
                // being offered for trade.
                if (!mat.isLegacy() && mat.isItem()) {
                    obtainable.add(mat);
                }
            }
        }

        return obtainable;
    }

    private void updateDirections() {
        directions.clear();

        for (String str : Setting.CHEST_DIRECTIONS.getStringList()) {
            try {
                directions.add(RelativeDirection.valueOf(str));
            } catch (IllegalArgumentException | NullPointerException ignored) {
            }
        }
    }

    private void updateInventoryMats() {
        //Clears the list before updating
        inventories.clear();

        PLUGIN.getDebugger().log("Inventory Materials from Config:", DebugLevels.STARTUP);
        PLUGIN.getDebugger().log("Config String | Status | Matching Type", DebugLevels.STARTUP);

        //For each String in the Allowed shops config setting, check if it is a valid inventory and add the ShopStorage.Storages object to the list
        for (String str : Setting.ALLOWED_SHOPS.getStringList()) {
            String logMsg = "- " + str;
            String storageName = PLUGIN.getStorages().isValidInventory(str);
            if (storageName.length() > 0) {
                ShopStorage.Storages storage = PLUGIN.getStorages().getValidInventory(storageName);
                inventories.add(storage);
                logMsg += " | Valid | " + storage.name();
            } else {
                logMsg += " | InValid";
            }

            PLUGIN.getDebugger().log(logMsg, DebugLevels.STARTUP);
        }
    }
}