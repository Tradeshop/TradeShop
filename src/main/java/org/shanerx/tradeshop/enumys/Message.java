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
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.shanerx.tradeshop.TradeShop;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Set;
import java.util.logging.Level;

public enum Message {

    AMOUNT_NOT_NUM(MessageSectionKeys.NONE, "&cYou should have an amount before each item."),
    BUY_FAILED_SIGN(MessageSectionKeys.NONE, "&cThis shop sign does not seem to be formatted correctly, please notify the owner."),
    CONFIRM_TRADE(MessageSectionKeys.NONE, "&eTrade &6{AMOUNT1} {ITEM1} &efor &6{AMOUNT2} {ITEM2} &e?"),
    EMPTY_TS_ON_SETUP(MessageSectionKeys.NONE, "&cTradeShop empty, please remember to fill it!"),
    FULL_AMOUNT(MessageSectionKeys.NONE, "&cYou must have &e{AMOUNT} &cof a single type of &e{ITEM}&c!"),
    HELD_EMPTY(MessageSectionKeys.NONE, "&eYou are currently holding nothing."),
    ITEM_ADDED(MessageSectionKeys.NONE, "&aItem successfully added to shop."),
    ILLEGAL_ITEM(MessageSectionKeys.NONE, "&cYou cannot use one or more of those items in shops."),
    INSUFFICIENT_ITEMS(MessageSectionKeys.NONE, "&cYou do not have &e{AMOUNT} {ITEM}&c!"),
    INVALID_ARGUMENTS(MessageSectionKeys.NONE, "&eTry &6/tradeshop help &eto display help!"),
    INVALID_SIGN(MessageSectionKeys.NONE, "&cInvalid sign format!"),
    MISSING_CHEST(MessageSectionKeys.NONE, "&cYour shop is missing a chest."),
    MISSING_ITEM(MessageSectionKeys.NONE, "&cYour sign is missing an item for trade."),
    MISSING_SHOP(MessageSectionKeys.NONE, "&cThere is not currently a shop here, please tell the owner or come back later!"),
    NO_CHEST(MessageSectionKeys.NONE, "&cYou need to put a chest under the sign!"),
    NO_COMMAND_PERMISSION(MessageSectionKeys.NONE, "&aYou do not have permission to execute this command"),
    NO_SIGHTED_SHOP(MessageSectionKeys.NONE, "&cNo shop in range!"),
    NO_TS_CREATE_PERMISSION(MessageSectionKeys.NONE, "&cYou don't have permission to create TradeShops!"),
    NO_TS_DESTROY(MessageSectionKeys.NONE, "&cYou may not destroy that TradeShop"),
    NO_TS_OPEN(MessageSectionKeys.NONE, "&cThat TradeShop does not belong to you"),
    NO_SHOP_PERMISSION(MessageSectionKeys.NONE, "&cYou do not have permission to edit that shop."),
    ON_TRADE(MessageSectionKeys.NONE, "&aYou have traded your &e{AMOUNT2} {ITEM2} &afor &e{AMOUNT1} {ITEM1} &awith {SELLER}"),
    PLAYER_FULL(MessageSectionKeys.NONE, "&cYour inventory is full, please make room before trading items!"),
    PLAYER_NOT_FOUND(MessageSectionKeys.NONE, "&cThat player could not be found."),
    PLAYER_ONLY_COMMAND(MessageSectionKeys.NONE, "&eThis command is only available to players."),
    SELF_OWNED(MessageSectionKeys.NONE, "&cYou cannot buy from a shop in which you are a user."),
    SETUP_HELP(MessageSectionKeys.NONE, "\n&2Setting up a TradeShop is easy! Just make sure to follow these steps:"
            + "\n \nStep 1: &ePlace down a chest."
            + "\n&2Step 2: &ePlace a sign on top of or around the chest."
            + "\n&2Step 3: &eWrite the following on the sign"
            + "\n&6     [%header%]"
            + "\n&6&o-- Leave Blank --"
            + "\n&6&o-- Leave Blank --"
            + "\n&6&o-- Leave Blank --"
            + "\n&2Step 4: &eUse the addCost and addProduct commands to add items to your shop"),
    SHOP_EMPTY(MessageSectionKeys.NONE, "&cThis TradeShop is currently &emissing &citems to complete the trade!"),
    SHOP_FULL(MessageSectionKeys.NONE, "&cThis TradeShop is full, please contact the owner to get it emptied!"),
    SHOP_CLOSED(MessageSectionKeys.NONE, "&cThis shop is currently closed."),
    SHOP_FULL_AMOUNT(MessageSectionKeys.NONE, "&cThe shop does not have &e{AMOUNT} &cof a single type of &e{ITEM}&c!"),
    SUCCESSFUL_SETUP(MessageSectionKeys.NONE, "&aYou have successfully setup a TradeShop!"),
    UNSUCCESSFUL_SHOP_MEMBERS(MessageSectionKeys.NONE, "&aThat player is either already on the shop, or you have reached the maximum number of users!"),
    UPDATED_SHOP_MEMBERS(MessageSectionKeys.NONE, "&aShop owners and members have been updated!"),
    NO_EDIT(MessageSectionKeys.NONE, "&cYou do not have permission to edit this shop."),
    CHANGE_CLOSED(MessageSectionKeys.NONE, "&cThe shop is now &l&bCLOSED&r&a."),
    CHANGE_OPEN(MessageSectionKeys.NONE, "&aThe shop is now &l&bOPEN&r&a."),
    EXISTING_SHOP(MessageSectionKeys.NONE, "&cYou may only have 1 shop per inventory block."),
    SHOP_TYPE_SWITCHED(MessageSectionKeys.NONE, "&aShop type has been switched to %newtype%."),
    WHO_MESSAGE(MessageSectionKeys.NONE, "&6Shop users are:\n&2Owner: &e{OWNER}\n&2Managers: &e{MANAGERS}\n&2Members: &e{MEMBERS}"),
    INVALID_SUBCOMMAND(MessageSectionKeys.NONE, "&cInvalid sub-command. Cannot display usage."),
    PLUGIN_BEHIND(MessageSectionKeys.NONE, "&cThe server is running an old version of TradeShop, please update the plugin."),
    MULTI_UPDATE(MessageSectionKeys.NONE, "&aTrade multiplier has been updated to %amount%."),
    MULTI_AMOUNT(MessageSectionKeys.NONE, "&aYour trade multiplier is %amount%."),
    TOO_MANY_CHESTS(MessageSectionKeys.NONE, "&cThere are too many shops in this chunk, you can not add another one."),
    SHOP_ITEM_LIST(MessageSectionKeys.NONE, "&aThe shops %type%:\n%list%"),
    ITEM_REMOVED(MessageSectionKeys.NONE, "&aItem successfully removed to shop."),
    ITEM_NOT_REMOVED(MessageSectionKeys.NONE, "&cItem could not be removed from shop."),
    TOO_MANY_ITEMS(MessageSectionKeys.NONE, "&cThis trade can not take any more %side%!"),
    FEATURE_DISABLED(MessageSectionKeys.NONE, "&cThis feature has been disabled on this server!"),
    SHOP_INSUFFICIENT_ITEMS(MessageSectionKeys.NONE, "&cThis shop does not have &e{AMOUNT} {ITEM}&c!");

