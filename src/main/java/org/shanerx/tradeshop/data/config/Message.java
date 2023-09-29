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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.item.ShopItemStack;
import org.shanerx.tradeshop.utils.debug.Debug;
import org.shanerx.tradeshop.utils.objects.Tuple;
import org.yaml.snakeyaml.Yaml;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Message {

    MESSAGE_VERSION(MessageSection.NONE, "message-version"),
    LANGUAGE(MessageSection.NONE, "language"),

    CHANGE_CLOSED(MessageSection.NONE, "change-closed"),
    PLAYER_LOCKED(MessageSection.NONE, "player-locked"),
    CHANGE_OPEN(MessageSection.NONE, "change-open"),
    EMPTY_TS_ON_SETUP(MessageSection.NONE, "empty-ts-on-setup"),
    EXISTING_SHOP(MessageSection.NONE, "existing-shop"),
    FEATURE_DISABLED(MessageSection.NONE, "feature-disabled"),
    HELD_EMPTY(MessageSection.NONE, "held-empty"),
    ILLEGAL_ITEM(MessageSection.NONE, "illegal-item"),
    NO_SHULKER_ITEM(MessageSection.NONE, "no-shulker-item"),
    INSUFFICIENT_ITEMS(MessageSection.NONE, "insufficient-items"),
    SHOP_INSUFFICIENT_ITEMS(MessageSection.NONE, "shop-insufficient-items"),
    INVALID_ARGUMENTS(MessageSection.NONE, "invalid-arguments"),
    ITEM_ADDED(MessageSection.NONE, "item-added"),
    ITEM_NOT_REMOVED(MessageSection.NONE, "item-not-removed"),
    ITEM_REMOVED(MessageSection.NONE, "item-removed"),
    MISSING_CHEST(MessageSection.NONE, "missing-chest"),
    MISSING_ITEM(MessageSection.NONE, "missing-item"),
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
    SHOP_ITEM_LIST(MessageSection.NONE, "shop-item-list"),
    SHOP_TYPE_SWITCHED(MessageSection.NONE, "shop-type-switched"),
    SUCCESSFUL_SETUP(MessageSection.NONE, "successful-setup"),
    TOO_MANY_CHESTS(MessageSection.NONE, "too-many-chests"),
    TOO_MANY_ITEMS(MessageSection.NONE, "too-many-items"),
    UPDATED_SHOP_USERS(MessageSection.NONE, "updated-shop-users"),
    UPDATED_SHOP_USERS_SUCCESSFUL(MessageSection.NONE, "pdated-shop-users-successful"),
    UPDATED_SHOP_USERS_FAILED(MessageSection.NONE, "updated-shop-users-failed"),
    UPDATED_SHOP_USERS_FAILED_CAPACITY(MessageSection.NONE, "updated-shop-users-failed-capacity"),
    UPDATED_SHOP_USERS_FAILED_EXISTING(MessageSection.NONE, "updated-shop-users-failed-existing"),
    UPDATED_SHOP_USERS_FAILED_MISSING(MessageSection.NONE, "updated-shop-users-failed-missing"),
    WHO_MESSAGE(MessageSection.NONE, "who-message"),
    VIEW_PLAYER_LEVEL(MessageSection.NONE, "view-player-level"),
    SET_PLAYER_LEVEL(MessageSection.NONE, "set-player-level"),
    VARIOUS_ITEM_TYPE(MessageSection.NONE, "various-item-type"),
    TOGGLED_STATUS(MessageSection.NONE, "toggled-status"),
    NO_SIGN_FOUND(MessageSection.NONE, "no-sign-found"),
    NO_SHOP_FOUND(MessageSection.NONE, "no-shop-found"),
    ADMIN_TOGGLED(MessageSection.NONE, "admin-toggled"),
    FAILED_TRADE(MessageSection.NONE, "failed-trade"),
    SHOP_LIMIT_REACHED(MessageSection.NONE, "shop-limit-reached"),
    METRICS_MESSAGE(MessageSection.METRICS, "message"),
    METRICS_COUNTER(MessageSection.METRICS, "counter"),
    METRICS_TIMED_COUNTER(MessageSection.METRICS, "times-counter"),
    METRICS_VERSION(MessageSection.METRICS, "version");

    public static final TradeShop PLUGIN = Objects.requireNonNull((TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop"));

    private final String key, path, MULTILINEREGEX = "[{](&V&=)[^}]*[}]";
    private final MessageSection section;

    Message(MessageSection section, String key) {
        this.section = section;
        this.key = key;
        this.path = (!section.getPath().isEmpty() ? section.getPath() + "." : "") + key;
    }

    // Method to fix any values that have changed with updates
    static boolean upgrade() {
        double version = MESSAGE_VERSION.getDouble();
        Set<Boolean> hasUpgraded = new HashSet<>(); // Uses this instead of a boolean to later replace below ifs with boolean return methods...

        //Changes if CONFIG_VERSION is below 1.1, then update to 1.1
        if (checkVersion(version, 1.1)) {
            if (TOO_MANY_ITEMS.getString().equals("&cThis trade can not take any more %side%!")) {
                TOO_MANY_ITEMS.setValue(PLUGIN.getLanguage().getDefault(Language.LangSection.SETTING, Language.LangSubSection.VALUES, TOO_MANY_ITEMS.getPath()));
                hasUpgraded.add(true);
            }
            version = 1.1;
        }

        //Changes if CONFIG_VERSION is below 1.2, then update to 1.2
        if (checkVersion(version, 1.2)) {
            Arrays.stream(values()).forEach((message) -> {
                String str = message.getString().replace("{", "%").replace("}", "%");

                if (!str.equals(message.getString())) {
                    message.setValue(str);
                    hasUpgraded.add(true);
                }

            });
            version = 1.2;
        }

        //Changes if CONFIG_VERSION is below 1.3, then update to 1.3
        if (checkVersion(version, 1.3)) {
            if (INSUFFICIENT_ITEMS.getString().equals("&cYou do not have &e%AMOUNT% %ITEM%&c!")) {
                INSUFFICIENT_ITEMS.setValue(PLUGIN.getLanguage().getDefault(Language.LangSection.MESSAGE, Language.LangSubSection.VALUES, INSUFFICIENT_ITEMS.getPath()));
                hasUpgraded.add(true);
            }
            if (SHOP_INSUFFICIENT_ITEMS.getString().equals("&cThis shop does not have enough &e%AMOUNT% %ITEM%&c to trade!")) {
                SHOP_INSUFFICIENT_ITEMS.setValue(PLUGIN.getLanguage().getDefault(Language.LangSection.MESSAGE, Language.LangSubSection.VALUES, SHOP_INSUFFICIENT_ITEMS.getPath()));
                hasUpgraded.add(true);
            }
            if (ON_TRADE.getString().equals("&aYou have traded your &e%AMOUNT2% %ITEM2% &afor &e%AMOUNT1% %ITEM1% &awith %SELLER%")) {
                ON_TRADE.setValue(PLUGIN.getLanguage().getDefault(Language.LangSection.MESSAGE, Language.LangSubSection.VALUES, ON_TRADE.getPath()));
                hasUpgraded.add(true);
            }


            version = 1.3;
        }

        MESSAGE_VERSION.setValue(version != 0.0 ? version : 1.3);

        return hasUpgraded.contains(true);
    }

    private static boolean checkVersion(double version, double maxVersion) {
        return version > 0.0 && version < maxVersion;
    }

    private void setValue(Object obj) {
        PLUGIN.getMessageManager().getConfig().set(getPath(), obj);
    }

    public String getKey() {
        return key;
    }

    public Object getDefaultValue() {
        return PLUGIN.getLanguage().getDefault(Language.LangSection.MESSAGE, Language.LangSubSection.VALUES, path);
    }

    public String getPostComment() {
        return PLUGIN.getLanguage().getPostComment(Language.LangSection.MESSAGE, Language.LangSubSection.VALUES, path);
    }

    public String getPreComment() {
        return PLUGIN.getLanguage().getPreComment(Language.LangSection.MESSAGE, Language.LangSubSection.VALUES, path);
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
        return PLUGIN.getMessageManager().colour(Setting.MESSAGE_PREFIX.getString().trim() + " " + this);
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
            message = message.replace(replace.getLeft().toUpperCase(), replace.getRight())
                    .replace(replace.getLeft().toLowerCase(), replace.getRight())
                    .replace(replace.getLeft(), replace.getRight());
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
            message = message.replace(replace.getLeft().toUpperCase(), replace.getRight())
                    .replace(replace.getLeft().toLowerCase(), replace.getRight())
                    .replace(replace.getLeft(), replace.getRight());
        }

        if (getString().startsWith("#json ")) {
            message = message.replaceFirst("#json ", "");
        }

        sendMessageDirect(sender, message);
    }

    @SafeVarargs
    public final void sendItemMultiLineMessage(Player player, Map<Variable, List<ItemStack>> itemsToFill, Tuple<String, String>... replacements) {
        if (itemsToFill.isEmpty()) {
            sendMessage(player, replacements);
            return;
        }

        boolean isJson = getString().startsWith("#json ");
        String message = getPrefixed().replaceFirst("#json ", "");

        Debug debug = TradeShop.getPlugin().getDebugger();

        for (Map.Entry<Variable, List<ItemStack>> entry : itemsToFill.entrySet()) {
            Pattern pattern = Pattern.compile(MULTILINEREGEX.replace("&V&", entry.getKey().toString()));
            Matcher matcher = pattern.matcher(message);

            if (entry.getValue().get(0) == null) {
                entry.getValue().remove(0);
            }

            while (matcher.find()) {
                StringBuilder itemList = new StringBuilder();
                String found = matcher.group(), format = found.replaceAll("[{}]", "").split("=")[1];

                for (ItemStack itm : entry.getValue()) {
                    itemList.append("\n")
                            .append(format.replace(Variable.ITEM.toString(), ShopItemStack.getCleanItemName(itm))
                                    .replace(Variable.AMOUNT.toString(), String.valueOf(itm.getAmount())));
                }

                message = message.replace(found, itemList.toString());
            }
        }

        for (Tuple<String, String> replace : replacements) {
            message = message.replace(replace.getLeft().toUpperCase(), replace.getRight())
                    .replace(replace.getLeft().toLowerCase(), replace.getRight())
                    .replace(replace.getLeft(), replace.getRight());
        }

        if (isJson) {
            sendMessageDirectJson(player, message);
        } else {
            sendMessageDirect(player, message);
        }
    }

    @SafeVarargs
    public final void sendUserEditMultiLineMessage(Player player, Map<Variable, Map<String, String>> valuesToFill, Tuple<String, String>... replacements) {
        if (valuesToFill.isEmpty()) {
            sendMessage(player, replacements);
            return;
        }

        boolean isJson = getString().startsWith("#json ");
        String message = getPrefixed().replaceFirst("#json ", "");

        Debug debug = TradeShop.getPlugin().getDebugger();

        for (Map.Entry<Variable, Map<String, String>> entry : valuesToFill.entrySet()) {
            Pattern pattern = Pattern.compile(MULTILINEREGEX.replace("&V&", entry.getKey().toString()));
            Matcher matcher = pattern.matcher(message);

            if (entry.getValue().get(0) == null) {
                entry.getValue().remove(0);
            }

            while (matcher.find()) {
                StringBuilder itemList = new StringBuilder();
                String found = matcher.group(), format = found.replaceAll("[{}]", "").split("=")[1];

                entry.getValue().forEach((k, v) -> {
                    itemList.append("\n")
                            .append(format.replace(Variable.SHOP.toString(), k).replace(Variable.STATUS.toString(), v));
                });

                message = message.replace(found, itemList.toString());
            }
        }

        for (Tuple<String, String> replace : replacements) {
            message = message.replace(replace.getLeft().toUpperCase(), replace.getRight())
                    .replace(replace.getLeft().toLowerCase(), replace.getRight())
                    .replace(replace.getLeft(), replace.getRight());
        }

        if (isJson) {
            sendMessageDirectJson(player, message);
        } else {
            sendMessageDirect(player, message);
        }
    }
}

