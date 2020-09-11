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

import com.google.gson.Gson;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

public class ShopItemStack implements Serializable {

    private transient ItemStack itemStack;

    private String itemStackB64;
    private int compareDurability; // -1 == 'off', 0 == '<=', 1 == '==', 2 == '>='
    private boolean compareEnchantments, compareName, compareLore, compareCustomModelData, compareItemFlags, compareUnbreakable, compareAttributeModifier;

    public ShopItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;

        itemStackB64 = toBase64(itemStack);

        compareDurability = 1;
        compareEnchantments = true;
        compareName = true;
        compareLore = true;
        compareCustomModelData = true;
        compareItemFlags = true;
        compareUnbreakable = true;
        compareAttributeModifier = true;

    }

    public ShopItemStack(String itemStackB64) {
        this.itemStackB64 = itemStackB64;

        itemStack = fromBase64(itemStackB64);

        compareDurability = 1;
        compareEnchantments = true;
        compareName = true;
        compareLore = true;
        compareCustomModelData = true;
        compareItemFlags = true;
        compareUnbreakable = true;
        compareAttributeModifier = true;
    }

    public boolean isSimilar(ItemStack toCompare) {

        // Return False if ItemStacks are different MaterialTypes
        if (!itemStack.getType().equals(toCompare.getType())) return false;

        // Return False if hasItemMeta differs (one has one doesn't)
        if (itemStack.hasItemMeta() != toCompare.hasItemMeta()) return false;

        ItemMeta itemStackMeta = itemStack.getItemMeta(), toCompareMeta = toCompare.getItemMeta();

        // Return True if both items don't has MetaData
        if (itemStackMeta != null && toCompareMeta != null) {

            // If compareDurability is on
            if (compareDurability > -1 && compareDurability < 3) {
                // Return False if Damageable is not equal (one has and one doesn't)
                if (itemStackMeta instanceof Damageable != toCompareMeta instanceof Damageable) return false;

                if (itemStackMeta instanceof Damageable) {

                    Damageable itemStackDamageable = (Damageable) itemStackMeta, toCompareDamageable = (Damageable) toCompareMeta;

                    // Return False compareDurability is set to '==' and ItemStack Damage is not equal
                    if (compareDurability == 1 && itemStackDamageable.getDamage() != toCompareDamageable.getDamage())
                        return false;

                    // Return False compareDurability is set to '<=' and ItemStack Damage less than toCompare Damage
                    if (compareDurability == 0 && itemStackDamageable.getDamage() > toCompareDamageable.getDamage())
                        return false;

                    // Return False compareDurability is set to '>=' and ItemStack Damage greater than toCompare Damage
                    if (compareDurability == 2 && itemStackDamageable.getDamage() < toCompareDamageable.getDamage())
                        return false;
                }

            }

            // If compareEnchantments is on
            if (compareEnchantments) {
                // Return False if hasEnchantments differs (one has one doesn't)
                if (itemStackMeta.hasEnchants() != toCompareMeta.hasEnchants()) return false;

                // Return False if Enchant maps are not equal
                if (!itemStackMeta.getEnchants().equals(toCompareMeta.getEnchants())) return false;
            }

            // If compareName is on
            if (compareName) {
                // Return False if hasDisplayName differs (one has one doesn't)
                if (itemStackMeta.hasDisplayName() != toCompareMeta.hasDisplayName()) return false;

                // Return False if Display Names are not equal
                if (!itemStackMeta.getDisplayName().equals(toCompareMeta.getDisplayName())) return false;
            }

            // If compareLore is on
            if (compareLore) {
                // Return False if hasLore differs (one has one doesn't)
                if (itemStackMeta.hasLore() != toCompareMeta.hasLore()) return false;

                // Return False if Lore is not equal
                if (!Objects.equals(itemStackMeta.getLore(), toCompareMeta.getLore())) return false;
            }

            // If compareCustomModelData is on
            if (compareCustomModelData) {
                // Return False if hasCustomModelData differs (one has one doesn't)
                if (itemStackMeta.hasCustomModelData() != toCompareMeta.hasCustomModelData()) return false;

                // Return False if Custom Model Data is not equal
                if (itemStackMeta.getCustomModelData() != toCompareMeta.getCustomModelData()) return false;
            }

            // If compareItemFlags is on
            if (compareItemFlags) {
                // Return False if getItemFlags sizes differs
                if (itemStackMeta.getItemFlags().size() != toCompareMeta.getItemFlags().size()) return false;

                // Return False if Lore is not equal
                if (!itemStackMeta.getItemFlags().equals(toCompareMeta.getItemFlags())) return false;
            }

            // Return False if compareUnbreakable is on and isUnbreakable differs
            if (compareUnbreakable && itemStackMeta.isUnbreakable() != toCompareMeta.isUnbreakable()) return false;

            // If compareAttributeModifier is on
            if (compareAttributeModifier) {
                if (itemStackMeta.hasAttributeModifiers() != toCompareMeta.hasAttributeModifiers()) return false;

                return Objects.equals(itemStackMeta.getAttributeModifiers(), toCompareMeta.getAttributeModifiers());
            }

        }
        return true;
    }

    public int isCompareDurability() {
        return compareDurability;
    }

    public void setCompareDurability(int compareDurability) {
        this.compareDurability = compareDurability;
    }

    public boolean isCompareEnchantments() {
        return compareEnchantments;
    }

    public void setCompareEnchantments(boolean compareEnchantments) {
        this.compareEnchantments = compareEnchantments;
    }

    public boolean isCompareName() {
        return compareName;
    }

    public void setCompareName(boolean compareName) {
        this.compareName = compareName;
    }

    public boolean isCompareLore() {
        return compareLore;
    }

    public void setCompareLore(boolean compareLore) {
        this.compareLore = compareLore;
    }

    public boolean isCompareCustomModelData() {
        return compareCustomModelData;
    }

    public void setCompareCustomModelData(boolean compareCustomModelData) {
        this.compareCustomModelData = compareCustomModelData;
    }

    public boolean isCompareItemFlags() {
        return compareItemFlags;
    }

    public void setCompareItemFlags(boolean compareItemFlags) {
        this.compareItemFlags = compareItemFlags;
    }

    public boolean isCompareUnbreakable() {
        return compareUnbreakable;
    }

    public void setCompareUnbreakable(boolean compareUnbreakable) {
        this.compareUnbreakable = compareUnbreakable;
    }

    public boolean isCompareAttributeModifier() {
        return compareAttributeModifier;
    }

    public void setCompareAttributeModifier(boolean compareAttributeModifier) {
        this.compareAttributeModifier = compareAttributeModifier;
    }

    public String getItemStackB64() {
        return itemStackB64;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public static ShopItemStack deserialize(String serialized) {
        return new Gson().fromJson(serialized, ShopItemStack.class);
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
     * A method to serialize an {@link ItemStack} array to Base64 String.
     *
     * @param item to turn into a Base64 String.
     * @return Base64 string of the items.
     * @throws IllegalStateException if ItemStack cannot be saved
     */
    private String toBase64(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Save every element in the list
            dataOutput.writeObject(item);

            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets an array of ItemStacks from Base64 string.
     *
     * @param data Base64 string to convert to ItemStack array.
     * @return ItemStack array created from the Base64 string.
     * @throws IOException if class type could not be decoded
     */
    private ItemStack fromBase64(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item;

            // Read the serialized inventory
            item = (ItemStack) dataInput.readObject();

            dataInput.close();
            return item;
        } catch (ClassNotFoundException | IOException e) {
            return null;
        }
    }
}