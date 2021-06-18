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

package org.shanerx.tradeshop.objects;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.shanerx.tradeshop.enumys.DebugLevels;
import org.shanerx.tradeshop.enumys.Setting;
import org.shanerx.tradeshop.enumys.ShopItemStackSettingKeys;
import org.shanerx.tradeshop.utils.ObjectHolder;
import org.shanerx.tradeshop.utils.Utils;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class ShopItemStack implements Serializable, Cloneable {

    private transient ItemStack itemStack;
    private transient Debug debugger;

    private String itemStackB64;

    private HashMap<ShopItemStackSettingKeys, ObjectHolder<?>> shopSettings;

    public ShopItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        shopSettings = new HashMap<>();
        buildMap();
        toBase64();
    }

    public ShopItemStack(String itemStackB64) {
        this.itemStackB64 = itemStackB64;
        shopSettings = new HashMap<>();
        buildMap();
        fromBase64();
    }

    public ShopItemStack(String itemStackB64, HashMap<ShopItemStackSettingKeys, ObjectHolder<?>> settingMap) {
        this.itemStackB64 = itemStackB64;
        this.shopSettings = settingMap;
        buildMap();
        fromBase64();
    }

    //Re-added for backwards compatibility
    @Deprecated
    public ShopItemStack(String itemStackB64, int compareDurability, boolean compareEnchantments,
                         boolean compareName, boolean compareLore, boolean compareCustomModelData,
                         boolean compareItemFlags, boolean compareUnbreakable, boolean compareAttributeModifier,
                         boolean compareBookAuthor, boolean compareBookPages, boolean compareShulkerInventory) {
        this.itemStackB64 = itemStackB64;

        shopSettings = new HashMap<>();

        shopSettings.putIfAbsent(ShopItemStackSettingKeys.COMPARE_DURABILITY, new ObjectHolder<>(compareDurability));
        shopSettings.putIfAbsent(ShopItemStackSettingKeys.COMPARE_NAME, new ObjectHolder<>(compareName));
        shopSettings.putIfAbsent(ShopItemStackSettingKeys.COMPARE_LORE, new ObjectHolder<>(compareLore));
        shopSettings.putIfAbsent(ShopItemStackSettingKeys.COMPARE_CUSTOM_MODEL_DATA, new ObjectHolder<>(compareCustomModelData));
        shopSettings.putIfAbsent(ShopItemStackSettingKeys.COMPARE_ITEM_FLAGS, new ObjectHolder<>(compareItemFlags));
        shopSettings.putIfAbsent(ShopItemStackSettingKeys.COMPARE_UNBREAKABLE, new ObjectHolder<>(compareUnbreakable));
        shopSettings.putIfAbsent(ShopItemStackSettingKeys.COMPARE_ATTRIBUTE_MODIFIER, new ObjectHolder<>(compareAttributeModifier));
        shopSettings.putIfAbsent(ShopItemStackSettingKeys.COMPARE_BOOK_AUTHOR, new ObjectHolder<>(compareBookAuthor));
        shopSettings.putIfAbsent(ShopItemStackSettingKeys.COMPARE_BOOK_PAGES, new ObjectHolder<>(compareBookPages));
        shopSettings.putIfAbsent(ShopItemStackSettingKeys.COMPARE_SHULKER_INVENTORY, new ObjectHolder<>(compareShulkerInventory));
        shopSettings.putIfAbsent(ShopItemStackSettingKeys.COMPARE_ENCHANTMENTS, new ObjectHolder<>(compareEnchantments));

        buildMap();
        fromBase64();
    }

    public HashMap<ShopItemStackSettingKeys, ObjectHolder<?>> getShopSettings() {
        return shopSettings;
    }

    public static ShopItemStack deserialize(String serialized) {
        ShopItemStack item = new Gson().fromJson(serialized, ShopItemStack.class);
        item.fromBase64();
        item.buildMap();
        return item;
    }

    public boolean getShopSettingAsBoolean(ShopItemStackSettingKeys key) {
        try {
            ObjectHolder<?> tempObj = shopSettings.get(key);
            return shopSettings.containsKey(key) ? (Boolean) tempObj.getObject() : (Boolean) key.getDefaultValue().getObject();
        } catch (ClassCastException | NullPointerException e) {
            return (Boolean) key.getDefaultValue().getObject();
        }
    }

    public int getShopSettingAsInteger(ShopItemStackSettingKeys key) {
        try {
            ObjectHolder<?> tempObj = shopSettings.get(key);
            return shopSettings.containsKey(key) ? (Integer) tempObj.getObject() : (Integer) key.getDefaultValue().getObject();
        } catch (ClassCastException | NullPointerException e) {
            return (Integer) key.getDefaultValue().getObject();
        }
    }

    private void buildMap() {
        if (shopSettings == null) {
            shopSettings = new HashMap<>();
        }

        for (ShopItemStackSettingKeys key : ShopItemStackSettingKeys.values()) {
            shopSettings.putIfAbsent(key, key.getDefaultValue());
        }
    }

    public ShopItemStack clone() {
        return new ShopItemStack(itemStackB64, shopSettings);
    }

    public boolean setShopSettings(ShopItemStackSettingKeys key, ObjectHolder<?> value) {
        if (shopSettings == null) {
            shopSettings = new HashMap<>();
            buildMap();
        }

        shopSettings.put(key, value);
        return false;
    }

    public String getItemStackB64() {
        return itemStackB64;
    }

    public boolean hasBase64() {
        return itemStackB64 != null && !itemStackB64.isEmpty();
    }

    public boolean isSimilar(ItemStack toCompare) {
        debugger = new Utils().debugger;

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

        ItemMeta itemStackMeta = itemStack.getItemMeta(), toCompareMeta = toCompare.getItemMeta();
        BookMeta itemStackBookMeta = itemStack.hasItemMeta() && itemStack.getItemMeta() instanceof BookMeta ? ((BookMeta) itemStackMeta) : null,
                toCompareBookMeta = toCompare.hasItemMeta() && toCompare.getItemMeta() instanceof BookMeta ? ((BookMeta) toCompareMeta) : null;


        boolean useMeta = itemStack.hasItemMeta() == toCompare.hasItemMeta() && itemStackMeta != null, useBookMeta = itemStackBookMeta != null && toCompareBookMeta != null;
        boolean compareFireworkDuration = Setting.FIREWORK_COMPARE_DURATION.getBoolean();
        boolean compareFireworkEffects = Setting.FIREWORK_COMPARE_EFFECTS.getBoolean();

        debugger.log("itemstack useMeta: " + useMeta, DebugLevels.ITEM_COMPARE);
        debugger.log("toCompare useMeta: " + useMeta, DebugLevels.ITEM_COMPARE);

        // If compareShulkerInventory is on
        if (itemStack.getType().toString().endsWith("SHULKER_BOX")) {
            if (getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_SHULKER_INVENTORY)) {
                try {
                    ArrayList<ItemStack> contents1 = Lists.newArrayList(((ShulkerBox) ((BlockStateMeta) toCompareMeta).getBlockState()).getInventory().getContents()),
                            contents2 = Lists.newArrayList(((ShulkerBox) ((BlockStateMeta) itemStackMeta).getBlockState()).getInventory().getContents());

                    contents1.removeIf(Objects::isNull);
                    contents2.removeIf(Objects::isNull);

                    if (contents1.isEmpty() != contents2.isEmpty())
                        return false;

                    for (ItemStack itm : contents2) {
                        if (!contents1.remove(itm))
                            return false;
                    }

                    if (!contents1.isEmpty())
                        return false;

                } catch (ClassCastException ex) {
                    return false;
                }
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

        // If compareName is on
        if (getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_NAME) && useMeta) {

            // If ItemStack Meta are BookMeta then compare title, otherwise compare displayname
            if (useBookMeta) {
                // Return False if hasTitle differs (one has one doesn't)
                if (itemStackBookMeta.hasTitle() != toCompareBookMeta.hasTitle()) return false;

                // Return False if itemStack hasTitle && Title is not equal
                if (itemStackBookMeta.hasTitle() && !itemStackBookMeta.getTitle().equals(toCompareBookMeta.getTitle()))
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
            if (getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_FIREWORK_DURATION) && compareFireworkDuration) {
                if (fireworkMeta.getPower() != toCompareFireworkMeta.getPower()) {
                    return false;
                }
            }

            if (getShopSettingAsBoolean(ShopItemStackSettingKeys.COMPARE_FIREWORK_EFFECTS) && compareFireworkEffects) {
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
            fromBase64();
        return itemStack;
    }

    public String getItemName() {
        return itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() ?
                itemStack.getItemMeta().getDisplayName() :
                itemStack.getType().toString();
    }

    public String serialize() {
        return new Gson().toJson(this);
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
    private void toBase64() {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Save every element in the list
            dataOutput.writeObject(itemStack);

            // Serialize that array
            dataOutput.close();
            itemStackB64 = Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (IOException e) {
            itemStackB64 = null;
        }
    }

    /**
     * Sets the objects {@link ItemStack} from its Base64.
     */
    private void fromBase64() {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(itemStackB64));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            // Read the serialized inventory
            itemStack = (ItemStack) dataInput.readObject();

            dataInput.close();
        } catch (ClassNotFoundException | IOException e) {
            itemStack = null;
        }
    }

    public String toConsoleText() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }

}