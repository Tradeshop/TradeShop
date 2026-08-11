package org.shanerx.tradeshop.data.storage;

import be.seeseemelk.mockbukkit.WorldMock;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.junit.jupiter.api.Test;
import org.shanerx.tradeshop.data.storage.Json.JsonShopData;
import org.shanerx.tradeshop.harness.MockBukkitEnvironment;
import org.shanerx.tradeshop.harness.ShopScenario;
import org.shanerx.tradeshop.player.ShopRole;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shoplocation.ShopLocation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A shop somebody has shared, saved and read back.
 *
 * <h2>The defect</h2>
 * {@code Shop.deserialize} read {@code managers} and {@code members} with
 * {@code data.getSerializableList(key, UUID.class)}. That call maps every element
 * of the stored list through {@code SimplixSerializer.deserialize(element,
 * UUID.class)}, which looks the target class up in a registry TradeShop never
 * registers {@code UUID} in, so it threw
 *
 * <pre>
 * de.leonhard.storage.internal.exceptions.SimplixValidationException: No serializable found for 'UUID'
 *   at de.leonhard.storage.internal.serialize.SimplixSerializer.deserialize(SimplixSerializer.java:61)
 *   at de.leonhard.storage.internal.DataStorage.getSerializableList(DataStorage.java:273)
 *   at org.shanerx.tradeshop.shop.Shop.deserialize(Shop.java:274)
 * </pre>
 *
 * on any list with something in it. An empty one never enters the mapping
 * function, which is the only reason a suite whose every fixture is an unshared
 * shop stayed green over it. The lookup is by the TARGET class, so it fails the
 * same way whatever the stored element happens to be - which is why both read
 * paths below were red rather than one.
 *
 * <h2>Why three rows and not one</h2>
 * A shop is read back over two paths that hold different objects, and a defect
 * has already been through this layer that was invisible on one of them:
 *
 * <ul>
 *   <li><b>Off disk</b> - a fresh {@link DataStorage} has an empty shop cache and
 *       an empty chunk cache, so it parses the JSON file. There the list is an
 *       array of strings.</li>
 *   <li><b>Out of memory</b> - the {@link JsonShopData} the save went through
 *       still holds the very map {@code Shop.serialize()} handed it, and that map's
 *       list holds {@code java.util.UUID} objects. Every reader of that chunk is
 *       answered out of it until the file is re-read.</li>
 * </ul>
 *
 * <p>Both are asserted, and the reader has to accept both element types for both
 * to pass.
 */
class ShopUsersSurviveStorageTest extends ShopScenario {

    private static final UUID MANAGER = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final UUID MEMBER = UUID.fromString("66666666-7777-8888-9999-aaaaaaaaaaaa");

    @Test
    void aShopWithAManagerComesBackOffDisk() {
        ShopLocation where = shopSharedWith(MANAGER, ShopRole.MANAGER);

        Shop reloaded = new DataStorage(DataType.FLATFILE).loadShopFromSign(where);

        assertNotNull(reloaded, "a shop with a manager should still be on disk");
        assertTrue(reloaded.getUsersUUID(ShopRole.MANAGER).contains(MANAGER),
                "the manager an owner added must come back when the shop is read off disk. Every "
                        + "shop anyone has ever shared takes this path on every server start");
    }

    @Test
    void aShopWithAMemberComesBackOffDisk() {
        ShopLocation where = shopSharedWith(MEMBER, ShopRole.MEMBER);

        Shop reloaded = new DataStorage(DataType.FLATFILE).loadShopFromSign(where);

        assertNotNull(reloaded, "a shop with a member should still be on disk");
        assertTrue(reloaded.getUsersUUID(ShopRole.MEMBER).contains(MEMBER),
                "and the member side is read by the same call as the manager side, so it fails and "
                        + "is fixed with it");
    }

    /**
     * The other read path: the live document, whose list still holds {@code UUID}
     * objects rather than the strings the file holds.
     */
    @Test
    void aShopWithAManagerComesBackOutOfTheLiveDocument() {
        ShopLocation where = shopSharedWith(MANAGER, ShopRole.MANAGER);

        JsonShopData live = (JsonShopData) MockBukkitEnvironment.plugin().getDataStorage()
                .getShopData(signBlock.getChunk());
        Shop reloaded = live.load(where);

        assertNotNull(reloaded, "the chunk the save went through should still hold the shop");
        assertTrue(reloaded.getUsersUUID(ShopRole.MANAGER).contains(MANAGER),
                "the manager must come back out of the storage layer's own in-memory document too. "
                        + "That document holds the ArrayList<UUID> Shop.serialize put in it, not the "
                        + "array of strings the file holds, and DataStorage.getMatchingShopsInChunk "
                        + "reads shops out of it - which is /tradeshop find");
    }

    /**
     * A file in the form on disk today, loaded by the build that has to keep
     * reading it.
     *
     * <p>The fixture is a real artefact rather than a hand-written approximation:
     * a chunk file written by {@code Shop.serialize} as it stands, captured before
     * the reader was changed, with one manager and one member on the shop. The
     * write side is not touched by the fix - a {@code List<UUID>} already renders
     * as an array of strings, because the JSON writer answers {@code toString()}
     * for anything in a {@code java.*} package - so there is no migration and this
     * row is what says so.
     */
    @Test
    void aShopFileInTodaysOnDiskFormStillLoadsItsSharedUsers() throws IOException {
        WorldMock world = MockBukkitEnvironment.server().addSimpleWorld("sharedshop");

        Block chest = world.getBlockAt(0, 64, 0);
        chest.setType(Material.CHEST);
        Block sign = world.getBlockAt(0, 65, 0);
        sign.setType(Material.OAK_SIGN);

        Path chunkFile = Path.of(JsonShopData.getPath("sharedshop"), "c;;sharedshop;;0;;0.json");
        Files.createDirectories(chunkFile.getParent());
        try (InputStream fixture = getClass().getResourceAsStream("/legacy/shop-with-shared-users.json")) {
            assertNotNull(fixture, "the fixture this row is about should be on the test classpath");
            Files.copy(fixture, chunkFile, StandardCopyOption.REPLACE_EXISTING);
        }

        Shop stored = new DataStorage(DataType.FLATFILE).loadShopFromSign(new ShopLocation(sign.getLocation()));

        assertNotNull(stored, "a shop file already on disk must still load");
        assertEquals(1, stored.getUsersUUID(ShopRole.MANAGER).size(),
                "the file's one manager should be read back");
        assertTrue(stored.getUsersUUID(ShopRole.MANAGER).contains(MANAGER),
                "and it should be the manager the file names");
        assertTrue(stored.getUsersUUID(ShopRole.MEMBER).contains(MEMBER),
                "and the member the same file names");
    }

    /** A shop the owner has shared with one other player, saved through the running plugin. */
    private ShopLocation shopSharedWith(UUID other, ShopRole role) {
        createShop("1 DIAMOND", "1 EMERALD");

        ShopLocation where = new ShopLocation(signBlock.getLocation());
        Shop shop = Shop.loadShop(where);
        assertNotNull(shop, "precondition: writing the sign created a shop");

        assertTrue(shop.addUser(other, role), "precondition: the owner could add the " + role);

        return where;
    }
}
