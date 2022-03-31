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
import org.shanerx.tradeshop.objects.PlayerSetting;
import org.shanerx.tradeshop.objects.Shop;
import org.shanerx.tradeshop.objects.ShopLocation;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DataCache {

    private transient Cache<ShopLocation, Shop> shopCache;
    private transient Cache<ShopLocation, ShopLocation> linkageCache;
    private transient Cache<UUID, PlayerSetting> playerCache;

    public DataCache() {
        buildCaches();
    }

    private void buildCaches() {
        shopCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build();
        linkageCache = CacheBuilder.newBuilder()
                .maximumSize(500)
                .expireAfterAccess(60, TimeUnit.MINUTES)
                .build();
        playerCache = CacheBuilder.newBuilder()
                .maximumSize(150)
                .expireAfterAccess(60, TimeUnit.MINUTES)
                .build();
    }

    protected void putInCache(CacheType cacheType, Object key, Object value) {
        if (key == null || value == null) {
            return;
        }

        switch (cacheType) {
            case SHOP:
                if (key instanceof ShopLocation && value instanceof Shop) {
                    shopCache.put((ShopLocation) key, (Shop) value);
                }
            case LINKAGE:
                if (key instanceof ShopLocation && value instanceof ShopLocation) {
                    linkageCache.put((ShopLocation) key, (ShopLocation) value);
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
            case SHOP:
                if (key instanceof ShopLocation) {
                    return shopCache.getIfPresent(key);
                }
            case LINKAGE:
                if (key instanceof ShopLocation) {
                    return linkageCache.getIfPresent(key);
                }
            case PLAYER:
                if (key instanceof UUID) {
                    return playerCache.getIfPresent(key);
                }
        }

        return null;
    }

    protected ShopLocation getLinkageFromCache(ShopLocation key) {
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
            case SHOP:
                if (key instanceof ShopLocation) {
                    shopCache.invalidate(key);
                }
            case LINKAGE:
                if (key instanceof ShopLocation) {
                    linkageCache.invalidate(key);
                }
            case PLAYER:
                if (key instanceof UUID) {
                    playerCache.invalidate(key);
                }
        }
    }

    protected void removeLinkagesForShop(ShopLocation valueLocation) {
        linkageCache.asMap().forEach((key, value) -> {
            if (value.equals(valueLocation))
                removeFromCache(CacheType.LINKAGE, key);
        });
    }

    protected void invalidateCaches() {
        shopCache.invalidateAll();
        linkageCache.invalidateAll();
        playerCache.invalidateAll();
    }

}
