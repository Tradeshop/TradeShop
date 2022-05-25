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

package org.shanerx.tradeshop.data.config;

import org.bukkit.Bukkit;
import org.shanerx.tradeshop.TradeShop;

import java.util.Objects;

public enum SettingSection {

    NONE(0, ""),
    SYSTEM_OPTIONS(1, "system-options"),
    LANGUAGE_OPTIONS(2, "language-options"),
    GLOBAL_OPTIONS(3, "global-options"),
    GLOBAL_MULTI_TRADE(0, GLOBAL_OPTIONS, "multi-trade"),
    SHOP_OPTIONS(4, "shop-options"),
    TRADE_SHOP_OPTIONS(6, "trade-shop-options"),
    ITRADE_SHOP_OPTIONS(7, "itrade-shop-options"),
    BITRADE_SHOP_OPTIONS(8, "bitrade-shop-options"),
    ILLEGAL_ITEM_OPTIONS(9, "illegal-item-options"),
    GLOBAL_ILLEGAL_ITEMS(0, ILLEGAL_ITEM_OPTIONS, "global-illegal-items"),
    COST_ILLEGAL_ITEMS(1, ILLEGAL_ITEM_OPTIONS, "cost-illegal-items"),
    PRODUCT_ILLEGAL_ITEMS(2, ILLEGAL_ITEM_OPTIONS, "product-illegal-items");

    public static final TradeShop PLUGIN = Objects.requireNonNull((TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop"));

    private final String key, lineLead;
    private final int weight; // Lower Weights are printed first
    private final SettingSection parent;

    SettingSection(int weight, String key) {
        this(weight, null, key);
    }

    SettingSection(int weight, SettingSection parent, String key) {
        this.weight = weight;
        this.key = key;
        this.parent = parent;
        this.lineLead = !key.isEmpty() ? parent != null ? parent.lineLead + "  " : "  " : "";
    }

    public String getKey() {
        return key;
    }

    public String getPath() {
        return !key.isEmpty() ? (hasParent() ? parent.getPath() + "." + key : key) : "";
    }

    public String getLineLead() {
        return hasParent() ? parent.getSectionLead() : "";
    }

    public String getSectionLead() {
        return (hasParent() ? parent.getSectionLead() : "") + lineLead;
    }

    public SettingSection getParent() {
        return parent;
    }

    public boolean hasParent() {
        return parent != null;
    }

    public String getPostComment() {
        return PLUGIN.getLanguage().getPostComment(Language.LangSection.SETTING_SECTION, name().toLowerCase().replace("_", "-"));
    }

    public String getPreComment() {
        return PLUGIN.getLanguage().getPreComment(Language.LangSection.SETTING_SECTION, name().toLowerCase().replace("_", "-"));
    }

    public String getSectionHeader() {
        return PLUGIN.getLanguage().getHeader(Language.LangSection.SETTING_SECTION, name().toLowerCase().replace("_", "-"));
    }

    public String getFileString() {
        StringBuilder sectionHeader = new StringBuilder();

        // If Section has a key add section to file
        if (!getPath().isEmpty()) {

            // If Section has a header format it
            if (!getSectionHeader().isEmpty()) {

                // First line of Section Header
                sectionHeader.append(getLineLead()).append("# |    ").append(getSectionHeader()).append("    |");

                // Second line of Section Header
                sectionHeader.append("\n").append(getLineLead()).append("# ");

                // Create `^` line
                // Length should be length of header text + `|    ` x2 for each side(10 total)
                for (int i = getSectionHeader().length() + 10; i > 0; i--) {
                    sectionHeader.append("^");
                }

                sectionHeader.append("\n");

            }

            // If Section has a Pre Comment add it
            if (!getPreComment().isEmpty()) {
                sectionHeader.append(getLineLead()).append("# ").append(PLUGIN.getSettingManager().fixCommentNewLines(getLineLead(), getPreComment())).append("\n");
            }

            // Add Sections Key line
            sectionHeader.append(getLineLead()).append(getKey()).append(": ").append("\n");

            // If Section has a Post Comment add it
            if (!getPostComment().isEmpty()) {
                if (getPostComment().equals(" ") || getPostComment().equals("\n"))
                    sectionHeader.append(getPostComment()).append("\n");
                else
                    sectionHeader.append(getSectionLead()).append("# ").append(PLUGIN.getSettingManager().fixCommentNewLines(getSectionLead(), getPostComment())).append("\n");
            }
        }

        return sectionHeader.toString();
    }

    public int getWeight() {
        return weight;
    }
}
