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

package org.shanerx.tradeshop.player;

import com.google.gson.annotations.SerializedName;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.data.config.Message;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.data.storage.DataStorage;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shop.ShopChest;
import org.shanerx.tradeshop.shop.ShopType;
import org.shanerx.tradeshop.shoplocation.ShopLocation;
import org.shanerx.tradeshop.utils.gsonprocessing.GsonProcessor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
public class ShopUser implements Serializable {

    @SerializedName("player")
    private final String playerUUID;
    private transient OfflinePlayer player;
    private ShopRole role;

    public ShopUser(OfflinePlayer player, ShopRole role) {
        this.player = player;
        playerUUID = player.getUniqueId().toString();
        this.role = role;
    }

    public ShopUser(UUID pUUID, ShopRole role) {
        this.player = Bukkit.getOfflinePlayer(pUUID);
        playerUUID = player.getUniqueId().toString();
        this.role = role;
    }

    public static ShopUser deserialize(String serialized) {
        ShopUser shopUser = new GsonProcessor().fromJson(serialized, ShopUser.class);
        shopUser.player = Bukkit.getOfflinePlayer(UUID.fromString(shopUser.playerUUID));
        return shopUser;
    }

    /**
     * Returns the Shop the player is looking at
     *
     * @return null if Shop is not found, Shop object if it is
     */
    public static Shop findObservedShop(Player observer) {
        if (observer == null) {
            return null;
        }

        TradeShop plugin = TradeShop.getPlugin();

        Block b = observer.getTargetBlockExact(Setting.MAX_EDIT_DISTANCE.getInt());
        try {
            if (b == null)
                throw new NoSuchFieldException();

            if (ShopType.isShop(b)) {
                return Shop.loadShop((Sign) b.getState());

            } else if (ShopChest.isShopChest(b)) {
                if (plugin.getDataStorage().getChestLinkage(new ShopLocation(b.getLocation())) != null)
                    return plugin.getDataStorage().loadShopFromStorage(new ShopLocation(b.getLocation()));

                return Shop.loadShop(new ShopChest(b.getLocation()).getShopSign());

            } else
                throw new NoSuchFieldException();

        } catch (NoSuchFieldException ex) {
            Message.NO_SIGHTED_SHOP.sendMessage(observer);
            return null;
        }
    }

    /**
     * Returns the Sign the player is looking at
     *
     * @return null if Sign is not found, Sign object if it is
     */
    public static Sign findObservedSign(Player observer) {
        Block b = observer.getTargetBlockExact(Setting.MAX_EDIT_DISTANCE.getInt());
        try {
            if (b == null)
                throw new NoSuchFieldException();

            if (TradeShop.getPlugin().getSigns().getSignTypes().contains(b.getType())) {
                return (Sign) b.getState();

            } else
                throw new NoSuchFieldException();

        } catch (NoSuchFieldException ex) {
            Message.NO_SIGN_FOUND.sendMessage(observer);
            return null;
        }
    }

    /**
     * Returns a list of shops that contain the specified items within the specified range
     *
     * @param center         Location to search from
     * @param range          Integer range from center location. If a chunk has a single block within this range, the whole chunk is checked.
     * @param desiredCost    ShopItemStack List containing Cost(s) to search for. Will only find shops that match *ALL* costs in the list. Set null to not check
     * @param desiredProduct ShopItemStack List containing Product(s) to search for. Will only find shops that match *ALL* costs in the list. Set null to not check
     * @return Shop List containing all shops in any chunk with a block in the range that match criteria specified
     */
    public static List<Shop> findProximityShop(Location center, int range, List<ItemStack> desiredCost, List<ItemStack> desiredProduct) {
        List<Chunk> chunksInRange = new ArrayList<>(); //Used to prevent checking a chunk more than once
        List<Shop> foundShops = new ArrayList<>();
        DataStorage dataStorage = TradeShop.getPlugin().getDataStorage();
        World world = center.getWorld();

        for (int x = center.getBlockX() - range; x <= center.getBlockX() + range; x++) {
            for (int z = center.getBlockZ() - range; z <= center.getBlockZ() + range; z++) {

                if (!chunksInRange.contains(world.getChunkAt(x, z)) && //If not already processed
                        ((int) Math.sqrt(Math.pow(x - center.getBlockX(), 2) + Math.pow(z - center.getBlockZ(), 2))) <= range) { //If within range
                    Chunk c = world.getChunkAt(x, z);
                    chunksInRange.add(c); //"Mark" as processed

                    foundShops.addAll(dataStorage.getMatchingShopsInChunk(c, desiredCost, desiredProduct));

                }
            }
        }
        return foundShops;
    }

    public OfflinePlayer getPlayer() {
        fix();
        return player;
    }

    public UUID getUUID() {
        return getPlayer().getUniqueId();
    }

    public ShopRole getRole() {
        return role;
    }

    public void setRole(ShopRole newRole) {
        role = newRole;
    }

    public String getName() {
        return getPlayer().getName();
    }

    private void fix() {
        if (player == null && playerUUID != null && !playerUUID.equalsIgnoreCase("")) {
            player = Bukkit.getOfflinePlayer(UUID.fromString(playerUUID));
        }
    }

    public String serialize() {
        return new GsonProcessor().toJson(this);
    }

    public ItemStack getHead() {
        ItemStack userHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta userMeta = (SkullMeta) userHead.getItemMeta();
        userMeta.setOwningPlayer(Bukkit.getOfflinePlayer(getUUID()));
        userHead.setItemMeta(userMeta);

        return userHead;
    }
}