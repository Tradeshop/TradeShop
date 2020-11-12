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

import org.bukkit.Chunk;
import org.bukkit.World;
import org.shanerx.tradeshop.enumys.DebugLevels;
import org.shanerx.tradeshop.objects.*;
import org.shanerx.tradeshop.utils.Utils;

import java.io.File;
import java.util.*;

public class DataStorage extends Utils {

    private transient DataType dataType;
    private final transient Map<World, Map<String, String>> chestLinkage = new HashMap<>();

    public DataStorage(DataType dataType) {
        reload(dataType);
    }

    public void reload(DataType dataType) {
        this.dataType = dataType;
        debugger.log("Data storage set to: " + dataType.name(), DebugLevels.DISABLED);
    }

    public Shop loadShopFromSign(ShopLocation sign) {
        switch (dataType) {
            case FLATFILE:
                return new JsonConfiguration(sign.getChunk()).loadShop(sign);
            case SQLITE:
                return null; //TODO add SQLITE support
        }
        return null;
    }

    public Shop loadShopFromStorage(ShopLocation chest) {
        switch (dataType) {
            case FLATFILE:
                return loadShopFromSign(getChestLinkage(chest));
            case SQLITE:
                return null; //TODO add SQLITE support
        }
        return null;
    }

    public void saveShop(Shop shop) {
        switch (dataType) {
            case FLATFILE:
                new JsonConfiguration(shop.getShopLocation().getChunk()).saveShop(shop);
                break;
            case SQLITE:
                //TODO add SQLITE support
                break;
        }
    }

    public void removeShop(Shop shop) {
        switch (dataType) {
            case FLATFILE:
                new JsonConfiguration(shop.getShopLocation().getChunk()).removeShop(shop.getShopLocationAsSL());
                break;
            case SQLITE:
                //TODO add SQLITE support
                break;
        }
    }

    public int getShopCountInChunk(Chunk chunk) {
        switch (dataType) {
            case FLATFILE:
                return new JsonConfiguration(chunk).getShopCount();
            case SQLITE:
                return 0; //TODO add SQLITE support
        }
        return 0;
    }

    public int getShopCountInWorld(World world) {
        int count = 0;
        switch (dataType) {
            case FLATFILE:
                for (File file : Objects.requireNonNull(new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Data" + File.separator + world.getName()).listFiles())) {
                    count += new JsonConfiguration(ShopChunk.deserialize(file.getName().replace(".json", ""))).getShopCount();
                }
                break;
            case SQLITE:
                //TODO add SQLITE support
                break;
        }
        return count;
    }

    public PlayerSetting loadPlayer(UUID uuid) {
        PlayerSetting playerSetting = null;
        switch (dataType) {
            case FLATFILE:
                playerSetting = new JsonConfiguration(uuid).loadPlayer();
                break;
            case SQLITE:
                //TODO add SQLITE support
                break;
        }

        //If playerSetting data not find create new and return
        return playerSetting != null ? playerSetting : new PlayerSetting(uuid);
    }

    public void savePlayer(PlayerSetting playerSetting) {
        switch (dataType) {
            case FLATFILE:
                new JsonConfiguration(playerSetting.getUuid()).savePlayer(playerSetting);
                break;
            case SQLITE:
                //TODO add SQLITE support
                break;
        }
    }

    public void removePlayer(PlayerSetting playerSetting) {
        switch (dataType) {
            case FLATFILE:
                new JsonConfiguration(playerSetting.getUuid()).removePlayer();
                break;
            case SQLITE:
                //TODO add SQLITE support
                break;
        }
    }

    private boolean loadChestLinkage(World world) {
        if (dataType != DataType.FLATFILE)
            return false;

        Map<String, String> loadedLinkage = new JsonConfiguration(world).loadChestLinkage();

        if (loadedLinkage != null) {
            chestLinkage.put(world, loadedLinkage);
            return true;
        } else {
            return false;
        }
    }

    public ShopLocation getChestLinkage(ShopLocation chestLocation) {
        switch (dataType) {
            case FLATFILE:
                if (loadChestLinkage(chestLocation.getWorld())
                        && chestLinkage.containsKey(chestLocation.getWorld())
                        && chestLinkage.get(chestLocation.getWorld()).containsKey(chestLocation.serialize())) {
                    return ShopLocation.deserialize(chestLinkage.get(chestLocation.getWorld()).get(chestLocation.serialize()));
                } else {
                    ShopChest shopChest = new ShopChest(chestLocation.getLocation());
                    if (shopChest.hasShopSign()) {
                        if (!chestLinkage.containsKey(chestLocation.getWorld())) {
                            chestLinkage.putIfAbsent(chestLocation.getWorld(),
                                    Collections.singletonMap(chestLocation.serialize(), shopChest.getShopSign().serialize()));
                        } else {
                            if (chestLinkage.get(chestLocation.getWorld()).containsKey(chestLocation.serialize())) {
                                chestLinkage.get(chestLocation.getWorld()).replace(chestLocation.serialize(), shopChest.getShopSign().serialize());
                            } else {
                                chestLinkage.get(chestLocation.getWorld()).put(chestLocation.serialize(), shopChest.getShopSign().serialize());
                            }
                        }
                        saveChestLinkages();
                    }
                    return shopChest.getShopSign();
                }
            case SQLITE:
                //TODO add SQLITE support
                break;
        }

        return null;
    }

    public void removeChestLinkage(ShopLocation chestLocation) {
        if (getChestLinkage(chestLocation) != null) {
            chestLinkage.get(chestLocation.getWorld()).remove(chestLocation.serialize());
            new JsonConfiguration(chestLocation.getWorld()).saveChestLinkage(chestLinkage.get(chestLocation.getWorld()));
        }
    }

    public void addChestLinkage(ShopLocation chestLocation, ShopLocation shopLocation) {
        if (chestLinkage.containsKey(chestLocation.getWorld())) {
            if (chestLinkage.get(chestLocation.getWorld()).containsKey(chestLocation.serialize()))
                chestLinkage.get(chestLocation.getWorld()).replace(chestLocation.serialize(), shopLocation.serialize());
            else
                chestLinkage.get(chestLocation.getWorld()).put(chestLocation.serialize(), shopLocation.serialize());
        } else {
            chestLinkage.put(chestLocation.getWorld(), Collections.singletonMap(chestLocation.serialize(), shopLocation.serialize()));
        }
        saveChestLinkages();
    }

    public void saveChestLinkages() {
        for (World world : chestLinkage.keySet()) {
            new JsonConfiguration(world).saveChestLinkage(chestLinkage.get(world));
        }
    }

    public void clearChestLinkages() {
        chestLinkage.clear();
    }


}