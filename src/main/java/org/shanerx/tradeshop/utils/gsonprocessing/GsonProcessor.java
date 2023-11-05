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
import com.bergerkiller.bukkit.common.utils.LogicUtil;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class GsonProcessor {
    private static final JsonSerializer jsonSerializer = new JsonSerializer();

    public static ItemStack fromJsonToItemStack(String json) throws JsonSerializer.JsonSyntaxException {
        return jsonSerializer.fromJsonToItemStack(json);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> jsonToMap(String json) throws JsonSerializer.JsonSyntaxException {
        return jsonSerializer.fromJson(json, Map.class);
    }

    public static <T> T fromJson(String json, Class<T> type) throws JsonSerializer.JsonSyntaxException {
        return jsonSerializer.fromJson(json, type);
    }

    public static String itemStackToJson(ItemStack item) {
        return jsonSerializer.mapToJson(LogicUtil.serializeDeep(item));
    }

    public static String mapToJson(Map<String, Object> map) {
        return jsonSerializer.mapToJson(map);
    }

    public static String toJson(Object value) {
        return jsonSerializer.toJson(value);
    }
}
