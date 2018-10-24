package org.shanerx.tradeshop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

@SuppressWarnings("unused")
public enum Message {

    AMOUNT_NOT_NUM,
    BUY_FAILED_SIGN,
    CONFIRM_TRADE,
    EMPTY_TS_ON_SETUP,
    FULL_AMOUNT,
    HELD_EMPTY,
    HELD_ITEM,
    ILLEGAL_ITEM,
    INSUFFICIENT_ITEMS,
    INVALID_ARGUMENTS,
    INVALID_SIGN,
    MISSING_INFO,
    MISSING_ITEM,
    MISSING_SHOP,
    NO_CHEST,
    NO_COMMAND_PERMISSION,
    NO_SIGHTED_SHOP,
    NO_TS_CREATE_PERMISSION,
    NO_TS_DESTROY,
    NO_TS_OPEN,
    NOT_OWNER,
    ON_TRADE,
    PLAYER_FULL,
    PLAYER_ONLY_COMMAND,
    SELF_OWNED,
    SETUP_HELP,
    SHOP_EMPTY,
    SHOP_FULL,
    SHOP_FULL_AMOUNT,
    SUCCESSFUL_SETUP,
    UNSUCCESSFUL_SHOP_MEMBERS,
    UPDATED_SHOP_MEMBERS,
    WHO_MESSAGE;

    public static final char COLOUR_CHAR = '&';
    private static TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");

    public static String colour(String x) {
        return ChatColor.translateAlternateColorCodes(COLOUR_CHAR, x);
    }

    @Override
    public String toString() {
        return colour(
                plugin.getMessages().getString(
                        name()
                                .toLowerCase()
                                .replace("_", "-")
                ).replace("%header%",
                        plugin.getSettings().getString("tradeshop-name"))
        );
    }
}
