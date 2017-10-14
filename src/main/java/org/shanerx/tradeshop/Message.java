package org.shanerx.tradeshop;

import org.bukkit.ChatColor;

public enum Message {
    
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
