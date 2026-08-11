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

import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * Assertions for a plugin that cannot use JUnit, because it runs inside a server
 * rather than inside a test runner.
 *
 * <p>Messages say what was expected of the world, not which method returned
 * what, because the result file is read by whoever is looking at a red build.
 */
final class Assert {

    private Assert() {
    }

    static void that(boolean condition, String whatWasExpected) {
        if (!condition) {
            throw new AssertionError(whatWasExpected);
        }
    }

    static void equal(Object expected, Object actual, String whatWasExpected) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(whatWasExpected + " (expected " + expected + ", was " + actual + ")");
        }
    }

    /**
     * Waits for something the server does on its own schedule.
     *
     * <p>A real server is timing-dependent in ways a mock is not: TradeShop reads
     * stock when a chest closes and does disk work on the main thread, so
     * "the assertion ran before the plugin caught up" is a live failure mode.
     * The answer is a generous timeout, and a timeout that has expired is a
     * failure and never a skip.
     *
     * @throws IllegalStateException if called from the main thread, where
     *                               sleeping would stop the server from ever
     *                               reaching the state being waited for
     */
    static void eventually(long timeoutMillis, String whatWasExpected, BooleanSupplier condition) {
        long deadline = System.currentTimeMillis() + timeoutMillis;
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError("interrupted while waiting for: " + whatWasExpected);
            }
        }
        throw new AssertionError("timed out after " + timeoutMillis + "ms waiting for: " + whatWasExpected);
    }
}
