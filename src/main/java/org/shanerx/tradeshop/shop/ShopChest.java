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

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.item.ShopItemStack;
import org.shanerx.tradeshop.shoplocation.ShopLocation;
import org.shanerx.tradeshop.utils.Utils;
import org.shanerx.tradeshop.utils.debug.DebugLevels;

import java.util.List;
import java.util.UUID;

public class ShopChest {

    private final static TradeShop PLUGIN = TradeShop.getPlugin();
    private final Location loc;
    private ShopLocation shopSign;
    private Block chest;
    private UUID owner;

    public ShopChest(Location chestLoc) {
        this.loc = chestLoc;
        ShopLocation signLoc = PLUGIN.getVarManager().getDataStorage().getChestLinkage(new ShopLocation(chestLoc));

        if (signLoc != null && Shop.loadShop(signLoc) != null) {
            this.shopSign = signLoc;
            this.owner = Shop.loadShop(signLoc).getOwner().getUUID();
        }

        getBlock();
    }

    public ShopChest(Block chest, UUID owner, Location sign) {
        this.loc = chest.getLocation();
        this.owner = owner;
        this.shopSign = new ShopLocation(sign);
        this.chest = chest;
    }

    public static boolean isShopChest(Block checking) {
        ShopLocation linked = PLUGIN.getVarManager().getDataStorage().getChestLinkage(new ShopLocation(checking.getLocation()));

        if (linked != null) return true;

        try {
            if (isDoubleChest(checking)) {
                DoubleChest dbl = getDoubleChest(checking);

                return ((Container) dbl.getLeftSide()).getPersistentDataContainer().has(PLUGIN.getVarManager().getSignKey(), PersistentDataType.STRING) ||
                        ((Container) dbl.getRightSide()).getPersistentDataContainer().has(PLUGIN.getVarManager().getSignKey(), PersistentDataType.STRING);
            }
            return ((Container) checking.getState()).getPersistentDataContainer().has(PLUGIN.getVarManager().getSignKey(), PersistentDataType.STRING);
        } catch (NullPointerException | ClassCastException ex) {
            PLUGIN.getVarManager().getDebugger().log("Error thrown during isShopChest by: \n" + ex, DebugLevels.PROTECTION);
        }
        return false;
    }

    public static boolean isShopChest(Inventory checking) {
        try {
            return isShopChest(checking.getLocation().getBlock());
        } catch (NullPointerException ignored) {
        }
        return false;
    }

    /**
     * The block that makes up the other half of {@code check}'s double chest.
     *
     * <p>A double chest's combined inventory sits on the boundary between its two
     * blocks, so exactly one of its coordinates ends in .5 and that coordinate is the
     * axis the pair runs along. Whichever half is being asked about, the answer is the
     * other end of that axis.
     *
     * <p>The Z branch used to compare {@code check.getX()} against the Z of the
     * inventory - an X against a Z. The two agree only where the chest happens to sit
     * on the diagonal, so off it one of the two halves was told its other half was
     * itself, and which one depended on the coordinates.
     */
    public static Block getOtherHalfOfDoubleChest(Block check) {
        Block otherChest = null;
        if (check.getState() instanceof Chest) {
            Chest chest = (Chest) check.getState();
            Location chestLoc = chest.getInventory().getLocation(), otherChestLoc = chest.getInventory().getLocation();
            if (chestLoc.getX() - Math.floor(chestLoc.getX()) > 0) {
                otherChestLoc.setX(check.getX() == Math.floor(chestLoc.getX()) ? Math.ceil(chestLoc.getX()) : Math.floor(chestLoc.getX()));
            } else if (chestLoc.getZ() - Math.floor(chestLoc.getZ()) > 0) {
                otherChestLoc.setZ(check.getZ() == Math.floor(chestLoc.getZ()) ? Math.ceil(chestLoc.getZ()) : Math.floor(chestLoc.getZ()));
            }
            otherChest = otherChestLoc.getBlock();
        }

        return otherChest;
    }

    public static DoubleChest getDoubleChest(Block chest) {
        try {
            return (DoubleChest) ((Chest) chest.getState()).getInventory().getHolder();
        } catch (ClassCastException | NullPointerException ex) {
            return null;
        }
    }

    public static boolean isDoubleChest(Block chest) {
        return getDoubleChest(chest) != null;
    }

    private void getBlock() {
        if (PLUGIN.getListManager().isInventory(loc.getBlock())) {
            Block block = loc.getBlock();

            try {
                if (isDoubleChest(block)) {
                    DoubleChest dbl = getDoubleChest(block);
                    Container left = ((Container) dbl.getLeftSide()),
                            right = ((Container) dbl.getRightSide());
                    chest = left.getPersistentDataContainer().has(PLUGIN.getVarManager().getSignKey(), PersistentDataType.STRING) ? left.getBlock() : right.getBlock();

                } else
                    chest = block;
            } catch (NullPointerException npe) {
                chest = block;
            }
        }
    }

    public BlockState getBlockState() {
        return chest.getState();
    }

    public Inventory getInventory() {
        try {
            return ((InventoryHolder) chest.getState()).getInventory();
        } catch (ClassCastException | NullPointerException ignored) {
        }

        return null;
    }

    public boolean hasStock(List<ShopItemStack> itemToCheck) {
        if (isEmpty()) return false;
        return itemToCheck.size() > 0 && new Utils().getItems(getInventory().getStorageContents(), itemToCheck, 1).get(0) != null;
    }

    public boolean isEmpty() {
        Inventory inv = getInventory();
        if (inv == null) {
            return true;
        }

        for (ItemStack i : inv.getStorageContents()) {
            if (i != null) {
                return false;
            }
        }

        return true;
    }

    public void setSign(ShopLocation newSign) {
        shopSign = newSign;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID uuid) {
        owner = uuid;
    }

    public Block getChest() {
        return chest;
    }

    public ShopLocation getShopSign() {
        return shopSign;
    }

    public boolean hasOwner() {
        return owner != null;
    }

    public boolean hasShopSign() {
        return shopSign != null;
    }

    public Shop getShop() {
        if (hasShopSign()) {
            return Shop.loadShop(getShopSign());
        }

        return null;
    }
}
