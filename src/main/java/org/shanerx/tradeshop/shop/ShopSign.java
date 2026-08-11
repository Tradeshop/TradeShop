/*
 *
 *                         Copyright (c) 2016-2023
 *                SparklingComet @ http://shanerx.org
 *               KillerOfPie @ http://killerofpie.github.io
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
 *
 *  NOTICE: All modifications made by others to the source code belong
 *  to the respective contributor. No contributor should be held liable for
 *  any damages of any kind, whether be material or moral, which were
 *  caused by their contribution(s) to the project. See the full License for more information.
 *
 */

package org.shanerx.tradeshop.shop;

import com.bergerkiller.bukkit.common.utils.MaterialUtil;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.utils.debug.DebugLevels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Which block materials TradeShop will accept a shop sign on, asked of the
 * running server rather than listed here.
 *
 * <h2>What this used to be, and what it cost</h2>
 * A twelve-constant enum, each entry carrying the Minecraft version it appeared
 * in, expanded at startup into standing, wall and hanging variants by string
 * surgery on the constant's name. Every new wood was an edit, and three defects
 * lived in it at once:
 *
 * <ul>
 *   <li>the hanging wall variant was built as {@code <WOOD>_HANGING_WALL_SIGN};
 *       the material is {@code <WOOD>_WALL_HANGING_SIGN}, so
 *       {@code matchMaterial} returned null, nulls entered the list, and no
 *       wall-hanging sign was ever recognised as a shop;</li>
 *   <li>{@code PALE_OAK} was never added, so pale oak signs could not be
 *       shops;</li>
 *   <li>the reverse lookup used {@code Enum.valueOf} on names such as
 *       {@code OAK_HANGING_SIGN} that were never constants, so a hanging sign
 *       that <em>was</em> registered threw when the shop tried to write it.</li>
 * </ul>
 *
 * <h2>Where the answer comes from now</h2>
 * Two sources, unioned, neither of them a list in this file:
 *
 * <ul>
 *   <li>{@link MaterialUtil#ISSIGN} - BKCommonLib's sign property, preprocessed
 *       against the server it is running on. This is the source the plugin is
 *       meant to lean on: it absorbs a per-version job so this plugin does not
 *       have to ship an update for it.</li>
 *   <li>Bukkit's own block tags. These are the server's registry, so they are
 *       correct by construction, and unlike the library they are readable
 *       without BKCommonLib having bootstrapped.</li>
 * </ul>
 *
 * The union is deliberate rather than a fallback chain. {@code MaterialUtil}
 * cannot be class-initialised unless BKCommonLib has bootstrapped against a real
 * server's internals - under MockBukkit its first touch throws
 * {@code NoClassDefFoundError: Could not initialize class
 * com.bergerkiller.bukkit.common.Common} - so a library-only answer would take
 * the whole tier-1 suite down with it. Taking both means a source that is
 * unavailable, or older than the server, can only ever narrow what it
 * contributes; it can never remove a sign type another source knows about.
 */
public class ShopSign {

    /**
     * The colour a sign's text defaults to on every wood but one. Dark oak is
     * dark enough that black text is unreadable on it.
     *
     * <p>This is the whole of what is still maintained by hand here, and it is
     * an exception table rather than a catalogue: a new wood needs an entry only
     * if black is the wrong default for it. The keys are config keys, so they
     * are the lower-case dashed form {@link #colourKey(Material)} produces.
     */
    private static final String DEFAULT_COLOUR = "&0";
    private static final Map<String, String> COLOUR_EXCEPTIONS =
            Collections.singletonMap("dark-oak-sign", "&f");

    private final List<Material> signTypes;

    public ShopSign() {
        signTypes = resolveSignTypes();

        TradeShop plugin = TradeShop.getPlugin();
        plugin.getDebugger().log(String.format("Recognising %d sign material(s): %s",
                signTypes.size(), signTypes), DebugLevels.STARTUP);
    }

    public List<Material> getSignTypes() {
        return signTypes;
    }

    /**
     * The config key a sign material's default colour is stored under.
     *
     * <p>One key per wood: the wall and hanging variants of a wood share the
     * colour of its standing sign, which is what the old enum expressed by only
     * ever having standing signs in it. Pure string work on a name that came out
     * of the material registry, so unlike the {@code valueOf} it replaces there
     * is no input it can throw on.
     */
    public static String colourKey(Material signMaterial) {
        return signMaterial.name()
                .replace("WALL_", "")
                .replace("HANGING_", "")
                .toLowerCase()
                .replace("_", "-");
    }

    /**
     * The colour a shop's sign text is written in, from the operator's config.
     *
     * <p>Null-tolerant on purpose. The map is seeded from the same materials
     * {@link #getSignTypes()} resolves, so every recognised sign has a key - but
     * the config is a file an operator edits, and a missing key must give a
     * readable sign rather than the literal text {@code null} written onto it.
     */
    public static String getDefaultColour(Material signMaterial) {
        String configured = Setting.SHOP_SIGN_DEFAULT_COLOURS.getMappedString(colourKey(signMaterial));
        return configured != null ? configured : DEFAULT_COLOUR;
    }

    /**
     * Seeds {@code SHOP_SIGN_DEFAULT_COLOURS} with one entry per wood the server
     * has signs for.
     *
     * <p>Resolves the sign types itself instead of reading them off the plugin:
     * this runs from {@code Setting}'s class initialiser, which is reached
     * before {@code TradeShop.onEnable} builds the {@code ShopSign} instance.
     */
    public static Map<String, String> getDefaultColourMap() {
        Map<String, String> colourMap = new TreeMap<>();

        for (Material signType : resolveSignTypes()) {
            String key = colourKey(signType);
            colourMap.put(key, COLOUR_EXCEPTIONS.getOrDefault(key, DEFAULT_COLOUR));
        }

        return colourMap;
    }

    private static List<Material> resolveSignTypes() {
        Set<Material> types = new LinkedHashSet<>();

        addLibrarySigns(types);

        // ALL_SIGNS covers the four families on a 1.20+ server in one tag. The
        // narrower three are asked for as well because a server older than the
        // API this is compiled against simply will not have the wider tag, and
        // Bukkit reports that as a null constant rather than an error.
        addTag(types, Tag.ALL_SIGNS);
        addTag(types, Tag.SIGNS);
        addTag(types, Tag.WALL_SIGNS);
        addTag(types, Tag.ALL_HANGING_SIGNS);

        return new ArrayList<>(types);
    }

    private static void addLibrarySigns(Set<Material> types) {
        try {
            types.addAll(MaterialUtil.ISSIGN.getMaterials());
        } catch (Throwable notBootstrapped) {
            // An Error, not an Exception: BKCommonLib builds its material tables
            // out of the server's internals during class initialisation, so
            // anywhere those internals are absent the failure arrives as
            // NoClassDefFoundError from the first field access. The tags below
            // are the whole answer in that case, which is what keeps the plugin
            // testable off a real server.
        }
    }

    private static void addTag(Set<Material> types, Tag<Material> tag) {
        if (tag == null) {
            return;
        }

        Collection<Material> values = tag.getValues();
        if (values != null) {
            types.addAll(values);
        }
    }
}
