/*
 *     Copyright (c) 2016-2017 SparklingComet @ http://shanerx.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * NOTICE: All modifications made by others to the source code belong
 * to the respective contributor. No contributor should be held liable for
 * any damages of any kind, whether be material or moral, which were
 * caused by their contribution(s) to the project. See the full License for more information
 */

package org.shanerx.tradeshop.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.shanerx.tradeshop.TradeShop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;

public class CustomItem {

    private static TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
    private static File file = new File(plugin.getDataFolder(), "customitems.yml");
    private static FileConfiguration config = YamlConfiguration.loadConfiguration(file);
    private static final char COLOUR_CHAR = '&';

    public static void addItem(String name, ItemStack itm) {
        if (!config.getValues(false).containsKey(name)) {
            config.createSection(name);

            config.set(name, itm.serialize());

            save();
        }
    }

    public static void removeItem(String name) {
        if (config.getValues(false).containsKey(name)) {
            config.set(name, null);

            save();
        }
    }

    public static Set<String> getItemSet() {
        return config.getValues(false).keySet();
    }

    private static void setDefaults() {
        if (config.getValues(false).isEmpty()) {
            ItemStack dataHolder = new ItemStack(Material.TRIPWIRE_HOOK);
            ItemMeta meta = dataHolder.getItemMeta();

            meta.setDisplayName("Key");
            meta.setLore(Arrays.asList("&aThe key to your dreams."));
            dataHolder.setItemMeta(meta);

            addItem("Key", dataHolder);
        }
    }

    public static ItemStack getItem(String name) {
        ItemStack itm = null;

        if (!(config.get(name) == null)) {
            itm = ItemStack.deserialize(config.getConfigurationSection(name).getValues(true));
            ItemMeta meta = itm.getItemMeta();

            if (meta.hasLore()) {
                ArrayList<String> str2 = new ArrayList<>();
                for (String s : meta.getLore()) {
                    str2.add(ChatColor.translateAlternateColorCodes('&', s));
                }
                meta.setLore(str2);
            }

            if (meta.hasDisplayName())
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', meta.getDisplayName()));

            itm.setItemMeta(meta);
        }

        return itm;
    }

    public static void reload() {
        try {
            if (!plugin.getDataFolder().isDirectory()) {
                plugin.getDataFolder().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not create Config file! Disabling plugin!", e);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }

        config = YamlConfiguration.loadConfiguration(file);
        setDefaults();
    }

    private static void save() {
        if (config != null) {
            try {
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration getConfig() {
        return config;
    }

    public static String colour(String x) {
        return ChatColor.translateAlternateColorCodes(COLOUR_CHAR, x);
    }
}
