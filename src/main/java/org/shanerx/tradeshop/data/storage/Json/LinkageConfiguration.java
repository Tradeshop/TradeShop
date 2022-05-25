/*
 *
 *                         Copyright (c) 2016-2019
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

package org.shanerx.tradeshop.data.storage.Json;

import com.google.gson.reflect.TypeToken;
import org.bukkit.World;
import org.shanerx.tradeshop.shop.ShopChest;
import org.shanerx.tradeshop.shoplocation.ShopLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LinkageConfiguration extends JsonConfiguration {

    Map<String, String> linkageData;

    public LinkageConfiguration(World world) {
        super(world.getName(), "chest_linkage");
        load();
    }

    public void load() {
        linkageData = gson.fromJson(jsonObj.get("linkage_data"), new TypeToken<Map<String, String>>() {
        }.getType());
        if (linkageData == null)
            linkageData = new HashMap<>();
    }

    public Map<String, String> getLinkageData() {
        return linkageData;
    }

    public ShopLocation getLinkedShop(ShopLocation chestLocation) {
        String loc = chestLocation.serialize();

        return linkageData.containsKey(loc) ? ShopLocation.deserialize(linkageData.get(chestLocation.serialize())) : null;
    }

    public void save() {
        jsonObj.add("linkage_data", gson.toJsonTree(linkageData));

        saveFile();
    }

    public int size() {
        return linkageData.size();
    }

    public void add(ShopLocation chestLocation, ShopLocation shopLocation) {
        if (ShopChest.isDoubleChest(chestLocation.getLocation().getBlock())) {
            ShopLocation otherSideLocation = new ShopLocation(ShopChest.getOtherHalfOfDoubleChest(chestLocation.getLocation().getBlock()).getLocation());
            addLinkage(otherSideLocation, shopLocation);
        }

        addLinkage(chestLocation, shopLocation);
        save();
    }

    public void removeChest(ShopLocation chestLocation) {
        linkageData.remove(chestLocation);
        save();
    }

    public void removeShop(ShopLocation shopLocation) {
        List<String> removeChests = new ArrayList<>();
        String shopLoc = shopLocation.serialize();

        linkageData.forEach((key, value) -> {
            if (value.equals(shopLoc))
                removeChests.add(key);
        });

        removeChests.forEach((k) -> linkageData.remove(k));
    }

    private void addLinkage(ShopLocation chestLocation, ShopLocation shopLocation) {
        if (linkageData.containsKey(chestLocation.serialize()))
            linkageData.replace(chestLocation.serialize(), shopLocation.serialize());
        else
            linkageData.put(chestLocation.serialize(), shopLocation.serialize());
    }

}
