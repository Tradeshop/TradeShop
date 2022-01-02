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

import org.bukkit.Material;
import org.shanerx.tradeshop.enumys.DebugLevels;
import org.shanerx.tradeshop.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IllegalItemList extends Utils {

    private ListType type;
    private ArrayList<Material> list;

    public IllegalItemList(ListType type, ArrayList<Material> list) {
        this.type = type;
        this.list = list;
    }

    public ArrayList<Material> getList() {
        return list;
    }

    public void setList(ArrayList<Material> list) {
        this.list = list;
    }

    public ListType getType() {
        return type;
    }

    public void setType(ListType newType) {
        this.type = newType;
    }

    public void setType(String newType) {
        setType(ListType.valueOf(newType));
    }

    public boolean isIllegal(Material mat) {
        switch (type) {
            case WHITELIST:
                return !list.contains(mat);
            case BLACKLIST:
                return list.contains(mat);
            case DISABLED:
            default:
                return false;
        }
    }

    public void clear() {
        list.clear();
    }

    public boolean add(Material mat) {
        debugger.log("Material being added to Illegal Item List: " + mat, DebugLevels.ILLEGAL_ITEMS_LIST);
        return list.add(mat);
    }

    public boolean remove(Material mat) {
        return list.remove(mat);
    }

    public void addColourSet(String mat) {
        List<String> colourSet = Arrays.asList("BLACK", "RED", "GREEN", "BROWN", "BLUE", "PURPLE", "LIGHT_BLUE", "LIGHT_GRAY", "GRAY", "PINK", "LIME", "YELLOW", "CYAN", "MAGENTA", "ORANGE", "WHITE");

        add(Material.matchMaterial(mat));
        for (String colour : colourSet) {
            add(Material.matchMaterial(colour + "_" + mat));
        }

    }

    public void checkAndAdd(String check) {
        Material mat = null;
        if (!(check.startsWith("$") && check.endsWith("^"))) {
            mat = Material.matchMaterial(check);
        }

        if (mat != null) {
            add(mat);
        } else {
            switch (check.toLowerCase()) {
                case "$banner^":
                    addColourSet("BANNER");
                    addColourSet("WALL_BANNER");
                    break;
                case "$candle^":
                    addColourSet("CANDLE");
                    break;
                case "$carpet^":
                    add(Material.MOSS_CARPET);
                    addColourSet("CARPET");
                    break;
                case "$concrete^":
                    addColourSet("CONCRETE");
                    break;
                case "$concrete_powder^":
                    addColourSet("CONCRETE_POWDER");
                    break;
                case "$dye^":
                    addColourSet("DYE");
                    break;
                case "$glazed_terracotta^":
                    addColourSet("GLAZED_TERRACOTTA");
                    break;
                case "$shulker_box^":
                    addColourSet("SHULKER_BOX");
                    break;
                case "$stained_glass^":
                    addColourSet("STAINED_GLASS");
                    break;
                case "$stained_glass_pane^":
                    addColourSet("STAINED_GLASS_PANE");
                    break;
                case "$terracotta^":
                    addColourSet("TERRACOTTA");
                    break;
                case "$wool^":
                    addColourSet("WOOL");
                    break;
            }
        }
    }

    public enum ListType {

        BLACKLIST,
        WHITELIST,
        DISABLED

    }

    public enum TradeItemType {

        COST,
        PRODUCT

    }
}
