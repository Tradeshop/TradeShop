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

    CHECK_UPDATES("check-updates", true),
    CONFIG_VERSION("config-version", 1.0),
    ALLOWED_SHOPS("allowed-shops", new String[]{"CHEST", "TRAPPED_CHEST", "SHULKER"}),
    ALLOWED_DIRECTIONS("allowed-directions", new String[]{"DOWN", "WEST", "SOUTH", "EAST", "NORTH", "UP"}),
    ITRADESHOP_OWNER("itradeshop.owner", "Server Shop"),
    ALLOW_MULTI_TRADE("allow-multi-trade", true),
    MAX_EDIT_DISTANCE("max-edit-distance", 4),
    MAX_SHOP_USERS("max-shop-users", 5),
    ILLEGAL_ITEMS("illegal-items", new String[]{"Air", "Void_Air", "Cave_Air", "Bedrock", "Command_Block"}),

    TRADESHOP_HEADER("tradeshop.header", "Trade"),
    ITRADESHOP_HEADER("itradeshop.header", "iTrade"),
    BITRADESHOP_HEADER("bitradeshop.header", "BiTrade"),

    TRADESHOP_EXPLODE("tradeshop.allow-explode", false),
    ITRADESHOP_EXPLODE("itradeshop.allow-explode", false),
    BITRADESHOP_EXPLODE("bitradeshop.allow-explode", false),

    TRADESHOP_HOPPER_EXPORT("tradeshop.allow-hopper-export", false),
    BITRADESHOP_HOPPER_EXPORT("bitradeshop.allow-hopper-export", false),

    SHOP_OPEN_STATUS("shop-open-status", "Open"),
    SHOP_CLOSED_STATUS("shop-closed-status", "Closed"),

    ALLOW_METRICS("allow-metrics", true),
    ENABLE_DEBUG("enable-debug", 0),
    MESSAGE_PREFIX("message-prefix", "&a[&eTradeShop&a]"),
    MAX_SHOPS_PER_CHUNK("max-shops-per-chunk", 128),
    MAX_ITEMS_PER_TRADE_SIDE("max-items-per-trade-side", 6);

	private static TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
	private static File file = new File(plugin.getDataFolder(), "config.yml");
	private static FileConfiguration config = YamlConfiguration.loadConfiguration(file);
    private String path;
    private Object defaultValue;

    Setting(String path, Object defaultValue) {
		this.path = path;
        this.defaultValue = defaultValue;
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

        for (Setting set : Setting.values()) {
            addSetting(set.path, set.defaultValue);
        }

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
        if (!config.isInt(ENABLE_DEBUG.path)) {
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
        config.set(toPath(), obj);
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