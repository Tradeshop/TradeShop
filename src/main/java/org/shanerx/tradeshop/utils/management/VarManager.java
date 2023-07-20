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

package org.shanerx.tradeshop.utils.management;

import com.google.common.collect.Lists;
import org.bukkit.NamespacedKey;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.data.config.ConfigManager;
import org.shanerx.tradeshop.data.config.Language;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.data.storage.DataStorage;
import org.shanerx.tradeshop.data.storage.DataType;
import org.shanerx.tradeshop.shop.ShopSign;
import org.shanerx.tradeshop.shop.ShopStorage;
import org.shanerx.tradeshop.utils.debug.Debug;
import org.shanerx.tradeshop.utils.debug.DebugLevels;
import org.shanerx.tradeshop.utils.logging.transactionlogging.TransactionLogger;
import org.shanerx.tradeshop.utils.versionmanagement.Expirer;
import org.shanerx.tradeshop.utils.versionmanagement.Updater;
import org.shanerx.tradeshop.utils.versionmanagement.Version;

import java.util.List;

public class VarManager {

    //<editor-fold desc="Managed Variables">
    private final NamespacedKey storageKey, signKey;
    private final TradeShop TRADESHOP;
    private final int spigotID = 32762,
            bStatsPluginID = 1690;
    protected int shopCounter = 0,
            lastIndex = -1;
    protected List<Integer> tradeCounter;
    private Expirer expirer;
    private MetricsManager metricsManager;
    private boolean skipHopperProtection = false;
    private ListManager listManager;
    private DataStorage dataStorage;
    private ConfigManager settingManager, messageManager;
    private Language language;
    private TransactionLogger transactionLogger;
    private Version version;
    private ShopSign signs;
    private ShopStorage storages;
    private Debug debugger;
    private Updater updater;
    //</editor-fold>

    public VarManager(TradeShop plugin) {
        this.TRADESHOP = plugin;
        storageKey = new NamespacedKey(TRADESHOP, "tradeshop-storage-data");
        signKey = new NamespacedKey(TRADESHOP, "tradeshop-sign-data");
        tradeCounter = Lists.newArrayList(0);
        lastIndex = tradeCounter.size() - 1;
    }

    public void startup() {
        getDebugger();

        expirer = new Expirer(TRADESHOP);
        if (!expirer.initiateDevExpiration()) {
            expirer = null;
        }
    }

    //<editor-fold desc="Getters & (Re)Setters">

    public Updater getUpdater() {
        if (updater == null) resetUpdater();

        return updater;
    }

    public void resetUpdater() {
        updater = new Updater(TRADESHOP.getDescription(), "https://api.spigotmc.org/legacy/update.php?resource=" + spigotID, "https://www.spigotmc.org/resources/tradeshop." + spigotID + "/");
    }

    public NamespacedKey getStorageKey() {
        return storageKey;
    }

    public NamespacedKey getSignKey() {
        return signKey;
    }

    public MetricsManager getMetricsManager() {
        if (this.metricsManager == null) resetMetricsManager();

        return metricsManager;
    }

    public void resetMetricsManager() {
        this.metricsManager = new MetricsManager(TRADESHOP);
    }

    public boolean isSkipHopperProtection() {
        return skipHopperProtection;
    }

    public void setSkipHopperProtection(boolean skipHopperProtection) {
        this.skipHopperProtection = skipHopperProtection;
    }

    public ListManager getListManager() {
        if (listManager == null) resetListManager();

        return listManager;
    }

    public void resetListManager() {
        this.listManager = new ListManager();
    }

    public DataStorage getDataStorage() {
        if (dataStorage == null) resetDataStorage();

        return dataStorage;
    }

    public void resetDataStorage() {
        if (dataStorage == null) {
            try {
                dataStorage = new DataStorage(DataType.valueOf(Setting.DATA_STORAGE_TYPE.getString().toUpperCase()));
            } catch (IllegalArgumentException iae) {
                debugger.log("Config value for data storage set to an invalid value: " + Setting.DATA_STORAGE_TYPE.getString(), DebugLevels.DATA_ERROR);
                debugger.log("TradeShop will now disable...", DebugLevels.DATA_ERROR);
                TRADESHOP.getServer().getPluginManager().disablePlugin(TRADESHOP);
            }
        }
    }

    public ConfigManager getSettingManager() {
        if (settingManager == null) resetSettingManager();

        return settingManager;
    }

    public void resetSettingManager() {
        this.settingManager = new ConfigManager(TRADESHOP, ConfigManager.ConfigType.CONFIG);
    }

    public ConfigManager getMessageManager() {
        if (messageManager == null) resetMessageManager();

        return messageManager;
    }

    public void resetMessageManager() {
        this.messageManager = new ConfigManager(TRADESHOP, ConfigManager.ConfigType.MESSAGES);
    }

    public Language getLanguage() {
        if (language == null) resetLanguage();

        return language;
    }

    public void resetLanguage() {
        this.language = new Language(TRADESHOP);
    }

    public TransactionLogger getTransactionLogger() {
        if (transactionLogger == null) resetTransactionLogger();

        return transactionLogger;
    }

    public void resetTransactionLogger() {
        this.transactionLogger = new TransactionLogger(TRADESHOP);
    }

    public Version getVersion() {
        if (this.version == null) resetVersion();

        return version;
    }

    public void resetVersion() {
        this.version = new Version(TRADESHOP.getServer().getVersion());
    }

    public ShopSign getSigns() {
        if (signs == null) resetSigns();

        return signs;
    }

    public void resetSigns() {
        this.signs = new ShopSign();
    }

    public ShopStorage getStorages() {
        if (storages == null) resetStorages();

        return storages;
    }

    public void resetStorages() {
        this.storages = new ShopStorage();
    }

    public Debug getDebugger() {
        if (debugger == null) resetDebugger();

        return debugger;
    }

    public void resetDebugger() {
        this.debugger = new Debug();
    }

    public int getbStatsPluginID() {
        return bStatsPluginID;
    }

    public int getSpigotID() {
        return spigotID;
    }

    public int getShopCounter() {
        return shopCounter;
    }

    public int getLastIndex() {
        return lastIndex;
    }

    public List<Integer> getTradeCounter() {
        return tradeCounter;
    }
    //</editor-fold>
}