	private static final char COLOUR_CHAR = '&';
	private static TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
	private static File file = new File(plugin.getDataFolder(), "messages.yml");
	private static FileConfiguration config = YamlConfiguration.loadConfiguration(file);
    private static String PREFIX = Setting.MESSAGE_PREFIX.getString() + " ";

    private String defaultValue, preComment = "", postComment = "";
    private MessageSectionKeys sectionKey;


    Message(MessageSectionKeys sectionKey, String defaultValue) {
        this.sectionKey = sectionKey;
        this.defaultValue = defaultValue;
    }

    Message(MessageSectionKeys sectionKey, String defaultValue, String preComment) {
        this.sectionKey = sectionKey;
        this.defaultValue = defaultValue;
        this.preComment = preComment;
    }

    Message(MessageSectionKeys sectionKey, String defaultValue, String preComment, String postComment) {
        this.sectionKey = sectionKey;
        this.defaultValue = defaultValue;
        this.preComment = preComment;
        this.postComment = postComment;
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
        Validate.notNull(file, "File cannot be null");

        if (config != null)
            try {
                Files.createParentDirs(file);

                StringBuilder data = new StringBuilder();

                data.append("###########################\n").append("#    TradeShop Messages   #\n").append("###########################\n");
                Set<MessageSectionKeys> messageSectionKeys = Sets.newHashSet(MessageSectionKeys.values());

                for (Message message : values()) {
                    if (messageSectionKeys.contains(message.sectionKey)) {
                        data.append(message.sectionKey.getFormattedHeader());
                        messageSectionKeys.remove(message.sectionKey);
                    }

                    if (!message.preComment.isEmpty()) {
                        data.append("# ").append(message.preComment).append("\n");
                    }

                    data.append(message.sectionKey.getValueLead()).append(message.getPath()).append(": ").append(new Yaml().dump(message.getMessage()));

                    if (!message.postComment.isEmpty()) {
                        data.append(message.postComment).append("\n");
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
        return sectionKey.getKey() + name().toLowerCase().replace("_", "-");
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

enum MessageSectionKeys {

    NONE("", "");

    private String key, sectionHeader, value_lead = "";
    private MessageSectionKeys parent;

    MessageSectionKeys(String key, String sectionHeader) {
        this.key = key;
        this.sectionHeader = sectionHeader;
        if (!key.isEmpty())
            this.value_lead = "  ";
    }

    MessageSectionKeys(MessageSectionKeys parent, String key, String sectionHeader) {
        this.key = key;
        this.sectionHeader = sectionHeader;
        this.parent = parent;
        if (!key.isEmpty())
            this.value_lead = parent.value_lead + "  ";
    }

    public String getKey() {
        return !key.isEmpty() ? (parent != null ? parent.getKey() + "." + key + "." : key + ".") : "";
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
