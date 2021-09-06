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

package org.shanerx.tradeshop.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.enumys.Message;
import org.shanerx.tradeshop.enumys.Permissions;
import org.shanerx.tradeshop.objects.PlayerSetting;
import org.shanerx.tradeshop.utils.BukkitVersion;
import org.shanerx.tradeshop.utils.Updater;
import org.shanerx.tradeshop.utils.Utils;

public class JoinEventListener extends Utils implements Listener {

	private final TradeShop plugin;

	public JoinEventListener(TradeShop instance) {
		plugin = instance;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onJoin(PlayerJoinEvent event) {

		Player player = event.getPlayer();

		//If player has Manage permission and plugin is behind, then send update message
		if (Permissions.hasPermission(player, Permissions.MANAGE_PLUGIN)) {
			BukkitVersion ver = new BukkitVersion();
			if (plugin.getUpdater().compareVersions((short) ver.getMajor(), (short) ver.getMinor(), (short) ver.getPatch()).equals(Updater.RelationalStatus.BEHIND))
				player.sendMessage(Message.PLUGIN_BEHIND.getPrefixed());
		}

		PlayerSetting playerSetting = plugin.getDataStorage().loadPlayer(player.getUniqueId());
		if (playerSetting.showInvolvedStatus()) {
			player.sendMessage(playerSetting.getInvolvedStatusesString());
		}
	}
}
