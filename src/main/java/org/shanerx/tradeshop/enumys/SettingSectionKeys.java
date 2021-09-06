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

public enum SettingSectionKeys {

    NONE("", ""),
    SYSTEM_OPTIONS("system-options", "System Options"),
    LANGUAGE_OPTIONS("language-options", "Language Options"),
    GLOBAL_OPTIONS("global-options", "Global Options"),
    GLOBAL_MULTI_TRADE(GLOBAL_OPTIONS, "multi-trade", ""),
    SHOP_OPTIONS("shop-options", "Shop Options"),
    ITEM_OPTIONS("item-options", "Item-specific Options"),
    TRADE_SHOP_OPTIONS("trade-shop-options", "Trade Shop Options"),
    ITRADE_SHOP_OPTIONS("itrade-shop-options", "ITrade Shop Options"),
    BITRADE_SHOP_OPTIONS("bitrade-shop-options", "BiTrade Shop Options");

    private final String key;
    private final String sectionHeader;
    private String value_lead = "";
    private SettingSectionKeys parent;

    SettingSectionKeys(String key, String sectionHeader) {
        this.key = key;
        this.sectionHeader = sectionHeader;
        if (!key.isEmpty())
            this.value_lead = "  ";
    }

    SettingSectionKeys(SettingSectionKeys parent, String key, String sectionHeader) {
        this.key = key;
        this.sectionHeader = sectionHeader;
        this.parent = parent;
        if (!key.isEmpty())
            this.value_lead = parent.value_lead + "  ";
    }

    public String getKey() {
        return !key.isEmpty() ? (parent != null ? parent.getKey() + "." + key + "." : key + ".") : "";
    }

    public String getValueLead() {
        return value_lead;
    }

    public String getFormattedHeader() {
        if (!sectionHeader.isEmpty() && !key.isEmpty()) {
            StringBuilder header = new StringBuilder();
            header.append("|    ").append(sectionHeader).append("    |");

            int line1Length = header.length();

            header.insert(0, "# ").append("\n").append("# ");

            while (line1Length > 0) {
                header.append("^");
                line1Length--;
            }

            header.append("\n").append(getFileText()).append(":\n");

            return header.toString();
        } else if (sectionHeader.isEmpty() && !key.isEmpty()) {
            StringBuilder header = new StringBuilder();

            header.append(getFileText()).append(":\n");

            return header.toString();
        }

        return "";
    }

    public String getFileText() {
        return parent != null ? parent.value_lead + key : key;
    }
}
