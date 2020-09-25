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

package org.shanerx.tradeshop.objects;

import org.shanerx.tradeshop.enumys.PlayerData;

import java.util.Map;
import java.util.UUID;

public class PlayerSetting {

    private transient UUID uuid;
    private transient Map<String, Integer> data;

    public PlayerSetting(UUID playerUUID, Map<String, Integer> data) {
        this.uuid = playerUUID;
        this.data = data;
        for (PlayerData key : PlayerData.values()) {
            if (!data.containsKey(key.getPath()))
                data.putIfAbsent(key.getPath(), key.getDefaultValue());
            else
                data.replace(key.getPath(), key.getDefaultValue());
        }
    }

    public Integer getObject(PlayerData key) {
        Integer value = data.get(key.getPath());
        return value != null ? value : key.getDefaultValue();
    }

    public void setObject(PlayerData key, Integer value) {
        if (!data.containsKey(key.getPath()))
            data.putIfAbsent(key.getPath(), value);
        else
            data.replace(key.getPath(), value);
    }

    public UUID getUuid() {
        return uuid;
    }

    public Map<String, Integer> getData() {
        return data;
    }
}

