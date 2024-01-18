/*
 *
 *                         Copyright (c) 2016-2023
 *                SparklingComet @ http://shanerx.org
 *               KillerOfPie @ http://killerofpie.github.io
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *                http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  NOTICE: All modifications made by others to the source code belong
 *  to the respective contributor. No contributor should be held liable for
 *  any damages of any kind, whether be material or moral, which were
 *  caused by their contribution(s) to the project. See the full License for more information.
 *
 */

package org.shanerx.tradeshop.data.storage;

import org.shanerx.tradeshop.shop.ShopChest;
import org.shanerx.tradeshop.shoplocation.ShopLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface LinkageConfiguration {

    void save();

    void load();

    Map<String, Object> getLinkageData();

    default ShopLocation getLinkedShop(ShopLocation chestLocation) {
        String loc = chestLocation.toString();
        return getLinkageData().containsKey(loc) ? ShopLocation.deserialize(getLinkageData().get(chestLocation.toString()).toString()) : null;
    }

    default int size() {
        return getLinkageData().size();
    }

    default void addLinkage(ShopLocation chestLocation, ShopLocation shopLocation) {
        if (getLinkageData().containsKey(chestLocation.toString()))
            getLinkageData().replace(chestLocation.toString(), shopLocation.toString());
        else
            getLinkageData().put(chestLocation.toString(), shopLocation.toString());
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
        String shopLoc = shopLocation.toString();

        getLinkageData().forEach((key, value) -> {
            if (value.equals(shopLoc))
                removeChests.add(key);
        });

        removeChests.forEach((k) -> getLinkageData().remove(k));
        save();
    }
}
