package org.shanerx.tradeshop;

import org.bukkit.ChatColor;

public enum Message {
    
    INVALID_ARGUMENTS,
    NO_COMMAND_PERMISSION,
    SETUP_HELP,
    NO_TS_CREATE_PERMISSION,
    NO_CHEST,
    INVALID_SIGN,
    NO_TS_DESTROY,
    SUCCESSFUL_SETUP,
    NO_TS_OPEN,
    EMPTY_TS_ON_SETUP,
    ON_TRADE,
    INSUFFICIENT_ITEMS,
    SHOP_FULL_AMOUNT,
    FULL_AMOUNT,
    SHOP_EMPTY,
    SHOP_FULL,
    PLAYER_FULL,
    CONFIRM_TRADE,
    HELD_ITEM,
    HELD_EMPTY,
    PLAYER_ONLY_COMMAND,
    MISSING_SHOP,
    NO_SIGHTED_SHOP,
    UPDATE_SHOP_MEMBERS,
    UNSUCCESSFUL_SHOP_MEMBERS,
    WHO_MESSAGE,
    SELF_OWNED,
    NOT_OWNER,
    ILLEGAL_ITEM,
    MISSING_ITEM,
    MISSING_INFO,
    AMOUNT_NOT_NUM,
    BUY_FAILED_SIGN,
    ;
    
    @Override
    public String toString() {
        return colour(
            plugin.getMessages().get(
                name().toLowerCase().replace("_", "-")
            )
        );
    }
    
    private static TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
    public static final char COLOUR_CHAR = '&';
    
    public static String colour(String x) {
        return ChatColor.translateAlternateColorCodes(COLOUR_CHAR, x);
    }
}
