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

package org.shanerx.tradeshop.utils;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Chunk;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.objects.Shop;
import org.shanerx.tradeshop.objects.ShopChunk;
import org.shanerx.tradeshop.objects.ShopLocation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JsonConfiguration extends Utils implements Serializable {
	private String pluginFolder;
	private String path;
	private File file;
	private File filePath;
	private JsonObject jsonObj;
	private int configType;
	private Gson gson;

	public JsonConfiguration(Chunk c) {
		gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
		configType = 0;
		ShopChunk chunk = new ShopChunk(c);
		this.pluginFolder = plugin.getDataFolder().getAbsolutePath();
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

	public JsonConfiguration(UUID uuid) {
		gson = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().serializeNulls().create();
		configType = 1;
		this.pluginFolder = plugin.getDataFolder().getAbsolutePath();
		this.path = this.pluginFolder + File.separator + "Data" + File.separator + "Players";
		this.file = new File(path + File.separator + uuid.toString() + ".json");
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

	public void savePlayer(Map<String, Integer> data) {
		if (configType != 1)
			return;

		JsonElement obj = gson.toJsonTree(data);
		jsonObj.add("data", obj);

		saveContents(gson.toJson(jsonObj));
	}

	public void removePlayer() {
		if (configType != 1)
			return;

		file.delete();
	}

	public Map<String, Integer> loadPlayer() {
		if (configType != 1)
			return null;

		Gson gson = new Gson();
		Map<String, Integer> data;

		if (jsonObj.has("data")) {
			data = gson.fromJson(jsonObj.get("data"), new TypeToken<Map<String, Integer>>() {
			}.getType());
		} else {
			data = new HashMap<>();
		}

		return data;
	}

	public void saveShop(Shop shop) {
		if (configType != 0)
			return;

		jsonObj.add(shop.getShopLocationAsSL().serialize(), gson.toJsonTree(shop));

		saveContents(gson.toJson(jsonObj));
	}

	public void removeShop(ShopLocation loc) {
		if (configType != 0)
			return;

		if (jsonObj.has(loc.serialize())) {
			jsonObj.remove(loc.serialize());
		}

		saveContents(gson.toJson(jsonObj));
	}

	public Shop loadShop(ShopLocation loc) {
		if (configType != 0)
			return null;

		Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
		Shop shop;

		if (jsonObj.has(loc.serialize())) {
			if (jsonObj.getAsJsonObject(loc.serialize()).getAsJsonPrimitive("productB64") != null) {
				String str = jsonObj.getAsJsonObject(loc.serialize()).get("productB64").getAsString();
				jsonObj.getAsJsonObject(loc.serialize()).remove("productB64");
				jsonObj.getAsJsonObject(loc.serialize()).add("productListB64", gson.toJsonTree(b64OverstackFixer(str)));
				saveContents(gson.toJson(jsonObj));
			}

			if (jsonObj.getAsJsonObject(loc.serialize()).getAsJsonPrimitive("costB64") != null) {
				String str = jsonObj.getAsJsonObject(loc.serialize()).get("costB64").getAsString();
				jsonObj.getAsJsonObject(loc.serialize()).remove("costB64");
				jsonObj.getAsJsonObject(loc.serialize()).add("costListB64", gson.toJsonTree(b64OverstackFixer(str)));
				saveContents(gson.toJson(jsonObj));
			}
			shop = gson.fromJson(jsonObj.get(loc.serialize()), Shop.class);
		} else {
			return null;
		}

		shop.fixAfterLoad();
		return shop;
	}

	public int getShopCount() {
		return jsonObj.size();
	}

	private List<String> b64OverstackFixer(String oldB64) {
		ItemStack oldStack = null;
		if (oldB64.length() > 0) {
			try {
				oldStack = ItemSerializer.itemStackArrayFromBase64(oldB64);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		if (oldStack == null)
			return null;

		if (!(oldStack.getAmount() > oldStack.getMaxStackSize())) {
			return Lists.newArrayList(ItemSerializer.itemStackArrayToBase64(oldStack));
		} else {
			List<String> newStacks = new ArrayList<>();
			int amount = oldStack.getAmount();

			while (amount > 0) {
				if (oldStack.getMaxStackSize() < amount) {
					ItemStack itm = oldStack.clone();
					itm.setAmount(oldStack.getMaxStackSize());
					newStacks.add(ItemSerializer.itemStackArrayToBase64(itm));
					amount -= oldStack.getMaxStackSize();
				} else {
					ItemStack itm = oldStack.clone();
					itm.setAmount(amount);
					newStacks.add(ItemSerializer.itemStackArrayToBase64(itm));
					amount -= amount;
				}
			}

			return newStacks;
		}
	}
}
