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

package org.shanerx.tradeshop.utils.config;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.utils.Tuple;
import org.yaml.snakeyaml.Yaml;

import java.util.Objects;

public enum Message {

    MESSAGE_VERSION(MessageSection.NONE, "message-version"),
    LANGUAGE(MessageSection.NONE, "language"),

    AMOUNT_NOT_NUM(MessageSection.UNUSED, "amount-not-num"),
    BUY_FAILED_SIGN(MessageSection.UNUSED, "buy-failed-sign"),
    CHANGE_CLOSED(MessageSection.NONE, "change-closed"),
    CHANGE_OPEN(MessageSection.NONE, "change-open"),
    CONFIRM_TRADE(MessageSection.UNUSED, "confirm-trade"),
    EMPTY_TS_ON_SETUP(MessageSection.NONE, "empty-ts-on-setup"),
    EXISTING_SHOP(MessageSection.NONE, "existing-shop"),
    FEATURE_DISABLED(MessageSection.NONE, "feature-disabled"),
    FULL_AMOUNT(MessageSection.UNUSED, "full-amount"),
    HELD_EMPTY(MessageSection.NONE, "held-empty"),
    ILLEGAL_ITEM(MessageSection.NONE, "illegal-item"),
    NO_SHULKER_COST(MessageSection.NONE, "no-shulker-cost"),
    INSUFFICIENT_ITEMS(MessageSection.NONE, "insufficient-items"),
    SHOP_INSUFFICIENT_ITEMS(MessageSection.NONE, "shop-insufficient-items"),
    INVALID_ARGUMENTS(MessageSection.NONE, "invalid-arguments"),
    INVALID_SIGN(MessageSection.UNUSED, "invalid-sign"),
    INVALID_SUBCOMMAND(MessageSection.UNUSED, "invalid-subcommand"),
    ITEM_ADDED(MessageSection.NONE, "item-added"),
    ITEM_NOT_REMOVED(MessageSection.NONE, "item-not-removed"),
    ITEM_REMOVED(MessageSection.NONE, "item-removed"),
    MISSING_CHEST(MessageSection.NONE, "missing-chest"),
    MISSING_ITEM(MessageSection.NONE, "missing-item"),
    MISSING_SHOP(MessageSection.UNUSED, "missing-shop"),
    MULTI_AMOUNT(MessageSection.NONE, "multi-amount"),
    MULTI_UPDATE(MessageSection.NONE, "multi-update"),
    NO_CHEST(MessageSection.NONE, "no-chest"),
    NO_COMMAND_PERMISSION(MessageSection.NONE, "no-command-permission"),
    NO_SHOP_PERMISSION(MessageSection.NONE, "no-shop-permission"),
    NO_TRADE_PERMISSION(MessageSection.NONE, "no-trade-permission"),
    NO_SIGHTED_SHOP(MessageSection.NONE, "no-sighted-shop"),
    NO_TS_CREATE_PERMISSION(MessageSection.NONE, "no-ts-create-permission"),
    NO_TS_DESTROY(MessageSection.NONE, "no-ts-destroy"),
    DESTROY_SHOP_SIGN_FIRST(MessageSection.NONE, "destroy-shop-sign-first"),
    NO_TS_OPEN(MessageSection.NONE, "no-ts-open"),
    ON_TRADE(MessageSection.NONE, "on-trade"),
    PLAYER_FULL(MessageSection.NONE, "player-full"),
    PLAYER_NOT_FOUND(MessageSection.NONE, "player-not-found"),
    PLAYER_ONLY_COMMAND(MessageSection.NONE, "player-only-command"),
    PLUGIN_BEHIND(MessageSection.NONE, "plugin-behind"),
    SELF_OWNED(MessageSection.NONE, "self-owned"),
    SETUP_HELP(MessageSection.NONE, "setup-help"),
    SHOP_CLOSED(MessageSection.NONE, "shop-closed"),
    SHOP_EMPTY(MessageSection.NONE, "shop-empty"),
    SHOP_FULL(MessageSection.NONE, "shop-full"),
    SHOP_FULL_AMOUNT(MessageSection.UNUSED, "shop-full-amount"),
    SHOP_ITEM_LIST(MessageSection.NONE, "shop-item-list"),
    SHOP_TYPE_SWITCHED(MessageSection.NONE, "shop-type-switched"),
    SUCCESSFUL_SETUP(MessageSection.NONE, "successful-setup"),
    TOO_MANY_CHESTS(MessageSection.NONE, "too-many-chests"),
    TOO_MANY_ITEMS(MessageSection.NONE, "too-many-items"),
    UNSUCCESSFUL_SHOP_MEMBERS(MessageSection.NONE, "unsuccessful-shop-members"),
    UPDATED_SHOP_MEMBERS(MessageSection.NONE, "updated-shop-members"),
    WHO_MESSAGE(MessageSection.NONE, "who-message"),
    VIEW_PLAYER_LEVEL(MessageSection.NONE, "view-player-level"),
    SET_PLAYER_LEVEL(MessageSection.NONE, "set-player-level"),
    VARIOUS_ITEM_TYPE(MessageSection.NONE, "various-item-type"),
    TOGGLED_STATUS(MessageSection.NONE, "toggled-status"),
    NO_SIGN_FOUND(MessageSection.NONE, "no-sign-found"),
    ADMIN_TOGGLED(MessageSection.NONE, "admin-toggled");

