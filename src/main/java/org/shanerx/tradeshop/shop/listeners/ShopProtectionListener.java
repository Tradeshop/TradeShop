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

package org.shanerx.tradeshop.shop.listeners;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.HangingSign;
import org.bukkit.block.data.type.WallHangingSign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSignOpenEvent;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.data.config.Message;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.framework.events.HopperShopAccessEvent;
import org.shanerx.tradeshop.framework.events.PlayerShopDestroyEvent;
import org.shanerx.tradeshop.framework.events.PlayerShopInventoryOpenEvent;
import org.shanerx.tradeshop.player.Permissions;
import org.shanerx.tradeshop.player.ShopRole;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shop.ShopChest;
import org.shanerx.tradeshop.shop.ShopSettingKeys;
import org.shanerx.tradeshop.shop.ShopType;
import org.shanerx.tradeshop.shoplocation.ShopLocation;
import org.shanerx.tradeshop.utils.Utils;
import org.shanerx.tradeshop.utils.debug.DebugLevels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ShopProtectionListener implements Listener {

    private final TradeShop plugin;

    public ShopProtectionListener(TradeShop instance) {
        plugin = instance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {

        //If all Hopper Settings should be allowed, ignore event
        if (plugin.getVarManager().doSkipHopperProtection()) {
            return;
        }

        if (event instanceof HopperShopAccessEvent ||
                !event.getInitiator().getType().equals(InventoryType.HOPPER)) {
            return;
        }

        Boolean skip = plugin.getListManager().canSkipHopper(event.getInitiator().getLocation());

        if (skip != null) {
            if (skip) event.setCancelled(true);
            return;
        }

        boolean fromHopper = event.getInitiator().equals(event.getSource());

        Block invBlock;

        // Ignore below warning, try/catch is for this NPE since It should almost never happen with previous checks I believe this is faster than an if null
        try {
            invBlock = (fromHopper ? event.getDestination() : event.getSource()).getLocation().getBlock();
        } catch (NullPointerException ignored) {
            plugin.getListManager().addSkippableHopper(event.getInitiator().getLocation(), false);
            plugin.getDebugger().log("Protection Null Catch for \n  " + event.getInitiator().getLocation(), DebugLevels.PROTECTION);
            return;
        }

        if (!plugin.getListManager().isInventory(invBlock)) {
            plugin.getListManager().addSkippableHopper(event.getInitiator().getLocation(), false);
            plugin.getDebugger().log("Protection Inventory Catch for \n  " + event.getInitiator().getLocation(), DebugLevels.PROTECTION);
            return;
        }

        if (!ShopChest.isShopChest(invBlock)) {
            //Just return as adding these to the skip would often catch the second half of hoppers pulling from shops then pushing to a chest
            return;
        }

        Shop shop = plugin.getDataStorage().loadShopFromStorage(new ShopLocation(invBlock.getLocation()));

        if (shop == null) {
            plugin.getListManager().addSkippableHopper(event.getInitiator().getLocation(), false);
            plugin.getDebugger().log("Protection Shop Null Catch for \n  " + event.getInitiator().getLocation(), DebugLevels.PROTECTION);
            return;
        }

        boolean isForbidden = !(fromHopper ? shop.getShopSetting(ShopSettingKeys.HOPPER_IMPORT).asBoolean() : shop.getShopSetting(ShopSettingKeys.HOPPER_EXPORT).asBoolean());
        if (isForbidden) {
            event.setCancelled(true);
            //plugin.getListManager().addSkippableShop(event.getInitiator().getLocation(), true);
            return;
        }

        plugin.getDebugger().log("ShopProtectionListener: Triggered > " + (fromHopper ? "FROM_HOPPER" : "TO_HOPPER"), DebugLevels.PROTECTION);
        plugin.getDebugger().log("ShopProtectionListener: Shop Location as SL > " + shop.getInventoryLocationAsSL().toString(), DebugLevels.PROTECTION);
        plugin.getDebugger().log("ShopProtectionListener: checked hopper setting > " + shop.getShopType().name() + "SHOP_HOPPER_EXPORT", DebugLevels.PROTECTION);
        HopperShopAccessEvent hopperEvent = new HopperShopAccessEvent(
                shop,
                event.getSource(),
                event.getDestination(),
                event.getItem(),
                fromHopper ? HopperShopAccessEvent.HopperDirection.FROM_HOPPER : HopperShopAccessEvent.HopperDirection.TO_HOPPER
        );
        plugin.getDebugger().log("ShopProtectionListener: (TSAF) HopperEvent fired! ", DebugLevels.PROTECTION);
        Bukkit.getPluginManager().callEvent(hopperEvent);
        plugin.getDebugger().log("ShopProtectionListener: (TSAF) HopperEvent recovered! ", DebugLevels.PROTECTION);
        plugin.getDebugger().log("ShopProtectionListener: (TSAF) HopperEvent isForbidden: " + hopperEvent.isForbidden(), DebugLevels.PROTECTION);
        event.setCancelled(hopperEvent.isForbidden());
        //plugin.getListManager().addSkippableShop(event.getInitiator().getLocation(), hopperEvent.isForbidden());

        if (!hopperEvent.isForbidden()) {
            new Utils().scheduleShopDelayUpdate("ShopProtectionListener#onInventoryMoveItem", shop, 2L);
        }
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
                        shop.remove();
                    }

                }

            } else if (ShopType.isShop(b)) {
                if (!Setting.findSetting(ShopType.getType((Sign) b.getState()).name() + "SHOP_EXPLODE".toUpperCase()).getBoolean()) {
                    i.remove();

                    toRemove.addAll(supportingBlocks(b));
                } else {
                    Shop shop = Shop.loadShop((Sign) b.getState());
                    if (shop != null) {
                        shop.remove();
                    }
                }
            }
        }

        event.blockList().removeAll(toRemove);
    }

    /**
     * The blocks a shop sign is held up by, and which an explosion must
     * therefore be kept away from: destroying one of them pops the sign off as
     * an item and the shop is gone despite being protected.
     *
     * <p>Read off the sign's {@link BlockData}, which is the server describing
     * its own block, so a mounting Minecraft adds later is answered correctly
     * without an edit here. The test this replaces was
     * {@code getType().toString().contains("WALL_SIGN")}, and
     * {@code "OAK_WALL_HANGING_SIGN".contains("WALL_SIGN")} is <em>false</em> -
     * the substring is {@code WALL_HANGING_SIGN} - so every wall-hanging sign
     * fell through to the block below it and the explosion was let through at
     * the wall that was actually holding it up.
     *
     * <p>Order matters: {@link WallHangingSign} is also {@link Directional}, so
     * it has to be asked about first or it answers as an ordinary wall sign.
     *
     * <p>BKCommonLib's {@code BlockUtil.getAttachedBlock} is not used here, and
     * that is a departure from the plan this change came from.
     * {@code BlockData.getAttachedFace()} resolves through
     * {@code org.bukkit.material.Attachable} - the pre-1.13 {@code MaterialData}
     * bridge - and returns {@link BlockFace#DOWN} for anything with no legacy
     * equivalent. Wall-hanging signs have none, so the library gives exactly the
     * wrong answer this method exists to fix.
     *
     * <p>Public and static so that it can be asserted on its own. The end-to-end
     * route to it is an {@code EntityExplodeEvent}, whose constructor is not
     * stable across the versions this plugin is built against and run on; the
     * decision this method makes is the whole of what the defect was, and a test
     * that names it is worth more than one that has to build an explosion first.
     */
    public static List<Block> supportingBlocks(Block signBlock) {
        BlockData data = signBlock.getBlockData();

        if (data instanceof WallHangingSign) {
            // A wall hanging sign spans two blocks and survives on either, so
            // both are load-bearing and both are protected. Its facing is the
            // way the text points, which is across the bar it hangs from.
            BlockFace facing = ((WallHangingSign) data).getFacing();
            return Arrays.asList(signBlock.getRelative(rotateClockwise(facing)),
                    signBlock.getRelative(rotateClockwise(facing.getOppositeFace())));
        }

        if (data instanceof HangingSign) {
            return Collections.singletonList(signBlock.getRelative(BlockFace.UP));
        }

        if (data instanceof Directional) {
            return Collections.singletonList(
                    signBlock.getRelative(((Directional) data).getFacing().getOppositeFace()));
        }

        return Collections.singletonList(signBlock.getRelative(BlockFace.DOWN));
    }

    /**
     * The next cardinal face clockwise, which Bukkit's {@link BlockFace} does
     * not provide.
     */
    private static BlockFace rotateClockwise(BlockFace face) {
        switch (face) {
            case NORTH:
                return BlockFace.EAST;
            case EAST:
                return BlockFace.SOUTH;
            case SOUTH:
                return BlockFace.WEST;
            case WEST:
                return BlockFace.NORTH;
            default:
                return face;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        if (event.isCancelled())
            return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Shop shop = null;

        if (ShopType.isShop(block)) {
            if (Setting.ALLOW_SIGN_BREAK.getBoolean()) {
                removeShopWhoseSignAnyoneMayBreak(block);
                return;
            }
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

        } else if (block.getType().name().contains("SIGN")) {
            removeShopStoredAgainst(block);

        } else {
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

    /**
     * Drops the shop stored against a sign that is being broken and no longer
     * reads as one, so that the record does not outlive the only block that
     * could have found it again.
     *
     * <p>{@link #onBlockBreak} above decides whether to clean a shop up by asking
     * {@link ShopType#isShop(Block)}, which is the block's <em>front</em> lines -
     * {@code isShop(Block)} hands the block state to
     * {@link ShopType#getType(Sign)}, and that is line 0 of the front. So the
     * question the listener asks is "does this block still READ as a shop" and
     * the question it needs answered is "is a shop STORED against this block".
     * This method is the second question, asked where the first has already said
     * no.
     *
     * <h2>Why the answer is to remove it rather than to protect it</h2>
     * A shop whose sign no longer names it is already unreachable, in every
     * direction. It cannot be traded with, because {@code ShopTradeListener}
     * reads the front. It cannot be repaired, because {@link Shop#getShopSign()}
     * is the same test, so {@link Shop#updateSign()} can never write the header
     * back. It cannot be removed by its owner, because every removal path starts
     * from a sign. What it can still do is count against its owner's limit and
     * against {@code MAX_SHOPS_PER_CHUNK}, and hold its storage block linked -
     * which is hopper protection and a break refusal enforced on behalf of a shop
     * nobody can use. Once the block is gone, this event is the last thing that
     * will ever mention that location.
     *
     * <p>Protecting the block instead was the other option and it is worse: the
     * plugin would be refusing to let a player break a sign that reads as an
     * ordinary sign, for a shop it will not show them, with no way for anyone to
     * clear it.
     *
     * <h2>What is NOT done here</h2>
     * Nothing already on disk is swept. This runs when a block breaks and touches
     * only the record at that block; a shop whose sign an explosion, a world edit
     * or a rollback took is left exactly where it is, because a sweep cannot tell
     * that shop from one whose chunk is merely unloaded and would delete both.
     * Removing those is a decision for whoever owns the plugin, and the argument
     * for making it is not "the sign is missing" - it is a report from an
     * operator who wants it.
     *
     * <p>{@link PlayerShopDestroyEvent} is not fired. It is cancellable, and a
     * listener that cancelled it would recreate the orphan this method exists to
     * prevent - the block breaks either way. It is also an event about a shop no
     * consumer of it could have seen: every path that hands a shop to another
     * plugin reads the front of the sign, which is what stopped naming this one.
     *
     * <h2>Cost</h2>
     * One storage lookup, and only for a block that is a sign and has already
     * failed {@code ShopType.isShop}. Nothing on the hopper path -
     * {@link #onInventoryMoveItem} is untouched and a hopper breaks no blocks -
     * and nothing on the ordinary break of a non-sign block, which reaches the
     * branch below this one without asking. The lookup itself is a hit in
     * {@code DataStorage}'s shop cache for any shop the server has touched, and
     * on a miss it is one read of the chunk's data file, which {@code DataStorage}
     * then keeps. A sign break is a player action at human rate; the plugin
     * already does the same read per chunk for every chunk in range of
     * {@code /tradeshop search}.
     */
    private void removeShopStoredAgainst(Block signBlock) {
        Shop stored = Shop.loadShop(new ShopLocation(signBlock.getLocation()));

        if (stored == null) return;

        // Shop.remove takes the chest linkage with it - DataStorage.removeShop
        // drops every linkage entry pointing at this shop - and purges the shop
        // from its users' files, so it stops counting against their limits. The
        // storage block is left alone: its contents are the owner's items and a
        // broken sign is not consent to touch them.
        stored.remove();

        plugin.getDebugger().log("ShopProtectionListener: removed the shop stored at "
                + stored.getShopLocationAsSL() + ", whose sign was broken while its text no longer "
                + "read as a shop. Its storage block, if any, has been unlinked and its contents "
                + "left untouched.", DebugLevels.PROTECTION);
    }

    /**
     * Takes the shop down when {@code allow-sign-break} has already decided the
     * break is going to happen.
     *
     * <p>{@link #onBlockBreak} returns on that setting before every check that
     * would otherwise have protected the sign, and it used to return before
     * {@link Shop#remove()} as well. The block became air and the record
     * survived, unreachable in every direction - no trade, because
     * {@code ShopTradeListener} reads the sign; no repair, because
     * {@link Shop#getShopSign()} is the same test, so {@link Shop#updateSign()}
     * can never write the header back; no removal, because every removal path
     * starts from a sign - while it went on counting against its owner's limit
     * and against {@code MAX_SHOPS_PER_CHUNK} and holding its storage block
     * linked. That is the same orphan {@link #removeShopStoredAgainst} closes,
     * reached through a setting rather than through a rewritten sign.
     *
     * <h2>The record and the linkage, and nothing else</h2>
     * The shop record goes and the chest linkage goes with it. Nothing here
     * touches the storage block or anything inside it.
     *
     * <p>Only the sign was broken. The storage block is a different block,
     * standing, unbroken, and full of the owner's items; a plugin that emptied it
     * would be inventing a drop the server never asked for. Dropping the contents
     * of a block is the server's job and it does it for the block that actually
     * broke - which here is a sign, and the server pops that as an item the way
     * it does for any sign, because the break is not cancelled. So what an
     * operator gets is a deleted shop and an ordinary chest, still holding
     * everything it held a moment before. If they want the items too, they break
     * the chest, and {@code allow-chest-break} is the setting that governs that.
     *
     * <h2>{@link PlayerShopDestroyEvent} is not fired</h2>
     * Same reason as {@link #removeShopStoredAgainst}, and one more that is
     * specific to this path. The event is cancellable and its
     * {@code destroyBlock()} defaults to {@code false}, so a listener that merely
     * cancelled it would reach {@code event.setCancelled(false)} - the block
     * still breaks, and the record it just refused to delete outlives it. And the
     * operator has said in config.yml that anyone may break a shop's sign; an
     * event that can refuse the break contradicts the setting it is running
     * under. Nothing was fired on this path before this change either, so no
     * consumer loses anything it was getting.
     */
    private void removeShopWhoseSignAnyoneMayBreak(Block signBlock) {
        Shop shop = Shop.loadShop((Sign) signBlock.getState());

        if (shop == null) return;

        // Shop.remove takes the chest linkage with it - DataStorage.removeShop
        // drops every linkage entry pointing at this shop - and purges the shop
        // from its users' files, so it stops counting against their limits. The
        // storage block is left alone, contents and all: it is not the block
        // being broken, and its contents are the owner's items.
        shop.remove();

        plugin.getDebugger().log("ShopProtectionListener: allow-sign-break is on, so the shop at "
                + shop.getShopLocationAsSL() + " was removed with its sign. Its storage block has "
                + "been unlinked and is otherwise untouched, contents included.", DebugLevels.PROTECTION);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChestOpen(PlayerInteractEvent e) {

        if (e.isCancelled())
            return;

        Block block = e.getClickedBlock();

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || !plugin.getListManager().isInventory(block)) {
            return;
        }

        if (ShopChest.isShopChest(block)) {
            Shop shop = new ShopChest(block.getLocation()).getShop();
            PlayerShopInventoryOpenEvent openEvent = new PlayerShopInventoryOpenEvent(e.getPlayer(), shop, e.getAction(), e.getItem(), e.getClickedBlock(), e.getBlockFace());

            if (shop == null) {
                return;
            }

            if (!Permissions.isAdminEnabled(e.getPlayer()) && !shop.getUsersUUID(ShopRole.OWNER, ShopRole.MANAGER, ShopRole.MEMBER).contains(e.getPlayer().getUniqueId())) {
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
    public void onSignChange(SignChangeEvent e) {
        if (e.isCancelled()) return;

        if (ShopType.isShop(e.getBlock())) e.setCancelled(true);
    }

    /**
     * A shop sign's editor does not open, on any server.
     *
     * <p>{@link PlayerSignOpenEvent} is plain Bukkit API: it lives in
     * {@code org.bukkit.event.player} and ships in {@code spigot-api} as well as
     * in {@code paper-api}, so it is there on every server this plugin can run
     * on. This handler therefore needs no capability test, no version check and
     * no second listener for a fork-specific event. Paper fires it too, beside
     * its own {@code io.papermc.paper.event.player.PlayerOpenSignEvent}:
     * measured on Paper 1.21.11 build 132 on every path probed, including the
     * right-click that opens the editor on a sign that is already written
     * ({@code Cause.INTERACT}).
     *
     * <p>Paper's copy of the class is deprecated for removal, in favour of
     * Paper's own event; Spigot's is not deprecated and Spigot offers nothing
     * else. Obeying that deprecation would mean a Paper-only guard and a Spigot
     * server with none, so the removal warning is suppressed at the use site
     * rather than obeyed, and this paragraph is the reason.
     *
     * <h2>Both sides, and why that is a decision rather than an oversight</h2>
     * {@link PlayerSignOpenEvent#getSide()} says which face the editor was opened
     * on, and it is not consulted. A shop's lines live on the front - it is
     * {@link ShopType#getType(Sign)} reading line 0, which is the front side -
     * so a side test would let the back of a shop sign open. It would open onto
     * nothing: {@link #onSignChange} above cancels the finished edit for the
     * whole block whichever side it came from, so what a player would get is a
     * screen whose result is thrown away. {@link #onShopSignInteract} below is
     * side-blind for the same reason - a right-click is denied at the block, not
     * at a face. Refusing both sides is the answer that agrees with both of the
     * guards either side of it, and it costs a player nothing they could have
     * kept.
     *
     * <p>An ordinary sign is untouched, and a blank sign being placed is untouched
     * too - {@code Cause.PLACE} arrives before anything is written, so a player
     * making a new shop still gets the editor that creates it.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    @SuppressWarnings("removal")
    public void onShopSignOpen(PlayerSignOpenEvent e) {
        if (ShopType.isShop(e.getSign())) e.setCancelled(true);
    }

    /**
     * A shop sign does not open for editing, whatever the shop is doing.
     *
     * <p>{@link #onSignChange} above already refuses the finished edit of a shop
     * sign, for everyone including an admin, so a sign editor that opens over
     * one is a screen whose result is thrown away. What was missing was anything
     * that stopped it opening at all: the only guard that did was a listener for
     * Paper's {@code PlayerOpenSignEvent}, registered from a version string that
     * had stopped matching Paper. {@link #onShopSignOpen} above is now the
     * portable answer to that, and this handler is the other half rather than a
     * fallback - see below. What the plugin was leaning on before either of them
     * was {@code ShopTradeListener}'s {@code e.setCancelled(true)}, and that line
     * sits below every early return in the method: no shop, a storage block that
     * is gone, an illegal item, CLOSED, INCOMPLETE, OUT_OF_STOCK and a cancelled
     * {@code PlayerPrepareTradeEvent} all return before it. So a player got
     * protection or not depending on what the shop happened to be doing.
     *
     * <h2>Not made redundant by {@link #onShopSignOpen}</h2>
     * A right-click on a sign does more than open an editor. Dye and a glow ink
     * sac recolour and light a sign's text without changing a character of it,
     * and they fire no sign event of any kind - measured, not reasoned about. So
     * a shop sign guarded only by the sign-open event is a shop sign anyone can
     * repaint. The two handlers answer different questions and both stay.
     *
     * <h2>Priority, which is the whole of the care this needs</h2>
     * {@code HIGHEST}, and it has to be: {@code ShopTradeListener} runs at
     * {@code LOW} and returns immediately when the block result is already
     * {@code DENY}. Denying before it would stop every shop on the server from
     * trading while looking like it had only shut a sign. Running after it means
     * the trade has already happened, and this only closes what the trade
     * listener left open.
     *
     * <h2>The block result, not setCancelled</h2>
     * Cancelling a {@link PlayerInteractEvent} denies the item in hand as well,
     * which would stop a player placing a block against a shop sign. What opens
     * a sign editor is the block's own interaction, so that is the only half
     * denied here.
     *
     * <p>Right-clicks only. A left-click on a sign is a break, and breaking is
     * {@link #onBlockBreak}'s decision rather than this one's.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShopSignInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.useInteractedBlock() == Event.Result.DENY) return;

        if (ShopType.isShop(e.getClickedBlock())) e.setUseInteractedBlock(Event.Result.DENY);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {

        if (event.isCancelled())
            return;

        Block block = event.getBlock();

        if (!plugin.getListManager().isInventory(block))
            return;

        Sign shopSign = new Utils().findShopSign(block);

        if (shopSign == null)
            return;

        Shop shop = Shop.loadShop(shopSign);

        if (shop.getShopType().isITrade())
            return;

        if (shop.getUsersUUID(ShopRole.OWNER, ShopRole.MANAGER).contains(event.getPlayer().getUniqueId())) {
            if (!shop.hasStorage()) {
                shop.setInventoryLocation(block.getLocation());
                shop.saveShop();
            }
        } else {
            event.setCancelled(true);
        }
    }
}