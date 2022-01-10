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

package org.shanerx.tradeshop.utils.data.Json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.shanerx.tradeshop.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

class JsonConfiguration extends Utils {
    protected final String path;
    protected final Gson gson;
    protected File file, pathFile;
    protected JsonObject jsonObj;

    /**
     * Creates a JsonConfiguration object assisting with managing JSON data
     *
     * @param folderFromData Directory path of the file
     * @param fileName       name of the file to load without extension
     */
    protected JsonConfiguration(String folderFromData, String fileName) {
        this.gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        this.path = PLUGIN.getDataFolder().getAbsolutePath() + File.separator + "Data" + File.separator + folderFromData;
        this.pathFile = new File(path);
        this.file = new File(path + File.separator + fileName + ".json");

        buildFilePath();
        loadFile();
    }

    private void buildFilePath() {
        try {
            pathFile.mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            PLUGIN.getLogger().log(Level.SEVERE, "Could not create " + file.getName() + " file! Data may be lost!", e);
        }
    }

    protected void loadFile() {
        try {
            jsonObj = new JsonParser().parse(new FileReader(file)).getAsJsonObject();
        } catch (FileNotFoundException e) {
            PLUGIN.getLogger().log(Level.SEVERE, "Could not load " + file.getName() + " file! Data may be lost!", e);
        } catch (IllegalStateException e) {
            jsonObj = new JsonObject();
        }
    }

    protected void saveFile() {
        String str = gson.toJson(jsonObj);
        if (!str.isEmpty()) {
            try {
                FileWriter fileWriter = new FileWriter(this.file);
                fileWriter.write(str);
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                PLUGIN.getLogger().log(Level.SEVERE, "Could not save " + file.getName() + " file! Data may be lost!", e);
            }
        }
    }
}
