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

package org.shanerx.tradeshop.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.enumys.Commands;
import org.shanerx.tradeshop.enumys.Setting;
import org.shanerx.tradeshop.enumys.ShopType;
import org.shanerx.tradeshop.objects.Shop;
import org.shanerx.tradeshop.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandTabCompleter extends Utils {

	private TradeShop plugin;
	private CommandPass command;
	private Player pSender;

	public CommandTabCompleter(TradeShop instance, CommandPass command) {
		this.plugin = instance;
		this.command = command;

		if (command.getSender() instanceof Player) {
			pSender = (Player) command.getSender();
		}
	}

	public List<String> help() {
		if (command.argsSize() == 2) {
			List<String> subCmds = Arrays.asList(new String[Commands.values().length]);
			for (int i = 0; i < Commands.values().length; i++) {
				subCmds.set(i, Commands.values()[i].getFirstName());
			}

			return subCmds;
		}

		return Collections.EMPTY_LIST;
	}

	public List<String> bugs() {
		return Collections.EMPTY_LIST;
	}

	public List<String> setup() {
		return Collections.EMPTY_LIST;
	}

	public List<String> reload() {
		return Collections.EMPTY_LIST;
	}

	public List<String> addProduct() {
		if (command.argsSize() == 2) {
			return Arrays.asList("1", "2", "4", "8", "16", "32", "64", "96", "128");
		} else if (command.argsSize() == 3) {
			List<String> materials = new ArrayList<>();

			for (Material mat : Material.values())
				materials.add(mat.toString());

			return materials;
		}
		return Collections.EMPTY_LIST;
	}

	public List<String> addCost() {
		if (command.argsSize() == 2) {
			return Arrays.asList("1", "2", "4", "8", "16", "32", "64", "96", "128");
		} else if (command.argsSize() == 3) {
			List<String> materials = new ArrayList<>();

			for (Material mat : Material.values())
				materials.add(mat.toString());

			return materials;
		}
		return Collections.EMPTY_LIST;
	}

	public List<String> open() {
		return Collections.EMPTY_LIST;
	}

	public List<String> close() {
		return Collections.EMPTY_LIST;
	}

	public List<String> switchShop() {
		return Collections.EMPTY_LIST;
	}

	public List<String> what() {
		return Collections.EMPTY_LIST;
	}

	public List<String> who() {
		return Collections.EMPTY_LIST;
	}

	public List<String> addManager() {
		if (command.argsSize() == 1) {
			List<String> names = new ArrayList<>();

			for (Player p : Bukkit.getOnlinePlayers()) {
				names.add(p.getName());
			}

			return names;
		}

		return Collections.EMPTY_LIST;
	}

	public List<String> removeManager() {
		if (command.argsSize() == 1) {
			Block b = pSender.getTargetBlock(null, Setting.MAX_EDIT_DISTANCE.getInt());
			Sign s;

			if (plugin.getListManager().isInventory(b.getType())) {
				s = findShopSign(b);
			} else if (ShopType.isShop(b)) {
				s = (Sign) b.getState();
			} else {
				return Collections.EMPTY_LIST;
			}
			Shop shop = Shop.loadShop(s);

			return shop.getManagersNames();
		}

		return Collections.EMPTY_LIST;
	}

	public List<String> addMember() {
		if (command.argsSize() == 1) {
			List<String> names = new ArrayList<>();

			for (Player p : Bukkit.getOnlinePlayers()) {
				names.add(p.getName());
			}

			return names;
		}

		return Collections.EMPTY_LIST;
	}

	public List<String> removeMember() {
		if (command.argsSize() == 1) {
			Block b = pSender.getTargetBlock(null, Setting.MAX_EDIT_DISTANCE.getInt());
			Sign s;

			if (plugin.getListManager().isInventory(b.getType())) {
				s = findShopSign(b);
			} else if (ShopType.isShop(b)) {
				s = (Sign) b.getState();
			} else {
				return Collections.EMPTY_LIST;
			}
			Shop shop = Shop.loadShop(s);

			return shop.getMembersNames();
		}

		return Collections.EMPTY_LIST;
	}
}
