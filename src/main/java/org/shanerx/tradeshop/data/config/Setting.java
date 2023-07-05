/*
 *
 *                         Copyright (c) 2016-2023
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

package org.shanerx.tradeshop.data.config;

import org.bukkit.Bukkit;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.item.IllegalItemList;
import org.shanerx.tradeshop.item.ShopItemStackSettingKeys;
import org.shanerx.tradeshop.shop.ShopSettingKeys;
import org.shanerx.tradeshop.shop.ShopSign;
import org.shanerx.tradeshop.shop.ShopType;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public enum Setting {

    // PostComment " " adds single newline below setting and "\n" adds 2 newlines below
    // PreComment `/n ` will have a new comment marker added after a sufficient space for proper formatting

    CONFIG_VERSION(SettingSection.NONE, "config-version", 1.3),

    // System Options
    DATA_STORAGE_TYPE(SettingSection.SYSTEM_OPTIONS, "data-storage-type", "FLATFILE"),
    ENABLE_DEBUG(SettingSection.SYSTEM_OPTIONS, "enable-debug", 0),
    CHECK_UPDATES(SettingSection.SYSTEM_OPTIONS, "check-updates", true),
    ALLOW_METRICS(SettingSection.SYSTEM_OPTIONS, "allow-metrics", true),
    UNLIMITED_ADMIN(SettingSection.SYSTEM_OPTIONS, "unlimited-admin", false),

    // ^ Logging
    ENABLE_TRANSACTION_LOGGING(SettingSection.TRANSACTION_LOGGING_OPTIONS, "enable-transaction-logging", true),
    OUTPUT_TYPE(SettingSection.TRANSACTION_LOGGING_OPTIONS, "output-type", "TSV"),
    LOG_TIME_SEPARATION(SettingSection.TRANSACTION_LOGGING_OPTIONS, "log-time-separation", "H"),
    TRANSACTION_LOG_FORMAT(SettingSection.TRANSACTION_LOGGING_OPTIONS, "transaction-log-format", "%Date_@_%Time_@_%ShopType_@_%Owner_@_%TradingPlayer_@_%World_@_%X_@_%Y_@_%Z_@_%CostList_@_%ProductList"),

    // Language Options
    MESSAGE_PREFIX(SettingSection.LANGUAGE_OPTIONS, "message-prefix", "&a[&eTradeShop&a]"),

    SHOP_GOOD_COLOUR(SettingSection.LANGUAGE_OPTIONS, "shop-good-colour", "&2"),
    SHOP_INCOMPLETE_COLOUR(SettingSection.LANGUAGE_OPTIONS, "shop-incomplete-colour", "&7"),
    SHOP_BAD_COLOUR(SettingSection.LANGUAGE_OPTIONS, "shop-bad-colour", "&4"),

    SHOP_OPEN_STATUS(SettingSection.LANGUAGE_OPTIONS, "shop-open-status", "&a<Open>"),
    SHOP_CLOSED_STATUS(SettingSection.LANGUAGE_OPTIONS, "shop-closed-status", "&c<Closed>"),
    SHOP_INCOMPLETE_STATUS(SettingSection.LANGUAGE_OPTIONS, "shop-incomplete-status", "&c<Incomplete>"),
    SHOP_OUTOFSTOCK_STATUS(SettingSection.LANGUAGE_OPTIONS, "shop-outofstock-status", "&c<Out Of Stock>"),

    // Global Options
    CHEST_DIRECTIONS(SettingSection.GLOBAL_OPTIONS, "chest-directions", new String[]{"BACK", "DOWN", "LEFT", "RIGHT", "UP", "FRONT"}),
    ALLOWED_SHOPS(SettingSection.GLOBAL_OPTIONS, "allowed-shops", new String[]{"CHEST", "TRAPPED_CHEST", "SHULKER"}),
    MAX_EDIT_DISTANCE(SettingSection.GLOBAL_OPTIONS, "max-edit-distance", 4),
    ALLOW_TOGGLE_STATUS(SettingSection.GLOBAL_OPTIONS, "allow-toggle-status", true),
    ALLOW_SIGN_BREAK(SettingSection.GLOBAL_OPTIONS, "allow-sign-break", false),
    ALLOW_CHEST_BREAK(SettingSection.GLOBAL_OPTIONS, "allow-chest-break", false),


    // ^ Multi Trade
    ALLOW_MULTI_TRADE(SettingSection.GLOBAL_MULTI_TRADE, "enable", true),
    MULTI_TRADE_DEFAULT(SettingSection.GLOBAL_MULTI_TRADE, "default-multi", 2),
    MULTI_TRADE_MAX(SettingSection.GLOBAL_MULTI_TRADE, "max-multi", 6),

    // ^ Global Find Options
    MAX_FIND_RANGE(SettingSection.GLOBAL_FIND_OPTIONS, "max-find-range", 256),
    DEFAULT_FIND_RANGE(SettingSection.GLOBAL_FIND_OPTIONS, "default-find-range", 64),


    // Shop Options
    MAX_SHOP_USERS(SettingSection.SHOP_OPTIONS, "max-shop-users", 5),
    MAX_SHOPS_PER_CHUNK(SettingSection.SHOP_OPTIONS, "max-shops-per-chunk", 128),
    SUM_PER_PLAYER_LIMIT(SettingSection.SHOP_OPTIONS, "sum-per-player-limit", false),
    MAX_SHOPS_PER_PLAYER(SettingSection.SHOP_OPTIONS, "max-shops-per-player", Collections.singletonMap("default", -1)),
    MAX_ITEMS_PER_TRADE_SIDE(SettingSection.SHOP_OPTIONS, "max-items-per-trade-side", 6),
    ALLOW_USER_PURCHASING(SettingSection.SHOP_OPTIONS, "allow-user-purchasing", false),
    MULTIPLE_ITEMS_ON_SIGN(SettingSection.SHOP_OPTIONS, "multiple-items-on-sign", "Use '/ts what'"),
    NO_COST_TEXT(SettingSection.SHOP_OPTIONS, "no-cost-text", "nothing"),
    NO_COST_AMOUNT(SettingSection.SHOP_OPTIONS, "no-cost-amount", "1"),

    SHOP_PER_ITEM_SETTINGS(SettingSection.SHOP_ITEM_OPTIONS, "shop-per-item-settings", ShopItemStackSettingKeys.getDefaultConfigMap()),

    SHOP_SIGN_DEFAULT_COLOURS(SettingSection.SHOP_SIGN_OPTIONS, "sign-default-colours", ShopSign.getDefaultColourMap()),

    // Trade Shop Options
    TRADESHOP_HEADER(SettingSection.TRADE_SHOP_OPTIONS, "header", "Trade"),
    TRADESHOP_EXPLODE(SettingSection.TRADE_SHOP_OPTIONS, "allow-explode", false),
    TRADE_PER_SHOP_SETTINGS(SettingSection.TRADE_SHOP_OPTIONS, "trade-per-shop-settings", ShopSettingKeys.getSettingConfigMap(ShopType.TRADE)),

    // ITrade Shop Options
    ITRADESHOP_OWNER(SettingSection.ITRADE_SHOP_OPTIONS, "owner", "Server Shop"),
    ITRADESHOP_HEADER(SettingSection.ITRADE_SHOP_OPTIONS, "header", "iTrade"),
    ITRADESHOP_EXPLODE(SettingSection.ITRADE_SHOP_OPTIONS, "allow-explode", false),
    ITRADE_PER_SHOP_SETTINGS(SettingSection.ITRADE_SHOP_OPTIONS, "itrade-per-shop-settings", ShopSettingKeys.getSettingConfigMap(ShopType.ITRADE)),

    // BiTrade Shop Options
    BITRADESHOP_HEADER(SettingSection.BITRADE_SHOP_OPTIONS, "header", "BiTrade"),
    BITRADESHOP_EXPLODE(SettingSection.BITRADE_SHOP_OPTIONS, "allow-explode", false),
    BITRADE_PER_SHOP_SETTINGS(SettingSection.BITRADE_SHOP_OPTIONS, "bitrade-per-shop-settings", ShopSettingKeys.getSettingConfigMap(ShopType.BITRADE)),

    // Illegal Item Options
    GLOBAL_ILLEGAL_ITEMS_TYPE(SettingSection.GLOBAL_ILLEGAL_ITEMS, "type", IllegalItemList.ListType.BLACKLIST.toString()),
    GLOBAL_ILLEGAL_ITEMS_LIST(SettingSection.GLOBAL_ILLEGAL_ITEMS, "list", new String[]{"Bedrock", "Command_Block", "Barrier"}),
    COST_ILLEGAL_ITEMS_TYPE(SettingSection.COST_ILLEGAL_ITEMS, "type", IllegalItemList.ListType.DISABLED.toString()),
    COST_ILLEGAL_ITEMS_LIST(SettingSection.COST_ILLEGAL_ITEMS, "list", new String[]{}),
    PRODUCT_ILLEGAL_ITEMS_TYPE(SettingSection.PRODUCT_ILLEGAL_ITEMS, "type", IllegalItemList.ListType.DISABLED.toString()),
    PRODUCT_ILLEGAL_ITEMS_LIST(SettingSection.PRODUCT_ILLEGAL_ITEMS, "list", new String[]{});

    public static final TradeShop PLUGIN = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");

    private final String key, path;
    private final Object defaultValue;
    private final SettingSection section;

    private final String leadIncrease = "  ";

    Setting(SettingSection section, String key, Object defaultValue) {
        this.section = section;
        this.key = key;
        this.path = (!section.getPath().isEmpty() ? section.getPath() + "." : "") + key;
        this.defaultValue = defaultValue;
    }

    public static Setting findSetting(String search) {
        return valueOf(search.toUpperCase().replace("-", "_"));
    }

    // Method to fix any values that have changed with updates
    static boolean upgrade() {
        double version = CONFIG_VERSION.getDouble();
        Set<Boolean> hasUpgraded = new HashSet<>(); // Uses this instead of a boolean to later replace below ifs with boolean return methods...
        ConfigManager configManager = PLUGIN.getSettingManager();

        // 2.2.2 Changed enable debug from true/false to integer
        if (!configManager.getConfig().isInt(ENABLE_DEBUG.path)) {
            ENABLE_DEBUG.clearSetting();
        }

        // 2.2.2 Better Sorted/potentially commented config
        if (version < 1.1) {
            if (configManager.getConfig().contains("itradeshop.owner")) {
                configManager.getConfig().set(ITRADESHOP_OWNER.path, configManager.getConfig().get("itradeshop.owner"));
                configManager.getConfig().set("itradeshop.owner", null);
                hasUpgraded.add(true);
            }

            if (configManager.getConfig().contains("itradeshop.header")) {
                configManager.getConfig().set(ITRADESHOP_HEADER.path, configManager.getConfig().get("itradeshop.header"));
                configManager.getConfig().set("itradeshop.header", null);
                hasUpgraded.add(true);
            }

            if (configManager.getConfig().contains("itradeshop.allow-explode")) {
                configManager.getConfig().set(ITRADESHOP_EXPLODE.path, configManager.getConfig().get("itradeshop.allow-explode"));
                configManager.getConfig().set("itradeshop.allow-explode", null);
                hasUpgraded.add(true);
            }

            if (configManager.getConfig().contains("tradeshop.header")) {
                configManager.getConfig().set(TRADESHOP_HEADER.path, configManager.getConfig().get("tradeshop.header"));
                configManager.getConfig().set("tradeshop.header", null);
                hasUpgraded.add(true);
            }

            if (configManager.getConfig().contains("tradeshop.allow-explode")) {
                configManager.getConfig().set(TRADESHOP_EXPLODE.path, configManager.getConfig().get("tradeshop.allow-explode"));
                configManager.getConfig().set("tradeshop.allow-explode", null);
                hasUpgraded.add(true);
            }

            if (configManager.getConfig().contains("tradeshop.allow-hopper-export")) {
                configManager.getConfig().set("trade-shop-options.allow-hopper-export", configManager.getConfig().get("tradeshop.allow-hopper-export"));
                configManager.getConfig().set("tradeshop.allow-hopper-export", null);
                hasUpgraded.add(true);
            }

            if (configManager.getConfig().contains("bitradeshop.header")) {
                configManager.getConfig().set(BITRADESHOP_HEADER.path, configManager.getConfig().get("bitradeshop.header"));
                configManager.getConfig().set("bitradeshop.header", null);
                hasUpgraded.add(true);
            }

            if (configManager.getConfig().contains("bitradeshop.allow-explode")) {
                configManager.getConfig().set(BITRADESHOP_EXPLODE.path, configManager.getConfig().get("bitradeshop.allow-explode"));
                configManager.getConfig().set("bitradeshop.allow-explode", null);
                hasUpgraded.add(true);
            }

            if (configManager.getConfig().contains("bitradeshop.allow-hopper-export")) {
                configManager.getConfig().set("bitrade-shop-options.allow-hopper-export", configManager.getConfig().get("bitradeshop.allow-hopper-export"));
                configManager.getConfig().set("bitradeshop.allow-hopper-export", null);
                hasUpgraded.add(true);
            }


            version = 1.1;
        }

        if (version < 1.2) {
            if (configManager.getConfig().contains("global-options.illegal-items")) {
                configManager.getConfig().set(GLOBAL_ILLEGAL_ITEMS_LIST.path, configManager.getConfig().get("global-options.illegal-items"));
                GLOBAL_ILLEGAL_ITEMS_LIST.setValue(configManager.getConfig().getStringList("global-options.illegal-items").removeAll(Arrays.asList("Air", "Void_Air", "Cave_Air")));
                configManager.getConfig().set("global-options.illegal-items", null);
                hasUpgraded.add(true);
            }

            version = 1.2;
        }

        if (version < 1.3) {
            String oldKey = "itrade-shop-options.no-cost-text";
            if (configManager.getConfig().contains(oldKey)) {
                configManager.getConfig().set(NO_COST_TEXT.path, configManager.getConfig().get(oldKey));
                configManager.getConfig().set(oldKey, null);
                hasUpgraded.add(true);
            }

            oldKey = "itrade-shop-options.no-cost-amount";
            if (configManager.getConfig().contains(oldKey)) {
                configManager.getConfig().set(NO_COST_AMOUNT.path, configManager.getConfig().get(oldKey));
                configManager.getConfig().set(oldKey, null);
                hasUpgraded.add(true);
            }

            oldKey = "bitrade-shop-options.allow-hopper-export";
            if (configManager.getConfig().contains(oldKey)) {
                configManager.getConfig().set(BITRADE_PER_SHOP_SETTINGS.getPath() + ".allow-hopper-export", configManager.getConfig().get(oldKey));
                configManager.getConfig().set(oldKey, null);
                hasUpgraded.add(true);
            }

            oldKey = "bitrade-shop-options.allow-hopper-import";//
            if (configManager.getConfig().contains(oldKey)) {
                configManager.getConfig().set(BITRADE_PER_SHOP_SETTINGS.getPath() + ".allow-hopper-import", configManager.getConfig().get(oldKey));
                configManager.getConfig().set(oldKey, null);
                hasUpgraded.add(true);
            }

            oldKey = "trade-shop-options.allow-hopper-export";//
            if (configManager.getConfig().contains(oldKey)) {
                configManager.getConfig().set(TRADE_PER_SHOP_SETTINGS.getPath() + ".allow-hopper-export", configManager.getConfig().get(oldKey));
                configManager.getConfig().set(oldKey, null);
                hasUpgraded.add(true);
            }

            oldKey = "trade-shop-options.allow-hopper-import";//
            if (configManager.getConfig().contains(oldKey)) {
                configManager.getConfig().set(TRADE_PER_SHOP_SETTINGS.getPath() + ".allow-hopper-import", configManager.getConfig().get(oldKey));
                configManager.getConfig().set(oldKey, null);
                hasUpgraded.add(true);
            }

            version = 1.3;
        }

        CONFIG_VERSION.setValue(version);

        return hasUpgraded.contains(true);
    }

    public String getKey() {
        return key;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getMappedString(String subKey) {
        return PLUGIN.getSettingManager().getConfig().getConfigurationSection(getPath()).getString(subKey.toLowerCase().replace("_", "-"));
    }

    public boolean getMappedBoolean(String subKey) {
        return PLUGIN.getSettingManager().getConfig().getConfigurationSection(getPath()).getBoolean(subKey.toLowerCase().replace("_", "-"));
    }

    public Object getMappedObject(String subKey) {
        return PLUGIN.getSettingManager().getConfig().getConfigurationSection(getPath()).get(subKey.toLowerCase().replace("_", "-"));
    }

    public String getPostComment() {
        return PLUGIN.getLanguage().getPostComment(Language.LangSection.SETTING, Language.LangSubSection.VALUES, path);
    }

    public String getPreComment() {
        return PLUGIN.getLanguage().getPreComment(Language.LangSection.SETTING, Language.LangSubSection.VALUES, path);
    }

    public SettingSection getSection() {
        return section;
    }

    public String getPath() {
        return path;
    }

    private String processMapValue(Map<?, ?> valueMap, String localLead) {
        StringBuilder processed = new StringBuilder();

        List<Object> sortedKeys = new ArrayList<>(valueMap.keySet());
        Collections.sort(sortedKeys, (o1, o2) -> o1.toString().compareTo(o1.toString()));

        //PLUGIN.getDebugger().log("Sorted Value: " + sortedValue +"\nValue: " + valueMap, DebugLevels.DATA_ERROR);

        for (Object key : sortedKeys) {
            Object value = valueMap.get(key);
            if (value instanceof Map) {
                processed.append(localLead).append(key.toString()).append(":\n");
                processed.append(processMapValue(((Map<?, ?>) value), localLead + leadIncrease));
            } else {
                processed.append(localLead).append(key.toString()).append(": ").append(new Yaml().dump(value));
            }
        }

        return processed.toString();
    }

    public String getFileString() {
        StringBuilder keyOutput = new StringBuilder();

        if (!getPreComment().isEmpty()) {
            keyOutput.append(section.getSectionLead()).append("# ").append(PLUGIN.getSettingManager().fixCommentNewLines(section.getSectionLead(), getPreComment())).append("\n");
        }

        if (defaultValue instanceof Map) {
            keyOutput.append(section.getSectionLead()).append(getKey()).append(":\n");
            keyOutput.append(processMapValue(((Map<?, ?>) defaultValue), section.getSectionLead() + leadIncrease));
        } else {
            keyOutput.append(section.getSectionLead()).append(getKey()).append(": ").append(new Yaml().dump(getSetting()));
        }

        if (!getPostComment().isEmpty()) {
            if (getPostComment().equals(" ") || getPostComment().equals("\n"))
                keyOutput.append(getPostComment()).append("\n");
            else
                keyOutput.append(section.getSectionLead()).append("# ").append(PLUGIN.getSettingManager().fixCommentNewLines(section.getSectionLead(), getPostComment())).append("\n");
        }

        return keyOutput.toString();
    }

    public Map<String, Object> getAsMap() {
        return PLUGIN.getSettingManager().getConfig().getConfigurationSection(getPath()).getValues(true);
    }

    public void setValue(Object obj) {
        PLUGIN.getSettingManager().getConfig().set(getPath(), obj);
    }

    public void setMappedValue(String subKey, Object obj) {
        String newNode = getPath() + "." + subKey;
        PLUGIN.getSettingManager().getConfig().set(newNode, obj);
    }

    public void clearSetting() {
        PLUGIN.getSettingManager().getConfig().set(getPath(), null);
    }

    public Object getSetting() {
        return PLUGIN.getSettingManager().getConfig().get(getPath());
    }

    public String getString() {
        return PLUGIN.getSettingManager().getConfig().getString(getPath());
    }

    public List<String> getStringList() {
        return PLUGIN.getSettingManager().getConfig().getStringList(getPath());
    }

    public int getInt() {
        return PLUGIN.getSettingManager().getConfig().getInt(getPath());
    }

    public double getDouble() {
        return PLUGIN.getSettingManager().getConfig().getDouble(getPath());
    }

    public boolean getBoolean() {
        return PLUGIN.getSettingManager().getConfig().getBoolean(getPath());
    }
}