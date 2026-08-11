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

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Running a piece of world access on the server thread from a scenario that is
 * not on it.
 *
 * <p>Every scenario in this harness runs off the main thread, because a
 * scenario's job is partly to wait - TradeShop reads stock on
 * {@code InventoryCloseEvent} and does disk work on the main thread - and
 * waiting on the main thread is waiting for yourself. Bukkit refuses world
 * access from anywhere else, loudly, which is the right behaviour, so every
 * touch is marshalled back through here.
 *
 * <p>Extracted rather than duplicated: the tier-2 scenes and the tier-3 client
 * phase both need it, and two copies of a thread-marshalling helper is two
 * chances to fix a deadlock in only one of them.
 */
final class Sync {

    /**
     * Long enough that a slow disk write is not reported as a stuck server, short
     * enough that a genuinely stuck server is reported rather than hung on.
     */
    private static final long TIMEOUT_SECONDS = 30;

    private Sync() {
    }

    static <T> T get(Plugin plugin, Callable<T> body) {
        if (Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("scenarios must not run on the server thread: "
                    + "they wait for the server, and waiting on the server thread is a deadlock");
        }
        try {
            return Bukkit.getScheduler().callSyncMethod(plugin, body).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new AssertionError("the server did not run a scheduled task within "
                    + TIMEOUT_SECONDS + "s - it is stuck, and a timeout is a failure");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof AssertionError error) {
                throw error;
            }
            throw new AssertionError(cause.toString(), cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("interrupted while waiting for the server");
        }
    }

    static void run(Plugin plugin, Runnable body) {
        get(plugin, () -> {
            body.run();
            return null;
        });
    }
}
