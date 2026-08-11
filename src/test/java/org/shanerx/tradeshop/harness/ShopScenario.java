package org.shanerx.tradeshop.harness;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import be.seeseemelk.mockbukkit.inventory.SimpleInventoryViewMock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.shanerx.tradeshop.shop.ShopType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A chest with a TradeShop sign on it, and the moves a player would make to use
 * it, so that a test can say what it is checking instead of how to set it up.
 *
 * <h2>How scenarios stay out of each other's way</h2>
 * The server and the plugin are shared for the whole JVM — see
 * {@link MockBukkitEnvironment} for why they have to be. Isolation comes from each
 * test getting a brand new world instead: the plugin keys every shop, chest
 * linkage and protection entry by world name plus coordinates, so a world nobody
 * has used is a namespace nobody has written to, whatever the earlier tests left
 * in the data store.
 */
@ExtendWith(MockBukkitEnvironment.class)
public abstract class ShopScenario {

    private static final AtomicInteger SCENARIO = new AtomicInteger();
    private static final int CHEST_Y = 64;

    protected ServerMock server;

    protected Block chestBlock;
    protected Block signBlock;
    protected PlayerMock owner;

    private int scenario;

    @BeforeEach
    void placeChestAndSign() {
        server = MockBukkitEnvironment.server();
        scenario = SCENARIO.incrementAndGet();

        WorldMock world = server.addSimpleWorld("scenario" + scenario);

        chestBlock = world.getBlockAt(0, CHEST_Y, 0);
        chestBlock.setType(Material.CHEST);

        signBlock = world.getBlockAt(0, CHEST_Y + 1, 0);
        signBlock.setType(Material.OAK_SIGN);

        owner = addOperator("owner" + scenario);
    }

    /**
     * Swaps the sign for another of the server's sign materials.
     *
     * <p>Called before {@link #createShop}, so that a scenario can ask whether a
     * wood or a mounting the plugin has never been given a line of its own is
     * still recognised as somewhere a shop can go.
     */
    protected void useSignMaterial(Material signMaterial) {
        signBlock.setType(signMaterial);
    }

    /** Line 1 is the product the shop gives, line 2 the cost it takes. */
    protected void createShop(String product, String cost) {
        SignChangeEvent event = new SignChangeEvent(signBlock, owner,
                new String[]{ShopType.TRADE.toHeader(), product, cost, ""});
        server.getPluginManager().callEvent(event);
        applySignEdit(event);
    }

    /**
     * Copies the finished lines of a {@link SignChangeEvent} onto the sign block.
     *
     * <p>A real server does this itself once the event returns, which is how a
     * plugin's edits to a sign become visible to everything downstream. MockBukkit
     * does not, so without this the sign stays blank, nothing recognises it as a
     * shop, and the trade silently never happens while every call still succeeds.
     */
    private void applySignEdit(SignChangeEvent event) {
        Sign state = (Sign) signBlock.getState();
        for (int line = 0; line < 4; line++) {
            state.getSide(Side.FRONT).setLine(line, event.getLine(line));
        }
        state.update(true);
    }

    protected String[] signLines() {
        return ((Sign) signBlock.getState()).getSide(Side.FRONT).getLines();
    }

    protected void stockShop(ItemStack stock) {
        Container chest = (Container) chestBlock.getState();
        chest.getInventory().addItem(stock);
        chest.update();
    }

    /**
     * The owner shuts the chest, which is the moment the plugin re-reads its stock.
     *
     * <p>{@code ShopRestockListener} listens for {@link org.bukkit.event.inventory.InventoryCloseEvent},
     * so putting items in the chest is only half of stocking a shop — until the lid
     * closes, the sign still says out of stock.
     */
    protected void closeChestAsOwner() {
        InventoryView view = new SimpleInventoryViewMock(owner, chestInventory(),
                owner.getInventory(), InventoryType.CHEST);
        server.getPluginManager().callEvent(new InventoryCloseEvent(view));
    }

    /**
     * The chest's inventory, with the location a real server would give it.
     *
     * <p>{@code InventoryMock.getLocation()} is one of the calls MockBukkit has not
     * implemented, and it is the only thing {@code ShopRestockListener} wants from
     * the closed inventory — it finds the shop by asking the inventory where it is.
     * Everything else is the chest's real inventory, so nothing here can make the
     * shop look stocked when the chest is not.
     */
    private Inventory chestInventory() {
        Inventory real = ((Container) chestBlock.getState()).getInventory();
        Location location = chestBlock.getLocation();

        return (Inventory) Proxy.newProxyInstance(
                ShopScenario.class.getClassLoader(),
                new Class<?>[]{Inventory.class},
                (proxy, method, args) -> {
                    if ("getLocation".equals(method.getName()) && (args == null || args.length == 0)) {
                        return location;
                    }
                    try {
                        return method.invoke(real, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
    }

    protected PlayerMock buyerHolding(ItemStack held) {
        PlayerMock buyer = addOperator("buyer" + scenario);
        buyer.getInventory().addItem(held);
        return buyer;
    }

    private PlayerMock addOperator(String name) {
        PlayerMock player = server.addPlayer(name);
        player.setOp(true);
        return player;
    }

    protected void rightClickSign(PlayerMock player) {
        server.getPluginManager().callEvent(new PlayerInteractEvent(player,
                Action.RIGHT_CLICK_BLOCK, null, signBlock, BlockFace.NORTH));
    }

    protected int countOf(PlayerMock player, Material material) {
        return count(player.getInventory().getContents(), material);
    }

    protected int countInChest(Material material) {
        return count(((Container) chestBlock.getState()).getInventory().getContents(), material);
    }

    private int count(ItemStack[] contents, Material material) {
        int total = 0;
        for (ItemStack stack : contents) {
            if (stack != null && stack.getType() == material) {
                total += stack.getAmount();
            }
        }
        return total;
    }
}
