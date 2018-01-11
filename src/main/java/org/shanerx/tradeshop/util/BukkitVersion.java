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

package org.shanerx.tradeshop.util;

import org.bukkit.Bukkit;

public class BukkitVersion {
    private final String SERVERVERSION = Bukkit.getBukkitVersion();

    private double major;
    private int minor;

    public BukkitVersion() {
        String[] ver = SERVERVERSION.split("-")[0].split("\\.");
        this.major = parseDouble(ver[0]) + parseMinor(ver[1]);
        this.minor = parseInt(ver[2]);
    }

    public BukkitVersion(double major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    public String toString() {
        return major + "." + minor;
    }

    public String getFullVersion() {
        return SERVERVERSION;
    }

    public double getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public boolean isBelow19() {
        return major < 1.9;
    }

    public boolean isAbove(double ver) {
        return major >= ver;
    }

    private double parseDouble(String toParse) {
        try {
            return Double.parseDouble(toParse);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int parseInt(String toParse) {
        try {
            return Integer.parseInt(toParse);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parseMinor(String toParse) {
        return parseDouble(toParse) / Math.pow(10, toParse.length());
    }
}