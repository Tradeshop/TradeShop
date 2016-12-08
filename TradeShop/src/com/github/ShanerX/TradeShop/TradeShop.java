<<<<<<< HEAD
package com.github.ShanerX.TradeShop;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.ShanerX.TradeShop.Commands.ts;
import com.github.ShanerX.TradeShop.Trade.Admin;
import com.github.ShanerX.TradeShop.Trade.CreateSign;
import com.github.ShanerX.TradeShop.Trade.Trade;


public class TradeShop extends JavaPlugin {
	
	TradeShop plugin = this;
	
	String dir = plugin.getDataFolder().toString();

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
		
		getCommand("tradeshop").setExecutor(new ts(this));
		getCommand("ts").setExecutor(new ts(this));
		
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
			config.set("insufficient-items", "&cYou do not have &e {AMOUNT} {ITEM}6c!");
			
			try {
				config.save(configFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		if (config.getString("shop-empty") == null) {
			config.set("shop-empty", "&cThis TradeShop does not have &e {AMOUNT} {ITEM}6c!");
			
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
		
		// &eExchange &6" + amount1
		//+ " " + item_name1.toLowerCase() + "&e for &6" + amount2 + " " + item_name2.toLowerCase() + "&e?"
		
	}

	@Override
	public void onDisable() {
		
		plugin = null;
		
	}
	
}
=======
package com.github.ShanerX.TradeShop;

import org.bukkit.plugin.java.JavaPlugin;

import com.github.ShanerX.TradeShop.Commands.ts;
import com.github.ShanerX.TradeShop.Trade.Admin;
import com.github.ShanerX.TradeShop.Trade.CreateSign;
import com.github.ShanerX.TradeShop.Trade.Trade;

/*TODO LIST:
 * Trade.java:72 Setting line not working outside SignChangeEvent;
 * Config for custom messages and basic settings
 * Turning sign colour red/green. - CreateSign.java is ok, just not Trade.java
 * Admin commands! <-- NEXT UPDATE!
 * Numeric item IDs <-- NEXT UPDATE!
 */
public class TradeShop extends JavaPlugin {
	
	@Override
	public void onEnable() {
		Trade shop = new Trade(this);
		CreateSign inst = new CreateSign(this);
		Admin admn = new Admin(this);
		getServer().getPluginManager().registerEvents(shop, this);
		getServer().getPluginManager().registerEvents(inst, this);
		getServer().getPluginManager().registerEvents(admn, this);
		
		getCommand("tradeshop").setExecutor(new ts(this));
		getCommand("ts").setExecutor(new ts(this));
	}
	
	@Override
	public void onDisable() {
		
	}
	
}
>>>>>>> origin/master
