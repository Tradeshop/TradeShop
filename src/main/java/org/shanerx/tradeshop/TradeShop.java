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

package org.shanerx.tradeshop;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.shanerx.tradeshop.commands.CommandCaller;
import org.shanerx.tradeshop.commands.CommandTabCaller;
import org.shanerx.tradeshop.data.config.ConfigManager;
import org.shanerx.tradeshop.data.config.Language;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.data.storage.DataStorage;
import org.shanerx.tradeshop.player.JoinEventListener;
import org.shanerx.tradeshop.player.Permissions;
import org.shanerx.tradeshop.shop.ShopSign;
import org.shanerx.tradeshop.shop.ShopStorage;
import org.shanerx.tradeshop.shop.listeners.PaperShopProtectionListener;
import org.shanerx.tradeshop.shop.listeners.ShopCreateListener;
import org.shanerx.tradeshop.shop.listeners.ShopProtectionListener;
import org.shanerx.tradeshop.shop.listeners.ShopRestockListener;
import org.shanerx.tradeshop.shop.listeners.ShopTradeListener;
import org.shanerx.tradeshop.utils.debug.Debug;
import org.shanerx.tradeshop.utils.logging.transactionlogging.TransactionLogger;
import org.shanerx.tradeshop.utils.logging.transactionlogging.listeners.SuccessfulTradeEventListener;
import org.shanerx.tradeshop.utils.management.ListManager;
import org.shanerx.tradeshop.utils.management.MetricsManager;
import org.shanerx.tradeshop.utils.management.VarManager;
import org.shanerx.tradeshop.utils.versionmanagement.Updater;
import org.shanerx.tradeshop.utils.versionmanagement.Version;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class TradeShop extends JavaPlugin {

    private VarManager varManager;

    public static TradeShop getPlugin() {
        TradeShop plugin = null;
        if (Bukkit.getPluginManager().isPluginEnabled("TradeShop"))
            plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
        else
            try {
                Bukkit.getPluginManager().enablePlugin(new TradeShop());
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "Tradeshop could not be found or enabled... \n" + Arrays.toString(e.getStackTrace()));
            }

        return plugin;
    }

    @Override
    public void onEnable() {
        getVarManager();

        if (!loadChecks()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        registration();

        getSettingManager().updateSkipHoppers();

        getSigns();
        getStorages();
        getListManager();

        if (Setting.CHECK_UPDATES.getBoolean()) {
            new Thread(() -> getUpdater().checkCurrentVersion()).start();
        }

        if (Setting.ALLOW_METRICS.getBoolean()) {
            getMetricsManager();
            getLogger().info("Metrics successfully initialized!");
        } else {
            getLogger().warning("Metrics are disabled! Please consider enabling them to support the authors!");
        }

        aliasCheck("ts");
    }

    @Override
    public void onDisable() {
        if (getListManager() != null)
            getListManager().clearManager();
    }

    //<editor-fold desc="Helpers">
    private boolean loadChecks() {
        varManager.startup();

        if (getVersion().isBelow(1, 9)) {
            getLogger().info("[TradeShop] Minecraft versions before 1.9 are not supported beyond TradeShop version 1.5.2!");
            return false;
        }

        if (getVersion().isBelow(1, 13)) {
            getLogger().info("[TradeShop] Minecraft versions before 1.13 are not supported beyond TradeShop version 1.8.2!");
            return false;
        }

        if (!getLanguage().isLoaded()) {
            return false;
        }

        getSettingManager().reload();
        getMessageManager().reload();

        return getDataStorage() != null;
    }

    private void registration() {

        Permissions.registerPermissions();


        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new JoinEventListener(this), this);
        pm.registerEvents(new ShopProtectionListener(this), this);
        pm.registerEvents(new ShopCreateListener(), this);
        pm.registerEvents(new ShopTradeListener(), this);
        pm.registerEvents(new ShopRestockListener(this), this);
        pm.registerEvents(new SuccessfulTradeEventListener(this), this);

        if (getServer().getVersion().toLowerCase().contains("paper")) {
            pm.registerEvents(new PaperShopProtectionListener(), this);
        }

        getCommand("tradeshop").setExecutor(new CommandCaller(this));
        getCommand("tradeshop").setTabCompleter(new CommandTabCaller(this));
    }

    private void aliasCheck(String conflictAlias) {
        Map<String, String[]> conflictingCMDs = new HashMap<>();
        getServer().getCommandAliases().forEach((cmd, alts) -> {
            if (Arrays.stream(alts).anyMatch((s -> s.equalsIgnoreCase(conflictAlias)))) {
                conflictingCMDs.put(cmd, alts);
            }
        });

        if (conflictingCMDs.size() > 1) {
            conflictingCMDs.forEach((k, v) -> {
                PluginCommand tsAlias = getCommand(conflictAlias);
                List<String> aliases = Lists.newArrayList(v);
                aliases.remove("ts");

                String newAlias = "ts", addition = newAlias.substring(1, newAlias.length() - 1), pl = tsAlias.getPlugin().getName();

                while (getCommand(newAlias) != null) {
                    //Get the character in the plugin name at the index after the first string of the existing addition
                    //If trying to grab a character after the end of the plugin name, then add the last digit of `i` instead
                    int i = pl.indexOf(addition) + 1;
                    addition += i <= pl.length() ? pl.charAt(i) : i % 10;

                    //take first letter of conflicting alias and add the addition to that
                    newAlias = newAlias.charAt(0) + addition;
                }
                aliases.add(newAlias);
                tsAlias.setAliases(aliases);
            });
        }
    }
    //</editor-fold>

    //<editor-fold desc="Getters">
    @Deprecated
    public boolean doSkipHopperProtection() {
        return varManager.isSkipHopperProtection();
    }

    @Deprecated
    public void setSkipHopperProtection(boolean skipHopperProtection) {
        varManager.setSkipHopperProtection(skipHopperProtection);
    }

    @Deprecated
    public NamespacedKey getStorageKey() {
        return varManager.getStorageKey();
    }

    @Deprecated
    public NamespacedKey getSignKey() {
        return varManager.getSignKey();
    }

    @Deprecated
    public ListManager getListManager() {
        return varManager.getListManager();
    }

    @Deprecated
    public Version getVersion() {
        return varManager.getVersion();
    }

    @Deprecated
    public ShopSign getSigns() {
        return varManager.getSigns();
    }

    @Deprecated
    public ShopStorage getStorages() {
        return varManager.getStorages();
    }

    @Deprecated
    public Updater getUpdater() {
        return varManager.getUpdater();
    }

    @Deprecated
    public Debug getDebugger() {
        return varManager.getDebugger();
    }

    @Deprecated
    public DataStorage getDataStorage() {
        return varManager.getDataStorage();
    }

    @Deprecated
    public MetricsManager getMetricsManager() {
        return varManager.getMetricsManager();
    }

    @Deprecated
    public ConfigManager getSettingManager() {
        return varManager.getSettingManager();
    }

    @Deprecated
    public ConfigManager getMessageManager() {
        return varManager.getMessageManager();
    }

    @Deprecated
    public Language getLanguage() {
        return varManager.getLanguage();
    }

    @Deprecated
    public TransactionLogger getTransactionLogger() {
        return varManager.getTransactionLogger();
    }

    public VarManager getVarManager() {
        if (varManager == null) varManager = new VarManager(this);

        return varManager;
    }

    //</editor-fold>
}
