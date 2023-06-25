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

package org.shanerx.tradeshop.commands.commandrunners;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.commands.CommandPass;
import org.shanerx.tradeshop.data.config.Message;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.item.ShopItemSide;
import org.shanerx.tradeshop.player.ShopUser;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.utils.debug.DebugLevels;
import org.shanerx.tradeshop.utils.objects.ObjectHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of CommandPass for the `find` command
 *
 * @since 2.6.1
 */
public class ShopFindSubCommand extends SubCommand {

    public ShopFindSubCommand(TradeShop plugin, CommandPass cmdPass) {
        super(plugin, cmdPass);
    }

    public void find() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Location searchFrom = command.getPlayerSender().getLocation();
                Map<Integer, List<ItemStack>> desiredCost = new HashMap<>(), desiredProduct = new HashMap<>();
                int desiredRange = Setting.MAX_FIND_RANGE.getInt();
                boolean inStock = false;

                if (desiredRange > 0) {

                    ArrayList<String> lowerArgs = (command.getArgs().stream().map(String::toLowerCase).collect(Collectors.toCollection(ArrayList::new)));
                    lowerArgs.remove("find");

                    List<Shop> shops = new ArrayList<>();

                    for (String arg : lowerArgs) {
                        for (String str : arg.split(";")) {
                            String[] keyVal = str.contains("=") ? str.split("=") : str.split(":");
                            plugin.getDebugger().log(" --- _S_F_ --- " + keyVal[0] + " = " + (keyVal.length > 1 ? keyVal[1] : "---No Value---"), DebugLevels.DATA_ERROR);
                            switch (keyVal[0]) {
                                case "c":
                                case "cost":
                                    desiredCost.putAll(processItemDesires(keyVal[1]));
                                    break;
                                case "p":
                                case "product":
                                    desiredProduct.putAll(processItemDesires(keyVal[1]));
                                    break;
                                case "s":
                                case "in-stock":
                                case "stock":
                                    ObjectHolder<?> stock = new ObjectHolder<>(keyVal[1]);
                                    inStock = stock.isBoolean() ? stock.asBoolean() : false;
                                    break;
                                case "d":
                                case "r":
                                case "distance":
                                case "range":
                                    ObjectHolder<?> dist = new ObjectHolder<>(keyVal[1]);
                                    desiredRange = (dist.canBeInteger() && dist.asInteger() < desiredRange) ?
                                            dist.asInteger() :
                                            Setting.DEFAULT_FIND_RANGE.getInt();
                            }
                        }
                    }

                    plugin.getDebugger().log(" --- _F_F_ --- \n" +
                            "Cost" + " = " + desiredCost + "\n" +
                            "Product" + " = " + desiredProduct + "\n" +
                            "Range" + " = " + desiredRange, DebugLevels.DATA_ERROR);

                    int finalDesiredRange = desiredRange > 0 ? desiredRange : Setting.DEFAULT_FIND_RANGE.getInt();
                    final boolean finalInStock = inStock;
                    desiredProduct.forEach((prodId, prodItems) -> {
                        desiredCost.forEach((costId, costItems) -> {
                            shops.addAll(ShopUser.findProximityShop(searchFrom, finalDesiredRange, finalInStock, costItems, prodItems));
                        });
                    });

                    ArrayList<TextComponent> foundShops = new ArrayList<>();

                    shops.forEach((shop -> {
                        TextComponent message = new TextComponent(shop.getShopLocationAsSL().toString() + "\n");

                        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM,
                                new ComponentBuilder(
                                        String.join("\n",
                                                Arrays.asList(
                                                        shop.getShopType().toString(),
                                                        shop.getStatus().getLine(),
                                                        "Cost: " + shop.getSideListNames(ShopItemSide.COST).toString(),
                                                        "Product: " + shop.getSideListNames(ShopItemSide.PRODUCT)))).create()));

                        foundShops.add(message);
                    }));


                    plugin.getDebugger().log(" --- _F_D_ --- " + Arrays.toString(foundShops.toArray(new BaseComponent[]{})), DebugLevels.DATA_ERROR);

                    if (foundShops.size() > 0)
                        command.getSender().spigot().sendMessage(foundShops.toArray(new BaseComponent[]{}));
                    else
                        Message.NO_SHOP_FOUND.sendMessage(command.getSender());
                } else {
                    Message.FEATURE_DISABLED.sendMessage(command.getSender());
                }
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
        });
    }
}
