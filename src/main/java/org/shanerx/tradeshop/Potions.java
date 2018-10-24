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

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

@SuppressWarnings("unused")
public enum Potions {

    P_EMPTY("uncraftable", "potion", false, false),
    P_WATER("water", "potion", false, false),
    P_MUNDANE("mundane", "potion", false, false),
    P_THICK("thick", "potion", false, false),
    P_AWKWARD("awkward", "potion", false, false),
    P_NIGHT("night_vision", "potion", false, false),
    P_NIGHT_T("night_vision", "potion", true, false),
    P_INVIS("invisibility", "potion", false, false),
    P_INVIS_T("invisibility", "potion", true, false),
    P_LEAP("jump", "potion", false, false),
    P_LEAP_T("jump", "potion", true, false),
    P_LEAP_I("jump", "potion", false, true),
    P_FIRERES("fire_resistance", "potion", false, false),
    P_FIRERES_T("fire_resistance", "potion", true, false),
    P_SWIFT("speed", "potion", false, false),
    P_SWIFT_T("speed", "potion", true, false),
    P_SWIFT_I("speed", "potion", false, true),
    P_SLOW("slowness", "potion", false, false),
    P_SLOW_T("slowness", "potion", true, false),
    P_BREATH("water_breathing", "potion", false, false),
    P_BREATH_T("water_breathing", "potion", true, false),
    P_HEALTH("instant_heal", "potion", false, false),
    P_HEALTH_I("instant_heal", "potion", false, true),
    P_HARM("instant_damage", "potion", false, false),
    P_HARM_I("instant_damage", "potion", false, true),
    P_POISON("poison", "potion", false, false),
    P_POISON_T("poison", "potion", true, false),
    P_POISON_I("poison", "potion", false, true),
    P_REGEN("regen", "potion", false, false),
    P_REGEN_T("regen", "potion", true, false),
    P_REGEN_I("regen", "potion", false, true),
    P_STRENGTH("strength", "potion", false, false),
    P_STRENGTH_T("strength", "potion", true, false),
    P_STRENGTH_I("strength", "potion", false, true),
    P_WEAK("weakness", "potion", false, false),
    P_WEAK_T("weakness", "potion", true, false),
    P_LUCK("luck", "potion", false, false),

    S_EMPTY("uncraftable", "splash_potion", false, false),
    S_WATER("water", "splash_potion", false, false),
    S_MUNDANE("mundane", "splash_potion", false, false),
    S_THICK("thick", "splash_potion", false, false),
    S_AWKWARD("awkward", "splash_potion", false, false),
    S_NIGHT("night_vision", "splash_potion", false, false),
    S_NIGHT_T("night_vision", "splash_potion", true, false),
    S_INVIS("invisibility", "splash_potion", false, false),
    S_INVIS_T("invisibility", "splash_potion", true, false),
    S_LEAP("jump", "splash_potion", false, false),
    S_LEAP_T("jump", "splash_potion", true, false),
    S_LEAP_I("jump", "splash_potion", false, true),
    S_FIRERES("fire_resistance", "splash_potion", false, false),
    S_FIRERES_T("fire_resistance", "splash_potion", true, false),
    S_SWIFT("speed", "splash_potion", false, false),
    S_SWIFT_T("speed", "splash_potion", true, false),
    S_SWIFT_I("speed", "splash_potion", false, true),
    S_SLOW("slowness", "splash_potion", false, false),
    S_SLOW_T("slowness", "splash_potion", true, false),
    S_BREATH("water_breathing", "splash_potion", false, false),
    S_BREATH_T("water_breathing", "splash_potion", true, false),
    S_HEALTH("instant_heal", "splash_potion", false, false),
    S_HEALTH_I("instant_heal", "splash_potion", false, true),
    S_HARM("instant_damage", "splash_potion", false, false),
    S_HARM_I("instant_damage", "splash_potion", false, true),
    S_POISON("poison", "splash_potion", false, false),
    S_POISON_T("poison", "splash_potion", true, false),
    S_POISON_I("poison", "splash_potion", false, true),
    S_REGEN("regen", "splash_potion", false, false),
    S_REGEN_T("regen", "splash_potion", true, false),
    S_REGEN_I("regen", "splash_potion", false, true),
    S_STRENGTH("strength", "splash_potion", false, false),
    S_STRENGTH_T("strength", "splash_potion", true, false),
    S_STRENGTH_I("strength", "splash_potion", false, true),
    S_WEAK("weakness", "splash_potion", false, false),
    S_WEAK_T("weakness", "splash_potion", true, false),
    S_LUCK("luck", "splash_potion", false, false),

