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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.commands.CommandPass;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.player.ShopUser;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.utils.objects.ObjectHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of CommandPass for the `find` command
 *
 * @since 2.6.1
 */
public class ShopFindCommand extends CommandRunner {

    public ShopFindCommand(TradeShop plugin, CommandPass cmdPass) {
        super(plugin, cmdPass);
    }

    public void find() {
        Location searchFrom = command.getPlayerSender().getLocation();
        Map<Integer, List<ItemStack>> desiredCost = new HashMap<>(), desiredProduct = new HashMap<>();
        int desiredRange = 0;

        ArrayList<String> lowerArgs = (command.getArgs().stream().map(String::toLowerCase).collect(Collectors.toCollection(ArrayList::new)));
        lowerArgs.remove("find");
        String searchParam = lowerArgs.stream().reduce("", (s1, s2) -> s1 + s2);

        List<Shop> shops = new ArrayList<>();

        for (String str : searchParam.split(";")) {
            String[] keyVal = str.split("=");
            switch (keyVal[0]) {
                case "cost":
                    desiredCost.putAll(processItemDesires(keyVal[1]));
                    break;
                case "product":
                    desiredProduct.putAll(processItemDesires(keyVal[1]));
                    break;
                case "distance":
                case "range":
                    ObjectHolder<?> dist = new ObjectHolder<>(keyVal[1]);
                    if (dist.isInteger() && dist.asInteger() < Setting.MAX_FIND_RANGE.getInt())
                        desiredRange = dist.asInteger();
                    break;
            }
        }

        int finalDesiredRange = desiredRange > 0 ? desiredRange : Setting.DEFAULT_FIND_RANGE.getInt();
        desiredProduct.forEach((prodId, prodItems) -> {
            desiredProduct.forEach((costId, costItems) -> {
                shops.addAll(ShopUser.findProximityShop(searchFrom, finalDesiredRange, costItems, prodItems));
            });
        });


        command.getSender().sendMessage(String.join("\n", shops.stream().map(shop -> shop.getShopLocationAsSL().serialize()).toArray(String[]::new)));
    }

    private Map<Integer, List<ItemStack>> processItemDesires(String desire) {
        Map<Integer, List<ItemStack>> desires = new HashMap<>();
        int group = 0;
        for (String str : desire.split("\\|")) {
            List<ItemStack> items = new ArrayList<>();
            for (String str2 : str.split(",")) {
                Material mat = Material.matchMaterial(str2);
                if (mat != null) items.add(new ItemStack(mat));
            }
            desires.put(group, items);
            group++;
        }
        return desires;
    }
}
