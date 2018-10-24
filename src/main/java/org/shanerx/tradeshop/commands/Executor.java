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

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.Message;
import org.shanerx.tradeshop.Potions;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.Utils;

import java.util.Arrays;
import java.util.Set;

public class Executor extends Utils implements CommandExecutor {

    private TradeShop plugin;

    public Executor(TradeShop instance) {
        plugin = instance;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(colorize(getPrefix() + Message.INVALID_ARGUMENTS));
            return true;

        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("help")) {
                if (!sender.hasPermission(getHelpPerm())) {
                    sender.sendMessage(colorize(getPrefix() + Message.NO_COMMAND_PERMISSION));
                    return true;
                }

                StringBuilder sb = new StringBuilder();
                String msg;

                sb.append("\n");
                sb.append("&2").append(getPluginName()).append(" ").append(getVersion()).append(" by ").append(pdf.getAuthors().get(0)).append(" & ").append(pdf.getAuthors().get(1)).append("\n");
                sb.append("\n");
                sb.append("\n");
                sb.append("&6/tradeshop help &c - Display help message");
                sb.append("\n");

                if (sender.hasPermission(getCreatePerm())) {
                    sb.append("&6/tradeshop setup &c - Display TradeShop setup tutorial");
                    sb.append("\n");
                    sb.append("&6/tradeshop item &c - Shows helpful information on item held by player");
                    sb.append("\n");
                }

                sb.append("&6/tradeshop bugs &c - Report bugs");
                sb.append("\n");
                sb.append("\n");
                sb.append("&6/tradeshop addowner|removeowner [target] - Add another owner to your shop");
                sb.append("\n");
                sb.append("&6/tradeshop addmember|removemember [target] - Add a collaborator to your shop");
                sb.append("\n");

                if (sender.hasPermission(getWhoPerm())) {
                    sb.append("&6/tradeshop who - Shows members shop being looked at");
                    sb.append("\n");
                }

                if (sender.hasPermission(getAdminPerm())) {
                    sb.append("\n");
                    sb.append("&6/tradeshop addItem [item name] &c - Adds custom items to config");
                    sb.append("\n");
                    sb.append("&6/tradeshop removeItem [item name] &c - Removes custom items to config");
                    sb.append("\n");
                    sb.append("&6/tradeshop getCustomItems &c - shows all custom items");
                    sb.append("\n");
                    sb.append("&6/tradeshop reload &c - Reloads plugin configuration files");
                    sb.append("\n");
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
                    sender.sendMessage(colorize(getPrefix() + Message.NO_COMMAND_PERMISSION));
                    return true;
                }
                sender.sendMessage(colorize(getPrefix() + Message.SETUP_HELP));
                return true;

            } else if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission(getAdminPerm())) {
                    sender.sendMessage(colorize(getPrefix() + Message.NO_COMMAND_PERMISSION));
                    return true;

                }
                plugin.reloadConfig();
                sender.sendMessage(colorize(getPrefix() + "&6The configuration files have been reloaded!"));
                return true;

            } else if (args[0].equalsIgnoreCase("item")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Message.PLAYER_ONLY_COMMAND.toString());
                    return true;
                }
                if (!sender.hasPermission(getCreatePerm())) {
                    sender.sendMessage(colorize(getPrefix() + Message.NO_COMMAND_PERMISSION));
                    return true;
                }

                Player pl = (Player) sender;
                ItemStack itm = pl.getInventory().getItemInMainHand();
                if (itm.getType() == null || itm.getType() == Material.AIR) {
                    sender.sendMessage(colorize(getPrefix() + Message.HELD_EMPTY));
                    return true;
                } else {
                    String name,
                            durability,
                            amount;
                    if (Potions.isPotion(itm)) {
                        name = Potions.findPotion(itm);
                        durability = "0";
                        amount = itm.getAmount() + "";
                    } else {
                        name = itm.getType().name();
                        durability = itm.getDurability() + "";
                        amount = itm.getAmount() + "";
                    }
                    sender.sendMessage(colorize(getPrefix() + Message.HELD_ITEM.toString()
                            .replace("{MATERIAL}", name != null ? name : "Unknown")
                            .replace("{DURABILITY}", durability)
                            .replace("{AMOUNT}", amount)));
                    return true;
                }

            } else if (args[0].equalsIgnoreCase("who")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Message.PLAYER_ONLY_COMMAND.toString());
                    return true;

                } else if (!sender.hasPermission(getWhoPerm())) {
                    sender.sendMessage(Message.NO_COMMAND_PERMISSION.toString());
                    return true;
                }

                Player p = (Player) sender;
                StringBuilder owners = new StringBuilder();
                StringBuilder members = new StringBuilder();
                Block b;
                Sign s;

                try {
                    b = p.getTargetBlock(null, plugin.getSettings().getInt("max-edit-distance"));

                    if (b == null || b.getType() == Material.AIR)
                        throw new NoSuchFieldException();

                    if (isSign(b)) {

                        if (!isShopSign(b))
                            throw new NoSuchFieldException();

                        s = (Sign) b.getState();

                        if (isInfiniteTradeShopSign(s.getBlock())) {
                            p.sendMessage(colorize(getPrefix() + Message.WHO_MESSAGE.toString()
                                    .replace("{OWNERS}", plugin.getSettings().getString("itrade-shop-name"))
                                    .replace("{MEMBERS}", "None")));
                            return true;
                        }

                    } else if (plugin.getAllowedInventories().contains(b.getType())) {
                        if (findShopSign(b) == null)
                            throw new NoSuchFieldException();

                        s = findShopSign(b);

                        if (isInfiniteTradeShopSign(s.getBlock())) {
                            p.sendMessage(colorize(getPrefix() + Message.WHO_MESSAGE.toString()
                                    .replace("{OWNERS}", plugin.getSettings().getString("itrade-shop-name"))
                                    .replace("{MEMBERS}", "None")));
                            return true;
                        }
                    } else
                        throw new NoSuchFieldException();

                    if (getShopOwners(s) != null)
                        for (OfflinePlayer pl : getShopOwners(s)) {
                            if (owners.toString().equals(""))
                                owners = new StringBuilder(pl.getName());
                            else
                                owners.append(", ").append(pl.getName());
                        }

                    if (getShopMembers(s) != null)
                        for (OfflinePlayer pl : getShopMembers(s)) {
                            if (members.toString().equals(""))
                                members = new StringBuilder(pl.getName());
                            else
                                members.append(", ").append(pl.getName());
                        }

                    if (owners.toString().equals("")) {
                        owners = new StringBuilder("None");
                    }
                    if (members.toString().equals("")) {
                        members = new StringBuilder("None");
                    }
                    p.sendMessage(colorize(getPrefix() + Message.WHO_MESSAGE.toString()
                            .replace("{OWNERS}", owners.toString())
                            .replace("{MEMBERS}", members.toString())));
                    return true;
                } catch (NoSuchFieldException e) {
                    p.sendMessage(colorize(getPrefix() + Message.NO_SIGHTED_SHOP));
                    return true;
                }

            } else if (args[0].equalsIgnoreCase("getCustomItems")) {
                if (!sender.hasPermission(getAdminPerm())) {
                    sender.sendMessage(Message.NO_COMMAND_PERMISSION.toString());
                    return true;
                }
                Set<String> items = plugin.getCustomItemSet();
                StringBuilder sb = new StringBuilder();
                sender.sendMessage(colorize("&aCurrent custom items:"));
                for (String s : items) {
                    sb.append("-").append(s).append("  ");
                }

                sender.sendMessage(sb.toString());
                return true;

            }

        } else if (args.length == 2) {
            if (Arrays.asList("addowner", "removeowner", "addmember", "removemember").contains(args[0].toLowerCase())) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(colorize(getPrefix() + Message.PLAYER_ONLY_COMMAND));
                    return true;

                }

                Player p = (Player) sender;
                Block b = p.getTargetBlock(null, plugin.getSettings().getInt("max-edit-distance"));
                if (b == null || b.getType() == Material.AIR) {
                    p.sendMessage(colorize(getPrefix() + Message.NO_SIGHTED_SHOP));
                    return true;

                } else if (isShopSign(b)) {
                    b = findShopChest(b);

                } else if (!plugin.getAllowedInventories().contains(b.getType()) || findShopSign(b) == null) {
                    p.sendMessage(colorize(getPrefix() + Message.NO_SIGHTED_SHOP));
                    return true;

                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                b = findShopChest(findShopSign(b).getBlock());

                switch (args[0].toLowerCase()) {

                    case "addowner":
                        if (!addOwner(b, target)) {
                            p.sendMessage(colorize(getPrefix() + Message.UNSUCCESSFUL_SHOP_MEMBERS));
                            return true;
                        }
                        break;
                    case "removeowner":
                        removeOwner(b, target);
                        break;
                    case "addmember":
                        if (!addMember(b, target)) {
                            p.sendMessage(colorize(getPrefix() + Message.UNSUCCESSFUL_SHOP_MEMBERS));
                            return true;
                        }
                        break;
                    case "removemember":
                        removeMember(b, target);
                        break;
                }
                p.sendMessage(colorize(getPrefix() + Message.UPDATED_SHOP_MEMBERS));
                return true;

            } else if (args[0].equalsIgnoreCase("addItem")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(colorize(getPrefix() + Message.PLAYER_ONLY_COMMAND));
                    return true;
                }

                if (!sender.hasPermission(getAdminPerm())) {
                    sender.sendMessage(colorize(getPrefix() + Message.NO_COMMAND_PERMISSION));
                    return true;
                }

                Player p = (Player) sender;
                String name = args[1];

                ItemStack itm = p.getInventory().getItemInMainHand();

                if (itm.getType().equals(Material.AIR) || itm.getType() == null) {
                    p.sendMessage(colorize("&cYou must ne holding an item to create a custom item."));
                    return true;
                }

                plugin.addCustomItem(name, itm);
                plugin.save();
                plugin.reloadConfig();
                p.sendMessage(colorize("&a" + name + " has been added to the custom items."));
                return true;

            } else if (args[0].equalsIgnoreCase("removeItem")) {
                if (!sender.hasPermission(getAdminPerm())) {
                    sender.sendMessage(colorize(getPrefix() + Message.NO_COMMAND_PERMISSION));
                    return true;
                }

                String name = args[1];

                plugin.removeCustomItem(name);
                plugin.save();
                plugin.reloadConfig();
                sender.sendMessage(colorize("&a" + name + " has been removed from the custom items."));
                return true;

            } else if (args[0].equalsIgnoreCase("getCI")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(colorize(getPrefix() + Message.PLAYER_ONLY_COMMAND));
                    return true;
                }

                if (!sender.hasPermission(getAdminPerm())) {
                    sender.sendMessage(colorize(getPrefix() + Message.NO_COMMAND_PERMISSION));
                    return true;
                }

                String name = args[1];

                ItemStack itm = plugin.getCustomItem(name);
                if (itm == null) {
                    sender.sendMessage(colorize("&c" + name + " could not be found."));
                } else {
                    ((Player) sender).getInventory().addItem(itm);
                    sender.sendMessage(colorize("&a" + name + " has been given to you."));
                }
                return true;
            }
        }

        sender.sendMessage(colorize(getPrefix() + Message.INVALID_ARGUMENTS));
        return true;
    }
}
