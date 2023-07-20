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

package org.shanerx.tradeshop.utils.management;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedBarChart;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimpleBarChart;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.World;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.data.config.Message;
import org.shanerx.tradeshop.data.config.Setting;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricsManager {
    private final Metrics metrics;
    private final VarManager varManager;

    public MetricsManager(TradeShop plugin) {
        this.varManager = plugin.getVarManager();
        metrics = new Metrics(plugin, varManager.getbStatsPluginID());

        for (World world : plugin.getServer().getWorlds()) {
            adjustShops(plugin.getDataStorage().getShopCountInWorld(world));
        }

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
        varManager.tradeCounter.set(varManager.lastIndex, varManager.tradeCounter.get(varManager.lastIndex) + 1);
    }

    public void adjustShops(int adjustment) {
        varManager.shopCounter += adjustment;
    }

    private void addTradeMetric() {
        metrics.addCustomChart(new SingleLineChart("trade-counter", () -> {
            int count = varManager.tradeCounter.get(varManager.lastIndex);
            varManager.tradeCounter.add(0);
            return count;
        }));
    }

    private void addShopMetric() {
        metrics.addCustomChart(new SingleLineChart("shop-counter", () -> varManager.shopCounter));
    }

    private void addFeatureMetric() {
        metrics.addCustomChart(new AdvancedBarChart("feature-status", () -> {
            Map<String, int[]> map = new HashMap<>();
            List<Setting> booleanSettingList = Arrays.asList(
                    Setting.CHECK_UPDATES,
                    Setting.UNLIMITED_ADMIN,
                    Setting.ALLOW_TOGGLE_STATUS,
                    Setting.ALLOW_SIGN_BREAK,
                    Setting.ALLOW_CHEST_BREAK,
                    Setting.ALLOW_MULTI_TRADE,
                    Setting.ALLOW_USER_PURCHASING,
                    Setting.TRADESHOP_EXPLODE,
                    Setting.ITRADESHOP_EXPLODE,
                    Setting.BITRADESHOP_EXPLODE);

            for (Setting setting : booleanSettingList) {
                if (setting.getBoolean()) {
                    map.put(setting.name(), new int[]{0, 1});
                } else {
                    map.put(setting.name(), new int[]{1, 0});
                }
            }

            return map;
        }));
    }

    private void addIllegalItemsBarMetric() {
        metrics.addCustomChart(new AdvancedBarChart("illegal-item-types", () -> {
            Map<String, int[]> map = new HashMap<>();

            switch (varManager.getListManager().getGlobalList().getType()) {
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

            switch (varManager.getListManager().getCostList().getType()) {
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

            switch (varManager.getListManager().getProductList().getType()) {
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
        }));
    }

    private void addSettingStringListMetrics() {
        List<Setting> settingList = Arrays.asList(Setting.CHEST_DIRECTIONS,
                Setting.ALLOWED_SHOPS);

        for (Setting setting : settingList) {
            metrics.addCustomChart(new SimpleBarChart(setting.name().toLowerCase().replace("_", "-") + "-list", () -> {
                Map<String, Integer> map = new HashMap<>();

                for (String string : setting.getStringList()) {
                    map.put(string, 1);
                }

                return map;
            }));
        }
    }

    private void addOtherSettingMetrics() {
        metrics.addCustomChart(new SimplePie("data-storage-type", Setting.DATA_STORAGE_TYPE::getString));

        metrics.addCustomChart(new SimplePie("language", Message.LANGUAGE::getString));

        metrics.addCustomChart(new SimplePie("max-shop-users", () -> String.valueOf(Setting.MAX_SHOP_USERS.getInt())));

        metrics.addCustomChart(new SimplePie("max-shops-per-chunk", () -> String.valueOf(Setting.MAX_SHOPS_PER_CHUNK.getInt())));

        metrics.addCustomChart(new SimplePie("max-items-per-trade-side", () -> String.valueOf(Setting.MAX_ITEMS_PER_TRADE_SIDE.getInt())));
    }


    // Temporary solutions while Bar graphs are unavailable
    private void addFeaturePieMetrics() {
        List<Setting> booleanSettingList = Arrays.asList(
                Setting.CHECK_UPDATES,
                Setting.UNLIMITED_ADMIN,
                Setting.ALLOW_TOGGLE_STATUS,
                Setting.ALLOW_SIGN_BREAK,
                Setting.ALLOW_CHEST_BREAK,
                Setting.ALLOW_MULTI_TRADE,
                Setting.ALLOW_USER_PURCHASING,
                Setting.TRADESHOP_EXPLODE,
                Setting.ITRADESHOP_EXPLODE,
                Setting.BITRADESHOP_EXPLODE);

        for (Setting setting : booleanSettingList) {
            metrics.addCustomChart(new SimplePie(setting.name().toLowerCase(), () -> String.valueOf(setting.getBoolean())));
        }
    }

    private void addSettingStringListAdvancedPieMetrics() {
        List<Setting> settingList = Arrays.asList(Setting.CHEST_DIRECTIONS,
                Setting.ALLOWED_SHOPS);

        for (Setting setting : settingList) {
            metrics.addCustomChart(new AdvancedPie(setting.name().toLowerCase(), () -> {
                Map<String, Integer> map = new HashMap<>();

                for (String string : setting.getStringList()) {
                    map.put(string, 1);
                }

                return map;
            }));
        }
    }

    private void addIllegalItemsPieMetric() {
        metrics.addCustomChart(new SimplePie("illegal-items-global-type", () -> varManager.getListManager().getGlobalList().getType().toString()));
        metrics.addCustomChart(new SimplePie("illegal-items-cost-type", () -> varManager.getListManager().getCostList().getType().toString()));
        metrics.addCustomChart(new SimplePie("illegal-items-product-type", () -> varManager.getListManager().getProductList().getType().toString()));
    }


}
