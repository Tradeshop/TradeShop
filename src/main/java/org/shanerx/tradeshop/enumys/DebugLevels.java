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

package org.shanerx.tradeshop.enumys;

import java.util.logging.Level;

public enum DebugLevels {

    DISABLED(0, Level.INFO),
    LIST_MANAGER(1, Level.WARNING),
    STARTUP(2, Level.INFO),
    PROTECTION(3, Level.WARNING);

    //position is what value to check for this level in the binary string -1.
    //
    int position;
    Level logLevel;

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
        return values().length - 1 > 32 ? 32 : values().length - 1;
    }

}