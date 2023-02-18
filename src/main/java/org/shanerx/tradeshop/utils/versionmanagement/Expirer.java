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

package org.shanerx.tradeshop.utils.versionmanagement;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitScheduler;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.player.Permissions;
import org.shanerx.tradeshop.utils.debug.DebugLevels;

import java.io.InputStreamReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;

public class Expirer {
    private final TradeShop plugin;

    public Expirer(TradeShop plugin) {
        this.plugin = plugin;
    }

    public boolean initiateDevExpiration() {
        if (!plugin.getDescription().getVersion().toUpperCase().endsWith("DEV") || plugin.getResource("builddate.yml") == null)
            return false;

        YamlConfiguration buildDateFile = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("builddate.yml")));

        LocalDateTime buildTime = LocalDateTime.parse(buildDateFile.getString("buildtime").replace(".", ":"));
        BukkitScheduler scheduler = plugin.getServer().getScheduler();

        final int devExpirationDays = 60, disableDayMessages = 25, expiredMessagesPerDay = 10;
        final long postExpirationRunTimeInTicks = 1728000, ticksPerDay = 1728000;


        if (buildTime.plusDays(devExpirationDays).isBefore(ChronoLocalDateTime.from(LocalDateTime.now()))) {

            // Sends a repeating message notifying of updates
            scheduler.runTaskTimerAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    plugin.getDebugger().log("You are currently running a DEV version that is past its expiration date. The plugin will disable soon!", DebugLevels.DEV_EXPIRATION);
                    plugin.getDebugger().log("Please update IMMEDIATELY to a newer DEV version for testing or BETA/RELEASE if on a live server.", DebugLevels.DEV_EXPIRATION);
                }
            }, 5L, postExpirationRunTimeInTicks / disableDayMessages);

            // Sends a messages when 90% of the run time is up
            scheduler.runTaskLaterAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    // Calculates then amount of seconds of 10% of the total time
                    long remainingTime = (long) (postExpirationRunTimeInTicks * 0.1 / 20);

                    plugin.getDebugger().log("Post expiration use time will expire and the plugin will be disabled in: " + formatSeconds(remainingTime), DebugLevels.DEV_EXPIRATION);
                    plugin.getServer().broadcast(colorize(plugin.getDebugger().getFormattedPrefix(DebugLevels.DEV_EXPIRATION) + "&4Post expiration use time will expire and the plugin will be disabled in: " + formatSeconds(remainingTime)), Permissions.ADMIN.getPerm().toString());
                }
            }, (long) (postExpirationRunTimeInTicks * 0.9));

            // Sends a messages when 95% of the run time is up
            scheduler.runTaskLaterAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    // Calculates then amount of seconds of 5% of the total time
                    long remainingTime = (long) (postExpirationRunTimeInTicks * 0.05 / 20);

                    plugin.getDebugger().log("Post expiration use time will expire and the plugin will be disabled in: " + formatSeconds(remainingTime), DebugLevels.DEV_EXPIRATION);
                    plugin.getServer().broadcast(colorize(plugin.getDebugger().getFormattedPrefix(DebugLevels.DEV_EXPIRATION) + "&4Post expiration use time will expire and the plugin will be disabled in: " + formatSeconds(remainingTime)), Permissions.ADMIN.getPerm().toString());
                }
            }, (long) (postExpirationRunTimeInTicks * 0.95));

            // Disables the plugin when time is up
            scheduler.runTaskLaterAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    plugin.getDebugger().log("Post expiration use time has Expired! The plugin will now disable.", DebugLevels.DEV_EXPIRATION);
                    plugin.getServer().broadcast(colorize(plugin.getDebugger().getFormattedPrefix(DebugLevels.DEV_EXPIRATION) + "&4Post expiration use time has Expired! The plugin will now disable."), Permissions.ADMIN.getPerm().toString());
                    plugin.getServer().getPluginManager().disablePlugin(plugin);
                }
            }, postExpirationRunTimeInTicks);

            return true;

            // Messages start after half of the expiration time
        } else if (buildTime.plusDays(devExpirationDays / 2).isBefore(ChronoLocalDateTime.from(LocalDateTime.now()))) {
            // Sends a repeating message notifying of updates
            scheduler.runTaskTimerAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    plugin.getDebugger().log("You are currently running a DEV version that is past its expiration date.", DebugLevels.DEV_EXPIRATION);
                    plugin.getDebugger().log("Please update to a newer DEV version for testing or BETA/RELEASE if on a live server.", DebugLevels.DEV_EXPIRATION);
                }
            }, 5L, ticksPerDay / expiredMessagesPerDay);

            return true;
        }

        return false;
    }

    public String colorize(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public String formatSeconds(long seconds) {
        Duration duration = Duration.ofSeconds(seconds);

        return String.format("%02d:%02d:%02d", duration.toHours(), duration.toMinutes() % 60, seconds % 60);
    }
}
