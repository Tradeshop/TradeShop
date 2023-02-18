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

package org.shanerx.tradeshop.commands.commandrunners;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.commands.CommandPass;
import org.shanerx.tradeshop.data.config.Message;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shop.ShopChest;
import org.shanerx.tradeshop.shop.ShopType;
import org.shanerx.tradeshop.shoplocation.ShopLocation;
import org.shanerx.tradeshop.utils.Utils;

public class CommandRunner extends Utils {

	protected final TradeShop plugin;
	protected final CommandPass command;
	protected Player pSender;

	public CommandRunner(TradeShop instance, CommandPass command) {
		this.plugin = instance;
		this.command = command;

		if (command.getSender() instanceof Player) {
			pSender = (Player) command.getSender();
		}
	}


	//region Util Methods
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Returns the Shop the player is looking at
	 *
	 * @return null if Shop is not found, Shop object if it is
	 */
	protected Shop findShop() {
		if (pSender == null) {
			return null;
		}

		Block b = pSender.getTargetBlockExact(Setting.MAX_EDIT_DISTANCE.getInt());
		try {
			if (b == null)
				throw new NoSuchFieldException();

			if (ShopType.isShop(b)) {
				return Shop.loadShop((Sign) b.getState());

			} else if (ShopChest.isShopChest(b)) {
				if (plugin.getDataStorage().getChestLinkage(new ShopLocation(b.getLocation())) != null)
					return plugin.getDataStorage().loadShopFromStorage(new ShopLocation(b.getLocation()));

				return Shop.loadShop(new ShopChest(b.getLocation()).getShopSign());

			} else
				throw new NoSuchFieldException();

		} catch (NoSuchFieldException ex) {
			Message.NO_SIGHTED_SHOP.sendMessage(pSender);
			return null;
		}
	}

	//------------------------------------------------------------------------------------------------------------------
	//endregion
}