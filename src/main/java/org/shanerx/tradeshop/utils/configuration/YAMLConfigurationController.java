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

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.utils.configuration.interfaces.YAMLConfigurationInterface;
import org.shanerx.tradeshop.utils.configuration.interfaces.YAMLConfigurationSectionInterface;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class YAMLConfigurationController {

    protected final TradeShop plugin;
    protected final File file;
    protected FileConfiguration config;

    public YAMLConfigurationController(TradeShop plugin, String fileNameNoExtension) {
        file = new File(plugin.getDataFolder(), fileNameNoExtension + ".yml");
        config = YamlConfiguration.loadConfiguration(file);
        this.plugin = plugin;
    }

    public void addKey(String path, Object value) {
        if (config.get(path) == null) {
            config.set(path, value);
        }
    }

    public void load() {
        try {
            if (!plugin.getDataFolder().isDirectory()) {
                plugin.getDataFolder().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not create " + file.getName() + " file! Disabling plugin!", e);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        Validate.notNull(file, "File cannot be null");

        if (config != null)
            try {
                Files.createParentDirs(file);

                // Map<Section.getPath, outputTest++>
                HashMap<String, String> outputMap = new HashMap<>();

                StringBuilder data = new StringBuilder();

                String bar = "", name = config.getString("file-name");

                for (int i = name.length() + 10; i > 0; i--) {
                    bar += "#";
                }

                data.append(bar).append("\n");
                data.append("#    ").append(name).append("    #\n");
                data.append(bar).append("\n");

                Set<YAMLConfigurationSectionInterface> sectionKeys = null;
                Set<YAMLConfigurationInterface> keys = null;

                if (name.contains("Config")) {
                    sectionKeys = Sets.newHashSet(SettingSectionKey.values());
                    keys = Sets.newHashSet(SettingsEnum.values());
                } else if (name.contains("Messages")) {
                    sectionKeys = Sets.newHashSet(MessageSectionKey.values());
                    keys = Sets.newHashSet(MessagesEnum.values());
                }

                // Create Header for each Section and add it to the map
                for (YAMLConfigurationSectionInterface section : sectionKeys) {
                    StringBuilder sectionHeader = new StringBuilder();

                    // If Section has a key add section to file
                    if (!section.getPath().isEmpty()) {

                        // If Section has a header format it
                        if (!section.getSectionHeader().isEmpty()) {

                            // First line of Section Header
                            sectionHeader.append(section.getFullLead()).append("# |    ").append(section.getSectionHeader()).append("    |");

                            // Second line of Section Header
                            sectionHeader.append("\n").append(section.getFullLead()).append("# ");

                            // Create `^` line
                            // Length should be length of header text + `|    ` x2 for each side(10 total)
                            for (int i = section.getSectionHeader().length() + 10; i > 0; i--) {
                                sectionHeader.append("^");
                            }

                            sectionHeader.append("\n");

                        }

                        // If Section has a Pre Comment add it
                        if (!section.getPreComment().isEmpty()) {
                            sectionHeader.append(section.getFullLead()).append("# ").append(fixCommentNewLines(section, section.getPreComment())).append("\n");
                        }

                        // Add Sections Key line
                        sectionHeader.append(section.getFileText()).append("\n");

                        // If Section has a Post Comment add it
                        if (!section.getPostComment().isEmpty()) {
                            sectionHeader.append(section.getFullLead()).append("# ").append(fixCommentNewLines(section, section.getPostComment())).append("\n");
                        }
                    }

                    // Add the section and its header to the output map
                    outputMap.put(section.getPath(), sectionHeader.toString());
                }

                for (YAMLConfigurationInterface key : keys) {
                    StringBuilder keyOutput = new StringBuilder(outputMap.get(key.getSectionKey().getPath()));

                    if (!key.getPreComment().isEmpty()) {
                        keyOutput.append(key.getSectionKey().getFullLead()).append("# ").append(key.getPreComment()).append("\n");
                    }

                    keyOutput.append(key.getSectionKey().getFullLead()).append(key.getKey()).append(": ").append(new Yaml().dump(getValueAsObject(key)));

                    if (!key.getPostComment().isEmpty()) {
                        keyOutput.append(key.getPostComment()).append("\n");
                    }

                    outputMap.put(key.getSectionKey().getPath(), keyOutput.toString());
                }

                // List to be filled with section keys sorted by weight
                List<YAMLConfigurationSectionInterface> sortedSectionList = new ArrayList<>(sectionKeys);

                sortedSectionList.sort(new Comparator<YAMLConfigurationSectionInterface>() {
                    @Override
                    public int compare(YAMLConfigurationSectionInterface section1, YAMLConfigurationSectionInterface section2) {
                        // Returns Weight 0 -> ++ and Orphaned First
                        if (section1.hasParent() != section2.hasParent())
                            return section1.hasParent() ? 1 : -1;

                        return section1.getWeight() - section2.getWeight();
                    }
                });

                // Adds the formatted strings from the outputData map to data in the above sorted order
                sortedSectionList.forEach((section) -> data.append(outputMap.get(section.getPath())));

                Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8);

                try {
                    writer.write(data.toString());
                } finally {
                    writer.close();
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public FileConfiguration getYAMLConfig() {
        return config;
    }

    private String fixCommentNewLines(YAMLConfigurationSectionInterface value, String str) {
        return str.replace("\n ", "\n" + value.getFullLead() + "# ");
    }

    public void setKey(YAMLConfigurationInterface value, Object obj) {
        config.set(value.getPath(), obj);
    }

    public void clearKey(YAMLConfigurationInterface value) {
        config.set(value.getPath(), null);
    }

    public Object getValueAsObject(YAMLConfigurationInterface value) {
        return config.get(value.getPath());
    }

    public String getValueAsString(YAMLConfigurationInterface value) {
        return config.getString(value.getPath());
    }

    public List<String> getValueAsStringList(YAMLConfigurationInterface value) {
        return config.getStringList(value.getPath());
    }

    public int getValueAsInt(YAMLConfigurationInterface value) {
        return config.getInt(value.getPath());
    }

    public double getValueAsDouble(YAMLConfigurationInterface value) {
        return config.getDouble(value.getPath());
    }

    public boolean getValueAsBoolean(YAMLConfigurationInterface value) {
        return config.getBoolean(value.getPath());
    }
}
