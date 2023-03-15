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

import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.commands.CommandPass;
import org.shanerx.tradeshop.commands.Commands;
import org.shanerx.tradeshop.data.config.Message;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.data.config.Variable;
import org.shanerx.tradeshop.player.PermStatus;
import org.shanerx.tradeshop.utils.objects.Tuple;

/**
 * Implementation of CommandRunner for plugin commands that return basic text/info to the user
 *
 * @since 2.6.0
 */
public class BasicTextCommand extends CommandRunner {

    public BasicTextCommand(TradeShop instance, CommandPass command) {
        super(instance, command);
    }

    /**
     * Builds and sends the sender the help message
     */
    public void help() {
        if (command.argsSize() == 2) {
            usage(command.getArgAt(1));
            return;
        }

        StringBuilder sb = new StringBuilder();

        sb.append("\n&2")
                .append(getPluginName())
                .append(" ")
                .append(getVersion())
                .append(" by ").append(pdf.getAuthors().get(0)).append(" & ").append(pdf.getAuthors().get(1))
                .append("\n\n&b/tradeshop &f &f Display help message\n");

        for (Commands c : Commands.values()) {
            if (c.checkPerm(command.getSender()) == PermStatus.GOOD) {
                sb.append(plugin.getMessageManager().colour(String.format("&b/ts %s  &f %s\n", c.getFirstName(), c.getDescription())));
            }
        }

        sb.append("\n ");
        command.sendMessage(colorize(sb.toString()));
    }

    /**
     * Retrieves and sends usage string to player for requested sub-command or error message if command cannot be found
     *
     * @param subcmd string we should search for a sub-command
     */
    public void usage(String subcmd) {
        Commands cmd = Commands.getType(subcmd);
        if (cmd == null) {
            command.sendMessage(plugin.getMessageManager().colour(String.format("&4Cannot find usages for &c%s&r", subcmd)));
            return;
        }
        command.sendMessage(plugin.getMessageManager().colour(String.format("&6Showing help for &c%s&r\n&bUsage:&e %s \n&bAliases: %s\n&bDescription:&e %s", subcmd, cmd.getUsage(), cmd.getAliases(), cmd.getDescription())));
    }

    /**
     * Sends the sender the bug message
     */
    public void bugs() {
        command.sendMessage("\n&a[&eTradeShop&a] \n&2To report any bugs to the author, either send a PM on &cSpigot &2- &egoo.gl/s6Jk23 &2or open an issue on &cGitHub &2-&e goo.gl/X4qqyg\n");
    }

    /**
     * Sends the sender the setup message
     */
    public void setup() {
        Message.SETUP_HELP.sendMessage(command.getPlayerSender(), new Tuple<>(Variable.HEADER.toString(), Setting.TRADESHOP_HEADER.getString()));
    }
}
