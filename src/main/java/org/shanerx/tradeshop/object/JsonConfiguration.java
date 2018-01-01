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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.shanerx.tradeshop.util.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;

@SuppressWarnings("unused")
public class JsonConfiguration extends Utils implements Serializable {
	private String pluginFolder;
	private File file;
	private File filePath;
	private Chunk chunk;
	private String div = "_";

	public JsonConfiguration(Chunk c) {
		this.chunk = c;
		this.pluginFolder = this.plugin.getDataFolder().getAbsolutePath();
		this.file = new File(this.pluginFolder + File.separator + "Data" + File.separator + chunk.getWorld() + File.separator + serializeChunk(chunk) + ".json");
		this.filePath = new File(this.pluginFolder + File.separator + "Data" + File.separator + chunk.getWorld());
		this.filePath.mkdirs();
		if (!this.file.exists()) {
			try {
				this.file.createNewFile();
			} catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		}
	}

	private void writeJSON(String path, String value) {
		JsonObject obj = new JsonObject();
		obj.add(path, new JsonParser().parse(value));
		try {
			FileWriter fileWriter = new FileWriter(this.file);
			fileWriter.write(obj.getAsString());
			fileWriter.flush();
			fileWriter.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private JsonObject readJSON(String path) {
		try {
			return new JsonParser().parse(new FileReader(file)).getAsJsonObject();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	// CAREFUL: DOES NOT OUTPUT JSON! USE ONLY FOR FILE NAMES!
	private String serializeChunk(Chunk chunk) {
		String world = chunk.getWorld().getName();
		int x = chunk.getX(), z = chunk.getZ();

		return "c" + div + world + div + x + div + z;
	}

	// CAREFUL: DOES NOT TAKE JSON AS PARAMETER! USE ONLY FOR FILE NAMES!
	private Chunk deserializeChunk(String loc) {
		if (loc.startsWith("c")) {
			String locA[] = loc.split(div);
			World world = Bukkit.getWorld(locA[1]);
			int x = Integer.parseInt(locA[2]), z = Integer.parseInt(locA[3]);

			return world.getChunkAt(x, z);
		}

		return null;
	}

	public String serializeLocation(Location loc) {
		return new Gson().toJson(loc);
	}

	public Location deserializeLocation(String loc) {
		return new Gson().fromJson(loc, Location.class);
	}
}
