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

package org.shanerx.tradeshop.item;

import com.bergerkiller.bukkit.common.config.JsonSerializer;
import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import de.leonhard.storage.sections.FlatFileSection;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.utils.debug.Debug;
import org.shanerx.tradeshop.utils.debug.DebugLevels;
import org.shanerx.tradeshop.utils.gsonprocessing.GsonProcessor;
import org.shanerx.tradeshop.utils.objects.ObjectHolder;
import org.shanerx.tradeshop.utils.simplix.serializers.ConfSerSerializer;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ShopItemStack implements Cloneable {

    @Expose(serialize = false)
    private ItemStack itemStack;
    private String itemStackString;
    private transient Debug debugger;

    @Expose(serialize = false)
    private String itemStackB64;

    @Expose
    @SerializedName(value = "itemSettings", alternate = "shopSettings")
    private Map<ShopItemStackSettingKeys, ObjectHolder<?>> itemSettings;

    /**
     * Create a ShopItemStack with a Base64 encoded {@link ItemStack} and default item Settings.
     *
     * @param itemStack Bukkit ItemStack object to be used
     * @deprecated Constructors for ShopItemStack are being depreciated in favor of {@link ShopItemStackBuilder}
     */
    @Deprecated
    public ShopItemStack(ItemStack itemStack) {
        this(itemStack, new HashMap<>());
    }

    /**
     * Create a ShopItemStack with an {@link ItemStack} and Map containing item Settings.
     * @deprecated
     * Constructors for ShopItemStack are being depreciated in favor of {@link ShopItemStackBuilder}
     *
     * @param itemStack Bukkit ItemStack object to be used
     * @param itemSettings Map of ShopItemStackSettingKeys and ObjectHolders
     */
    @Deprecated
    public ShopItemStack(ItemStack itemStack, Map<ShopItemStackSettingKeys, ObjectHolder<?>> itemSettings) {
        this.itemStack = itemStack;
        this.itemSettings = itemSettings;
        buildMap();
    }

    /**
     * Create a ShopItemStack with a Base64 encoded {@link ItemStack} and default item Settings.
     * @deprecated
     * Constructors for ShopItemStack are being depreciated in favor of {@link ShopItemStackBuilder}
     *
     * @param itemStackB64 Base64 encoded ItemStack
     */
    @Deprecated
    public ShopItemStack(String itemStackB64) {
        this(itemStackB64, new HashMap<>());
    }

    /**
     * Create a ShopItemStack with a Base64 encoded {@link ItemStack} and Map containing item Settings.
     * @deprecated
     * Constructors for ShopItemStack are being depreciated in favor of {@link ShopItemStackBuilder}
     *
     * @param itemStackB64 Base64 encoded ItemStack
     * @param itemSettings Map of ShopItemStackSettingKeys and ObjectHolders
     */
    @Deprecated
    public ShopItemStack(String itemStackB64, HashMap<String, ObjectHolder<?>> itemSettings) {
        this.itemStackB64 = itemStackB64;
        this.itemSettings = new HashMap<>();

        itemSettings.forEach((key, value) -> this.itemSettings.put(ShopItemStackSettingKeys.match(key), value));

        buildMap();
        try {
            fixLoadedData();
        } catch (JsonSerializer.JsonSyntaxException e) {
            itemStack = null;
            itemSettings = null;
        }
    }

    /**
     * Re-added for backwards compatibility
     * Create a ShopItemStack with a Base64 encoded {@link ItemStack} and argument for each setting.
     * @deprecated
     * Constructors for ShopItemStack are being depreciated in favor of {@link ShopItemStackBuilder}
     *
     * @param itemStackB64 Base64 encoded ItemStack
     * @param compareDurability Compare Durability
     * @param compareEnchantments Compare Enchantments
     * @param compareName Compare Name
     * @param compareLore Compare Lore
     * @param compareCustomModelData Compare Custom Model Data
     * @param compareItemFlags Compare Item Flags
     * @param compareUnbreakable Compare Unbreakable
     * @param compareAttributeModifier Compare Attribute Modifier
     * @param compareBookAuthor Compare Book Author
     * @param compareBookPages Compare Book Pages
     * @param compareShulkerInventory Compare Shulker Inventory
     */
    @Deprecated
    public ShopItemStack(String itemStackB64, int compareDurability, boolean compareEnchantments,
                         boolean compareName, boolean compareLore, boolean compareCustomModelData,
                         boolean compareItemFlags, boolean compareUnbreakable, boolean compareAttributeModifier,
                         boolean compareBookAuthor, boolean compareBookPages, boolean compareShulkerInventory) {
        this.itemStackB64 = itemStackB64;

        itemSettings = new HashMap<>();

        itemSettings.putIfAbsent(ShopItemStackSettingKeys.COMPARE_DURABILITY, new ObjectHolder<>(compareDurability));
        itemSettings.putIfAbsent(ShopItemStackSettingKeys.COMPARE_NAME, new ObjectHolder<>(compareName));
        itemSettings.putIfAbsent(ShopItemStackSettingKeys.COMPARE_LORE, new ObjectHolder<>(compareLore));
        itemSettings.putIfAbsent(ShopItemStackSettingKeys.COMPARE_CUSTOM_MODEL_DATA, new ObjectHolder<>(compareCustomModelData));
        itemSettings.putIfAbsent(ShopItemStackSettingKeys.COMPARE_ITEM_FLAGS, new ObjectHolder<>(compareItemFlags));
        itemSettings.putIfAbsent(ShopItemStackSettingKeys.COMPARE_UNBREAKABLE, new ObjectHolder<>(compareUnbreakable));
        itemSettings.putIfAbsent(ShopItemStackSettingKeys.COMPARE_ATTRIBUTE_MODIFIER, new ObjectHolder<>(compareAttributeModifier));
        itemSettings.putIfAbsent(ShopItemStackSettingKeys.COMPARE_BOOK_AUTHOR, new ObjectHolder<>(compareBookAuthor));
        itemSettings.putIfAbsent(ShopItemStackSettingKeys.COMPARE_BOOK_PAGES, new ObjectHolder<>(compareBookPages));
        itemSettings.putIfAbsent(ShopItemStackSettingKeys.COMPARE_SHULKER_INVENTORY, new ObjectHolder<>(compareShulkerInventory));
        itemSettings.putIfAbsent(ShopItemStackSettingKeys.COMPARE_ENCHANTMENTS, new ObjectHolder<>(compareEnchantments));

        buildMap();
        try {
            fixLoadedData();
        } catch (JsonSerializer.JsonSyntaxException e) {
            itemStack = null;
            itemSettings = null;
        }
    }

    public static ShopItemStack deserialize(FlatFileSection serialized) {
        ShopItemStackBuilder item = new ShopItemStackBuilder();

        Map<ShopItemStackSettingKeys, ObjectHolder<?>> settings = new HashMap<>();

        for (String key : serialized.keySet()) {
            switch (key) {
                case "itemStackString":
                    item.setItemStack(ConfSerSerializer.deserializeItemStack(serialized.getMapParameterized(key)));
                    break;
                case "itemSettings":
                case "shopSettings":
                    Map<String, Map<String, Object>> temp = serialized.getMapParameterized(key);
                    temp.forEach((k, v) -> settings.put(ShopItemStackSettingKeys.valueOf(k), ObjectHolder.deserialize(v)));
                    item.setItemSettings(settings);
                    break;
                case "itemStackB64":
                    item.setItemStackB64(serialized.getString(key));
                    break;
            }
        }

        return item.build();
    }

    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        Map<String, Object> settings = new HashMap<>();
        itemSettings.forEach((key, value) -> settings.put(key.getConfigName(), value.serialize()));

        map.put("itemStackString", ConfSerSerializer.serialize(itemStack));
        map.put("itemSettings", settings);
        if (itemStackB64 != null && !itemStackB64.isEmpty()) map.put("itemStackB64", itemStackB64);

        return map;
    }

    public ObjectHolder<?> getShopSetting(ShopItemStackSettingKeys key) {
        if (itemSettings == null) buildMap();

        if (key.isUserEditable() && itemSettings.containsKey(key)) {
            ObjectHolder<?> tempObj = itemSettings.get(key);
            if (tempObj != null && tempObj.getObject() != null)
                return tempObj;
        }

        return key.getDefaultValue();
    }

    private void buildMap() {
        if (itemSettings == null) {
            itemSettings = new HashMap<>();
        }

        for (ShopItemStackSettingKeys key : ShopItemStackSettingKeys.values()) {
            itemSettings.putIfAbsent(key, key.getDefaultValue());
        }
    }

    public ShopItemStack clone() {
        try {
            ShopItemStack clone = (ShopItemStack) super.clone();
            if (itemStack != null)
                clone.itemStack = this.itemStack.clone();

            return clone;
        } catch (CloneNotSupportedException var2) {
            throw new Error(var2);
        }
    }

    public void setShopSettings(ShopItemStackSettingKeys key, ObjectHolder<?> value) {
        if (itemSettings == null) {
            itemSettings = new HashMap<>();
            buildMap();
        }

        itemSettings.put(key, value);
    }

    public int getAmount() {
        return itemStack == null ? 0 : itemStack.getAmount();
    }

    public void setAmount(int amount) {
        itemStack.setAmount(amount);
    }

    public boolean hasBase64() {
        return itemStackB64 != null && !itemStackB64.isEmpty();
    }

    public boolean isSimilar(ItemStack toCompare) {
        debugger = TradeShop.getPlugin().getDebugger();

        // Return False if either item is null
        if (itemStack == null || toCompare == null) {
            return false;
        }

        // Return True if items are equal
        if (itemStack == toCompare) {
            return false;
        }

        // Return False if ItemStacks are different MaterialTypes
        if (itemStack.getType() != toCompare.getType()) {
            debugger.log("itemstack material: " + itemStack.getType(), DebugLevels.ITEM_COMPARE);
            debugger.log("toCompare material: " + toCompare.getType(), DebugLevels.ITEM_COMPARE);
            return false;
        }

        ItemMeta itemStackMeta = itemStack.getItemMeta(),
                toCompareMeta = toCompare.getItemMeta();
        BookMeta itemStackBookMeta = itemStack.hasItemMeta() && itemStack.getItemMeta() instanceof BookMeta ? ((BookMeta) itemStackMeta) : null,
                toCompareBookMeta = toCompare.hasItemMeta() && toCompare.getItemMeta() instanceof BookMeta ? ((BookMeta) toCompareMeta) : null;

        boolean useMeta = itemStack.hasItemMeta() == toCompare.hasItemMeta() && itemStack.hasItemMeta(),
                useBookMeta = itemStackBookMeta != null && toCompareBookMeta != null;

        debugger.log("itemstack useMeta: " + useMeta, DebugLevels.ITEM_COMPARE);
        debugger.log("toCompare useMeta: " + useMeta, DebugLevels.ITEM_COMPARE);

        // If compareShulkerInventory is on
        if (itemStack.getType().toString().endsWith("SHULKER_BOX") &&
                getShopSetting(ShopItemStackSettingKeys.COMPARE_SHULKER_INVENTORY).asBoolean()) {
            try {
                ArrayList<ItemStack> itemStackContents = Lists.newArrayList(((ShulkerBox) ((BlockStateMeta) toCompareMeta).getBlockState()).getInventory().getContents()),
                        toCompareContents = Lists.newArrayList(((ShulkerBox) ((BlockStateMeta) itemStackMeta).getBlockState()).getInventory().getContents());

                itemStackContents.removeIf(Objects::isNull);
                toCompareContents.removeIf(Objects::isNull);

                if (itemStackContents.isEmpty() != toCompareContents.isEmpty())
                    return false;

                for (ItemStack itm : toCompareContents) {
                    if (!itemStackContents.remove(itm))
                        return false;
                }

                if (!itemStackContents.isEmpty())
                    return false;

            } catch (ClassCastException ex) {
                return false;
            }
        }

        // If compareBundleInventory is on and version is above 1.17 also check Bundles
        if (TradeShop.getPlugin().getVersion().isAtLeast(1, 17) &&
                itemStack.getType().equals(Material.BUNDLE) &&
                getShopSetting(ShopItemStackSettingKeys.COMPARE_BUNDLE_INVENTORY).asBoolean()) {
            try {
                if (((BundleMeta) itemStackMeta).hasItems() != ((BundleMeta) toCompareMeta).hasItems()) return false;

                ArrayList<ItemStack> itemStackContents = Lists.newArrayList(((BundleMeta) toCompareMeta).getItems()),
                        toCompareContents = Lists.newArrayList(((BundleMeta) itemStackMeta).getItems());

                itemStackContents.removeIf(Objects::isNull);
                toCompareContents.removeIf(Objects::isNull);

                for (ItemStack itm : toCompareContents) {
                    if (!itemStackContents.remove(itm))
                        return false;
                }

                if (!itemStackContents.isEmpty())
                    return false;

            } catch (ClassCastException ex) {
                return false;
            }
        }

        // If compareDurability is on
        int compareDurability = getShopSetting(ShopItemStackSettingKeys.COMPARE_DURABILITY).asInteger();
        if (compareDurability > -1 && compareDurability < 3 && useMeta) {

            // Return False if Damageable is not equal (one has and one doesn't)
            if (itemStackMeta instanceof Damageable != toCompareMeta instanceof Damageable) {
                debugger.log("toCompareMeta isDamageable: " + (itemStackMeta instanceof Damageable), DebugLevels.ITEM_COMPARE);
                debugger.log("toCompareMeta isDamageable: " + (toCompareMeta instanceof Damageable), DebugLevels.ITEM_COMPARE);
                return false;
            }

            if (itemStackMeta instanceof Damageable) {

                Damageable itemStackDamageable = (Damageable) itemStackMeta, toCompareDamageable = (Damageable) toCompareMeta;

                // Return False compareDurability is set to '==' and ItemStack Damage is not equal
                if (compareDurability == 1 && itemStackDamageable.getDamage() != toCompareDamageable.getDamage()) {
                    debugger.log("itemstack Durability (==): " + itemStackDamageable.getDamage(), DebugLevels.ITEM_COMPARE);
                    debugger.log("toCompare Durability (==): " + toCompareDamageable.getDamage(), DebugLevels.ITEM_COMPARE);
                    return false;
                }

                // Return False compareDurability is set to '<=' and ItemStack Damage less than toCompare Damage
                if (compareDurability == 0 && itemStackDamageable.getDamage() > toCompareDamageable.getDamage()) {
                    debugger.log("itemstack Durability (<=): " + itemStackDamageable.getDamage(), DebugLevels.ITEM_COMPARE);
                    debugger.log("toCompare Durability (<=): " + toCompareDamageable.getDamage(), DebugLevels.ITEM_COMPARE);
                    return false;
                }

                // Return False compareDurability is set to '>=' and ItemStack Damage greater than toCompare Damage
                if (compareDurability == 2 && itemStackDamageable.getDamage() < toCompareDamageable.getDamage()) {
                    debugger.log("itemstack Durability (>=): " + itemStackDamageable.getDamage(), DebugLevels.ITEM_COMPARE);
                    debugger.log("toCompare Durability (>=): " + toCompareDamageable.getDamage(), DebugLevels.ITEM_COMPARE);
                    return false;
                }
            }

        }

        // If compareEnchantments is on
        if (getShopSetting(ShopItemStackSettingKeys.COMPARE_ENCHANTMENTS).asBoolean() && useMeta) {
            if (itemStackMeta instanceof EnchantmentStorageMeta && toCompareMeta instanceof EnchantmentStorageMeta) {
                EnchantmentStorageMeta itemStackEnchantmentStorageMeta = (EnchantmentStorageMeta) itemStackMeta,
                        toCompareEnchantmentStorageMeta = (EnchantmentStorageMeta) toCompareMeta;

                debugger.log("itemStackEnchantmentStorageMeta Enchants: " + itemStackEnchantmentStorageMeta.getStoredEnchants(), DebugLevels.ENCHANT_CHECKS);
                debugger.log("toCompareEnchantmentStorageMeta Enchants: " + toCompareEnchantmentStorageMeta.getStoredEnchants(), DebugLevels.ENCHANT_CHECKS);

                // Return False if hasEnchantments differs (one has one doesn't)
                if (itemStackEnchantmentStorageMeta.hasStoredEnchants() != toCompareEnchantmentStorageMeta.hasStoredEnchants())
                    return false;

                // Return False if itemStack hasEnchantments && Enchant maps are not equal
                if (itemStackEnchantmentStorageMeta.hasStoredEnchants() && !itemStackEnchantmentStorageMeta.getStoredEnchants().equals(toCompareEnchantmentStorageMeta.getStoredEnchants()))
                    return false;
            } else {

                // Return False if hasEnchantments differs (one has one doesn't)
                if (itemStackMeta.hasEnchants() != toCompareMeta.hasEnchants()) {
                    debugger.log("itemStackMeta hasEnchants: " + itemStackMeta.hasEnchants(), DebugLevels.ITEM_COMPARE);
                    debugger.log("toCompareMeta hasEnchants: " + toCompareMeta.hasEnchants(), DebugLevels.ITEM_COMPARE);
                    return false;
                }

                // Return False if itemStack hasEnchantments && Enchant maps are not equal
                if (itemStackMeta.hasEnchants() && !itemStackMeta.getEnchants().equals(toCompareMeta.getEnchants()))
                    return false;
            }
        }

        // If compareName is on
        if (getShopSetting(ShopItemStackSettingKeys.COMPARE_NAME).asBoolean()) {
            debugger.log("ShopItemStack > isSimilar > getDisplayName: " + itemStackMeta.getDisplayName() + " - " + toCompareMeta.getDisplayName(), DebugLevels.NAME_COMPARE);

            // If ItemStack Meta are BookMeta then compare title, otherwise compare item#displayName
            if (useBookMeta) {
                // Return False if hasTitle differs (one has one doesn't)
                if (itemStackBookMeta.hasTitle() != toCompareBookMeta.hasTitle()) return false;

                // Return False if itemStack hasTitle && Title is not equal
                if (itemStackBookMeta.hasTitle() && !Objects.equals(itemStackBookMeta.getTitle(), toCompareBookMeta.getTitle()))
                    return false;
            } else {
                // Return False if hasDisplayName differs (one has one doesn't)
                if (itemStackMeta.hasDisplayName() != toCompareMeta.hasDisplayName()) return false;

                // Return False if itemStack hasDisplayName && DisplayName is not equal
                if (itemStackMeta.hasDisplayName() && !itemStackMeta.getDisplayName().equals(toCompareMeta.getDisplayName()))
                    return false;
            }
        }

        // If useBookMeta and compareBookAuthor are true
        if (useBookMeta && getShopSetting(ShopItemStackSettingKeys.COMPARE_BOOK_AUTHOR).asBoolean()) {
            // Return False if hasAuthor differs (one has one doesn't)
            debugger.log("itemStackBookMeta hasAuthor: " + itemStackBookMeta.hasAuthor(), DebugLevels.ITEM_COMPARE);
            debugger.log("toCompareBookMeta hasAuthor: " + toCompareBookMeta.hasAuthor(), DebugLevels.ITEM_COMPARE);
            if (itemStackBookMeta.hasAuthor() != toCompareBookMeta.hasAuthor()) {
                return false;
            }

            // Return False if itemStack hasAuthor && Author is not equal
            debugger.log("itemStackBookMeta getAuthor: " + itemStackBookMeta.getAuthor(), DebugLevels.ITEM_COMPARE);
            debugger.log("toCompareBookMeta getAuthor: " + toCompareBookMeta.getAuthor(), DebugLevels.ITEM_COMPARE);
            if (itemStackBookMeta.hasAuthor() && !Objects.equals(itemStackBookMeta.getAuthor(), toCompareBookMeta.getAuthor())) {
                return false;
            }
        }

        // If useBookMeta and compareBookPages are true
        if (useBookMeta && getShopSetting(ShopItemStackSettingKeys.COMPARE_BOOK_PAGES).asBoolean()) {
            // Return False if hasPages differs (one has one doesn't)
            debugger.log("itemStackBookMeta hasPages: " + itemStackBookMeta.hasPages(), DebugLevels.ITEM_COMPARE);
            debugger.log("toCompareBookMeta hasPages: " + toCompareBookMeta.hasPages(), DebugLevels.ITEM_COMPARE);
            if (itemStackBookMeta.hasPages() != toCompareBookMeta.hasPages()) {
                return false;
            }

            // Return False if itemStack hasPages && Pages is not equal
            debugger.log("itemStackBookMeta isNull: " + itemStackBookMeta.getPages(), DebugLevels.ITEM_COMPARE);
            debugger.log("toCompareBookMeta isNull: " + toCompareBookMeta.getPages(), DebugLevels.ITEM_COMPARE);
            if (itemStackBookMeta.hasPages() && !Objects.equals(itemStackBookMeta.getPages(), toCompareBookMeta.getPages())) {
                return false;
            }
        }

        // If compareLore is on
        if (getShopSetting(ShopItemStackSettingKeys.COMPARE_LORE).asBoolean() && useMeta) {
            // Return False if hasLore differs (one has one doesn't)
            if (itemStackMeta.hasLore() != toCompareMeta.hasLore()) return false;

            // Return False if itemStack hasLore && Lore is not equal
            if (itemStackMeta.hasLore() && !Objects.equals(itemStackMeta.getLore(), toCompareMeta.getLore()))
                return false;
        }

        // If compareCustomModelData is on
        if (getShopSetting(ShopItemStackSettingKeys.COMPARE_CUSTOM_MODEL_DATA).asBoolean() && useMeta) {
            // Return False if hasCustomModelData differs (one has one doesn't)
            if (itemStackMeta.hasCustomModelData() != toCompareMeta.hasCustomModelData()) return false;

            // Return False if itemStack hasCustomModelData && Custom Model Data is not equal
            if (itemStackMeta.hasCustomModelData() && itemStackMeta.getCustomModelData() != toCompareMeta.getCustomModelData())
                return false;
        }

        // If compareItemFlags is on
        if (getShopSetting(ShopItemStackSettingKeys.COMPARE_ITEM_FLAGS).asBoolean() && useMeta) {
            // Return False if getItemFlags sizes differs
            if (itemStackMeta.getItemFlags().size() != toCompareMeta.getItemFlags().size()) return false;

            // Return False if Lore is not equal
            if (!itemStackMeta.getItemFlags().equals(toCompareMeta.getItemFlags())) return false;
        }

        // Return False if compareUnbreakable is on and isUnbreakable differs
        if (getShopSetting(ShopItemStackSettingKeys.COMPARE_UNBREAKABLE).asBoolean() && useMeta && itemStackMeta.isUnbreakable() != toCompareMeta.isUnbreakable())
            return false;

        // If item is firework rocket
        if (itemStack.getType() == Material.FIREWORK_ROCKET) {
            FireworkMeta fireworkMeta = (FireworkMeta) itemStackMeta;
            FireworkMeta toCompareFireworkMeta = (FireworkMeta) toCompareMeta;

            // If server compare firework duration is disabled local setting is ignores
            if (getShopSetting(ShopItemStackSettingKeys.COMPARE_FIREWORK_DURATION).asBoolean()) {
                if (fireworkMeta.getPower() != toCompareFireworkMeta.getPower()) {
                    return false;
                }
            }

            if (getShopSetting(ShopItemStackSettingKeys.COMPARE_FIREWORK_EFFECTS).asBoolean()) {
                if (fireworkMeta.hasEffects()) {
                    if (fireworkMeta.getEffects().size() != toCompareFireworkMeta.getEffects().size()) {
                        return false;
                    }

                    for (int i = 0; i < fireworkMeta.getEffects().size(); ++i) {
                        FireworkEffect effect = fireworkMeta.getEffects().get(i);
                        FireworkEffect effectCompare = toCompareFireworkMeta.getEffects().get(i);
                        if (!effect.equals(effectCompare)) {
                            return false;
                        }
                    }
                }
            }
        }

        // If compareAttributeModifier is on
        if (getShopSetting(ShopItemStackSettingKeys.COMPARE_ATTRIBUTE_MODIFIER).asBoolean() && useMeta) {
            if (itemStackMeta.hasAttributeModifiers() != toCompareMeta.hasAttributeModifiers()) return false;

            // Return False if itemStack hasAttributeModifiers && getAttributeModifiers are not equal
            return !itemStackMeta.hasAttributeModifiers() || Objects.equals(itemStackMeta.getAttributeModifiers(), toCompareMeta.getAttributeModifiers());
        }

        return true;
    }

    public ItemStack getItemStack() {
        if (itemStack == null)
            try {
                fixLoadedData();
            } catch (JsonSerializer.JsonSyntaxException e) {
                itemStack = null;
                itemSettings = null;
            }
        return itemStack;
    }

    public String getItemName() {
        return itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() ?
                itemStack.getItemMeta().getDisplayName() :
                itemStack.getType().toString();
    }

    static public String getCleanItemName(ItemStack itemStack) {
        BookMeta bookmeta = itemStack.hasItemMeta() && itemStack.getItemMeta() instanceof BookMeta ? ((BookMeta) itemStack.getItemMeta()) : null;

        if (bookmeta != null)
            return bookmeta.getTitle();

        return ChatColor.stripColor(itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() ?
                itemStack.getItemMeta().getDisplayName() :
                WordUtils.capitalizeFully(itemStack.getType().toString().replace("_", " ")));
    }

    public String getCleanItemName() {
        return ShopItemStack.getCleanItemName(itemStack);
    }

    public String getStateString(ShopItemStackSettingKeys key) {
        return getStateString(itemSettings.get(key));
    }

    public String getStateString(ObjectHolder<?> stateSetting) {
        try {
            String ret = "Unknown";
            if (stateSetting.isBoolean()) {
                ret = (Boolean) stateSetting.getObject() ? "True" : "False";
            } else if (stateSetting.isInteger()) {
                try {
                    ret = (new String[]{"<=", "==", ">="})[stateSetting.asInteger()];
                } catch (ArrayIndexOutOfBoundsException ignored) {
                    ret = "False";
                }
            }

            return "State: " + ret;
        } catch (ClassCastException ex) {
            ex.printStackTrace();
            return "State: ERROR";
        }
    }

    @Override
    public String toString() {
        return serialize().toString();
    }

    /**
     * Sets the objects {@link ItemStack} from its Base64.
     */
    private void fixLoadedData() throws JsonSerializer.JsonSyntaxException {
        if (itemStack == null) {
            if (hasBase64()) {
                try {
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(itemStackB64));
                    BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

                    // Read the serialized inventory
                    itemStack = new ItemStack((ItemStack) dataInput.readObject());

                    dataInput.close();
                } catch (ClassNotFoundException | IOException e) {
                    itemStack = null;
                }
            } else if (itemStackString != null && !itemStackString.isEmpty()) {
                itemStack = GsonProcessor.fromJsonToItemStack(itemStackString);
            }
        }

    }

    public String toConsoleText() {
        return serialize().toString();
    }
}

