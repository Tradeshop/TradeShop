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

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.shanerx.tradeshop.admin.AdminEventListener;
import org.shanerx.tradeshop.bitrade.BiShopCreateEventListener;
import org.shanerx.tradeshop.bitrade.BiTradeEventListener;
import org.shanerx.tradeshop.commands.Executor;
import org.shanerx.tradeshop.itrade.IShopCreateEventListener;
import org.shanerx.tradeshop.itrade.ITradeEventListener;
import org.shanerx.tradeshop.trade.ShopCreateEventListener;
import org.shanerx.tradeshop.trade.TradeEventListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

public class TradeShop extends JavaPlugin {
    private File messagesFile = new File(this.getDataFolder(), "messages.yml");
    private FileConfiguration messages;
    private File settingsFile = new File(this.getDataFolder(), "config.yml");
    private FileConfiguration settings;
    private boolean mc18 = this.getServer().getVersion().contains("1.8");

    private ArrayList<Material> inventories = new ArrayList<>();
    private ArrayList<BlockFace> directions = new ArrayList<>();

    public File getMessagesFile() {
        return messagesFile;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public File getSettingsFile() {
        return settingsFile;
    }

    public FileConfiguration getSettings() {
        return settings;
    }

    public Boolean isAboveMC18() {
        return !mc18;
    }

    @Deprecated
    @Override
    public FileConfiguration getConfig() {
        return settings;
    }

    @Override
    public void reloadConfig() {
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        addMessageDefaults();
        settings = YamlConfiguration.loadConfiguration(settingsFile);
        addSettingsDefaults();

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
        }

        createConfigs();
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
        
        new Thread(() -> new Updater(getDescription()).checkCurrentVersion()).start();
    }

    private void addMaterials() {
        ArrayList<Material> allowedOld = new ArrayList<>();
        allowedOld.addAll(Arrays.asList(Material.CHEST, Material.TRAPPED_CHEST, Material.DROPPER, Material.HOPPER, Material.DISPENSER));

        for (String str : getConfig().getStringList("allowed-shops")) {
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
        ArrayList<BlockFace> allowed = new ArrayList<>();
        allowed.addAll(Arrays.asList(BlockFace.DOWN, BlockFace.WEST, BlockFace.SOUTH, BlockFace.EAST, BlockFace.NORTH, BlockFace.UP));

        for (String str : getConfig().getStringList("allowed-directions")) {
            if (allowed.contains(BlockFace.valueOf(str)))
                directions.add(BlockFace.valueOf(str));
        }
    }

    private void addMessage(String node, String message) {
        if (messages.getString(node) == null) {
            messages.set(node, message);
        }
    }

    private void addSetting(String node, Object value) {
        if (settings.getString(node) == null) {
            settings.set(node, value);
        }
    }

    private void addMessageDefaults() {
        addMessage("invalid-arguments", "&eTry &6/tradeshop help &eto display help!");
        addMessage("no-command-permission", "&aYou do not have permission to execute this command");
        addMessage("setup-help", "\n&2Setting up a TradeShop is easy! Just make sure to follow these steps:"
                + "\n \nStep 1: &ePlace down a chest."
                + "\n&2Step 2: &ePlace a sign on top of the chest."
                + "\n&2Step 3: &eWrite the following on the sign"
                + "\n&6[Trade]\n<amount> <item_you_sell>\n<amount> <item_you_buy>\n&6&oEmpty line"
                + "\n&2Step 4: &eIf you are unsure what the item is, use &6/tradeshop item");
        addMessage("no-ts-create-permission", "&cYou don't have permission to create TradeShops!");
        addMessage("no-chest", "&cYou need to put a chest under the sign!");
        addMessage("invalid-sign", "&cInvalid sign format!");
        addMessage("no-ts-destroy", "&cYou may not destroy that TradeShop");
        addMessage("successful-setup", "&aYou have successfully setup a TradeShop!");
        addMessage("no-ts-open", "&cThat TradeShop does not belong to you");
        addMessage("empty-ts-on-setup", "&cTradeShop empty, please remember to fill it!");
        addMessage("on-trade", "&aYou have traded your&e {AMOUNT2} {ITEM2} &a for &e {AMOUNT1} {ITEM1} &awith {SELLER}");
        addMessage("insufficient-items", "&cYou do not have &e {AMOUNT} {ITEM}&c!");
        addMessage("shop-full-amount", "&cThe shop does not have &e{AMOUNT} &cof a single type of &e{ITEM}&c!");
        addMessage("full-amount", "&cYou must have &e{AMOUNT} &cof a single type of &e{ITEM}&c!");
        addMessage("shop-empty", "&cThis TradeShop does not have &e {AMOUNT} {ITEM}&c!");
        addMessage("shop-full", "&cThis TradeShop is full, please contact the owner to get it emptied!");
        addMessage("player-full", "&cYour inventory is full, please make room before trading items!");
        addMessage("confirm-trade", "&eTrade &6 {AMOUNT1} {ITEM1} &e for &6 {AMOUNT2} {ITEM2} &e?");
        addMessage("held-item", "\n&6You are currently holding: " +
                "\n&2Material: &e{MATERIAL}" +
                "\n&2ID Number: &e{ID}" +
                "\n&2Durability: &e{DURABILITY}" +
                "\n&2Amount: &e{AMOUNT}" +
                "\n&6You do not need to use the durability if it is 0" +
                "\n&6Put this on your TradeShop sign: " +
                "\n&e{AMOUNT} {MATERIAL}:{DURABILITY} " +
                "\n&e{AMOUNT} {ID}:{DURABILITY}");
        addMessage("held-empty", "&eYou are currently holding nothing.");
        addMessage("player-only-command", "&eThis command is only available to players.");
        addMessage("missing-shop", "&cThere is not currently a shop here, please tell the owner or come back later!");
        addMessage("no-sighted-shop", "&cNo shop in range!");
        addMessage("updated-shop-members", "&aShop owners and members have been updated!");
        addMessage("unsuccessful-shop-members", "&aThat player is either already on the shop, or you have reached the maximum number of users!");
        addMessage("who-message", "&6Shop users are:\n&2Owners: &e{OWNERS}\n&2Members: &e{MEMBERS}");
        addMessage("self-owned", "&cYou cannot buy from a shop in which you are a user.");
        addMessage("not-owner", "&cYou cannot create a sign for a shop that you do not own.");

        save();
    }

    private void addSettingsDefaults() {
        addSetting("allowed-shops", new String[]{"CHEST", "TRAPPED_CHEST", "SHULKER"});
        addSetting("allowed-directions", new String[]{"DOWN", "WEST", "SOUTH", "EAST", "NORTH", "UP"});
        addSetting("itrade-shop-name", "Server Shop");
        addSetting("allow-double-trade", true);
        addSetting("allow-quad-trade", true);
        addSetting("max-edit-distance", 4);
        addSetting("max-shop-users", 5);

        save();
    }

    private void save() {
        if (messages != null)
            try {
                messages.save(messagesFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

        if (settings != null)
            try {
                settings.save(settingsFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private void createConfigs() {
        try {
            if (!getDataFolder().isDirectory()) {
                getDataFolder().mkdirs();
            }
            if (!messagesFile.exists()) {
                messagesFile.createNewFile();
            }
            if (!settingsFile.exists()) {
                settingsFile.createNewFile();
            }
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not create config files! Disabling plugin!", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
    }
}
