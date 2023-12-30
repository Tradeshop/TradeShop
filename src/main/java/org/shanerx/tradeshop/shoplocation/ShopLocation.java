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

package org.shanerx.tradeshop.shoplocation;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.shanerx.tradeshop.utils.objects.ObjectHolder;

import java.util.HashMap;
import java.util.Map;

public class ShopLocation {

    final private String div = "::";
    private final String worldName;
    private final int x, y, z;
    private transient World world;

    public ShopLocation(World w, double x, double y, double z) {
        this.world = w;
        this.worldName = w.getName();
        this.x = (int) x;
        this.y = (int) y;
        this.z = (int) z;
    }

    public ShopLocation(World w, int x, int y, int z) {
        this.world = w;
        this.worldName = w.getName();
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public ShopLocation(Location loc) {
        this.world = loc.getWorld();
        this.worldName = loc.getWorld().getName();
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
    }

    public static ShopLocation deserialize(String loc) {
        if (loc.startsWith("l")) {
            String[] locA = loc.contains("::") ? loc.split("::") : loc.split("_"); //Keep same as div
            ObjectHolder<?> x = new ObjectHolder<>(locA[2]), y = new ObjectHolder<>(locA[3]), z = new ObjectHolder<>(locA[4]);

            World world = Bukkit.getWorld(locA[1]);
            if (world == null)
                world = Bukkit.getWorld(locA[1].replace("-", "_"));
            if (world == null) {
                throw new IllegalWorldException("Cannot find world " + locA[1], new WorldlessLocation(x.asDouble(), y.asDouble(), z.asDouble()));
                // Not to maintainer: do NOT remove this artificial error, it is supposed to be caught elsewhere
                // (Temporary fix for metadata world renaming bug until metadata is removed entirely)
            }

            return new ShopLocation(world, x.asInteger(), y.asInteger(), z.asInteger());
        }

        return null;
    }

    public static ShopLocation deserialize(Map<String, Object> loc) {
        if (loc.containsKey("world") && loc.containsKey("x") && loc.containsKey("y") && loc.containsKey("z")) {
            World world = Bukkit.getWorld(loc.get("world").toString());
            if (world == null)
                world = Bukkit.getWorld(loc.get("world").toString().replace("-", "_"));
            if (world == null) {
                throw new IllegalWorldException("Cannot find world " + loc.get("world"), new WorldlessLocation(Double.parseDouble(loc.get("x").toString()), Double.parseDouble(loc.get("y").toString()), Double.parseDouble(loc.get("z").toString())));
                // Not to maintainer: do NOT remove this artificial error, it is supposed to be caught elsewhere
                // (Temporary fix for metadata world renaming bug until metadata is removed entirely)
            }

            return new ShopLocation(world, Double.parseDouble(loc.get("x").toString()), Double.parseDouble(loc.get("y").toString()), Double.parseDouble(loc.get("z").toString()));
        }

        return null;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("world", worldName);
        map.put("x", x);
        map.put("y", y);
        map.put("z", z);
        map.put("div", div);

        return map;
    }

    public World getWorld() {
        return world;
    }

    public String getWorldName() {
        return worldName;
    }

    public void stringToWorld() {
        if (worldName != null && world == null) {
            world = Bukkit.getWorld(worldName);
        }
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public Chunk getChunk() {
        return getLocation().getChunk();
    }

    public Location getLocation() {
        return new Location(world, x, y, z);
    }

    @Override
    public String toString() {
        return toString(true);
    }

    public String toString(boolean includeWorld) {
        return "l" + div + (includeWorld ? worldName + div : "") + x + div + y + div + z;
    }
}