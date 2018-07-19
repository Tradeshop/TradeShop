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

package org.shanerx.tradeshop.objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.Serializable;

public class ShopLocation implements Serializable {

	private World world;
	private double x, y, z;
	private String div = "_";

	public ShopLocation(World w, double x, double y, double z) {
		this.world = w;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public ShopLocation(Location loc) {
		this.world = loc.getWorld();
		this.x = loc.getX();
		this.y = loc.getY();
		this.z = loc.getZ();
	}

	public String serialize() {
		return "l" + div + world.getName() + div + x + div + y + div + z;
	}

	public Location deserialize(String loc) {
		if (loc.startsWith("l")) {
			String locA[] = loc.split(div);
			World world = Bukkit.getWorld(locA[1]);
			double x = Integer.parseInt(locA[2]), y = Integer.parseInt(locA[3]), z = Integer.parseInt(locA[4]);

			return new Location(world, x, y, z);
		}

		return null;
	}

	public World getWorld() {
		return world;
	}

	public String getWorldName() {
		return world.getName();
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

	public Location getLocation() {
		return new Location(world, x, y, z);
	}
}