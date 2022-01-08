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

import org.bukkit.configuration.file.YamlConfiguration;
import org.shanerx.tradeshop.TradeShop;

import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Language {

    private final String LANG_FILE = "Lang" + File.separator,
            defaultLang = "en-us";
    private final TradeShop PLUGIN;
    private String lang = defaultLang;
    private YamlConfiguration langYAML;

    public Language(TradeShop plugin) {
        this.PLUGIN = plugin;

        reload();
    }

    public void reload() {
        File messageFile = new File(PLUGIN.getDataFolder(), "messages.yml");
        if (PLUGIN.getDataFolder().isDirectory() && messageFile.exists()) {
            YamlConfiguration messageConfig = YamlConfiguration.loadConfiguration(messageFile);

            changeLang(messageConfig.getString("language"));
        }

        langYAML = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(PLUGIN.getResource(LANG_FILE + lang + ".yml"))));
    }

    public void changeLang(String newLang) {
        if (newLang == null || newLang.isEmpty() || PLUGIN.getResource(LANG_FILE + newLang.toLowerCase() + ".yml") == null) {
            newLang = defaultLang;
        }

        newLang = newLang.toLowerCase();

        // if the Language is changed rename the old lang files
        if (!lang.equals(newLang)) {
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss"));
            new File(PLUGIN.getDataFolder(), "config.yml").renameTo(new File(PLUGIN.getDataFolder(), "config.yml" + date + ".old"));
            new File(PLUGIN.getDataFolder(), "messages.yml").renameTo(new File(PLUGIN.getDataFolder(), "messages.yml" + date + ".old"));
        }

        lang = newLang;
    }

    public String getLang() {
        return lang;
    }

    public String getHeader(LangSection section, String path) {
        return langYAML.getString(section + "." + path + ".header", "");
    }

    public Object getDefault(LangSection section, String path) {
        return langYAML.get(section + "." + path + ".default", "");
    }

    public String getPreComment(LangSection section, String path) {
        return langYAML.getString(section + "." + path + ".pre-comment", "");
    }

    public String getPostComment(LangSection section, String path) {
        return langYAML.getString(section + "." + path + ".post-comment", "");
    }


    public enum LangSection {
        MESSAGE_SECTION,
        MESSAGE,
        SETTING_SECTION,
        SETTING;

        LangSection() {
        }

        @Override
        public String toString() {
            return name().toLowerCase().replace("_", "-");
        }
    }

}
