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

package org.shanerx.tradeshop.data.storage.Json;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import org.shanerx.tradeshop.data.storage.ShopConfiguration;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shoplocation.ShopChunk;
import org.shanerx.tradeshop.shoplocation.ShopLocation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonShopConfiguration extends JsonConfiguration implements ShopConfiguration {

    private final ShopChunk chunk;

    public JsonShopConfiguration(ShopChunk chunk) {
        super(chunk.getWorld().getName(), chunk.serialize());
        this.chunk = chunk;
    }

    public static boolean doesConfigExist(ShopChunk chunk) {
        return getFile(chunk.getWorld().getName(), chunk.serialize()).exists();
    }

    @Override
    public void loadFile() {
        if (!this.file.exists()) {
            // If could not find file try with old separators
            String oldFile = file.getPath() + File.separator + chunk.serialize().replace(";;", "_") + ".json";
            if (new File(oldFile).exists())
                new File(oldFile).renameTo(file);
        }

        super.loadFile();

        for (Map.Entry<String, JsonElement> entry : Sets.newHashSet(jsonObj.entrySet())) {
            if (entry.getKey().contains("l_")) {
                jsonObj.add(ShopLocation.deserialize(entry.getKey()).serialize(), entry.getValue());
                jsonObj.remove(entry.getKey());
            }
        }
    }

    @Override
    public void save(Shop shop) {
        jsonObj.add(shop.getShopLocationAsSL().serialize(), gson.toJsonTree(shop));

        saveFile();
    }

    @Override
    public void remove(ShopLocation loc) {
        if (jsonObj.has(loc.serialize()))
            jsonObj.remove(loc.serialize());

        saveFile();
    }

    @Override
    public Shop load(ShopLocation loc) {
        Shop shop;
        String locStr = loc.serialize();

        if (!jsonObj.has(locStr)) {
            return null;
        }

        shop = gson.fromJson(jsonObj.get(locStr), Shop.class);

        shop.fixAfterLoad();
        return shop;
    }

    @Override
    public List<ShopLocation> list() {
        List<ShopLocation> shopsInFile = new ArrayList<>();
        jsonObj.keySet().forEach(str -> shopsInFile.add(ShopLocation.deserialize(str)));
        return shopsInFile;
    }

    @Override
    public int size() {
        return jsonObj.size();
    }
}
