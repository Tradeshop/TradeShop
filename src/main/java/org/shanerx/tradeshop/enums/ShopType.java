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

package org.shanerx.tradeshop.enums;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.shanerx.tradeshop.TradeShop;

@SuppressWarnings("unused")
public enum ShopType {

	TRADE("trade"),

    ITRADE("[" + getITrade() + "]"),

    BITRADE("[" + getBiTrade() + "]");

	private String key;
	private static TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");

	ShopType(String key) {
		this.key = key;
	}

	public String stripped() {
		return Setting.findSetting(key + "shop-name").getString();
	}

	@Override
	public String toString() {
		return "[" + stripped() + "]";
	}

    private static String getITrade() {
        return plugin.getSettings().getString("itradeshop-name");
    }

    private static String getBiTrade() {
        return plugin.getSettings().getString("bitradeshop-name");
    }

		if (check.equalsIgnoreCase(TRADE.key)) {
			return TRADE;

		} else if (check.equalsIgnoreCase(ITRADE.key)) {
			return ITRADE;

    @Override
    public String toString() {
        return header;
    }

    public boolean isProtectedFromExplosions() {
        switch (this) {
            case TRADE:
                return !plugin.getSettings().getBoolean("explode.trade");

            case ITRADE:
                return !plugin.getSettings().getBoolean("explode.itrade");

            case BITRADE:
                return !plugin.getSettings().getBoolean("explode.bitrade");

            default:
                return false;
        }
    }

    public static ShopType getType(Sign s) {
        String header = ChatColor.stripColor(s.getLine(0));

        if (header.equalsIgnoreCase(TRADE.toString())) {
            return TRADE;

        } else if (header.equalsIgnoreCase(ITRADE.toString())) {
            return ITRADE;

        } else if (header.equalsIgnoreCase(BITRADE.toString())) {
            return BITRADE;
        }

        return null;
    }
}