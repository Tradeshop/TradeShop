/*
 *     Copyright (c) 2016-2017 SparklingComet @ http://shanerx.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * NOTICE: All modifications made by others to the source code belong
 * to the respective contributor. No contributor should be held liable for
 * any damages of any kind, whether be material or moral, which were
 * caused by their contribution(s) to the project. See the full License for more information
 */

package org.shanerx.tradeshop.object;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.shanerx.tradeshop.util.Utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class JsonConfiguration
        extends Utils {
    String pluginFolder;
    Location loc;
    File file;
    File filePath;
    String div = "_";

    public JsonConfiguration(Location l) {
        this.loc = l;
        this.pluginFolder = this.plugin.getDataFolder().getAbsolutePath();
        this.file = new File(this.pluginFolder + File.separator + "Data" + loc + ".json");
        this.filePath = new File(this.pluginFolder + File.separator + "Data");
        this.filePath.mkdirs();
        if (!this.file.exists()) {
            try {
                this.file.createNewFile();
            } catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public void writeJSON(String path, String value) {
        JSONObject jObj = new JSONObject();
        jObj.put(path, value);
        try {
            FileWriter fileWriter = new FileWriter(this.file);
            fileWriter.write(jObj.toJSONString());
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception fileWriter) {
            // empty catch block
        }
    }

    public String readJSON(String path) {
        String var = null;
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(this.file));
            JSONObject jsonObject = (JSONObject) obj;
            var = (String) jsonObject.get(path);
        } catch (Exception parser) {
            // empty catch block
        }
        return var;
    }

    public String serializeLocation(Location loc) {
        String world = loc.getWorld().getName();
        int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();

        return world + div + x + div + y + div + z;
    }

    public Location deserializeLocation(String loc) {
        String locA[] = loc.split(div);
        String world = locA[0];
        int x = Integer.parseInt(locA[1]), y = Integer.parseInt(locA[2]), z = Integer.parseInt(locA[3]);

        return new Location(Bukkit.getWorld(world), x, y, z);
    }
}
