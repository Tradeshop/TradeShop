package org.shanerx.tradeshop;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.shanerx.tradeshop.harness.MockBukkitEnvironment;
import org.shanerx.tradeshop.harness.ShopScenario;
import org.shanerx.tradeshop.shop.ShopSign;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Which signs a shop can be put on.
 *
 * <p>Every row here failed before the hand-maintained sign catalogue was
 * replaced by the server's own answer, and each names the way it failed.
 *
 * <h2>What this tier cannot say, and what says it instead</h2>
 * Two of the three defects can only be finished off on a real server, and the
 * rows that do it are in {@code it/IntegrationPlugin} rather than weakened into
 * something this tier can pass:
 *
 * <ul>
 *   <li><b>Pale oak.</b> MockBukkit's material set is the one its 1.21.1 line
 *       ships and pale oak arrived in 1.21.2, so
 *       {@code Material.matchMaterial("PALE_OAK_SIGN")} is null here. A pale oak
 *       row at this tier would pass by not testing anything.</li>
 *   <li><b>A shop on a hanging sign.</b> MockBukkit refuses to build a block
 *       state for one - {@code IllegalArgumentException: Cannot create a
 *       SignMock from OAK_HANGING_SIGN} - so the create-and-trade flow cannot be
 *       driven over a hanging sign at all. What it recognises, and the sign
 *       colour lookup that used to throw on those materials, are both reachable
 *       and are asserted below.</li>
 * </ul>
 */
class ShopSignTypeTest extends ShopScenario {

    /**
     * The catalogue this replaces built {@code <WOOD>_HANGING_WALL_SIGN}, which
     * is not a Bukkit material - the name is {@code <WOOD>_WALL_HANGING_SIGN}.
     * {@code Material.matchMaterial} returned null for all twenty of them and
     * the nulls were added to the recognised list anyway.
     */
    @Test
    void everyRecognisedSignIsARealMaterial() {
        List<Material> signTypes = MockBukkitEnvironment.plugin().getSigns().getSignTypes();

        assertFalse(signTypes.isEmpty(), "the plugin recognises no sign materials at all");
        assertFalse(signTypes.contains(null),
                "a null in the recognised list is a material name that matched nothing: " + signTypes);
    }

    @Test
    void wallHangingSignsAreRecognisedAsSomewhereAShopCanGo() {
        List<Material> signTypes = MockBukkitEnvironment.plugin().getSigns().getSignTypes();

        assertTrue(signTypes.contains(Material.OAK_WALL_HANGING_SIGN),
                "wall-hanging signs were never recognised, because the name the catalogue "
                        + "built for them matched no material");
        assertTrue(signTypes.contains(Material.OAK_HANGING_SIGN),
                "ceiling hanging signs should be recognised too");
        assertTrue(signTypes.contains(Material.OAK_WALL_SIGN),
                "and the ordinary wall sign, which always worked, still does");
    }

    /**
     * Two things at once, and they are the same call.
     *
     * <p>The per-wood colours are the one thing here still written by hand, and
     * deleting the enum must not have taken them with it. And the lookup that
     * reaches them is the one that used to throw: it was
     * {@code Signs.valueOf(name.replace("WALL_", ""))}, and
     * {@code OAK_HANGING_SIGN} was never one of the enum's constants - so a
     * hanging sign that had been registered as a shop sign threw
     * {@code IllegalArgumentException} the moment the shop wrote its sign. That
     * is the call below, on exactly those materials.
     */
    @Test
    void everyWoodKeepsItsDefaultSignColourIncludingTheMountingsThatUsedToThrow() {
        assertEquals("&0", ShopSign.getDefaultColour(Material.OAK_SIGN),
                "black is the default on every wood but one");
        assertEquals("&f", ShopSign.getDefaultColour(Material.DARK_OAK_SIGN),
                "dark oak is the exception, and it is the reason the map exists");
        assertEquals("&f", ShopSign.getDefaultColour(Material.DARK_OAK_WALL_HANGING_SIGN),
                "a wall-hanging dark oak sign is still dark oak, and asking used to throw");
        assertEquals("&0", ShopSign.getDefaultColour(Material.OAK_HANGING_SIGN),
                "a ceiling hanging sign is the other name the old lookup had no constant for");
        assertEquals("dark-oak-sign", ShopSign.colourKey(Material.DARK_OAK_WALL_SIGN),
                "wall and hanging variants share the standing sign's config key");
    }
}
