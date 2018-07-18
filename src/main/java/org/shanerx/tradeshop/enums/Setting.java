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

	CHECK_UPDATES("check-updates"),
	ALLOWED_SHOPS("allowed-shops"),
	ALLOWED_DIRECTIONS("allowed-directions"),
	ITRADESHOP_OWNER("tradeshop.owner"),
	ALLOW_DOUBLE_TRADE("allow-double-trade"),
	ALLOW_QUAD_TRADE("allow-quad-trade"),
	MAX_EDIT_DISTANCE("max-edit-distance"),
	MAX_SHOP_USERS("max-shop-users"),
	ILLEGAL_ITEMS("illegal-items"),
	ALLOW_CUSTOM_ILLEGAL_ITEMS("allow-custom-illegal-items"),
	TRADESHOP_HEADER("tradeshop.header"),
	ITRADESHOP_HEADER("tradeshop.header"),
	BITRADESHOP_HEADER("tradeshop.header"),
	TRADESHOP_EXPLODE("tradeshop.allow-explode"),
	ITRADESHOP_EXPLODE("itradeshop.allow-explode"),
	BITRADESHOP_EXPLODE("bitradeshop.allow-explode"),
	SHOP_OPEN_STATUS("shop-open-status"),
	SHOP_CLOSED_STATUS("shop-closed-status"),
	ALLOW_METRICS("allow-metrics");

	String path;

	Setting(String path) {
		this.path = path;
	}

	public String toPath() {
		return path;
	}

	public Object getSetting() {
		return config.get(toPath());
	}

	public String getString() {
		return config.getString(toPath());
	}

	public List<String> getStringList() {
		return config.getStringList(toPath());
	}

	public int getInt() {
		return config.getInt(toPath());
	}

	public double getDouble() {
		return config.getDouble(toPath());
	}

	public boolean getBoolean() {
		return config.getBoolean(toPath());
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
		addSetting(CHECK_UPDATES.path, true);
		addSetting(ALLOWED_SHOPS.path, new String[]{"CHEST", "TRAPPED_CHEST", "SHULKER"});
		addSetting(ALLOWED_DIRECTIONS.path, new String[]{"DOWN", "WEST", "SOUTH", "EAST", "NORTH", "UP"});
		addSetting(ALLOW_DOUBLE_TRADE.path, true);
		addSetting(ALLOW_QUAD_TRADE.path, true);
		addSetting(MAX_EDIT_DISTANCE.path, 4);
		addSetting(MAX_SHOP_USERS.path, 5);
		addSetting(ILLEGAL_ITEMS.path, new String[]{"Bedrock", "Command_Block"});
		addSetting(ALLOW_CUSTOM_ILLEGAL_ITEMS.path, true);
		addSetting(SHOP_OPEN_STATUS.path, "Open");
		addSetting(SHOP_CLOSED_STATUS.path, "Closed");
		addSetting(ALLOW_METRICS.path, true);

		addSetting(TRADESHOP_HEADER.path, "Trade");
		addSetting(TRADESHOP_EXPLODE.path, false);

		addSetting(ITRADESHOP_HEADER.path, "iTrade");
		addSetting(ITRADESHOP_OWNER.path, "Server Shop");
		addSetting(ITRADESHOP_EXPLODE.path, false);

		addSetting(BITRADESHOP_HEADER.path, "BiTrade");
		addSetting(BITRADESHOP_EXPLODE.path, false);

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
