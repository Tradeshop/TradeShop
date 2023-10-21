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

package org.shanerx.tradeshop.utils.objects;

import com.google.gson.annotations.SerializedName;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ObjectHolder<Type> {

    @SerializedName(value = "value", alternate = "obj")
    private final Type obj;

    public ObjectHolder(Type obj) {
        this.obj = obj;
    }

    public Type getObject() {
        return obj;
    }

    public boolean isNull() {
        return obj == null;
    }

    public boolean isBoolean() {
        return obj != null && obj instanceof Boolean;
    }

    /**
     * Converts string to boolean based on acceptable responses
     *
     * @return true if acceptable string was found
     */
    public boolean canBeBoolean() {
        switch (obj.toString().toLowerCase()) {
            case "true":
            case "t":
            case "tru":
            case "yes":
            case "y":
            case "all":
                return true;
            default:
                return false;
        }
    }

    public boolean isInteger() {
        return obj != null && (obj instanceof Integer || (obj instanceof Double && Double.parseDouble(obj.toString()) % 1 == 0));
    }

    public boolean canBeInteger() {
        try {
            Integer.parseInt(obj.toString());
            return true;
        } catch (NumberFormatException | NullPointerException ignored) {
        }

        return false;
    }


    public boolean isDouble() {
        return obj != null && (obj instanceof Double || obj instanceof Integer);
    }

    public boolean isString() {
        return obj != null && obj instanceof String;
    }

    public Boolean asBoolean() {
        return canBeBoolean();
    }

    public Integer asInteger() {
        return isInteger() ? Integer.valueOf((int) Double.parseDouble(obj.toString())) : canBeInteger() ? Integer.parseInt(obj.toString()) : null;
    }

    public Double asDouble() {
        return isDouble() ? Double.parseDouble(obj.toString()) : null;
    }

    public boolean isMap() {
        return obj != null && obj instanceof Map;
    }

    public Map<String, Object> asMap() {
        if (isMap()) {
            try {
                return (Map<String, Object>) obj;
            } catch (ClassCastException ignored) {
            }
        }
        return null;
    }

    public boolean isList() {
        return !isNull() && obj instanceof List;
    }

    public Optional<List<String>> asStringList() {
        Optional<List<String>> ret = Optional.empty();
        if (isList()) {
            try {
                ret = Optional.of((List<String>) obj);
            } catch (ClassCastException ignored) {
            }
        }
        return ret;
    }

    public boolean canBeMaterial() {
        return asMaterial() != null;
    }

    public Material asMaterial() {
        return Material.matchMaterial(obj.toString());
    }

    @Override
    public String toString() {
        return obj.toString();
    }
}
