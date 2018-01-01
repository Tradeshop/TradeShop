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

package org.shanerx.tradeshop.enums;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.shanerx.tradeshop.TradeShop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public enum Setting {

	CHECK_UPDATES,
	ALLOWED_SHOPS,
	ALLOWED_DIRECTIONS,
	ITRADE_SHOP_NAME,
	ALLOW_DOUBLE_TRADE,
	ALLOW_QUAD_TRADE,
	MAX_EDIT_DISTANCE,
	MAX_SHOP_USERS,
	ILLEGAL_ITEMS,
	ALLOW_CUSTOM_ILLEGAL_ITEMS,
	TRADESHOP_NAME,
	ITRADESHOP_NAME,
	BITRADESHOP_NAME,
	SHOP_OPEN_STATUS,
	SHOP_CLOSED_STATUS,
	ALLOW_METRICS;

	@Override
	public String toString() {
		return name().toLowerCase().replace("_", "-");
	}

	public Object getSetting() {
		return config.get(toString());
	}

	public String getString() {
		return config.getString(toString());
	}

	public List<String> getStringList() {
		return config.getStringList(toString());
	}

	public int getInt() {
		return config.getInt(toString());
	}

	public double getDouble() {
		return config.getDouble(toString());
	}

	public boolean getBoolean() {
		return config.getBoolean(toString());
	}

	public static ArrayList<String> getItemBlackList() {
		ArrayList<String> blacklist = new ArrayList<>();
		blacklist.add("air");
		blacklist.addAll(Setting.ILLEGAL_ITEMS.getStringList());

		return blacklist;
	}

	public static Setting findSetting(String search) {
		search.toUpperCase().replace("-", "_");

		return valueOf(search);
	}

	private static TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
	private static File file = new File(plugin.getDataFolder(), "config.yml");
	private static FileConfiguration config = YamlConfiguration.loadConfiguration(file);

	private static void setDefaults() {
		addSetting("check-updates", true);
		addSetting("allowed-shops", new String[]{"CHEST", "TRAPPED_CHEST", "SHULKER"});
		addSetting("allowed-directions", new String[]{"DOWN", "WEST", "SOUTH", "EAST", "NORTH", "UP"});
		addSetting("itrade-shop-name", "Server Shop");
		addSetting("allow-double-trade", true);
		addSetting("allow-quad-trade", true);
		addSetting("max-edit-distance", 4);
		addSetting("max-shop-users", 5);
		addSetting("illegal-items", new String[]{"Bedrock", "Command_Block"});
		addSetting("allow-custom-illegal-items", true);
		addSetting("tradeshop-name", "Trade");
		addSetting("itradeshop-name", "iTrade");
		addSetting("bitradeshop-name", "BiTrade");
		addSetting("shop-open-status", "Open");
		addSetting("shop-closed-status", "Closed");
		addSetting("allow-metrics", true);

		save();
	}

	private static void addSetting(String node, Object value) {
		if (config.get(node) == null) {
			config.set(node, value);
		}
	}

	private static void save() {
		if (config != null)
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	public static void reload() {
		try {
			if (!plugin.getDataFolder().isDirectory()) {
				plugin.getDataFolder().mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			plugin.getLogger().log(Level.SEVERE, "Could not create Config file! Disabling plugin!", e);
			plugin.getServer().getPluginManager().disablePlugin(plugin);
		}

		config = YamlConfiguration.loadConfiguration(file);
		setDefaults();
	}

	public static FileConfiguration getConfig() {
		return config;
	}
}
