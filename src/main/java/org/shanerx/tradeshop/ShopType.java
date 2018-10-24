/*
 *     Copyright (c) 2016-2017 SparklingComet @ http://shanerx.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * NOTICE: All modifications made by others to the source code belong
 * to the respective contributor. No contributor should be held liable for
 * any damages of any kind, whether be material or moral, which were
 * caused by their contribution(s) to the project. See the full License for more information
 */

package org.shanerx.tradeshop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;

@SuppressWarnings("unused")
public enum ShopType {

    TRADE("[" + getTrade() + "]"),

    ITRADE("[" + getITrade() + "]"),

    BITRADE("[" + getBiTrade() + "]");

    private static TradeShop plugin = null;
    private String header;

    ShopType(String s) {
        header = s;
    }

    private static void setPlugin() {
        if (plugin == null) {
            plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
        }
    }

    private static TradeShop getPlugin() {
        setPlugin();
        return plugin;
    }

    private static String getTrade() {
        return getPlugin().getSettings().getString("tradeshop-name");
    }

    private static String getITrade() {
        return getPlugin().getSettings().getString("itradeshop-name");
    }

    private static String getBiTrade() {
        return getPlugin().getSettings().getString("bitradeshop-name");
    }

    public static ShopType getType(Sign s) {
        String header = ChatColor.stripColor(s.getLine(0));

        if (header.equalsIgnoreCase(TRADE.header)) {
            return TRADE;

        } else if (header.equalsIgnoreCase(ITRADE.header)) {
            return ITRADE;

        } else if (header.equalsIgnoreCase(BITRADE.header)) {
            return BITRADE;
        }

        return null;
    }

    public String header() {
        return header;
    }

    @Override
    public String toString() {
        return header;
    }

    public boolean isProtectedFromExplosions() {
        switch (this) {
            case TRADE:
                return !getPlugin().getSettings().getBoolean("explode.trade");

            case ITRADE:
                return !getPlugin().getSettings().getBoolean("explode.itrade");

            case BITRADE:
                return !getPlugin().getSettings().getBoolean("explode.bitrade");

            default:
                return false;
        }
    }
}