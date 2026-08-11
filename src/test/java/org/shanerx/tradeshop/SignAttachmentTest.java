package org.shanerx.tradeshop;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.junit.jupiter.api.Test;
import org.shanerx.tradeshop.harness.ShopScenario;
import org.shanerx.tradeshop.shop.listeners.ShopProtectionListener;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Which block an explosion has to be kept away from to leave a shop sign
 * standing.
 *
 * <p>Getting this wrong is silent: the shop is protected, the sign's support is
 * not, the explosion takes the support, and the sign pops off as an item. The
 * test that used to make the decision was
 * {@code getType().toString().contains("WALL_SIGN")}.
 *
 * <h2>What this tier cannot say</h2>
 * The case that was broken is the wall-hanging sign, and MockBukkit will not
 * build a block state for one. The rows for both hanging mountings are in
 * {@code it/IntegrationPlugin}, on a real 1.21.11 server. What is here is the
 * two mountings the mock can represent, so that fixing the third did not quietly
 * break them.
 */
class SignAttachmentTest extends ShopScenario {

    @Test
    void aStandingSignIsHeldUpByTheBlockBelowIt() {
        signBlock.setType(Material.OAK_SIGN);

        List<Block> supports = ShopProtectionListener.supportingBlocks(signBlock);

        assertEquals(1, supports.size(), "a standing sign rests on one block");
        assertEquals(signBlock.getRelative(BlockFace.DOWN), supports.get(0));
    }

    @Test
    void aWallSignIsHeldUpByTheBlockItFacesAwayFrom() {
        signBlock.setType(Material.OAK_WALL_SIGN);
        BlockFace facing = ((Directional) signBlock.getBlockData()).getFacing();

        List<Block> supports = ShopProtectionListener.supportingBlocks(signBlock);

        assertEquals(1, supports.size(), "an ordinary wall sign hangs off one block");
        assertEquals(signBlock.getRelative(facing.getOppositeFace()), supports.get(0),
                "the wall is behind the sign, opposite the way its text points");
        assertTrue(supports.get(0).getY() == signBlock.getY(),
                "and it is beside the sign, not under it - which is where the old test sent it "
                        + "for every mounting it did not recognise");
    }
}
