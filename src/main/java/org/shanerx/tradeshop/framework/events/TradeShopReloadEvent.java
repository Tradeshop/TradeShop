package org.shanerx.tradeshop.framework.events;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.server.ServerEvent;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.objects.Debug;
import org.shanerx.tradeshop.objects.ListManager;
import org.shanerx.tradeshop.utils.data.DataStorage;

// TODO javadocs for TradeShopReloadEvent
public class TradeShopReloadEvent extends ServerEvent {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public TradeShop plugin;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public TradeShopReloadEvent(TradeShop plugin) {
        super();
        this.plugin = plugin;
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public DataStorage getDataStorage() {
        return plugin.getDataStorage();
    }

    public ListManager getListManager() {
        return plugin.getListManager();
    }

    public Debug getDebugger() {
        return plugin.getDebugger();
    }
}
