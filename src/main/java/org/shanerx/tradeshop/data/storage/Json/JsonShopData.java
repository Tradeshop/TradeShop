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

import com.bergerkiller.bukkit.common.config.JsonSerializer;
import com.google.gson.JsonIOException;
import com.google.gson.stream.MalformedJsonException;
import org.apache.logging.log4j.util.Chars;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.data.storage.ShopConfiguration;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shoplocation.ShopChunk;
import org.shanerx.tradeshop.shoplocation.ShopLocation;
import org.shanerx.tradeshop.utils.debug.Debug;
import org.shanerx.tradeshop.utils.debug.DebugLevels;
import org.shanerx.tradeshop.utils.gsonprocessing.GsonProcessor;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class JsonShopData extends JsonConfiguration implements ShopConfiguration {

    private final ShopChunk chunk;
    private Map<String, Object> chunkMap;

    public JsonShopData(ShopChunk chunk) {
        super(chunk.getWorldName(), chunk.serialize());
        this.chunk = chunk;
    }

    public static boolean doesConfigExist(ShopChunk chunk) {
        return getFile(chunk.getWorldName(), chunk.serialize()).isFile();
    }

    @Override
    public void save(Shop shop) {
        chunkMap.put(shop.getShopLocationAsSL().serialize(), shop);
        saveFile();
    }

    @Override
    public void remove(ShopLocation loc) {
        chunkMap.remove(loc.serialize());
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
        Object obj = chunkMap.get(loc.serialize());

        return obj instanceof Shop ? (Shop) obj : null;
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
        SaveThreadMaster.getInstance().enqueue(this.file, this.chunkMap);
    }

    @Override
    protected void loadFile() {
        if (!this.file.isFile()) {
            // If could not find file try with old separators
            String oldFile = file.getPath() + File.separator + chunk.serialize().replace(";;", "_") + ".json";
            if (new File(oldFile).isFile()) new File(oldFile).renameTo(file);
        }

        try {
            JsonSerializer jsonSer = new JsonSerializer();
            chunkMap = jsonSer.jsonToMap(Arrays.toString(Files.readAllBytes(file.toPath())));

            for (Map.Entry<String, Object> entry : chunkMap.entrySet()) {
                if (entry.getKey().contains("l_")) {
                    chunkMap.put(ShopLocation.deserialize(entry.getKey()).serialize(), entry.getValue());
                    chunkMap.remove(entry.getKey());
                }

                if (!(entry.getValue() instanceof Shop)) {
                    Shop shop = (Shop) entry.getValue();
                    chunkMap.put(entry.getKey(), shop);
                }
            }
        } catch (JsonSerializer.JsonSyntaxException | IOException e) {
            chunkMap = new HashMap<>();
        }
    }

    public static class SaveOperation implements Comparable<SaveOperation> {

        private final File file;
        private final Map<String, Object> chunkMap;
        private final long time;

        SaveOperation(File file, Map<String, Object> chunkMap) {
            this.file = file;
            this.chunkMap = chunkMap;
            this.time = System.currentTimeMillis();
        }

        @Override
        public int compareTo(@NotNull SaveOperation so) {
            if (!this.file.exists() || (this.file.isFile() != so.file.isFile())) {
                return -1;
            }

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

        public Map<String, Object> getChunkMap() {
            return chunkMap;
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
        private final GsonProcessor gson = new GsonProcessor();

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

        synchronized void enqueue(File file, Map<String, Object> chunkMap) {
            SaveOperation op = new SaveOperation(file, chunkMap);
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

            Logger logger = TradeShop.getPlugin().getLogger();
            SaveOperation op;

            while ((op = master.pollNext()) != null) {
                File file = op.getFile(), bak = new File(file.getParentFile(), file.getName() + ".bak"), mjf = new File(file.getParentFile(), file.getName() + ".mjf");
                synchronized (file) {
                    Map<String, Object> chunkMap = op.getChunkMap();
                    String str = GsonProcessor.mapToJson(chunkMap);

                    if (str.isEmpty() || chunkMap.entrySet().isEmpty()) {
                        file.delete();
                        continue;
                    }

                    int expectedLength = str.getBytes(StandardCharsets.UTF_8).length;
                    Debug debug = Debug.findDebugger();

                    try {
                        debug.log(str, DebugLevels.JSON_SAVING, "JsonShopData^SaveThreadMaster#run().try-1 - " + file.getName());
                        if (file.exists()) {
                            bak.delete();
                            mjf.delete(); // Delete previous malformed and bak files to prevent conflicts with new ones. Could potentially rename with a counter if we really want to see a lot of malformed files.
                            file.renameTo(bak); // Create Backup Json file in case new write is bad.
                        }
                        int fileBytes = write(file, str);
                        write(mjf, str); // Create a Malformed Json File(MJF), this will be deleted if the saved file is verified
                        if (fileBytes != expectedLength) {
                            FileChannel chan = FileChannel.open(file.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE);
                            ByteBuffer byteBuff = ByteBuffer.allocate((int) chan.size());
                            chan.read(byteBuff);
                            ArrayList<Character> chars = new String(byteBuff.asCharBuffer().array()).chars().mapToObj(i -> (char) i).collect(Collectors.toCollection(ArrayList::new));
                            while (chars.contains('{') && chars.contains('}')) {
                                chars.set(chars.indexOf('{'), Chars.SPACE);
                                chars.set(chars.indexOf('}'), Chars.SPACE);
                            }
                            char c = chars.contains('{') ? '{' : '}';
                            while (chars.contains(c)) {
                                byteBuff.putChar(chars.indexOf(c), Chars.SPACE);
                            }

                            fileBytes = chan.write(byteBuff);
                            debug.log(new String(byteBuff.asCharBuffer().array()), DebugLevels.JSON_SAVING, "JsonShopData^SaveThreadMaster#run().try-1_post-verify - " + file.getName());
                            chan.force(true);
                            chan.close();

                            if (fileBytes != expectedLength)
                                throw new JsonIOException("Saved json could not be validated, please contact developers...");

                            throw new MalformedJsonException("Written length of file is not equal to expected length! File was fixed with a temporary solution, please notify the developers... \n Expected/Written = " + fileBytes + "/" + file.length());
                        }
                        mjf.delete();
                    } catch (MalformedJsonException mje) {
                        logger.log(Level.WARNING, mje.getMessage());
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "Could not save " + file.getName() + " file! Data may be lost!", e);
                    }

                }
            }

            // task dies now:
            master.runningTasks.remove(this);
        }

        private int write(File file, String str) throws IOException {
            FileChannel chan = FileChannel.open(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            int ret = chan.write(ByteBuffer.wrap(str.getBytes()));
            chan.force(true);
            chan.close();
            return ret;
        }

        @Override
        public int hashCode() {
            return master.maxThreads == 0 ? 0 : super.getTaskId();
        }
    }
}
