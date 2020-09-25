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

import org.bukkit.Material;
import org.shanerx.tradeshop.utils.BukkitVersion;
import org.shanerx.tradeshop.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

enum Signs {
    SIGN("", "1.13.2"),
    OAK_SIGN("1.14.0", ""),
    SPRUCE_SIGN("1.14.0", ""),
    BIRCH_SIGN("1.14.0", ""),
    JUNGLE_SIGN("1.14.0", ""),
    ACACIA_SIGN("1.14.0", ""),
    DARK_OAK_SIGN("1.14.0", ""),
    CRIMSON_SIGN("1.16.0", ""),
    WARPED_SIGN("1.16.0", ""),
    WALL_SIGN("", "1.13.2"),
    OAK_WALL_SIGN("1.14.0", ""),
    SPRUCE_WALL_SIGN("1.14.0", ""),
    BIRCH_WALL_SIGN("1.14.0", ""),
    JUNGLE_WALL_SIGN("1.14.0", ""),
    ACACIA_WALL_SIGN("1.14.0", ""),
    DARK_OAK_WALL_SIGN("1.14.0", ""),
    CRIMSON_WALL_SIGN("1.16.0", ""),
    WARPED_WALL_SIGN("1.16.0", "");

    private List<Integer> minVer = Arrays.asList(new Integer[3]), maxVer = Arrays.asList(new Integer[3]);
    private boolean hasMin = true, hasMax = true;

    Signs(String minVersion, String maxVersion) {
        if (minVersion.equalsIgnoreCase(""))
            hasMin = false;

        if (maxVersion.equalsIgnoreCase(""))
            hasMax = false;

        if (hasMin) {
            String[] minVerArray = minVersion.split("[.]");
            for (int i = 0; i < minVerArray.length; i++) {
                minVer.set(i, Integer.parseInt(minVerArray[i]));
            }
        }

        if (hasMax) {
            String[] maxVerArray = maxVersion.split("[.]");
            for (int i = 0; i < maxVerArray.length; i++) {
                maxVer.set(i, Integer.parseInt(maxVerArray[i]));
            }
        }
    }

    public boolean hasMinVersion() {
        return hasMin;
    }

    public boolean hasMaxVersion() {
        return hasMax;
    }

    public List<Integer> getMinVer() {
        return minVer;
    }

    public List<Integer> getMaxVer() {
        return maxVer;
    }

    public String getMinVersionAsString() {
        return hasMinVersion() ? getMinVer().get(0) + "." + getMinVer().get(1) + "." + getMinVer().get(2) : "None";
    }

    public String getMaxVersionAsString() {
        return hasMaxVersion() ? getMaxVer().get(0) + "." + getMaxVer().get(1) + "." + getMaxVer().get(2) : "None";
    }

}

public class ShopSign extends Utils {

    private BukkitVersion version = new BukkitVersion();
    private ArrayList<Material> signTypes = new ArrayList<>();

    public ShopSign() {
        for (Signs type : Signs.values()) {
            boolean pass = true;
            debugger.log(type.toString(), DebugLevels.STARTUP);
            debugger.log(String.format("MinVer: %s", type.getMinVersionAsString()), DebugLevels.STARTUP);
            debugger.log(String.format("MaxVer: %s", type.getMaxVersionAsString()), DebugLevels.STARTUP);

            if (type.hasMinVersion() && version.isBelow(type.getMinVer().get(0), type.getMinVer().get(1), type.getMinVer().get(2))) {
                pass = false;
            } else if (type.hasMaxVersion() && version.isAbove(type.getMaxVer().get(0), type.getMaxVer().get(1), type.getMaxVer().get(2))) {
                pass = false;
            }

            if (pass)
                signTypes.add(Material.matchMaterial(type.toString()));
        }


    }

    public List<Material> getSignTypes() {
        return signTypes;
    }


}