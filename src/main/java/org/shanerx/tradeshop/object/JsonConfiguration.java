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
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.shanerx.tradeshop.util.Utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class JsonConfiguration
        extends Utils {
    String pluginFolder;
    File file;
    File filePath;
    Chunk chunk;
    String div = "_";

    public JsonConfiguration(Chunk c) {
        this.chunk = chunk;
        this.pluginFolder = this.plugin.getDataFolder().getAbsolutePath();
        this.file = new File(this.pluginFolder + File.separator + "Data" + File.separator + chunk.getWorld() + File.separator + serializeChunk(chunk) + ".json");
        this.filePath = new File(this.pluginFolder + File.separator + "Data" + File.separator + chunk.getWorld());
        this.filePath.mkdirs();
        if (!this.file.exists()) {
            try {
                this.file.createNewFile();
            } catch (Exception exception) {
                // empty catch block
            }
        }
    }

    private void writeJSON(String path, String value) {
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

    private String readJSON(String path) {
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

    public String serializeChunk(Chunk chunk) {
        String world = chunk.getWorld().getName();
        int x = chunk.getX(), z = chunk.getZ();

        return "c" + div + world + div + x + div + z;
    }

    public Chunk deserializeChunk(String loc) {
        if (loc.startsWith("c")) {
            String locA[] = loc.split(div);
            World world = Bukkit.getWorld(locA[1]);
            int x = Integer.parseInt(locA[2]), z = Integer.parseInt(locA[3]);

            return world.getChunkAt(x, z);
        }

        return null;
    }

    public String serializeLocation(Location loc) {
        String world = loc.getWorld().getName();
        int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();

        return "l" + div + world + div + x + div + y + div + z;
    }

    public Location deserializeLocation(String loc) {
        if (loc.startsWith("l")) {
            String locA[] = loc.split(div);
            World world = Bukkit.getWorld(locA[1]);
            int x = Integer.parseInt(locA[2]), y = Integer.parseInt(locA[3]), z = Integer.parseInt(locA[4]);

            return new Location(world, x, y, z);
        }

        return null;
    }
}
