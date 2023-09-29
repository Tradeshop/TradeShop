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

import com.google.gson.reflect.TypeToken;
import org.shanerx.tradeshop.data.storage.PlayerConfiguration;
import org.shanerx.tradeshop.player.PlayerSetting;

import java.io.File;
import java.util.Map;
import java.util.UUID;

public class JsonPlayerConfiguration extends JsonConfiguration implements PlayerConfiguration {

    private final transient UUID playerUUID;
    private transient PlayerSetting playerSetting;
    private static final String playerFolder = "Players";

    public JsonPlayerConfiguration(UUID uuid) {
        super(playerFolder, uuid.toString());

        playerUUID = uuid;
    }

    @Override
    public void save(PlayerSetting playerSetting) {
        this.playerSetting = playerSetting;
        jsonObj.add(playerSetting.getUuid().toString(), gson.toJsonTree(playerSetting));

        saveFile();
    }

    @Override
    public PlayerSetting load() {
        if (jsonObj.has("data")) {
            playerSetting = new PlayerSetting(playerUUID, gson.fromJson(jsonObj.get("data"), new TypeToken<Map<String, Integer>>() {
            }.getType()));
            jsonObj.remove("data");
            saveFile();
        } else {
            playerSetting = gson.fromJson(jsonObj.get(playerUUID.toString()), PlayerSetting.class);
        }

        if (playerSetting != null)
            playerSetting.load();

        return playerSetting;
    }

    @Override
    public void remove() {
        file.delete();
    }

    public static File[] getAllPlayers() {
        return getFilesInFolder(playerFolder);
    }
}
