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
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Updater {

	private static Logger log;
	private PluginDescriptionFile pdf;
	private BuildType build;
	private URL url = null;

	public Updater(PluginDescriptionFile pdf) {
		this.pdf = pdf;

		log = Bukkit.getPluginManager().getPlugin("TradeShop").getLogger();

		try {
			url = new URL("https://api.spigotmc.org/legacy/update.php?resource=32762"); // Edit API URL.
		} catch (MalformedURLException ex) {
			log.log(Level.WARNING, "Error: Bad URL while checking {0} !", pdf.getName());
		}
	}

	public static void setLogger(Logger log) {
		Updater.log = log;
	}

	public String getVersion() {
		return pdf.getVersion();
	}

	public String getNakedVersion() {
		return getVersion();
	}

	public String getVersionComponent(SemVer semver) {
		String[] ver = getNakedVersion().split("\\.");
		switch (semver) {
			case MAJOR:
				return ver[0];
			case MINOR:
				return ver[1];
			case PATCH:
				return ver[2].split("-")[0];
			default:
				return ver[2].split("-").length > 1 ? ver[2].split("-")[0] : BuildType.STABLE.toString();
		}
	}

	public BuildType getBuildType() {
		return build;
	}

	public RelationalStatus checkCurrentVersion() {
		try {

			BufferedReader in = new BufferedReader(
					new InputStreamReader(url.openStream()));

			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				String[] ver = inputLine.split("\\.");
				RelationalStatus rs = compareVersions(ver[0], ver[1], ver[2].split("-")[0]);
				if (rs == RelationalStatus.BEHIND) {
					log.log(Level.WARNING, "[Updater] +------------------------------------------------+");
					log.log(Level.WARNING, "[Updater] You are running an outdated version of the plugin!");
					log.log(Level.WARNING, "[Updater] Most recent stable version: " + inputLine);
					log.log(Level.WARNING, "[Updater] Please update asap from: https://goo.gl/Qv2iVZ");
					log.log(Level.WARNING, "[Updater] +------------------------------------------------+");
					in.close();
					return RelationalStatus.BEHIND;
				} else if (rs == RelationalStatus.AHEAD) {
					log.log(Level.WARNING, "[Updater] +-----------------------------------------------------+");
					log.log(Level.WARNING, "[Updater] You are running a developmental version of the plugin!");
					log.log(Level.WARNING, "[Updater] Most recent stable version: " + inputLine);
					log.log(Level.WARNING, "[Updater] Please notice that the build may contain critical bugs!");
					log.log(Level.WARNING, "[Updater] +-----------------------------------------------------+");
					in.close();
					return RelationalStatus.AHEAD;
				} else {
					log.log(Level.WARNING, "[Updater] You are running the latest version of the plugin!");
					in.close();
					return RelationalStatus.UP_TO_DATE;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.log(Level.WARNING, "[Updater] +----------------------------------------------------+");
			log.log(Level.WARNING, "[Updater] Could not establish a connection to check for updates!");
			log.log(Level.WARNING, "[Updater] +----------------------------------------------------+");
		}
		return RelationalStatus.UNKNOWN;
	}

	public RelationalStatus compareVersions(final String major, final String minor, final String patch) {
		try {
			return compareVersions(Short.parseShort(major), Short.parseShort(minor), Short.parseShort(patch));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("All arguments must be numbers!");
		}
	}

	public RelationalStatus compareVersions(final short major, final short minor, final short patch) {
		try {
			short selfMajor = Short.parseShort(getVersionComponent(SemVer.MAJOR));
			short selfMinor = Short.parseShort(getVersionComponent(SemVer.MINOR));
			short selfPatch = Short.parseShort(getVersionComponent(SemVer.PATCH));

			if (selfMajor == major && selfMinor == minor && selfPatch == patch) {
				return RelationalStatus.UP_TO_DATE;
			} else if (selfMajor > major) {
				return RelationalStatus.AHEAD;
			} else if (selfMajor < major) {
				return RelationalStatus.BEHIND;
			} else if (selfMinor > minor) {
				return RelationalStatus.AHEAD;
			} else if (selfMinor < minor) {
				return RelationalStatus.BEHIND;
			} else if (selfPatch > patch) {
				return RelationalStatus.AHEAD;
			} else {
				return RelationalStatus.BEHIND;
			}

		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("All arguments must be numbers!");
		}
	}

	public enum BuildType {

		DEV,

		BETA,

		STABLE,

		FINAL
	}

	public enum SemVer {

		MAJOR,

		MINOR,

		PATCH,

		BUILD_TYPE
	}

	public enum RelationalStatus {

		AHEAD,

		UP_TO_DATE,

		BEHIND,

		UNKNOWN
	}
}