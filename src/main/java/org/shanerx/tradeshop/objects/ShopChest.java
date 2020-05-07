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
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.utils.Utils;

import java.util.List;
import java.util.UUID;

public class ShopChest extends Utils {

	private transient static TradeShop plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");
	private ShopLocation shopSign;
	private Location loc;
	private Block chest;
	private UUID owner;
	private String sectionSeparator = "\\$ \\^", titleSeparator = ":";

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
            if (isDoubleChest(checking)) {
                DoubleChest dbl = getDoubleChest(checking);
                return ((Container) dbl.getLeftSide().getInventory().getLocation().getBlock()).getPersistentDataContainer().has(plugin.getStorageKey(), PersistentDataType.STRING) ||
                        ((Container) dbl.getRightSide().getInventory().getLocation().getBlock()).getPersistentDataContainer().has(plugin.getStorageKey(), PersistentDataType.STRING);
            }
            return ((Container) checking.getState()).getPersistentDataContainer().has(plugin.getStorageKey(), PersistentDataType.STRING);
        } catch (NullPointerException | ClassCastException ex) {
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


	/*
		Begin Old Chest name Removal
	*/

    public static DoubleChest getDoubleChest(Block chest) {
        try {
            return (DoubleChest) chest.getState();
        } catch (ClassCastException | NullPointerException ex) {
            return null;
        }
    }

	/*
		End Old Chest name Removal
	*/

	public static boolean isDoubleChest(Block chest) {
        return getDoubleChest(chest) != null;
	}

	private void getBlock() {
		if (loc.getBlock() != null && plugin.getListManager().isInventory(loc.getBlock())) {
            Block block = loc.getBlock();
            if (isDoubleChest(block)) {
                chest = getDoubleChest(block).getInventory().getLocation().getBlock();
            } else {
                chest = block;
            }
		}
	}

	public BlockState getBlockState() {
		return chest.getState();
	}

	public Inventory getInventory() {
		BlockState bs = chest.getState();
		if (bs instanceof InventoryHolder) {
			return ((InventoryHolder) bs).getInventory();
		}

		return null;
	}

    public boolean hasStock(List<ItemStack> product) {
        return product.size() >= 1 && getItems(getInventory(), product, 1).get(0) != null;
    }

	public void loadFromName() {
        if (isShopChest(chest)) {
            String[] name = ((Container) chest.getState()).getPersistentDataContainer().get(plugin.getStorageKey(), PersistentDataType.STRING).split(sectionSeparator);
			shopSign = ShopLocation.deserialize(name[1].split(titleSeparator)[1]);
			owner = UUID.fromString(name[2].split(titleSeparator)[1]);
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
		sb.append("$ ^Sign:");
		sb.append(shopSign.serialize());
		sb.append("$ ^Owner:");
		sb.append(owner.toString());

		return sb.toString();
	}

	public void resetName() {
        if (isShopChest(chest)) {
            Container container = (Container) chest.getState();
            container.getPersistentDataContainer().remove(plugin.getStorageKey());
            container.update();

/*
			if (isDoubleChest(chest)) {
				Container container2 = (Container)getOtherHalfOfDoubleChest(chest).getState();
				container2.getPersistentDataContainer().remove(plugin.getStorageKey());
				container2.update();
			}
*/

		}
	}

	public void setName() {
        setName(chest);
    }

    public void setName(Block toSet) {
        Container container = (Container) chest.getState();
        container.getPersistentDataContainer().set(plugin.getStorageKey(), PersistentDataType.STRING, getName());
        container.update();

/*
		if (isDoubleChest(chest)) {
			Container container2 = (Container)getOtherHalfOfDoubleChest(chest).getState();
			container2.getPersistentDataContainer().set(plugin.getStorageKey(), PersistentDataType.STRING, getName());
			container2.update();
		}
*/
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
