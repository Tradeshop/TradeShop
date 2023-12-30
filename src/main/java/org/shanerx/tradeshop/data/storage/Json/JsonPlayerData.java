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

import org.shanerx.tradeshop.data.storage.PlayerConfiguration;
import org.shanerx.tradeshop.player.PlayerSetting;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class JsonPlayerData extends JsonConfiguration implements PlayerConfiguration {

    private final transient UUID playerUUID;
    private transient PlayerSetting playerSetting;
    private static final String playerFolder = "Players";

    public JsonPlayerData(UUID uuid) {
        super(playerFolder, uuid.toString());

        playerUUID = uuid;
    }

    public static List<File> getAllPlayers() {
        return getFilesInFolder(playerFolder);
    }

    @Override
    public void save(PlayerSetting playerSetting) {
        this.playerSetting = playerSetting;
        set(playerSetting.getUuid().toString(), playerSetting.serialize());

        saveFile();
    }

    @Override
    public PlayerSetting load() {
        playerSetting = PlayerSetting.deserialize(getMapParameterized(playerUUID.toString()));

        if (playerSetting == null) {
            playerSetting = new PlayerSetting(playerUUID);
            save(playerSetting);
        }

        playerSetting.load();

        return playerSetting;
    }

    @Override
    public void remove() {
        super.remove(playerUUID.toString());
    }
}
