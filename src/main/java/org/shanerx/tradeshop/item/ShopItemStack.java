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

package org.shanerx.tradeshop.item;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
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
import org.shanerx.tradeshop.utils.Utils;
import org.shanerx.tradeshop.utils.debug.Debug;
import org.shanerx.tradeshop.utils.debug.DebugLevels;
import org.shanerx.tradeshop.utils.objects.ObjectHolder;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ShopItemStack implements Serializable, Cloneable {

    private transient ItemStack itemStack;
    private transient Debug debugger;

    @Expose(serialize = false)
    private String itemStackB64;

    @Expose
    private Map<String, Object> serialItemStack;

    @Expose
    @SerializedName(value = "itemSettings", alternate = "shopSettings")
    private Map<ShopItemStackSettingKeys, ObjectHolder<?>> itemSettings;

    public ShopItemStack(ItemStack itemStack) {
        this(itemStack, new HashMap<>());
    }

    public ShopItemStack(ItemStack itemStack, HashMap<ShopItemStackSettingKeys, ObjectHolder<?>> itemSettings) {
        this.itemStack = itemStack;
        this.itemSettings = itemSettings;
        buildMap();
    }

    public ShopItemStack(String itemStackB64) {
        this(itemStackB64, new HashMap<>());
    }

    public ShopItemStack(String itemStackB64, HashMap<String, ObjectHolder<?>> itemSettings) {
        this.itemStackB64 = itemStackB64;
        this.itemSettings = new HashMap<>();

        itemSettings.forEach((key, value) -> this.itemSettings.put(ShopItemStackSettingKeys.match(key), value));

        buildMap();
        loadData();
    }

    public ShopItemStack(Map<String, Object> serialItemStack) {
        this(serialItemStack, new HashMap<>());
    }

    public ShopItemStack(Map<String, Object> serialItemStack, HashMap<String, ObjectHolder<?>> itemSettings) {
        this.serialItemStack = serialItemStack;
        this.itemSettings = new HashMap<>();

        itemSettings.forEach((key, value) -> this.itemSettings.put(ShopItemStackSettingKeys.match(key), value));

        buildMap();
        loadData();
    }

    //Re-added for backwards compatibility
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
        loadData();
    }

    public Map<ShopItemStackSettingKeys, ObjectHolder<?>> getItemSettings() {
        return itemSettings;
    }

    public String serialize() {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(this);
    }

    public static ShopItemStack deserialize(String serialized) {
        ShopItemStack item = new Gson().fromJson(serialized, ShopItemStack.class);
        item.loadData();
        item.buildMap();
        return item;
    }

    public boolean getShopSettingAsBoolean(ShopItemStackSettingKeys key) {
        if (key.isUserEditable()) {
            try {
                ObjectHolder<?> tempObj = itemSettings.get(key);
                return itemSettings.containsKey(key) ? (Boolean) tempObj.getObject() : (Boolean) key.getDefaultValue().getObject();
            } catch (ClassCastException | NullPointerException ignored) {
            }
        }
        return (Boolean) key.getDefaultValue().getObject();
    }

    public int getShopSettingAsInteger(ShopItemStackSettingKeys key) {
        if (key.isUserEditable()) {
            try {
                ObjectHolder<?> tempObj = itemSettings.get(key);
                return itemSettings.containsKey(key) ? (Integer) tempObj.getObject() : (Integer) key.getDefaultValue().getObject();
            } catch (ClassCastException | NullPointerException ignored) {
            }
        }

        return (Integer) key.getDefaultValue().getObject();
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

    public boolean setShopSettings(ShopItemStackSettingKeys key, ObjectHolder<?> value) {
        if (itemSettings == null) {
            itemSettings = new HashMap<>();
            buildMap();
        }

        itemSettings.put(key, value);
        return false;
    }

    public int getAmount() {
        if (itemStack == null) //TODO this fixes an NPE from this method when itemstack is null(idk why itemstack would be null, this fixes for now so hopefully it will be enough)
            return 0;
        return itemStack.getAmount();
    }

    public void setAmount(int amount) {
        itemStack.setAmount(amount);
        unloadData();
    }

    public String getItemStackB64() {
        return itemStackB64;
    }

    public Map<String, Object> getSerialItemStack() {
        if (serialItemStack == null)
            unloadData();
        return serialItemStack;
    }


    public boolean hasBase64() {
        return itemStackB64 != null && !itemStackB64.isEmpty();
    }

    public boolean isSimilar(ItemStack toCompare) {
        debugger = new Utils().PLUGIN.getDebugger();

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
                getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_SHULKER_INVENTORY)) {
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
        if (new Utils().PLUGIN.getVersion().isAtLeast(1, 17) &&
                itemStack.getType().equals(Material.BUNDLE) &&
                getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_BUNDLE_INVENTORY)) {
            try {
                ArrayList<ItemStack> itemStackContents = Lists.newArrayList(((BundleMeta) toCompareMeta).getItems()),
                        toCompareContents = Lists.newArrayList(((BundleMeta) itemStackMeta).getItems());

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

        // If compareDurability is on
        int compareDurability = getShopSettingAsInteger(ShopItemStackSettingKeys.COMPARE_DURABILITY);
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
                    debugger.log("itemstack Durabilty (==): " + itemStackDamageable.getDamage(), DebugLevels.ITEM_COMPARE);
                    debugger.log("toCompare Durabilty (==): " + toCompareDamageable.getDamage(), DebugLevels.ITEM_COMPARE);
                    return false;
                }

                // Return False compareDurability is set to '<=' and ItemStack Damage less than toCompare Damage
                if (compareDurability == 0 && itemStackDamageable.getDamage() > toCompareDamageable.getDamage()) {
                    debugger.log("itemstack Durabilty (<=): " + itemStackDamageable.getDamage(), DebugLevels.ITEM_COMPARE);
                    debugger.log("toCompare Durabilty (<=): " + toCompareDamageable.getDamage(), DebugLevels.ITEM_COMPARE);
                    return false;
                }

                // Return False compareDurability is set to '>=' and ItemStack Damage greater than toCompare Damage
                if (compareDurability == 2 && itemStackDamageable.getDamage() < toCompareDamageable.getDamage()) {
                    debugger.log("itemstack Durabilty (>=): " + itemStackDamageable.getDamage(), DebugLevels.ITEM_COMPARE);
                    debugger.log("toCompare Durabilty (>=): " + toCompareDamageable.getDamage(), DebugLevels.ITEM_COMPARE);
                    return false;
                }
            }

        }

        // If compareEnchantments is on
        if (getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_ENCHANTMENTS) && useMeta) {
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
        if (getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_NAME)) {
            debugger.log("ShopItemStack > isSimilar > getDisplayName: " + itemStackMeta.getDisplayName() + " - " + toCompareMeta.getDisplayName(), DebugLevels.NAME_COMPARE);

            // If ItemStack Meta are BookMeta then compare title, otherwise compare displayname
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
        if (useBookMeta && getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_BOOK_AUTHOR)) {
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
        if (useBookMeta && getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_BOOK_PAGES)) {
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
        if (getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_LORE) && useMeta) {
            // Return False if hasLore differs (one has one doesn't)
            if (itemStackMeta.hasLore() != toCompareMeta.hasLore()) return false;

            // Return False if itemStack hasLore && Lore is not equal
            if (itemStackMeta.hasLore() && !Objects.equals(itemStackMeta.getLore(), toCompareMeta.getLore()))
                return false;
        }

        // If compareCustomModelData is on
        if (getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_CUSTOM_MODEL_DATA) && useMeta) {
            // Return False if hasCustomModelData differs (one has one doesn't)
            if (itemStackMeta.hasCustomModelData() != toCompareMeta.hasCustomModelData()) return false;

            // Return False if itemStack hasCustomModelData && Custom Model Data is not equal
            if (itemStackMeta.hasCustomModelData() && itemStackMeta.getCustomModelData() != toCompareMeta.getCustomModelData())
                return false;
        }

        // If compareItemFlags is on
        if (getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_ITEM_FLAGS) && useMeta) {
            // Return False if getItemFlags sizes differs
            if (itemStackMeta.getItemFlags().size() != toCompareMeta.getItemFlags().size()) return false;

            // Return False if Lore is not equal
            if (!itemStackMeta.getItemFlags().equals(toCompareMeta.getItemFlags())) return false;
        }

        // Return False if compareUnbreakable is on and isUnbreakable differs
        if (getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_UNBREAKABLE) && useMeta && itemStackMeta.isUnbreakable() != toCompareMeta.isUnbreakable())
            return false;

        // If item is firework rocket
        if (itemStack.getType() == Material.FIREWORK_ROCKET) {
            FireworkMeta fireworkMeta = (FireworkMeta) itemStackMeta;
            FireworkMeta toCompareFireworkMeta = (FireworkMeta) toCompareMeta;

            // If server compare firework duration is disabled local setting is ignores
            if (getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_FIREWORK_DURATION)) {
                if (fireworkMeta.getPower() != toCompareFireworkMeta.getPower()) {
                    return false;
                }
            }

            if (getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_FIREWORK_EFFECTS)) {
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
        if (getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_ATTRIBUTE_MODIFIER) && useMeta) {
            if (itemStackMeta.hasAttributeModifiers() != toCompareMeta.hasAttributeModifiers()) return false;

            // Return False if itemStack hasAttributeModifiers && getAttributeModifiers are not equal
            return !itemStackMeta.hasAttributeModifiers() || Objects.equals(itemStackMeta.getAttributeModifiers(), toCompareMeta.getAttributeModifiers());
        }

        return true;
    }

    public ItemStack getItemStack() {
        if (itemStack == null)
            loadData();
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
            String ret = "";
            if (stateSetting.isBoolean()) {
                if ((Boolean) stateSetting.getObject()) {
                    ret = "True";
                } else {
                    ret = "False";
                }
            } else if (stateSetting.isInteger() || stateSetting.isDouble()) {
                switch (stateSetting.isDouble() ? ((Double) stateSetting.getObject()).intValue() : (Integer) stateSetting.getObject()) {
                    case 2:
                        ret = ">=";
                        break;
                    case 1:
                        ret = "==";
                        break;
                    case 0:
                        ret = "<=";
                        break;
                    case -1:
                    default:
                        ret = "False";
                }
            }

            if (ret.length() < 2)
                ret = "Unknown";

            return "State: " + ret;
        } catch (ClassCastException ex) {
            return "State: ERROR";
        }
    }

    @Override
    public String toString() {
        return "ShopItemStack{" +
                "itemStack=" + getItemStack().serialize() +
                ", shopSettings=" + itemSettings +
                '}';
    }

    /**
     *
     * Original code from https://gist.github.com/graywolf336/8153678
     * Tweaked for use with single itemstacks
     *
     */

    /**
     * Sets the objects Base64 from its {@link ItemStack}
     */
    private void unloadData() {
        if (itemStack != null) {
            itemStackB64 = null;
            serialItemStack = itemStack.serialize();
        }
    }

    /**
     * Sets the objects {@link ItemStack} from its Base64 or Serialized values.
     */
    private void loadData() {
        if (serialItemStack != null && !serialItemStack.isEmpty()) {
            itemStack = ItemStack.deserialize(serialItemStack);
        }

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
        }

        unloadData();
    }

    public int getItemSize() {
        unloadData();
        return serialItemStack.toString().length();
    }

    public String toConsoleText() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
}