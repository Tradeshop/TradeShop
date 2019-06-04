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

package org.shanerx.tradeshop;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.shanerx.tradeshop.commands.CommandCaller;
import org.shanerx.tradeshop.commands.CommandTabCaller;
import org.shanerx.tradeshop.enumys.Message;
import org.shanerx.tradeshop.enumys.Setting;
import org.shanerx.tradeshop.enumys.ShopSign;
import org.shanerx.tradeshop.framework.AddonManager;
import org.shanerx.tradeshop.listeners.AddonListener;
import org.shanerx.tradeshop.listeners.AdminEventListener;
import org.shanerx.tradeshop.listeners.CustomInventoryListener;
import org.shanerx.tradeshop.listeners.JoinEventListener;
import org.shanerx.tradeshop.listeners.ShopCreateListener;
import org.shanerx.tradeshop.listeners.ShopProtectionListener;
import org.shanerx.tradeshop.listeners.ShopTradeListener;
import org.shanerx.tradeshop.objects.ListManager;
import org.shanerx.tradeshop.utils.BukkitVersion;
import org.shanerx.tradeshop.utils.Updater;

public class TradeShop extends JavaPlugin {

	private ListManager lists;
	private BukkitVersion version;
	private ShopSign signs;
	private AddonManager addonManager;

	private Metrics metrics;

	public ListManager getListManager() {
		return lists;
	}

	public BukkitVersion getVersion() {
		return version;
	}

	public ShopSign getSigns() {
		return signs;
	}

	public AddonManager getAddonManager() {
		return addonManager;
	}

	public Updater getUpdater() {
		return new Updater(getDescription());
	}

	@Override
	public void onEnable() {
		version = new BukkitVersion();

		if (version.isBelow(1, 9)) {
			getLogger().info("[TradeShop] Minecraft versions before 1.9 are not supported beyond TradeShop version 1.5.2!");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		if (version.isBelow(1, 13)) {
			getLogger().info("[TradeShop] Minecraft versions before 1.13 are not supported beyond TradeShop version 1.8.2!");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		Message.reload();
		Setting.reload();

		lists = new ListManager();

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new AdminEventListener(this), this);
		pm.registerEvents(new JoinEventListener(this), this);
		pm.registerEvents(new ShopProtectionListener(this), this);
		pm.registerEvents(new ShopCreateListener(), this);
		pm.registerEvents(new ShopTradeListener(), this);
		pm.registerEvents(new CustomInventoryListener(), this);
		pm.registerEvents(new AddonListener(this), this);

		getCommand("tradeshop").setExecutor(new CommandCaller(this));
		getCommand("tradeshop").setTabCompleter(new CommandTabCaller(this));

		signs = new ShopSign();

		boolean checkUpdates = Setting.CHECK_UPDATES.getBoolean();

		if (checkUpdates) {
			new Thread(() -> new Updater(getDescription()).checkCurrentVersion()).start();
		}

		if (Setting.ALLOW_METRICS.getBoolean()) {
			metrics = new Metrics(this);
			getLogger().info("Metrics successfully initialized!");

		} else {
			getLogger().warning("Metrics are disabled! Please consider enabling them to support the authors!");
		}

		addonManager = new AddonManager(this);
	}
}