    public static final TradeShop PLUGIN = Objects.requireNonNull((TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop"));

    private final String key, path;
    private final MessageSection section;

    Message(MessageSection section, String key) {
        this.section = section;
        this.key = key;
        this.path = (!section.getPath().isEmpty() ? section.getPath() + "." : "") + key;
    }

    // Method to fix any values that have changed with updates
    static void upgrade() {
        double version = MESSAGE_VERSION.getDouble();
        ConfigManager configManager = PLUGIN.getMessageManager();

        //Changes if CONFIG_VERSION is below 1, then sets config version to 1.0
        if (version < 1.0) {
            if (TOO_MANY_ITEMS.getString().equals("&cThis trade can not take any more %side%!")) {
                TOO_MANY_ITEMS.setValue(PLUGIN.getLanguage().getDefault(Language.LangSection.MESSAGE, TOO_MANY_ITEMS.getPath()));
                version = 1.1;
            }
        }

        MESSAGE_VERSION.setValue(version);
    }

    private void setValue(Object obj) {
        PLUGIN.getMessageManager().getConfig().set(getPath(), obj);
    }

    public String getKey() {
        return key;
    }

    public Object getDefaultValue() {
        return PLUGIN.getLanguage().getDefault(Language.LangSection.MESSAGE, path);
    }

    public String getPostComment() {
        return PLUGIN.getLanguage().getPostComment(Language.LangSection.MESSAGE, path);
    }

    public String getPreComment() {
        return PLUGIN.getLanguage().getPreComment(Language.LangSection.MESSAGE, path);
    }

    public MessageSection getSection() {
        return section;
    }

    public String getPath() {
        return path;
    }

    public String getFileString() {
        StringBuilder keyOutput = new StringBuilder();

        if (!getPreComment().isEmpty()) {
            keyOutput.append(section.getSectionLead()).append("# ").append(PLUGIN.getMessageManager().fixCommentNewLines(section.getSectionLead(), getPreComment())).append("\n");
        }

        keyOutput.append(section.getSectionLead()).append(getKey()).append(": ").append(new Yaml().dump(getObject()));

        if (!getPostComment().isEmpty()) {
            if (getPostComment().equals(" ") || getPostComment().equals("\n"))
                keyOutput.append(getPostComment()).append("\n");
            else
                keyOutput.append(section.getSectionLead()).append("# ").append(PLUGIN.getMessageManager().fixCommentNewLines(section.getSectionLead(), getPostComment())).append("\n");
        }

        return keyOutput.toString();
    }

    public Object getObject() {
        return PLUGIN.getMessageManager().getConfig().get(getPath());
    }

    public String getString() {
        return PLUGIN.getMessageManager().getConfig().getString(getPath());
    }

    public double getDouble() {
        return PLUGIN.getMessageManager().getConfig().getDouble(getPath());
    }

    @Override
    public String toString() {
        return PLUGIN.getMessageManager().colour(getString().replace("%header%", Setting.TRADESHOP_HEADER.getString()));
    }

    public String getPrefixed() {
        return PLUGIN.getMessageManager().colour(Setting.MESSAGE_PREFIX.getString() + " " + this);
    }


    private void sendMessageDirect(CommandSender sendTo, String message) {
        sendTo.sendMessage(PLUGIN.getMessageManager().colour(message));
    }

    //Not currently working
    private void sendMessageDirectJson(Player sendTo, String message) {
        sendTo.sendRawMessage(PLUGIN.getMessageManager().colour(message));
    }

    public void sendMessage(Player player) {
        String message = getPrefixed();
        if (getString().startsWith("#json ")) {
            message.replaceFirst("#json ", "");
            sendMessageDirectJson(player, message);
        } else {
            sendMessageDirect(player, message);
        }
    }

    public void sendMessage(CommandSender sender) {
        sendMessageDirect(sender, getPrefixed());
    }

    @SafeVarargs
    public final void sendMessage(Player player, Tuple<String, String>... replacements) {
        String message = getPrefixed();
        for (Tuple<String, String> replace : replacements) {
            message = message.replace(replace.getLeft(), replace.getRight());
        }

        if (getString().startsWith("#json ")) {
            message = message.replaceFirst("#json ", "");
            sendMessageDirectJson(player, message);
        } else {
            sendMessageDirect(player, message);
        }
    }

    @SafeVarargs
    public final void sendMessage(CommandSender sender, Tuple<String, String>... replacements) {
        if (sender instanceof Player) {
            sendMessage((Player) sender, replacements);
            return;
        }

        String message = getPrefixed();
        for (Tuple<String, String> replace : replacements) {
            message = message.replace(replace.getLeft(), replace.getRight());
        }

        if (getString().startsWith("#json ")) {
            message = message.replaceFirst("#json ", "");
        }

        sendMessageDirect(sender, message);
    }
}

