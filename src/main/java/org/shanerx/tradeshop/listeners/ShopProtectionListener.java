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
import org.bukkit.Location;
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
import org.shanerx.tradeshop.enumys.DebugLevels;
import org.shanerx.tradeshop.enumys.Permissions;
import org.shanerx.tradeshop.enumys.ShopType;
import org.shanerx.tradeshop.framework.events.HopperShopAccessEvent;
import org.shanerx.tradeshop.framework.events.PlayerShopDestroyEvent;
import org.shanerx.tradeshop.framework.events.PlayerShopInventoryOpenEvent;
import org.shanerx.tradeshop.objects.Shop;
import org.shanerx.tradeshop.objects.ShopChest;
import org.shanerx.tradeshop.objects.ShopLocation;
import org.shanerx.tradeshop.utils.Tuple;
import org.shanerx.tradeshop.utils.Utils;
import org.shanerx.tradeshop.utils.config.Message;
import org.shanerx.tradeshop.utils.config.Setting;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ShopProtectionListener extends Utils implements Listener {

    private final TradeShop plugin;

    public ShopProtectionListener(TradeShop instance) {
        plugin = instance;
    }

    private int nanoMath(int t, int t2) {
        if (t > t2)
            t = 999999999 - t;

        return t2 - t;
    }

    private String nanoDiff(int t, int t2) {
        return String.format("%.6f", (nanoMath(t, t2)) / 1000000.0) + "ms";
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {

        Instant now = Instant.now();
        boolean output = now.getEpochSecond() % 5 == 0 && now.getNano() % 500000 == 0;
        double epochKey = (now.getEpochSecond() % 10000) + ((((now.getNano() % 1000.0) + 1000) / 1000.0) - 1);
        int nanoOffset = now.getNano();
        StringBuilder timingOut = new StringBuilder();

        // TODO: Remove
        timingOut.append("\nHopper Timing Start ").append(epochKey).append("-001: ").append("0.000000ms"); //0ms


        //If all Hopper Settings should be allowed, ignore event
        if (plugin.doSkipHopperProtections()) {
            return;
        }

        // TODO: Remove
        now = Instant.now();
        timingOut.append("\nHopper Timing ").append(epochKey).append("-002: ").append(nanoDiff(nanoOffset, now.getNano())); //+0.000742ms
        nanoOffset = now.getNano();

        if (event.isCancelled() ||
                event instanceof HopperShopAccessEvent ||
                !event.getInitiator().getType().equals(InventoryType.HOPPER)) {
            if (output) {
                now = Instant.now();
                timingOut.append("\nHopper Timing ").append(epochKey).append("-002;B: ").append(nanoDiff(nanoOffset, now.getNano())); //+?
                debugger.log("Hopper Timing: " + epochKey + ":>" + timingOut, DebugLevels.HOPPER_TIMINGS);
            }
            return;
        }

        // TODO: Remove
        now = Instant.now();
        timingOut.append("\nHopper Timing ").append(epochKey).append("-003: ").append(nanoDiff(nanoOffset, now.getNano())); //+0.000919ms
        nanoOffset = now.getNano();

        boolean fromHopper;

        Location srcLoc = event.getSource().getLocation();
        Location destLoc = event.getDestination().getLocation();

        //Locations available but unknown if hoppers

        if (srcLoc == null || destLoc == null) {
            if (output) { // TODO: Remove
                now = Instant.now();
                timingOut.append("\nHopper Timing ").append(epochKey).append("-003;B: ").append(nanoDiff(nanoOffset, now.getNano())); //+?
                debugger.log("Hopper Timing: " + epochKey + ":>" + timingOut, DebugLevels.HOPPER_TIMINGS);
            }
            return;
        } else if (plugin.getListManager().isInventory(srcLoc.getBlock())) {
            fromHopper = false;
        } else if (plugin.getListManager().isInventory(destLoc.getBlock())) {
            fromHopper = true;
        } else {
            if (output) { // TODO: Remove
                now = Instant.now();
                timingOut.append("\nHopper Timing ").append(epochKey).append("-003;C: ").append(nanoDiff(nanoOffset, now.getNano())); //+0.006299ms
                debugger.log("Hopper Timing: " + epochKey + ":>" + timingOut, DebugLevels.HOPPER_TIMINGS);
            }
            return;
        }

        // TODO: Remove
        now = Instant.now();
        timingOut.append("\nHopper Timing ").append(epochKey).append("-004: ").append(nanoDiff(nanoOffset, now.getNano())); //+0.005094ms
        nanoOffset = now.getNano();

        Tuple<Location, Location> searchID = new Tuple<>(event.getSource().getLocation(), event.getDestination().getLocation());
        Boolean cachedState = plugin.getListManager().hopperCheck(searchID);

        if (cachedState != null) {
            event.setCancelled(cachedState);
            if (output) {  // TODO: Remove
                now = Instant.now();
                timingOut.append("\nHopper Timing Cache-Out ").append(epochKey).append("-004;B: ").append(nanoDiff(nanoOffset, now.getNano())); //+?
                debugger.log("Hopper Timing: " + epochKey + ":>" + timingOut, DebugLevels.HOPPER_TIMINGS);
            }
            return;
        }

        // TODO: Remove
        now = Instant.now();
        timingOut.append("\nHopper Timing ").append(epochKey).append("-004;C: ").append(nanoDiff(nanoOffset, now.getNano())); //+0.010800ms
        nanoOffset = now.getNano();

        Block invBlock = (fromHopper ? destLoc : srcLoc).getBlock();
        if (!ShopChest.isShopChest(invBlock)) {
            //plugin.getListManager().hopperPut(searchID, new Tuple<>(false, Instant.now()));
            if (output) {  // TODO: Remove
                now = Instant.now();
                timingOut.append("\nHopper Timing ").append(epochKey).append("-004;D: ").append(nanoDiff(nanoOffset, now.getNano())); //+0.017681ms
                debugger.log("Hopper Timing: " + epochKey + ":>" + timingOut, DebugLevels.HOPPER_TIMINGS);
            }
            return;
        }

        // TODO: Remove
        now = Instant.now();
        timingOut.append("\nHopper Timing ").append(epochKey).append("-005: ").append(nanoDiff(nanoOffset, now.getNano())); //+0.061283ms
        nanoOffset = now.getNano();

        Shop shop = new ShopChest(invBlock.getLocation()).getShop();

        // TODO: Remove
        now = Instant.now();
        timingOut.append("\nHopper Timing ").append(epochKey).append("-005;A: ").append(nanoDiff(nanoOffset, now.getNano())); //+0.061283ms
        nanoOffset = now.getNano();

        boolean isForbidden = !Setting.findSetting(shop.getShopType().name() + "SHOP_HOPPER_" + (fromHopper ? "IMPORT" : "EXPORT")).getBoolean();

        // TODO: Remove
        now = Instant.now();
        timingOut.append("\nHopper Timing ").append(epochKey).append("-005;B: ").append(nanoDiff(nanoOffset, now.getNano())); //+0.061283ms
        nanoOffset = now.getNano();

        if (isForbidden) {
            event.setCancelled(true);

            // TODO: Remvoe
            now = Instant.now();
            timingOut.append("\nHopper Timing ").append(epochKey).append("-006: ").append(nanoDiff(nanoOffset, now.getNano())); //+?
            nanoOffset = now.getNano();

            //plugin.getListManager().hopperPut(searchID, new Tuple<>(true, Instant.now()));

            //TODO: Remove
            now = Instant.now();
            timingOut.append("\nHopper Timing ").append(epochKey).append("-006;B: ").append(nanoDiff(nanoOffset, now.getNano())); //+?
            debugger.log("Hopper Timing: " + epochKey + ":>" + timingOut, DebugLevels.HOPPER_TIMINGS);
            return;
        }

        // TODO: Remove
        now = Instant.now();
        timingOut.append("\nHopper Timing ").append(epochKey).append("-006;C: ").append(nanoDiff(nanoOffset, now.getNano())); //+?
        nanoOffset = now.getNano();

        debugger.log("ShopProtectionListener: Triggered > " + (fromHopper ? "FROM_HOPPER" : "TO_HOPPER"), DebugLevels.PROTECTION);
        debugger.log("ShopProtectionListener: Shop Location as SL > " + shop.getInventoryLocationAsSL().serialize(), DebugLevels.PROTECTION);
        debugger.log("ShopProtectionListener: checked hopper setting > " + shop.getShopType().name() + "SHOP_HOPPER_EXPORT", DebugLevels.PROTECTION);
        HopperShopAccessEvent hopperEvent = new HopperShopAccessEvent(
                shop,
                event.getSource(),
                event.getDestination(),
                event.getItem(),
                fromHopper ? HopperShopAccessEvent.HopperDirection.FROM_HOPPER : HopperShopAccessEvent.HopperDirection.TO_HOPPER
        );
        debugger.log("ShopProtectionListener: (TSAF) HopperEvent fired! ", DebugLevels.PROTECTION);
        Bukkit.getPluginManager().callEvent(hopperEvent);
        debugger.log("ShopProtectionListener: (TSAF) HopperEvent recovered! ", DebugLevels.PROTECTION);
        event.setCancelled(hopperEvent.isForbidden());
        debugger.log("ShopProtectionListener: (TSAF) HopperEvent isForbidden: " + hopperEvent.isForbidden(), DebugLevels.PROTECTION);


        // TODO: Remove
        now = Instant.now();
        timingOut.append("\nHopper Timing Final").append(epochKey).append("-007: ").append(nanoDiff(nanoOffset, now.getNano())); //+?
        debugger.log("Hopper Timing: " + epochKey + ":>" + timingOut, DebugLevels.HOPPER_TIMINGS);
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
            if (Permissions.isAdminEnabled(player) || player.getUniqueId().equals(shop.getOwner().getUUID())) {
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
            if (Permissions.isAdminEnabled(player) || player.getUniqueId().equals(shop.getOwner().getUUID())) {
                PlayerShopDestroyEvent destroyEvent = new PlayerShopDestroyEvent(player, shop);
                Bukkit.getPluginManager().callEvent(destroyEvent);
                if (destroyEvent.isCancelled()) {
                    event.setCancelled(destroyEvent.destroyBlock());
                    return;
                }

                ShopChest sc = shop.getChestAsSC();
                if (sc != null)
                    sc.resetName();

                if (shop.getInventoryLocationAsSL().equals(new ShopLocation(block.getLocation())))
                    shop.removeStorage();
                else
                    plugin.getDataStorage().removeChestLinkage(new ShopLocation(block.getLocation()));

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
        } else if (!block.getType().name().contains("SIGN")) {
            boolean ret = true;
            for (BlockFace face : Arrays.asList(BlockFace.UP, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)) {
                Block temp = block.getRelative(face);
                if (face.equals(BlockFace.UP) && temp.getType().name().contains("SIGN") && !temp.getType().name().contains("WALL_SIGN")) {
                    ret = !ShopType.isShop(temp);
                } else if (temp.getType().name().contains("WALL_SIGN") && ((Directional) temp.getBlockData()).getFacing().equals(face)) {
                    ret = !ShopType.isShop(temp);
                }
            }
            if (ret)
                return;

            event.setCancelled(true);
            player.sendMessage(Message.DESTROY_SHOP_SIGN_FIRST.getPrefixed());
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

            if (shop == null) {
                new ShopChest(block.getLocation()).resetName();
                return;
            }

            if (!Permissions.isAdminEnabled(e.getPlayer()) && !shop.getUsersUUID().contains(e.getPlayer().getUniqueId())) {
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