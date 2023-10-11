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
import org.jetbrains.annotations.NotNull;
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
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
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

    public static class SaveOperation implements Comparable<SaveOperation> {

        private File file;
        private JsonObject jsonObj;
        private long time;

        SaveOperation(File file, JsonObject jsonObj) {
            this.file = file;
            this.jsonObj = jsonObj;
            this.time = System.currentTimeMillis();
        }

        @Override
        public int compareTo(@NotNull SaveOperation so) {
            try {
                if (Files.isSameFile(this.file.toPath(), so.file.toPath())) return 0;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (this.time <= so.time) return -1;
            return 1;
        }

        @Override
        public int hashCode() {
            return file.hashCode();
        }

        public File getFile() {
            return file;
        }

        public JsonObject getJson() {
            return jsonObj;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SaveOperation)) return false;
            return hashCode() == obj.hashCode();
        }
    }

    public static class SaveThreadMaster {
        private static SaveThreadMaster singleton;

        private ConcurrentSkipListSet<SaveOperation> saveQueue;
        private Set<SaveTask> runningTasks;

        private int maxThreads;
        private final GsonProcessor gson = new GsonProcessor();

        private SaveThreadMaster() {
            if (singleton != null) {
                throw new UnsupportedOperationException("Attempting to create further instance of singleton class SaveThreadMaster!");
            }

            saveQueue = new ConcurrentSkipListSet<>();
            runningTasks = Collections.newSetFromMap(new ConcurrentHashMap<SaveTask, Boolean>());
            maxThreads = Math.max(0, Setting.MAX_SAVE_THREADS.getInt());

            singleton = this;
        }

        public static SaveThreadMaster getInstance() {
            if (singleton == null) {
                return new SaveThreadMaster();
            }
            return singleton;
        }

        private SaveTask makeRunnable() {
            return new SaveTask();
        }

        synchronized void enqueue(File file, JsonObject jsonObj) {
            SaveOperation op = new SaveOperation(file, jsonObj);
            saveQueue.remove(op); // removes ops with a similar file
            saveQueue.add(op);

            if (maxThreads == 0) {
                makeRunnable().run();
            } else if (runningTasks.size() < maxThreads) {
                makeRunnable().runTaskAsynchronously(TradeShop.getPlugin());
            }
        }

        private synchronized SaveOperation pollNext() {
            return saveQueue.pollFirst();
        }

        public void saveEverythingNow() {
            if (saveQueue.isEmpty()) return;
            for (int i = runningTasks.size(); i < maxThreads; ++i) {
                makeRunnable().runTaskAsynchronously(TradeShop.getPlugin());
            }
        }
    }

    static class SaveTask extends BukkitRunnable {
        private SaveThreadMaster master;

        SaveTask() {
            master = SaveThreadMaster.getInstance();
        }

        @Override
        public void run() {
            master.runningTasks.add(this);

            Logger logger = TradeShop.getPlugin().getLogger();
            SaveOperation op;

            while ((op = master.pollNext()) != null) {
                File file = op.getFile();
                JsonObject jsonObj = op.getJson();
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
            master.runningTasks.remove(this);
        }

        @Override
        public int hashCode() {
            return master.maxThreads == 0 ? 0 : super.getTaskId();
        }
    }
}
