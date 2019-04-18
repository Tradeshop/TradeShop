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

	HELP(Lists.newArrayList("help", "?"), 0, 0),
	SETUP(Lists.newArrayList("setup", "start", "create", "make"), 0, 0),
	BUGS(Lists.newArrayList("bug", "bugs"), 0, 0),
	ADDOWNER(Lists.newArrayList("addOwner"), 0, 0),
	REMOVEOWNER(Lists.newArrayList("removeOwner"), 0, 0),
	ADDMEMBER(Lists.newArrayList("addMember"), 0, 0),
	REMOVEMEMBER(Lists.newArrayList("removeMember"), 0, 0),
	ADDPRODUCT(Lists.newArrayList("addProduct"), 0, 0),
	ADDCOST(Lists.newArrayList("addCost"), 0, 0),
	OPEN(Lists.newArrayList("open"), 0, 0),
	CLOSE(Lists.newArrayList("close"), 0, 0),
	WHO(Lists.newArrayList("who"), 0, 0),
	WHAT(Lists.newArrayList("what", "peek", "windowShop", "shop", "view"), 0, 0),
	ADDITEM(Lists.newArrayList("additem", "newItem"), 0, 0),
	REMOVEITEM(Lists.newArrayList("removeItem", "delItem"), 0, 0),
	GETCUSTOMITEMS(Lists.newArrayList("getCustomItems", "getCIs"), 0, 0),
	RELOAD(Lists.newArrayList("reload"), 0, 0);

	private String name;
	private List<String> names;
	private int minArgs, maxArgs;

	Commands(List<String> names, int minArgs, int maxArgs) {
		this.names = names;
		this.minArgs = minArgs;
		this.maxArgs = maxArgs;
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

	public boolean isName(String str) {
		for (String test : names) {
			if (test.equalsIgnoreCase(str)) {
				return true;
			}
		}

		return false;
	}

}
