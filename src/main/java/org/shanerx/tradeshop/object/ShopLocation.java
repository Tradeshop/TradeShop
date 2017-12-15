/*
 *     Copyright (c) 2016-2017 SparklingComet @ http://shanerx.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * NOTICE: All modifications made by others to the source code belong
 * to the respective contributor. No contributor should be held liable for
 * any damages of any kind, whether be material or moral, which were
 * caused by their contribution(s) to the project. See the full License for more information
 */

package org.shanerx.tradeshop.object;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class ShopLocation {

    String world;
    double x, y, z;
    Location location;

    public ShopLocation(String w, double x, double y, double z) {
        this.world = w;
        this.x = x;
        this.y = y;
        this.z = z;
        location = new Location(Bukkit.getWorld(w), x, y, z);
    }

    public String serializeLocation() {
        return "l" + "_" + world + "_" + x + "_" + y + "_" + z;
    }

    public static ShopLocation deserializeLocation(String loc) {
        String locA[] = loc.split("_");
        if (locA[0].equalsIgnoreCase("l")) {
            return new ShopLocation(locA[1],
                    Integer.parseInt(locA[2]),
                    Integer.parseInt(locA[3]),
                    Integer.parseInt(locA[4]));
        }

        return null;
    }

}
