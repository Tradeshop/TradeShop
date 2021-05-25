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

    compareDurability(new ObjectHolder<>(1)), // -1 == 'off', 0 == '<=', 1 == '==', 2 == '>='
    compareEnchantments(new ObjectHolder<>(true)),
    compareName(new ObjectHolder<>(true)),
    compareLore(new ObjectHolder<>(true)),
    compareCustomModelData(new ObjectHolder<>(true)),
    compareItemFlags(new ObjectHolder<>(true)),
    compareUnbreakable(new ObjectHolder<>(true)),
    compareAttributeModifier(new ObjectHolder<>(true)),
    compareBookAuthor(new ObjectHolder<>(true)),
    compareBookPages(new ObjectHolder<>(true)),
    compareShulkerInventory(new ObjectHolder<>(true)),
    compareFireworkDuration(new ObjectHolder<>(true)),
    compareFireworkEffects(new ObjectHolder<>(true));

    private final ObjectHolder<?> defaultValue;

    ShopItemStackSettingKeys(ObjectHolder<?> defaultValue) {
        this.defaultValue = defaultValue;
    }

    public ObjectHolder<?> getDefaultValue() {
        return defaultValue;
    }
}
