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

package org.shanerx.tradeshop.commands;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.player.ShopRole;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shop.ShopType;
import org.shanerx.tradeshop.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandTabCompleter {

    private final TradeShop plugin;
    private final CommandSender sender;
    private final String[] args;
    private Player pSender;


    public CommandTabCompleter(TradeShop instance, CommandSender sender, String[] args) {
        this.plugin = instance;
        this.sender = sender;
        this.args = args;

        if (sender instanceof Player) {
            pSender = (Player) sender;
        }
    }

    public List<String> help() {
        if (args.length == 2) {
            List<String> subCmds = new ArrayList<>();
            for (CommandType cmds : CommandType.values()) {
                if (cmds.isPartialName(SubCommand.getArgAt(args, 1)))
                    subCmds.add(cmds.getFirstName());
            }

            return subCmds;
        }

        return Collections.EMPTY_LIST;
    }

    public List<String> addSet() {
        if (args.length == 2) {
            return Arrays.asList("1", "2", "4", "8", "16", "32", "64", "80", "96", "128");
        } else if (args.length == 3) {
            return partialGameMats(SubCommand.getArgAt(args, 2));
        }
        return Collections.EMPTY_LIST;
    }

    public List<String> fillServerPlayer() {
        if (args.length == 2) {
            return null;
        }

        return Collections.EMPTY_LIST;
    }

    public List<String> fillShopPlayer() {
        if (args.length != 2) return Collections.EMPTY_LIST;

        Block b = pSender.getTargetBlock(null, Setting.MAX_EDIT_DISTANCE.getInt());
        Sign s;

        if (plugin.getListManager().isInventory(b)) {
            s = new Utils().findShopSign(b);
        } else if (ShopType.isShop(b)) {
            s = (Sign) b.getState();
        } else {
            return Collections.EMPTY_LIST;
        }
        Shop shop = Shop.loadShop(s);

        return shop.getUserNames(ShopRole.MANAGER, ShopRole.MEMBER);
    }

    private List<String> partialGameMats(String request) {
        List<String> toReturn = new ArrayList<>();
        for (String str : plugin.getListManager().getGameMats()) {
            if (str.toLowerCase().contains(request.toLowerCase())) {
                toReturn.add(str);
            }
        }

        return toReturn;
    }
}
