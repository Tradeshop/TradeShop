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
import org.shanerx.tradeshop.utils.objects.ObjectHolder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.logging.Level;

public abstract class Debug {

    protected final String PREFIX;

    protected HashSet<DebugLevels> debugToConsole = new HashSet<>(),
            debugToFile = new HashSet<>();

    public Debug(int version) {
        PREFIX = "[TradeShop Debug%V%%level%] ".replace("%V%", "V" + version);
        reload();
    }

    public static Debug findDebugger() {
        final TradeShop plugin = ((TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop"));
        if (plugin == null) return null;

        Debug debugger = plugin.getVarManager().getDebugger();
        if (debugger != null) return debugger;

        return newDebug();
    }

    /**
     * @return true if the debug should be V3
     */
    public static Optional<Boolean> detectDebugVersion() {
        Optional<Boolean> v3 = Optional.empty();

        ObjectHolder<Object> debugToConsole = new ObjectHolder<>(Setting.DEBUG_TO_CONSOLE.getSetting()),
                debugToFile = new ObjectHolder<>(Setting.DEBUG_TO_FILE.getSetting());

        if (debugToConsole.isNull()) {
            Setting.DEBUG_TO_CONSOLE.resetSetting();
            debugToConsole = new ObjectHolder<>(Setting.DEBUG_TO_CONSOLE.getSetting());
        }

        if (debugToFile.isNull()) {
            Setting.DEBUG_TO_FILE.resetSetting();
            debugToFile = new ObjectHolder<>(Setting.DEBUG_TO_FILE.getSetting());
        }


        boolean debugToConsoleAsList = debugToConsole.asStringList().orElse(Collections.emptyList()).isEmpty(),
                debugToFileAsList = debugToFile.isList() && debugToFile.asStringList().orElse(Collections.emptyList()).isEmpty(),
                debugToConsoleAsInt = debugToConsole.isInteger() && debugToConsole.asInteger() == 0,
                debugToFileAsInt = debugToFile.isInteger() && debugToFile.asInteger() == 0;

        if (debugToConsole.isInteger() && debugToFile.isInteger()) {
            v3 = Optional.of(true);
        } else if (debugToConsole.isList() && debugToFile.isList()) {
            v3 = Optional.of(true);
        } else if (debugToConsoleAsList || debugToFileAsList || debugToConsoleAsInt || debugToFileAsInt) {
            if (debugToConsoleAsInt || debugToConsoleAsList) {
                v3 = Optional.of(debugToFile.isList());
            } else {
                v3 = Optional.of(debugToConsole.isList());
            }
        }
        return v3;
    }

    public static Debug newDebug() {
        Optional<Boolean> debugVersion = detectDebugVersion();
        if (debugVersion.isPresent()) {
            return debugVersion.get() ? new DebugV3() : new DebugV2();
        } else {
            Setting.DEBUG_TO_CONSOLE.resetSetting();
            Setting.DEBUG_TO_FILE.resetSetting();
            return new DebugV3();
        }
    }

    public void reload() {
        if (loadDebugLevel(Setting.DEBUG_TO_CONSOLE, debugToConsole)) {
            Bukkit.getLogger().log(Level.INFO, PREFIX.replace("%level%", "") + "Console Debugging enabled!");
            Bukkit.getLogger().log(Level.INFO, PREFIX.replace("%level%", "") + "Enabled debuggers: " + debugToConsole.toString());
        }
        if (loadDebugLevel(Setting.DEBUG_TO_FILE, debugToFile)) {
            Bukkit.getLogger().log(Level.INFO, PREFIX.replace("%level%", "") + "File Debugging enabled!");
            Bukkit.getLogger().log(Level.INFO, PREFIX.replace("%level%", "") + "Enabled debuggers: " + debugToFile.toString());
        }
    }

    /**
     * @return true if debugging is enabled
     * @implNote This method is called by {@link #reload()} to load the debug level. It should add enums from DebugLevels to the {@link HashSet} specified by debugDestination to enable debugging for each item.
     * @var debugDestination The {@link HashSet} to add the debug levels to
     * @var settingToReadFrom The {@link Setting} to read the debug level from
     */
    public abstract boolean loadDebugLevel(Setting settingToReadFrom, HashSet<DebugLevels> debugDestination);

    public String getFormattedPrefix(DebugLevels debugLevel) {
        return PREFIX.replace("%level%", debugLevel.getPrefix());
    }

    public void log(String message, DebugLevels level) {
        log(message, level, null);
    }

    public void log(String message, DebugLevels level, String positionalNote) {
        StringBuilder messageBuilder = new StringBuilder();

        if (debugToConsole.contains(level)) {
            message = PREFIX.replace("%level%", level.getPrefix()) + message;
        } else if (level == DebugLevels.DISABLED) {
            message = PREFIX.replaceAll("( Debug.%level%)", "") + message;
        } else if (level.getPosition() < 0) {
            message = PREFIX.replace("%level%", level.getPrefix()) + message;
        }

        if (positionalNote != null && !positionalNote.isEmpty()) {
            messageBuilder.append("{ ").append(level.getPrefix()).append(" }\n");
        }

        messageBuilder.append(message);

        if (debugToConsole.contains(level)) logToConsole(level.getLogLevel(), messageBuilder.toString());
        if (debugToFile.contains(level)) logToFile(level.getLogLevel(), messageBuilder.toString());
    }

    private void logToConsole(Level level, String message) {
        Bukkit.getLogger().log(level, message);
    }

    private void logToFile(Level level, String message) {
        Bukkit.getLogger().log(level, "\n----- LOGTOFILE -----" + message + "\n----- END LOGTOFILE -----\n"); //TODO: Add file logging
    }

}
