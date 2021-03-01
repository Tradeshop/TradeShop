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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Nameable;
import org.bukkit.block.*;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.shanerx.tradeshop.IllegalWorldException;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.enumys.DebugLevels;
import org.shanerx.tradeshop.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ShopChest extends Utils {

	private final transient static TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
	private ShopLocation shopSign;
	private final Location loc;
	private Block chest;
	private UUID owner;
	private final String sectionSeparator = "\\$ \\^";
	private final String titleSeparator = ";;";

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
		plugin.getDebugger().log("isShopChest checking Block at " + new ShopLocation(checking.getLocation()).serialize() + "", DebugLevels.PROTECTION);
        try {
            if (isDoubleChest(checking)) {
                DoubleChest dbl = getDoubleChest(checking);
				boolean leftHas = ((Container) dbl.getLeftSide()).getPersistentDataContainer().has(plugin.getSignKey(), PersistentDataType.STRING),
						rightHas = ((Container) dbl.getRightSide()).getPersistentDataContainer().has(plugin.getSignKey(), PersistentDataType.STRING);

				plugin.getDebugger().log("Block is DoubleChest", DebugLevels.PROTECTION);
				plugin.getDebugger().log("Left side PerData: " + (leftHas ? ((Container) dbl.getLeftSide()).getPersistentDataContainer().get(plugin.getSignKey(), PersistentDataType.STRING) : "null"), DebugLevels.PROTECTION);
				plugin.getDebugger().log("Right side PerData: " + (rightHas ? ((Container) dbl.getRightSide()).getPersistentDataContainer().get(plugin.getSignKey(), PersistentDataType.STRING) : "null"), DebugLevels.PROTECTION);

				return leftHas || rightHas;
            }
			boolean conHas = ((Container) checking.getState()).getPersistentDataContainer().has(plugin.getSignKey(), PersistentDataType.STRING);
			plugin.getDebugger().log("Block is SINGLE inventory", DebugLevels.PROTECTION);
			plugin.getDebugger().log("Storage Block PerData: " + (conHas ? ((Container) checking.getState()).getPersistentDataContainer().get(plugin.getSignKey(), PersistentDataType.STRING) : "null"), DebugLevels.PROTECTION);
			return conHas;
        } catch (NullPointerException | ClassCastException ex) {
			plugin.getDebugger().log("NPE thrown during isShopChest by: \n" + ex.getCause(), DebugLevels.PROTECTION);
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

    public boolean hasStock(List<ShopItemStack> product) {
		return product.size() > 0 && getItems(getInventory(), product, 1).get(0) != null;
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
		StringBuilder sb = new StringBuilder();
		sb.append("$ ^Sign");
		sb.append(titleSeparator);
		sb.append(shopSign.serialize());
		sb.append("$ ^Owner");
		sb.append(titleSeparator);
		sb.append(owner.toString());

		return sb.toString();
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
