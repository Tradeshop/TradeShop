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

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.shanerx.tradeshop.commands.Executor;
import org.shanerx.tradeshop.itrade.IShopCreateEventListener;
import org.shanerx.tradeshop.itrade.IShopCreateEventListener;
import org.shanerx.tradeshop.itrade.ITradeEventListener;
import org.shanerx.tradeshop.trade.AdminEventListener;
import org.shanerx.tradeshop.trade.ShopCreateEventListener;
import org.shanerx.tradeshop.trade.TradeEventListener;

public class TradeShop extends JavaPlugin {

    private File configFile = new File(this.getDataFolder(), "messages.yml");
    private FileConfiguration config;

    public File getConfigFile() {
    	return configFile;
	}
	
	@Override
	public FileConfiguration getConfig() {
    	return config;
	}
	
	@Override
	public void reloadConfig() {
    	config = YamlConfiguration.loadConfiguration(configFile);
    	addConfigDefaults(config);
	}

    @Override
    public void onEnable() {

        if (! configFile.exists()) {
            if (! getDataFolder().exists()) {
                getDataFolder().mkdir();
            }
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        reloadConfig();

        TradeEventListener shop = new TradeEventListener(this);
		ShopCreateEventListener inst = new ShopCreateEventListener(this);
        AdminEventListener admn = new AdminEventListener(this);
    
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(shop, this);
        pm.registerEvents(inst, this);
        pm.registerEvents(admn, this);
        pm.registerEvents(new ITradeEventListener(this), this);
        pm.registerEvents(new IShopCreateEventListener(this), this);

        getCommand("tradeshop").setExecutor(new Executor(this));
    }

    private void addConfigDefaults(FileConfiguration config) {
        if (config.getString("invalid-arguments") == null) {
            config.set("invalid-arguments", "&eTry &6/tradeshop help &eto display help!");
        }

        if (config.getString("no-command-permission") == null) {
            config.set("no-command-permission", "&aYou do not have permission to execute this command");
        }

        if (config.getString("setup-help") == null) {
            config.set("setup-help", "\n&2Setting up a TradeShop is easy! Just make sure to follow these steps:"
                    + "\n \nStep 1: &ePlace down a chest."
                    + "\n&2Step 2: &ePlace a sign on top of the chest."
                    + "\n&2Step 3: &eWrite the following on the sign"
                    + "\n&6[Trade]\n<amount> <item_you_sell>\n<amount> <item_you_buy>\n&6&oEmpty line\n");
        }

        if (config.getString("no-ts-create-permission") == null) {
            config.set("no-ts-create-permission", "&cYou don't have permission to create TradeShops!");
        }

        if (config.getString("no-chest") == null) {
            config.set("no-chest", "&cYou need to put a chest under the sign!");
        }

        if (config.getString("invalid-sign") == null) {
            config.set("invalid-sign", "&cInvalid sign format!");
        }

        if (config.getString("no-ts-destroy") == null) {
            config.set("no-ts-destroy", "&cYou may not destroy that TradeShop");
        }

        if (config.getString("successful-setup") == null) {
            config.set("successful-setup", "&aYou have sucessfully setup a TradeShop!");
        }

        if (config.getString("no-ts-open") == null) {
            config.set("no-ts-open", "&cThat TradeShop does not belong to you");
        }

        if (config.getString("empty-ts-on-setup") == null) {
            config.set("empty-ts-on-setup", "&cTradeShop empty, please remember to fill it!");
        }

        if (config.getString("on-trade") == null) {
            config.set("on-trade", "&aYou have traded your&e {AMOUNT2} {ITEM2} &a for &e {AMOUNT1} {ITEM1} &awith {SELLER}");
        }

        if (config.getString("insufficient-items") == null) {
            config.set("insufficient-items", "&cYou do not have &e {AMOUNT} {ITEM}&c!");
        }

        if (config.getString("shop-full-amount") == null) {
            config.set("shop-full-amount", "&cThe shop does not have &e{AMOUNT} &cof a single type of &e{ITEM}&c!");
        }

        if (config.getString("full-amount") == null) {
            config.set("full-amount", "&cYou must have &e{AMOUNT} &cof a single type of &e{ITEM}&c!");
        }

        if (config.getString("shop-empty") == null) {
            config.set("shop-empty", "&cThis TradeShop does not have &e {AMOUNT} {ITEM}&c!");
        }

        if (config.getString("shop-full") == null) {
            config.set("shop-full", "&cThis TradeShop is full, please contact the owner to get it emptied!");
        }
        
        if (config.getString("player-full") == null) {
            config.set("player-full", "&cYour inventory is full, please make room before trading items!");
        }
	    
        if (config.getString("confirm-trade") == null) {
            config.set("confirm-trade", "&eTrade &6 {AMOUNT1} {ITEM1} &e for &6 {AMOUNT2} {ITEM2} &e?");
        }
        save();
    }
    
    private void save() {
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    @Override
    public void onDisable() {}
}
