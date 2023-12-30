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

import de.leonhard.storage.Json;
import de.leonhard.storage.util.FileUtils;
import org.shanerx.tradeshop.TradeShop;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class JsonConfiguration extends Json {
    protected static final Map<String, JsonConfiguration> fileCache = new HashMap<>();

    protected final TradeShop PLUGIN = TradeShop.getPlugin();
    protected final String fileKey;

    /**
     * Creates a JsonConfiguration object assisting with managing JSON data
     *
     * @param folderFromData Directory path of the file
     * @param fileName       name of the file to load without extension
     */
    protected JsonConfiguration(String folderFromData, String fileName) {
        super(new File(getPath(folderFromData), fileName + ".json"));
        fileKey = folderFromData + ":" + fileName;

        loadFile();
    }

    public static JsonConfiguration getJsonConfiguration(String folderFromData, String fileName) {
        String key = folderFromData + ":" + fileName;

        if (fileCache.containsKey(key)) {
            return fileCache.get(key);
        }

        return new JsonConfiguration(folderFromData, fileName);
    }

    public static String getPath(String folderFromData) {
        return TradeShop.getPlugin().getDataFolder().getAbsolutePath() + File.separator + "Data" + File.separator + folderFromData;
    }

    public static List<File> getFilesInFolder(String folderFromData) {
        return FileUtils.listFiles(new File(getPath(folderFromData)), "json");
    }

    protected void loadFile() {
        forceReload();
        fileCache.put(fileKey, this);
    }

    protected void saveFile() {
        if (!file.exists()) create();
        try {
            write();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
