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

package org.shanerx.tradeshop.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.data.config.Language;
import org.shanerx.tradeshop.utils.Utils;
import org.shanerx.tradeshop.utils.debug.DebugLevels;

public enum Permissions {

    HELP("help", PermissionDefault.TRUE),

    CREATE("create", PermissionDefault.TRUE),

    CREATEI("create.infinite", PermissionDefault.OP),

    CREATEBI("create.bi", PermissionDefault.TRUE),

    ADMIN("admin", PermissionDefault.OP),

    EDIT("edit", PermissionDefault.TRUE), // non admin perm

    INFO("info", PermissionDefault.TRUE),

    FIND("info.find", PermissionDefault.TRUE),

    MANAGE_PLUGIN("manage-plugin", PermissionDefault.OP),

    TRADE("trade", PermissionDefault.TRUE),

    NONE("", PermissionDefault.TRUE);

    private final static TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
    private final transient Utils utils = new Utils();
    private final String key;
    private final PermissionDefault defaultState;
    private final String description;

    Permissions(String key, PermissionDefault defaultState) {
        this.key = key;
        this.defaultState = defaultState;
        this.description = TradeShop.getPlugin().getLanguage().getString(Language.LangSection.PERMISSION, Language.LangSubSection.VALUES, name().toLowerCase().replace("_", "-"), "description");
    }

    public static void registerPermissions() {
        PluginManager pm = Bukkit.getPluginManager();
        for (Permissions perm : values()) {
            if (!perm.equals(Permissions.NONE)) {
                Permission permission = perm.getPerm();
                pm.addPermission(permission);
                plugin.getDebugger().log("Permission registered: " + permission.getName() + " | State: " + permission.getDefault(), DebugLevels.STARTUP);
            }
        }

        StringBuilder str = new StringBuilder();
        str.append("defaultPermissions: \n");
        Bukkit.getPluginManager().getDefaultPermissions(false).forEach((perm) -> str.append(perm.getName()).append("\n"));
        plugin.getDebugger().log(str.toString(), DebugLevels.STARTUP);
    }

    public static boolean hasPermission(Player player, Permissions permission) {
        return permission.equals(Permissions.NONE) || player.hasPermission(permission.getPerm());
    }

    public static boolean isAdminEnabled(Player player) {
        return hasPermission(player, Permissions.ADMIN) && plugin.getDataStorage().loadPlayer(player.getUniqueId()).isAdminEnabled();
    }

    @Override
    public String toString() {
        return "tradeshop." + key;
    }

    public String getValue() {
        return this.toString();
    }

    public Permission getPerm() {
        return new Permission(toString(), description, defaultState);
    }
}