package org.shanerx.tradeshop.data.storage.sqlite;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.World;
import org.shanerx.tradeshop.data.storage.LinkageConfiguration;
import org.shanerx.tradeshop.shoplocation.ShopLocation;

import java.util.Map;

public class SQLiteLinkageConfiguration implements LinkageConfiguration {

    String worldName;
    public SQLiteLinkageConfiguration(World world) {
        this.worldName = world.getName();
        load();
        throw new NotImplementedException("not impl.");
    }

    @Override
    public void save() {

    }

    @Override
    public void load() {

    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Map<String, String> getLinkageData() {
        return null;
    }

    @Override
    public ShopLocation getLinkedShop(ShopLocation chestLocation) {
        return null;
    }

    @Override
    public void add(ShopLocation chestLocation, ShopLocation shopLocation) {

    }

    @Override
    public void removeChest(ShopLocation chestLocation) {

    }

    @Override
    public void removeShop(ShopLocation shopLocation) {

    }
}
