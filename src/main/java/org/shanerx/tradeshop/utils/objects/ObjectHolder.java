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

public class ObjectHolder<Type> {

    @SerializedName(value = "value", alternate = "obj")
    private final Type obj;

    public ObjectHolder(Type obj) {
        this.obj = obj;
    }

    public Type getObject() {
        return obj;
    }

    public boolean isBoolean() {
        return obj != null && obj instanceof Boolean;
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
        return isBoolean() ? Boolean.parseBoolean(obj.toString()) : null;
    }

    public Integer asInteger() {
        return isInteger() ? Integer.valueOf((int) Double.parseDouble(obj.toString())) : canBeInteger() ? Integer.parseInt(obj.toString()) : null;
    }

    public Double asDouble() {
        return isDouble() ? Double.parseDouble(obj.toString()) : null;
    }

    @Override
    public String toString() {
        return obj.toString();
    }
}
