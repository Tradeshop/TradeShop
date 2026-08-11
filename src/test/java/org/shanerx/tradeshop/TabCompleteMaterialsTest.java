package org.shanerx.tradeshop;

import org.junit.jupiter.api.Test;
import org.shanerx.tradeshop.harness.MockBukkitEnvironment;
import org.shanerx.tradeshop.harness.ShopScenario;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What {@code /tradeshop setProduct <tab>} offers.
 *
 * <p>The list used to be every {@code Material} constant minus a sixty-nine
 * entry blocklist written by hand, and the blocklist had gone stale: its wall
 * signs stopped at {@code WARPED_WALL_SIGN}, so everything added since was still
 * being offered as something a shop could trade. It is now the complement of a
 * live question - "does this material have an item form" - which cannot go
 * stale.
 *
 * <h2>Which implementation this tier sees</h2>
 * The library answer, {@code ItemUtil.getItemTypes()}, reads the server's item
 * registry and cannot be class-initialised without BKCommonLib bootstrapped
 * against a real server, so what runs here is the {@code Material.isItem()}
 * fallback. The assertions are about the list a player is shown, and both
 * implementations owe the same answers; the real server's registry is asserted
 * at tier 2, in {@code IntegrationPlugin}.
 */
class TabCompleteMaterialsTest extends ShopScenario {

    private List<String> gameMats() {
        return MockBukkitEnvironment.plugin().getListManager().getGameMats();
    }

    @Test
    void aMaterialThatCanBeHeldIsStillOffered() {
        assertTrue(gameMats().contains("DIAMOND"), "a diamond is the plainest tradeable item there is");
        assertTrue(gameMats().contains("OAK_SIGN"), "a sign is an item and can be traded");
        assertTrue(gameMats().size() > 500,
                "the list should still be most of the game, it was " + gameMats().size());
    }

    /**
     * The blocklist's wall signs stopped at {@code WARPED_WALL_SIGN}. Every
     * mounting added after that - eleven woods of wall-hanging sign, and the
     * wall signs of the woods added since 1.19 - was offered for trade despite
     * having no item form at all.
     */
    @Test
    void aBlockWithNoItemFormIsNotOffered() {
        assertFalse(gameMats().contains("OAK_WALL_HANGING_SIGN"),
                "a wall-hanging sign has no item form, and the blocklist never learned about it");
        assertFalse(gameMats().contains("CHERRY_WALL_SIGN"),
                "nor does a cherry wall sign, added after the blocklist stopped being updated");
        assertFalse(gameMats().contains("OAK_WALL_SIGN"),
                "the wall signs the blocklist did know about are still excluded");
        assertFalse(gameMats().contains("WATER"),
                "and so is water, which the blocklist never mentioned either");
    }

    /**
     * The pre-1.13 name table is in the {@code Material} enum and in no
     * registry. The blocklist did not exclude it, so four hundred and sixty-three
     * dead names were on offer.
     */
    @Test
    void legacyMaterialNamesAreNotOffered() {
        for (String offered : gameMats()) {
            assertFalse(offered.startsWith("LEGACY_"),
                    "the pre-1.13 name table is not tradeable: " + offered);
        }
    }
}
