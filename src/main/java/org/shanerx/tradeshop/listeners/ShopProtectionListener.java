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

package org.shanerx.tradeshop.listeners;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.enumys.*;
import org.shanerx.tradeshop.framework.events.HopperShopAccessEvent;
import org.shanerx.tradeshop.framework.events.PlayerShopDestroyEvent;
import org.shanerx.tradeshop.framework.events.PlayerShopInventoryOpenEvent;
import org.shanerx.tradeshop.objects.Shop;
import org.shanerx.tradeshop.objects.ShopChest;
import org.shanerx.tradeshop.utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ShopProtectionListener extends Utils implements Listener {

    private final TradeShop plugin;

    public ShopProtectionListener(TradeShop instance) {
        plugin = instance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {

        //try {
            if (event.isCancelled()) {
                return;
            }

            if (event instanceof HopperShopAccessEvent) {
                return;
            }

        if (!(event.getInitiator().getType().equals(InventoryType.HOPPER) &&
                plugin.getListManager().isInventory(Objects.requireNonNull(event.getSource().getLocation()).getBlock()))) {
            return;
        }

            Block invBlock = event.getSource().getLocation().getBlock();

            if (ShopChest.isShopChest(invBlock)) {
                Shop shop = new ShopChest(invBlock.getLocation()).getShop();
                debugger.log("ShopProtectionListener: Shop Location as SL > " + shop.getInventoryLocationAsSL().serialize(), DebugLevels.PROTECTION);
                boolean isForbidden = !Setting.findSetting(shop.getShopType().name() + "SHOP_HOPPER_EXPORT").getBoolean();
                debugger.log("ShopProtectionListener: isForbidden > " + isForbidden, DebugLevels.PROTECTION);
                debugger.log("ShopProtectionListener: checked hopper setting > " + shop.getShopType().name() + "SHOP_HOPPER_EXPORT", DebugLevels.PROTECTION);
                HopperShopAccessEvent hopperEvent = new HopperShopAccessEvent(shop, event.getSource(), event.getDestination(), event.getItem(), isForbidden);
                Bukkit.getPluginManager().callEvent(hopperEvent);
                debugger.log("ShopProtectionListener: (TSAF) HopperEvent fired! ", DebugLevels.PROTECTION);
                event.setCancelled(hopperEvent.isForbidden());
                debugger.log("ShopProtectionListener: (TSAF) HopperEvent isCancelled: " + hopperEvent.isForbidden(), DebugLevels.PROTECTION);
                debugger.log("ShopProtectionListener: (TSAF) HopperEvent isForbidden: " + isForbidden, DebugLevels.PROTECTION);
            }
        // } catch (NullPointerException ignored) {
        //} // Fix for random NPE triggering from this event that shows no stack trace
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityExplodeItem(EntityExplodeEvent event) {

        if (event.isCancelled())
            return;

		List<Block> toRemove = new ArrayList<>();
		for (Iterator<Block> i = event.blockList().iterator(); i.hasNext(); ) {
			Block b = i.next();
			if (ShopChest.isShopChest(b)) {
				Shop shop = Shop.loadShop((new ShopChest(b.getLocation())).getShopSign());
				if (shop != null) {
                    if (!Setting.findSetting((shop.getShopType().name() + "SHOP_EXPLODE").toUpperCase()).getBoolean())
						i.remove();
					else {
						if (shop.getStorage() != null)
							shop.getChestAsSC().resetName();
						shop.remove();
					}

				}

			} else if (ShopType.isShop(b)) {
                if (!Setting.findSetting(ShopType.getType((Sign) b.getState()).name() + "SHOP_EXPLODE".toUpperCase()).getBoolean()) {
					i.remove();

					if (plugin.getVersion().isBelow(1, 14)) {
						org.bukkit.material.Sign s = (org.bukkit.material.Sign) b.getState().getData();
						toRemove.add(b.getRelative(s.getAttachedFace()));
					} else if (b.getType().toString().contains("WALL_SIGN")) {
						BlockData data = b.getBlockData();
						if (data instanceof Directional)
							toRemove.add(b.getRelative(((Directional) data).getFacing().getOppositeFace()));
					} else {
						toRemove.add(b.getRelative(BlockFace.DOWN));
					}
				} else {
					Shop shop = Shop.loadShop((Sign) b.getState());
					if (shop != null) {

						if (shop.getStorage() != null)
							shop.getChestAsSC().resetName();

						shop.remove();
					}
				}
			}
		}

		event.blockList().removeAll(toRemove);
	}

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        if (event.isCancelled())
            return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Shop shop = null;

        if (ShopType.isShop(block)) {
            if (Setting.ALLOW_SIGN_BREAK.getBoolean()) return;
            shop = Shop.loadShop((Sign) block.getState());
            if (shop == null)
                return;
            if (Permissions.hasPermission(player, Permissions.ADMIN) || player.getUniqueId().equals(shop.getOwner().getUUID())) {
                PlayerShopDestroyEvent destroyEvent = new PlayerShopDestroyEvent(player, shop);
                Bukkit.getPluginManager().callEvent(destroyEvent);
                if (destroyEvent.isCancelled()) {
                    event.setCancelled(destroyEvent.destroyBlock());
                    return;
                }

                if (shop.getChestAsSC() != null)
                    shop.getChestAsSC().resetName();
                shop.remove();
                return;
            }

            event.setCancelled(true);
            player.sendMessage(Message.NO_TS_DESTROY.getPrefixed());

        } else if (ShopChest.isShopChest(block)) {
            if (Setting.ALLOW_CHEST_BREAK.getBoolean()) return;
            shop = new ShopChest(block.getLocation()).getShop();
            if (shop == null)
                return;
            if (Permissions.hasPermission(player, Permissions.ADMIN) || player.getUniqueId().equals(shop.getOwner().getUUID())) {
                PlayerShopDestroyEvent destroyEvent = new PlayerShopDestroyEvent(player, shop);
                Bukkit.getPluginManager().callEvent(destroyEvent);
                if (destroyEvent.isCancelled()) {
                    event.setCancelled(destroyEvent.destroyBlock());
                    return;
                }

                shop.getChestAsSC().resetName();
                shop.removeStorage();

                if (shop.getShopSign() == null) {
                    shop.remove();
                } else {
                    shop.updateSign();
                }

                shop.saveShop();
                return;
            }

            event.setCancelled(true);
            player.sendMessage(Message.NO_TS_DESTROY.getPrefixed());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChestOpen(PlayerInteractEvent e) {

        if (e.isCancelled())
            return;

        Block block = e.getClickedBlock();

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || !plugin.getListManager().isInventory(block)) {
            return;
        }

        ShopChest.resetOldName(block);

        if (ShopChest.isShopChest(block)) {
            Shop shop = new ShopChest(block.getLocation()).getShop();
            PlayerShopInventoryOpenEvent openEvent = new PlayerShopInventoryOpenEvent(e.getPlayer(), shop, e.getAction(), e.getItem(), e.getClickedBlock(), e.getBlockFace());

            if (!Permissions.hasPermission(e.getPlayer(), Permissions.ADMIN) && !shop.getUsersUUID().contains(e.getPlayer().getUniqueId())) {
                openEvent.setCancelled(true);
            }

            Bukkit.getPluginManager().callEvent(openEvent);
            e.setCancelled(openEvent.isCancelled());
            if (e.isCancelled()) {
                e.getPlayer().sendMessage(Message.NO_TS_OPEN.getPrefixed());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {

        if (event.isCancelled())
            return;

        Block block = event.getBlock();

        if (!plugin.getListManager().isInventory(block))
            return;

        Sign shopSign = findShopSign(block);

        if (shopSign == null)
            return;

        Shop shop = Shop.loadShop(shopSign);

        if (shop.getShopType().isITrade())
            return;

        if (shop.getUsersUUID().contains(event.getPlayer().getUniqueId())) {
            if (!shop.hasStorage()) {
                new ShopChest(block, shop.getOwner().getUUID(), shopSign.getLocation()).setEventName(event);
                shop.setInventoryLocation(block.getLocation());
                shop.saveShop();
            }
        } else {
            event.setCancelled(true);
        }
        return;
    }
}