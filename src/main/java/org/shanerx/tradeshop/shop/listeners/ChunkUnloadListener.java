package org.shanerx.tradeshop.shop.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.shoplocation.ShopChunk;

public class ChunkUnloadListener implements Listener {

    private TradeShop plugin;

    public ChunkUnloadListener(TradeShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        ShopChunk shopChunk = new ShopChunk(e.getChunk());
        TradeShop.getPlugin().getVarManager().getDataStorage().dropShopData(shopChunk);
    }
}
