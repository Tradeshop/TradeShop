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
