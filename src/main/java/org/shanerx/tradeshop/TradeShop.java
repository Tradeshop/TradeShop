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

package org.shanerx.tradeshop;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.shanerx.tradeshop.admin.AdminEventListener;
import org.shanerx.tradeshop.bitrade.BiShopCreateEventListener;
import org.shanerx.tradeshop.bitrade.BiTradeEventListener;
import org.shanerx.tradeshop.commands.Executor;
import org.shanerx.tradeshop.enums.Message;
import org.shanerx.tradeshop.enums.Setting;
import org.shanerx.tradeshop.itrade.IShopCreateEventListener;
import org.shanerx.tradeshop.itrade.ITradeEventListener;
import org.shanerx.tradeshop.object.CustomItemManager;
import org.shanerx.tradeshop.object.ListManager;
import org.shanerx.tradeshop.trade.ShopCreateEventListener;
import org.shanerx.tradeshop.trade.TradeEventListener;
import org.shanerx.tradeshop.util.Updater;

public class TradeShop extends JavaPlugin {
    private final boolean mc18 = this.getServer().getVersion().contains("1.8");
    private Metrics metrics;
    private ListManager listManager;
    private CustomItemManager customItemManager;

    public ListManager getListManager() {
        return listManager;
    }

    public CustomItemManager getCustomItemManager() {
        return customItemManager;
    }

    private Boolean isAboveMC18() {
        return !mc18;
    }

    @Override
    public void reloadConfig() {
        Message.reload();
        Setting.reload();
    }

    @Override
    public void onEnable() {
        if (!isAboveMC18()) {
            getLogger().info("[TradeShop] Minecraft versions before 1.9 are not supported beyond TradeShop version 1.5.2!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        reloadConfig();
        listManager = new ListManager();
        customItemManager = new CustomItemManager();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new TradeEventListener(this), this);
        pm.registerEvents(new ShopCreateEventListener(this), this);
        pm.registerEvents(new BiTradeEventListener(this), this);
        pm.registerEvents(new BiShopCreateEventListener(this), this);
        pm.registerEvents(new AdminEventListener(this), this);
        pm.registerEvents(new ITradeEventListener(this), this);
        pm.registerEvents(new IShopCreateEventListener(this), this);

        getCommand("tradeshop").setExecutor(new Executor(this));

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
    }
}
