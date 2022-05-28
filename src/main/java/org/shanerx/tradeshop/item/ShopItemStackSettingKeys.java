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

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.utils.objects.ObjectHolder;

public enum ShopItemStackSettingKeys {

    //New per shop settings and their default value should be added below and will be added to the shops

    COMPARE_DURABILITY(new ObjectHolder<>(Setting.COMPARE_DURABILITY_DEFAULT.getInt()), new ItemStack(Material.DAMAGED_ANVIL)), // -1 == 'off', 0 == '<=', 1 == '==', 2 == '>='
    COMPARE_ENCHANTMENTS(new ObjectHolder<>(Setting.COMPARE_ENCHANTMENTS_DEFAULT.getBoolean()), new ItemStack(Material.ENCHANTED_BOOK)),
    COMPARE_NAME(new ObjectHolder<>(Setting.COMPARE_NAME_DEFAULT.getBoolean()), new ItemStack(Material.NAME_TAG)),
    COMPARE_LORE(new ObjectHolder<>(Setting.COMPARE_LORE_DEFAULT.getBoolean()), new ItemStack(Material.BOOK)),
    COMPARE_CUSTOM_MODEL_DATA(new ObjectHolder<>(Setting.COMPARE_CUSTOM_MODEL_DATA_DEFAULT.getBoolean()), new ItemStack(Material.STICK)),
    COMPARE_ITEM_FLAGS(new ObjectHolder<>(Setting.COMPARE_ITEM_FLAGS_DEFAULT.getBoolean()), new ItemStack(Material.WHITE_BANNER)),
    COMPARE_UNBREAKABLE(new ObjectHolder<>(Setting.COMPARE_UNBREAKABLE_DEFAULT.getBoolean()), new ItemStack(Material.BEDROCK)),
    COMPARE_ATTRIBUTE_MODIFIER(new ObjectHolder<>(Setting.COMPARE_ATTRIBUTE_MODIFIER_DEFAULT.getBoolean()), new ItemStack(Material.BARRIER)),
    COMPARE_BOOK_AUTHOR(new ObjectHolder<>(Setting.COMPARE_BOOK_AUTHOR_DEFAULT.getBoolean()), new ItemStack(Material.PLAYER_HEAD)),
    COMPARE_BOOK_PAGES(new ObjectHolder<>(Setting.COMPARE_BOOK_PAGES_DEFAULT.getBoolean()), new ItemStack(Material.PAPER)),
    COMPARE_SHULKER_INVENTORY(new ObjectHolder<>(Setting.COMPARE_SHULKER_INVENTORY_DEFAULT.getBoolean()), new ItemStack(Material.CHEST_MINECART)),
    COMPARE_BUNDLE_INVENTORY(new ObjectHolder<>(Setting.COMPARE_BUNDLE_INVENTORY_DEFAULT.getBoolean()), new ItemStack(Material.CHEST_MINECART)),
    COMPARE_FIREWORK_DURATION(new ObjectHolder<>(Setting.COMPARE_FIREWORK_DURATION_DEFAULT.getBoolean()), new ItemStack(Material.GUNPOWDER)),
    COMPARE_FIREWORK_EFFECTS(new ObjectHolder<>(Setting.COMPARE_FIREWORK_EFFECTS_DEFAULT.getBoolean()), new ItemStack(Material.FIREWORK_STAR));

    private final ObjectHolder<?> defaultValue;
    private final boolean userEditable;
    private final ItemStack displayItem;

    ShopItemStackSettingKeys(ObjectHolder<?> defaultValue, ItemStack displayItem) {
        this.defaultValue = defaultValue;
        this.userEditable = Setting.findSetting(this.name() + "_USER_EDITABLE").getBoolean();
        this.displayItem = displayItem;
    }

    public ObjectHolder<?> getDefaultValue() {
        return defaultValue;
    }

    public String makeReadable() {
        StringBuilder ret = new StringBuilder();

        //Replaces '_' with ' ' followed by a Capital letter, other letters are lower cased.
        for (int i = 0; i < name().length(); i++) {
            char ch = name().charAt(i);

            if (i == 0)
                ret.append(Character.toUpperCase(ch));
            else if (ret.charAt(i - 1) == ' ')
                ret.append(Character.toUpperCase(ch));
            else if (ch == '_')
                ret.append(" ");
            else
                ret.append(Character.toLowerCase(ch));
        }

        //removes any Leading/Trailing spaces and returns
        return ret.toString().trim();

    }

    public boolean isUserEditable() {
        return userEditable;
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }
}
