package org.shanerx.tradeshop.harness;

import be.seeseemelk.mockbukkit.ServerMock;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link ServerMock} with the gaps TradeShop happens to fall into filled in.
 *
 * <p>MockBukkit reports API it has not covered by throwing
 * {@code UnimplementedOperationException}, which extends JUnit's
 * {@code TestAbortedException} — so a plugin that touches such API has its test
 * <em>skipped</em> rather than failed. The build then reports success over a
 * suite that tested nothing. The POM turns skips into build failures, and every
 * override here exists because TradeShop tripped that wire.
 */
public class TradeShopServerMock extends ServerMock {

    /** {@code TradeShop.onEnable} ends in {@code aliasCheck("ts")}, which reads this map. */
    @Override
    public Map<String, String[]> getCommandAliases() {
        return new HashMap<>();
    }
}
