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

package org.shanerx.tradeshop.data.storage;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.data.storage.Json.JsonLinkageConfiguration;
import org.shanerx.tradeshop.data.storage.Json.JsonPlayerConfiguration;
import org.shanerx.tradeshop.data.storage.Json.JsonShopConfiguration;
import org.shanerx.tradeshop.item.ShopItemSide;
import org.shanerx.tradeshop.player.PlayerSetting;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shoplocation.ShopChunk;
import org.shanerx.tradeshop.shoplocation.ShopLocation;
import org.shanerx.tradeshop.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataStorage extends Utils {

    private transient DataType dataType;

    public DataStorage(DataType dataType) {
        reload(dataType);
    }

    public void reload(DataType dataType) {
        this.dataType = dataType;
        //TradeShop.getPlugin().getDebugger().log("Data storage set to: " + dataType.name(), DebugLevels.DISABLED);
    }

    public Shop loadShopFromSign(ShopLocation sign) {
        return getShopConfiguration(sign.getChunk()).load(sign);
    }

    public Shop loadShopFromStorage(ShopLocation chest) {
        return loadShopFromSign(getLinkageConfiguration(chest.getWorld()).getLinkedShop(chest));
    }

    public void saveShop(Shop shop) {
        getShopConfiguration(shop.getShopLocation().getChunk()).save(shop);
    }

    public void removeShop(Shop shop) {
        getShopConfiguration(shop.getShopLocation().getChunk()).remove(shop.getShopLocationAsSL());
        getLinkageConfiguration(shop.getShopLocationAsSL().getWorld()).removeShop(shop.getShopLocationAsSL());
    }

    public int getShopCountInChunk(Chunk chunk) {
        return getShopConfiguration(chunk).size();
    }

    public List<Shop> getMatchingShopsInChunk(Chunk chunk, List<ItemStack> desiredCosts, List<ItemStack> desiredProducts) {
        List<Shop> matchingShops = new ArrayList<>();
        ShopConfiguration config = getShopConfiguration(chunk);

        config.list().forEach(shopLoc -> matchingShops.add(config.load(shopLoc))); //Load all shops and add to matchingShops

        matchingShops.removeIf(shop ->
                !shop.containsSideItems(ShopItemSide.COST, desiredCosts) ||
                        !shop.containsSideItems(ShopItemSide.PRODUCT, desiredProducts)); //Remove any shops that don't have a matching  cost or product.

        return matchingShops;
    }

    public int getShopCountInWorld(World world) {
        int count = 0;
        switch (dataType) {
            case FLATFILE:
                File folder = new File(TradeShop.getPlugin().getDataFolder().getAbsolutePath() + File.separator + "Data" + File.separator + world.getName());
                if (folder.exists() && folder.listFiles() != null) {
                    for (File file : folder.listFiles()) {
                        if (file.getName().contains(world.getName()))
                            count += new JsonShopConfiguration(ShopChunk.deserialize(file.getName().replace(".json", ""))).size();
                    }
                }
                break;
            case SQLITE:
                //TODO add SQLITE support
                throw new NotImplementedException("SQLITE for getShopCountInWorld has not been implemented yet.");
        }
        return count;
    }

    public PlayerSetting loadPlayer(UUID uuid) {
        PlayerSetting playerSetting = getPlayerConfiguration(uuid).load();

        //If playerSetting data not find create new and return
        return playerSetting != null ? playerSetting : new PlayerSetting(uuid);
    }

    public void savePlayer(PlayerSetting playerSetting) {
        getPlayerConfiguration(playerSetting.getUuid()).save(playerSetting);
    }

    public void removePlayer(PlayerSetting playerSetting) {
        getPlayerConfiguration(playerSetting.getUuid()).remove();
    }

    public ShopLocation getChestLinkage(ShopLocation chestLocation) {
        return getLinkageConfiguration(chestLocation.getWorld()).getLinkedShop(chestLocation);
    }

    public void addChestLinkage(ShopLocation chestLocation, ShopLocation shopLocation) {
        getLinkageConfiguration(chestLocation.getWorld()).add(chestLocation, shopLocation);
    }

    public void removeChestLinkage(ShopLocation chestLocation) {
        getLinkageConfiguration(chestLocation.getWorld()).removeChest(chestLocation);
    }

    protected PlayerConfiguration getPlayerConfiguration(UUID uuid) {
        if (dataType == DataType.FLATFILE) {
            return new JsonPlayerConfiguration(uuid);
        }
        throw new NotImplementedException("Data storage type " + dataType + " has not been implemented yet.");
    }

    protected ShopConfiguration getShopConfiguration(Chunk chunk) {
        return getShopConfiguration(new ShopChunk(chunk));
    }

    protected ShopConfiguration getShopConfiguration(ShopChunk chunk) {
        if (dataType == DataType.FLATFILE) {
            return new JsonShopConfiguration(chunk);
        }
        throw new NotImplementedException("Data storage type " + dataType + " has not been implemented yet.");
    }

    protected LinkageConfiguration getLinkageConfiguration(World w) {
        if (dataType == DataType.FLATFILE) {
            return new JsonLinkageConfiguration(w);
        }
        throw new NotImplementedException("Data storage type " + dataType + " has not been implemented yet.");
    }
}

