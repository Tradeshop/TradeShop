package org.shanerx.tradeshop.objects;

import org.bukkit.Location;
import org.bukkit.World;

public class WorldlessLocation {

    private double x, y, z;

    public WorldlessLocation(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Location getLocation(World w) {
        return new Location(w, x, y, z);
    }
}
