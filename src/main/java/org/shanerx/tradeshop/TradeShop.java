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
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.shanerx.tradeshop.commands.CommandCaller;
import org.shanerx.tradeshop.commands.CommandTabCaller;
import org.shanerx.tradeshop.enumys.*;
import org.shanerx.tradeshop.listeners.*;
import org.shanerx.tradeshop.objects.Debug;
import org.shanerx.tradeshop.objects.ListManager;
import org.shanerx.tradeshop.utils.BukkitVersion;
import org.shanerx.tradeshop.utils.Updater;
import org.shanerx.tradeshop.utils.data.DataStorage;
import org.shanerx.tradeshop.utils.data.DataType;

public class TradeShop extends JavaPlugin {


	private final NamespacedKey storageKey = new NamespacedKey(this, "tradeshop-storage-data");
	private final NamespacedKey signKey = new NamespacedKey(this, "tradeshop-sign-data");

	private final int bStatsPluginID = 1690;
	private Metrics metrics;

	private boolean useInternalPerms = false;

	private ListManager lists;
	private DataStorage dataStorage;

	private BukkitVersion version;
	private ShopSign signs;
	private ShopStorage storages;

	private Debug debugger;

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

		Setting.reload();
		Message.reload();

		debugger = new Debug();

		try {
			dataStorage = new DataStorage(DataType.valueOf(Setting.DATA_STORAGE_TYPE.getString().toUpperCase()));
		} catch (IllegalArgumentException iae) {
			debugger.log("Config value for data storage set to an invalid value: " + Setting.DATA_STORAGE_TYPE.getString(), DebugLevels.DATA_ERROR);
			debugger.log("TradeShop will now disable...", DebugLevels.DATA_ERROR);
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		signs = new ShopSign();
		storages = new ShopStorage();
		lists = new ListManager();

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new JoinEventListener(this), this);
		pm.registerEvents(new ShopProtectionListener(this), this);
		pm.registerEvents(new ShopCreateListener(), this);
		pm.registerEvents(new ShopTradeListener(), this);
        pm.registerEvents(new ShopRestockListener(this), this);

		getCommand("tradeshop").setExecutor(new CommandCaller(this));
		getCommand("tradeshop").setTabCompleter(new CommandTabCaller(this));

        if (Setting.CHECK_UPDATES.getBoolean()) {
			new Thread(() -> new Updater(getDescription()).checkCurrentVersion()).start();
		}

		if (Setting.ALLOW_METRICS.getBoolean()) {
            metrics = new Metrics(this, bStatsPluginID);
			getLogger().info("Metrics successfully initialized!");

		} else {
			getLogger().warning("Metrics are disabled! Please consider enabling them to support the authors!");
		}
	}

	@Override
	public void onDisable() {
		dataStorage.saveChestLinkages();

		getListManager().clearManager();
	}

	public boolean useInternalPerms() {
		return useInternalPerms;
	}

	public void setUseInternalPerms(boolean useInternalPerms) {
		this.useInternalPerms = useInternalPerms;
	}

	public NamespacedKey getStorageKey() {
		return storageKey;
	}

	public NamespacedKey getSignKey() {
		return signKey;
	}

	public ListManager getListManager() {
        return lists;
    }

    public BukkitVersion getVersion() {
        return version;
    }

    public ShopSign getSigns() {
        return signs;
    }

    public ShopStorage getStorages() {
        return storages;
    }

    public Updater getUpdater() {
        return new Updater(getDescription());
    }

    public Debug getDebugger() {
        return debugger;
    }

	public DataStorage getDataStorage() {
		return dataStorage;
	}
}
