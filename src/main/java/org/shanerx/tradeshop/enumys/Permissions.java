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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.shanerx.tradeshop.TradeShop;

public enum Permissions {

	HELP("help", 0),

	CREATE("create", 0),

	CREATEI("create.infinite", 1),

	CREATEBI("create.bi", 0),

	ADMIN("admin", 1),

	EDIT("edit", 0), // non admin perm

	INFO("info", 0),

	MANAGE_PLUGIN("manage-plugin", 2),

	NONE("", 0);

	private final static TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
	private final String key;
	private final int level;

	Permissions(String key, int level) {
		this.key = key;
		this.level = level;
	}

	@Override
	public String toString() {
		return "tradeshop." + key;
	}

	public String getValue() {
		return this.toString();
	}

	public Permission getPerm() {
		return new Permission(toString());
	}

	public static boolean hasPermission(Player player, Permissions permission) {
		if (plugin.useInternalPerms()) {
			return plugin.getDataStorage().loadPlayer(player.getUniqueId()).getType() >= permission.getLevel();
		} else {
			return permission.equals(Permissions.NONE) || player.hasPermission(permission.getPerm());
		}
	}

	public int getLevel() {
		return level;
	}
}