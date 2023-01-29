package org.shanerx.tradeshop.data.storage.sqlite;

import org.bukkit.World;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.shoplocation.ShopChunk;

import java.util.Map;
import java.util.UUID;

public class DatabaseCache {

    private static int refcount = 0;

    private TradeShop plugin;
    private DatabaseManager db;
    private JobsDispatch dispatch;

    private Map<String, SQLiteLinkageConfiguration> linkageData;
    private Map<UUID, SQLitePlayerConfiguration> playerData;
    private Map<String, SQLiteShopConfiguration> chunkData;

    public DatabaseCache(DatabaseManager db, TradeShop plugin) {
        this.plugin = plugin;
        this.db = db;
        this.dispatch = db.getJobsDispatch();

        if (refcount >= 1) {
            throw new UnsupportedOperationException("Cannot create more than one instance of DatabaseCache. No singleton access method exists (this is intentional).");
        }
        ++refcount;
    }

    public SQLiteLinkageConfiguration getLinkageData(World w) {
        String name = w.getName();
        if (!linkageData.containsKey(name)) {
            SQLiteLinkageConfiguration linkage =  new SQLiteLinkageConfiguration(w);
            linkageData.put(w.getName(), linkage);
            return linkage;
        }
        return linkageData.get(w.getName());
    }

    public SQLitePlayerConfiguration getPlayerData(UUID uuid) {
        if (!playerData.containsKey(uuid)) {
            SQLitePlayerConfiguration player =  new SQLitePlayerConfiguration(uuid);
            playerData.put(uuid, player);
            return player;
        }
        return playerData.get(uuid);
    }

    public SQLiteShopConfiguration getShopData(ShopChunk sc) {
        if (!chunkData.containsKey(sc.serialize())) {
            SQLiteShopConfiguration chunk =  new SQLiteShopConfiguration(sc);
            chunkData.put(sc.serialize(), chunk);
            return chunk;
        }
        return chunkData.get(sc.serialize());
    }
}
