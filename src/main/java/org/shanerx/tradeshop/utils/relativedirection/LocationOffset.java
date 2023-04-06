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

package org.shanerx.tradeshop.utils.relativedirection;

public class LocationOffset {

    private final int xOffset, yOffset, zOffset;
    private final transient int max = 1, min = -1;

    public LocationOffset(int xOffset, int yOffset, int zOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
    }

    public LocationOffset add(LocationOffset toAdd) {
        return new LocationOffset(getXOffset() + toAdd.getXOffset(), getYOffset() + toAdd.getYOffset(), getZOffset() + toAdd.getZOffset()).normalize();
    }

    public LocationOffset normalize() {
        return new LocationOffset(bringWithinRange(getXOffset()), bringWithinRange(getYOffset()), bringWithinRange(getZOffset()));
    }

    private int bringWithinRange(int i) {
        return Math.max(Math.min(i, max), min);
    }

    public int getZOffset() {
        return zOffset;
    }

    public int getYOffset() {
        return yOffset;
    }

    public int getXOffset() {
        return xOffset;
    }
}
