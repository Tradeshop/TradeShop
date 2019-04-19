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

package org.shanerx.tradeshop.enumys;

import com.google.common.collect.Lists;

import java.util.List;

public enum Commands {

	HELP(Lists.newArrayList("help", "?"), Permissions.HELP, 1, 2, false),
	SETUP(Lists.newArrayList("setup", "start", "create", "make"), Permissions.HELP, 1, 1, false),
	BUGS(Lists.newArrayList("bug", "bugs"), Permissions.NONE, 1, 1, false),
	ADDOWNER(Lists.newArrayList("addOwner"), Permissions.NONE, 2, 2, true),
	REMOVEOWNER(Lists.newArrayList("removeOwner", "delOwner"), Permissions.NONE, 2, 2, true),
	ADDMEMBER(Lists.newArrayList("addMember"), Permissions.NONE, 2, 2, true),
	REMOVEMEMBER(Lists.newArrayList("removeMember", "delMember"), Permissions.NONE, 2, 2, true),
	ADDPRODUCT(Lists.newArrayList("addProduct"), Permissions.NONE, 1, 3, true),
	ADDCOST(Lists.newArrayList("addCost"), Permissions.NONE, 1, 3, true),
	OPEN(Lists.newArrayList("open"), Permissions.NONE, 1, 1, true),
	CLOSE(Lists.newArrayList("close"), Permissions.NONE, 1, 1, true),
	WHO(Lists.newArrayList("who"), Permissions.INFO, 1, 1, true),
	WHAT(Lists.newArrayList("what", "peek", "windowShop", "shop", "view"), Permissions.INFO, 1, 1, true),
	ADDITEM(Lists.newArrayList("additem", "newItem"), Permissions.ADMIN, 1, 2, true),
	REMOVEITEM(Lists.newArrayList("removeItem", "delItem"), Permissions.ADMIN, 2, 2, false),
	GETCUSTOMITEMS(Lists.newArrayList("getCustomItems", "getCIs"), Permissions.ADMIN, 2, 2, true),
	RELOAD(Lists.newArrayList("reload"), Permissions.ADMIN, 1, 1, false),
	SWITCH(Lists.newArrayList("switch"), Permissions.EDIT, 1, 1, true);

	private String name;
	private List<String> names;
	private int minArgs, maxArgs;
	private Permissions perm;
	private boolean needsPlayer;

	Commands(List<String> names, Permissions perm, int minArgs, int maxArgs, boolean needsPlayer) {
		this.names = names;
		this.perm = perm;
		this.minArgs = minArgs;
		this.maxArgs = maxArgs;
		this.needsPlayer = needsPlayer;
		name = name();
	}

	public List<String> getNames() {
		return names;
	}

	public int getMinArgs() {
		return minArgs;
	}

	public int getMaxArgs() {
		return maxArgs;
	}

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

	public boolean isName(String str) {
		for (String test : names) {
			if (test.equalsIgnoreCase(str)) {
				return true;
			}
		}

		return false;
	}

	public Permissions getPerm() {
		return perm;
	}

	public boolean needsPlayer() {
		return needsPlayer;
	}
}
