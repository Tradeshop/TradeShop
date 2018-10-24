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
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.shanerx.tradeshop.admin.ShopProtectionHandler;
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
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;

public class TradeShop extends JavaPlugin {

    private File messagesFile = new File(this.getDataFolder(), "messages.yml");
    private FileConfiguration messages;

    private File settingsFile = new File(this.getDataFolder(), "config.yml");
    private FileConfiguration settings;

    private File customItemsFile = new File(this.getDataFolder(), "customitems.yml");
    private FileConfiguration customItems;

    private boolean mc18 = this.getServer().getVersion().contains("1.8");

    private ArrayList<Material> inventories = new ArrayList<>();
    private ArrayList<BlockFace> directions = new ArrayList<>();
    private ArrayList<String> blacklist = new ArrayList<>();

    public FileConfiguration getMessages() {
        return messages;
    }

    @Override
    public FileConfiguration getConfig() {
        return settings;
    }

    public FileConfiguration getCustomItems() {
        return customItems;
    }

    public Boolean isAboveMC18() {
        return !mc18;
    }

    public FileConfiguration getSettings() {
        return settings;
    }

    @Override
    public void reloadConfig() {

        messages = YamlConfiguration.loadConfiguration(messagesFile);
        settings = YamlConfiguration.loadConfiguration(settingsFile);
        customItems = YamlConfiguration.loadConfiguration(customItemsFile);

        addMessageDefaults();
        addSettingsDefaults();
        addCustomItemsDefaults();

        addMaterials();
        addDirections();
        addIllegalItems();
    }

    public ArrayList<Material> getAllowedInventories() {
        return inventories;
    }

    public ArrayList<BlockFace> getAllowedDirections() {
        return directions;
    }

    public ArrayList<String> getIllegalItems() {
        return blacklist;
    }

