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

package org.shanerx.tradeshop.utils.gsonprocessing.typeadapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.shanerx.tradeshop.utils.debug.Debug;
import org.shanerx.tradeshop.utils.debug.DebugLevels;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

//Based off of ConfigurationSerializableAdapter by Schottky <https://www.spigotmc.org/members/schottky.632864/> @ https://www.spigotmc.org/threads/configurationserializable-to-json-using-gson.467776/
public class ConfigurationSerializableAdapter implements JsonSerializer<ConfigurationSerializable>, JsonDeserializer<ConfigurationSerializable> {

    final Type objectStringMapType = new TypeToken<Map<String, Object>>() {
    }.getType();

    @Override
    public ConfigurationSerializable deserialize(
            JsonElement json,
            Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {
        final Map<String, Object> map = new LinkedHashMap<>();

        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
            final JsonElement value = entry.getValue();
            final String name = entry.getKey();

            if (value.isJsonObject() && value.getAsJsonObject().has(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                map.put(name, this.deserialize(value, value.getClass(), context));
            } else {
                Object val = context.deserialize(value, Object.class);
                map.put(name, val instanceof Double && ((Double) val) % 1 == 0 ? ((Double) val).intValue() : val);
            }
        }

        return ConfigurationSerialization.deserializeObject(map);
    }

    @Override
    public JsonElement serialize(
            ConfigurationSerializable src,
            Type typeOfSrc,
            JsonSerializationContext context) {

        final Map<String, Object> map = new LinkedHashMap<>();
        map.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(src.getClass()));
        map.putAll(configSerRecursiveSerialize(src));
        return context.serialize(map, objectStringMapType);
    }

    private Map<String, Object> configSerRecursiveSerialize(ConfigurationSerializable src) {
        final Map<String, Object> map = new LinkedHashMap<>();
        Debug debug = Debug.findDebugger();
        debug.log("configSerRecursiveSerialize 4Ea : src | " + src.serialize(), DebugLevels.GSON);
        src.serialize().forEach((string, object) -> {
            debug.log("configSerRecursiveSerialize 4Ea : " + string + " | " + object.toString(), DebugLevels.GSON);
            if (object instanceof ConfigurationSerializable) {
                map.put(string, configSerRecursiveSerialize((ConfigurationSerializable) object));
                debug.log("configSerRecursiveSerialize 4Ea Tunneling...", DebugLevels.GSON);
            } else {
                map.put(string, object);
                debug.log("configSerRecursiveSerialize 4Ea Launching...", DebugLevels.GSON);
            }
        });

        return map;
    }
}
