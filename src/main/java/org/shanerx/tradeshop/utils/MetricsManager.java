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

package org.shanerx.tradeshop.utils;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedBarChart;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimpleBarChart;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.World;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.enumys.Setting;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class MetricsManager {
    private final int bStatsPluginID = 1690;
    private final TradeShop plugin;
    private final Metrics metrics;
    private int tradeCounter = 0;

    public MetricsManager(TradeShop plugin) {
        this.plugin = plugin;
        metrics = new Metrics(plugin, bStatsPluginID);

        addTradeMetric();
        addShopMetric();
        addOtherSettingMetrics();

        // Add to bstats when Bar Charts are re-added
        addFeatureMetric();
        addIllegalItemsBarMetric();
        addSettingStringListMetrics();

        // Remove when above are added
        addFeaturePieMetrics();
        addSettingStringListAdvancedPieMetrics();
        addIllegalItemsPieMetric();
    }

    public void addTrade() {
        tradeCounter++;
    }

    private void addTradeMetric() {
        metrics.addCustomChart(new SingleLineChart("trade-counter", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int count = tradeCounter;
                tradeCounter = 0;
                return count;
            }
        }));
    }

    private void addShopMetric() {
        metrics.addCustomChart(new SingleLineChart("shop-counter", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int count = 0;
                for (World world : plugin.getServer().getWorlds()) {
                    count += plugin.getDataStorage().getShopCountInWorld(world);
                }
                return count;
            }
        }));
    }

    private void addFeatureMetric() {
        metrics.addCustomChart(new AdvancedBarChart("feature-status", new Callable<Map<String, int[]>>() {
            @Override
            public Map<String, int[]> call() throws Exception {
                Map<String, int[]> map = new HashMap<>();
                List<Setting> booleanSettingList = Arrays.asList(Setting.CHECK_UPDATES,
                        Setting.UNLIMITED_ADMIN,
                        Setting.USE_INTERNAL_PERMISSIONS,
                        Setting.ALLOW_TOGGLE_STATUS,
                        Setting.ALLOW_SIGN_BREAK,
                        Setting.ALLOW_CHEST_BREAK,
                        Setting.ALLOW_MULTI_TRADE,
                        Setting.ALLOW_USER_PURCHASING,
                        Setting.TRADESHOP_EXPLODE,
                        Setting.TRADESHOP_HOPPER_EXPORT,
                        Setting.TRADESHOP_HOPPER_IMPORT,
                        Setting.ITRADESHOP_EXPLODE,
                        Setting.BITRADESHOP_EXPLODE,
                        Setting.BITRADESHOP_HOPPER_EXPORT,
                        Setting.BITRADESHOP_HOPPER_IMPORT);

                for (Setting setting : booleanSettingList) {
                    if (setting.getBoolean()) {
                        map.put(setting.name(), new int[]{0, 1});
                    } else {
                        map.put(setting.name(), new int[]{1, 0});
                    }
                }

                return map;
            }
        }));
    }

    private void addIllegalItemsBarMetric() {
        metrics.addCustomChart(new AdvancedBarChart("illegal-item-types", new Callable<Map<String, int[]>>() {
            @Override
            public Map<String, int[]> call() throws Exception {
                Map<String, int[]> map = new HashMap<>();

                switch (plugin.getListManager().getGlobalList().getType()) {
                    case BLACKLIST:
                        map.put("General List", new int[]{1, 0, 0});
                        break;
                    case WHITELIST:
                        map.put("General List", new int[]{0, 1, 0});
                        break;
                    case DISABLED:
                    default:
                        map.put("General List", new int[]{0, 0, 1});
                        break;
                }

                switch (plugin.getListManager().getCostList().getType()) {
                    case BLACKLIST:
                        map.put("Cost List", new int[]{1, 0, 0});
                        break;
                    case WHITELIST:
                        map.put("Cost List", new int[]{0, 1, 0});
                        break;
                    case DISABLED:
                    default:
                        map.put("Cost List", new int[]{0, 0, 1});
                        break;
                }

                switch (plugin.getListManager().getProductList().getType()) {
                    case BLACKLIST:
                        map.put("Product List", new int[]{1, 0, 0});
                        break;
                    case WHITELIST:
                        map.put("Product List", new int[]{0, 1, 0});
                        break;
                    case DISABLED:
                    default:
                        map.put("Product List", new int[]{0, 0, 1});
                        break;
                }
                return map;
            }
        }));
    }

    private void addSettingStringListMetrics() {
        List<Setting> settingList = Arrays.asList(Setting.ALLOWED_DIRECTIONS,
                Setting.ALLOWED_SHOPS);

        for (Setting setting : settingList) {
            metrics.addCustomChart(new SimpleBarChart(setting.name().toLowerCase().replace("_", "-") + "-list", new Callable<Map<String, Integer>>() {
                @Override
                public Map<String, Integer> call() throws Exception {
                    Map<String, Integer> map = new HashMap<>();

                    for (String string : setting.getStringList()) {
                        map.put(string, 1);
                    }

                    return map;
                }
            }));
        }
    }

    private void addOtherSettingMetrics() {
        metrics.addCustomChart(new SimplePie("data-storage-type", Setting.DATA_STORAGE_TYPE::getString));

        metrics.addCustomChart(new SimplePie("max-shop-users", () -> {
            return String.valueOf(Setting.MAX_SHOP_USERS.getInt());
        }));

        metrics.addCustomChart(new SimplePie("max-shops-per-chunk", () -> {
            return String.valueOf(Setting.MAX_SHOPS_PER_CHUNK.getInt());
        }));

        metrics.addCustomChart(new SimplePie("max-items-per-trade-side", () -> {
            return String.valueOf(Setting.MAX_ITEMS_PER_TRADE_SIDE.getInt());
        }));


    }


    // Temporary solutions while Bar graphs are unavailable
    private void addFeaturePieMetrics() {
        List<Setting> booleanSettingList = Arrays.asList(Setting.CHECK_UPDATES,
                Setting.UNLIMITED_ADMIN,
                Setting.USE_INTERNAL_PERMISSIONS,
                Setting.ALLOW_TOGGLE_STATUS,
                Setting.ALLOW_SIGN_BREAK,
                Setting.ALLOW_CHEST_BREAK,
                Setting.ALLOW_MULTI_TRADE,
                Setting.ALLOW_USER_PURCHASING,
                Setting.TRADESHOP_EXPLODE,
                Setting.TRADESHOP_HOPPER_EXPORT,
                Setting.TRADESHOP_HOPPER_IMPORT,
                Setting.ITRADESHOP_EXPLODE,
                Setting.BITRADESHOP_EXPLODE,
                Setting.BITRADESHOP_HOPPER_EXPORT,
                Setting.BITRADESHOP_HOPPER_IMPORT);

        for (Setting setting : booleanSettingList) {
            metrics.addCustomChart(new SimplePie(setting.name().toLowerCase(), () -> {
                return String.valueOf(setting.getBoolean());
            }));
        }
    }

    private void addSettingStringListAdvancedPieMetrics() {
        List<Setting> settingList = Arrays.asList(Setting.ALLOWED_DIRECTIONS,
                Setting.ALLOWED_SHOPS);

        for (Setting setting : settingList) {
            metrics.addCustomChart(new AdvancedPie(setting.name().toLowerCase(), new Callable<Map<String, Integer>>() {
                @Override
                public Map<String, Integer> call() throws Exception {
                    Map<String, Integer> map = new HashMap<>();

                    for (String string : setting.getStringList()) {
                        map.put(string, 1);
                    }

                    return map;
                }
            }));
        }
    }

    private void addIllegalItemsPieMetric() {
        metrics.addCustomChart(new SimplePie("illegal-items-global-type", () -> {
            return plugin.getListManager().getGlobalList().getType().toString();
        }));
        metrics.addCustomChart(new SimplePie("illegal-items-cost-type", () -> {
            return plugin.getListManager().getCostList().getType().toString();
        }));
        metrics.addCustomChart(new SimplePie("illegal-items-product-type", () -> {
            return plugin.getListManager().getProductList().getType().toString();
        }));
    }


}
