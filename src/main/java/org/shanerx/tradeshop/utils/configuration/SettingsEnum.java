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

package org.shanerx.tradeshop.utils.configuration;

import org.shanerx.tradeshop.objects.IllegalItemList;
import org.shanerx.tradeshop.utils.configuration.interfaces.YAMLConfigurationInterface;

public enum SettingsEnum implements YAMLConfigurationInterface {
    // PostComment " " adds single newline below setting and "\n" adds 2 newlines below
    // PreComment `/n ` will have a new comment marker added after a sufficient space for proper formatting

    FILE_NAME(SettingSectionKey.NONE, "file-name", "TradeShop Config"),
    CONFIG_VERSION(SettingSectionKey.NONE, "config-version", 1.2, "", "\n"),

    // System Options
    DATA_STORAGE_TYPE(SettingSectionKey.SYSTEM_OPTIONS, "data-storage-type", "FLATFILE", "How would you like your servers data stored? (FLATFILE)"),
    ENABLE_DEBUG(SettingSectionKey.SYSTEM_OPTIONS, "enable-debug", 0, "What debug code should be run. This will add significant amounts of spam to the console/log, generally not used unless requested by Devs"),
    CHECK_UPDATES(SettingSectionKey.SYSTEM_OPTIONS, "check-updates", true, "Should we check for updates when the server starts"),
    ALLOW_METRICS(SettingSectionKey.SYSTEM_OPTIONS, "allow-metrics", true, "Allow us to connect anonymous metrics so we can see how our plugin is being used to better develop it"),
    UNLIMITED_ADMIN(SettingSectionKey.SYSTEM_OPTIONS, "unlimited-admin", false, "We do not recommend enabling this setting since any editing an admin should need to do can be done without this \n Should players with Admin permission be able to use any commands on any shops?"),
    USE_INTERNAL_PERMISSIONS(SettingSectionKey.SYSTEM_OPTIONS, "use-internal-permissions", false, "Should our internal permission system be used? (Only enable if you aren't using a permission plugin)", "\n"),

    // Language Options
    MESSAGE_PREFIX(SettingSectionKey.LANGUAGE_OPTIONS, "message-prefix", "&a[&eTradeShop&a] ", "The prefix the displays before all plugin messages", "\n"),

    SHOP_GOOD_COLOUR(SettingSectionKey.LANGUAGE_OPTIONS, "shop-good-colour", "&2", "Header Colours, if the codes are showing in the header, set to \"\"\n Color for successfully created and stocked signs"),
    SHOP_INCOMPLETE_COLOUR(SettingSectionKey.LANGUAGE_OPTIONS, "shop-incomplete-colour", "&7", "Color for shops that are missing data to make trades"),
    SHOP_BAD_COLOUR(SettingSectionKey.LANGUAGE_OPTIONS, "shop-bad-colour", "&4", "Color for shops that were not successfully created", "\n"),

    SHOP_OPEN_STATUS(SettingSectionKey.LANGUAGE_OPTIONS, "shop-open-status", "&a<Open>", "Status Text, What will be shown in the bottom line of shop sign for each status\n Open"),
    SHOP_CLOSED_STATUS(SettingSectionKey.LANGUAGE_OPTIONS, "shop-closed-status", "&c<Closed>", "Closed"),
    SHOP_INCOMPLETE_STATUS(SettingSectionKey.LANGUAGE_OPTIONS, "shop-incomplete-status", "&c<Incomplete>", "Incomplete"),
    SHOP_OUTOFSTOCK_STATUS(SettingSectionKey.LANGUAGE_OPTIONS, "shop-outofstock-status", "&c<Out Of Stock>", "Out of Stock", "\n"),

    // Global Options
    ALLOWED_DIRECTIONS(SettingSectionKey.GLOBAL_OPTIONS, "allowed-directions", new String[]{"DOWN", "WEST", "SOUTH", "EAST", "NORTH", "UP"}, "Directions an allowed shop can be from a sign. Allowed directions are:\n Up, Down, North, East, South, West"),
    ALLOWED_SHOPS(SettingSectionKey.GLOBAL_OPTIONS, "allowed-shops", new String[]{"CHEST", "TRAPPED_CHEST", "SHULKER"}, "Inventories to allow for shops. Allowed blocks are:\n Chest, Trapped_Chest, Dropper, Hopper, Dispenser, Shulker, ..."),
    MAX_EDIT_DISTANCE(SettingSectionKey.GLOBAL_OPTIONS, "max-edit-distance", 4, "Max distance a player can be from a shop to edit it"),
    ALLOW_TOGGLE_STATUS(SettingSectionKey.GLOBAL_OPTIONS, "allow-toggle-status", true, "Can players toggle view of involved shops?"),
    ALLOW_SIGN_BREAK(SettingSectionKey.GLOBAL_OPTIONS, "allow-sign-break", false, "Should we allow anyone to destroy a shops sign?"),
    ALLOW_CHEST_BREAK(SettingSectionKey.GLOBAL_OPTIONS, "allow-chest-break", false, "Should we allow anyone to destroy a shops storage?", "\n"),

    // ^ Multi Trade
    ALLOW_MULTI_TRADE(SettingSectionKey.GLOBAL_MULTI_TRADE, "enable", true, "Should we allow multi trades with shift + click (true/false)"),
    MULTI_TRADE_DEFAULT(SettingSectionKey.GLOBAL_MULTI_TRADE, "default-multi", 2, "Default multiplier for trades using shift + click"),
    MULTI_TRADE_MAX(SettingSectionKey.GLOBAL_MULTI_TRADE, "max-multi", 6, "Maximum amount a player can set their multiplier to. Not recommended to set any higher than 6 as this can cause bugs with iTrade Shops", "\n"),

