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

import java.util.HashSet;
import java.util.Set;

/**
 * The one place every {@link RealShop}-consuming suite gets its patch of the
 * world from, so that "these two suites must not overlap" is something the
 * runtime refuses rather than something a comment merely claims.
 *
 * <h2>Why a comment was not enough</h2>
 * {@code IssueRows} and {@code SettingToggleRows} both once declared their own
 * {@code FIRST_SITE = 30}, each with a javadoc comment naming every other
 * suite's range as proof it was clear of them - and neither comment named the
 * other, because neither author had a reason to suspect they needed to. The
 * two suites stayed green only because of which one happened to finish
 * building at that address before the other started. Elsewhere in this
 * project a scene that landed on a site it did not own already turned that
 * kind of luck into a red run over nothing the code under test had done
 * wrong: it left blocks floating where {@code ClientPhase} expected bare
 * ground, and {@code getHighestBlockYAt} moved the client's site out from
 * under the bot. A range that has to be typed twice and agree by hand is a
 * range that will eventually disagree.
 *
 * <h2>How this closes it</h2>
 * A suite calls {@link #reserve} exactly once, naming itself and how many
 * sites it needs, and gets back a {@link Reservation} that is the only way to
 * turn an offset into a site index. Every reservation is cut from one
 * ever-advancing cursor, so two calls can no more overlap than two slices of
 * the same tape measure can - there is no step where a number is chosen that
 * might already be taken, which is what makes this an allocator and not a
 * second place to keep the same agreement. A suite that grows past the size
 * it originally reserved gets an {@link IllegalStateException} the moment it
 * asks for the offset it forgot to declare, rather than a shop quietly built
 * on top of whatever the next suite in line had already claimed there.
 *
 * <p>A third suite that skips this class and writes its own literal
 * {@code FIRST_SITE} again is not caught by anything here - Java has no way to
 * refuse an {@code int} for where it came from. What this class guarantees is
 * that a suite which asks it for a range can never be handed one that
 * collides with another suite that also asked; it cannot make asking
 * mandatory.
 */
final class SiteAllocator {

    private SiteAllocator() {
    }

    /**
     * Tier 3 does not go through {@link RealShop} at all - {@code ClientPhase}
     * places its own chest and sign directly, from {@code getHighestBlockYAt}
     * rather than from the fixed {@code CHEST_Y} every {@link RealShop} scene
     * uses - so it cannot call {@link #reserve} and the cursor below cannot
     * protect it. It is reserved by number instead, and both
     * {@code ClientPhase} and {@link RealShop} read this constant rather than
     * each carrying their own copy of "7".
     */
    static final int TIER_3_CLIENT = 7;

    /**
     * Every site {@code IntegrationPlugin} spends as a bare literal - 1 through
     * 52, a span that also contains {@link #TIER_3_CLIENT}. The highest of them
     * is 52, and this number must stay at or above it: the block exists to hold
     * those literals out of the cursor's reach, so a literal above it would be
     * handed out again to the next suite that reserves. Reserved wholesale
     * here, as one block, rather than as the exact sparse set actually used:
     * that file has no {@code FIRST_SITE} of its own to grow from, so the
     * headroom this costs - a handful of indices nothing will ever claim - is
     * cheaper than teaching this class about every literal in it.
     */
    private static final int LEGACY_TIER_2_SIZE = 52;

    private static int cursor = 1;
    private static final Set<String> claimed = new HashSet<>();

    static {
        reserve("IntegrationPlugin's literal tier-2 scenarios (sites 1-" + LEGACY_TIER_2_SIZE
                + ", which contains tier 3's site " + TIER_3_CLIENT + ")", LEGACY_TIER_2_SIZE);
    }

    /**
     * Reserves {@code size} consecutive site indices for {@code suiteName}.
     *
     * <p>Called once per suite, from a {@code private static final} field, so
     * that the reservation happens the moment anything first refers to the
     * suite's class - before its {@code rows(IntegrationPlugin)} runs, and
     * long before any scenario it builds touches the world.
     *
     * @throws IllegalArgumentException if {@code size} is not positive
     * @throws IllegalStateException if {@code suiteName} has already reserved
     *              a range - each suite is expected to call this exactly once
     */
    static synchronized Reservation reserve(String suiteName, int size) {
        if (size < 1) {
            throw new IllegalArgumentException(suiteName + " asked to reserve " + size
                    + " site(s), which is not a range at all");
        }
        if (!claimed.add(suiteName)) {
            throw new IllegalStateException("\"" + suiteName + "\" already reserved a site range - "
                    + "SiteAllocator.reserve must run exactly once per suite, from a field initializer, "
                    + "not from inside rows()");
        }

        int first = cursor;
        cursor += size;
        return new Reservation(suiteName, first, size);
    }

    /**
     * A suite's block of site indices, and the only way to turn an offset
     * within it into a real one.
     */
    static final class Reservation {

        private final String suiteName;
        private final int first;
        private final int size;

        private Reservation(String suiteName, int first, int size) {
            this.suiteName = suiteName;
            this.first = first;
            this.size = size;
        }

        /**
         * The site for the {@code offset}-th scene in this suite, 0-based.
         *
         * @throws IllegalStateException if {@code offset} falls outside the
         *              size this suite originally reserved - the suite grew a
         *              new scenario and the call to {@link SiteAllocator#reserve}
         *              was not updated to match it
         */
        int at(int offset) {
            if (offset < 0 || offset >= size) {
                throw new IllegalStateException("\"" + suiteName + "\" asked for site offset " + offset
                        + " but reserved only " + size + " site(s) starting at " + first
                        + " - bump the size passed to SiteAllocator.reserve(\"" + suiteName
                        + "\", ...) before adding another one");
            }
            return first + offset;
        }
    }
}
