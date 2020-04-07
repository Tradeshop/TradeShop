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

package org.shanerx.tradeshop.enumys;

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
    NO_PERM_MODE("no-perm-mode"),
    CONFIG_VERSION("config-version"),
	ALLOWED_SHOPS("allowed-shops"),
	ALLOWED_DIRECTIONS("allowed-directions"),
	ITRADESHOP_OWNER("itradeshop.owner"),
	ALLOW_MULTI_TRADE("allow-multi-trade"),
	MAX_EDIT_DISTANCE("max-edit-distance"),
	MAX_SHOP_USERS("max-shop-users"),
	ILLEGAL_ITEMS("illegal-items"),

	TRADESHOP_HEADER("tradeshop.header"),
	ITRADESHOP_HEADER("itradeshop.header"),
	BITRADESHOP_HEADER("bitradeshop.header"),

	TRADESHOP_EXPLODE("tradeshop.allow-explode"),
	ITRADESHOP_EXPLODE("itradeshop.allow-explode"),
	BITRADESHOP_EXPLODE("bitradeshop.allow-explode"),

	TRADESHOP_HOPPER_EXPORT("tradeshop.allow-hopper-export"),
	BITRADESHOP_HOPPER_EXPORT("bitradeshop.allow-hopper-export"),

	SHOP_OPEN_STATUS("shop-open-status"),
	SHOP_CLOSED_STATUS("shop-closed-status"),

	ALLOW_METRICS("allow-metrics"),
	ENABLE_DEBUG("enable-debug"),
	MAX_SHOPS_PER_CHUNK("max-shops-per-chunk"),
	MAX_ITEMS_PER_TRADE_SIDE("max-items-per-trade-side");

	private static TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
	private static File file = new File(plugin.getDataFolder(), "config.yml");
	private static FileConfiguration config = YamlConfiguration.loadConfiguration(file);
	String path;

	Setting(String path) {
		this.path = path;
	}

	public static ArrayList<String> getItemBlackList() {
		ArrayList<String> blacklist = new ArrayList<>();
		blacklist.add("air");
		blacklist.addAll(Setting.ILLEGAL_ITEMS.getStringList());

		return blacklist;
	}

	public static Setting findSetting(String search) {
		return valueOf(search.toUpperCase().replace("-", "_"));
	}

	private static void setDefaults() {
		config = YamlConfiguration.loadConfiguration(file);

		addSetting(CHECK_UPDATES.path, true);
		addSetting(ALLOWED_SHOPS.path, new String[]{"CHEST", "TRAPPED_CHEST", "SHULKER"});
		addSetting(ALLOWED_DIRECTIONS.path, new String[]{"DOWN", "WEST", "SOUTH", "EAST", "NORTH", "UP"});
		addSetting(ALLOW_MULTI_TRADE.path, true);
		addSetting(MAX_EDIT_DISTANCE.path, 4);
		addSetting(MAX_SHOP_USERS.path, 5);
		addSetting(ILLEGAL_ITEMS.path, new String[]{"Air", "Void_Air", "Cave_Air", "Bedrock", "Command_Block"});
		addSetting(SHOP_OPEN_STATUS.path, "Open");
		addSetting(SHOP_CLOSED_STATUS.path, "Closed");
		addSetting(MAX_SHOPS_PER_CHUNK.path, 128);
		addSetting(MAX_ITEMS_PER_TRADE_SIDE.path, 6);
		addSetting(ALLOW_METRICS.path, true);
        addSetting(ENABLE_DEBUG.path, 0);
        addSetting(CONFIG_VERSION.path, 0);

		addSetting(TRADESHOP_HEADER.path, "Trade");
		addSetting(TRADESHOP_EXPLODE.path, false);
		addSetting(TRADESHOP_HOPPER_EXPORT.path, false);

		addSetting(ITRADESHOP_HEADER.path, "iTrade");
		addSetting(ITRADESHOP_OWNER.path, "Server Shop");
		addSetting(ITRADESHOP_EXPLODE.path, false);

		addSetting(BITRADESHOP_HEADER.path, "BiTrade");
		addSetting(BITRADESHOP_EXPLODE.path, false);
		addSetting(BITRADESHOP_HOPPER_EXPORT.path, false);

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

        fixUp();

		setDefaults();
		config = YamlConfiguration.loadConfiguration(file);
	}

    // Method to fix any values that have changed with updates
    private static void fixUp() {
        boolean changes = false;

        // 2.2.2 Changed enable debug from true/false to integer
        // Value will be turned into binary representation where each bit represents a set of debug code or level
        if (config.isBoolean(ENABLE_DEBUG.path)) {
            ENABLE_DEBUG.clearSetting();
            changes = true;
        }

        //Changes if CONFIG_VERSION is below 1, then sets config version to 1.0
        if (CONFIG_VERSION.getDouble() < 1.0) {
            ENABLE_DEBUG.clearSetting();

            CONFIG_VERSION.setSetting(1.0);
            changes = true;
        }


        if (changes)
            save();
    }

	public static FileConfiguration getConfig() {
		return config;
	}

	public String toPath() {
		return path;
	}

    public void setSetting(Object obj) {
        config.set(toPath(), obj.toString());
    }

    public void clearSetting() {
        config.set(toPath(), null);
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
}
