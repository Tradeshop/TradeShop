/*
 * Copyright (c) 2016-2026
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
 */

package org.shanerx.tradeshop.it;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The one thing a real server will not give a headless harness: a player.
 *
 * <h2>What this is, and what it is not</h2>
 * Everything else in this tier is real. The sign is a real block in a real
 * world, the chest is a real container with a real inventory, the item stacks
 * are the server's own, and TradeShop is the freshly shaded jar reading and
 * writing its real data files. The player is the exception: a
 * {@link Player} exists on a Minecraft server only as the far end of a network
 * connection, and there is no supported way to conjure one without a client.
 *
 * <p>So this is a proxy over the {@code Player} interface with a real
 * {@link Inventory} behind its {@link PlayerInventory}. It answers the handful
 * of questions TradeShop asks a buyer - who are you, what are you holding, may
 * you do this - and throws on everything else, loudly, so that a code path
 * reaching for something this player cannot honestly provide shows up as a red
 * run rather than as a silent null.
 *
 * <p>Driving the server through a real client instead is a separate piece of
 * work, and this class is exactly the seam it replaces: the scenarios stay, the
 * assertions stay, and the actions come from a bot rather than from here.
 */
final class HarnessPlayer {

    /**
     * Everything TradeShop has said to each player, by UUID.
     *
     * <p>Chat is not decoration: it is the whole of what a player is told when
     * the plugin refuses to do something, and "the shop silently did not appear"
     * is a defect of exactly the same shape as "the shop appeared and does not
     * work". A scenario that can only see the world cannot tell the two apart,
     * so what was said is kept as well as what was done.
     *
     * <p>Concurrent both ways round: written on the server thread while the
     * scenario thread reads it.
     */
    private static final Map<UUID, List<String>> SAID = new ConcurrentHashMap<>();

    private HarnessPlayer() {
    }

    /** Every line this player has been sent, colour stripped, oldest first. */
    static List<String> heardBy(Player player) {
        return SAID.getOrDefault(player.getUniqueId(), List.of());
    }

    /**
     * A player with an empty 36-slot inventory, standing at {@code where}.
     *
     * <p>The UUID is derived the way Bukkit derives offline-mode UUIDs, so that
     * {@code Bukkit.getOfflinePlayer(uuid)} - which is what TradeShop stores a
     * shop owner as - resolves to the same identity the harness used.
     */
    static Player create(String name, Location where) {
        return create(name, where, null);
    }

    /**
     * As {@link #create(String, Location)}, but looking at a particular block.
     *
     * <p>Every TradeShop command works on "the sign in front of you":
     * {@code ShopUser.findObservedSign} asks the player for
     * {@code getTargetBlockExact}. Ray-tracing from a player who is not really
     * standing anywhere would be theatre, so the harness answers that one
     * question with the block the scenario means and lets the command do the
     * rest for real.
     */
    static Player create(String name, Location where, Block lookingAt) {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        Inventory backing = Bukkit.createInventory(null, 36, name + "'s inventory");

        Object[] self = new Object[1];
        PlayerInventory inventory = playerInventory(backing, self, where);

        Player player = (Player) Proxy.newProxyInstance(
                HarnessPlayer.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getUniqueId" -> uuid;
                    case "getName", "getDisplayName", "getCustomName", "getPlayerListName" -> name;
                    case "getInventory" -> inventory;
                    case "getLocation", "getEyeLocation" -> where.clone();
                    case "getWorld" -> where.getWorld();
                    case "getServer" -> Bukkit.getServer();
                    case "getType" -> EntityType.PLAYER;
                    case "isOp", "isOnline", "isPermissionSet", "isValid" -> true;
                    // Op-equivalent, matching what the tier-1 scenarios give
                    // their players. A permission check is not what this tier is
                    // here to prove.
                    case "hasPermission" -> true;
                    case "isSneaking", "isDead", "isSleeping" -> false;
                    case "getGameMode" -> org.bukkit.GameMode.SURVIVAL;
                    case "getItemInHand" -> inventory.getItemInMainHand();
                    case "getTargetBlockExact", "getTargetBlock" -> lookingAt;
                    // Chat has nowhere to go without a connection, but it is how
                    // TradeShop explains a refusal - "no chest", "shop limit
                    // reached" - so it goes to the console instead of nowhere.
                    // A scenario that fails for a reason the plugin already
                    // stated should not need a second run to find out why.
                    case "sendMessage", "sendRawMessage" -> {
                        String said = chat(args);
                        SAID.computeIfAbsent(uuid, who -> new CopyOnWriteArrayList<>()).add(said);
                        Bukkit.getLogger().info("[harness] " + name + " was told: " + said);
                        yield null;
                    }
                    // Client-side refreshes have nowhere to go either, and
                    // nothing asserts on them.
                    case "updateInventory", "playSound", "closeInventory" -> null;
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> uuid.hashCode();
                    case "toString" -> "HarnessPlayer(" + name + ")";
                    default -> throw new UnsupportedOperationException(
                            "the harness player cannot answer " + method.getName()
                                    + "() - it is a proxy, not a connected client");
                });

