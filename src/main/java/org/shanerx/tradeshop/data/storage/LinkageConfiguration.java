package org.shanerx.tradeshop.data.storage;

import org.shanerx.tradeshop.shop.ShopChest;
import org.shanerx.tradeshop.shoplocation.ShopLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface LinkageConfiguration {

    void save();
    void load();
    Map<String, String> getLinkageData();

    default ShopLocation getLinkedShop(ShopLocation chestLocation) {
        String loc = chestLocation.serialize();
        return getLinkageData().containsKey(loc) ? ShopLocation.deserialize(getLinkageData().get(chestLocation.serialize())) : null;
    }

    default int size() {
        return getLinkageData().size();
    }

    default void addLinkage(ShopLocation chestLocation, ShopLocation shopLocation) {
        if (getLinkageData().containsKey(chestLocation.serialize()))
            getLinkageData().replace(chestLocation.serialize(), shopLocation.serialize());
        else
            getLinkageData().put(chestLocation.serialize(), shopLocation.serialize());
    }

    default void add(ShopLocation chestLocation, ShopLocation shopLocation) {
        if (ShopChest.isDoubleChest(chestLocation.getLocation().getBlock())) {
            ShopLocation otherSideLocation = new ShopLocation(ShopChest.getOtherHalfOfDoubleChest(chestLocation.getLocation().getBlock()).getLocation());
            addLinkage(otherSideLocation, shopLocation);
        }

        addLinkage(chestLocation, shopLocation);
        save();
    }

    default void removeChest(ShopLocation chestLocation) {
        getLinkageData().remove(chestLocation);
        save();
    }

    default void removeShop(ShopLocation shopLocation) {
        List<String> removeChests = new ArrayList<>();
        String shopLoc = shopLocation.serialize();

        getLinkageData().forEach((key, value) -> {
            if (value.equals(shopLoc))
                removeChests.add(key);
        });

        removeChests.forEach((k) -> getLinkageData().remove(k));
    }
}
