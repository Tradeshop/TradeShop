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

package org.shanerx.tradeshop.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.data.config.Language;
import org.shanerx.tradeshop.utils.Utils;
import org.shanerx.tradeshop.utils.debug.DebugLevels;

public enum Permissions {
	//Available defaultState options are: `true`, `false`, `op`, and `not_op`

	HELP("help", 0, "true"),

	CREATE("create", 0, "true"),

	CREATEI("create.infinite", 1, "op"),

	CREATEBI("create.bi", 0, "true"),

	ADMIN("admin", 1, "op"),

	EDIT("edit", 0, "true"), // non admin perm

	INFO("info", 0, "true"),

	MANAGE_PLUGIN("manage-plugin", 2, "op"),

	PREVENT_TRADE("prevent-trade", -1, "false"),

	TRADE("trade", 0, "true"),

	NONE("", 0, "true");

	private final static TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
	private final Utils utils = new Utils();
	private final String key;
	private final int level;
	private final String defaultState, description;

	Permissions(String key, int level, String defaultState) {
		this.key = key;
		this.level = level;
		this.defaultState = defaultState;
		this.description = utils.PLUGIN.getLanguage().getString(Language.LangSection.PERMISSION, name().toLowerCase().replace("_", "-"), "description");
	}

	@Override
	public String toString() {
		return "tradeshop." + key;
	}

	public String getValue() {
		return this.toString();
	}

	public static void registerPermissions() {
		for (Permissions perm : values()) {
			Permission permission = perm.getPerm();
			Bukkit.getPluginManager().addPermission(permission);
			plugin.getDebugger().log("Permission registered: " + permission, DebugLevels.STARTUP);
		}
	}

	public static boolean hasPermission(Player player, Permissions permission) {
		if (plugin.useInternalPerms()) {
			return plugin.getDataStorage().loadPlayer(player.getUniqueId()).getType() >= permission.getLevel();
		} else {
			return permission.equals(Permissions.NONE) || player.hasPermission(permission.getPerm());
		}
	}


	public static boolean isAdminEnabled(Player player) {
		return hasPermission(player, Permissions.ADMIN) && plugin.getDataStorage().loadPlayer(player.getUniqueId()).isAdminEnabled();
	}

	public Permission getPerm() {
		return new Permission(toString(), getDescription(), getDefaultState());
	}

	public int getLevel() {
		return level;
	}

	public PermissionDefault getDefaultState() {
		return PermissionDefault.valueOf(defaultState);
	}

	public String getDescription() {
		return description;
	}
}