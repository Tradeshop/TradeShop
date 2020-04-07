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

package org.shanerx.tradeshop.objects;

import org.bukkit.Bukkit;
import org.shanerx.tradeshop.enumys.DebugLevels;
import org.shanerx.tradeshop.enumys.Setting;

public class Debug {

    protected final String PREFIX = "[TradeShop Debug] ";
    private int decimalDebugLevel;
    private String binaryDebugLevel;

    public Debug() {
        reload();
    }

    public void reload() {
        decimalDebugLevel = Setting.ENABLE_DEBUG.getInt();
        binaryDebugLevel = new StringBuilder(Integer.toBinaryString(decimalDebugLevel)).reverse().toString();
    }

    public void log(String message, DebugLevels level) {
        if (level.getPosition() - 1 != -1 && binaryDebugLevel.charAt(level.getPosition() - 1) == '1') {
            Bukkit.getLogger().log(level.getLogLevel(), PREFIX + message);
        }
    }
}