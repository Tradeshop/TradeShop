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

package org.shanerx.tradeshop.utils.versionmanagement;

import org.shanerx.tradeshop.utils.objects.ObjectHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version {
	private final String VERSION;
	private final Map<String, ObjectHolder<Object>> verMap;

	public Version(String version) {
		VERSION = version;
		verMap = getVerMap();
	}

	public String toString() {
		return getMajor() + "." + getMinor() + "." + getPatch();
	}

	public String getFullVersion() {
		return VERSION;
	}

	public int getMajor() {
		return verMap.get("major").asInteger();
	}

	public int getMinor() {
		return verMap.get("minor").asInteger();
	}

	public int getPatch() {
		return verMap.get("patch").asInteger();
	}

	public boolean isAtLeast(int... minVersion) {
		return compare(minVersion, ">=");
	}

	public boolean isAtMost(int... minVersion) {
		return compare(minVersion, "<=");
	}

	public boolean isBelow(int... maxVersion) {
		return compare(maxVersion, "<");
	}

	public boolean isAbove(int... maxVersion) {
		return compare(maxVersion, ">");
	}

	public boolean isNotEqual(int... deniedVersion) {
		return compare(deniedVersion, "<>");
	}

	public boolean isEqual(int... expectedVersion) {
		return compare(expectedVersion, "==");
	}

	public boolean compare(int[] compVersion, String mathComp) {
		int[] temp = new int[3];
		for (int i = 0; i < temp.length; i++) {
			if (compVersion.length > i) temp[i] = compVersion[i];
			else temp[i] = 0;
		}
		compVersion = temp;

		ObjectHolder<Object> simpBukkVer = verMap.get("simplified"),
				simpCompVer = new ObjectHolder<>((compVersion[0] * 100) + (compVersion[1]) + (compVersion[2] / 100.0));

		// Add ? preceding math sign to compare only Major Minor
		switch (mathComp) {
			case ">":
			case "?>":
				return (mathComp.contains("?") ? simpBukkVer.asInteger() : simpBukkVer.asDouble()) > (mathComp.contains("?") ? simpCompVer.asInteger() : simpCompVer.asDouble()); //Above
			case ">=":
			case "?>=":
				return (mathComp.contains("?") ? simpBukkVer.asInteger() : simpBukkVer.asDouble()) >= (mathComp.contains("?") ? simpCompVer.asInteger() : simpCompVer.asDouble()); //At Least
			case "<":
			case "?<":
				return (mathComp.contains("?") ? simpBukkVer.asInteger() : simpBukkVer.asDouble()) < (mathComp.contains("?") ? simpCompVer.asInteger() : simpCompVer.asDouble()); //Is Below
			case "<=":
			case "?<=":
				return (mathComp.contains("?") ? simpBukkVer.asInteger() : simpBukkVer.asDouble()) <= (mathComp.contains("?") ? simpCompVer.asInteger() : simpCompVer.asDouble()); //At Most
			case "<>":
			case "?<>":
				return (mathComp.contains("?") ? simpBukkVer.asInteger() : simpBukkVer.asDouble()) != (mathComp.contains("?") ? simpCompVer.asInteger() : simpCompVer.asDouble()); //Not Equal
			case "==":
			case "?==":
			case "?":
			default:
				return (mathComp.contains("?") ? simpBukkVer.asInteger() : simpBukkVer.asDouble()) == (mathComp.contains("?") ? simpCompVer.asInteger() : simpCompVer.asDouble()); //Equals
		}

	}

	public Map<String, ObjectHolder<Object>> getVerMap() {
		Pattern pat = Pattern.compile("(?!\\.)(\\d+(\\.\\d+)+)(?![\\d\\.])");
		Matcher matcher = pat.matcher(VERSION);

		String ver;
		if (matcher.find()) ver = matcher.group();
		else return null;

		String[] verSplit = ver.split("\\.");
		ArrayList<ObjectHolder<Object>> verInts = new ArrayList<>(3);

		for (String s : verSplit) {
			ObjectHolder<Object> iOH = new ObjectHolder<>(s);
			if (iOH.canBeInteger()) verInts.add(iOH);
		}

		while (verInts.size() < 3) verInts.add(new ObjectHolder<>(0));

		Map<String, ObjectHolder<Object>> map = new HashMap<>();
		map.put("major", verInts.get(0));
		map.put("minor", verInts.get(1));
		map.put("patch", verInts.get(2));
		map.put("simplified", new ObjectHolder<>(
				(verInts.get(0).asInteger() * 100) +
						(verInts.get(1).asInteger()) +
						(verInts.get(2).asInteger() / 100.0)));

		return map;
	}
}