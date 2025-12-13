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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Nameable;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.item.ShopItemStack;
import org.shanerx.tradeshop.shoplocation.IllegalWorldException;
import org.shanerx.tradeshop.shoplocation.ShopLocation;
import org.shanerx.tradeshop.utils.Utils;
import org.shanerx.tradeshop.utils.debug.DebugLevels;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ShopChest extends Utils {

    private final static TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
    private final Location loc;
    private final String sectionSeparator = "\\$ \\^";
    private final String titleSeparator = ";;";
    private ShopLocation shopSign;
    private Block chest;
    private UUID owner;

    public ShopChest(Location chestLoc) {
        this.loc = chestLoc;

        getBlock();
        loadFromName();
    }

    public ShopChest(Block chest, UUID owner, Location sign) {
        this.loc = chest.getLocation();
        this.owner = owner;
        this.shopSign = new ShopLocation(sign);
        this.chest = chest;
    }

    public static boolean isShopChest(Block checking) {
        try {
            // Check if input is valid
            if (checking == null) {
                plugin.getDebugger().log("isShopChest: Block input is null", DebugLevels.PROTECTION);
                return false;
            }

            if (isDoubleChest(checking)) {
                //plugin.getDebugger().log("isShopChest: Block is a double chest", DebugLevels.PROTECTION);
                DoubleChest dbl = getDoubleChest(checking);

                if (dbl == null) {
                    plugin.getDebugger().log("isShopChest: getDoubleChest returned null", DebugLevels.PROTECTION);
                    return false;
                }

                // Check left side
                InventoryHolder leftSide = dbl.getLeftSide();
                if (leftSide == null) {
                    //plugin.getDebugger().log("isShopChest: Double chest left side is null", DebugLevels.PROTECTION);
                } else {
                    Container leftContainer = (Container) leftSide;
                    if (leftContainer.getPersistentDataContainer() == null) {
                        //plugin.getDebugger().log("isShopChest: Left container persistent data container is null", DebugLevels.PROTECTION);
                    } else if (leftContainer.getPersistentDataContainer().has(plugin.getSignKey(), PersistentDataType.STRING)) {
                        //plugin.getDebugger().log("isShopChest: Found sign key in left side of double chest", DebugLevels.PROTECTION);
                        return true;
                    }
                }

                // Check right side
                InventoryHolder rightSide = dbl.getRightSide();
                if (rightSide == null) {
                    //plugin.getDebugger().log("isShopChest: Double chest right side is null", DebugLevels.PROTECTION);
                } else {
                    Container rightContainer = (Container) rightSide;
                    if (rightContainer.getPersistentDataContainer() == null) {
                        //plugin.getDebugger().log("isShopChest: Right container persistent data container is null", DebugLevels.PROTECTION);
                    } else if (rightContainer.getPersistentDataContainer().has(plugin.getSignKey(), PersistentDataType.STRING)) {
                        //plugin.getDebugger().log("isShopChest: Found sign key in right side of double chest", DebugLevels.PROTECTION);
                        return true;
                    }
                }

                return false;
            }

            // Single chest check
            //plugin.getDebugger().log("isShopChest: Block is a single inventory", DebugLevels.PROTECTION);
            BlockState state = checking.getState();
            if (state == null) {
                plugin.getDebugger().log("isShopChest: Block state is null", DebugLevels.PROTECTION);
                return false;
            }

            if (!(state instanceof Container)) {
                plugin.getDebugger().log("isShopChest: Block state is not a Container: " + state.getType().toString(), DebugLevels.PROTECTION);
                return false;
            }

            Container container = (Container) state;
            if (container.getPersistentDataContainer() == null) {
                plugin.getDebugger().log("isShopChest: Container persistent data container is null", DebugLevels.PROTECTION);
                return false;
            }

            boolean conHas = container.getPersistentDataContainer().has(plugin.getSignKey(), PersistentDataType.STRING);
            //plugin.getDebugger().log("isShopChest: Single chest sign key check result: " + conHas, DebugLevels.PROTECTION);
            return conHas;

        } catch (NullPointerException ex) {
            plugin.getDebugger().log("NPE thrown during isShopChest: " + ex.getMessage(), DebugLevels.PROTECTION);
            plugin.getDebugger().log("Stack trace: ", DebugLevels.PROTECTION);
            ex.printStackTrace();
        } catch (ClassCastException ex) {
            plugin.getDebugger().log("ClassCastException thrown during isShopChest: " + ex.getMessage(), DebugLevels.PROTECTION);
        }
        return false;
    }

    public static boolean isShopChest(Inventory checking) {
        try {
            return isShopChest(checking.getLocation().getBlock());
        } catch (NullPointerException ex) {
        }
        return false;
    }

    public static void resetOldName(Block checking) {
        if (checking != null) {
            BlockState bs = checking.getState();
            if (bs instanceof Nameable && ((Nameable) bs).getCustomName() != null) {

                if (isDoubleChest(checking)) {
                    DoubleChest dbl = getDoubleChest(checking);
                    BlockState stateLeft = dbl.getLeftSide().getInventory().getLocation().getBlock().getState();
                    BlockState stateRight = dbl.getRightSide().getInventory().getLocation().getBlock().getState();

                    if (((Nameable) stateRight).getCustomName().contains("$ ^Sign:l_")) {
                        ((Nameable) stateRight).setCustomName(((Nameable) stateRight).getCustomName().split("\\$ \\^")[0]);
                        stateRight.update();
                    }
                    if (((Nameable) stateLeft).getCustomName().contains("$ ^Sign:l_")) {
                        ((Nameable) stateLeft).setCustomName(((Nameable) stateLeft).getCustomName().split("\\$ \\^")[0]);
                        stateLeft.update();
                    }

                } else if (((Nameable) bs).getCustomName().contains("$ ^Sign:l_")) {
                    ((Nameable) bs).setCustomName(((Nameable) bs).getCustomName().split("\\$ \\^")[0]);
                    bs.update();
                }
            }
        }
    }

    public static Block getOtherHalfOfDoubleChest(Block check) {
        Block otherChest = null;
        if (check.getState() instanceof Chest) {
            Chest chest = (Chest) check.getState();
            Location chestLoc = chest.getInventory().getLocation(), otherChestLoc = chest.getInventory().getLocation();
            if (chestLoc.getX() - Math.floor(chestLoc.getX()) > 0) {
                otherChestLoc.setX(check.getX() == Math.floor(chestLoc.getX()) ? Math.ceil(chestLoc.getX()) : Math.floor(chestLoc.getX()));
            } else if (chestLoc.getZ() - Math.floor(chestLoc.getZ()) > 0) {
                otherChestLoc.setZ(check.getX() == Math.floor(chestLoc.getZ()) ? Math.ceil(chestLoc.getZ()) : Math.floor(chestLoc.getZ()));
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
        if (plugin.getListManager().isInventory(loc.getBlock())) {
            Block block = loc.getBlock();

            try {
                if (isDoubleChest(block)) {
                    DoubleChest dbl = getDoubleChest(block);
                    Container left = ((Container) dbl.getLeftSide()),
                            right = ((Container) dbl.getRightSide());
                    chest = left.getPersistentDataContainer().has(plugin.getSignKey(), PersistentDataType.STRING) ? left.getBlock() : right.getBlock();

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
        } catch (ClassCastException | NullPointerException ex) {
        }

        return null;
    }

    public boolean hasStock(List<ShopItemStack> itemToCheck) {
        return itemToCheck.size() > 0 && getItems(getInventory().getStorageContents(), itemToCheck, 1).get(0) != null;
    }

    public void loadFromName() {
        if (isShopChest(chest)) {
            String[] name = ((Container) chest.getState()).getPersistentDataContainer().get(plugin.getSignKey(), PersistentDataType.STRING)
                    .replaceAll("Sign:", "Sign" + titleSeparator).replaceAll("Owner:", "Owner" + titleSeparator)
                    .split(sectionSeparator);
            Map<String, String> chestData = new HashMap<>();
            for (String s : name) {
                chestData.put(s.split(titleSeparator)[0], s.replace(s.split(titleSeparator)[0] + titleSeparator, ""));
            }

            chestData.forEach((k, v) -> plugin.getDebugger().log(k + " = " + v, DebugLevels.PROTECTION));
            try {
                shopSign = ShopLocation.deserialize(chestData.get("Sign"));
            } catch (IllegalWorldException e) {
                shopSign = new ShopLocation(e.getLoc().getLocation(chest.getWorld()));
            }

            owner = UUID.fromString(chestData.get("Owner"));
        }
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

    public String getName() {
        String sb = "$ ^Sign" +
                titleSeparator +
                shopSign.serialize() +
                "$ ^Owner" +
                titleSeparator +
                owner.toString();

        return sb;
    }

    public void setName(Block toSet) {
        Container container = (Container) chest.getState();
        container.getPersistentDataContainer().set(plugin.getSignKey(), PersistentDataType.STRING, getName());
        container.update();
    }

    public void setName() {
        setName(chest);
    }

    public void resetName() {
        if (isShopChest(chest)) {
            Container container = (Container) chest.getState();
            container.getPersistentDataContainer().remove(plugin.getStorageKey());
            container.getPersistentDataContainer().remove(plugin.getSignKey());
            container.update();
        }
    }

    public void setEventName(BlockPlaceEvent event) {
        setName(event.getBlockPlaced());
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
