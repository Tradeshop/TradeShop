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

package org.shanerx.tradeshop.commands;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.Utils;

import java.util.HashSet;
import java.util.Set;

public class Executor extends Utils implements CommandExecutor {

    private TradeShop plugin;

    public Executor(TradeShop instance) {
        plugin = instance;
    }

    @SuppressWarnings("deprecation")
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(colorize(getPrefix() + plugin.getMessages().getString("invalid-arguments")));
            return true;
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("help")) {
                if (!sender.hasPermission(getHelpPerm())) {
                    sender.sendMessage(colorize(getPrefix() + plugin.getMessages().getString("no-command-permission")));
                    return true;
                }

                String[] help = new String[9];

                help[0] = "\n";
                help[1] = "&2" + getPluginName() + " " + getVersion() + " by " + pdf.getAuthors().get(0) + "\n";
                help[2] = "\n";
                help[3] = "\n";
                help[4] = "&6/tradeshop help &c - Display help message\n";

                if (sender.hasPermission(getCreatePerm())) {
                    help[5] = "&6/tradeshop setup &c - Display TradeShop setup tutorial\n";
                    help[6] = "&6/tradeshop item &c - Shows helpful iformation on item held by player\n";
                }

                help[7] = "&6/tradeshop bugs &c - Report bugs\n \n";

                String msg;
                StringBuilder sb = new StringBuilder();
                for (String str : help) {
                    if (str != null)
                        sb.append(str);
                }
                msg = sb.toString();

                sender.sendMessage(colorize(msg));
                return true;
            } else if (args[0].equalsIgnoreCase("bugs")) {
                sender.sendMessage(colorize("\n &2To report any bugs to the author, either send a PM on"
                        + " &cSpigot &2- &egoo.gl/s6Jk23 &2or open an issue on &cGitHub &2-&e goo.gl/X4qqyg\n"));
                return true;

            } else if (args[0].equalsIgnoreCase("setup")) {
                if (!sender.hasPermission(getCreatePerm())) {
                    sender.sendMessage(colorize(getPrefix() + plugin.getMessages().getString("no-command-permission")));
                    return true;
                }
                sender.sendMessage(colorize(getPrefix() + plugin.getMessages().getString("setup-help")));
                return true;

            } else if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission(getAdminPerm())) {
                    sender.sendMessage(colorize(getPrefix() + plugin.getMessages().getString("no-command-permission")));
                    return true;

                }
                plugin.reloadConfig();
                sender.sendMessage(colorize(getPrefix() + "&6The configuration files have been reloaded!"));
                return true;

            } else if (args[0].equalsIgnoreCase("item")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.getMessages().getString("player-only-command"));
                    return true;
                }
                if (!sender.hasPermission(getCreatePerm())) {
                    sender.sendMessage(colorize(getPrefix() + plugin.getMessages().getString("no-command-permission")));
                    return true;
                }

                Player pl = (Player) sender;
                ItemStack itm = null;

                if (plugin.getAboveMC18())
                    itm = pl.getInventory().getItemInMainHand();
                else
                    itm = pl.getInventory().getItemInHand();

                if (itm.getType() == null || itm.getType() == Material.AIR) {
                    sender.sendMessage(colorize(getPrefix() + plugin.getMessages().getString("held-empty")));
                    return true;
                } else {
                    sender.sendMessage(colorize(getPrefix() + plugin.getMessages().getString("held-item"))
                            .replace("{MATERIAL}", itm.getType().name())
                            .replace("{DURABILITY}", itm.getDurability() + "")
                            .replace("{ID}", itm.getTypeId() + "")
                            .replace("{AMOUNT}", itm.getAmount() + ""));
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("break")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.getMessages().getString("player-only-command"));
                    return true;
                }

                if (!sender.hasPermission(getCreatePerm())) {
                    sender.sendMessage(plugin.getMessages().getString("no-command-permission"));
                    return true;
                }

                boolean noShop = false;
                Player p = (Player) sender;
                while (!noShop) {
                    Block b;
                    if (p.getTargetBlock((Set<Material>) null, plugin.getSettings().getInt("max-break-distance")) == null)
                        noShop = true;

                    b = p.getTargetBlock((HashSet<Byte>) null, plugin.getSettings().getInt("max-break-distance"));

                    if (isSign(b)) {
                        if (!isShopSign(b))
                            noShop = true;

                        if (((Sign) b.getState()).getLine(3).equalsIgnoreCase(p.getName()) || p.hasPermission(getAdminPerm())) {
                            b.breakNaturally();
                            return true;
                        } else {
                            plugin.getMessages().getString("no-ts-destroy");
                            return true;
                        }
                    } else if (plugin.getAllowedInventories().contains(b.getType())) {
                        if (findShopSign(b) == null)
                            noShop = true;

                        b = (Block) findShopSign(b).getBlock();

                        if (!isShopSign(b))
                            noShop = true;

                        if (((Sign) b.getState()).getLine(3).equalsIgnoreCase(p.getName()) || p.hasPermission(getAdminPerm())) {
                            b.breakNaturally();
                            return true;
                        } else {
                            plugin.getMessages().getString("no-ts-destroy");
                            return true;
                        }
                    } else
                        noShop = true;
                }
                plugin.getMessages().getString("no-sighted-shop");
                return true;
            }
        }
        sender.sendMessage(colorize(getPrefix() + plugin.getMessages().getString("invalid-arguments")));
        return true;
    }
}
