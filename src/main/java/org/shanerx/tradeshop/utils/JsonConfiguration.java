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

package org.shanerx.tradeshop.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Chunk;
import org.shanerx.tradeshop.objects.Shop;
import org.shanerx.tradeshop.objects.ShopChunk;
import org.shanerx.tradeshop.objects.ShopLocation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;

@SuppressWarnings("unused")
public class JsonConfiguration extends Utils implements Serializable {
	private String pluginFolder;
	private String path;
	private File file;
	private File filePath;
	private ShopChunk chunk;
	private String div = "_";
	private JsonObject jsonObj;
	private Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

	public JsonConfiguration(Chunk c) {
		this.chunk = new ShopChunk(c);
		this.pluginFolder = this.plugin.getDataFolder().getAbsolutePath();
		this.path = this.pluginFolder + File.separator + "Data" + File.separator + chunk.getWorld().getName();
		this.file = new File(path + File.separator + chunk.serialize() + ".json");
		this.filePath = new File(path);
		this.filePath.mkdirs();
		if (!this.file.exists()) {
			try {
				this.file.createNewFile();
			} catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		}

		loadContents();
	}

	private void loadContents() {
		try {
			jsonObj = new JsonParser().parse(new FileReader(file)).getAsJsonObject();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IllegalStateException e) {
			jsonObj = new JsonObject();
		}
	}

	private void saveContents(String str) {
		try {
			FileWriter fileWriter = new FileWriter(this.file);
			fileWriter.write(str);
			fileWriter.flush();
			fileWriter.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		loadContents();
	}

	public void saveShop(Shop shop) {
		String sl = shop.getShopLocationAsSL().serialize();
		JsonElement obj = gson.toJsonTree(shop);
		jsonObj.add(sl, obj);

		saveContents(gson.toJson(jsonObj));
	}

	public void removeShop(ShopLocation loc) {
		if (jsonObj.has(loc.serialize())) {
			jsonObj.remove(loc.serialize());
		}

		saveContents(gson.toJson(jsonObj));
	}

	public Shop loadShop(ShopLocation loc) {
		Gson gson = new Gson();
		Shop shop;

		if (jsonObj.has(loc.serialize())) {
			shop = gson.fromJson(jsonObj.get(loc.serialize()), Shop.class);
		} else {
			return null;
		}

		shop.fixAfterLoad();
		return shop;
	}
}
