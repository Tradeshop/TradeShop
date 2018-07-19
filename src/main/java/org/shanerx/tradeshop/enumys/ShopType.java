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

package org.shanerx.tradeshop.enumys;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.shanerx.tradeshop.TradeShop;

@SuppressWarnings("unused")
public enum ShopType {

	TRADE(Setting.TRADESHOP_HEADER.getString(), Setting.TRADESHOP_EXPLODE.getBoolean()),

	ITRADE(Setting.ITRADESHOP_HEADER.getString(), Setting.ITRADESHOP_EXPLODE.getBoolean()),

	BITRADE(Setting.BITRADESHOP_HEADER.getString(), Setting.BITRADESHOP_EXPLODE.getBoolean());

	private String key;
	private boolean explode;
	private static TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");

	ShopType(String key, boolean explode) {
		this.key = key;
		this.explode = explode;
	}

	@Override
	public String toString() {
		return key;
	}

	public String toHeader() {
		return "[" + key + "]";
	}

	public boolean protectFromExplosion() {
		return !explode;
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