        self[0] = player;
        return player;
    }

    /**
     * A {@link PlayerInventory} over a real inventory.
     *
     * <p>Every method {@link Inventory} declares is delegated to a real 36-slot
     * inventory the server made, so adding, removing, counting and stacking are
     * the server's own logic rather than the harness's idea of it. Only the
     * handful of methods {@code PlayerInventory} adds on top are answered here.
     */
    private static PlayerInventory playerInventory(Inventory backing, Object[] holder, Location where) {
        InvocationHandler handler = (proxy, method, args) -> {
            switch (method.getName()) {
                case "getHolder":
                    return holder[0];
                case "getLocation":
                    return where.clone();
                case "getType":
                    return InventoryType.PLAYER;
                case "getHeldItemSlot":
                    return 0;
                case "getItemInMainHand":
                    return orAir(backing.getItem(0));
                case "getItemInOffHand":
                    return new ItemStack(Material.AIR);
                case "setItemInMainHand":
                    backing.setItem(0, (ItemStack) args[0]);
                    return null;
                case "getArmorContents":
                case "getExtraContents":
                    return new ItemStack[0];
                case "getItem":
                    if (args.length == 1 && args[0] instanceof EquipmentSlot) {
                        return orAir(backing.getItem(0));
                    }
                    break;
                case "toString":
                    return "HarnessPlayerInventory";
                default:
                    break;
            }

            try {
                return method.invoke(backing, args);
            } catch (IllegalArgumentException e) {
                throw new UnsupportedOperationException(
                        "the harness player inventory cannot answer " + method.getName() + "()");
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };

        return (PlayerInventory) Proxy.newProxyInstance(
                HarnessPlayer.class.getClassLoader(),
                new Class<?>[]{PlayerInventory.class},
                handler);
    }

    private static String chat(Object[] args) {
        if (args == null || args.length == 0) {
            return "(nothing)";
        }
        StringBuilder text = new StringBuilder();
        for (Object arg : args) {
            if (arg instanceof String[] many) {
                for (String one : many) {
                    text.append(ChatColor.stripColor(one)).append(' ');
                }
            } else if (arg != null) {
                text.append(ChatColor.stripColor(String.valueOf(arg))).append(' ');
            }
        }
        return text.toString().trim();
    }

    private static ItemStack orAir(ItemStack stack) {
        return stack == null ? new ItemStack(Material.AIR) : stack;
    }

    /**
     * The view a player has open onto a chest.
     *
     * <p>TradeShop re-reads a shop's stock when a chest closes, and the close
     * event carries a view rather than an inventory. The top half is the real
     * chest - which is where {@code ShopRestockListener} gets the location it
     * looks the shop up by - so nothing here can make a shop look stocked when
     * the chest is not.
     */
    static InventoryView viewOf(Player player, Inventory chest) {
        return (InventoryView) Proxy.newProxyInstance(
                HarnessPlayer.class.getClassLoader(),
                new Class<?>[]{InventoryView.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getTopInventory" -> chest;
                    case "getBottomInventory" -> player.getInventory();
                    case "getPlayer" -> player;
                    case "getType" -> InventoryType.CHEST;
                    case "getTitle", "getOriginalTitle" -> "Chest";
                    case "close" -> null;
                    case "toString" -> "HarnessInventoryView";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "the harness inventory view cannot answer " + method.getName() + "()");
                });
    }
}
