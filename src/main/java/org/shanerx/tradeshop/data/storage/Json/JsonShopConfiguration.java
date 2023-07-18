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
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.data.storage.ShopConfiguration;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shoplocation.ShopChunk;
import org.shanerx.tradeshop.shoplocation.ShopLocation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class JsonShopConfiguration extends JsonConfiguration implements ShopConfiguration {

    private final ShopChunk chunk;

    public JsonShopConfiguration(ShopChunk chunk) {
        super(chunk.getWorldName(), chunk.serialize());
        this.chunk = chunk;
    }

    public static boolean doesConfigExist(ShopChunk chunk) {
        return getFile(chunk.getWorldName(), chunk.serialize()).exists();
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
        Shop shop = loadASync(loc);

        if (shop == null)
            return null;

        shop.fixAfterLoad();
        return shop;
    }

    @Override
    public Shop loadASync(ShopLocation loc) {
        String locStr = loc.serialize();

        if (!jsonObj.has(locStr)) return null;

        Shop shop = gson.fromJson(jsonObj.get(locStr), Shop.class);

        shop.aSyncFix();

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

    @Override
    protected void saveFile() {
        if (!PLUGIN.getDataStorage().saving.containsKey(file)) {
            final String str = gson.toJson(jsonObj);
            PLUGIN.getDataStorage().saving.put(file, str);

            Bukkit.getScheduler().runTaskAsynchronously(TradeShop.getPlugin(), () -> {
                if (!str.isEmpty()) {
                    try {
                        FileWriter fileWriter = new FileWriter(this.file);
                        fileWriter.write(str);
                        fileWriter.flush();
                        fileWriter.close();
                    } catch (IOException e) {
                        PLUGIN.getLogger().log(Level.SEVERE, "Could not save " + this.file.getName() + " file! Data may be lost!", e);
                    }
                }
                PLUGIN.getDataStorage().saving.remove(this.file);
            });
        }
    }

    @Override
    protected void loadFile() {
        if (!this.file.exists()) {
            // If could not find file try with old separators
            String oldFile = file.getPath() + File.separator + chunk.serialize().replace(";;", "_") + ".json";
            if (new File(oldFile).exists())
                new File(oldFile).renameTo(file);
        }


        if (PLUGIN.getDataStorage().saving.containsKey(file)) {
            jsonObj = JsonParser.parseString(PLUGIN.getDataStorage().saving.get(file)).getAsJsonObject();
        } else {
            super.loadFile();
        }

        for (Map.Entry<String, JsonElement> entry : Sets.newHashSet(jsonObj.entrySet())) {
            if (entry.getKey().contains("l_")) {
                jsonObj.add(ShopLocation.deserialize(entry.getKey()).serialize(), entry.getValue());
                jsonObj.remove(entry.getKey());
            }
        }
    }
}
