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

package org.shanerx.tradeshop.framework;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.shanerx.tradeshop.TradeShop;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/* TODO Add Messages and Notifications */

public class AddonManager {

	CustomCommandHandler handler;
	private TradeShop plugin;
	private List<Plugin> addons;

	public AddonManager(TradeShop plugin) {
		this.plugin = plugin;
		this.addons = new ArrayList<>();

		CustomCommandHandler.init(plugin);
		this.handler = CustomCommandHandler.getInstance();
	}

	public boolean isRegistered(String name) {
		return getAddon(name) != null;
	}

	public boolean isRegistered(Plugin plugin) {
		return addons.contains(plugin);
	}

	public Plugin getAddon(String name) {
		for (Plugin p : addons) {
			if (p.getName().equalsIgnoreCase(name)) {
				return p;
			}
		}

		return null;
	}

	public boolean hookAddon(String name) {
		if (isRegistered(name)) {
			return false;
		}

		Plugin pl = Bukkit.getPluginManager().getPlugin(name);
		return hookAddon(pl);
	}

	public boolean hookAddon(Plugin pl) {
		if (pl == null || isRegistered(pl)) {
			return false;
		}

		addons.add(pl);
		return true;
	}

	public boolean unhookAddon(String name) {
		if (!isRegistered(name))
			return false;

		Iterator<String> iter = handler.iter();
		Set<String> toRemove = new HashSet<>();
		while (iter.hasNext()) {
			String s = iter.next();
			if (handler.addonCmds.get(s).getPlugin().getName().equals(name)) {
				toRemove.add(s);
			}
		}

		toRemove.forEach(s -> handler.addonCmds.remove(s));

		addons.remove(Bukkit.getPluginManager().getPlugin(name));
		return true;
	}

	public boolean unhookAddon(Plugin plugin) {
		if (!isRegistered(plugin)) {
			return false;
		}

		Iterator<String> iter = handler.iter();
		Set<String> toRemove = new HashSet<>();
		while (iter.hasNext()) {
			String s = iter.next();
			if (handler.addonCmds.get(s).getPlugin().equals(plugin)) {
				toRemove.add(s);
			}
		}

		toRemove.forEach(s -> handler.addonCmds.remove(s));

		addons.remove(plugin);
		return true;
	}
}
