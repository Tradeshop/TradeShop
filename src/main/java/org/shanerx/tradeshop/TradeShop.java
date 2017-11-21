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

package org.shanerx.tradeshop;

import org.bstats.bukkit.Metrics;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.shanerx.tradeshop.admin.AdminEventListener;
import org.shanerx.tradeshop.bitrade.BiShopCreateEventListener;
import org.shanerx.tradeshop.bitrade.BiTradeEventListener;
import org.shanerx.tradeshop.commands.Executor;
import org.shanerx.tradeshop.enums.Message;
import org.shanerx.tradeshop.enums.Setting;
import org.shanerx.tradeshop.itrade.IShopCreateEventListener;
import org.shanerx.tradeshop.itrade.ITradeEventListener;
import org.shanerx.tradeshop.trade.ShopCreateEventListener;
import org.shanerx.tradeshop.trade.TradeEventListener;
import org.shanerx.tradeshop.util.CustomItem;
import org.shanerx.tradeshop.util.Updater;

import java.util.ArrayList;
import java.util.Arrays;

public class TradeShop extends JavaPlugin {
    private boolean mc18 = this.getServer().getVersion().contains("1.8");

    private ArrayList<Material> inventories = new ArrayList<>();
    private ArrayList<BlockFace> directions = new ArrayList<>();
    
    private Metrics metrics;


    public Boolean isAboveMC18() {
        return !mc18;
    }

    @Override
    public void reloadConfig() {
        Message.reload();
        Setting.reload();
        CustomItem.reload();

        addMaterials();
        addDirections();
    }

    public ArrayList<Material> getAllowedInventories() {
        return inventories;
    }

    public ArrayList<BlockFace> getAllowedDirections() {
        return directions;
    }

    @Override
    public void onEnable() {
        if (!isAboveMC18()) {
            getLogger().info("[TradeShop] Minecraft versions before 1.9 are not supported beyond TradeShop version 1.5.2!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        reloadConfig();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new TradeEventListener(this), this);
        pm.registerEvents(new ShopCreateEventListener(this), this);
        pm.registerEvents(new BiTradeEventListener(this), this);
        pm.registerEvents(new BiShopCreateEventListener(this), this);
        pm.registerEvents(new AdminEventListener(this), this);
        pm.registerEvents(new ITradeEventListener(this), this);
        pm.registerEvents(new IShopCreateEventListener(this), this);

        getCommand("tradeshop").setExecutor(new Executor(this));

        boolean checkUpdates = Setting.CHECK_UPDATES.getBoolean();
        if (checkUpdates) {
            new Thread(() -> new Updater(getDescription()).checkCurrentVersion()).start();
        }

        if (Setting.ALLOW_METRICS.getBoolean()) {
            metrics = new Metrics(this);
            getLogger().info("Metrics successfully initialized!");
            
        } else {
            getLogger().warning("Metrics are disabled! Please consider enabling them to support the authors!");
        }
    }

    private void addMaterials() {
        inventories.clear();
        ArrayList<Material> allowedOld = new ArrayList<>();
        allowedOld.addAll(Arrays.asList(Material.CHEST, Material.TRAPPED_CHEST, Material.DROPPER, Material.HOPPER, Material.DISPENSER));

        for (String str : Setting.ALLOWED_SHOPS.getStringList()) {
            if (str.equalsIgnoreCase("shulker")) {
                inventories.addAll(Arrays.asList(Material.BLACK_SHULKER_BOX,
                        Material.BLUE_SHULKER_BOX,
                        Material.BROWN_SHULKER_BOX,
                        Material.CYAN_SHULKER_BOX,
                        Material.GRAY_SHULKER_BOX,
                        Material.GREEN_SHULKER_BOX,
                        Material.LIGHT_BLUE_SHULKER_BOX,
                        Material.LIME_SHULKER_BOX,
                        Material.MAGENTA_SHULKER_BOX,
                        Material.ORANGE_SHULKER_BOX,
                        Material.PINK_SHULKER_BOX,
                        Material.RED_SHULKER_BOX,
                        Material.SILVER_SHULKER_BOX,
                        Material.WHITE_SHULKER_BOX,
                        Material.YELLOW_SHULKER_BOX,
                        Material.PURPLE_SHULKER_BOX));
            } else {
                if (allowedOld.contains(Material.valueOf(str)))
                    inventories.add(Material.valueOf(str));

            }
        }
    }

    private void addDirections() {
        directions.clear();
        ArrayList<BlockFace> allowed = new ArrayList<>();
        allowed.addAll(Arrays.asList(BlockFace.DOWN, BlockFace.WEST, BlockFace.SOUTH, BlockFace.EAST, BlockFace.NORTH, BlockFace.UP));

        for (String str : Setting.ALLOWED_DIRECTIONS.getStringList()) {
            if (allowed.contains(BlockFace.valueOf(str)))
                directions.add(BlockFace.valueOf(str));
        }
    }
}
