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

import java.util.Arrays;
import java.util.stream.Collectors;

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
    BITRADE_SHOP_OPTIONS("bitrade-shop-options", "BiTrade Shop Options"),
    ILLEGAL_ITEMS_OPTIONS("illegal-items-options", "", "", "Valid Types: " + Arrays.stream(ListType.values()).map(Enum::toString).collect(Collectors.joining(", "))),
    GLOBAL_ILLEGAL_ITEMS(ILLEGAL_ITEMS_OPTIONS, "global-illegal-items", "", "List for illegal items for both Cost and Product", ""),
    COST_ILLEGAL_ITEMS(ILLEGAL_ITEMS_OPTIONS, "cost-illegal-items", "", "List for illegal items for only Cost items", ""),
    PRODUCT_ILLEGAL_ITEMS(ILLEGAL_ITEMS_OPTIONS, "product-illegal-items", "", "List for illegal items for only Product items", "");

    private final String key;
    private final String sectionHeader;
    private String value_lead = "",
            postComment = "",
            preComment = "";
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

    SettingSectionKeys(String key, String sectionHeader, String preComment, String postComment) {
        this.key = key;
        this.sectionHeader = sectionHeader;
        this.postComment = postComment;
        this.preComment = preComment;
        if (!key.isEmpty())
            this.value_lead = parent.value_lead + "  ";
    }

    SettingSectionKeys(SettingSectionKeys parent, String key, String sectionHeader, String preComment, String postComment) {
        this.key = key;
        this.sectionHeader = sectionHeader;
        this.parent = parent;
        this.postComment = postComment;
        this.preComment = preComment;
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
        if (!key.isEmpty()) {
            StringBuilder header = new StringBuilder();

            //Create Header if one exists
            if (!sectionHeader.isEmpty()) {

                // Create First line of Header and count length for 2nd line
                header.append("|    ").append(sectionHeader).append("    |");
                int line1Length = header.length();

                // Add Comment symbols and new lines
                header.insert(0, "# ").append("\n").append("# ");

                // Create second line
                while (line1Length > 0) {
                    header.append("^");
                    line1Length--;
                }
            }

            // Create Json Section text line
            header.append("\n").append(getFileText()).append(":\n");

            // Add optional comment
            if (!postComment.isEmpty()) {
                header.append("# ").append(postComment).append("\n");
            }

            return header.toString();
        }

        return "";
    }

    public String getFileText() {
        return parent != null ? parent.value_lead + key : key;
    }
}
