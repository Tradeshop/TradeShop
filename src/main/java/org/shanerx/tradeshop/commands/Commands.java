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

package org.shanerx.tradeshop.commands;

import com.google.common.collect.Lists;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.shanerx.tradeshop.player.PermStatus;
import org.shanerx.tradeshop.player.Permissions;

import java.util.List;

/**
 * Enum holding all commands as well as aliases,
 * required permissions, minimum and maximum
 * arguments, and whether the command must be
 * run by a player
 **/

public enum Commands {

    // Shop other management commands
    OPEN(Lists.newArrayList("open"), Permissions.NONE, 1, 1, true, "Open shop", "/tradeshop $cmd$"),
    CLOSE(Lists.newArrayList("close"), Permissions.NONE, 1, 1, true, "Close shop", "/tradeshop $cmd$"),
    SWITCH(Lists.newArrayList("switch"), Permissions.EDIT, 1, 1, true, "Switch shop type", "/tradeshop $cmd$"),
    EDIT(Lists.newArrayList("edit", "e"), Permissions.EDIT, 1, 1, true, "Opens Edit GUI for shop", "/tradeshop $cmd$"),
    CREATE_TRADE(Lists.newArrayList("createtrade", "create", "cr"), Permissions.CREATE, 1, 1, true, "Create regular shop", "/tradeshop $cmd$"),
    CREATE_BITRADE(Lists.newArrayList("createbitrade", "createbi", "crbi"), Permissions.CREATEBI, 1, 1, true, "Create BiTrade shop", "/tradeshop $cmd$"),
    CREATE_ITRADE(Lists.newArrayList("createitrade", "createi", "cri"), Permissions.CREATEI, 1, 1, true, "Create iTrade shop", "/tradeshop $cmd$"),

    // Shop user management commands
    REMOVE_USER(Lists.newArrayList("removeUser", "removeManager", "removeMember"), Permissions.NONE, 2, 3, true, "Remove user from shop", "/tradeshop $cmd$ <Name> [all shops]"),
    ADD_MANAGER(Lists.newArrayList("addManager"), Permissions.NONE, 2, 3, true, "Add manager to shop", "/tradeshop $cmd$ <name> [all shops]"),
    ADD_MEMBER(Lists.newArrayList("addMember"), Permissions.NONE, 2, 3, true, "Add member to shop", "/tradeshop $cmd$ <name> [all shops]"),
    SET_MANAGER(Lists.newArrayList("setManager"), Permissions.NONE, 2, 3, true, "Sets a player to Manager on a shop", "/tradeshop $cmd$ <name> [all shops]"),
    SET_MEMBER(Lists.newArrayList("setMember"), Permissions.NONE, 2, 3, true, "Sets a player to Member on a shop", "/tradeshop $cmd$ <name> [all shops]"),

    // Shop item management commands
    ADD_PRODUCT(Lists.newArrayList("addProduct"), Permissions.NONE, 1, 3, true, "Add product to shop", "/tradeshop $cmd$ [Amount] [Material]"),
    ADD_COST(Lists.newArrayList("addCost"), Permissions.NONE, 1, 3, true, "Add cost to shop", "/tradeshop $cmd$ [Amount] [Material]"),
    SET_PRODUCT(Lists.newArrayList("setProduct"), Permissions.NONE, 1, 3, true, "Set product of shop ", "/tradeshop $cmd$ [Amount] [Material]"),
    SET_COST(Lists.newArrayList("setCost"), Permissions.NONE, 1, 3, true, "Set cost of shop", "/tradeshop $cmd$ [Amount] [Material]"),
    REMOVE_PRODUCT(Lists.newArrayList("removeProduct", "delProduct"), Permissions.NONE, 1, 2, true, "Removes a product from the shop", "/tradeshop $cmd$ <List #>"),
    REMOVE_COST(Lists.newArrayList("removeCost", "delCost"), Permissions.NONE, 1, 2, true, "Removes a product from the shop", "/tradeshop $cmd$ <List #>"),
    LIST_PRODUCT(Lists.newArrayList("listProduct"), Permissions.NONE, 1, 1, true, "Lists the products in the shop", "/tradeshop $cmd$"),
    LIST_COST(Lists.newArrayList("listCost"), Permissions.NONE, 1, 1, true, "Lists the costs in a shop", "/tradeshop $cmd$"),

    // Shop info/Player commands
    WHO(Lists.newArrayList("who"), Permissions.INFO, 1, 1, true, "Shop members of shop", "/tradeshop $cmd$"),
    WHAT(Lists.newArrayList("what", "peek", "shop", "view"), Permissions.INFO, 1, 1, true, "Peek at shop inventory", "/tradeshop $cmd$"),
    MULTI(Lists.newArrayList("multi", "multiply", "many"), Permissions.NONE, 1, 2, true, "Changes trade multiplier for this login", "/tradeshop $cmd$ <Amount>"),
    STATUS(Lists.newArrayList("status", "stats", "s"), Permissions.INFO, 1, 2, true, "Displays the status of all shops the player has a relation to", "/tradeshop $cmd$ [Name]"),
    TOGGLE_STATUS(Lists.newArrayList("togglestatus", "togglemotd", "tstatus"), Permissions.INFO, 1, 1, true, "Toggles the join message containing the list of shops one is involved with", "/tradeshop togglestatus"),
    FIND(Lists.newArrayList("find", "where", "nearby"), Permissions.INFO, 2, 50, true, "Finds nearby shops. <Search Parameters> can include \n'cost=item1,item2,moreItems;'\n'product=item1,item2,moreItems;'\n'distance=number'\n    '|' can be used in place of commas in the list if you would like on of multiple items.\n    The lists are read from left to right so 'minecraft:dirt|minecraft:stone,minecraft:dirt' would be dirt of (stone and dit). A shop with just stone would not be found with this.", "/tradeshop $cmd$ <Search Paramaters>"),

