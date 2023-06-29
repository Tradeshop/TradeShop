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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
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
import org.shanerx.tradeshop.utils.debug.DebugLevels;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class DataStorage extends Utils {

    private transient DataType dataType;
    public final Map<File, String> saving;

    private final Cache<World, LinkageConfiguration> linkCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build();
    private final Cache<String, Shop> shopCache = CacheBuilder.newBuilder()
            .maximumSize(200)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build();
    private final Cache<UUID, PlayerSetting> playerCache = CacheBuilder.newBuilder()
            .maximumSize(50)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build();

    public DataStorage(DataType dataType) {
        saving = new HashMap<>();
        reload(dataType);
    }

    public void reload(DataType dataType) {
        this.dataType = dataType;
        if (!validate()) {
            TradeShop.getPlugin().getLogger().log(Level.SEVERE, "At least one err file(s) were found in the data folders! " +
                    "\n Please fix any error .json files, remove the .err files, and restart the plugin/server.");
            TradeShop.getPlugin().getServer().getPluginManager().disablePlugin(TradeShop.getPlugin());
        }
        //TradeShop.getPlugin().getDebugger().log("Data storage set to: " + dataType.name(), DebugLevels.DISABLED);
    }

    public boolean validate() {
        if (dataType == DataType.FLATFILE) {
            List<File> errFiles = new ArrayList<>();
            Bukkit.getServer().getWorlds().forEach((w) -> {
                File[] list = JsonLinkageConfiguration.getShopFiles(w.getName());
                if (list != null && list.length > 0) {
                    errFiles.addAll(Arrays.stream(list).filter((f) -> FilenameUtils.getExtension(f.getName()).toLowerCase().contains("err")).collect(Collectors.toList()));
                }
            });

            return errFiles.size() == 0;
        }
        throw new NotImplementedException("Data storage type " + dataType + " has not been implemented yet.");
    }

    public Shop loadShopFromSign(ShopLocation sign) {
        if (shopCache.getIfPresent(sign.serialize()) != null)
            return shopCache.getIfPresent(sign.serialize());
        return getShopConfiguration(sign.getChunk()).load(sign);
    }

    public Shop loadShopFromStorage(ShopLocation chest) {
        return loadShopFromSign(getLinkageConfiguration(chest.getWorld()).getLinkedShop(chest));
    }

    public void saveShop(Shop shop) {
        shopCache.put(shop.getShopLocationAsSL().serialize(), shop);
        Bukkit.getScheduler().runTaskAsynchronously(TradeShop.getPlugin(), () -> {
            getShopConfiguration(shop.getShopLocation().getChunk()).save(shop);
        });
    }

    public void removeShop(Shop shop) {
        Bukkit.getScheduler().runTaskAsynchronously(TradeShop.getPlugin(), () -> {
            shopCache.invalidate(shop.getShopLocationAsSL().serialize());
            getShopConfiguration(shop.getShopLocation().getChunk()).remove(shop.getShopLocationAsSL());
            getLinkageConfiguration(shop.getShopLocationAsSL().getWorld()).removeShop(shop.getShopLocationAsSL());
        });
    }

    public int getShopCountInChunk(Chunk chunk) {
        return getShopConfiguration(chunk).size();
    }

    public List<Shop> getMatchingShopsInChunk(ChunkSnapshot chunk, boolean inStock, List<ItemStack> desiredCosts, List<ItemStack> desiredProducts) {
        List<Shop> matchingShops = new ArrayList<>();
        ShopChunk shopChunk = new ShopChunk(chunk);

        if (chunkExists(shopChunk)) {
            ShopConfiguration config = getShopConfiguration(shopChunk);

            config.list().forEach(shopLoc -> matchingShops.add(config.loadASync(shopLoc))); //Load all shops and add to matchingShops

            if (desiredCosts != null && !desiredCosts.isEmpty())
                matchingShops.removeIf(shop -> shop.isMissingSideItems(ShopItemSide.COST, desiredCosts)); //Remove any shops that don't have a matching cost
            if (desiredProducts != null && !desiredProducts.isEmpty())
                matchingShops.removeIf(shop -> shop.isMissingSideItems(ShopItemSide.PRODUCT, desiredProducts)); //Remove any shops that don't have a matching product
            if (inStock)
                matchingShops.removeIf(shop -> shop.getAvailableTrades() == 0); //Remove any shops that can't make trades

            TradeShop.getPlugin().getDebugger().log(" --- _G_M_ --- " + Arrays.toString(matchingShops.stream().map(shop -> shop.getShopLocationAsSL().serialize()).toArray(String[]::new)), DebugLevels.DATA_ERROR);
        }
        return matchingShops;
    }

    public int getShopCountInWorld(World world) {
        String worldName = world.getName();

        AtomicInteger count = new AtomicInteger();
        Bukkit.getScheduler().runTaskAsynchronously(TradeShop.getPlugin(), () -> {
            switch (dataType) {
                case FLATFILE:
                    File folder = new File(TradeShop.getPlugin().getDataFolder().getAbsolutePath() + File.separator + "Data" + File.separator + worldName);
                    if (folder.exists() && folder.listFiles() != null) {
                        for (File file : folder.listFiles()) {
                            if (file.getName().contains(worldName))
                                count.addAndGet(new JsonShopConfiguration(ShopChunk.deserialize(file.getName().replace(".json", ""))).size());
                        }
                    }
                    break;
                case SQLITE:
                    //TODO add SQLITE support
                    throw new NotImplementedException("SQLITE for getShopCountInWorld has not been implemented yet.");
            }
        });
        return count.get();
    }

    public PlayerSetting loadPlayer(UUID uuid) {
        PlayerSetting playerSetting = playerCache.getIfPresent(uuid) != null ? playerCache.getIfPresent(uuid) : getPlayerConfiguration(uuid).load();

        //If playerSetting data not find create new and return
        return playerSetting != null ? playerSetting : new PlayerSetting(uuid);
    }

    public void savePlayer(PlayerSetting playerSetting) {
        playerCache.put(playerSetting.getUuid(), playerSetting);
        Bukkit.getScheduler().runTaskAsynchronously(TradeShop.getPlugin(), () -> {
            getPlayerConfiguration(playerSetting.getUuid()).save(playerSetting);
        });
    }

    public void removePlayer(PlayerSetting playerSetting) {
        Bukkit.getScheduler().runTaskAsynchronously(TradeShop.getPlugin(), () -> {
            playerCache.invalidate(playerSetting.getUuid());
            getPlayerConfiguration(playerSetting.getUuid()).remove();
        });
    }

    public ShopLocation getChestLinkage(ShopLocation chestLocation) {
        return getLinkageConfiguration(chestLocation.getWorld()).getLinkedShop(chestLocation);
    }

    public void addChestLinkage(ShopLocation chestLocation, ShopLocation shopLocation) {
        if (Bukkit.isPrimaryThread())
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

    protected boolean chunkExists(ShopChunk chunk) {
        if (dataType == DataType.FLATFILE) {
            return JsonShopConfiguration.doesConfigExist(chunk);
        }
        throw new NotImplementedException("Data storage type " + dataType + " has not been implemented yet.");
    }

    protected LinkageConfiguration getLinkageConfiguration(World w) {

        if (linkCache.getIfPresent(w) == null) {
            if (dataType == DataType.FLATFILE) {
                linkCache.put(w, new JsonLinkageConfiguration(w));
            }
        }

        if (linkCache.getIfPresent(w) != null) //Not else to catch the set if it was null
            return linkCache.getIfPresent(w);


        throw new NotImplementedException("Data storage type " + dataType + " has not been implemented yet.");
    }
}

