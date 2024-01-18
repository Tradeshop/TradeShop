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

package org.shanerx.tradeshop.utils.gsonprocessing;

import com.bergerkiller.bukkit.common.config.JsonSerializer;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.item.ShopItemStack;
import org.shanerx.tradeshop.item.ShopItemStackSettingKeys;
import org.shanerx.tradeshop.player.ShopUser;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shop.ShopSettingKeys;
import org.shanerx.tradeshop.shop.ShopStatus;
import org.shanerx.tradeshop.shop.ShopType;
import org.shanerx.tradeshop.shoplocation.ShopLocation;
import org.shanerx.tradeshop.utils.objects.ObjectHolder;
import org.shanerx.tradeshop.utils.objects.Tuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class GsonProcessor {
    private static final JsonSerializer jsonSerializer = new JsonSerializer();

    public static ItemStack fromJsonToItemStack(String json) throws JsonSerializer.JsonSyntaxException {
        return jsonSerializer.fromJsonToItemStack(json);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> jsonToMap(String json) throws JsonSerializer.JsonSyntaxException {
        return jsonSerializer.fromJson(json, Map.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromJson(JsonObject json, Class<T> type) throws JsonSerializer.JsonSyntaxException {
        if (type.equals(Shop.class) && (json.has("product") || json.has("cost"))) {
            return (T) jsonToShop(json);
        }

        return jsonSerializer.fromJson(json.toString(), type);
    }

    public static <T> T fromJson(String json, Class<T> type) throws JsonSerializer.JsonSyntaxException {
        if (type.equals(Shop.class)) {
            return fromJson(stringToJsonObject(json), type);
        }

        return jsonSerializer.fromJson(json, type);
    }

    public static String itemStackToJson(ItemStack item) {
        return jsonSerializer.itemStackToJson(item);
    }

    public static String mapToJson(Map<String, Object> map) {
        return jsonSerializer.mapToJson(map);
    }

    public static String toJson(Object value) {
        return jsonSerializer.toJson(value);
    }

    public static JsonObject stringToJsonObject(String json) throws JsonSerializer.JsonSyntaxException {
        return jsonSerializer.fromJson(json, JsonObject.class);
    }

    public static Shop jsonToShop(JsonObject json) {
        Shop shop;

        try {
            ShopLocation shopLoc = fromJson(json.get("shopLoc").getAsString(), ShopLocation.class),
                    chestLoc = fromJson(json.get("chestLoc").getAsString(), ShopLocation.class);
            ShopUser owner = fromJson(json.get("owner").getAsString(), ShopUser.class);
            ShopStatus shopStatus = fromJson(json.get("shopStatus").getAsString(), ShopStatus.class);
            Map<ShopSettingKeys, ObjectHolder<?>> shopSettings = new HashMap<>();
            List<UUID> managers = fromJsonInJsonArray(json.getAsJsonArray("managers"), UUID.class),
                    members = fromJsonInJsonArray(json.getAsJsonArray("members"), UUID.class);
            ShopType shopType = fromJson(json.get("shopType").getAsString(), ShopType.class);

            HashSet<UUID> users = new HashSet<>(Objects.nonNull(managers) ? managers : Collections.emptySet());

            jsonToMap(json.get("shopSettings").getAsString()).forEach((key, oh) -> shopSettings.put(ShopSettingKeys.valueOf(key), (ObjectHolder<?>) oh));

            List<ShopItemStack> product = fromJsonInJsonArrayToShopItemstackList(json.getAsJsonArray("product")),
                    cost = fromJsonInJsonArrayToShopItemstackList(json.getAsJsonArray("cost"));

            shop = new Shop(new Tuple<>(shopLoc.getLocation(),
                    chestLoc.getLocation()),
                    shopType,
                    owner,
                    new Tuple<>(
                            new HashSet<>(Objects.nonNull(managers) ? managers : Collections.emptyList()),
                            new HashSet<>(Objects.nonNull(members) ? members : Collections.emptyList())
                    ),
                    fromJsonInJsonArrayToShopItemstackList(json.getAsJsonArray("product")),
                    fromJsonInJsonArrayToShopItemstackList(json.getAsJsonArray("cost")));
            shop.setShopSettings(shopSettings);
            shop.setStatus(shopStatus);

        } catch (JsonSerializer.JsonSyntaxException | NullPointerException e) {
            return null;
        }

        return shop;
    }

    private static <LT> List<LT> fromJsonInJsonArray(JsonArray json, Class<LT> type) {
        List<LT> ret = new ArrayList<>();
        new ArrayList<>(json.asList()).forEach((jsonElement) -> {
            try {
                ret.add(fromJson(jsonElement.getAsString(), type));
            } catch (JsonSerializer.JsonSyntaxException ignored) {
            }
        });
        ret.remove(null);

        return ret.isEmpty() ? null : ret;
    }

    private static List<ShopItemStack> fromJsonInJsonArrayToShopItemstackList(JsonArray json) {
        List<ShopItemStack> ret = new ArrayList<>();
        Map<ShopItemStackSettingKeys, ObjectHolder<?>> shopItemStackSettings = new HashMap<>();
        new ArrayList<>(json.asList()).forEach((jsonElement) -> {
            JsonObject jsonItem = jsonElement.getAsJsonObject();
            shopItemStackSettings.clear();
            try {
                jsonToMap(jsonItem.get("itemSettings").getAsString()).forEach((key, oh) -> shopItemStackSettings.put(ShopItemStackSettingKeys.valueOf(key), (ObjectHolder<?>) oh));
                ret.add(new ShopItemStack(fromJsonToItemStack(jsonItem.get("itemStackString").getAsString()), shopItemStackSettings));
            } catch (JsonSerializer.JsonSyntaxException ignored) {
            }
        });
        ret.remove(null);

        return ret.isEmpty() ? null : ret;
    }
}
