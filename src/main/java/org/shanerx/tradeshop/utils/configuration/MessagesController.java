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

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.utils.Tuple;
import org.shanerx.tradeshop.utils.configuration.interfaces.YAMLConfigurationControllerInterface;
import org.shanerx.tradeshop.utils.configuration.interfaces.YAMLConfigurationInterface;

public class MessagesController extends YAMLConfigurationController implements YAMLConfigurationControllerInterface {

    private String header, prefix;

    public MessagesController(TradeShop plugin) {
        super(plugin, "messages");
        reload();
    }

    @Override
    public YAMLConfigurationInterface findKey(String name) {
        return MessagesEnum.valueOf(name.toUpperCase().replace("-", "_"));
    }

    @Override
    public void setDefaults() {
        for (MessagesEnum message : MessagesEnum.values()) {
            addKey(message.getPath(), message.getDefaultValue());
        }
    }

    @Override
    public void reload() {
        load();

        setDefaults();

        upgrade();

        save();

        header = plugin.getSettings().getValueAsString(SettingsEnum.TRADESHOP_HEADER);
        prefix = plugin.getSettings().getValueAsString(SettingsEnum.MESSAGE_PREFIX);
    }

    @Override
    public void upgrade() {
        double startVersion = getValueAsDouble(MessagesEnum.MESSAGE_VERSION), version = startVersion;

        // Don't accept changes to the filename...
        setKey(MessagesEnum.FILE_NAME, MessagesEnum.FILE_NAME.getDefaultValue());

        //Changes if CONFIG_VERSION is below 1, then sets config version to 1.0
        if (version < 1.0) {
            if (getValueAsString(MessagesEnum.TOO_MANY_ITEMS).equals("&cThis trade can not take any more %side%!")) {
                setKey(MessagesEnum.TOO_MANY_ITEMS, MessagesEnum.TOO_MANY_ITEMS.getDefaultValue());
                version = 1.1;
            }
        }

        if (startVersion != version) {
            setKey(MessagesEnum.MESSAGE_VERSION, version);
        }
    }

    public String colour(String toColour) {
        return ChatColor.translateAlternateColorCodes('&', toColour);
    }

    public String getRawMessage(MessagesEnum message) {
        return getValueAsString(message);
    }

    public String getMessage(MessagesEnum message) {
        return colour(getRawMessage(message).replace("%header%", header));
    }

    public String getPrefixed(MessagesEnum message) {
        return colour(prefix + getMessage(message));
    }


    public void sendMessageDirect(CommandSender sendTo, String message) {
        sendTo.sendMessage(colour(message));
    }

    //Not currently working
    public void sendMessageDirectJson(Player sendTo, String message) {
        sendTo.sendRawMessage(colour(message));
    }

    public void sendMessage(MessagesEnum message, Player player) {
        String str = getPrefixed(message);
        if (getRawMessage(message).startsWith("#json ")) {
            str.replaceFirst("#json ", "");
            sendMessageDirectJson(player, str);
        } else {
            sendMessageDirect(player, str);
        }
    }

    public void sendMessage(MessagesEnum message, CommandSender sender) {
        sendMessageDirect(sender, getPrefixed(message));
    }

    @SafeVarargs
    public final void sendMessage(MessagesEnum message, Player player, Tuple<String, String>... replacements) {
        String str = getPrefixed(message);
        for (Tuple<String, String> replace : replacements) {
            str = str.replace(replace.getLeft(), replace.getRight());
        }

        if (getRawMessage(message).startsWith("#json ")) {
            str = str.replaceFirst("#json ", "");
            sendMessageDirectJson(player, str);
        } else {
            sendMessageDirect(player, str);
        }
    }

    @SafeVarargs
    public final void sendMessage(MessagesEnum message, CommandSender sender, Tuple<String, String>... replacements) {
        if (sender instanceof Player) {
            sendMessage(message, (Player) sender, replacements);
            return;
        }

        String str = getPrefixed(message);
        for (Tuple<String, String> replace : replacements) {
            str = str.replace(replace.getLeft(), replace.getRight());
        }

        if (getRawMessage(message).startsWith("#json ")) {
            str = str.replaceFirst("#json ", "");
        }

        sendMessageDirect(sender, str);
    }
}
