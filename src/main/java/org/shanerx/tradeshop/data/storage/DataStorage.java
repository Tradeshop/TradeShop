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
import org.apache.commons.io.FileUtils;
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
import org.shanerx.tradeshop.shop.ShopStatus;
import org.shanerx.tradeshop.shoplocation.ShopChunk;
import org.shanerx.tradeshop.shoplocation.ShopLocation;
import org.shanerx.tradeshop.utils.Utils;
import org.shanerx.tradeshop.utils.debug.DebugLevels;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class DataStorage extends Utils {

    private transient DataType dataType;
    private final String BROKEN_JSON_START = "}(.*[\"\\w:])";

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
        reload(dataType);
    }

    public void reload(DataType dataType) {
        this.dataType = dataType;
        TradeShop.getPlugin().getDebugger().log("Data storage set to: " + dataType.name(), DebugLevels.DATA_VERIFICATION);
        if (!validate()) {
            TradeShop.getPlugin().getLogger().log(Level.SEVERE, "Data could not be properly validated! \nAccessing these files could cause errors.");
        }
    }

    public boolean validate() {
        if (dataType == DataType.FLATFILE) {
            List<File> errFiles = new ArrayList<>();
            Map<File, String> correctedFiles = new HashMap<>();

            //Check for err files
            Bukkit.getServer().getWorlds().forEach((w) -> {
                File[] list = JsonShopConfiguration.getFilesInFolder(w.getName());
                if (list != null && list.length > 0) {
                    errFiles.addAll(Arrays.stream(list).filter((f) -> FilenameUtils.getExtension(f.getName()).toLowerCase().contains("err")).collect(Collectors.toList()));
                }
            });

            TradeShop.getPlugin().getDebugger().log("FLATFILE ERR Files Found: \n" + errFiles, DebugLevels.DATA_VERIFICATION);

            //Check for and correct malformed files
            Bukkit.getServer().getWorlds().forEach((w) -> {
                File[] list = JsonShopConfiguration.getFilesInFolder(w.getName());
                if (list != null && list.length > 0) {
                    Arrays.stream(list).forEach((f) -> {
                        try {
                            String fileStr = FileUtils.readFileToString(f, StandardCharsets.UTF_8),
                                    correctedString = fileStr.split(BROKEN_JSON_START)[0];
                            if (correctedString.length() < fileStr.length()) {
                                correctedFiles.put(f, correctedString);

                                TradeShop.getPlugin().getDebugger().log("Error found in file: " + f.getName() + "\n Text Removed: ---\n" + correctedString, DebugLevels.DATA_VERIFICATION);
                            }
                        } catch (IOException e) {
                            correctedFiles.put(f, null);
                        }
                    });
                }
            });

            //Write corrected malformed files
            if (correctedFiles.size() > 0) {
                correctedFiles.forEach((k, v) -> {
                    if (v != null && !v.isEmpty()) {
                        try {
                            FileWriter fileWriter = new FileWriter(k);
                            fileWriter.write(v);
                            fileWriter.flush();
                            fileWriter.close();
                        } catch (IOException e) {
                            TradeShop.getPlugin().getDebugger().log("Could not save corrected " + k.getName() + " file! Data may be lost!", DebugLevels.DATA_ERROR);
                        }
                    }
                });
            }


            TradeShop.getPlugin().getDebugger().log("Removing empty player files... ", DebugLevels.DATA_VERIFICATION);

            File[] playerFiles = JsonPlayerConfiguration.getAllPlayers();
            if (playerFiles != null) {
                List<String> deletedResults = new ArrayList<>();
                Map<String, Exception> failedResults = new HashMap<>();
                Arrays.stream(playerFiles).forEach((file) -> {
                    if (file.isFile() && file.length() == 0) {
                        try {
                            file.delete();
                            deletedResults.add(file.getName());
                        } catch (Exception e) {
                            failedResults.put(file.getName(), e);
                        }
                    }
                });

                if (deletedResults.size() != 0)
                    TradeShop.getPlugin().getDebugger().log("Empty files deleted: " + deletedResults.size(), DebugLevels.DATA_VERIFICATION);
                if (failedResults.size() != 0)
                    TradeShop.getPlugin().getDebugger().log("# of empty player files that couldn't be deleted: "
                            + failedResults.size()
                            + (failedResults.size() > 0 ? ("\nFailed Deletion results: \n" + failedResults.entrySet().stream().map((entry) -> entry.getKey() + ": " + entry.getValue().getMessage()).collect(Collectors.joining("\n"))) : "\n")
                            , DebugLevels.DATA_ERROR);
            }

            return errFiles.size() < 1;
        }
        throw new NotImplementedException("Data storage type " + dataType + " has not been implemented yet.");
    }

    public Shop loadShopFromSign(ShopLocation sign) {
        Shop cached = shopCache.getIfPresent(sign.serialize());
        return cached != null ? cached : getShopConfiguration(sign.getChunk()).load(sign);
    }

    public Shop loadShopFromStorage(ShopLocation chest) {
        return loadShopFromSign(getLinkageConfiguration(chest.getWorld()).getLinkedShop(chest));
    }

    public void saveShop(Shop shop) {
        shopCache.put(shop.getShopLocationAsSL().serialize(), shop);
        getShopConfiguration(shop.getShopLocation().getChunk()).save(shop);
    }

    public void removeShop(Shop shop) {
        shopCache.invalidate(shop.getShopLocationAsSL().serialize());
        getShopConfiguration(shop.getShopLocation().getChunk()).remove(shop.getShopLocationAsSL());
        getLinkageConfiguration(shop.getShopLocationAsSL().getWorld()).removeShop(shop.getShopLocationAsSL());
    }

    public int getShopCountInChunk(Chunk chunk) {
        return getShopConfiguration(chunk).size();
    }

    public List<Shop> getMatchingShopsInChunk(ChunkSnapshot chunk, boolean inStock, List<ItemStack> desiredCosts, List<ItemStack> desiredProducts) {
        List<Shop> matchingShops = new ArrayList<>();
        ShopChunk shopChunk = new ShopChunk(chunk);

        if (chunkExists(shopChunk)) {
            ShopConfiguration config = getShopConfiguration(shopChunk);

            config.list().forEach((shopLoc) -> {
                Shop shop = config.loadASync(shopLoc);

                if ((desiredCosts != null && shop.isMissingSideItems(ShopItemSide.COST, desiredCosts)) ||
                        (desiredProducts != null && shop.isMissingSideItems(ShopItemSide.PRODUCT, desiredProducts)))
                    return; //Ignore any shops that don't have a matching product/cost

                if (!inStock || shop.getStatus().equals(ShopStatus.OPEN)) {
                    matchingShops.add(shop);
                }
            });
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
        getPlayerConfiguration(playerSetting.getUuid()).save(playerSetting);
    }

    public void removePlayer(PlayerSetting playerSetting) {
        playerCache.invalidate(playerSetting.getUuid());
        getPlayerConfiguration(playerSetting.getUuid()).remove();
    }

    public ShopLocation getChestLinkage(ShopLocation chestLocation) {
        return getLinkageConfiguration(chestLocation.getWorld()).getLinkedShop(chestLocation);
    }

    public void addChestLinkage(ShopLocation chestLocation, ShopLocation shopLocation) {
        if (Bukkit.isPrimaryThread() && getChestLinkage(chestLocation) == null)
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

    public void ensureFinalSave() {
        // for onDisable !!!
        if (dataType == DataType.FLATFILE) {
            JsonShopConfiguration.SaveThreadMaster.getInstance().saveEverythingNow();
        }
        // SQLITE will have an analogous branch
    }
}