    @Override
    public void onEnable() {

        if (!isAboveMC18()) {
            getLogger().info("[TradeShop] Minecraft versions before 1.9 are not supported beyond TradeShop version 1.5.2!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        createConfigs();
        reloadConfig();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new TradeEventListener(this), this);
        pm.registerEvents(new ShopCreateEventListener(this), this);
        pm.registerEvents(new BiTradeEventListener(this), this);
        pm.registerEvents(new BiShopCreateEventListener(this), this);
        pm.registerEvents(new ShopProtectionHandler(this), this);
        pm.registerEvents(new ITradeEventListener(this), this);
        pm.registerEvents(new IShopCreateEventListener(this), this);

        getCommand("tradeshop").setExecutor(new Executor(this));

        boolean checkUpdates = getSettings().getBoolean("check-updates");
        if (checkUpdates) {
            new Thread(() -> new Updater(getDescription()).checkCurrentVersion()).start();
        }

        if (getSettings().getBoolean("allow-metrics")) {
            new Metrics(this);
            getLogger().info("Metrics successfully initialized!");

        } else {
            getLogger().warning("Metrics are disabled! Please consider enabling them to support the authors!");
        }
    }

    private void addMaterials() {

        inventories.clear();
        ArrayList<Material> allowedOld = new ArrayList<>(Arrays.asList(Material.CHEST, Material.TRAPPED_CHEST, Material.DROPPER, Material.HOPPER, Material.DISPENSER));

        for (String str : getSettings().getStringList("allowed-shops")) {
            if (str.equalsIgnoreCase("shulker")) {
                inventories.addAll(Arrays.asList(
                        Material.BLACK_SHULKER_BOX,
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
                        Material.LIGHT_GRAY_SHULKER_BOX,
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
        ArrayList<BlockFace> allowed = new ArrayList<>(Arrays.asList(BlockFace.DOWN, BlockFace.WEST, BlockFace.SOUTH, BlockFace.EAST, BlockFace.NORTH, BlockFace.UP));

        for (String str : getSettings().getStringList("allowed-directions")) {
            if (allowed.contains(BlockFace.valueOf(str)))
                directions.add(BlockFace.valueOf(str));
        }
    }

    private void addIllegalItems() {
        blacklist.clear();
        blacklist.add("air");
        for (String s : getSettings().getStringList("illegal-items")) {
            blacklist.add(s.toLowerCase());
        }
    }

    private int addMessage(String node, String message) {
        if (messages.get(node) == null) {
            messages.set(node, message);
            return 1;
        }

        return 0;
    }

    private int addSetting(String node, Object value) {
        if (settings.get(node) == null) {
            settings.set(node, value);
            return 1;
        }

        return 0;
    }

    private void addMessageDefaults() {
        int toSave = 0;

        toSave += addMessage("invalid-arguments", "&eTry &6/tradeshop help &eto display help!");
        toSave += addMessage("no-command-permission", "&aYou do not have permission to execute this command");
        toSave += addMessage("setup-help", "\n&2Setting up a TradeShop is easy! Just make sure to follow these steps:"
                + "\n \nStep 1: &ePlace down a chest."
                + "\n&2Step 2: &ePlace a sign on top of the chest."
                + "\n&2Step 3: &eWrite the following on the sign"
                + "\n&6[%header%]\n<amount> <item_you_sell>\n<amount> <item_you_buy>\n&6&oEmpty line"
                + "\n&2Step 4: &eIf you are unsure what the item is, use &6/tradeshop item");
        toSave += addMessage("no-ts-create-permission", "&cYou don't have permission to create TradeShops!");
        toSave += addMessage("no-chest", "&cYou need to put a chest under the sign!");
        toSave += addMessage("invalid-sign", "&cInvalid sign format!");
        toSave += addMessage("no-ts-destroy", "&cYou may not destroy that TradeShop");
        toSave += addMessage("successful-setup", "&aYou have successfully setup a TradeShop!");
        toSave += addMessage("no-ts-open", "&cThat TradeShop does not belong to you");
        toSave += addMessage("empty-ts-on-setup", "&cTradeShop empty, please remember to fill it!");
        toSave += addMessage("on-trade", "&aYou have traded your&e {AMOUNT2} {ITEM2} &a for &e {AMOUNT1} {ITEM1} &awith {SELLER}");
        toSave += addMessage("insufficient-items", "&cYou do not have &e {AMOUNT} {ITEM}&c!");
        toSave += addMessage("shop-full-amount", "&cThe shop does not have &e{AMOUNT} &cof a single type of &e{ITEM}&c!");
        toSave += addMessage("full-amount", "&cYou must have &e{AMOUNT} &cof a single type of &e{ITEM}&c!");
        toSave += addMessage("shop-empty", "&cThis TradeShop does not have &e {AMOUNT} {ITEM}&c!");
        toSave += addMessage("shop-full", "&cThis TradeShop is full, please contact the owner to get it emptied!");
        toSave += addMessage("player-full", "&cYour inventory is full, please make room before trading items!");
        toSave += addMessage("confirm-trade", "&eTrade &6 {AMOUNT1} {ITEM1} &e for &6 {AMOUNT2} {ITEM2} &e?");
        toSave += addMessage("held-item", "\n&6You are currently holding: " +
                "\n&2Material: &e{MATERIAL}" +
                "\n&2ID Number: &e{ID}" +
                "\n&2Durability: &e{DURABILITY}" +
                "\n&2Amount: &e{AMOUNT}" +
                "\n&6You do not need to use the durability if it is 0" +
                "\n&6Put this on your TradeShop sign: " +
                "\n&e{AMOUNT} {MATERIAL}:{DURABILITY} " +
                "\n&e{AMOUNT} {ID}:{DURABILITY}");
        toSave += addMessage("held-empty", "&eYou are currently holding nothing.");
        toSave += addMessage("player-only-command", "&eThis command is only available to players.");
        toSave += addMessage("missing-shop", "&cThere is not currently a shop here, please tell the owner or come back later!");
        toSave += addMessage("no-sighted-shop", "&cNo shop in range!");
        toSave += addMessage("updated-shop-members", "&aShop owners and members have been updated!");
        toSave += addMessage("unsuccessful-shop-members", "&aThat player is either already on the shop, or you have reached the maximum number of users!");
        toSave += addMessage("who-message", "&6Shop users are:\n&2Owners: &e{OWNERS}\n&2Members: &e{MEMBERS}");
        toSave += addMessage("self-owned", "&cYou cannot buy from a shop in which you are a user.");
        toSave += addMessage("not-owner", "&cYou cannot create a sign for a shop that you do not own.");
        toSave += addMessage("illegal-item", "&cYou cannot use one or more of those items in shops.");
        toSave += addMessage("missing-item", "&cYour sign is missing an item for trade.");
        toSave += addMessage("missing-info", "&cYour sign is missing necessary information.");
        toSave += addMessage("amount-not-num", "&cYou should have an amount before each item.");
        toSave += addMessage("buy-failed-sign", "&cThis shop sign does not seem to be formatted correctly, please notify the owner.");

        if (toSave > 0)
            save();
    }

    private void addSettingsDefaults() {
        int toSave = 0;

        toSave += addSetting("check-updates", true);
        toSave += addSetting("allow-metrics", true);
        toSave += addSetting("allowed-shops", new String[]{"CHEST", "TRAPPED_CHEST", "SHULKER"});
        toSave += addSetting("allowed-directions", new String[]{"DOWN", "WEST", "SOUTH", "EAST", "NORTH", "UP"});
        toSave += addSetting("itrade-shop-name", "Server Shop");
        toSave += addSetting("allow-double-trade", true);
        toSave += addSetting("allow-quad-trade", true);
        toSave += addSetting("max-edit-distance", 4);
        toSave += addSetting("max-shop-users", 5);
        toSave += addSetting("illegal-items", new String[]{"Bedrock", "Command_Block"});
        toSave += addSetting("allow-custom-illegal-items", true);
        toSave += addSetting("tradeshop-name", "Trade");
        toSave += addSetting("itradeshop-name", "iTrade");
        toSave += addSetting("bitradeshop-name", "BiTrade");
        toSave += addSetting("allow-metrics", true);
        toSave += addSetting("explode.trade", false);
        toSave += addSetting("explode.itrade", false);
        toSave += addSetting("explode.bitrade", false);

        if (toSave > 0)
            save();
    }

    public void addCustomItem(String name, ItemStack itm) {
        if (!customItems.getValues(false).containsKey(name)) {
            customItems.createSection(name);

            customItems.set(name, itm.serialize());

            save();
        }
    }

    public void removeCustomItem(String name) {
        if (customItems.getValues(false).containsKey(name)) {
            customItems.set(name, null);

            save();
        }
    }

    public Set<String> getCustomItemSet() {
        return customItems.getValues(false).keySet();
    }

    private void addCustomItemsDefaults() {

        if (getCustomItems().getValues(false).isEmpty()) {
            ItemStack dataHolder = new ItemStack(Material.TRIPWIRE_HOOK);
            ItemMeta meta = dataHolder.getItemMeta();

            meta.setDisplayName("Key");
            meta.setLore(Collections.singletonList("&aThe key to your dreams."));
            dataHolder.setItemMeta(meta);

            addCustomItem("Key", dataHolder);
        }
    }

    public ItemStack getCustomItem(String name) {

        ItemStack itm = null;
        String cName = "";

        for (String s : getCustomItems().getKeys(false)) {
            if (s.equalsIgnoreCase(name)) {
                cName = s;
            }
        }

        if (cName.length() == 0) {
            cName = name;
        }

        if (!(getCustomItems().get(cName) == null)) {
            itm = ItemStack.deserialize(getCustomItems().getConfigurationSection(name).getValues(true));
            ItemMeta meta = itm.getItemMeta();

            if (meta.hasLore()) {
                ArrayList<String> str2 = new ArrayList<>();
                for (String s : meta.getLore()) {
                    str2.add(ChatColor.translateAlternateColorCodes('&', s));
                }
                meta.setLore(str2);
            }

            if (meta.hasDisplayName())
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', meta.getDisplayName()));

            itm.setItemMeta(meta);
        }

        return itm;
    }

    public void save() {

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

        if (customItems != null)
            try {
                customItems.save(customItemsFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

        reloadConfig();
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
            if (!customItemsFile.exists()) {
                customItemsFile.createNewFile();
            }
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not create config files! Disabling plugin!", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }
}
