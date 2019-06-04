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

package org.shanerx.tradeshop.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.enumys.Commands;
import org.shanerx.tradeshop.enumys.Message;
import org.shanerx.tradeshop.framework.CustomCommandHandler;
import org.shanerx.tradeshop.framework.TradeCommand;

/**
 * This class is used for calling command methods from CommandRunner
 * as well as doing initial checks for necessary arguments,
 * permissions, and sender type
 **/
public class CommandCaller implements CommandExecutor {

    private TradeShop plugin;

    public CommandCaller(TradeShop instance) {
        plugin = instance;
    }

    private CommandPass cmdPass;
    private Commands command;
    private CommandRunner cmdRnnr;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        cmdPass = new CommandPass(sender, cmd, label, args);
        command = Commands.getType(cmdPass.getArgAt(0));


        if (!cmdPass.hasArgs()) {
            sender.sendMessage(Message.INVALID_ARGUMENTS.getPrefixed());
            return true;

        }

        if (command == null) {
            CustomCommandHandler handler = CustomCommandHandler.getInstance();
            String subcmd = cmdPass.getArgAt(0);

            if (!handler.isAvailable(subcmd)) {
                TradeCommand exec = handler.getExecutable(subcmd);
                if (checkPerm(exec)) {
                    if (!exec.run(args)) {
                        sender.sendMessage(Message.INVALID_ARGUMENTS.getPrefixed());
                    }
                }
                return true;
            }

            sender.sendMessage(Message.INVALID_ARGUMENTS.getPrefixed());
            return true;
        }

        if (!checkPerm()) {
            return true;
        }

        if (command.getMinArgs() > args.length || command.getMaxArgs() < args.length) {
            sender.sendMessage(Message.INVALID_ARGUMENTS.getPrefixed());
            return true;
        }

        if (command.needsPlayer() && !(sender instanceof Player)) {
            sender.sendMessage(Message.PLAYER_ONLY_COMMAND.getPrefixed());
            return true;
        }

        cmdRnnr = new CommandRunner(plugin, cmdPass);

        switch (command) {
            case HELP:
                cmdRnnr.help();
                break;
            case BUGS:
                cmdRnnr.bugs();
                break;
            case SETUP:
                cmdRnnr.setup();
                break;
            case RELOAD:
                cmdRnnr.reload();
                break;
            case ADD_PRODUCT:
                cmdRnnr.addProduct();
                break;
            case ADD_COST:
                cmdRnnr.addCost();
                break;
            case OPEN:
                cmdRnnr.open();
                break;
            case CLOSE:
                cmdRnnr.close();
                break;
            case SWITCH:
                cmdRnnr.switchShop();
                break;
            case WHAT:
                cmdRnnr.what();
                break;
            case WHO:
                cmdRnnr.who();
                break;
            case ADD_MANAGER:
                cmdRnnr.addManager();
                break;
            case REMOVE_USER:
                cmdRnnr.removeUser();
                break;
            case ADD_MEMBER:
                cmdRnnr.addMember();
                break;
            case MULTI:
                cmdRnnr.multi();
                break;
            case SET_PRODUCT:
                cmdRnnr.setProduct();
                break;
            case SET_COST:
                cmdRnnr.setCost();
                break;
            case LIST_PRODUCT:
                cmdRnnr.listProduct();
                break;
            case LIST_COST:
                cmdRnnr.listCost();
                break;
            case REMOVE_PRODUCT:
                cmdRnnr.removeProduct();
                break;
            case REMOVE_COST:
                cmdRnnr.removeCost();
                break;
        }

        return true;
    }


    /**
     * Checks if the sender has the required permission
     *
     * @return true if permission is NONE or sender has permission
     */
    public boolean checkPerm() {
		if (!command.checkPerm(cmdPass.getSender())) {
            cmdPass.getSender().sendMessage(Message.NO_COMMAND_PERMISSION.getPrefixed());
            return false;
        }

        return true;
    }

    /**
     * Checks if the sender has the required permission
     *
     * @return true if permission is NONE or sender has permission
     */
    public boolean checkPerm(TradeCommand exec) {
		if (!(cmdPass.getSender().hasPermission(exec.getPermission()) || exec.getPermission().equalsIgnoreCase("None"))) {
            cmdPass.getSender().sendMessage(Message.NO_COMMAND_PERMISSION.getPrefixed());
            return false;
        }

        return true;
    }
}
