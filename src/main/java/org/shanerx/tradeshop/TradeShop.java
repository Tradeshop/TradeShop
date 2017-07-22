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
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.shanerx.tradeshop.commands.Executor;
import org.shanerx.tradeshop.itrade.IShopCreateEventListener;
import org.shanerx.tradeshop.itrade.ITradeEventListener;
import org.shanerx.tradeshop.trade.AdminEventListener;
import org.shanerx.tradeshop.trade.ShopCreateEventListener;
import org.shanerx.tradeshop.trade.TradeEventListener;

public class TradeShop extends JavaPlugin {

    private File messagesFile = new File(this.getDataFolder(), "messages.yml");
    private FileConfiguration messages;
	private File settingsFile = new File(this.getDataFolder(), "config.yml");
	private FileConfiguration settings;

    public File getMessagesFile() {
    	return messagesFile;
	}
	
	public FileConfiguration getMessages() {
		return messages;
	}
	
	public File getSettingsFile() {
		return settingsFile;
	}
	
	public FileConfiguration getSettings() {
    	return settings;
	}
	
	@Deprecated
	@Override
	public FileConfiguration getConfig() {
    	return settings;
	}
	
	@Override
	public void reloadConfig() {
    	messages = YamlConfiguration.loadConfiguration(messagesFile);
    	addMessageDefaults();
    	settings = YamlConfiguration.loadConfiguration(settingsFile);
	}
	
    @Override
    public void onEnable() {
        createConfigs();
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

    private void addMessageDefaults() {
        if (messages.getString("invalid-arguments") == null) {
			messages.set("invalid-arguments", "&eTry &6/tradeshop help &eto display help!");
        }

        if (messages.getString("no-command-permission") == null) {
			messages.set("no-command-permission", "&aYou do not have permission to execute this command");
        }

        if (messages.getString("setup-help") == null) {
			messages.set("setup-help", "\n&2Setting up a TradeShop is easy! Just make sure to follow these steps:"
                    + "\n \nStep 1: &ePlace down a chest."
                    + "\n&2Step 2: &ePlace a sign on top of the chest."
                    + "\n&2Step 3: &eWrite the following on the sign"
                    + "\n&6[Trade]\n<amount> <item_you_sell>\n<amount> <item_you_buy>\n&6&oEmpty line"
                    + "\n&2Step 4: &eIf you are unsure what the item is, use &6/tradeshop item");
        }

        if (messages.getString("no-ts-create-permission") == null) {
			messages.set("no-ts-create-permission", "&cYou don't have permission to create TradeShops!");
        }

        if (messages.getString("no-chest") == null) {
			messages.set("no-chest", "&cYou need to put a chest under the sign!");
        }

        if (messages.getString("invalid-sign") == null) {
			messages.set("invalid-sign", "&cInvalid sign format!");
        }

        if (messages.getString("no-ts-destroy") == null) {
			messages.set("no-ts-destroy", "&cYou may not destroy that TradeShop");
        }

        if (messages.getString("successful-setup") == null) {
			messages.set("successful-setup", "&aYou have sucessfully setup a TradeShop!");
        }

        if (messages.getString("no-ts-open") == null) {
			messages.set("no-ts-open", "&cThat TradeShop does not belong to you");
        }

        if (messages.getString("empty-ts-on-setup") == null) {
			messages.set("empty-ts-on-setup", "&cTradeShop empty, please remember to fill it!");
        }

        if (messages.getString("on-trade") == null) {
			messages.set("on-trade", "&aYou have traded your&e {AMOUNT2} {ITEM2} &a for &e {AMOUNT1} {ITEM1} &awith {SELLER}");
        }

        if (messages.getString("insufficient-items") == null) {
			messages.set("insufficient-items", "&cYou do not have &e {AMOUNT} {ITEM}&c!");
        }

        if (messages.getString("shop-full-amount") == null) {
			messages.set("shop-full-amount", "&cThe shop does not have &e{AMOUNT} &cof a single type of &e{ITEM}&c!");
        }

        if (messages.getString("full-amount") == null) {
			messages.set("full-amount", "&cYou must have &e{AMOUNT} &cof a single type of &e{ITEM}&c!");
        }

        if (messages.getString("shop-empty") == null) {
			messages.set("shop-empty", "&cThis TradeShop does not have &e {AMOUNT} {ITEM}&c!");
        }

        if (messages.getString("shop-full") == null) {
			messages.set("shop-full", "&cThis TradeShop is full, please contact the owner to get it emptied!");
        }
        
        if (messages.getString("player-full") == null) {
			messages.set("player-full", "&cYour inventory is full, please make room before trading items!");
        }
	    
        if (messages.getString("confirm-trade") == null) {
			messages.set("confirm-trade", "&eTrade &6 {AMOUNT1} {ITEM1} &e for &6 {AMOUNT2} {ITEM2} &e?");
        }
        
        if (messages.getString("held-item") == null) {
			messages.set("held-item", "\n&6You are curently holding: \n&2Material: &e{MATERIAL}\n&2ID Number: &e{ID}\n&2Durability: &e{DURABILITY}\n&2Amount: &e{AMOUNT}\n&6Put this on your TradeShop sign: \n&e{AMOUNT} {MATERIAL}:{DURABILITY} \n&e{AMOUNT} {ID}:{DURABILITY}");
        }

        if (messages.getString("held-empty") == null) {
			messages.set("held-empty", "&eYou are currently holding nothing.");
        }
        
        if (messages.getString("player-only-command") == null) {
			messages.set("player-only-command", "&eThis command is only available to players.");
		}
		
        save();
    }
    
    private void save() {
		try {
			messages.save(messagesFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void createConfigs() {
    	try {
			if (!getDataFolder().isDirectory()) {
				getDataFolder().mkdirs();
			}
			if (!messagesFile.exists()) {
				messagesFile.createNewFile();
			}
			if (!settingsFile.exists()) {
				settingsFile.createNewFile();
			}
		} catch (IOException e) {
    		getLogger().log(Level.SEVERE, "Could not create config files! Disabling plugin!", e);
    		getServer().getPluginManager().disablePlugin(this);
		}
	}

    @Override
    public void onDisable() {}
}
