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

import org.shanerx.tradeshop.utils.configuration.interfaces.YAMLConfigurationInterface;

public enum MessagesEnum implements YAMLConfigurationInterface {
    FILE_NAME(MessageSectionKey.NONE, "TradeShop Messages"),
    MESSAGE_VERSION(MessageSectionKey.NONE, 1.0, "Version of the current config file.\n Do not change!", "\n"),

    AMOUNT_NOT_NUM(MessageSectionKey.UNUSED, "&cYou should have an amount before each item.", "\\Unused\\"),
    BUY_FAILED_SIGN(MessageSectionKey.UNUSED, "&cThis shop sign does not seem to be formatted correctly, please notify the owner.", "\\Unused\\"),
    CHANGE_CLOSED(MessageSectionKey.NONE, "&cThe shop is now &l&bCLOSED&r&a."),
    CHANGE_OPEN(MessageSectionKey.NONE, "&aThe shop is now &l&bOPEN&r&a."),
    CONFIRM_TRADE(MessageSectionKey.UNUSED, "&eTrade &6{AMOUNT1} {ITEM1} &efor &6{AMOUNT2} {ITEM2} &e?", "\\Unused\\"),
    EMPTY_TS_ON_SETUP(MessageSectionKey.NONE, "&cTradeShop empty, please remember to fill it!", "Text to display when a player places a TradeSign above an empty chest:"),
    EXISTING_SHOP(MessageSectionKey.NONE, "&cThis storage or sign is already linked to a shop."),
    FEATURE_DISABLED(MessageSectionKey.NONE, "&cThis feature has been disabled on this server!"),
    FULL_AMOUNT(MessageSectionKey.UNUSED, "&cYou must have &e{AMOUNT} &cof a single type of &e{ITEM}&c!", "\\Unused\\"),
    HELD_EMPTY(MessageSectionKey.NONE, "&eYou are currently holding nothing.", "Text to display when the player is not holding anything"),
    ILLEGAL_ITEM(MessageSectionKey.NONE, "&cYou cannot use one or more of those items in shops.", "Text to display when a shop failed creation due to an illegal item "),
    NO_SHULKER_COST(MessageSectionKey.NONE, "&cYou cannot add a Shulker Box as a cost when the shop uses it for storage.", "Text to display when a shop failed creation due to using a shulker box as cost when the shop uses it for storage: "),
    INSUFFICIENT_ITEMS(MessageSectionKey.NONE, "&cYou do not have &e{AMOUNT} {ITEM}&c!", "Text to display when the player does not have enough items:"),
    SHOP_INSUFFICIENT_ITEMS(MessageSectionKey.NONE, "&cThis shop does not have enough &e{AMOUNT} {ITEM}&c to trade!", "Text to display when the shop does not have enough items:"),
    INVALID_ARGUMENTS(MessageSectionKey.NONE, "&eTry &6/tradeshop help &eto display help!", "Text to display when invalid arguments are submitted through the \"/tradeshop\" command:"),
    INVALID_SIGN(MessageSectionKey.UNUSED, "&cInvalid sign format!", "\\Unused\\"),
    INVALID_SUBCOMMAND(MessageSectionKey.UNUSED, "&cInvalid sub-command. Cannot display usage.", "\\Unused\\"),
    ITEM_ADDED(MessageSectionKey.NONE, "&aItem successfully added to shop."),
    ITEM_NOT_REMOVED(MessageSectionKey.NONE, "&cItem could not be removed from shop."),
    ITEM_REMOVED(MessageSectionKey.NONE, "&aItem successfully removed to shop."),
    MISSING_CHEST(MessageSectionKey.NONE, "&cYour shop is missing a chest."),
    MISSING_ITEM(MessageSectionKey.NONE, "&cYour sign is missing an item for trade.", "Text to display when a shop sign failed creation due to missing an item"),
    MISSING_SHOP(MessageSectionKey.UNUSED, "&cThere is not currently a shop here, please tell the owner or come back later!", "\\Unused\\"),
    MULTI_AMOUNT(MessageSectionKey.NONE, "&aYour trade multiplier is %amount%."),
    MULTI_UPDATE(MessageSectionKey.NONE, "&aTrade multiplier has been updated to %amount%."),
    NO_CHEST(MessageSectionKey.NONE, "&cYou need to put a chest under the sign!", "Text to display when a player attempts to place a sign without placing the chest first:"),
    NO_COMMAND_PERMISSION(MessageSectionKey.NONE, "&cYou do not have permission to execute this command", "Text to display when a player attempts to run administrator commands:"),
    NO_SHOP_PERMISSION(MessageSectionKey.NONE, "&cYou do not have permission to edit that shop."),
    NO_TRADE_PERMISSION(MessageSectionKey.NONE, "&cYou do not have permission to trade with TradeShops", "Text to display when a player attempts to trade while having the `Prevent Trade` permission:"),
    NO_SIGHTED_SHOP(MessageSectionKey.NONE, "&cNo shop in range!", "Text to display when a player is too far from a shop"),
    NO_TS_CREATE_PERMISSION(MessageSectionKey.NONE, "&cYou don't have permission to create TradeShops!", "Text to display when a player attempts to setup a shoptype they are not allowed to create:"),
    NO_TS_DESTROY(MessageSectionKey.NONE, "&cYou may not destroy that TradeShop", "Text to display when a player attempts to destroy a shop they do not own:"),
    DESTROY_SHOP_SIGN_FIRST(MessageSectionKey.NONE, "&cYou must destroy the shops sign first.", "Text to display when a player attempts to destroy a block with a shop sign attached to it:"),
    NO_TS_OPEN(MessageSectionKey.NONE, "&cThat TradeShop does not belong to you", "Text to display when a player attempts to open a shop they do not own nor have been granted access to (1.6):"),
    ON_TRADE(MessageSectionKey.NONE, "&aYou have traded your &e{AMOUNT2} {ITEM2} &afor &e{AMOUNT1} {ITEM1} &awith {SELLER}", "Text to display upon a successful trade:"),
    PLAYER_FULL(MessageSectionKey.NONE, "&cYour inventory is full, please make room before trading items!", "Text to display when the players inventory is too full to recieve the trade:"),
    PLAYER_NOT_FOUND(MessageSectionKey.NONE, "&cThat player could not be found."),
    PLAYER_ONLY_COMMAND(MessageSectionKey.NONE, "&eThis command is only available to players.", "Text to display when console tries to use a player only command"),
    PLUGIN_BEHIND(MessageSectionKey.NONE, "&cThe server is running an old version of TradeShop, please update the plugin."),
    SELF_OWNED(MessageSectionKey.NONE, "&cYou cannot buy from a shop in which you are a user.", "Text to display when a player tries to buy form a shop in which they are a user"),
    SETUP_HELP(MessageSectionKey.NONE, "\n&2Setting up a TradeShop is easy! Just make sure to follow these steps:"
            + "\n \nStep 1: &ePlace down a chest."
            + "\n&2Step 2: &ePlace a sign on top of or around the chest."
            + "\n&2Step 3: &eWrite the following on the sign"
            + "\n&6     [%header%]"
            + "\n&6&o-- Leave Blank --"
            + "\n&6&o-- Leave Blank --"
            + "\n&6&o-- Leave Blank --"
            + "\n&2Step 4: &eUse the addCost and addProduct commands to add items to your shop", "Text to display on \"/tradeshop setup\":"),
    SHOP_CLOSED(MessageSectionKey.NONE, "&cThis shop is currently closed."),
    SHOP_EMPTY(MessageSectionKey.NONE, "&cThis TradeShop is currently missing items to complete the trade!", "Text to display when the shop does not have enough stock:"),
    SHOP_FULL(MessageSectionKey.NONE, "&cThis TradeShop is full, please contact the owner to get it emptied!", "Text to display when the shop storage is full:"),
    SHOP_FULL_AMOUNT(MessageSectionKey.UNUSED, "&cThe shop does not have &e{AMOUNT} &cof a single type of &e{ITEM}&c!", "\\Unused\\"),
    SHOP_ITEM_LIST(MessageSectionKey.NONE, "&aThe shops %type%:\n%list%"),
    SHOP_TYPE_SWITCHED(MessageSectionKey.NONE, "&aShop type has been switched to %newtype%."),
    SUCCESSFUL_SETUP(MessageSectionKey.NONE, "&aYou have successfully setup a TradeShop!", "Text to display when a player successfully creates a TradeShop:"),
    TOO_MANY_CHESTS(MessageSectionKey.NONE, "&cThere are too many shops in this chunk, you can not add another one."),
    TOO_MANY_ITEMS(MessageSectionKey.NONE, "&cThis shop cannot take any more %side% items!"),
    UNSUCCESSFUL_SHOP_MEMBERS(MessageSectionKey.NONE, "&aThat player is either already on the shop, or you have reached the maximum number of users!", "Text to display when shop users could not be updated"),
    UPDATED_SHOP_MEMBERS(MessageSectionKey.NONE, "&aShop owners and members have been updated!", "Text to display when shop users have been updated successfully"),
    WHO_MESSAGE(MessageSectionKey.NONE, "&6Shop users are:\n&2Owner: &e{OWNER}\n&2Managers: &e{MANAGERS}\n&2Members: &e{MEMBERS}", "Text to display when players use the who command"),
    VIEW_PLAYER_LEVEL(MessageSectionKey.NONE, "&e%player% has a level of %level%.", "Text to display when viewing a players level with /ts PlayerLevel"),
    SET_PLAYER_LEVEL(MessageSectionKey.NONE, "&aYou have set the level of %player% to %level%!", "Text to display after setting a players level"),
    VARIOUS_ITEM_TYPE(MessageSectionKey.NONE, "Various", "Text to display when a message uses an Item Type and the Type varies"),
    TOGGLED_STATUS(MessageSectionKey.NONE, "Toggled status: &c%status%"),
    NO_SIGN_FOUND(MessageSectionKey.NONE, "&cNo sign in range!", "Text to display when a player is too far from a sign"),
    ADMIN_TOGGLED(MessageSectionKey.NONE, "&aYour Admin mode is currently &e{STATE}&a.", "Text to display when an admin toggles or views their Admin abilities. \n# \"{STATE}\" will be replaced by the state that the player is in after the command.");


    private final String key, path, preComment, postComment;
    private final Object defaultValue;
    private final MessageSectionKey sectionKey;

    MessagesEnum(MessageSectionKey sectionKey, Object defaultValue) {
        this(sectionKey, defaultValue, "", "");
    }

    MessagesEnum(MessageSectionKey sectionKey, Object defaultValue, String preComment) {
        this(sectionKey, defaultValue, preComment, "");
    }

    MessagesEnum(MessageSectionKey sectionKey, Object defaultValue, String preComment, String postComment) {
        this.sectionKey = sectionKey;
        this.key = name().toLowerCase().replace("_", "-");
        this.path = sectionKey.getPath() + getKey();
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
    public MessageSectionKey getSectionKey() {
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
