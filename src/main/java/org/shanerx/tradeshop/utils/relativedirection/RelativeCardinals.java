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

package org.shanerx.tradeshop.utils.relativedirection;

import org.bukkit.block.BlockFace;

public enum RelativeCardinals {

    SELF("0", "0", "0"),
    DOWN("0", "-1", "0"),
    UP("0", "1", "0"),
    RIGHT("z", "0", "-x"),
    LEFT("-z", "0", "x"),
    BACK("-x", "0", "-z"),
    FRONT("x", "0", "z");

    private final transient String xTranslation;
    private final transient String yTranslation;
    private final transient String zTranslation;

    RelativeCardinals(String xTranslation, String yTranslation, String zTranslation) {
        this.xTranslation = xTranslation;
        this.yTranslation = yTranslation;
        this.zTranslation = zTranslation;
    }

    public LocationOffset getTranslation(BlockFace facing) {
        return new LocationOffset(processTranslation(xTranslation, facing), processTranslation(yTranslation, facing), processTranslation(zTranslation, facing));
    }

    private int processTranslation(String translation, BlockFace facing) {
        int temp = 0;

        try {
            temp = Integer.parseInt(translation);
            return temp;
        } catch (NumberFormatException ignored) {
        }

        if (translation.toLowerCase().contains("x"))
            temp = facing.getModX();
        else if (translation.toLowerCase().contains("y"))
            temp = facing.getModY();
        else if (translation.toLowerCase().contains("z"))
            temp = facing.getModZ();

        if (translation.contains("-"))
            return Math.negateExact(temp);

        return temp;
    }
}
