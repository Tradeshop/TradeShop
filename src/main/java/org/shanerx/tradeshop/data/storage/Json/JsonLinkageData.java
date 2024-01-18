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

import org.bukkit.World;
import org.shanerx.tradeshop.data.storage.LinkageConfiguration;

import java.util.Map;

public class JsonLinkageData extends JsonConfiguration implements LinkageConfiguration {

    Map<String, Object> linkageData;

    public JsonLinkageData(World world) {
        super(world.getName(), "chest_linkage");
        load();
    }

    @Override
    public void load() {
        loadFile();
        linkageData = getMapParameterized("linkage_data");
    }

    @Override
    public Map<String, Object> getLinkageData() {
        return linkageData;
    }

    @Override
    public void save() {
        if (linkageData == null) load();
        set("linkage_data", linkageData);
        saveFile();
    }
}
