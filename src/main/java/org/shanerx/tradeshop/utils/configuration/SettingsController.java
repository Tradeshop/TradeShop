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

package org.shanerx.tradeshop.utils.configuration;

import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.utils.configuration.interfaces.YAMLConfigurationControllerInterface;
import org.shanerx.tradeshop.utils.configuration.interfaces.YAMLConfigurationInterface;

import java.util.Arrays;

public class SettingsController extends YAMLConfigurationController implements YAMLConfigurationControllerInterface {

    public SettingsController(TradeShop plugin) {
        super(plugin, "config");
        reload();
    }

    @Override
    public YAMLConfigurationInterface findKey(String name) {
        return SettingsEnum.valueOf(name.toUpperCase().replace("-", "_"));
    }

    @Override
    public void setDefaults() {
        for (SettingsEnum setting : SettingsEnum.values()) {
            addKey(setting.getPath(), setting.getDefaultValue());
        }
    }

    @Override
    public void reload() {
        load();

        setDefaults();

        upgrade();

        save();

        plugin.setUseInternalPerms(getValueAsBoolean(SettingsEnum.USE_INTERNAL_PERMISSIONS));
    }

    @Override
    public void upgrade() {
        double startVersion = getValueAsDouble(SettingsEnum.CONFIG_VERSION), version = startVersion;

        // Don't accept changes to the filename...
        setKey(SettingsEnum.FILE_NAME, SettingsEnum.FILE_NAME.getDefaultValue());

        // 2.2.2 Changed enable debug from true/false to integer
        if (!config.isInt(SettingsEnum.ENABLE_DEBUG.getPath())) {
            clearKey(SettingsEnum.ENABLE_DEBUG);
        }

        // 2.2.2 Better Sorted/potentially commented config
        if (version < 1.1) {
            if (config.contains("itradeshop.owner")) {
                config.set(SettingsEnum.ITRADESHOP_OWNER.getPath(), config.get("itradeshop.owner"));
                config.set("itradeshop.owner", null);
            }

            if (config.contains("itradeshop.header")) {
                config.set(SettingsEnum.ITRADESHOP_HEADER.getPath(), config.get("itradeshop.header"));
                config.set("itradeshop.header", null);
            }

            if (config.contains("itradeshop.allow-explode")) {
                config.set(SettingsEnum.ITRADESHOP_EXPLODE.getPath(), config.get("itradeshop.allow-explode"));
                config.set("itradeshop.allow-explode", null);
            }

            if (config.contains("tradeshop.header")) {
                config.set(SettingsEnum.TRADESHOP_HEADER.getPath(), config.get("tradeshop.header"));
                config.set("tradeshop.header", null);
            }

            if (config.contains("tradeshop.allow-explode")) {
                config.set(SettingsEnum.TRADESHOP_EXPLODE.getPath(), config.get("tradeshop.allow-explode"));
                config.set("tradeshop.allow-explode", null);
            }

            if (config.contains("tradeshop.allow-hopper-export")) {
                config.set(SettingsEnum.TRADESHOP_HOPPER_EXPORT.getPath(), config.get("tradeshop.allow-hopper-export"));
                config.set("tradeshop.allow-hopper-export", null);
            }

            if (config.contains("bitradeshop.header")) {
                config.set(SettingsEnum.BITRADESHOP_HEADER.getPath(), config.get("bitradeshop.header"));
                config.set("bitradeshop.header", null);
            }

            if (config.contains("bitradeshop.allow-explode")) {
                config.set(SettingsEnum.BITRADESHOP_EXPLODE.getPath(), config.get("bitradeshop.allow-explode"));
                config.set("bitradeshop.allow-explode", null);
            }

            if (config.contains("bitradeshop.allow-hopper-export")) {
                config.set(SettingsEnum.BITRADESHOP_HOPPER_EXPORT.getPath(), config.get("bitradeshop.allow-hopper-export"));
                config.set("bitradeshop.allow-hopper-export", null);
            }


            version = 1.1;
        }

        if (version < 1.2) {
            if (config.contains("global-options.illegal-items")) {
                setKey(SettingsEnum.GLOBAL_ILLEGAL_ITEMS_LIST, config.getStringList("global-options.illegal-items").removeAll(Arrays.asList("Air", "Void_Air", "Cave_Air")));
                config.set("global-options.illegal-items", null);
            }

            version = 1.2;
        }

        if (startVersion != version) {
            setKey(MessagesEnum.MESSAGE_VERSION, version);
        }
    }
}
