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

import org.shanerx.tradeshop.data.config.Setting;

import java.util.HashSet;

public class DebugV2 extends Debug {

    public DebugV2() {
        super(2);
    }

    @Override
    public boolean loadDebugLevel(Setting settingToReadFrom, HashSet<DebugLevels> debugDestination) {
        int decimalDebugLevel = settingToReadFrom.getInt();
        if (decimalDebugLevel < 0) {
            decimalDebugLevel = DebugLevels.maxValue();
        }
        StringBuilder sb = new StringBuilder(Integer.toBinaryString(decimalDebugLevel));
        while (sb.length() < DebugLevels.levels())
            sb.insert(0, 0);

        String binaryDebugLevel = sb.reverse().toString();

        for (DebugLevels level : DebugLevels.values()) {
            if (level.getPosition() - 1 < binaryDebugLevel.length() && binaryDebugLevel.charAt(level.getPosition()) == '1') {
                debugDestination.add(level);
            }
        }

        return !debugDestination.isEmpty();
    }
}