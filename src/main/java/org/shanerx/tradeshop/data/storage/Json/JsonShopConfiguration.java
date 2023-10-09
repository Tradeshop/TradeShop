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

package org.shanerx.tradeshop.data.storage.Json;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.scheduler.BukkitRunnable;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.data.storage.ShopConfiguration;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shoplocation.ShopChunk;
import org.shanerx.tradeshop.shoplocation.ShopLocation;
import org.shanerx.tradeshop.utils.gsonprocessing.GsonProcessor;
import org.shanerx.tradeshop.utils.objects.Tuple;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonShopConfiguration extends JsonConfiguration implements ShopConfiguration {

    private final ShopChunk chunk;

    public JsonShopConfiguration(ShopChunk chunk) {
        super(chunk.getWorldName(), chunk.serialize());
        this.chunk = chunk;
    }

    public static boolean doesConfigExist(ShopChunk chunk) {
        return getFile(chunk.getWorldName(), chunk.serialize()).exists();
    }

    @Override
    public void save(Shop shop) {
        jsonObj.add(shop.getShopLocationAsSL().serialize(), gson.toJsonTree(shop));

        saveFile();
    }

    @Override
    public void remove(ShopLocation loc) {
        if (jsonObj.has(loc.serialize()))
            jsonObj.remove(loc.serialize());
        saveFile();
    }

    @Override
    public Shop load(ShopLocation loc) {
        Shop shop = loadASync(loc);

        if (shop == null)
            return null;

        shop.fixAfterLoad();
        return shop;
    }

    @Override
    public Shop loadASync(ShopLocation loc) {
        String locStr = loc.serialize();
        Shop shop = null;

        if (!jsonObj.has(locStr)) return null;

        try {
            shop = gson.fromJson(jsonObj.get(locStr), Shop.class);
            shop.aSyncFix();
        } catch (IllegalArgumentException iAe) {
            iAe.printStackTrace();
            remove(loc);
        }

        return shop;
    }

    @Override
    public List<ShopLocation> list() {
        List<ShopLocation> shopsInFile = new ArrayList<>();
        jsonObj.keySet().forEach(str -> shopsInFile.add(ShopLocation.deserialize(str)));
        return shopsInFile;
    }

    @Override
    public int size() {
        return jsonObj.size();
    }

    @Override
    protected void saveFile() {
        SaveThreadMaster.getInstance().enqueue(this.file, this.jsonObj);
    }

    @Override
    protected void loadFile() {
        if (!this.file.exists()) {
            // If could not find file try with old separators
            String oldFile = file.getPath() + File.separator + chunk.serialize().replace(";;", "_") + ".json";
            if (new File(oldFile).exists())
                new File(oldFile).renameTo(file);
        }

        super.loadFile();

        for (Map.Entry<String, JsonElement> entry : Sets.newHashSet(jsonObj.entrySet())) {
            if (entry.getKey().contains("l_")) {
                jsonObj.add(ShopLocation.deserialize(entry.getKey()).serialize(), entry.getValue());
                jsonObj.remove(entry.getKey());
            }
        }
    }

    public static class SaveThreadMaster {
        private static SaveThreadMaster singleton;

        private Queue<Tuple<File, JsonObject>> saveQueue;
        private Set<BukkitRunnable> runningTasks;
        private Map<File, SaveTask> filesBeingSaved;

        private int maxThreads;
        private final GsonProcessor gson = new GsonProcessor();

        private SaveThreadMaster() {
            if (singleton != null) {
                throw new UnsupportedOperationException("Attempting to create further instance of singleton class SaveThreadMaster!");
            }

            saveQueue = new ConcurrentLinkedQueue<>();
            runningTasks = new HashSet<>();
            filesBeingSaved = new ConcurrentHashMap<>();
            maxThreads = Math.max(0, Setting.MAX_SAVE_THREADS.getInt());

            singleton = this;
        }

        public static SaveThreadMaster getInstance() {
            if (singleton == null) {
                return new SaveThreadMaster();
            }

            return singleton;
        }

        Queue<Tuple<File, JsonObject>> getSaveQueue() {
            return saveQueue;
        }

        private SaveTask makeRunnable() {
            return new SaveTask();
        }

        void enqueue(File file, JsonObject jsonObj) {
            if (maxThreads == 0) {
                saveQueue.add(new Tuple<>(file, jsonObj));
                makeRunnable().run();
                if (!saveQueue.isEmpty()) {
                    throw new IllegalStateException("saveQueue should be empty but has unsaved shop data: " + saveQueue.size());
                }
                return;
            } else if (filesBeingSaved.containsKey(file)) {
                SaveTask task = filesBeingSaved.get(file);
                if (runningTasks.contains(task)) {
                    task.enqueue(new Tuple<>(file, jsonObj));
                    return;
                }
                // fallthrough
            }

            saveQueue.add(new Tuple<>(file, jsonObj));
            if (runningTasks.size() < maxThreads) {
                makeRunnable().runTaskAsynchronously(TradeShop.getPlugin());
            }
        }

        public void saveEverythingNow() {
            if (saveQueue.isEmpty()) return;
            for (int i = 0; i < maxThreads; ++i) {
                makeRunnable().runTaskAsynchronously(TradeShop.getPlugin());
            }
        }
    }

    static class SaveTask extends BukkitRunnable {
        private SaveThreadMaster master;
        private Queue<Tuple<File, JsonObject>> ownQueue;
        private Set<File> ownFiles;

        SaveTask() {
            master = SaveThreadMaster.getInstance();
            ownQueue = new ConcurrentLinkedQueue<>();
            ownFiles = new HashSet<>();
        }

        private Tuple<File, JsonObject> pollNext() {
            if (!ownQueue.isEmpty()) return ownQueue.poll();
            else return master.getSaveQueue().poll();
        }

        void enqueue(Tuple<File, JsonObject> elem) {
            ownQueue.add(elem);
            ownFiles.add(elem.getLeft());
        }

        @Override
        public void run() {
            Logger logger = TradeShop.getPlugin().getLogger();
            Tuple<File, JsonObject> elem;

            while ((elem = pollNext()) != null) {
                File file = elem.getLeft();
                JsonObject jsonObj = elem.getRight();
                String str = master.gson.toJson(jsonObj);

                if (str.isEmpty() || jsonObj.entrySet().isEmpty()) {
                    file.delete();
                    continue;
                }

                try {
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(str);
                    fileWriter.flush();
                    fileWriter.close();
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Could not save " + file.getName() + " file! Data may be lost!", e);
                }
            }

            // task dies now:
            ownFiles.forEach(f -> master.filesBeingSaved.remove(f, this));
            ownFiles.clear();
        }

        @Override
        public int hashCode() {
            return master.maxThreads == 0 ? 0 : super.getTaskId();
        }
    }
}
