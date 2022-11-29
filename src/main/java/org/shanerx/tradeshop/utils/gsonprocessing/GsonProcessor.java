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

package org.shanerx.tradeshop.utils.gsonprocessing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.shanerx.tradeshop.utils.gsonprocessing.typeadapters.ConfigurationSerializableAdapter;

import java.lang.reflect.Type;

public class GsonProcessor {
    private final Gson globalGson;

    public GsonProcessor(boolean doPrettyPrinting) {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .disableHtmlEscaping()
                .serializeNulls()
                .enableComplexMapKeySerialization()
                .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new ConfigurationSerializableAdapter());

        if (doPrettyPrinting) gsonBuilder.setPrettyPrinting();

        globalGson = gsonBuilder.create();
    }

    public GsonProcessor() {
        this(true);
    }

    public <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {
        return globalGson.fromJson(json, (Type) classOfT);
    }

    public <T> T fromJson(JsonElement json, Type typeOfT) throws JsonSyntaxException {
        return globalGson.fromJson(json, typeOfT);
    }

    public String toJson(Object src) {
        return globalGson.toJson(src, src.getClass());
    }

    public JsonElement toJsonTree(Object src) {
        return globalGson.toJsonTree(src, src.getClass());
    }

    public Gson getGlobalGson() {
        return globalGson;
    }
}
