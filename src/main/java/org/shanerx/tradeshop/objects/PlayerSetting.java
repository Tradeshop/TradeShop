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

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import org.shanerx.tradeshop.enumys.Setting;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerSetting implements Serializable {

    private transient UUID uuid;
    private String uuidString;

    private int type = 0, multi = Setting.MULTI_TRADE_DEFAULT.getInt();

    private Set<String> ownedShops, staffShops;

    public PlayerSetting(UUID playerUUID, Map<String, Integer> data) {
        this.uuid = playerUUID;
        this.uuidString = uuid.toString();

        if (data.containsKey("type")) type = data.get("type");
        if (data.containsKey("multi")) multi = data.get("multi");

        ownedShops = Sets.newHashSet();
        staffShops = Sets.newHashSet();

        load();
    }

    public PlayerSetting(UUID playerUUID) {
        this.uuid = playerUUID;
        this.uuidString = uuid.toString();

        ownedShops = Sets.newHashSet();
        staffShops = Sets.newHashSet();

        load();
    }

    public static PlayerSetting deserialize(String serialized) {
        PlayerSetting playerSetting = new Gson().fromJson(serialized, PlayerSetting.class);
        playerSetting.load();
        return playerSetting;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getMulti() {
        return multi;
    }

    public void setMulti(int multi) {
        this.multi = multi;
    }

    public Set<String> getOwnedShops() {
        return ownedShops;
    }

    public void addShop(Shop shop) {
        if (shop.getOwner().getUUID().equals(uuid) &&
                !ownedShops.contains(shop.getShopLocationAsSL().serialize()))
            ownedShops.add(shop.getShopLocationAsSL().serialize());
        else if (shop.getUsersUUID().contains(uuid) &&
                !ownedShops.contains(shop.getShopLocationAsSL().serialize()))
            staffShops.add(shop.getShopLocationAsSL().serialize());
    }

    public void removeShop(Shop shop) {
        ownedShops.remove(shop.getShopLocationAsSL().serialize());
        staffShops.remove(shop.getShopLocationAsSL().serialize());
    }

    public void updateShops(Shop shop) {
        if (!shop.getUsersUUID().contains(uuid))
            removeShop(shop);
        else
            addShop(shop);

    }

    public UUID getUuid() {
        return uuid;
    }

    public Set<String> getStaffShops() {
        return staffShops;
    }

    public void load() {
        if (uuid == null) uuid = UUID.fromString(uuidString);
        if (multi > Setting.MULTI_TRADE_MAX.getInt()) multi = Setting.MULTI_TRADE_MAX.getInt();
    }

    public String serialize() {
        return new Gson().toJson(this);
    }
}

