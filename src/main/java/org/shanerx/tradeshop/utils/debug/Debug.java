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

package org.shanerx.tradeshop.utils.debug;

import org.bukkit.Bukkit;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.data.config.Setting;

import java.util.logging.Level;

public class Debug {

    private final String PREFIX = "[TradeShop Debug%level%] ";
    private int decimalDebugLevel;
    private String binaryDebugLevel;

    public Debug() {
        reload();
    }

    public static Debug findDebugger() {
        final TradeShop plugin = TradeShop.getPlugin();
        if (plugin != null) {
            return plugin.getVarManager().getDebugger();
        } else {
            return new Debug();
        }
    }

    public void reload() {
        decimalDebugLevel = Setting.ENABLE_DEBUG.getInt();
        if (decimalDebugLevel < 0) {
            decimalDebugLevel = DebugLevels.maxValue();
        }
        StringBuilder sb = new StringBuilder(Integer.toBinaryString(decimalDebugLevel));
        while (sb.length() < DebugLevels.levels())
            sb.insert(0, 0);

        binaryDebugLevel = sb.reverse().toString();


        if (decimalDebugLevel > 0) {
            Bukkit.getLogger().log(Level.INFO, PREFIX.replace("%level%", "") + "Debugging enabled!");
            Bukkit.getLogger().log(Level.INFO, PREFIX.replace("%level%", "") + "Decimal Debug level: " + decimalDebugLevel);
            Bukkit.getLogger().log(Level.INFO, PREFIX.replace("%level%", "") + "Debug levels: " + binaryDebugLevel);
        }
    }

    public void log(String message, DebugLevels level, String positionalNote) {
        StringBuilder messageBuilder = new StringBuilder();
        if (level.getPosition() > 0 && decimalDebugLevel <= 0) {
            return;
        }


        if (level.getPosition() > 0 && binaryDebugLevel.charAt(level.getPosition() - 1) == '1') {
            message = PREFIX.replace("%level%", level.getPrefix()) + message;
        } else if (level == DebugLevels.DISABLED) {
            message = PREFIX.replaceAll("( Debug.%level%)", "(D) ") + message;
        } else if (level.getPosition() < 0) {
            message = PREFIX.replace("%level%", level.getPrefix()) + message;
        }

        if (positionalNote != null && !positionalNote.isEmpty()) {
            messageBuilder.append("{ ").append(positionalNote).append(" }\n");
        }

        messageBuilder.append(message);

        Bukkit.getLogger().log(level.getLogLevel(), messageBuilder.toString());
    }

    public void log(String message, DebugLevels level) {
        log(message, level, null);
    }

    public String getFormattedPrefix(DebugLevels debugLevel) {
        return PREFIX.replace("%level%", debugLevel.getPrefix());
    }
}