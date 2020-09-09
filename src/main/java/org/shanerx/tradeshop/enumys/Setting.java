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

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.shanerx.tradeshop.TradeShop;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public enum Setting {

    CONFIG_VERSION(SectionKeys.NONE, "config-version", 1.1, "", "\n"),

    // System Options
    ENABLE_DEBUG(SectionKeys.SYSTEM_OPTIONS, "enable-debug", 0),
    CHECK_UPDATES(SectionKeys.SYSTEM_OPTIONS, "check-updates", true),
    ALLOW_METRICS(SectionKeys.SYSTEM_OPTIONS, "allow-metrics", true, "", "\n"),

    // Language Options
    MESSAGE_PREFIX(SectionKeys.LANGUAGE_OPTIONS, "message-prefix", "&a[&eTradeShop&a] ", "", "\n"),

    SHOP_GOOD_COLOUR(SectionKeys.LANGUAGE_OPTIONS, "shop-good-colour", "&2", "Header Colours, if the codes are showing in the header, set to \"\""),
    SHOP_INCOMPLETE_COLOUR(SectionKeys.LANGUAGE_OPTIONS, "shop-incomplete-colour", "&7"),
    SHOP_BAD_COLOUR(SectionKeys.LANGUAGE_OPTIONS, "shop-bad-colour", "&4", "", "\n"),

    SHOP_OPEN_STATUS(SectionKeys.LANGUAGE_OPTIONS, "shop-open-status", "&a<Open>", "Status Text, What will be shown in the bottom line of shop sign for each status"),
    SHOP_CLOSED_STATUS(SectionKeys.LANGUAGE_OPTIONS, "shop-closed-status", "&c<Closed>"),
    SHOP_INCOMPLETE_STATUS(SectionKeys.LANGUAGE_OPTIONS, "shop-incomplete-status", "&c<Incomplete>"),
    SHOP_OUTOFSTOCK_STATUS(SectionKeys.LANGUAGE_OPTIONS, "shop-outofstock-status", "&c<Out Of Stock>", "", "\n"),

    // Global Options
    ALLOWED_DIRECTIONS(SectionKeys.GLOBAL_OPTIONS, "allowed-directions", new String[]{"DOWN", "WEST", "SOUTH", "EAST", "NORTH", "UP"}),
    ALLOWED_SHOPS(SectionKeys.GLOBAL_OPTIONS, "allowed-shops", new String[]{"CHEST", "TRAPPED_CHEST", "SHULKER"}),
    MAX_EDIT_DISTANCE(SectionKeys.GLOBAL_OPTIONS, "max-edit-distance", 4),
    ILLEGAL_ITEMS(SectionKeys.GLOBAL_OPTIONS, "illegal-items", new String[]{"Air", "Void_Air", "Cave_Air", "Bedrock", "Command_Block"}, "", "\n"),

    // ^ Multi Trade
    ALLOW_MULTI_TRADE(SectionKeys.GLOBAL_MULTI_TRADE, "enable", true),
    MULTI_TRADE_DEFAULT(SectionKeys.GLOBAL_MULTI_TRADE, "default-multi", 2),
    MULTI_TRADE_MAX(SectionKeys.GLOBAL_MULTI_TRADE, "max-multi", 6, "", "\n"),

    // Shop Options
    MAX_SHOP_USERS(SectionKeys.SHOP_OPTIONS, "max-shop-users", 5),
    MAX_SHOPS_PER_CHUNK(SectionKeys.SHOP_OPTIONS, "max-shops-per-chunk", 128),
    MAX_ITEMS_PER_TRADE_SIDE(SectionKeys.SHOP_OPTIONS, "max-items-per-trade-side", 6, "", "\n"),


    // Trade Shop Options
    TRADESHOP_HEADER(SectionKeys.TRADE_SHOP_OPTIONS, "header", "Trade"),
    TRADESHOP_EXPLODE(SectionKeys.TRADE_SHOP_OPTIONS, "allow-explode", false),
    TRADESHOP_HOPPER_EXPORT(SectionKeys.TRADE_SHOP_OPTIONS, "allow-hopper-export", false, "", "\n"),


    // ITrade Shop Options
    ITRADESHOP_OWNER(SectionKeys.ITRADE_SHOP_OPTIONS, "owner", "Server Shop"),
    ITRADESHOP_HEADER(SectionKeys.ITRADE_SHOP_OPTIONS, "header", "iTrade"),
    ITRADESHOP_EXPLODE(SectionKeys.ITRADE_SHOP_OPTIONS, "allow-explode", false, "", "\n"),


    // BiTrade Shop Options
    BITRADESHOP_HEADER(SectionKeys.BITRADE_SHOP_OPTIONS, "header", "BiTrade"),
    BITRADESHOP_EXPLODE(SectionKeys.BITRADE_SHOP_OPTIONS, "allow-explode", false),
    BITRADESHOP_HOPPER_EXPORT(SectionKeys.BITRADE_SHOP_OPTIONS, "allow-hopper-export", false);


	private static TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
	private static File file = new File(plugin.getDataFolder(), "config.yml");
	private static FileConfiguration config = YamlConfiguration.loadConfiguration(file);
    private String key, path, preComment = "", postComment = "";
    private Object defaultValue;
    private SectionKeys sectionKey;

    Setting(SectionKeys sectionKey, String path, Object defaultValue) {
        this.sectionKey = sectionKey;
        this.key = path;
        this.path = sectionKey.getKey() + path;
        this.defaultValue = defaultValue;
    }

    Setting(SectionKeys sectionKey, String path, Object defaultValue, String preComment) {
        this.sectionKey = sectionKey;
        this.key = path;
        this.path = sectionKey.getKey() + path;
        this.defaultValue = defaultValue;
        this.preComment = preComment;
    }

    Setting(SectionKeys sectionKey, String path, Object defaultValue, String preComment, String postComment) {
        this.sectionKey = sectionKey;
        this.key = path;
        this.path = sectionKey.getKey() + path;
        this.defaultValue = defaultValue;
        this.preComment = preComment;
        this.postComment = postComment;
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
        Validate.notNull(file, "File cannot be null");

		if (config != null)
			try {
                Files.createParentDirs(file);

                StringBuilder data = new StringBuilder();

                data.append("##########################\n").append("#    TradeShop Config    #\n").append("##########################\n");
                Set<SectionKeys> sectionKeys = Sets.newHashSet(SectionKeys.values());

                for (Setting setting : values()) {
                    if (sectionKeys.contains(setting.sectionKey)) {
                        data.append(setting.sectionKey.getFormattedHeader());
                        sectionKeys.remove(setting.sectionKey);
                    }

                    if (!setting.preComment.isEmpty()) {
                        data.append("# ").append(setting.preComment).append("\n");
                    }

                    data.append(setting.sectionKey.getValueLead()).append(setting.key).append(": ").append(new Yaml().dump(setting.getSetting()));

                    if (!setting.postComment.isEmpty()) {
                        data.append(setting.postComment).append("\n");
                    }
                }

                Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8);

                try {
                    writer.write(data.toString());
                } finally {
                    writer.close();
                }


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

        //Changes if CONFIG_VERSION is below 1, then sets config version to 1.0
        if (CONFIG_VERSION.getDouble() < 1.0) {
            CONFIG_VERSION.setSetting(1.0);
            changes = true;
        }

        // 2.2.2 Changed enable debug from true/false to integer
        if (!config.isInt(ENABLE_DEBUG.path)) {
            ENABLE_DEBUG.clearSetting();
            changes = true;
        }

        // 2.2.2 Better Sorted/potentially commented config
        if (CONFIG_VERSION.getDouble() < 1.1) {
            if (config.contains("itradeshop.owner")) {
                config.set(ITRADESHOP_OWNER.path, config.get("itradeshop.owner"));
                config.set("itradeshop.owner", null);
                changes = true;
            }

            if (config.contains("itradeshop.header")) {
                config.set(ITRADESHOP_HEADER.path, config.get("itradeshop.header"));
                config.set("itradeshop.header", null);
                changes = true;
            }

            if (config.contains("itradeshop.allow-explode")) {
                config.set(ITRADESHOP_EXPLODE.path, config.get("itradeshop.allow-explode"));
                config.set("itradeshop.allow-explode", null);
                changes = true;
            }

            if (config.contains("tradeshop.header")) {
                config.set(TRADESHOP_HEADER.path, config.get("tradeshop.header"));
                config.set("tradeshop.header", null);
                changes = true;
            }

            if (config.contains("tradeshop.allow-explode")) {
                config.set(TRADESHOP_EXPLODE.path, config.get("tradeshop.allow-explode"));
                config.set("tradeshop.allow-explode", null);
                changes = true;
            }

            if (config.contains("tradeshop.allow-hopper-export")) {
                config.set(TRADESHOP_HOPPER_EXPORT.path, config.get("tradeshop.allow-hopper-export"));
                config.set("tradeshop.allow-hopper-export", null);
                changes = true;
            }

            if (config.contains("bitradeshop.header")) {
                config.set(BITRADESHOP_HEADER.path, config.get("bitradeshop.header"));
                config.set("bitradeshop.header", null);
                changes = true;
            }

            if (config.contains("bitradeshop.allow-explode")) {
                config.set(BITRADESHOP_EXPLODE.path, config.get("bitradeshop.allow-explode"));
                config.set("bitradeshop.allow-explode", null);
                changes = true;
            }

            if (config.contains("bitradeshop.allow-hopper-export")) {
                config.set(BITRADESHOP_HOPPER_EXPORT.path, config.get("bitradeshop.allow-hopper-export"));
                config.set("bitradeshop.allow-hopper-export", null);
                changes = true;
            }


            CONFIG_VERSION.setSetting(1.1);
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

enum SectionKeys {

    NONE("", ""),
    SYSTEM_OPTIONS("system-options", "System Options"),
    LANGUAGE_OPTIONS("language-options", "Language Options"),
    GLOBAL_OPTIONS("global-options", "Global Options"),
    GLOBAL_MULTI_TRADE(GLOBAL_OPTIONS, "multi-trade", ""),
    SHOP_OPTIONS("shop-options", "Shop Options"),
    TRADE_SHOP_OPTIONS("trade-shop-options", "Trade Shop Options"),
    ITRADE_SHOP_OPTIONS("itrade-shop-options", "ITrade Shop Options"),
    BITRADE_SHOP_OPTIONS("bitrade-shop-options", "BiTrade Shop Options");

    private String key, sectionHeader, value_lead = "";
    private SectionKeys parent;

    SectionKeys(String key, String sectionHeader) {
        this.key = key;
        this.sectionHeader = sectionHeader;
        if (!key.isEmpty())
            this.value_lead = "  ";
    }

    SectionKeys(SectionKeys parent, String key, String sectionHeader) {
        this.key = key;
        this.sectionHeader = sectionHeader;
        this.parent = parent;
        if (!key.isEmpty())
            this.value_lead = parent.value_lead + "  ";
    }

    public String getKey() {
        return parent != null ? parent.getKey() + "." + key + "." : key + ".";
    }

    public String getValueLead() {
        return value_lead;
    }

    public String getFormattedHeader() {
        if (!sectionHeader.isEmpty() && !key.isEmpty()) {
            StringBuilder header = new StringBuilder();
            header.append("|    ").append(sectionHeader).append("    |");

            int line1Length = header.length();

            header.insert(0, "# ").append("\n").append("# ");

            while (line1Length > 0) {
                header.append("^");
                line1Length--;
            }

            header.append("\n").append(getFileText()).append(":\n");

            return header.toString();
        } else if (sectionHeader.isEmpty() && !key.isEmpty()) {
            StringBuilder header = new StringBuilder();

            header.append(getFileText()).append(":\n");

            return header.toString();
        }

        return "";
    }

    public String getFileText() {
        return parent != null ? parent.value_lead + key : key;
    }
}