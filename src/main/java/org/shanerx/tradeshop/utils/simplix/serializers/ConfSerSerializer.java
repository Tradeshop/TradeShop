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

package org.shanerx.tradeshop.utils.simplix.serializers;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.LinkedHashMap;
import java.util.Map;

public class ConfSerSerializer {
    public static ItemStack deserializeItemStack(Map<String, Object> map) {
        return (ItemStack) ConfigurationSerialization.deserializeObject(reviveNestedMeta(map), ItemStack.class);
    }

    /**
     * Rebuilds a pre-1.20.5 record's nested {@code meta} map into a real {@link ItemMeta}.
     *
     * <p>A record written before data components looks like
     * <pre>{"type":"DIAMOND_SWORD","v":3700,"meta":{"meta-type":"UNSPECIFIC",
     * "enchants":{"DAMAGE_ALL":3},"display-name":...,"lore":[...]}}</pre>
     * and {@link org.bukkit.inventory.ItemStack#deserialize} applies {@code meta} only
     * when it is already an {@code instanceof ItemMeta}. A plain {@link Map} is silently
     * skipped, so the item came back as a bare sword: no enchantment, no name, no lore.
     *
     * <p>The reason it is a plain map is {@link #toMap} directly below, which flattens
     * nested {@code ConfigurationSerializable} values and drops the {@code "=="} type key
     * Bukkit's own YAML writer puts there. Putting that key back is exactly enough to
     * make the map the shape Bukkit deserialises natively.
     *
     * <p>Records written today do not reach any of this: since 1.20.5 an ItemStack
     * serialises to a component schema - {@code {"count":1,"schema_version":1,
     * "DataVersion":...,"id":"minecraft:diamond"}} - with no nested meta object at all.
     * So this is a reader-side migration for data already on disk, and nothing about
     * what gets written changes.
     *
     * @param map one stored item, in any generation's shape
     * @return the same map with {@code meta} revived, or the original when there is
     *         nothing to revive or it could not be read
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> reviveNestedMeta(Map<String, Object> map) {
        Object raw = map.get("meta");

        // Modern component record, or a meta Bukkit already built for us.
        if (!(raw instanceof Map)) return map;

        Map<String, Object> metaMap = new LinkedHashMap<>((Map<String, Object>) raw);
        metaMap.putIfAbsent(ConfigurationSerialization.SERIALIZED_TYPE_KEY, "ItemMeta");

        Object meta;
        try {
            meta = ConfigurationSerialization.deserializeObject(metaMap);
        } catch (IllegalArgumentException cannotBeRead) {
            // An item whose metadata cannot be read is still an item. Returning the map
            // untouched loses the metadata exactly as before rather than the whole stack.
            return map;
        }

        if (!(meta instanceof ItemMeta)) return map;

        Map<String, Object> revived = new LinkedHashMap<>(map);
        revived.put("meta", meta);
        return revived;
    }

    public static Map<String, Object> serialize(ConfigurationSerializable configurationSerializable) {
        return toMap(configurationSerializable);
    }

    public static Map<String, Object> toMap(ConfigurationSerializable src) {
        Map<String, Object> map = new LinkedHashMap<>(src.serialize());

        map.forEach((k, v) -> {
            if (v instanceof ConfigurationSerializable) {
                map.replace(k, toMap((ConfigurationSerializable) v));
            }
        });

        return map;
    }
}
