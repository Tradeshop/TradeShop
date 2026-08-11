package org.shanerx.tradeshop.data.storage;

import org.bukkit.Chunk;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.shanerx.tradeshop.harness.MockBukkitEnvironment;
import org.shanerx.tradeshop.shoplocation.ShopChunk;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * One chunk file, one live view of it.
 *
 * <h2>What {@code chunkDataCache} is for</h2>
 * {@code JsonShopData} is not a handle onto a file, it is a copy of one: it reads
 * the whole chunk into memory when it is constructed and writes the whole of that
 * memory back on every save. Two of them over the same file are therefore two
 * copies of the same shops, and a save through either writes its own copy over
 * whatever the other one put there. That is why {@code DataStorage} keeps a cache
 * of them at all, and it is the same reason {@code shopCache} exists one level up -
 * "One live object per shop, which is what the rest of the plugin assumes when it
 * loads a shop, changes it and saves it" ({@code DataStorage.java:229-232}).
 *
 * <h2>Why this is a test and not a code reading</h2>
 * The miss path caches one object and returns a different one
 * ({@code DataStorage.java:347-349}), so the cache never hands back the instance
 * the caller that filled it was using. Reading that off the source is easy; what a
 * test adds is that it stays true, because the damage it does is not easy to read
 * at all. The library underneath reloads a stale copy from disk only when the
 * file's modification time has moved past the millisecond the copy was taken -
 * {@code FlatFile.shouldReload} on {@code ReloadSettings.INTELLIGENT}, measured
 * from the shaded 3.2.7 jar - so whether a lost write shows up depends on how
 * quickly the two saves follow each other. A shop vanishing on a fast disk and not
 * on a slow one is exactly the shape of {@code aShopReadBackFromDiskStillTakesItsCost},
 * which was found as a {@code cost: []} in a file rather than by anyone reasoning
 * about it.
 *
 * <p>So the assertion is the invariant rather than the symptom: two asks for one
 * chunk answer one object. An invariant fails the same way on every machine.
 */
@ExtendWith(MockBukkitEnvironment.class)
class ChunkShopDataCacheTest {

    @Test
    void twoAsksForOneChunkAnswerTheSameLiveView() {
        DataStorage storage = MockBukkitEnvironment.plugin().getDataStorage();
        Chunk chunk = MockBukkitEnvironment.server().addSimpleWorld("chunk-cache").getChunkAt(0, 0);

        ShopConfiguration first = storage.getShopData(new ShopChunk(chunk));
        ShopConfiguration second = storage.getShopData(new ShopChunk(chunk));

        assertSame(first, second,
                "a chunk's shop data must be one object however many times it is asked for. "
                        + "DataStorage.java:347-349 puts one JsonShopData in the cache and returns a "
                        + "second one built from the same file, so the caller that filled the cache and "
                        + "every caller after it are holding different copies of the same shops - and a "
                        + "save through either writes its whole copy back over the other's");
    }

    /**
     * And the cache still lets go, because the leak this is next to would be worse
     * than the defect: {@code dropShopData} is how a chunk stops being held once
     * nothing is using it.
     */
    @Test
    void droppingAChunkGivesTheNextAskAFreshView() {
        DataStorage storage = MockBukkitEnvironment.plugin().getDataStorage();
        Chunk chunk = MockBukkitEnvironment.server().addSimpleWorld("chunk-cache-drop").getChunkAt(0, 0);

        ShopConfiguration held = storage.getShopData(new ShopChunk(chunk));
        storage.dropShopData(new ShopChunk(chunk));

        org.junit.jupiter.api.Assertions.assertNotSame(held, storage.getShopData(new ShopChunk(chunk)),
                "a dropped chunk must be re-read rather than handed back out of the cache");
    }
}
