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

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.shanerx.tradeshop.TradeShop;

public enum Permissions {

	HELP("help"),

	CREATE("create"),

	CREATEI("create.infinite"),

	CREATEBI("create.bi"),

	ADMIN("admin"),

	WHO("who");

	private String key;
	private final static TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");

	Permissions(String key) {
		this.key = key;
	}

	@Override
	public String toString() {
		return "tradeshop." + key;
	}

	public Permission getPerm() {
		return new Permission(toString());
	}
}