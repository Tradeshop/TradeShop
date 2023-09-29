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

package org.shanerx.tradeshop.shop;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.utils.objects.ObjectHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ShopSettingKeys {

    // Use null as default if the shop type does not use this setting

    HOPPER_IMPORT(new ItemStack(Material.HOPPER), false, false, null),
    HOPPER_EXPORT(new ItemStack(Material.DISPENSER), false, false, null),
    NO_COST(new ItemStack(Material.GOLD_INGOT), false, null, false);

    public static final String defaultKey = "default", userEditableKey = "user-editable";
    private final ItemStack displayItem;
    private final Map<ShopType, Object> defaults;

    ShopSettingKeys(ItemStack displayItem, Object tradeDefault, Object biTradeDefault, Object iTradeDefault) {
        this.displayItem = displayItem;
        defaults = new HashMap<>();
        defaults.put(ShopType.TRADE, tradeDefault);
        defaults.put(ShopType.BITRADE, biTradeDefault);
        defaults.put(ShopType.ITRADE, iTradeDefault);
    }

    public static Map<String, Object> getSettingConfigMap(ShopType type) {
        Map<String, Object> configMap = new HashMap<>();

        for (ShopSettingKeys value : getValidSettings(type)) {
            Map<String, Object> subConfigMap = new HashMap<>();
            subConfigMap.put(defaultKey, value.defaults.get(type));
            subConfigMap.put(userEditableKey, true);

            configMap.put(value.getConfigKey(), subConfigMap);
        }
        return configMap;
    }

    public static List<ShopSettingKeys> getValidSettings(ShopType type) {
        List<ShopSettingKeys> settings = new ArrayList<>();

        Arrays.stream(values()).forEach((setting) -> {
            if (setting.defaults.get(type) != null) settings.add(setting);
        });

        return settings;
    }

    public static ShopSettingKeys findShopSetting(String search) {
        return valueOf(search.toUpperCase().replace("-", "_"));
    }

    public static Setting settingExpand(ShopType type) {
        return Setting.findSetting(type.name() + "_PER_SHOP_SETTINGS");
    }

    public String makeReadable() {
        return WordUtils.capitalizeFully(name().replace("_", " "));

    }

    public ObjectHolder<?> getDefaultValue(ShopType type) {
        return isUsable(type) ? new ObjectHolder<>(settingExpand(type).getMappedObject(getConfigKey() + "." + defaultKey)) : null;
    }

    public ObjectHolder<?> getDefault(ShopType type) {
        return isUsable(type) ? new ObjectHolder<>(defaults.get(type)) : null;
    }

    public boolean isUserEditable(ShopType type) {
        return isUsable(type) && settingExpand(type).getMappedBoolean(getConfigKey() + "." + userEditableKey);
    }

    public boolean isUsable(ShopType type) {
        return defaults.get(type) != null;
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }

    public String getConfigKey() {
        return name().toLowerCase().replace("_", "-");
    }
}
