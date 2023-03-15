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
import org.shanerx.tradeshop.player.Permissions;
import org.shanerx.tradeshop.player.PlayerSetting;
import org.shanerx.tradeshop.utils.objects.Tuple;

public class GeneralPlayerCommand extends CommandRunner {

    public GeneralPlayerCommand(TradeShop instance, CommandPass command) {
        super(instance, command);
    }

    /**
     * Changes the players trade multiplier for current login
     */
    public void multi() {
        if (!Setting.ALLOW_MULTI_TRADE.getBoolean()) {
            Message.FEATURE_DISABLED.sendMessage(command.getPlayerSender());
            return;
        }

        PlayerSetting playerSetting = plugin.getDataStorage().loadPlayer(command.getPlayerSender().getUniqueId());

        if (command.argsSize() == 1) {
            Message.MULTI_AMOUNT.sendMessage(command.getPlayerSender(), new Tuple<>(Variable.AMOUNT.toString(), String.valueOf(playerSetting.getMulti())));
        } else {
            int amount = Setting.MULTI_TRADE_DEFAULT.getInt();

            if (isInt(command.getArgAt(1)))
                amount = Integer.parseInt(command.getArgAt(1));

            amount = Math.min(Math.max(2, amount), Setting.MULTI_TRADE_MAX.getInt());

            playerSetting.setMulti(amount);
            plugin.getDataStorage().savePlayer(playerSetting);

            Message.MULTI_UPDATE.sendMessage(command.getPlayerSender(), new Tuple<>(Variable.AMOUNT.toString(), String.valueOf(amount)));
        }
    }

    /**
     * Toggles the join status message for the player
     */
    public void toggleStatus() {
        if (!Setting.ALLOW_TOGGLE_STATUS.getBoolean()) {
            Message.FEATURE_DISABLED.sendMessage(command.getPlayerSender());
            return;
        }

        PlayerSetting playerSetting = plugin.getDataStorage().loadPlayer(command.getPlayerSender().getUniqueId());
        playerSetting.setShowInvolvedStatus(!playerSetting.showInvolvedStatus());
        plugin.getDataStorage().savePlayer(playerSetting);
        Message.TOGGLED_STATUS.sendMessage(command.getPlayerSender(), new Tuple<>(Variable.STATUS.toString(), playerSetting.showInvolvedStatus() ? "on" : "off"));
    }

    /**
     * Shows the player the status of all shops they are involved with or the specified player is involved with
     */
    public void status() {
        if (command.hasArgAt(1)) {
            if (!Permissions.isAdminEnabled(command.getPlayerSender())) {
                Message.NO_COMMAND_PERMISSION.sendMessage(command.getPlayerSender());
                return;
            }
            if (Bukkit.getOfflinePlayer(command.getArgAt(1)).hasPlayedBefore()) {
                plugin.getDataStorage().loadPlayer(Bukkit.getOfflinePlayer(command.getArgAt(1)).getUniqueId())
                        .getInvolvedStatusesInventory().show(command.getPlayerSender().getPlayer());
            } else {
                Message.PLAYER_NOT_FOUND.sendMessage(command.getPlayerSender());
            }
        } else {
            plugin.getDataStorage().loadPlayer(command.getPlayerSender().getUniqueId()).getInvolvedStatusesInventory().show(command.getPlayerSender().getPlayer());
        }
    }
}
