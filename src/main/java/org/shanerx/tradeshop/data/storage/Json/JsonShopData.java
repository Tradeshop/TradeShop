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

import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.data.storage.ShopConfiguration;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shoplocation.ShopChunk;
import org.shanerx.tradeshop.shoplocation.ShopLocation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class JsonShopData extends JsonConfiguration implements ShopConfiguration {

    private final ShopChunk chunk;
    private Map<String, Object> chunkMap;

    public JsonShopData(ShopChunk chunk) {
        super(chunk.getWorldName(), chunk.serialize());
        this.chunk = chunk;
    }


    @Override
    public void save(Shop shop) {
        set(shop.getShopLocationAsSL().toString(), shop.serialize());
        saveFile();
    }

    @Override
    public void remove(ShopLocation loc) {
        remove(loc.toString());
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
        String locStr = loc.toString();
        if (!fileData.containsKey(locStr))
            return null;

        Shop shop = Shop.deserialize(getSection(locStr));

        shop.aSyncFix();
        return shop;
    }

    @Override
    public List<ShopLocation> list() {
        List<ShopLocation> shopsInFile = new ArrayList<>();
        keySet().forEach(str -> shopsInFile.add(ShopLocation.deserialize(str)));
        return shopsInFile;
    }

    @Override
    public int size() {
        return keySet().size();
    }

    @Override
    protected void saveFile() {
        SaveThreadMaster.getInstance().enqueue(this);
    }

    @Override
    protected void loadFile() {

        if (!this.file.isFile()) {
            // If could not find file try with old separators
            String oldFile = file.getPath() + File.separator + chunk.serialize().replace(";;", "_") + ".json";
            if (new File(oldFile).isFile()) new File(oldFile).renameTo(file);
        }

        String data = "";

        try {
            JsonSerializer jsonSer = new JsonSerializer();
            data = Arrays.toString(Files.readAllBytes(file.toPath()));
            chunkMap = jsonSer.jsonToMap(data);

            for (Map.Entry<String, Object> entry : chunkMap.entrySet()) {
                if (entry.getKey().contains("l_")) {
                    Debug.findDebugger().log("Found old shop location format, converting...", DebugLevels.JSON_LOADING, "JsonShopData#loadFile()-forKeys-contans{l_} - " + file.getName());
                    chunkMap.put(ShopLocation.deserialize(entry.getKey()).serialize(), entry.getValue()); //de -> re serialization performs reformatting
                    chunkMap.remove(entry.getKey());
                }

        try {
            for (Map.Entry<String, Object> entry : readToMap().entrySet()) {
                if (entry.getKey().contains("l_")) {
                    set(ShopLocation.deserialize(entry.getKey()).toString(), entry.getValue());
                    remove(entry.getKey());
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static class SaveOperation implements Comparable<SaveOperation> {
      
        @Getter
        private final JsonConfiguration jsonConfig;
        private final long time;

        SaveOperation(JsonConfiguration jsonConfig) {
            this.jsonConfig = jsonConfig;
            this.time = System.currentTimeMillis();
        }

        @Override
        public int compareTo(@NotNull SaveOperation so) {
            if (!this.file.exists() || (this.file.isFile() != so.file.isFile())) {
                return -1;
            }

            try {
                if (Files.isSameFile(this.jsonConfig.getFile().toPath(), so.jsonConfig.getFile().toPath())) return 0;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (this.time <= so.time) return -1;
            return 1;
        }

        @Override
        public int hashCode() {
            return jsonConfig.getFile().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SaveOperation)) return false;
            return hashCode() == obj.hashCode();
        }
    }

    public static class SaveThreadMaster {
        private static SaveThreadMaster singleton;

        private final ConcurrentSkipListSet<SaveOperation> saveQueue;
        private final Set<SaveTask> runningTasks;

        private final int maxThreads;

        private SaveThreadMaster() {
            if (singleton != null) {
                throw new UnsupportedOperationException("Attempting to create further instance of singleton class SaveThreadMaster!");
            }

            saveQueue = new ConcurrentSkipListSet<>();
            runningTasks = Collections.newSetFromMap(new ConcurrentHashMap<>());
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


        synchronized void enqueue(JsonConfiguration jsonConfig) {
            SaveOperation op = new SaveOperation(jsonConfig);
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
        private final SaveThreadMaster master;

        SaveTask() {
            master = SaveThreadMaster.getInstance();
        }

        @Override
        public void run() {
            master.runningTasks.add(this);
            SaveOperation op;

            while ((op = master.pollNext()) != null) {
                File file = op.jsonConfig.getFile();
                synchronized (file) {
                    if (op.jsonConfig.keySet().isEmpty()) {
                        file.delete();
                        continue;
                    }

                    op.jsonConfig.saveFile();
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
