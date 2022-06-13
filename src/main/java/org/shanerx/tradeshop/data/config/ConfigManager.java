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

package org.shanerx.tradeshop.data.config;

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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class ConfigManager {

    private final TradeShop PLUGIN;
    private final ConfigType configType;
    private final File file;
    private FileConfiguration config;


    public ConfigManager(TradeShop plugin, ConfigType configType) {
        this.PLUGIN = plugin;
        this.configType = configType;

        file = new File(PLUGIN.getDataFolder(), configType.toFileName());

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

    public boolean load() {
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

        return setDefaults();
    }

    public void reload() {
        Set<Boolean> hasUpgraded = new HashSet<>();
        hasUpgraded.add(load());

        switch (configType) {
            case CONFIG:
                hasUpgraded.add(Setting.upgrade());
                break;
            case MESSAGES:
                hasUpgraded.add(Message.upgrade());
                break;
        }

        save(hasUpgraded.contains(true));

        PLUGIN.setSkipHopperProtection(
                Setting.BITRADESHOP_HOPPER_EXPORT.getBoolean() &&
                        Setting.BITRADESHOP_HOPPER_IMPORT.getBoolean() &&
                        Setting.TRADESHOP_HOPPER_IMPORT.getBoolean() &&
                        Setting.TRADESHOP_HOPPER_EXPORT.getBoolean());
    }

    public boolean setDefaults() {
        Set<Boolean> hasUpgraded = new HashSet<>();

        switch (configType) {
            case CONFIG:
                Arrays.stream(Setting.values()).forEach((setting) -> hasUpgraded.add(addKeyValue(setting.getPath(), setting.getDefaultValue())));
                break;
            case MESSAGES:
                Arrays.stream(Message.values()).forEach((message) -> hasUpgraded.add(addKeyValue(message.getPath(), message.getDefaultValue())));
                break;
        }

        return hasUpgraded.contains(true);
    }

    /**
     * Saves the file if passed boolean is true
     *
     * @param shouldSave true if save should proceed
     */
    public void save(boolean shouldSave) {
        if (shouldSave) save();
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
                        Arrays.stream(Setting.values()).forEach((setting) -> outputMap.put(setting.getSection().getPath(),
                                outputMap.getOrDefault(setting.getSection().getPath(), "") + setting.getFileString()));
                        break;
                    case MESSAGES:
                        Arrays.stream(MessageSection.values()).sorted(new CompareMessageSections()).forEach((section) -> outputMap.put(section.getPath(), section.getFileString()));
                        Arrays.stream(Message.values()).forEach((message) -> outputMap.put(message.getSection().getPath(),
                                outputMap.getOrDefault(message.getSection().getPath(), "") + message.getFileString()));
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


        config = YamlConfiguration.loadConfiguration(file);
    }

    private boolean addKeyValue(String node, Object value) {
        if (value instanceof Map) {
            for (Map.Entry entry : ((Map<?, ?>) value).entrySet()) {
                String newNode = node + "." + entry.getKey().toString();
                if (config.get(newNode) == null || (config.get(newNode) != null && config.get(newNode).toString().isEmpty())) {
                    config.set(newNode, entry.getValue().toString());
                    return true;
                }
            }
        } else if (config.get(node) == null || (config.get(node) != null && config.get(node).toString().isEmpty())) {
            config.set(node, value);
            return true;
        }

        return false;
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

        public String toFileName() {
            return name().toLowerCase() + ".yml";
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
                // if neither is the parent of the other but one Section has a parent and the other doesn't
            } else if (o1.hasParent() != o2.hasParent()) {
                return o1.hasParent() ? compare(o1.getParent(), o2) : compare(o1, o2.getParent()); // compare existing parent to other section
                // if both have parents but they aren't the same parent
            } else if (o1.getParent() != null && !o1.getParent().equals(o2.getParent())) {
                return compare(o1.getParent(), o2.getParent()); // compare the parents of both
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
                // if neither is the parent of the other but one Section has a parent and the other doesn't
            } else if (o1.hasParent() != o2.hasParent()) {
                return o1.hasParent() ? compare(o1.getParent(), o2) : compare(o1, o2.getParent()); // compare existing parent to other section
                // if both have parents but they aren't the same parent
            } else if (o1.getParent() != null && !o1.getParent().equals(o2.getParent())) {
                return compare(o1.getParent(), o2.getParent()); // compare the parents of both
            }

            // Otherwise, compare weight
            return o1.getWeight() - o2.getWeight();
        }
    }
}