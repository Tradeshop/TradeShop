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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.utils.Utils;
import org.shanerx.tradeshop.utils.debug.DebugLevels;
import org.shanerx.tradeshop.utils.gsonprocessing.GsonProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;

class JsonConfiguration extends Utils {
    protected final GsonProcessor gson;
    protected File file, pathFile;
    protected JsonObject jsonObj;

    protected final TradeShop PLUGIN = TradeShop.getPlugin();

    /**
     * Creates a JsonConfiguration object assisting with managing JSON data
     *
     * @param folderFromData Directory path of the file
     * @param fileName       name of the file to load without extension
     */
    protected JsonConfiguration(String folderFromData, String fileName) {
        this.gson = new GsonProcessor();
        this.pathFile = getPath(folderFromData);
        this.file = getFile(folderFromData, fileName);

        buildFilePath();
        loadFile();
    }

    public static File getFile(String folderFromData, String fileName) {
        return new File(getPath(folderFromData).getPath() + File.separator + fileName + ".json");
    }

    public static File getPath(String folderFromData) {
        return new File(TradeShop.getPlugin().getDataFolder().getAbsolutePath() + File.separator + "Data" + File.separator + folderFromData);
    }

    private void buildFilePath() {
        try {
            pathFile.mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            PLUGIN.getLogger().log(Level.SEVERE, "Could not create " + file.getName() + " file! Data may be lost!", e);
        }
    }

    public static void jsonSyntaxError(File file, Exception e) {
        String errStr = "";

        try {
            errStr = new String(Files.readAllBytes(file.toPath()));
        } catch (IOException ex) {
            TradeShop.getPlugin().getLogger().log(Level.SEVERE, "Could not read " + file.getName() + " file! ERR", e);
        }

        File err = new File(file.getPath() + File.separator + file.getName().replace(".json", ".err"));

        try {
            FileWriter fileWriter = new FileWriter(err);
            fileWriter.write(e.toString());
            fileWriter.append("\n\n--------------------------------------------------------------------------------------\n\n");
            fileWriter.append(errStr);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException ex) {
            TradeShop.getPlugin().getLogger().log(Level.SEVERE, "Could not save " + file.getName() + " file! Writing err to console.", e);
            TradeShop.getPlugin().getLogger().log(Level.SEVERE, e + "\n\n--------------------------------------------------------------------------------------\n\n" + errStr, e);
        }
    }

    public static File[] getFilesInFolder(String folderFromData) {
        File dir = getPath(folderFromData);
        TradeShop.getPlugin().getVarManager().getDebugger().log("reading file list from " + dir.getAbsolutePath(), DebugLevels.DATA_VERIFICATION);
        return dir.listFiles();
    }

    protected void loadFile() {
        try {
            jsonObj = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();

        } catch (FileNotFoundException e) {
            PLUGIN.getLogger().log(Level.SEVERE, "Could not load " + file.getName() + " file! Data may be lost!", e);
        } catch (IllegalStateException e) {
            jsonObj = new JsonObject();
        } catch (JsonSyntaxException e) {
            PLUGIN.getLogger().log(Level.SEVERE, "Could not load " + file.getName() + " file due to malformed Json! \n Please send the .err file with the same name to the TradeShop Devs. \n\nTradeShop will now disable, please remove/fix any err files before restarting the plugin.", e);

            jsonSyntaxError(file, e);
        }
    }

    protected void saveFile() {
        final String str = gson.toJson(jsonObj);
        if (!str.isEmpty()) {
            try {
                FileWriter fileWriter = new FileWriter(this.file);
                fileWriter.write(str);
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                PLUGIN.getLogger().log(Level.SEVERE, "Could not save " + this.file.getName() + " file! Data may be lost!", e);
            }
        }
        PLUGIN.getDataStorage().saving.remove(this.file);

    }
}
