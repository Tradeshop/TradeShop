package org.shanerx.tradeshop.harness;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.TestAbortedException;
import org.shanerx.tradeshop.TradeShop;

/**
 * One mocked server and one loaded TradeShop for the whole JVM, shared by every
 * scenario, torn down when the last test class is done.
 *
 * <h2>Why once per JVM and not once per class</h2>
 * TradeShop resolves the running plugin into {@code static final} fields at class
 * initialisation:
 * <ul>
 *   <li>{@code ShopChest.PLUGIN}         — {@code TradeShop.getPlugin()}</li>
 *   <li>{@code ShopType.plugin}          — {@code Bukkit.getPluginManager().getPlugin("TradeShop")}</li>
 *   <li>{@code Setting.PLUGIN}, {@code Message.PLUGIN},
 *       {@code SettingSection.PLUGIN}, {@code MessageSection.PLUGIN},
 *       {@code Permissions.plugin} — the same</li>
 * </ul>
 * A class initialiser runs once per classloader. Surefire gives the whole suite
 * one classloader, so the second {@code MockBukkit.mock()} in a JVM leaves those
 * fields pointing at the plugin instance from the <em>first</em> one, along with
 * its {@code ListManager} and {@code DataStorage}, both built around a world that
 * {@code unmock()} has already thrown away.
 *
 * <p>Nothing announces itself when that happens. {@code ShopChest.getBlock()} asks
 * the stale {@code ListManager} whether the block is an inventory, gets false,
 * and leaves its {@code chest} field null; {@code ShopChest.getInventory()}
 * swallows the resulting NPE and returns null; and
 * {@code Shop.updateFullTradeCount} dereferences that null. That is the
 * NullPointerException on {@code shopInventory} that a previous attempt worked
 * around with a JVM per test class.
 *
 * <p>Mocking once sidesteps all of it without touching the plugin: the fields are
 * initialised against the only plugin instance there will ever be, and they stay
 * correct. Scenarios are isolated from each other by a fresh world each instead —
 * see {@link ShopScenario}.
 */
public final class MockBukkitEnvironment implements BeforeAllCallback {

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(MockBukkitEnvironment.class);

    private static Session session;

    @Override
    public void beforeAll(ExtensionContext context) {
        // The root store is closed once, after the last test class in the JVM.
        context.getRoot().getStore(NAMESPACE)
                .getOrComputeIfAbsent(Session.class, key -> new Session(), Session.class);
    }

    public static ServerMock server() {
        return require().server;
    }

    public static TradeShop plugin() {
        return require().plugin;
    }

    private static Session require() {
        if (session == null) {
            throw new IllegalStateException(
                    "MockBukkit is not running. Annotate the test class with @ExtendWith("
                            + MockBukkitEnvironment.class.getSimpleName() + ".class).");
        }
        return session;
    }

    /** Held in JUnit's root store so JUnit closes it when the JVM's last test finishes. */
    static final class Session implements ExtensionContext.Store.CloseableResource {

        private final ServerMock server;
        private final TradeShop plugin;

        Session() {
            try {
                server = MockBukkit.mock(new TradeShopServerMock());
                plugin = MockBukkit.load(TradeShop.class);
            } catch (TestAbortedException aborted) {
                // Startup reached unimplemented MockBukkit API. Aborting here would
                // skip every test in the JVM and still exit 0; the interceptor cannot
                // see this because it is an extension callback, not a test method.
                MockBukkit.unmock();
                throw SkippedTestsAreFailures.asFailure(aborted);
            }
            session = this;
        }

        @Override
        public void close() {
            session = null;
            MockBukkit.unmock();
        }
    }
}
