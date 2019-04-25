/*
 *                 Copyright (c) 2016-2017
 *         SparklingComet @ http://shanerx.org
 *      KillerOfPie @ http://killerofpie.github.io
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
 * caused by their contribution(s) to the project. See the full License for more information.
 */

package org.shanerx.tradeshop.framework;

import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.enumys.Commands;

import java.util.HashMap;
import java.util.Map;

public class CustomCommandHandler {

	private static CustomCommandHandler instance;

	public static void init(TradeShop plugin) {
		instance = new CustomCommandHandler(plugin);
	}

	public static CustomCommandHandler getInstance() {
		return instance;
	}

	/* *** */

	private TradeShop plugin;
	private Map<String, TradeCommand> addonCmds;

	private CustomCommandHandler(TradeShop plugin) {
		if (instance != null) {
			throw new IllegalStateException("Access forbidden: attempting to create further instances of simpleton class!");
		}

		this.plugin = plugin;
		this.addonCmds = new HashMap<>();
	}

	public boolean isNativeCommand(String subCmd) {
			Commands nativeCmd = Commands.getType(subCmd);
			return nativeCmd != null;
	}

	public boolean isAvailable(String subCmd) {
		return !(isNativeCommand(subCmd) || addonCmds.containsKey(subCmd.toLowerCase()));
	}

	public void registerCommand(TradeCommand subCmd) {
		if (!isAvailable(subCmd.getCmd())) {
			throw new IllegalArgumentException("Another addon has already register a subcommand with this name already exists!");
		}

		addonCmds.put(subCmd.getCmd().toLowerCase(), subCmd);
	}

	public TradeCommand getExecutable(String subCmd) {
		return addonCmds.get(subCmd.toLowerCase());
	}
}
