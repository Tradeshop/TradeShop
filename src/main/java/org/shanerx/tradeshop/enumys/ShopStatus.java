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

import org.bukkit.ChatColor;

public enum ShopStatus {

    OPEN("&a<Open>", Setting.SHOP_OPEN_STATUS, true),
    CLOSED("&c<Closed>", Setting.SHOP_CLOSED_STATUS, false),
    INCOMPLETE("&c<Incomplete>", Setting.SHOP_INCOMPLETE_STATUS, false),
    OUT_OF_STOCK("&c<Out Of Stock>", Setting.SHOP_OUTOFSTOCK_STATUS, false);

	private static final char COLOUR_CHAR = '&';
    private String label;
    private boolean tradingAllowed;
    private Setting labelEnum;

    ShopStatus(String label, Setting labelEnum, boolean tradingAllowed) {
        this.label = label;
        this.labelEnum = labelEnum;
        this.tradingAllowed = tradingAllowed;
	}

	public static String colorize(String x) {
		return ChatColor.translateAlternateColorCodes(COLOUR_CHAR, x);
	}

	@Override
	public String toString() {
        return name();
	}

	public String getLine() {
        return colorize(labelEnum.getString().isEmpty() ? label : labelEnum.getString());
    }

    public boolean isTradingAllowed() {
        return tradingAllowed;
    }

}
