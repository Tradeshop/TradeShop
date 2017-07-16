package org.shanerx.tradeshop;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.shanerx.tradeshop.commands.Ts;
import org.shanerx.tradeshop.itrade.CreateISign;
import org.shanerx.tradeshop.itrade.ITrade;
import org.shanerx.tradeshop.trade.Admin;
import org.shanerx.tradeshop.trade.CreateSign;
import org.shanerx.tradeshop.trade.Trade;

public class TradeShop extends JavaPlugin {
	
	TradeShop plugin = this;

	@SuppressWarnings("unused")
	private String dir = plugin.getDataFolder().toString();

	public File configFile = new File(this.getDataFolder(), "messages.yml");
	public FileConfiguration config;
	
	
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
		
		config = YamlConfiguration.loadConfiguration(configFile);
		
		addConfigDefaults(config);
		
		Trade shop = new Trade(this);
		CreateSign inst = new CreateSign(this);
		Admin admn = new Admin(this);
		
		getServer().getPluginManager().registerEvents(shop, this);
		getServer().getPluginManager().registerEvents(inst, this);
		getServer().getPluginManager().registerEvents(admn, this);
		
		getServer().getPluginManager().registerEvents(new ITrade(this), this);
		getServer().getPluginManager().registerEvents(new CreateISign(this), this);
		
		getCommand("tradeshop").setExecutor(new Ts(this));
		
	}
	
	private void addConfigDefaults(FileConfiguration config) {

		if (config.getString("invalid-arguments") == null) {
			config.set("invalid-arguments", "&eTry &6/tradeshop help &eto display help!");
		
			try {
				config.save(configFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		if (config.getString("no-command-permission") == null) {
			config.set("no-command-permission", "&aYou do not have permission to execute this command");
		
			try {
				config.save(configFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		if (config.getString("setup-help") == null) {
			config.set("setup-help", "\n&2Setting up a TradeShop is easy! Just make sure to follow these steps:"
					+ "\n \nStep 1: &ePlace down a chest."
					+ "\n&2Step 2: &ePlace a sign on top of the chest."
					+ "\n&2Step 3: &eWrite the following on the sign"
					+ "\n&6[Trade]\n<amount> <item_you_sell>\n<amount> <item_you_buy>\n&6&oEmpty line\n");
	
			try {
				config.save(configFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		if (config.getString("no-ts-create-permission") == null) {
			config.set("no-ts-create-permission", "&cYou don't have permission to create TradeShops!");
			
			try {
				config.save(configFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		if (config.getString("no-chest") == null) {
			config.set("no-chest", "&cYou need to put a chest under the sign!");
			
			try {
				config.save(configFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		if (config.getString("invalid-sign") == null) {
			config.set("invalid-sign", "&cInvalid sign format!");
			
			try {
				config.save(configFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		if (config.getString("no-ts-destroy") == null) {
			config.set("no-ts-destroy", "&cYou may not destroy that TradeShop");
			
			try {
				config.save(configFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		if (config.getString("successful-setup") == null) {
			config.set("successful-setup", "&aYou have sucessfully setup a TradeShop!");
			
			try {
				config.save(configFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		if (config.getString("no-ts-open") == null) {
			config.set("no-ts-open", "&cThat TradeShop does not belong to you");
			
			try {
				config.save(configFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		if (config.getString("empty-ts-on-setup") == null) {
			config.set("empty-ts-on-setup", "&cTradeShop empty, please remember to fill it!");
			
			try {
				config.save(configFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		if (config.getString("on-trade") == null) {
			config.set("on-trade", "&aYou have traded your&e {AMOUNT2} {ITEM2} &a for &e {AMOUNT1} {ITEM1} &awith {SELLER}");
			
			try {
				config.save(configFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		if (config.getString("insufficient-items") == null) {
			config.set("insufficient-items", "&cYou do not have &e {AMOUNT} {ITEM}&c!");
			
			try {
				config.save(configFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
        
        if (config.getString("shop-full-amount") == null) {
            config.set("shop-full-amount", "&cThe shop does not have &e{AMOUNT} &cof a single type of &e{ITEM}&c!");
            
            try {
                config.save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }
		
		if (config.getString("full-amount") == null) {
            config.set("full-amount", "&cYou must have &e{AMOUNT} &cof a single type of &e{ITEM}&c!");
            
            try {
                config.save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }
		
		if (config.getString("shop-empty") == null) {
			config.set("shop-empty", "&cThis TradeShop does not have &e {AMOUNT} {ITEM}&c!");
			
			try {
				config.save(configFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		if (config.getString("confirm-trade") == null) {
			config.set("confirm-trade", "&eTrade &6 {AMOUNT1} {ITEM1} &e for &6 {AMOUNT2} {ITEM2} &e?");
			
			try {
				config.save(configFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}

	@Override
	public void onDisable() {
		
		plugin = null;
		
	}
	
}
