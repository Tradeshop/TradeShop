/*
 *
 *                         Copyright (c) 2016-2023
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

package org.shanerx.tradeshop.shop;

import com.bergerkiller.bukkit.common.config.JsonSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.player.Permissions;
import org.shanerx.tradeshop.utils.gsonprocessing.GsonProcessor;


public enum ShopType {

    TRADE(Permissions.CREATE),

    ITRADE(Permissions.CREATEI),

    BITRADE(Permissions.CREATEBI);

    private final static TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
    private final transient Permissions perm;
    private Setting key;

    ShopType(Permissions perm) {
        this.perm = perm;
    }

    public static boolean isShop(Sign s) {
        return getType(s) != null;
    }

    public static boolean isShop(Block b) {
        if (b != null && plugin.getSigns().getSignTypes().contains(b.getType())) {
            return getType((Sign) b.getState()) != null;
        }

        return false;
    }

    public static ShopType getType(Sign s) {
        String header = ChatColor.stripColor(s.getLine(0));

        for (ShopType type : ShopType.values()) {
            if (header.equalsIgnoreCase(type.toHeader()))
                return type;
        }

        return null;
    }

    public static ShopType deserialize(String serialized) {
        try {
            return GsonProcessor.fromJson(serialized, ShopType.class);
        } catch (JsonSerializer.JsonSyntaxException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return getKey().getString();
    }

    public String toHeader() {
        return "[" + getKey().getString() + "]";
    }

    private Setting getKey() {
        if (key == null)
            key = Setting.findSetting(name() + "SHOP_HEADER");
        return key;
    }

    public boolean checkPerm(Player pl) {
        return Permissions.hasPermission(pl, perm);
    }

    public String serialize() {
        return GsonProcessor.toJson(this);
    }

    public boolean isTrade() {
        return this.equals(TRADE);
    }

    public boolean isITrade() {
        return this.equals(ITRADE);
    }

    public boolean isBiTrade() {
        return this.equals(BITRADE);
    }
}