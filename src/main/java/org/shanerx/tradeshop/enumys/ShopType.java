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

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.shanerx.tradeshop.TradeShop;

import java.io.Serializable;

@SuppressWarnings("unused")
public enum ShopType implements Serializable {

	TRADE(Setting.TRADESHOP_HEADER.getString(), Setting.TRADESHOP_EXPLODE.getBoolean(), Permissions.CREATE.getPerm()),

	ITRADE(Setting.ITRADESHOP_HEADER.getString(), Setting.ITRADESHOP_EXPLODE.getBoolean(), Permissions.CREATEI.getPerm()),

	BITRADE(Setting.BITRADESHOP_HEADER.getString(), Setting.BITRADESHOP_EXPLODE.getBoolean(), Permissions.CREATEBI.getPerm());

	private String key;
	private transient static TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
	private transient boolean explode;
	private transient Permission perm;

	ShopType(String key, boolean explode, Permission perm) {
		this.key = key;
		this.explode = explode;
		this.perm = perm;
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

	public static boolean isShop(Sign s) {
		return getType(s) != null;
	}

	public static boolean isShop(Block b) {
		if (b != null && (b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN)) {
			return isShop((Sign) b.getState());
		}

		return false;
	}

    public static ShopType getType(Sign s) {
        String header = ChatColor.stripColor(s.getLine(0));

		if (header.equalsIgnoreCase(TRADE.toHeader())) {
            return TRADE;

		} else if (header.equalsIgnoreCase(ITRADE.toHeader())) {
            return ITRADE;

		} else if (header.equalsIgnoreCase(BITRADE.toHeader())) {
            return BITRADE;
        }

        return null;
    }

	public static ShopRole deserialize(String serialized) {
		ShopRole shopRole = new Gson().fromJson(serialized, ShopRole.class);
		return shopRole;
	}

	public boolean checkPerm(Player pl) {
		return pl.hasPermission(perm);
	}

	public String serialize() {
		return new Gson().toJson(this);
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