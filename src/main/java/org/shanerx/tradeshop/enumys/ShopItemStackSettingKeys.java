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

package org.shanerx.tradeshop.enumys;

import org.shanerx.tradeshop.utils.ObjectHolder;

public enum ShopItemStackSettingKeys {

    //New per shop settings and their default value should be added below and will be added to the shops

    COMPARE_DURABILITY(new ObjectHolder<>(1)), // -1 == 'off', 0 == '<=', 1 == '==', 2 == '>='
    COMPARE_ENCHANTMENTS(new ObjectHolder<>(true)),
    COMPARE_NAME(new ObjectHolder<>(true)),
    COMPARE_LORE(new ObjectHolder<>(true)),
    COMPARE_CUSTOM_MODEL_DATA(new ObjectHolder<>(true)),
    COMPARE_ITEM_FLAGS(new ObjectHolder<>(true)),
    COMPARE_UNBREAKABLE(new ObjectHolder<>(true)),
    COMPARE_ATTRIBUTE_MODIFIER(new ObjectHolder<>(true)),
    COMPARE_BOOK_AUTHOR(new ObjectHolder<>(true)),
    COMPARE_BOOK_PAGES(new ObjectHolder<>(true)),
    COMPARE_SHULKER_INVENTORY(new ObjectHolder<>(true)),
    COMPARE_FIREWORK_DURATION(new ObjectHolder<>(true)),
    COMPARE_FIREWORK_EFFECTS(new ObjectHolder<>(true));

    private final ObjectHolder<?> defaultValue;

    ShopItemStackSettingKeys(ObjectHolder<?> defaultValue) {
        this.defaultValue = defaultValue;
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
}
