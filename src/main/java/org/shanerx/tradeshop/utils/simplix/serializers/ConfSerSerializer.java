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

import java.util.LinkedHashMap;
import java.util.Map;

public class ConfSerSerializer {
    public static ItemStack deserializeItemStack(Map<String, Object> map) {
        return (ItemStack) ConfigurationSerialization.deserializeObject(map, ItemStack.class);
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
