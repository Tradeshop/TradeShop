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

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.shanerx.tradeshop.enumys.DebugLevels;
import org.shanerx.tradeshop.objects.PlayerSetting;
import org.shanerx.tradeshop.objects.Shop;
import org.shanerx.tradeshop.objects.ShopChunk;
import org.shanerx.tradeshop.objects.ShopLocation;
import org.shanerx.tradeshop.utils.Utils;
import org.shanerx.tradeshop.utils.data.Json.LinkageConfiguration;
import org.shanerx.tradeshop.utils.data.Json.PlayerConfiguration;
import org.shanerx.tradeshop.utils.data.Json.ShopConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DataStorage extends Utils {

    private transient DataType dataType;
    private transient DataCache dataCache;

    public DataStorage(DataType dataType) {
        reload(dataType);
    }

    public void reload(DataType dataType) {
        this.dataType = dataType;
        debugger.log("Data storage set to: " + dataType.name(), DebugLevels.DISABLED);

        if (dataCache != null) {
            dataCache.invalidateCaches();
        }

    }

    public Shop loadShopFromSign(ShopLocation sign) {
        // Cache Loading
        if (getDataCache().isInCache(CacheType.SHOP, sign)) {
            return getDataCache().getShopFromCache(sign);
        }
        Shop shop = null;

        if (getDataCache().isLocationShop(sign)) {
            // DataType specific loading
            switch (dataType) {
                case FLATFILE:
                    shop = new ShopConfiguration(new ShopChunk(sign.getChunk())).load(sign);
                    break;
                case SQLITE:
                    //TODO add SQLITE support
                    break;
            }

            if (shop != null) {
                getDataCache().putInCache(CacheType.SHOP, shop.getShopLocationAsSL(), shop);
            }
        }

        return shop;
    }

    public Shop loadShopFromStorage(ShopLocation chest) {
        // Cache Loading
        ShopLocation linkedShop = null;

        if (getDataCache().isInCache(CacheType.LINKAGE, chest)) {
            linkedShop = getDataCache().getLinkageFromCache(chest);
            if (getDataCache().isInCache(CacheType.SHOP, linkedShop)) {
                return getDataCache().getShopFromCache(getDataCache().getLinkageFromCache(linkedShop));
            }
        }

        Shop shop = null;
        if (linkedShop == null) {
            // DataType specific loading
            switch (dataType) {
                case FLATFILE:
                    linkedShop = new LinkageConfiguration(chest.getWorld()).getLinkedShop(chest);
                    break;
                case SQLITE:
                    //TODO add SQLITE support
                    break;
            }
        }

        shop = loadShopFromSign(linkedShop);


        if (shop != null) {
            getDataCache().putInCache(CacheType.SHOP, shop.getShopLocationAsSL(), shop);
        }

        return shop;
    }

    public void saveShop(Shop shop) {
        getDataCache().putInCache(CacheType.SHOP, shop.getShopLocationAsSL(), shop);

        switch (dataType) {
            case FLATFILE:
                new ShopConfiguration(new ShopChunk(shop.getShopLocation().getChunk())).save(shop);
                break;
            case SQLITE:
                //TODO add SQLITE support
                break;
        }
    }

    public void removeShop(Shop shop) {
        getDataCache().removeFromCache(CacheType.SHOP, shop.getShopLocationAsSL());
        getDataCache().removeLinkagesForShop(shop.getShopLocationAsSL());

        switch (dataType) {
            case FLATFILE:
                new ShopConfiguration(new ShopChunk(shop.getShopLocation().getChunk())).remove(shop.getShopLocationAsSL());
                new LinkageConfiguration(shop.getShopLocationAsSL().getWorld()).removeShop(shop.getShopLocationAsSL());
                break;
            case SQLITE:
                //TODO add SQLITE support
                break;
        }
    }

    public int getShopCountInChunk(Chunk chunk) {
        switch (dataType) {
            case FLATFILE:
                return new ShopConfiguration(new ShopChunk(chunk)).size();
            case SQLITE:
                return 0; //TODO add SQLITE support
        }
        return 0;
    }

    public int getShopCountInWorld(World world) {
        int count = 0;
        switch (dataType) {
            case FLATFILE:
                File folder = new File(PLUGIN.getDataFolder().getAbsolutePath() + File.separator + "Data" + File.separator + world.getName());
                if (folder.exists() && folder.listFiles() != null) {
                    for (File file : folder.listFiles()) {
                        if (file.getName().contains(world.getName()))
                            count += new ShopConfiguration(ShopChunk.deserialize(file.getName().replace(".json", ""))).size();
                    }
                }
                break;
            case SQLITE:
                //TODO add SQLITE support
                break;
        }
        return count;
    }

    public List<String> getAllShopLocations() {
        List<String> shops = new ArrayList<>();

        switch (dataType) {
            case FLATFILE:
                File folder = new File(PLUGIN.getDataFolder().getAbsolutePath() + File.separator + "Data");
                if (folder.exists() && folder.listFiles() != null) {
                    for (File file : folder.listFiles()) {
                        World world = Bukkit.getWorld(file.getName());
                        if (file.exists() && file.listFiles() != null && world != null) {
                            for (File file2 : file.listFiles()) {
                                if (file2.getName().contains(world.getName())) {
                                    ShopChunk sc = ShopChunk.deserialize(file2.getName().replace(".json", ""));
                                    if (sc != null) {
                                        new ShopConfiguration(sc).getShops().forEach((key) -> shops.add(key.serialize()));
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            case SQLITE:
                //TODO add SQLITE support
                break;
        }
        return shops;
    }

    public Map<String, String> getAllChestLinkages() {
        Map<String, String> allLinkageData = new HashMap<>();

        switch (dataType) {
            case FLATFILE:
                File folder = new File(PLUGIN.getDataFolder().getAbsolutePath() + File.separator + "Data");
                if (folder.exists() && folder.listFiles() != null) {
                    for (File file : folder.listFiles()) {
                        World world = Bukkit.getWorld(file.getName());
                        if (file.exists() && file.listFiles() != null && world != null) {
                            allLinkageData.putAll(new LinkageConfiguration(world).getLinkageData());
                        }
                    }
                }
                break;
            case SQLITE:
                //TODO add SQLITE support
                break;
        }
        return allLinkageData;
    }

    public PlayerSetting loadPlayer(UUID uuid) {
        // Cache Loading
        if (getDataCache().isInCache(CacheType.PLAYER, uuid)) {
            return getDataCache().getPlayerFromCache(uuid);
        }

        PlayerSetting playerSetting = null;

        switch (dataType) {
            case FLATFILE:
                playerSetting = new PlayerConfiguration(uuid).load();
                break;
            case SQLITE:
                //TODO add SQLITE support
                break;
        }

        //If playerSetting data not find create new and return
        if (playerSetting == null) {
            playerSetting = new PlayerSetting(uuid);
        }

        getDataCache().putInCache(CacheType.PLAYER, uuid, playerSetting);
        return playerSetting;
    }

    public void savePlayer(PlayerSetting playerSetting) {
        getDataCache().putInCache(CacheType.PLAYER, playerSetting.getUuid(), playerSetting);

        switch (dataType) {
            case FLATFILE:
                new PlayerConfiguration(playerSetting.getUuid()).save(playerSetting);
                break;
            case SQLITE:
                //TODO add SQLITE support
                break;
        }
    }

    public void removePlayer(UUID uuid) {
        getDataCache().removeFromCache(CacheType.PLAYER, uuid);

        switch (dataType) {
            case FLATFILE:
                new PlayerConfiguration(uuid).remove();
                break;
            case SQLITE:
                //TODO add SQLITE support
                break;
        }
    }

    public ShopLocation getChestLinkage(ShopLocation chestLocation) {
        ShopLocation shopLocation = null;

        // Cache Loading
        if (getDataCache().isInCache(CacheType.LINKAGE, chestLocation)) {
            shopLocation = getDataCache().getLinkageFromCache(chestLocation);
        }

        /*switch (dataType) {
            case FLATFILE:
                shopLocation = new LinkageConfiguration(chestLocation.getWorld()).getLinkedShop(chestLocation);
                break;
            case SQLITE:
                //TODO add SQLITE support
                break;
        }

        if (shopLocation != null) {
            getDataCache().putInCache(CacheType.LINKAGE, chestLocation, shopLocation);
        }*/

        return shopLocation;
    }

    public void addChestLinkage(ShopLocation chestLocation, ShopLocation shopLocation) {
        getDataCache().putInCache(CacheType.LINKAGE, chestLocation, shopLocation);

        switch (dataType) {
            case FLATFILE:
                new LinkageConfiguration(chestLocation.getWorld()).add(chestLocation, shopLocation);
            case SQLITE:
                //TODO add SQLITE support
                break;
        }
    }

    public void removeChestLinkage(ShopLocation chestLocation) {
        getDataCache().removeFromCache(CacheType.LINKAGE, chestLocation);

        switch (dataType) {
            case FLATFILE:
                new LinkageConfiguration(chestLocation.getWorld()).removeChest(chestLocation);
            case SQLITE:
                //TODO add SQLITE support
                break;
        }
    }

    public DataCache getDataCache() {
        if (dataCache == null) {
            dataCache = new DataCache(this);
        }

        return dataCache;
    }
}

