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

package org.shanerx.tradeshop.data.storage;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.shanerx.tradeshop.data.storage.Json.LinkageConfiguration;
import org.shanerx.tradeshop.data.storage.Json.PlayerConfiguration;
import org.shanerx.tradeshop.data.storage.Json.ShopConfiguration;
import org.shanerx.tradeshop.player.PlayerSetting;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shoplocation.ShopChunk;
import org.shanerx.tradeshop.shoplocation.ShopLocation;
import org.shanerx.tradeshop.utils.Utils;
import org.shanerx.tradeshop.utils.debug.DebugLevels;

import java.io.File;
import java.util.UUID;

public class DataStorage extends Utils {

    private transient DataType dataType;

    public DataStorage(DataType dataType) {
        reload(dataType);
    }

    public void reload(DataType dataType) {
        this.dataType = dataType;
        PLUGIN.getDebugger().log("Data storage set to: " + dataType.name(), DebugLevels.DISABLED);
    }

    public Shop loadShopFromSign(ShopLocation sign) {
        switch (dataType) {
            case FLATFILE:
                return new ShopConfiguration(new ShopChunk(sign.getChunk())).load(sign);
            case SQLITE:
                return null; //TODO add SQLITE support
        }
        return null;
    }

    public Shop loadShopFromStorage(ShopLocation chest) {
        switch (dataType) {
            case FLATFILE:
                return loadShopFromSign(new LinkageConfiguration(chest.getWorld()).getLinkedShop(chest));
            case SQLITE:
                return null; //TODO add SQLITE support
        }
        return null;
    }

    public void saveShop(Shop shop) {
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

    public PlayerSetting loadPlayer(UUID uuid) {
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
        return playerSetting != null ? playerSetting : new PlayerSetting(uuid);
    }

    public void savePlayer(PlayerSetting playerSetting) {
        switch (dataType) {
            case FLATFILE:
                new PlayerConfiguration(playerSetting.getUuid()).save(playerSetting);
                break;
            case SQLITE:
                //TODO add SQLITE support
                break;
        }
    }

    public void removePlayer(PlayerSetting playerSetting) {
        switch (dataType) {
            case FLATFILE:
                new PlayerConfiguration(playerSetting.getUuid()).remove();
                break;
            case SQLITE:
                //TODO add SQLITE support
                break;
        }
    }

    public ShopLocation getChestLinkage(ShopLocation chestLocation) {
        switch (dataType) {
            case FLATFILE:
                return new LinkageConfiguration(chestLocation.getWorld()).getLinkedShop(chestLocation);
            case SQLITE:
                //TODO add SQLITE support
                break;
        }

        return null;
    }

    public void addChestLinkage(ShopLocation chestLocation, ShopLocation shopLocation) {
        switch (dataType) {
            case FLATFILE:
                new LinkageConfiguration(chestLocation.getWorld()).add(chestLocation, shopLocation);
            case SQLITE:
                //TODO add SQLITE support
                break;
        }
    }

    public void removeChestLinkage(ShopLocation chestLocation) {
        switch (dataType) {
            case FLATFILE:
                new LinkageConfiguration(chestLocation.getWorld()).removeChest(chestLocation);
            case SQLITE:
                //TODO add SQLITE support
                break;
        }
    }
}