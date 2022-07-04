package org.shanerx.tradeshop.data.storage;

import org.shanerx.tradeshop.shoplocation.ShopLocation;

import java.util.Map;

public interface LinkageConfiguration {

    void save();
    void load();
    int size();
    Map<String, String> getLinkageData();
    ShopLocation getLinkedShop(ShopLocation chestLocation);
    void add(ShopLocation chestLocation, ShopLocation shopLocation);
    void removeChest(ShopLocation chestLocation);
    void removeShop(ShopLocation shopLocation);
}
