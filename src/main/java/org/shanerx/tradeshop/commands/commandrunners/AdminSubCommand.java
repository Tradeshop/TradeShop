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

package org.shanerx.tradeshop.commands.commandrunners;

import org.bukkit.Bukkit;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.commands.CommandPass;
import org.shanerx.tradeshop.data.config.Message;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.data.config.Variable;
import org.shanerx.tradeshop.data.storage.DataType;
import org.shanerx.tradeshop.framework.events.TradeShopReloadEvent;
import org.shanerx.tradeshop.player.PlayerSetting;
import org.shanerx.tradeshop.utils.debug.DebugLevels;
import org.shanerx.tradeshop.utils.objects.Tuple;

/**
 * Implementation of CommandRunner for commands that are used for administration purposes
 *
 * @since 2.6.0
 */
public class AdminSubCommand extends SubCommand {

    public AdminSubCommand(TradeShop instance, CommandPass command) {
        super(instance, command);
    }

    /**
     * Reloads the plugin and sends success message
     */
    public void reload() {
        plugin.getLanguage().reload();
        plugin.getSettingManager().reload();
        plugin.getMessageManager().reload();
        plugin.getListManager().reload();
        plugin.getDebugger().reload();
        try {
            plugin.getDataStorage().reload(DataType.valueOf(Setting.DATA_STORAGE_TYPE.getString().toUpperCase()));
        } catch (IllegalArgumentException iae) {
            plugin.getDebugger().log("Config value for data storage set to an invalid value: " + Setting.DATA_STORAGE_TYPE.getString(), DebugLevels.DATA_ERROR);
            plugin.getDebugger().log("TradeShop will now disable...", DebugLevels.DATA_ERROR);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        command.sendMessage(Setting.MESSAGE_PREFIX.getString().trim() + "&6The configuration files have been reloaded!");
        Bukkit.getPluginManager().callEvent(new TradeShopReloadEvent(plugin, command.getSender()));
    }

    /**
     * Changes the players with the ADMIN permission to toggle whether it is enabled for them
     */
    public void toggleAdmin() {
        PlayerSetting playerSetting = plugin.getDataStorage().loadPlayer(command.getPlayerSender().getUniqueId());

        playerSetting.setAdminEnabled(!playerSetting.isAdminEnabled());
        plugin.getDataStorage().savePlayer(playerSetting);

        Message.ADMIN_TOGGLED.sendMessage(command.getPlayerSender(), new Tuple<>(Variable.STATE.toString(), playerSetting.isAdminEnabled() ? "enabled" : "disabled"));
    }

    /**
     * Shows players their current admin mode or changes with optional variable
     */
    public void admin() {
        PlayerSetting playerSetting = plugin.getDataStorage().loadPlayer(command.getPlayerSender().getUniqueId());
        boolean initialValue = playerSetting.isAdminEnabled();

        if (command.hasArgAt(1)) {

            switch (command.getArgAt(1).toLowerCase()) {
                case "true":
                case "t":
                    playerSetting.setAdminEnabled(true);
                    break;
                case "false":
                case "f":
                    playerSetting.setAdminEnabled(false);
                    break;
            }

            if (initialValue != playerSetting.isAdminEnabled())
                plugin.getDataStorage().savePlayer(playerSetting);
        }

        Message.ADMIN_TOGGLED.sendMessage(command.getPlayerSender(), new Tuple<>(Variable.STATE.toString(), playerSetting.isAdminEnabled() ? "enabled" : "disabled"));
    }
}
