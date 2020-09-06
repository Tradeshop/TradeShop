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
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.shanerx.tradeshop.TradeShop;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public enum Message {

    AMOUNT_NOT_NUM("&cYou should have an amount before each item."),
    BUY_FAILED_SIGN("&cThis shop sign does not seem to be formatted correctly, please notify the owner."),
    CONFIRM_TRADE("&eTrade &6 {AMOUNT1} {ITEM1} &e for &6 {AMOUNT2} {ITEM2} &e?"),
    EMPTY_TS_ON_SETUP("&cTradeShop empty, please remember to fill it!"),
    FULL_AMOUNT("&cYou must have &e{AMOUNT} &cof a single type of &e{ITEM}&c!"),
    HELD_EMPTY("&eYou are currently holding nothing."),
    ITEM_ADDED("&aItem successfully added to shop."),
    ILLEGAL_ITEM("&cYou cannot use one or more of those items in shops."),
    INSUFFICIENT_ITEMS("&cYou do not have &e {AMOUNT} {ITEM}&c!"),
    INVALID_ARGUMENTS("&eTry &6/tradeshop help &eto display help!"),
    INVALID_SIGN("&cInvalid sign format!"),
    MISSING_CHEST("&cYour shop is missing a chest."),
    MISSING_ITEM("&cYour sign is missing an item for trade."),
    MISSING_SHOP("&cThere is not currently a shop here, please tell the owner or come back later!"),
    NO_CHEST("&cYou need to put a chest under the sign!"),
    NO_COMMAND_PERMISSION("&aYou do not have permission to execute this command"),
    NO_SIGHTED_SHOP("&cNo shop in range!"),
    NO_TS_CREATE_PERMISSION("&cYou don't have permission to create TradeShops!"),
    NO_TS_DESTROY("&cYou may not destroy that TradeShop"),
    NO_TS_OPEN("&cThat TradeShop does not belong to you"),
    NO_SHOP_PERMISSION("&cYou do not have permission to edit that shop."),
    ON_TRADE("&aYou have traded your&e {AMOUNT2} {ITEM2} &a for &e {AMOUNT1} {ITEM1} &awith {SELLER}"),
    PLAYER_FULL("&cYour inventory is full, please make room before trading items!"),
    PLAYER_NOT_FOUND("&cThat player could not be found."),
    PLAYER_ONLY_COMMAND("&eThis command is only available to players."),
    SELF_OWNED("&cYou cannot buy from a shop in which you are a user."),
    SETUP_HELP("\n&2Setting up a TradeShop is easy! Just make sure to follow these steps:"
            + "\n \nStep 1: &ePlace down a chest."
            + "\n&2Step 2: &ePlace a sign on top of or around the chest."
            + "\n&2Step 3: &eWrite the following on the sign"
            + "\n&6[%header%]"
            + "\n&6&o-- Leave Blank --"
            + "\n&6&o-- Leave Blank --"
            + "\n&6&o-- Leave Blank --"
            + "\n&2Step 4: &eUse the addCost and addProduct commands to add items to your shop"),
    SHOP_EMPTY("&cThis TradeShop is currently &emissing &citems to complete the trade!"),
    SHOP_FULL("&cThis TradeShop is full, please contact the owner to get it emptied!"),
    SHOP_CLOSED("&cThis shop is currently closed."),
    SHOP_FULL_AMOUNT("&cThe shop does not have &e{AMOUNT} &cof a single type of &e{ITEM}&c!"),
    SUCCESSFUL_SETUP("&aYou have successfully setup a TradeShop!"),
    UNSUCCESSFUL_SHOP_MEMBERS("&aThat player is either already on the shop, or you have reached the maximum number of users!"),
    UPDATED_SHOP_MEMBERS("&aShop owners and members have been updated!"),
    NO_EDIT("&cYou do not have permission to edit this shop."),
    CHANGE_CLOSED("&cThe shop is now &l&bCLOSED&r&a."),
    CHANGE_OPEN("&aThe shop is now &l&bOPEN&r&a."),
    EXISTING_SHOP("&cYou may only have 1 shop per inventory block."),
    SHOP_TYPE_SWITCHED("&aShop type has been switched to %newtype%."),
    WHO_MESSAGE("&6Shop users are:\n&2Owner: &e{OWNER}\n&2Managers: &e{MANAGERS}\n&2Members: &e{MEMBERS}"),
    INVALID_SUBCOMMAND("&cInvalid sub-command. Cannot display usage."),
    PLUGIN_BEHIND("&cThe server is running an old version of TradeShop, please update the plugin."),
    MULTI_UPDATE("&aTrade multiplier has been updated to %amount%."),
    MULTI_AMOUNT("&aYour trade multiplier is %amount%."),
    TOO_MANY_CHESTS("&cThere are too many shops in this chunk, you can not add another one."),
    SHOP_ITEM_LIST("&aThe shops %type%:\n%list%"),
    ITEM_REMOVED("&aItem successfully removed to shop."),
    ITEM_NOT_REMOVED("&cItem could not be removed from shop."),
    TOO_MANY_ITEMS("&cThis trade can not take any more %side%!"),
    SHOP_INSUFFICIENT_ITEMS("&cThis shop does not have &e {AMOUNT} {ITEM}&c!");

	private static final char COLOUR_CHAR = '&';
	private static TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
	private static File file = new File(plugin.getDataFolder(), "messages.yml");
	private static FileConfiguration config = YamlConfiguration.loadConfiguration(file);
    private static String PREFIX = Setting.MESSAGE_PREFIX.getString() + " ";

    private String defaultValue;

    Message(String defaultValue) {
        this.defaultValue = defaultValue;
    }

	public static void setDefaults() {
		config = YamlConfiguration.loadConfiguration(file);

        for (Message message : Message.values()) {
            if (config.get(message.getPath()) == null) {
                config.set(message.getPath(), message.defaultValue);
            }
        }

		save();
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
			plugin.getLogger().log(Level.SEVERE, "Could not create Message file! Disabling plugin!", e);
			plugin.getServer().getPluginManager().disablePlugin(plugin);
		}

		setDefaults();
		config = YamlConfiguration.loadConfiguration(file);

        PREFIX = Setting.MESSAGE_PREFIX.getString();
	}

	public static String colour(String x) {
		return ChatColor.translateAlternateColorCodes(COLOUR_CHAR, x);
	}

    public String getPath() {
        return name().toLowerCase().replace("_", "-");
    }

    public String getMessage() {
        return config.getString(getPath());
    }

	@Override
	public String toString() {
        return colour(getMessage().replace("%header%", Setting.TRADESHOP_HEADER.getString()));
	}

	public String getPrefixed() {
		return colour(PREFIX + toString());
	}
}
