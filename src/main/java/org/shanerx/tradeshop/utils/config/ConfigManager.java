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

package org.shanerx.tradeshop.utils.config;

import com.google.common.base.CaseFormat;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.shanerx.tradeshop.TradeShop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.logging.Level;

public class ConfigManager {

    private final TradeShop PLUGIN;
    private final ConfigType configType;
    private File file;
    private FileConfiguration config;


    public ConfigManager(TradeShop plugin, ConfigType configType) {
        this.PLUGIN = plugin;
        this.configType = configType;

        switch (configType) {
            case CONFIG:
                file = new File(PLUGIN.getDataFolder(), "config.yml");
                break;
            case MESSAGES:
                file = new File(PLUGIN.getDataFolder(), "messages.yml");
                break;
        }


        load();
    }

    public File getFile() {
        return file;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public String fixCommentNewLines(String lineLead, String str) {
        return str.replace("\n ", "\n" + lineLead + "# ");
    }

    public String colour(String toColour) {
        return ChatColor.translateAlternateColorCodes('&', toColour);
    }

    public void load() {
        try {
            if (!PLUGIN.getDataFolder().isDirectory()) {
                PLUGIN.getDataFolder().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            PLUGIN.getLogger().log(Level.SEVERE, "Could not create " + file.getName() + " file! Disabling plugin!", e);
            PLUGIN.getServer().getPluginManager().disablePlugin(PLUGIN);
        }

        config = YamlConfiguration.loadConfiguration(file);

        setDefaults();
    }

    public void reload() {
        load();

        switch (configType) {
            case CONFIG:
                Setting.upgrade();
                PLUGIN.setUseInternalPerms(Setting.USE_INTERNAL_PERMISSIONS.getBoolean());
                break;
            case MESSAGES:
                Message.upgrade();
                break;
        }

        save();
    }

    public void setDefaults() {
        switch (configType) {
            case CONFIG:
                Arrays.stream(Setting.values()).forEach((setting) -> addKeyValue(setting.getPath(), setting.getDefaultValue()));
                break;
            case MESSAGES:
                Arrays.stream(Message.values()).forEach((message) -> addKeyValue(message.getPath(), message.getDefaultValue()));
                break;
        }
    }

    public void save() {
        Validate.notNull(file, "File cannot be null");

        if (config != null)
            try {
                Files.createParentDirs(file);

                // Map<Section.getPath, outputText++>
                LinkedHashMap<String, String> outputMap = new LinkedHashMap<>();

                StringBuilder data = new StringBuilder();

                String name = "TradeShop " + configType;
                StringBuilder bar = new StringBuilder();

                for (int i = name.length() + 10; i > 0; i--) {
                    bar.append("#");
                }

                data.append(bar).append("\n");
                data.append("#    ").append(name).append("    #\n");
                data.append(bar).append("\n");

                switch (configType) {
                    case CONFIG:
                        Arrays.stream(SettingSection.values()).sorted(new CompareSettingSections()).forEach((section) -> outputMap.put(section.getPath(), section.getFileString()));
                        Arrays.stream(Setting.values()).forEach((section) -> outputMap.put(section.getSection().getPath(),
                                outputMap.getOrDefault(section.getSection().getPath(), "") + section.getFileString()));
                        break;
                    case MESSAGES:
                        Arrays.stream(MessageSection.values()).sorted(new CompareMessageSections()).forEach((section) -> outputMap.put(section.getPath(), section.getFileString()));
                        Arrays.stream(Message.values()).forEach((section) -> outputMap.put(section.getSection().getPath(),
                                outputMap.getOrDefault(section.getSection().getPath(), "") + section.getFileString()));
                        break;
                }

                // Adds the formatted strings from the outputData map to data in the above sorted order
                outputMap.forEach((k, v) -> data.append(v));

                // Automatically closes once it's done
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8)) {
                    writer.write(data.toString());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private void addKeyValue(String node, Object value) {
        if (config.get(node) == null) {
            config.set(node, value);
        }
    }

    public enum ConfigType {
        CONFIG,
        MESSAGES;

        ConfigType() {
        }

        @Override
        public String toString() {
            return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name());
        }
    }

    static class CompareSettingSections implements Comparator<SettingSection> {
        @Override
        public int compare(SettingSection o1, SettingSection o2) {
            // if o2's parent is o1 then o1 goes first
            if (o2.getParent() == o1) {
                return -1;
                // if o1's parent is o2 then o2 goes first
            } else if (o1.getParent() == o2) {
                return 1;
                // if neither of the above are true and one Section has a parent and the other doesn't, then the one without the parent goes first
            }

            // Otherwise, compare weight
            return o1.getWeight() - o2.getWeight();
        }
    }

    static class CompareMessageSections implements Comparator<MessageSection> {
        @Override
        public int compare(MessageSection o1, MessageSection o2) {
            // if o2's parent is o1 then o1 goes first
            if (o2.getParent() == o1) {
                return -1;
                // if o1's parent is o2 then o2 goes first
            } else if (o1.getParent() == o2) {
                return 1;
                // if neither of the above are true and one Section has a parent and the other doesn't, then the one without the parent goes first
            } else if (o1.hasParent() != o2.hasParent()) {
                return o1.hasParent() ? 1 : -1;
            }

            // Otherwise, compare weight
            return o1.getWeight() - o2.getWeight();
        }
    }
}