    L_EMPTY("uncraftable", "lingering_potion", false, false),
    L_WATER("water", "lingering_potion", false, false),
    L_MUNDANE("mundane", "lingering_potion", false, false),
    L_THICK("thick", "lingering_potion", false, false),
    L_AWKWARD("awkward", "lingering_potion", false, false),
    L_NIGHT("night_vision", "lingering_potion", false, false),
    L_NIGHT_T("night_vision", "lingering_potion", true, false),
    L_INVIS("invisibility", "lingering_potion", false, false),
    L_INVIS_T("invisibility", "lingering_potion", true, false),
    L_LEAP("jump", "lingering_potion", false, false),
    L_LEAP_T("jump", "lingering_potion", true, false),
    L_LEAP_I("jump", "lingering_potion", false, true),
    L_FIRERES("fire_resistance", "lingering_potion", false, false),
    L_FIRERES_T("fire_resistance", "lingering_potion", true, false),
    L_SWIFT("speed", "lingering_potion", false, false),
    L_SWIFT_T("speed", "lingering_potion", true, false),
    L_SWIFT_I("speed", "lingering_potion", false, true),
    L_SLOW("slowness", "lingering_potion", false, false),
    L_SLOW_T("slowness", "lingering_potion", true, false),
    L_BREATH("water_breathing", "lingering_potion", false, false),
    L_BREATH_T("water_breathing", "lingering_potion", true, false),
    L_HEALTH("instant_heal", "lingering_potion", false, false),
    L_HEALTH_I("instant_heal", "lingering_potion", false, true),
    L_HARM("instant_damage", "lingering_potion", false, false),
    L_HARM_I("instant_damage", "lingering_potion", false, true),
    L_POISON("poison", "lingering_potion", false, false),
    L_POISON_T("poison", "lingering_potion", true, false),
    L_POISON_I("poison", "lingering_potion", false, true),
    L_REGEN("regen", "lingering_potion", false, false),
    L_REGEN_T("regen", "lingering_potion", true, false),
    L_REGEN_I("regen", "lingering_potion", false, true),
    L_STRENGTH("strength", "lingering_potion", false, false),
    L_STRENGTH_T("strength", "lingering_potion", true, false),
    L_STRENGTH_I("strength", "lingering_potion", false, true),
    L_WEAK("weakness", "lingering_potion", false, false),
    L_WEAK_T("weakness", "lingering_potion", true, false),
    L_LUCK("luck", "lingering_potion", false, false);

    private String potionType, type;
    private boolean extended, amplified;

    Potions(String p, String t, boolean e, boolean a) {
        potionType = p;
        type = t;
        extended = e;
        amplified = a;
    }

    public static String findPotion(ItemStack itm) {
        if (itm.getType() != Material.POTION && itm.getType() != Material.SPLASH_POTION && itm.getType() != Material.LINGERING_POTION) {
            return null;
        }

        PotionData pd = ((PotionMeta) itm.getItemMeta()).getBasePotionData();
        String pT = "", t = "", mod = "", pot;

        if (itm.getType() == Material.POTION) {
            t = "P_";
        } else if (itm.getType() == Material.SPLASH_POTION) {
            t = "S_";
        } else if (itm.getType() == Material.LINGERING_POTION) {
            t = "L_";
        }

        if (pd.isExtended()) {
            mod = "_T";
        } else if (pd.isUpgraded()) {
            mod = "_I";
        }

        switch (pd.getType().toString().toLowerCase()) {
            case "uncraftable":
                pT = "EMPTY";
                break;
            case "water":
                pT = "WATER";
                break;
            case "thick":
                pT = "THICK";
                break;
            case "awkward":
                pT = "AWKWARD";
                break;
            case "night_vision":
                pT = "NIGHT";
                break;
            case "invisibility":
                pT = "INVIS";
                break;
            case "jump":
                pT = "LEAP";
                break;
            case "speed":
                pT = "SWIFT";
                break;
            case "slowness":
                pT = "SLOW";
                break;
            case "water_breathing":
                pT = "BREATH";
                break;
            case "mundane":
                pT = "MUNDANE";
                break;
            case "fire_resistance":
                pT = "FIRERES";
                break;
            case "instant_heal":
                pT = "HEALTH";
                break;
            case "instant_damage":
                pT = "HARM";
                break;
            case "poison":
                pT = "POISON";
                break;
            case "regen":
                pT = "REGEN";
                break;
            case "strength":
                pT = "STRENGTH";
                break;
            case "weakness":
                pT = "WEAK";
                break;
            case "luck":
                pT = "LUCK";
                break;
        }

        pot = t + pT + mod;

        return pot;
    }

    public static Potions getPotion(String str) {
        if (isType(str)) {
            return valueOf(str.toUpperCase());
        } else {
            return null;
        }
    }

    public static Potions getPotion(ItemStack itm) {
        String str = findPotion(itm);

        return getPotion(str);
    }

    public static boolean isPotion(ItemStack itm) {
        String str = findPotion(itm);
        return !(str == null || str.equalsIgnoreCase(""));
    }

    public static boolean isType(String type) {
        if (type == null) {
            return false;
        }

        try {
            valueOf(type.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public PotionData getPotionData() {
        return new PotionData(PotionType.valueOf(potionType.toUpperCase()), extended, amplified);
    }

    public Material getType() {
        return Material.valueOf(type.toUpperCase());
    }

    public ItemStack getItem() {
        ItemStack itm = new ItemStack(getType());
        PotionMeta pm = (PotionMeta) itm.getItemMeta();
        pm.setBasePotionData(getPotionData());
        itm.setItemMeta(pm);

        return itm;
    }

    public String toString() {
        return name();
    }

}
