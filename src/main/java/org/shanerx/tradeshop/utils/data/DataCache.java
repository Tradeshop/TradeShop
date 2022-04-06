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

package org.shanerx.tradeshop.utils.data;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Location;
import org.shanerx.tradeshop.enumys.DebugLevels;
import org.shanerx.tradeshop.objects.PlayerSetting;
import org.shanerx.tradeshop.objects.Shop;
import org.shanerx.tradeshop.objects.ShopLocation;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DataCache {

    private final transient DataStorage dataStorage;

    private transient List<String> shopLocations;
    private transient Map<String, String> linkageCache;

    private transient Cache<ShopLocation, Shop> shopCache;
    private transient Cache<UUID, PlayerSetting> playerCache;
    private transient Cache<Location, Boolean> skippableHoppers; //second data type doesn't matter and isn't used, boolean chosen as it is the smallest

    public DataCache(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
        buildCaches();
    }

    private void buildCaches() {
        reloadStaticCaches();

        shopCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build();
        playerCache = CacheBuilder.newBuilder()
                .maximumSize(150)
                .expireAfterAccess(60, TimeUnit.MINUTES)
                .build();
        skippableHoppers = CacheBuilder.newBuilder()
                .maximumSize(100000)
                .expireAfterAccess(3000, TimeUnit.MILLISECONDS)
                .build();
    }

    protected void putInCache(CacheType cacheType, Object key, Object value) {
        if (key == null) {
            return;
        }

        switch (cacheType) {
            case SHOP_LOCATION:
                if (key instanceof ShopLocation && value == null) {
                    shopLocations.add(((ShopLocation) key).serialize());
                }
            case SHOP:
                if (key instanceof ShopLocation && value instanceof Shop) {
                    shopCache.put((ShopLocation) key, (Shop) value);
                    shopLocations.add(((ShopLocation) key).serialize());
                }
            case LINKAGE:
                if (key instanceof ShopLocation && value instanceof ShopLocation) {
                    linkageCache.put(((ShopLocation) key).serialize(), ((ShopLocation) value).serialize());
                }
            case PLAYER:
                if (key instanceof UUID && value instanceof PlayerSetting) {
                    playerCache.put((UUID) key, (PlayerSetting) value);
                }
        }
    }

    protected Object getFromCache(CacheType cacheType, Object key) {
        if (key == null) {
            return null;
        }

        switch (cacheType) {
            case SHOP_LOCATION:
                if (key instanceof ShopLocation) {
                    return shopLocations.contains(((ShopLocation) key).serialize());
                }
            case SHOP:
                if (key instanceof ShopLocation) {
                    return shopCache.getIfPresent(key);
                }
            case LINKAGE:
                if (key instanceof ShopLocation) {
                    return linkageCache.get(((ShopLocation) key).serialize());
                }
            case PLAYER:
                if (key instanceof UUID) {
                    return playerCache.getIfPresent(key);
                }
        }

        return null;
    }

    public Boolean isLocationShop(ShopLocation key) {
        dataStorage.debugger.log("isLocationShop >" + key + "<: " + getFromCache(CacheType.SHOP_LOCATION, key), DebugLevels.STATIC_CACHING);
        return key == null ? null : (Boolean) getFromCache(CacheType.SHOP_LOCATION, key);
    }

    public ShopLocation getLinkageFromCache(ShopLocation key) {
        return key == null ? null : (ShopLocation) getFromCache(CacheType.LINKAGE, key);
    }

    protected Shop getShopFromCache(ShopLocation key) {
        return key == null ? null : (Shop) getFromCache(CacheType.SHOP, key);
    }

    protected PlayerSetting getPlayerFromCache(UUID key) {
        return key == null ? null : (PlayerSetting) getFromCache(CacheType.PLAYER, key);
    }

    protected boolean isInCache(CacheType cacheType, Object key) {
        if (key == null) {
            return false;
        }

        switch (cacheType) {
            case SHOP_LOCATION:
                if (key instanceof ShopLocation) {
                    return shopLocations.contains(((ShopLocation) key).serialize());
                }
            case SHOP:
                if (key instanceof ShopLocation) {
                    return getShopFromCache((ShopLocation) key) != null;
                }
            case LINKAGE:
                if (key instanceof ShopLocation) {
                    return getLinkageFromCache((ShopLocation) key) != null;
                }
            case PLAYER:
                if (key instanceof UUID) {
                    return getPlayerFromCache((UUID) key) != null;
                }
        }

        return false;
    }

    protected void removeFromCache(CacheType cacheType, Object key) {
        if (key == null) {
            return;
        }

        switch (cacheType) {
            case SHOP_LOCATION:
                if (key instanceof ShopLocation) {
                    shopLocations.remove(((ShopLocation) key).serialize());
                }
            case SHOP:
                if (key instanceof ShopLocation) {
                    shopCache.invalidate(key);
                    shopLocations.remove(((ShopLocation) key).serialize());
                }
            case LINKAGE:
                if (key instanceof ShopLocation) {
                    linkageCache.remove(((ShopLocation) key).serialize());
                }
            case PLAYER:
                if (key instanceof UUID) {
                    playerCache.invalidate(key);
                }
        }
    }

    protected void removeLinkagesForShop(ShopLocation valueLocation) {
        linkageCache.forEach((key, value) -> {
            if (value.equals(valueLocation.serialize()))
                removeFromCache(CacheType.LINKAGE, key);
        });
    }

    protected void invalidateCaches() {
        shopCache.invalidateAll();
        playerCache.invalidateAll();
        skippableHoppers.invalidateAll();

        reloadStaticCaches();
    }

    protected void reloadStaticCaches() {
        shopLocations = dataStorage.getAllShopLocations();
        linkageCache = dataStorage.getAllChestLinkages();

        dataStorage.debugger.log(("Shop Locations Cache: \n" + shopLocations.toString() + "\nLinkage Cache: \n" + linkageCache.toString())
                        .replace("[", "")
                        .replace("]", "")
                        .replace("{", "")
                        .replace("}", "")
                        .replace(", ", "\n"),
                DebugLevels.STATIC_CACHING);
    }

    public Boolean canSkipHopper(Location location) {
        if (skippableHoppers.getIfPresent(location) == null)
            return null;
        else
            return skippableHoppers.getIfPresent(location);
    }

    public void addSkippableHopper(Location location, boolean shouldBlock) {
        skippableHoppers.put(location, shouldBlock);
    }

}
