/*
 *                 Copyright (c) 2016-2019
 *         SparklingComet @ http://shanerx.org
 *      KillerOfPie @ http://killerofpie.github.io
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
 * caused by their contribution(s) to the project. See the full License for more information.
 */

package org.shanerx.tradeshop.enumys;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Enum holding all commands as well as aliases,
 * required permissions, minimum and maximum
 * arguments, and whether or not command must be
 * run by a player
 **/

public enum Commands {

    HELP(Lists.newArrayList("help", "?"), Permissions.HELP, 1, 2, false, "Display help message", "/ts help [command]"),
    SETUP(Lists.newArrayList("setup", "start", "create", "make"), Permissions.HELP, 1, 1, false, "Display shop setup tutorial", "/tradeshop setup"),
	BUGS(Lists.newArrayList("bugs", "bug"), Permissions.NONE, 1, 1, false, "Report bugs to the developers", "/tradeshop bugs"),
    ADDMANAGER(Lists.newArrayList("addManager"), Permissions.NONE, 2, 2, true, "Add manager to shop", "/tradeshop addmanager <name>"),
    REMOVEMANGAER(Lists.newArrayList("removeManager", "delManager"), Permissions.NONE, 2, 2, true, "Remove manager from shop", "/tradeshop removemanager <name>"),
    ADDMEMBER(Lists.newArrayList("addMember"), Permissions.NONE, 2, 2, true, "Add member to shop", "/tradeshop addmember <name>"),
    REMOVEMEMBER(Lists.newArrayList("removeMember", "delMember"), Permissions.NONE, 2, 2, true, "Remove member from shop", "/tradeshop removemember <name>"),
    ADDPRODUCT(Lists.newArrayList("addProduct", "setProduct"), Permissions.NONE, 1, 3, true, "Add item to shop", "/tradeshop addproduct|setproduct"),
    ADDCOST(Lists.newArrayList("addCost", "setCost"), Permissions.NONE, 1, 3, true, "Set cost of trade", "/tradeshop addcost|setcost"),
    OPEN(Lists.newArrayList("open"), Permissions.NONE, 1, 1, true, "Open shop", "/tradeshop open"),
    CLOSE(Lists.newArrayList("close"), Permissions.NONE, 1, 1, true, "Close shop", "tradeshop close"),
    WHO(Lists.newArrayList("who"), Permissions.INFO, 1, 1, true, "Shop members of shop", "tradeshop who"),
    WHAT(Lists.newArrayList("what", "peek", "windowShop", "shop", "view"), Permissions.INFO, 1, 1, true, "Peek at shop inventory", "/tradeshop what|peek|view|shop"),
    RELOAD(Lists.newArrayList("reload"), Permissions.ADMIN, 1, 1, false, "Reload configuration files", "/tradeshop reload"),
    SWITCH(Lists.newArrayList("switch"), Permissions.EDIT, 1, 1, true, "Switch shop type", "tradeshop switch");

    /**
     * Name of the permission
     **/
    private String name;

    /**
     * All names that can be used to call the command
     **/
    private List<String> names;

    /**
     * Minimum and Maximum arguments required for the command
     **/
    private int minArgs, maxArgs;

    /**
     * Permission required for the command
     **/
    private Permissions perm;

    /**
     * Whether the command requires a player to run
     **/
    private boolean needsPlayer;

    /**
     * Description for command
     */
    private String description;

	/**
	 * Command usage
	 */
	private String usage;

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
     * @return List<String> containing names
     */
    public List<String> getNames() {
        return names;
    }


	/**
	 * Returns list of all usable names for the command
	 *
	 * @return List<String> containing names
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
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns command usage
	 * @return usage
	 */
	public String getUsage() {
		return usage;
	}
}