    // Illegal Item Options
    GLOBAL_ILLEGAL_ITEMS_TYPE(SettingSectionKey.GLOBAL_ILLEGAL_ITEMS, "type", IllegalItemList.ListType.BLACKLIST.toString()),
    GLOBAL_ILLEGAL_ITEMS_LIST(SettingSectionKey.GLOBAL_ILLEGAL_ITEMS, "list", new String[]{"Bedrock", "Command_Block", "Barrier"}, "", " "),
    COST_ILLEGAL_ITEMS_TYPE(SettingSectionKey.COST_ILLEGAL_ITEMS, "type", IllegalItemList.ListType.DISABLED.toString()),
    COST_ILLEGAL_ITEMS_LIST(SettingSectionKey.COST_ILLEGAL_ITEMS, "list", new String[]{}, "", " "),
    PRODUCT_ILLEGAL_ITEMS_TYPE(SettingSectionKey.PRODUCT_ILLEGAL_ITEMS, "type", IllegalItemList.ListType.DISABLED.toString()),
    PRODUCT_ILLEGAL_ITEMS_LIST(SettingSectionKey.PRODUCT_ILLEGAL_ITEMS, "list", new String[]{}, "", " "),

    // Shop Options
    MAX_SHOP_USERS(SettingSectionKey.SHOP_OPTIONS, "max-shop-users", 5, "Maximum users(Managers/Members) a shop can have"),
    MAX_SHOPS_PER_CHUNK(SettingSectionKey.SHOP_OPTIONS, "max-shops-per-chunk", 128, "Maximum shops that can exist in a single chunk"),
    MAX_ITEMS_PER_TRADE_SIDE(SettingSectionKey.SHOP_OPTIONS, "max-items-per-trade-side", 6, "Maximum amount of item stacks per side of trade"),
    ALLOW_USER_PURCHASING(SettingSectionKey.SHOP_OPTIONS, "allow-user-purchasing", false, "Can players purchase from a shop in which they are a user of (true/false)"),
    MULTIPLE_ITEMS_ON_SIGN(SettingSectionKey.SHOP_OPTIONS, "multiple-items-on-sign", "Use '/ts what'", "Text that shows on trade signs that contain more than 1 item", "\n"),

    // Trade Shop Options
    TRADESHOP_HEADER(SettingSectionKey.TRADE_SHOP_OPTIONS, "header", "Trade", "The header that appears at the top of the shop signs, this is also what the player types to create the sign"),
    TRADESHOP_EXPLODE(SettingSectionKey.TRADE_SHOP_OPTIONS, "allow-explode", false, "Can explosions damage the shop sign/storage (true/false)"),
    TRADESHOP_HOPPER_EXPORT(SettingSectionKey.TRADE_SHOP_OPTIONS, "allow-hopper-export", false, "Can hoppers pull items from the shop storage (true/false)"),
    TRADESHOP_HOPPER_IMPORT(SettingSectionKey.TRADE_SHOP_OPTIONS, "allow-hopper-import", false, "Can hoppers push items into the shop storage (true/false)", "\n"),

    // ITrade Shop Options
    ITRADESHOP_OWNER(SettingSectionKey.ITRADE_SHOP_OPTIONS, "owner", "Server Shop", "Name to put on the bottom of iTrade signs"),
    ITRADESHOP_HEADER(SettingSectionKey.ITRADE_SHOP_OPTIONS, "header", "iTrade", "The header that appears at the top of the shop signs, this is also what the player types to create the sign"),
    ITRADESHOP_EXPLODE(SettingSectionKey.ITRADE_SHOP_OPTIONS, "allow-explode", false, "Can explosions damage the shop sign (true/false)", ""),
    ITRADESHOP_NO_COST_TEXT(SettingSectionKey.ITRADE_SHOP_OPTIONS, "no-cost-text", "nothing", "What text should be used for successful trades when no cost is present", "\n"),

    // BiTrade Shop Options
    BITRADESHOP_HEADER(SettingSectionKey.BITRADE_SHOP_OPTIONS, "header", "BiTrade", "The header that appears at the top of the shop signs, this is also what the player types to create the sign"),
    BITRADESHOP_EXPLODE(SettingSectionKey.BITRADE_SHOP_OPTIONS, "allow-explode", false, "Can explosions damage the shop sign/storage (true/false)"),
    BITRADESHOP_HOPPER_EXPORT(SettingSectionKey.BITRADE_SHOP_OPTIONS, "allow-hopper-export", false, "Can hoppers pull items from the shop storage (true/false)"),
    BITRADESHOP_HOPPER_IMPORT(SettingSectionKey.BITRADE_SHOP_OPTIONS, "allow-hopper-import", false, "Can hoppers push items into the shop storage (true/false)", "\n");

    private final String key, path, preComment, postComment;
    private final Object defaultValue;
    private final SettingSectionKey sectionKey;

    SettingsEnum(SettingSectionKey sectionKey, String key, Object defaultValue) {
        this(sectionKey, key, defaultValue, "", "");
    }

    SettingsEnum(SettingSectionKey sectionKey, String key, Object defaultValue, String preComment) {
        this(sectionKey, key, defaultValue, preComment, "");
    }

    SettingsEnum(SettingSectionKey sectionKey, String key, Object defaultValue, String preComment, String postComment) {
        this.sectionKey = sectionKey;
        this.key = key;
        this.path = sectionKey.getPath() + key;
        this.defaultValue = defaultValue;
        this.preComment = preComment;
        this.postComment = postComment;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public SettingSectionKey getSectionKey() {
        return sectionKey;
    }

    @Override
    public String getPreComment() {
        return preComment;
    }

    @Override
    public String getPostComment() {
        return postComment;
    }
}