class ShopItemStackBuilder {
    private ItemStack itemStack = null;
    private Map<ShopItemStackSettingKeys, ObjectHolder<?>> itemSettings = null;
    private String itemStackB64 = "", itemStackString = "";

    ShopItemStackBuilder() {
    }

    ShopItemStackBuilder(ItemStack itm) {
        itemStack = itm;
    }

    ShopItemStackBuilder(String json) {
        itemStackString = json;
    }

    ShopItemStackBuilder setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        return this;
    }

    ShopItemStackBuilder setItemSettings(Map<ShopItemStackSettingKeys, ObjectHolder<?>> itemSettings) {
        this.itemSettings = itemSettings;
        return this;
    }

    ShopItemStackBuilder setItemStackB64(String itemStackB64) {
        this.itemStackB64 = itemStackB64;
        return this;
    }

    ShopItemStackBuilder setItemStackString(String itemStackString) {
        this.itemStackString = itemStackString;
        return this;
    }

    ShopItemStack build() {
        processLoadedData();

        return itemStack == null ? null : new ShopItemStack(itemStack, itemSettings);
    }

    /**
     * Verifies that the objects {@link ItemStack} is appropriately set.
     */
    private void processLoadedData() {
        processB64();
        processJSON();

        if (itemSettings == null) {
            itemSettings = ShopItemStackSettingKeys.getDefaultSettings();
        }
    }

    private void processB64() {
        if (itemStack == null && !itemStackB64.isEmpty()) {
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(itemStackB64));
                BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

                // Read the serialized inventory
                itemStack = new ItemStack((ItemStack) dataInput.readObject());

                dataInput.close();
            } catch (ClassNotFoundException | IOException e) {
                itemStack = null;
            }
        }
    }

    private void processJSON() {
        if (itemStack == null && itemStackString != null && !itemStackString.isEmpty()) {
            try {
                itemStack = GsonProcessor.fromJsonToItemStack(itemStackString);
            } catch (JsonSerializer.JsonSyntaxException e) {
                itemStack = null;
            }
        }
    }
}