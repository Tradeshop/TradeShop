package org.shanerx.tradeshop.data.storage.sqlite;

import org.shanerx.tradeshop.data.storage.ShopConfiguration;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shoplocation.ShopChunk;
import org.shanerx.tradeshop.shoplocation.ShopLocation;

public class SQLiteShopConfiguration implements ShopConfiguration {

    private final ShopChunk chunk;

    public SQLiteShopConfiguration(ShopChunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public void save(Shop shop) {

    }

    @Override
    public void remove(ShopLocation loc) {

    }

    @Override
    public Shop load(ShopLocation loc) {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }
}