    // Other commands
    HELP(Lists.newArrayList("help", "?"), Permissions.HELP, 1, 2, false, "Display help message", "/tradeshop $cmd$ [command]"),
    SETUP(Lists.newArrayList("setup", "start", "create", "make"), Permissions.HELP, 1, 1, false, "Display shop setup tutorial", "/tradeshop $cmd$"),
    BUGS(Lists.newArrayList("bugs", "bug"), Permissions.NONE, 1, 1, false, "Report bugs to the developers", "/tradeshop $cmd$"),
    TOGGLE_ADMIN(Lists.newArrayList("toggleadmin", "tadmin", "ta"), Permissions.ADMIN, 1, 1, true, "Toggles Admin mode for players with the admin permission", "/tradeshop $cmd$"),
    ADMIN(Lists.newArrayList("admin"), Permissions.ADMIN, 1, 2, true, "Shows players their current admin mode or changes with optional variable", "/tradeshop $cmd$ [True/False]"),
    RELOAD(Lists.newArrayList("reload"), Permissions.MANAGE_PLUGIN, 1, 1, false, "Reload configuration files", "/tradeshop $cmd$");


    /**
     * Name of the permission
     **/
    private final String name;

    /**
     * All names that can be used to call the command
     **/
    private final List<String> names;

    /**
     * Minimum and Maximum arguments required for the command
     **/
    private final int minArgs;
    private final int maxArgs;

    /**
     * Permission required for the command
     **/
    private final Permissions perm;

    /**
     * Whether the command requires a player to run
     **/
    private final boolean needsPlayer;

    /**
     * Description for command
     */
    private final String description;

    /**
     * Command usage
     */
    private final String usage;

    Commands(List<String> names, Permissions perm, int minArgs, int maxArgs, boolean needsPlayer, String description, String usage) {
        this.names = names;
        this.perm = perm;
        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
        this.needsPlayer = needsPlayer;
        this.description = description;
        this.usage = usage;
        name = name();
    }

    /**
     * Returns Enum Value if present of the string variable
     *
     * @param toCheck String to check if enum exists for
     * @return Commands Enum value of string
     */
    public static Commands getType(String toCheck) {
        for (Commands cmd : values()) {
            for (String str : cmd.getNames()) {
                if (str.equalsIgnoreCase(toCheck)) {
                    return cmd;
                }
            }
        }

        return null;
    }

    /**
     * Returns list of all usable names for the command
     *
     * @return String List containing names
     */
    public List<String> getNames() {
        return names;
    }

    /**
     * Returns true if the sent string is contained within a command or its aliases
     *
     * @param cmd String to test for partial command match
     *
     * @return true if the sent string is contained within a command or its aliases
     */
    public boolean isPartialName(String cmd) {
        for (String name : getNames()) {
            if (name.toLowerCase().contains(cmd.toLowerCase()))
                return true;
        }

        return false;
    }


    /**
     * Returns list of all usable names for the command
     *
     * @return String List containing names
     */
    public String getFirstName() {
        return names.get(0);
    }

    /**
     * Returns minimum required arguments
     *
     * @return int minimum arguments
     */
    public int getMinArgs() {
        return minArgs;
    }

    /**
     * Returns maximum required arguments
     *
     * @return int maximum arguments
     */
    public int getMaxArgs() {
        return maxArgs;
    }

    /**
     * Returns true if string has equivalent command enum
     *
     * @param toCheck String to check if enum exists for
     * @return true if enum exists
     */
    public boolean isName(String toCheck) {
        for (String test : names) {
            if (test.equalsIgnoreCase(toCheck)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns Permission enum required for command
     *
     * @return Permissions enum required for command
     */
    public Permissions getPerm() {
        return perm;
    }

    /**
     * Returns true if command needs a player to run
     *
     * @return true if command requires player
     */
    public boolean needsPlayer() {
        return needsPlayer;
    }

    /**
     * Returns the description for the command
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns command usage
     *
     * @return usage
     */
    public String getUsage() {
        return usage.replace("$cmd$", getFirstName());
    }

    /**
     * Returns command aliases
     *
     * @return usage
     */
    public String getAliases() {
        int namesSize = getNames().size();
        if (namesSize == 1)
            return "&eNone";

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < namesSize; i++) {
            sb.append("&e");
            sb.append(getNames().get(i));
            if (i < namesSize - 1)
                sb.append("&b | ");
        }

        return sb.toString();
    }

    /**
     * Checks if the player has permission for the command
     *
     * @param sender sender to check perm for
     * @return true is player has perm
     */
    public PermStatus checkPerm(CommandSender sender) {
        if (sender instanceof Player) {
            if (!Permissions.hasPermission((Player) sender, getPerm()))
                return PermStatus.NO_PERM;
        } else {
            if (needsPlayer())
                return PermStatus.PLAYER_ONLY;
        }

        return PermStatus.GOOD;
    }
}