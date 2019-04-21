/*
 *                 Copyright (c) 2016-2017
 *         SparklingComet @ http://shanerx.org
 *      KillerOfPie @ http://killerofpie.github.io
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
 * caused by their contribution(s) to the project. See the full License for more information.
 */

package org.shanerx.tradeshop.objects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.shanerx.tradeshop.TradeShop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class CustomItemManager {

	private Map<String, ItemStack> customItems = new HashMap<>();
	private static final TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
	private static File file = new File(plugin.getDataFolder(), "customitems.yml");
	private static FileConfiguration config = YamlConfiguration.loadConfiguration(file);
	private static final char COLOUR_CHAR = '&';

	public CustomItemManager() {
		loadItems();
	}

	private void loadItems() {
		for (String key : config.getKeys(false)) {
			if (config.get(key) != null) {
				customItems.put(key, loadItem(key));
			}
		}

		if (customItems.isEmpty()) {
			addDefault();
		}
	}

	public void clearManager() {
		save();
		customItems.clear();
	}

	private ItemStack loadItem(String name) {
		ItemStack itm = ItemStack.deserialize(config.getConfigurationSection(name).getValues(true));
		ItemMeta meta = itm.getItemMeta();

		if (meta.hasLore()) {
			ArrayList<String> str2 = new ArrayList<>();
			for (String s : meta.getLore()) {
				str2.add(ChatColor.translateAlternateColorCodes('&', s));
			}
			meta.setLore(str2);
		}

		if (meta.hasDisplayName())
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', meta.getDisplayName()));

		itm.setItemMeta(meta);

		return itm;
	}

	private void addDefault() {
		if (customItems.isEmpty()) {
			ItemStack dataHolder = new ItemStack(Material.TRIPWIRE_HOOK);
			ItemMeta meta = dataHolder.getItemMeta();

			meta.setDisplayName("Key");
			meta.setLore(Collections.singletonList("&aThe key to your dreams."));
			dataHolder.setItemMeta(meta);

			addItem("Key", dataHolder);
		}
	}

	public ItemStack getItem(String key) {
		if (customItems.containsKey(key)) {
			return customItems.get(key);
		}

		return null;
	}

	public void addItem(String key, ItemStack itm) {
		if (customItems.containsKey(key)) {
			customItems.replace(key, itm);
		} else {
			customItems.put(key, itm);
		}
	}

	public void removeItem(String key) {
		customItems.remove(key);

		if (config.contains(key)) {
			config.set(key, null);
		}
	}

	public Set<String> getItems() {
		return customItems.keySet();
	}

	public void reload() {
		try {
			if (!plugin.getDataFolder().isDirectory() && plugin.getDataFolder().mkdirs()
					|| !file.exists() && file.createNewFile());
			else throw new IOException();

		} catch (IOException e) {
			plugin.getLogger().log(Level.SEVERE, "Could not create Config file! Disabling plugin!", e);
			plugin.getServer().getPluginManager().disablePlugin(plugin);
		}

		config = YamlConfiguration.loadConfiguration(file);
		customItems.clear();
		loadItems();
	}

	private void save() {
		for (String str : customItems.keySet()) {
			if (config.contains(str)) {
				config.set(str, customItems.get(str).serialize());
			} else {
				config.createSection(str);
				config.set(str, customItems.get(str).serialize());
			}
		}

		if (config != null) {
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		config = YamlConfiguration.loadConfiguration(file);
		customItems.clear();
		loadItems();
	}

	public FileConfiguration getConfig() {
		return config;
	}

}
