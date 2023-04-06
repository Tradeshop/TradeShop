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

import java.util.logging.Level;

public enum DebugLevels {

    DEV_EXPIRATION(-2, Level.SEVERE),
    DATA_ERROR(-1, Level.SEVERE),
    DISABLED(0, Level.INFO), // 0
    STARTUP(1, Level.INFO), // 1
    ILLEGAL_ITEMS_LIST(2, Level.INFO), // 2
    PROTECTION(3, Level.WARNING), // 4
    TRADE(4, Level.WARNING), // 8
    LIST_MANAGER(5, Level.WARNING), // 16
    ITEM_COMPARE(6, Level.WARNING), // 32
    NAME_COMPARE(7, Level.WARNING), // 64
    SHULKERS_SUCK(8, Level.WARNING), // 128
    ENCHANT_CHECKS(9, Level.WARNING), // 256
    OUTPUT(10, Level.WARNING), // 512
    SHOP_CREATION(11, Level.INFO), // 1024
    SQLITE(12, Level.INFO), // 2048
    GSON(13, Level.INFO) // 4096


    ;

    //position is what value to check for this level in the binary string -1.
    //
    int position;
    Level logLevel;
    private static int max = 0;

    DebugLevels(int position, Level logLevel) {
        this.position = position;
        this.logLevel = logLevel;
    }

    public int getPosition() {
        return position;
    }

    public Level getLogLevel() {
        return logLevel;
    }

    public static int levels() {
        return Math.min(values().length - 1, 32);
    }

    public static int maxValue() {
        if (max <= 1) {
            for (DebugLevels lvl : values()) {
                max += Math.pow(2, lvl.position - 1);
            }
        }

        return max;
    }

    public String getPrefix() {
        return " - " + name();
    }

}