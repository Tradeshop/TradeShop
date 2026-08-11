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

import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.utils.objects.ObjectHolder;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public enum ShopItemStackSettingKeys {

    //New per shop settings and their default value should be added below and will be added to the shops

    COMPARE_DURABILITY(new ItemStack(Material.DAMAGED_ANVIL), 1), // -1 = 'off', 0 = '<=', 1 = '==', 2 = '>='
    COMPARE_ENCHANTMENTS(new ItemStack(Material.ENCHANTED_BOOK), true),
    COMPARE_NAME(new ItemStack(Material.NAME_TAG), true),
    COMPARE_LORE(new ItemStack(Material.BOOK), true),
    COMPARE_CUSTOM_MODEL_DATA(new ItemStack(Material.STICK), true),
    COMPARE_ITEM_FLAGS(new ItemStack(Material.WHITE_BANNER), true),
    COMPARE_UNBREAKABLE(new ItemStack(Material.BEDROCK), true),
    COMPARE_ATTRIBUTE_MODIFIER(new ItemStack(Material.BARRIER), true),
    COMPARE_BOOK_AUTHOR(new ItemStack(Material.PLAYER_HEAD), true),
    COMPARE_BOOK_PAGES(new ItemStack(Material.PAPER), true),
    COMPARE_SHULKER_INVENTORY(new ItemStack(Material.CHEST_MINECART), true),
    COMPARE_BUNDLE_INVENTORY(new ItemStack(Material.CHEST_MINECART), true),
    COMPARE_FIREWORK_DURATION(new ItemStack(Material.GUNPOWDER), true),
    COMPARE_FIREWORK_EFFECTS(new ItemStack(Material.FIREWORK_STAR), true),
    COMPARE_POTION_EFFECTS(new ItemStack(Material.BREWING_STAND), true);

    private final ItemStack displayItem;
    private final Object preConfigDefault;
    private static final String defaultKey = "default", userEditableKey = "user-editable";

    ShopItemStackSettingKeys(ItemStack displayItem, Object preConfigDefault) {
        this.displayItem = displayItem;
        this.preConfigDefault = preConfigDefault;
    }

    public static Map<String, Object> getDefaultConfigMap() {
        Map<String, Object> configMap = new HashMap<>();

        Stream.of(values()).forEach(key -> configMap.put(key.getKey(String.class), key.getSubConfigMap()));


        return configMap;
    }

    public static Map<ShopItemStackSettingKeys, ObjectHolder<?>> getDefaultSettings() {
        Map<ShopItemStackSettingKeys, ObjectHolder<?>> defaultMap = new HashMap<>();

        Stream.of(values()).forEach(key -> defaultMap.put(key.getKey(ShopItemStackSettingKeys.class), key.getDefaultValue()));

        return defaultMap;
    }

    private Map<String, Object> getSubConfigMap() {
        Map<String, Object> subConfigMap = new HashMap<>();
        subConfigMap.put(defaultKey, preConfigDefault);
        subConfigMap.put(userEditableKey, true);
        return subConfigMap;
    }

    private <T> T getKey(Type T) {
        if (T.equals(ShopItemStackSettingKeys.class)) {
            return (T) this;
        } else if (T.equals(String.class)) {
            return (T) getConfigName();
        }
        return null;
    }

    public String makeReadable() {
        return WordUtils.capitalizeFully(name().replace("_", " "));

    }

    /**
     * The server-wide default for this setting.
     *
     * <p>Falls back to the value compiled into this enum when the config file has no
     * answer. A hole in {@code shop-per-item-settings} used to produce a holder with
     * nothing in it, and every caller of this method reads it as a boolean or an int:
     * the boolean path threw and the int path handed back a null that was unboxed. The
     * fallback is the same value the file would have held, so a hole now costs nothing
     * beyond the operator's own override of it.
     */
    public ObjectHolder<?> getDefaultValue() {
        Object configured = Setting.SHOP_PER_ITEM_SETTINGS.getMappedObject(getConfigName() + "." + defaultKey);
        return new ObjectHolder<>(configured != null ? configured : preConfigDefault);
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }

    /**
     * Whether a shop owner may override this setting on an item.
     *
     * <p>Falls back to editable when the config file has no answer, for the same
     * reason {@link #getDefaultValue()} falls back to the value compiled into this
     * enum - and with more at stake, because {@code false} here is not an absent
     * answer, it is a decision. {@code user-editable: false} is how an operator pins
     * a comparison server-wide, and {@code ShopItemStack.java:287} enforces it by
     * refusing to read the item's own value at all. Reading a hole as {@code false},
     * which is what {@code getBoolean} does for a key that is not there, therefore
     * enacted the opposite of the default: every per-item override on the server
     * stopped being consulted, shops went back to comparing things their owners had
     * switched off, and nothing was logged.
     */
    public boolean isUserEditable() {
        Object configured = Setting.SHOP_PER_ITEM_SETTINGS.getMappedObject(getConfigName() + "." + userEditableKey);
        return configured == null || new ObjectHolder<>(configured).asBoolean();
    }

    public String getConfigName() {
        return name().toLowerCase().replace("_", "-");
    }

    public static ShopItemStackSettingKeys match(String name) {
        return valueOf(name.toUpperCase().replace("-", "_"));
    }
}
