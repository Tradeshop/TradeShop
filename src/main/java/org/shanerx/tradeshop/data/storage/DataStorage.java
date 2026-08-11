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
import de.leonhard.storage.shaded.json.JSONException;
import de.leonhard.storage.shaded.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.data.storage.Json.JsonLinkageData;
import org.shanerx.tradeshop.data.storage.Json.JsonPlayerData;
import org.shanerx.tradeshop.data.storage.Json.JsonShopData;
import org.shanerx.tradeshop.item.ShopItemSide;
import org.shanerx.tradeshop.player.PlayerSetting;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shop.ShopStatus;
import org.shanerx.tradeshop.shoplocation.ShopChunk;
import org.shanerx.tradeshop.shoplocation.ShopLocation;
import org.shanerx.tradeshop.utils.debug.DebugLevels;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class DataStorage {

    private transient DataType dataType;

    /**
     * Where a file with junk appended after a complete shop object is cut.
     *
     * <p>Only ever applied to a file that has already been shown NOT to parse, and
     * only kept if the cut produces something that does. On its own this pattern
     * cannot tell a broken file from a sound one: a '}' followed by more content is
     * ordinary inside an item's data components, which embed JSON in a JSON string.
     */
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
                List<File> list = JsonShopData.getFilesInFolder(w.getName());
                if (!list.isEmpty()) {
                    errFiles.addAll(list.stream().filter((f) -> FilenameUtils.getExtension(f.getName()).toLowerCase().contains("err")).collect(Collectors.toList()));
                }
            });

            TradeShop.getPlugin().getDebugger().log("FLATFILE ERR Files Found: \n" + errFiles, DebugLevels.DATA_VERIFICATION);

            //Check for and correct malformed files
            Bukkit.getServer().getWorlds().forEach((w) -> {
                List<File> list = JsonShopData.getFilesInFolder(w.getName());
                if (!list.isEmpty()) {
                    list.forEach((f) -> {
                        try {
                            String fileStr = FileUtils.readFileToString(f, StandardCharsets.UTF_8);

                            // A file that parses is not broken, whatever it happens to
                            // contain. This test has to come first and has to be a parse:
                            // BROKEN_JSON_START looks for a '}' followed by more content,
                            // and since item data components an item's own JSON is embedded
                            // INSIDE a JSON string - "minecraft:custom_name":
                            // "{extra:[\"Kingsblade\"],text:\"\"}" - so that shape is now
                            // normal content. No regex can tell it apart from a real break;
                            // only a parser can.
                            if (parses(fileStr)) return;

                            String correctedString = fileStr.split(BROKEN_JSON_START)[0];

                            // Only rewrite when the repair actually produces a readable
                            // file. The truncation used to be written back unconditionally,
                            // which could leave a file just as broken as before with data
                            // additionally cut off the end.
                            if (correctedString.length() < fileStr.length() && parses(correctedString)) {
                                correctedFiles.put(f, correctedString);

                                TradeShop.getPlugin().getDebugger().log("Error found in file: " + f.getName() + "\n Text Removed: ---\n" + correctedString, DebugLevels.DATA_VERIFICATION);
                            } else {
                                TradeShop.getPlugin().getDebugger().log("File " + f.getName() + " is not readable JSON and could not be repaired; it has been left untouched rather than truncated.", DebugLevels.DATA_ERROR);
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

            List<File> playerFiles = JsonPlayerData.getAllPlayers();
            if (playerFiles != null) {
                List<String> deletedResults = new ArrayList<>();
                Map<String, Exception> failedResults = new HashMap<>();
                playerFiles.forEach((file) -> {
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

    /**
     * Whether {@code json} is a document the shop loader can actually read.
     *
     * <p>Deliberately the same parser the storage layer itself uses, so that a
     * "yes" here means "the loader will accept this" rather than "some parser
     * somewhere accepted it".
     */
    private boolean parses(String json) {
        try {
            new JSONObject(json);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    public Shop loadShopFromSign(ShopLocation sign) {
        if (sign == null) return null;
        Shop cached = shopCache.getIfPresent(sign.toString());
        if (cached != null) return cached;

        Shop loaded = getShopData(sign.getChunk()).load(sign);

        // One live object per shop, which is what the rest of the plugin assumes when
        // it loads a shop, changes it and saves it. Deserialization used to reach this
        // cache by saving the shop it was still building - the save is gone, so the
        // caching it was doing as a side effect is done here on purpose.
        if (loaded != null) shopCache.put(sign.toString(), loaded);

        return loaded;
    }

    public Shop loadShopFromStorage(ShopLocation chest) {
        return loadShopFromSign(getLinkageData(chest.getWorld()).getLinkedShop(chest));
    }

    public void saveShop(Shop shop) {
        shopCache.put(shop.getShopLocationAsSL().toString(), shop);
        getShopData(shop.getShopLocation().getChunk()).save(shop);
    }

    public void removeShop(Shop shop) {
        shopCache.invalidate(shop.getShopLocationAsSL().toString());
        getShopData(shop.getShopLocation().getChunk()).remove(shop.getShopLocationAsSL());
        getLinkageData(shop.getShopLocationAsSL().getWorld()).removeShop(shop.getShopLocationAsSL());
    }

    public int getShopCountInChunk(Chunk chunk) {
        return getShopData(chunk).size();
    }

    public List<Shop> getMatchingShopsInChunk(ChunkSnapshot chunk, boolean inStock, List<ItemStack> desiredCosts, List<ItemStack> desiredProducts) {
        List<Shop> matchingShops = new ArrayList<>();

        ShopConfiguration config = getShopData(new ShopChunk(chunk));

        config.list().forEach((shopLoc) -> {
            Shop shop = config.loadASync(shopLoc);

            if ((desiredCosts != null && shop.isMissingSideItems(ShopItemSide.COST, desiredCosts)) ||
                (desiredProducts != null && shop.isMissingSideItems(ShopItemSide.PRODUCT, desiredProducts)))
                return; //Ignore any shops that don't have a matching product/cost

            if (!inStock || shop.getStatus().equals(ShopStatus.OPEN)) {
                matchingShops.add(shop);
            }
        });

        return matchingShops;
    }

    /**
     * How many shops this world has on disk.
     *
     * <p>Counted on the calling thread and returned. It used to hand the counting to
     * {@code runTaskAsynchronously} and then return {@code count.get()} on the next
     * line, before the scheduler had given that task a thread - so the answer was
     * zero, every time, and the {@link AtomicInteger} the task later filled in was
     * never read by anybody. Its one caller is {@code VarManager.startup}, whose
     * total feeds the bStats "shop-counter" chart, so the figure this plugin has
     * been publishing about itself counted no shop that existed before the server
     * started.
     *
     * <p>The thread hop was worth keeping and has moved to that caller, which is
     * where a decision about blocking startup belongs: a method that says it returns
     * a count cannot also decide not to have one yet. Reading the world's name here
     * rather than inside a task keeps that call on the thread that owns the world.
     */
    public int getShopCountInWorld(World world) {
        String worldName = world.getName();

        if (dataType != DataType.FLATFILE) {
            //TODO add SQLITE support
            throw new NotImplementedException("Data storage type " + dataType + " for getShopCountInWorld has not been implemented yet.");
        }

        File folder = new File(TradeShop.getPlugin().getDataFolder().getAbsolutePath() + File.separator + "Data" + File.separator + worldName);
        File[] chunkFiles = folder.exists() ? folder.listFiles() : null;
        if (chunkFiles == null) return 0;

        int count = 0;
        for (File file : chunkFiles) {
            if (file.getName().contains(worldName) && file.getName().endsWith(".json"))
                count += new JsonShopData(ShopChunk.deserialize(file.getName().replace(".json", ""))).size();
        }

        return count;
    }

    public PlayerSetting loadPlayer(UUID uuid) {
        PlayerSetting playerSetting = playerCache.getIfPresent(uuid) != null ? playerCache.getIfPresent(uuid) : getPlayerData(uuid).load();

        //If playerSetting data not find create new and return
        return playerSetting != null ? playerSetting : new PlayerSetting(uuid);
    }

    public void savePlayer(PlayerSetting playerSetting) {
        playerCache.put(playerSetting.getUuid(), playerSetting);
        getPlayerData(playerSetting.getUuid()).save(playerSetting);
    }

    public void removePlayer(PlayerSetting playerSetting) {
        playerCache.invalidate(playerSetting.getUuid());
        getPlayerData(playerSetting.getUuid()).remove();
    }

    public ShopLocation getChestLinkage(ShopLocation chestLocation) {
        return getLinkageData(chestLocation.getWorld()).getLinkedShop(chestLocation);
    }

    public void addChestLinkage(ShopLocation chestLocation, ShopLocation shopLocation) {
        if (Bukkit.isPrimaryThread() && getChestLinkage(chestLocation) == null)
            getLinkageData(chestLocation.getWorld()).add(chestLocation, shopLocation);
    }

    public void removeChestLinkage(ShopLocation chestLocation) {
        getLinkageData(chestLocation.getWorld()).removeChest(chestLocation);
    }

    protected PlayerConfiguration getPlayerData(UUID uuid) {
        if (dataType == DataType.FLATFILE) {
            return new JsonPlayerData(uuid);
        }
        throw new NotImplementedException("Data storage type " + dataType + " has not been implemented yet.");
    }

    protected ShopConfiguration getShopData(Chunk chunk) {
        return getShopData(new ShopChunk(chunk));
    }

    private final Map<String, JsonShopData> chunkDataCache = new HashMap<>();

    /**
     * The one handle on a chunk's shops, cached per chunk.
     *
     * <p><b>The instance that is cached is the instance that is returned.</b> It
     * used to cache one and hand back a second, freshly constructed from the same
     * file, so the first caller after a cache miss worked on a copy nothing else
     * could see. Every write that caller made - a save, or a remove - landed on
     * disk and never on the cached object, and the next reader was answered out of
     * the cached object.
     *
     * <p>That is not theoretical and it is not only a wasted file read.
     * {@code ChunkUnloadListener} drops a chunk's entry on every unload, so any
     * shop operation is one unload away from being the first call after a miss. A
     * shop removed by that call is written out of its file and stays in memory,
     * where the next {@link #loadShopFromSign} finds it and hands it back alive -
     * and anything that then saves it writes it back to disk. Measured on Paper
     * 1.21.11 build 132 while proving that breaking a sign removes the shop stored
     * against it: the chunk's file was left holding {@code {}} and the shop still
     * loaded.
     */
    protected ShopConfiguration getShopData(ShopChunk chunk) {
        if (dataType == DataType.FLATFILE) {
            String serializedChunk = chunk.serialize();
            if (chunkDataCache.containsKey(serializedChunk))
                return chunkDataCache.get(serializedChunk);
            JsonShopData data = new JsonShopData(chunk);
            chunkDataCache.put(serializedChunk, data);
            return data;

        }

        throw new NotImplementedException("Data storage type " + dataType + " has not been implemented yet.");
    }

    public void dropShopData(ShopChunk chunk) {
        chunkDataCache.remove(chunk.serialize());
    }

    protected LinkageConfiguration getLinkageData(World w) {

        if (linkCache.getIfPresent(w) == null) {
            if (dataType == DataType.FLATFILE) {
                linkCache.put(w, new JsonLinkageData(w));
            }
        }

        if (linkCache.getIfPresent(w) != null) //Not else to catch the set if it was null
            return linkCache.getIfPresent(w);


        throw new NotImplementedException("Data storage type " + dataType + " has not been implemented yet.");
    }

    public void ensureFinalSave() {
        // for onDisable !!!
        /* WILL BE ADDED BACK IN LATER
        if (dataType == DataType.FLATFILE) {
            JsonShopData.SaveThreadMaster.getInstance().saveEverythingNow();
        }
        // SQLITE will have an analogous branch
        */
    }
}

