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

package org.shanerx.tradeshop.utils.configuration;

import org.shanerx.tradeshop.utils.configuration.interfaces.YAMLConfigurationSectionInterface;

public enum MessageSectionKey implements YAMLConfigurationSectionInterface {

    NONE(10, "", ""),
    UNUSED(100, "", "");

    private final String key, sectionHeader, valueLead, postComment, preComment;
    private final int weight; // Lower Weights are printed first
    private final MessageSectionKey parent;

    MessageSectionKey(int weight, String key, String sectionHeader) {
        this(weight, null, key, sectionHeader, "", "");
    }

    MessageSectionKey(int weight, String key, String sectionHeader, String preComment, String postComment) {
        this(weight, null, key, sectionHeader, preComment, postComment);
    }

    MessageSectionKey(int weight, MessageSectionKey parent, String key, String sectionHeader) {
        this(weight, parent, key, sectionHeader, "", "");
    }

    MessageSectionKey(int weight, MessageSectionKey parent, String key, String sectionHeader, String preComment, String postComment) {
        this.weight = weight;
        this.key = key;
        this.sectionHeader = sectionHeader;
        this.parent = parent;
        this.postComment = !postComment.isEmpty() ? postComment : "";
        this.preComment = !preComment.isEmpty() ? preComment : "";
        this.valueLead = !key.isEmpty() ? parent != null ? parent.valueLead + "  " : "  " : "";
    }

    @Override
    public String getKey() {
        return !key.isEmpty() ? key : "";
    }

    @Override
    public String getPath() {
        return !key.isEmpty() ? (hasParent() ? parent.getPath() + "." + key : key) : "";
    }

    @Override
    public String getFullLead() {
        return (hasParent() ? parent.getFullLead() : "") + valueLead;
    }

    @Override
    public String getFileText() {
        return hasParent() ? parent.valueLead + key : key;
    }

    @Override
    public MessageSectionKey getParent() {
        return parent;
    }

    @Override
    public boolean hasParent() {
        return parent != null;
    }

    @Override
    public String getPostComment() {
        return postComment;
    }

    @Override
    public String getPreComment() {
        return preComment;
    }

    @Override
    public String getSectionHeader() {
        return sectionHeader;
    }

    @Override
    public int getWeight() {
        return weight;
    }
}