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

import de.leonhard.storage.sections.FlatFileSection;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.data.config.Setting;
import org.shanerx.tradeshop.item.ShopItemSide;
import org.shanerx.tradeshop.item.ShopItemStack;
import org.shanerx.tradeshop.player.PlayerSetting;
import org.shanerx.tradeshop.player.ShopRole;
import org.shanerx.tradeshop.player.ShopUser;
import org.shanerx.tradeshop.shoplocation.ShopChunk;
import org.shanerx.tradeshop.shoplocation.ShopLocation;
import org.shanerx.tradeshop.utils.Utils;
import org.shanerx.tradeshop.utils.debug.DebugLevels;
import org.shanerx.tradeshop.utils.objects.ObjectHolder;
import org.shanerx.tradeshop.utils.objects.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Shop {

    private final ShopLocation shopLoc;
    private transient TradeShop plugin = TradeShop.getPlugin();
    private ShopUser owner;
    private Set<UUID> managers, members;
    /**
     * -- GETTER --
     * Returns the type of the shop
     *
     * @return list of managers as ShopUser
     */
    @Getter
    private ShopType shopType;
    private List<ShopItemStack> product, cost;
    private ShopLocation chestLoc;
    private transient Utils utils = new Utils();
    private ShopStatus status = ShopStatus.INCOMPLETE;
    private Map<ShopSettingKeys, ObjectHolder<?>> shopSettings;
    /**
     * -- GETTER --
     * Returns the amount of trades the shop could do when last accessed
     *
     * @return amount of trades the shop can do
     */
    @Getter
    private int availableTrades = 0;
    private transient boolean aSync = false;

    /**
     * Creates a Shop object
     *
     * @param locations    Location of shop sign and chest as Tuple, left = Sign location, right = inventory location
     * @param owner        Owner of the shop as a ShopUser
     * @param shopType     Type of the shop as ShopType
     * @param productItems Product items to go into the shop
     * @param costItems    Cost items to go into the shop
     * @param players      Users to be added to the shop as Tuple, left = Managers, right = Members
     */
    public Shop(Tuple<Location, Location> locations, ShopType shopType, ShopUser owner, Tuple<Set<UUID>, Set<UUID>> players, List<ShopItemStack> productItems, List<ShopItemStack> costItems) {
        shopLoc = new ShopLocation(locations.getLeft());
        this.owner = owner;

        if (locations.getRight() != null) {
            chestLoc = new ShopLocation(locations.getRight());
            plugin.getDataStorage().addChestLinkage(chestLoc, shopLoc);
        }

        this.shopType = shopType;

        managers = players.getLeft() == null ? new HashSet<>() : players.getLeft();
        members = players.getRight() == null ? new HashSet<>() : players.getRight();

        product = productItems != null ? new ArrayList<>(productItems) : new ArrayList<>();
        cost = costItems != null ? new ArrayList<>(costItems) : new ArrayList<>();

        product.removeIf(shopItemStack -> shopItemStack.getItemStack() == null);
        cost.removeIf(shopItemStack -> shopItemStack.getItemStack() == null);

        fixAfterLoad();
    }

    /**
     * Creates a Shop object
     *
     * @param locations Location of shop sign and chest as Tuple, left = Sign location, right = inventory location
     * @param owner     Owner of the shop as a ShopUser
     * @param shopType  Type of the shop as ShopType
     * @param items     Items to go into the shop as Tuple, left = Product, right = Cost
     * @param players   Users to be added to the shop as Tuple, left = Managers, right = Members
     */
    public Shop(Tuple<Location, Location> locations, ShopType shopType, ShopUser owner, Tuple<Set<UUID>, Set<UUID>> players, Tuple<ItemStack, ItemStack> items) {
        this(locations, shopType, owner, players, Collections.singletonList(new ShopItemStack(items.getLeft())), Collections.singletonList(new ShopItemStack(items.getRight())));
    }

    /**
     * Creates a Shop object
     *
     * @param locations Location of shop sign and chest as Tuple, left = Sign location, right = inventory location
     * @param owner     Owner of the shop as a ShopUser
     * @param shopType  Type of the shop as ShopType
     */
    public Shop(Tuple<Location, Location> locations, ShopType shopType, ShopUser owner) {
        this(locations, shopType, owner, new Tuple<>(null, null), new Tuple<>(null, null));
    }

    /**
     * Creates a Shop object
     *
     * @param location Location of shop sign
     * @param owner    Owner of the shop as a ShopUser
     * @param shopType Type of the shop as ShopType
     */
    public Shop(Location location, ShopType shopType, ShopUser owner) {
        this(new Tuple<>(location, null), shopType, owner, new Tuple<>(null, null), new Tuple<>(null, null));
    }

    /**
     * Loads a shop from file and returns the Shop object
     *
     * @param loc Location of the shop sign
     * @return The shop from file
     */
    public static Shop loadShop(ShopLocation loc) {
        return TradeShop.getPlugin().getDataStorage().loadShopFromSign(loc);
    }

    /**
     * Loads the shop from file
     *
     * @param s Shop sign to load from
     * @return Shop Object
     */
    public static Shop loadShop(Sign s) {
        return loadShop(new ShopLocation(s.getLocation()));
    }

    /**
     * Returns the storage block as a ShopChest
     *
     * @return Shop storage as ShopChest
     */
    public ShopChest getChestAsSC() {
        ShopChest sc = new ShopChest(chestLoc.getLocation());
        return sc != null ? sc : new ShopChest(getInventoryLocation().getBlock(), getOwner().getUUID(), getShopLocation());
    }

    /**
     * Returns location of shops inventory
     *
     * @return inventory location as Location
     */
    public Location getInventoryLocation() {
        return chestLoc != null ? chestLoc.getLocation() : null;
    }

    /**
     * Sets the inventory location
     *
     * @param newLoc new location to set
     */
    public void setInventoryLocation(Location newLoc) {
        chestLoc = new ShopLocation(newLoc);
        plugin.getDataStorage().addChestLinkage(chestLoc, shopLoc);
    }

    /**
     * Returns the location of the shop sign
     *
     * @return Location of the shops sign
     */
    public Location getShopLocation() {
        return getShopLocationAsSL().getLocation();
    }

    /**
     * Sets the shops type
     *
     * @param newType new type as ShopType
     */
    public void setShopType(ShopType newType) {
        if (shopType != newType)
            setShopSettings(newType);

        shopType = newType;
    }

    /**
     * Deserializes the object to Json using Gson
     *
     * @param data Shop Map to be deserialized from file data
     * @return Shop object from file
     */
    public static Shop deserialize(FlatFileSection data) {
        Shop shop = new Shop(ShopLocation.deserialize(data.getMapParameterized("shopLoc")).getLocation(),
            ShopType.valueOf(data.getString("shopType")),
            ShopUser.deserialize(data.getMapParameterized("owner")));

        StringBuilder dataRemaining = new StringBuilder();

        // Nothing is written while the shop is being read, and that is load-bearing
        // rather than tidy. `data` is a LIVE VIEW of the file: the storage layer
        // re-reads it on every access once its modification time has moved. The loop
        // below used to move it - addSideItem ends at saveShop - so the half-built
        // shop was written over the file the loop was still reading, and every key
        // after the first side was answered out of that. The cost side came back
        // empty, the emptied shop was saved again, and the shop reloaded INCOMPLETE.
        //
        // aSync is the flag this class already uses for "not attached to the world,
        // do not touch disk", and loadASync sets it the moment deserialization
        // returns; it is simply set for the loop as well. Restored to false at the
        // end because that is the state the constructor left it in.
        shop.aSync = true;

        try {
            // singleLayerKeySet, NOT keySet. FlatFileSection.keySet() is the DEEP key set:
            // a shop whose file holds a nested "chestLoc" object answers "chestLoc.world",
            // "chestLoc.x", "chestLoc.y"... and never the bare "chestLoc" this switch is
            // written against. Every branch below whose value is an object or a list was
            // therefore dead, and the shop came back with no chest location, no settings and
            // - the one that costs a shop owner their items - an empty product and cost side.
            for (String key : data.singleLayerKeySet()) {
                switch (key) {
                    case "shopLoc":
                    case "shopType":
                    case "owner":
                        break; // Already used so skip
                    case "managers":
                        shop.managers = deserializeUsers(data, key);
                        break;
                    case "members":
                        shop.members = deserializeUsers(data, key);
                        break;
                    case "product":
                        deserializeSide(data, key).forEach((itm) -> shop.addSideItem(ShopItemSide.PRODUCT, itm));
                        break;
                    case "cost":
                        deserializeSide(data, key).forEach((itm) -> shop.addSideItem(ShopItemSide.COST, itm));
                        break;
                    case "chestLoc":
                        // Written as a nested object today and as a location string by older
                        // builds, so both are read. The object form goes through
                        // getMapParameterized rather than a cast of get(): the raw get()
                        // hands back the storage layer's own node type, which is not a
                        // java.util.Map, and only the typed getters convert it.
                        shop.chestLoc = data.get(key) instanceof String ?
                                ShopLocation.deserialize(data.get(key).toString()) :
                                ShopLocation.deserialize(data.getMapParameterized(key));
                        break;
                    case "status":
                        shop.status = ShopStatus.valueOf(data.get(key).toString());
                        break;
                    case "shopSettings":
                        shop.shopSettings = deserializeShopSettings(data.getMapParameterized(key));
                        break;
                    case "availableTrades":
                        shop.availableTrades = (int) data.get(key);
                        break;
                    default:
                        dataRemaining.append(key).append(": ").append(data.get(key)).append("\n");
                        break;
                }
            }
        } finally {
            shop.aSync = false;
        }

        if (dataRemaining.length() > 0) {
            TradeShop.getPlugin().getVarManager().getDebugger().log("Shop (" + shop.shopLoc + ") deserialized completed with data remaining: \n" + dataRemaining, DebugLevels.DATA_ERROR);
        }

        return shop;
    }

    /**
     * Reads a shop's managers or its members out of its file.
     *
     * <p>Both keys used to be read with {@code data.getSerializableList(key,
     * UUID.class)}, which maps every element of the stored list through
     * {@code SimplixSerializer.deserialize(element, UUID.class)}. That call looks
     * the <em>target</em> class up in a registry nothing in this plugin registers
     * {@code UUID} in, so it threw
     *
     * <pre>
     * SimplixValidationException: No serializable found for 'UUID'
     * </pre>
     *
     * for any list with something in it - and an empty list, which never enters the
     * mapping function at all, was the only case that worked. Every shop anyone had
     * shared was therefore a shop the server could not load: it stops trading, and
     * its owner's evidence is that it "just stopped".
     *
     * <h2>Why the parse is here rather than a registered serializable</h2>
     * Registering a {@code UUID} serializable with SimplixStorage would fix this
     * key and silently change how every other {@code UUID} in the plugin round
     * trips, including ones written by builds that never had it. Two keys need this
     * and both are here, so the parse is here.
     *
     * <h2>The two forms, and why both are read</h2>
     * <b>Nothing about what is written changes</b>, and nothing on disk needs
     * migrating: {@code serialize()} puts an {@code ArrayList<UUID>} into the
     * document, and the JSON writer answers {@code toString()} for anything whose
     * package starts with {@code java.}, so a file has always held
     * {@code "managers": ["11111111-..."]} - an array of strings. What differs is
     * <em>where</em> the read comes from. The storage layer hands the next reader of
     * a chunk the very map this class gave it until the file is re-read, and in that
     * map the elements are still {@code java.util.UUID} objects. So both are
     * accepted: a string is parsed, a UUID is taken as it is.
     *
     * <p>An element that is neither is dropped with a log rather than failing the
     * whole load, on the same reasoning as {@link #deserializeShopSettings}: one
     * unreadable name costs a shop one of its staff, and refusing to load costs the
     * owner the shop.
     */
    private static Set<UUID> deserializeUsers(FlatFileSection data, String key) {
        Set<UUID> users = new HashSet<>();

        for (Object stored : data.getList(key)) {
            if (stored instanceof UUID) {
                users.add((UUID) stored);
                continue;
            }

            try {
                users.add(UUID.fromString(String.valueOf(stored)));
            } catch (IllegalArgumentException notAUuid) {
                TradeShop.getPlugin().getVarManager().getDebugger().log(
                        "Shop user '" + stored + "' under '" + key + "' is not a UUID and was skipped while loading a shop.",
                        DebugLevels.DATA_ERROR);
            }
        }

        return users;
    }

    /**
     * Reads one side of a shop - the product list or the cost list - out of its file.
     *
     * <p>{@code serialize()} writes each side as a JSON list of item maps, so the side
     * is read as a list of maps. It used to be walked with
     * {@code data.keySet(key).forEach(...)}, which enumerates keys underneath a
     * <em>section</em> and answers nothing at all for a list, so every reloaded shop
     * came back with zero items on both sides.
     */
    private static List<ShopItemStack> deserializeSide(FlatFileSection data, String key) {
        List<ShopItemStack> items = new ArrayList<>();

        for (Map<String, Object> serializedItem : data.<Map<String, Object>>getListParameterized(key)) {
            ShopItemStack item = ShopItemStack.deserialize(serializedItem);
            if (item != null) items.add(item);
        }

        return items;
    }

    /**
     * Turns the stored {@code shopSettings} object back into the typed map the field holds.
     *
     * <p>The keys on disk are config names - {@code hopper-export} - and the values are
     * whatever {@link ObjectHolder} wrote. Assigning the raw map straight onto the field,
     * as this branch used to, left it keyed by String and made the next
     * {@code serialize()} throw. Any key this build no longer knows is dropped rather
     * than failing the whole load, and {@code aFixup()} fills in whatever is missing.
     */
    private static Map<ShopSettingKeys, ObjectHolder<?>> deserializeShopSettings(Map<String, Object> stored) {
        Map<ShopSettingKeys, ObjectHolder<?>> settings = new HashMap<>();

        stored.forEach((name, value) -> {
            try {
                settings.put(ShopSettingKeys.findShopSetting(name),
                        value instanceof Map ?
                                ObjectHolder.deserialize((Map<String, Object>) value) :
                                new ObjectHolder<>(value));
            } catch (IllegalArgumentException unknownSetting) {
                TradeShop.getPlugin().getVarManager().getDebugger().log("Unknown shop setting '" + name + "' skipped while loading a shop.", DebugLevels.DATA_ERROR);
            }
        });

        return settings;
    }

    /**
     * Serializes the object to Json using Gson
     *
     * @return serialized string
     */
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        Map<String, ObjectHolder<?>> settings = new HashMap<>();
        ArrayList<Map<String, Object>> products = new ArrayList<>(), costs = new ArrayList<>();

        product.forEach(item -> products.add(item.serialize()));
        cost.forEach(item -> costs.add(item.serialize()));
        shopSettings.forEach((key, value) -> settings.put(key.getConfigKey(), value));

        map.put("shopLoc", shopLoc.serialize());
        map.put("owner", owner.serialize());
        // Copied into lists rather than handed over as the Sets they are held in,
        // and that is a correctness fix rather than defensive copying. This map is
        // not only written to a file: ShopConfiguration.save puts it straight into
        // the storage layer's IN-MEMORY document, which is what the next reader of
        // that chunk is answered out of until the file is re-read. deserializeUsers
        // asks for a List - as getSerializableList did before it - so a Set read
        // back before the reload threw
        //
        //   java.lang.ClassCastException: class java.util.HashSet cannot be cast
        //   to class java.util.List
        //
        // out of Shop.deserialize, and DataStorage.getMatchingShopsInChunk is the
        // only caller of that reached from ShopUser.findProximityShop, which is
        // /tradeshop find. A list is what the file holds and what the reader
        // wants, so it is what is stored.
        //
        // The UUIDs themselves are handed over as they are and are NOT stringified
        // here. What reaches a file is an array of strings either way - the JSON
        // writer answers toString() for anything in a java.* package - so writing
        // strings would change nothing on disk while making this map disagree with
        // every shop file written before it. deserializeUsers reads both forms;
        // see its comment for which read path sees which.
        map.put("managers", new ArrayList<>(managers));
        map.put("members", new ArrayList<>(members));
        map.put("shopType", shopType.name());
        map.put("product", products);
        map.put("cost", costs);
        map.put("chestLoc", chestLoc.serialize());
        map.put("status", status.toString());
        map.put("shopSettings", settings);
        map.put("availableTrades", availableTrades);

        return map;
    }

    /**
     * Returns the shop signs location as a ShopLocation
     *
     * @return Shop sign's Location as ShopLocation
     */
    public ShopLocation getShopLocationAsSL() {
        return shopLoc;
    }

    /**
     * Returns the shop inventories location as a ShopLocation
     *
     * @return Shop inventory's Location as ShopLocation
     */
    public ShopLocation getInventoryLocationAsSL() {
        return chestLoc;
    }

    /**
     * Fixes values and objects after loading or creating a Shop
     */
    public void fixAfterLoad() {
        aSync = false;

        shopLoc.stringToWorld();

        if (!getShopType().isITrade()) {
            if (chestLoc == null && getShopLocation() != null) {
                chestLoc = new ShopLocation(new Utils().findShopChest(getShopLocation().getBlock()).getLocation());
            }
            chestLoc.stringToWorld();

            cost.removeIf(item -> item.getItemStack() == null || (item.getItemStack().getType().toString().endsWith("SHULKER_BOX") && getInventoryLocation().getBlock().getType().toString().endsWith("SHULKER_BOX")));
            plugin.getDataStorage().addChestLinkage(chestLoc, shopLoc);
        }

        aFixup();

        if (getShopSign() != null)
            updateSign();
    }

    /**
     * Fixes values and objects after loading or creating a Shop
     */
    public void aSyncFix() {
        aSync = true;
        aFixup();
    }

    private void aFixup() {
        if (utils == null) utils = new Utils();
        if (plugin == null) plugin = TradeShop.getPlugin();

        setShopSettings();

        fixSide(ShopItemSide.COST);
        fixSide(ShopItemSide.PRODUCT);
    }

    /**
     * Generates a semi readable output of the shop object for debug output
     *
     * @return Return String for debug output
     */
    public String toDebug() {
        StringBuilder sb = new StringBuilder();
        sb.append("Shop Debug: \n");
        sb.append("Shop Chunk: ").append(new ShopChunk(shopLoc.getChunk()).serialize()).append("\n");
        sb.append("Sign Location: ").append(shopLoc).append("\n");
        sb.append("Shop Type: ").append((isMissingItems() ? Setting.SHOP_INCOMPLETE_COLOUR : Setting.SHOP_GOOD_COLOUR).getString()).append(shopType.toHeader()).append("\n");
        sb.append("Shop Status: ").append(status.getLine()).append("\n");
        sb.append("Storage Location: ").append(hasStorage() ? getInventoryLocationAsSL().toString() : "N/A").append("\n");
        sb.append("Storage Type: ").append(hasStorage() ? getStorage().getType().toString() : "N/A").append("\n");
        sb.append("Owner: ").append(owner.getName()).append(" | ").append(owner.getUUID()).append("\n");
        sb.append("Managers: ").append(managers.isEmpty() ? "N/A" : managers.size()).append("\n");
        if (!managers.isEmpty())
            getUsers(ShopRole.MANAGER).forEach(manager -> sb.append("          ").append(manager.getName()).append(" | ").append(manager.getUUID()).append("\n"));
        sb.append("Members: ").append(members.isEmpty() ? "N/A" : members.size()).append("\n");
        if (!members.isEmpty())
            getUsers(ShopRole.MEMBER).forEach(member -> sb.append("         ").append(member.getName()).append(" | ").append(member.getUUID()).append("\n"));
        sb.append("Products: ").append(product.isEmpty() ? "N/A" : product.size()).append("\n");
        if (!product.isEmpty())
            product.forEach(productItem -> sb.append("          ").append(productItem.toConsoleText()).append("\n"));
        sb.append("Costs: ").append(cost.isEmpty() ? "N/A" : cost.size()).append("\n");
        if (!cost.isEmpty())
            cost.forEach(costItem -> sb.append("       ").append(costItem.toConsoleText()).append("\n"));

        return utils.colorize(sb.toString());
    }

    /**
     * Saves the shop to file
     *
     * <p>{@link #updateStatus()} runs before the shop is handed to storage, not
     * after. {@link #updateSign()} recomputes status on its way to line four
     * ({@code updateSignLines} :592), and it used to be the only thing that did -
     * from <em>after</em> the write - so what reached the file was the status the
     * shop had before this save. A shop built from the chat bar was stored
     * {@code INCOMPLETE}, the value the field starts at, however complete it
     * actually was.
     *
     * <p>That value is read rather than recomputed by everything that comes at a
     * shop through storage: {@code DataStorage.getMatchingShopsInChunk} loads with
     * {@code loadASync}, which fixes a shop up without touching its status, and
     * {@code ShopUser.findProximityShop} is {@code /tradeshop find}. So the sign in
     * the world and the answer that command gives disagreed about the same shop.
     */
    public void saveShop() {
        if (aSync) {
            plugin.getVarManager().getDebugger().log("Save aSync skipped for shop: " + shopLoc, DebugLevels.GSON);
            return;
        }

        updateFullTradeCount();
        updateStatus();
        plugin.getDataStorage().saveShop(this);
        updateSign();
        updateUserFiles();
    }

    /**
     * Saves the shop to file if passed boolean is true
     *
     * @param shouldSave true if save should proceed
     */
    public void saveShop(boolean shouldSave) {
        if (shouldSave) saveShop();
    }

    /**
     * Returns the shops sign as a Sign
     *
     * @return Shop sign as Sign
     */
    public Sign getShopSign() {
        Block b = shopLoc.getLocation().getBlock();
        Sign s = null;

        if (ShopType.isShop(b)) {
            s = (Sign) b.getState();
        }
        return s;
    }

    /**
     * Updates the text on the shops sign
     */
    public void updateSign() {
        updateSign(getShopSign());
    }

    /**
     * Updates the text on the shops sign using the events sign if it matches location
     */
    public void updateSign(SignChangeEvent event) {
        if (aSync) return;

        if (event == null || !event.getBlock().getLocation().equals(getShopLocation()))
            return;

        String[] signLines = updateSignLines(ShopSign.getDefaultColour(event.getBlock().getType()));

        for (int i = 0; i < 4; i++) {
            event.setLine(i, signLines[i]);
        }
    }

    /**
     * Updates the text on the shops sign using the passed sign if it matches location
     */
    public void updateSign(Sign sign) {
        if (aSync) return;

        if (sign == null || !sign.getLocation().equals(getShopLocation()))
            return;

        String[] signLines = updateSignLines(ShopSign.getDefaultColour(sign.getType()));

        for (int i = 0; i < 4; i++) {
            sign.setLine(i, signLines[i]);
        }

        sign.update();
    }

    /**
     * Updates the text for the shop signs
     *
     * @return String array containing updated sign lines to be set
     */
    private String[] updateSignLines(String defaultColour) {
        String[] signLines = new String[4];

        signLines[0] = utils.colorize((isMissingItems() ? Setting.SHOP_INCOMPLETE_COLOUR : Setting.SHOP_GOOD_COLOUR).getString() + shopType.toHeader());

        if (product.isEmpty()) {
            signLines[1] = "";
        } else if (product.size() == 1) {
            signLines[1] = itemLineFormatter(defaultColour, String.valueOf(product.get(0).getItemStack().getAmount()), " ", product.get(0).getCleanItemName());
        } else {
            signLines[1] = itemLineFormatter(defaultColour, Setting.MULTIPLE_ITEMS_ON_SIGN.getString().replace("%amount%", ""));
        }

        if (cost.isEmpty()) {
            signLines[2] = "";
        } else if (cost.size() == 1) {
            signLines[2] = itemLineFormatter(defaultColour, String.valueOf(cost.get(0).getItemStack().getAmount()), " ", cost.get(0).getCleanItemName());
        } else {
            signLines[2] = itemLineFormatter(defaultColour, Setting.MULTIPLE_ITEMS_ON_SIGN.getString().replace("%amount%", ""));
        }

        updateStatus();

        signLines[3] = status.getLine();

        for (int i = 0; i < 4; i++) {
            if (signLines[i] == null)
                signLines[i] = " ";
        }

        return signLines;
    }

    private String itemLineFormatter(String defaultColour, String... strings) {
        StringBuilder sb = new StringBuilder();

        for (String s : strings) {
            sb.append(s);
        }

        String colorFix = ChatColor.stripColor(utils.colorize(sb.toString()));

        return utils.colorize(defaultColour + colorFix.substring(0, Math.min(colorFix.length(), 15)));
    }

    /**
     * Returns the shops inventory as a BlockState
     *
     * <p>Null when the shop has no storage it can actually trade out of, which
     * is not the same question as whether the location it remembers has a
     * {@link BlockState}. <b>An air block still has one</b>, so a shop whose
     * chest had been broken answered this call with the state of the hole and
     * every guard built on it - {@link #hasStorage()} and therefore
     * {@link #updateFullTradeCount()} :741 - was told the chest was still
     * there. :746 then asked {@link #getChestAsSC()} for its inventory, which is
     * null, because {@code ShopChest.getBlock} assigns its block only for a
     * location that holds an inventory and {@code ShopChest.getInventory}
     * answers null for one that does not. :748 dereferenced it:
     * {@code Cannot invoke "Inventory.getStorageContents()" because
     * "shopInventory" is null}.
     *
     * <p>So the test is the one {@code ShopChest} itself makes - is this a block
     * this plugin reads a shop's stock out of - and the state is handed back only
     * when it is. Whatever removed the block is irrelevant: a break, an
     * explosion, a piston, an edit from another plugin, or an operator taking
     * that storage type out of allowed-shops all arrive here the same way.
     *
     * @return shops inventory as BlockState, or null if the shop has none
     */
    public BlockState getStorage() {
        if (aSync) return null;

        try {
            Block block = getInventoryLocation().getBlock();

            if (!plugin.getListManager().isInventory(block)) return null;

            BlockState state = block.getState();
            return state instanceof InventoryHolder ? state : null;
        } catch (NullPointerException npe) {
            return null;
        }
    }

    /**
     * Removes the shops inventory from the shop
     */
    public void removeStorage() {
        if (hasStorage()) {
            plugin.getDataStorage().removeChestLinkage(chestLoc);
            chestLoc = null;
        }
    }

    /**
     * Returns if the shop has storage it can trade out of
     *
     * @return true if the shop's storage block is there and holds an inventory
     */
    public boolean hasStorage() {
        return getStorage() != null;
    }

    /**
     * Returns the shops status as ShopStatus
     *
     * @return Shops status as ShopStatus
     */
    public ShopStatus getStatus() {
        return status;
    }

    /**
     * Sets the shops status to closed
     */
    public void setStatus(ShopStatus newStatus) {
        status = newStatus;
    }

    /**
     * Sets the shop to open if the shop has all necessary information to make a trade
     *
     * @return true if shop opened
     */
    public ShopStatus setOpen() {
        setStatus(ShopStatus.OPEN);
        updateStatus();

        saveShop();
        updateSign();
        return status;
    }

    /**
     * Automatically updates a shops status if it is not CLOSED
     *
     * <p>A shop whose storage block is gone is {@link ShopStatus#INCOMPLETE} and
     * stays that way until its owner puts one back. It is not closed - CLOSED is
     * something an owner chooses and this method deliberately refuses to move a
     * shop out of it, so a repaired shop would stay shut - and it is not
     * removed, because the item lists on both sides are the owner's work and a
     * block that vanished is not consent to throw them away. The repair itself
     * is {@code ShopProtectionListener.onBlockPlace}, which re-links a storage
     * block placed under the sign only while {@code !hasStorage()}.
     */
    public void updateStatus() {
        if (!status.equals(ShopStatus.CLOSED)) {
            // hasStorage() rather than `chestLoc != null`: a shop can remember a
            // storage location whose block is no longer there, and a shop that
            // cannot reach its stock is incomplete rather than merely empty. An
            // aSync shop is not attached to the world at all - getStorage()
            // answers null for every one of them - so it keeps the weaker test
            // instead of being reported broken for being off the main thread.
            boolean somewhereToTradeFrom = shopType.isITrade() || (aSync ? chestLoc != null : hasStorage());

            if (!isMissingItems() && somewhereToTradeFrom) {
                if (getAvailableTrades() > 0)
                    setStatus(ShopStatus.OPEN);
                else
                    setStatus(ShopStatus.OUT_OF_STOCK);
            } else {
                setStatus(ShopStatus.INCOMPLETE);
            }
        }
    }

    /**
     * Removes this shop from file
     */
    public void remove() {
        purgeFromUserFiles();
        plugin.getDataStorage().removeShop(this);
    }

    /**
     * Switches the type of the shop between 'Trade' and 'BiTrade'
     */
    public void switchType() {
        if (shopType == ShopType.TRADE)
            setShopType(ShopType.BITRADE);
        else if (shopType == ShopType.BITRADE)
            setShopType(ShopType.TRADE);

        saveShop();
        updateSign();
    }

    /**
     * Checks if all Items in the list are valid for trade
     *
     * @param side  Which side of the trade should be checked for illegal items
     * @param items List<ShopItemStack> to check
     * @return true if all products are valid
     */
    private boolean areItemsValid(ShopItemSide side, List<ShopItemStack> items) {
        for (ShopItemStack iS : items) {
            if (utils.isIllegal(side, iS.getItemStack().getType()))
                return false;
        }

        return true;
    }

    /**
     * Updates the number of trades the shop can make
     */
    public void updateFullTradeCount() {
        if (getShopType().equals(ShopType.ITRADE) && hasSide(ShopItemSide.PRODUCT)) {
            availableTrades = 999;
            return;
        }

        if (!hasStorage() || !hasSide(ShopItemSide.PRODUCT)) {
            availableTrades = 0;
            return;
        }

        Inventory shopInventory = getChestAsSC().getInventory();

        int count = countItems(getSideList(ShopItemSide.PRODUCT), shopInventory.getStorageContents());

        if (count == 0 && getShopType().equals(ShopType.BITRADE)) {
            count = countItems(getSideList(ShopItemSide.COST), shopInventory.getStorageContents());
        }

        availableTrades = count;
    }

    private int countItems(List<ShopItemStack> countItems, ItemStack[] storageContents) {
        Inventory storage = Bukkit.createInventory(null, Utils.scratchInventorySize(storageContents.length));
        storage.setContents(storageContents);

        int totalCount = 0, currentCount = 0;

        for (ShopItemStack item : countItems) {
            totalCount += item.getItemStack().getAmount();
            int traded;
            for (ItemStack storageItem : storage.getStorageContents()) {
                if (storageItem != null && item.isSimilar(storageItem)) {
                    traded = Math.min(storageItem.getAmount(), item.getItemStack().getMaxStackSize());

                    storageItem.setAmount(traded);
                    storage.removeItem(storageItem);
                    currentCount += traded;
                }
            }
        }

        return (currentCount == 0 || totalCount == 0) ? 0 : currentCount / totalCount;
    }


    //region Setting Management - Methods for managing a shops settings
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Sets the Shops settings based on the ShopType and server defaults.
     */
    public void setShopSettings() {
        setShopSettings(shopType);
    }

    /**
     * Retrieves the specified setting if it is usable on the shop
     */
    public ObjectHolder<?> getShopSetting(ShopSettingKeys settingKey) {
        return settingKey.isUsable(shopType) ? shopSettings.get(settingKey) : null;
    }

    /**
     * Retrieves the specified setting if it is usable on the shop
     */
    public boolean hasShopSetting(ShopSettingKeys settingKey) {
        return settingKey.isUsable(shopType) && shopSettings.containsKey(settingKey);
    }

    /**
     * Retrieves the specified setting if it is usable on the shop
     */
    public Map<ShopSettingKeys, ObjectHolder<?>> getShopSettings() {
        return shopSettings;
    }

    /**
     * Sets the Shops settings based on the passed ShopType
     */
    public void setShopSettings(ShopType type) {
        if (shopSettings == null)
            shopSettings = new HashMap<>();

        for (ShopSettingKeys settingKey : ShopSettingKeys.values()) {
            if (shopSettings.containsKey(settingKey) && !settingKey.isUsable(type)) {
                shopSettings.remove(settingKey);
            } else {
                shopSettings.putIfAbsent(settingKey, settingKey.getDefaultValue(type));
            }

            if (!settingKey.isUserEditable(type) && shopSettings.get(settingKey) != settingKey.getDefaultValue(type)) {
                shopSettings.put(settingKey, settingKey.getDefaultValue(type));
            }
        }
    }

    /**
     * Retrieves the specified setting if it is usable on the shop
     */
    public void setShopSettings(Map<ShopSettingKeys, ObjectHolder<?>> newSettings) {
        if (newSettings.size() > 0)
            plugin.getListManager().removeSkippableShop(getShopLocation());

        for (ShopSettingKeys settingKey : newSettings.keySet()) {
            if (settingKey.isUsable(shopType) && newSettings.get(settingKey) != null) {
                shopSettings.put(settingKey, newSettings.get(settingKey));
            }
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    //endregion

    //region User Management - Methods for adding/deleting/updating/viewing a shops users
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Returns a list of all users for the shop based on specified roles.
     *
     * @param roles role to get users with
     * @return list of ShopUsers.
     */
    public List<ShopUser> getUsers(ShopRole... roles) {
        return getUsersUUID(roles).stream().map((uuid) -> new ShopUser(uuid, ShopRole.MANAGER)).collect(Collectors.toList());
    }

    /**
     * Returns a list of all users for the shop based on specified roles. Will exclude any users in the list.
     *
     * @param excludedPlayers player UUIDs to exclude from the results
     * @param roles           role to get users with
     * @return list of ShopUsers.
     */
    public List<ShopUser> getUsersExcluding(List<UUID> excludedPlayers, ShopRole... roles) {
        return getUsers(roles).stream().filter((shopUser) -> !excludedPlayers.contains(shopUser.getUUID())).collect(Collectors.toList());
    }

    /**
     * Returns a list of all usernames for the shop based on specified roles.
     *
     * @param roles role to get users with
     * @return List of specified users names
     */
    public List<String> getUserNames(ShopRole... roles) {
        return hasUsers(roles) ? getUsers(roles).stream().map(ShopUser::getName).collect(Collectors.toList()) : new ArrayList<>();
    }

    /**
     * Returns a list of all user UUIDs for the shop based on specified roles.
     *
     * @param roles role to get users with
     * @return List of specified users names
     */
    public List<UUID> getUsersUUID(ShopRole... roles) {
        List<UUID> users = new ArrayList<>();
        for (ShopRole role : roles) {
            switch (role) {
                case MEMBER:
                    users.addAll(members);
                    break;
                case MANAGER:
                    users.addAll(managers);
                    break;
                case OWNER:
                    users.add(owner.getUUID());
                    break;
            }
        }

        return users;
    }

    /**
     * Returns true if the shop has users of the specified role
     *
     * @param roles role to check for users
     * @return true if users exist
     */
    public boolean hasUsers(ShopRole... roles) {
        return !getUsers(roles).isEmpty();
    }

    /**
     * Returns the shops owner
     *
     * @return ShopUser object of owner
     */
    public ShopUser getOwner() {
        return owner;
    }

    /**
     * Sets the owner (don't know if this will ever be used)
     *
     * @param owner The new owner of the shop
     */
    public void setOwner(ShopUser owner) {
        this.owner = owner;
    }

    /**
     * Sets a user to the shop with the specified role after removing it if it was already on the shop
     *
     * @param newUser the player to be set
     * @param role    role to set the player to
     * @return true if player has been set
     */
    public boolean setUser(UUID newUser, ShopRole role) {
        removeUser(newUser);
        return addUser(newUser, role);
    }

    /**
     * Adds a user to the shop with the specified role
     *
     * @param newUser the player to be added
     * @param role    role to set the player to
     * @return true if player has been added
     */
    public boolean addUser(UUID newUser, ShopRole role) {
        if (getUsers(ShopRole.MANAGER, ShopRole.MEMBER).size() >= Setting.MAX_SHOP_USERS.getInt())
            return false;

        if (!getUsersUUID(ShopRole.MANAGER, ShopRole.MEMBER).contains(newUser)) {
            switch (role) {
                case MANAGER:
                    saveShop(managers.add(newUser));
                    return true;
                case MEMBER:
                    saveShop(members.add(newUser));
                    return true;
            }
        }

        return false;
    }

    /**
     * Updates all shop users
     */
    public void updateShopUsers(Set<ShopUser> updatedUserSet) {
        for (ShopUser user : updatedUserSet) {
            removeUser(user.getUUID());
            addUser(user.getUUID(), user.getRole());
        }
        saveShop();
    }

    /**
     * Returns the ShopRole of the supplied UUID
     *
     * @param uuidToCheck uuid to check for role of
     * @return the ShopRole of the supplied UUID
     */
    public ShopRole checkRole(UUID uuidToCheck) {
        if (owner.getUUID().equals(uuidToCheck)) {
            return ShopRole.OWNER;
        } else if (managers.contains(uuidToCheck)) {
            return ShopRole.MANAGER;
        } else if (members.contains(uuidToCheck)) {
            return ShopRole.MEMBER;
        } else {
            return ShopRole.SHOPPER;
        }
    }

    /**
     * Removes a user from the shop
     *
     * @param oldUser the UUID of the player to be removed
     * @return true if user was removed
     */
    public boolean removeUser(UUID oldUser) {
        boolean ret = false;
        if (getUsersUUID(ShopRole.MANAGER).contains(oldUser)) {
            managers.remove(oldUser);
            ret = true;
        }

        if (getUsersUUID(ShopRole.MEMBER).contains(oldUser)) {
            members.remove(oldUser);
            ret = true;
        }

        saveShop(ret);

        return ret;
    }

    /**
     * Sets the list of users to a role
     *
     * @param users the managers to be set to the shop
     */
    public boolean setUsers(Set<UUID> users, ShopRole role) {
        boolean ret = false;
        switch (role) {
            case MANAGER:
                users.forEach(this::removeUser);
                managers = users;
                ret = true;
                break;
            case MEMBER:
                users.forEach(this::removeUser);
                members = users;
                ret = true;
                break;
        }

        saveShop(ret);

        return ret;
    }

    /**
     * Updates the saved player data for all users
     */
    private void updateUserFiles() {
        for (UUID user : getUsersUUID(ShopRole.OWNER, ShopRole.MANAGER, ShopRole.MEMBER)) {
            PlayerSetting playerSetting = plugin.getDataStorage().loadPlayer(user);
            playerSetting.updateShop(this);
            plugin.getDataStorage().savePlayer(playerSetting);
        }
    }

    /**
     * Removes this shop from all users
     */
    private void purgeFromUserFiles() {
        for (UUID user : getUsersUUID(ShopRole.OWNER, ShopRole.MANAGER, ShopRole.MEMBER)) {
            PlayerSetting playerSetting = plugin.getDataStorage().loadPlayer(user);
            playerSetting.removeShop(this);
            plugin.getDataStorage().savePlayer(playerSetting);
        }
    }


    //------------------------------------------------------------------------------------------------------------------
    //endregion

    //region Item Management - Methods for adding/deleting/updating/viewing Cost and Product lists
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Checks if shop has a bad side and fixes if necessary
     */
    public void fixSide(ShopItemSide side) {
        List<ShopItemStack> ogItems = (side.equals(ShopItemSide.PRODUCT) ? product : cost);

        Set<Material> matSet = new HashSet<>();

        ogItems.stream().filter(shopItemStack -> shopItemStack.getItemStack() != null).forEach((item) -> matSet.add(item.getItemStack().getType()));


        if (ogItems.size() > 1 && ogItems.size() != matSet.size()) {
            List<ShopItemStack> scrapList = new ArrayList<>(ogItems);
            (side.equals(ShopItemSide.PRODUCT) ? product : cost).clear();

            ogItems.forEach((item) -> {
                while (scrapList.contains(item)) {
                    addSideItem(side, item);
                    scrapList.remove(scrapList.lastIndexOf(item));
                }
            });

            updateFullTradeCount();
            if (!aSync) updateSign();
            saveShop();
        }
    }

    /**
     * Checks if shop has items on specified side
     *
     * @return True if side != null
     */
    public boolean hasSide(ShopItemSide side) {
        return getSide(side).size() > 0;
    }

    /**
     * Checks if shop has necessary items to make a trade
     *
     * @return true if items are missing
     */
    public boolean isMissingItems() {
        return isNoCost() ? product.isEmpty() : product.isEmpty() || cost.isEmpty();
    }

    /**
     * Checks if the shop is set to not use cost
     *
     * @return true if cost is not needed
     */
    public boolean isNoCost() {
        if (!ShopSettingKeys.NO_COST.isUsable(getShopType())) return false;

        Boolean noCost = shopSettings.get(ShopSettingKeys.NO_COST).asBoolean();
        return noCost != null ? noCost : false;
    }

    /**
     * Returns the ItemStacks on specified side
     *
     * @param side Side to get ItemStacks form
     * @return ItemStack List
     */
    public List<ItemStack> getSideItemStacks(ShopItemSide side) {
        List<ItemStack> ret = new ArrayList<>();
        for (ShopItemStack itm : getSide(side))
            ret.add(itm.getItemStack().clone());
        return ret;
    }

    /**
     * Returns the appropriate list for the side of the trade for object internal use
     *
     * @return Side ShopItemStack List
     */
    private List<ShopItemStack> getSide(ShopItemSide side) {
        return side.equals(ShopItemSide.PRODUCT) ? product : cost;
    }

    /**
     * Returns a list of the items on the shops specified side
     *
     * @param side Side to get items for
     * @return Side ShopItemStack List
     */
    public List<ShopItemStack> getSideList(ShopItemSide side) {
        return getSideList(side, false);
    }

    /**
     * Returns a list of the items on the shops specified side, or the opposite side it doBiTradeAlternate is true
     *
     * @param side               Side to get items for
     * @param doBiTradeAlternate Should side be flipped(for biTrade + Left Click)
     * @return Side ShopItemStack List
     */
    public List<ShopItemStack> getSideList(ShopItemSide side, boolean doBiTradeAlternate) {
        List<ShopItemStack> ret = new ArrayList<>();
        for (ShopItemStack itm : getSide(doBiTradeAlternate ? side.getReverse() : side))
            ret.add(itm.clone());
        return ret;
    }

    public List<String> getSideListNames(ShopItemSide side) {
        return getSideList(side).stream().map((item) -> item.getAmount() + " x " + item.getItemName()).collect(Collectors.toList());
    }

    /**
     * Removes the item at the index of the specified side
     *
     * @param side  side of the trade to remove item from
     * @param index index of item to remove
     * @return true if Item is removed from the specified Side
     */
    public boolean removeSideIndex(ShopItemSide side, int index) {
        if (getSide(side).size() > index) {
            getSide(side).remove(index);

            saveShop();
            updateSign();
            return true;
        }

        return false;
    }

    /**
     * Sets the Item for the specified side of the trade
     *
     * @param side    Side of the trade to set
     * @param newItem ItemStack to be set
     */
    public void setSideItems(ShopItemSide side, ItemStack newItem) {
        if (utils.isIllegal(side, newItem.getType()))
            return;

        getSide(side).clear();

        addSideItem(side, newItem);
    }

    /**
     * Adds more items to the specified side
     *
     * @param side        Side to add items to
     * @param newShopItem ShopItemStack to be added
     */
    public void addSideItem(ShopItemSide side, ShopItemStack newShopItem) {
        if (utils.isIllegal(side, newShopItem.getItemStack().getType()))
            return;

        int toRemoveShopItemStack = -1;


        for (int i = 0; i < getSide(side).size(); i++) {
            if (getSideList(side).get(i).getItemStack().getType().equals(newShopItem.getItemStack().getType()) && getSideList(side).get(i).isSimilar(newShopItem.getItemStack())) {
                toRemoveShopItemStack = i;
                newShopItem = getSideList(side).get(i).clone();
                newShopItem.setAmount(getSideList(side).get(i).getAmount() + newShopItem.getItemStack().getAmount());
                break;
            }
        }

        if (toRemoveShopItemStack > -1)
            getSide(side).remove(toRemoveShopItemStack);

        getSide(side).add(newShopItem);
        //*/


        if (!getShopType().isITrade() && chestLoc != null)
            getSide(side).removeIf(item -> item.getItemStack().getType().toString().endsWith("SHULKER_BOX") && getInventoryLocation().getBlock().getType().toString().endsWith("SHULKER_BOX"));

        saveShop();
        updateSign();
    }

    /**
     * Adds more items to the specified side
     *
     * @param side    Side to add items to
     * @param newItem ItemStack to be added
     */
    public void addSideItem(ShopItemSide side, ItemStack newItem) {
        addSideItem(side, new ShopItemStack(newItem));
    }

    /**
     * Checks if the shop has sufficient stock to make a trade on specified side
     */
    public boolean hasSideStock(ShopItemSide side) {
        return !shopType.isITrade() && hasSide(side) && getChestAsSC() != null && getChestAsSC().hasStock(getSideList(side));
    }

    /**
     * Checks if all Items on the specified side are valid for trade
     *
     * @param side Side to check
     * @return true if all Items are valid
     */
    public boolean isSideValid(ShopItemSide side) {
        return areItemsValid(side, getSideList(side));
    }

    /**
     * Updates list on specified side
     *
     * @param side            Side to be updated
     * @param updatedItemList list to set updatedItemList to
     * @param save2disk       whether or not to save to disk
     */
    public void updateSide(ShopItemSide side, List<ShopItemStack> updatedItemList, boolean save2disk) {
        if (!getShopType().isITrade() && chestLoc != null)
            updatedItemList.removeIf(item -> item.getItemStack().getType().toString().endsWith("SHULKER_BOX") && getInventoryLocation().getBlock().getType().toString().endsWith("SHULKER_BOX"));

        if (side.equals(ShopItemSide.PRODUCT)) product = updatedItemList;
        else cost = updatedItemList;

        if (save2disk) saveShop();
        updateSign();
    }

    /**
     * Updates item on specified side at index
     *
     * @param side        Side to be updated
     * @param updatedItem Item to be updated at the specified index and side
     * @param index       index of the item t be updated
     */
    public void updateSideItem(ShopItemSide side, ShopItemStack updatedItem, int index, boolean save2disk) {
        if (!getShopType().isITrade() &&
                chestLoc != null &&
                updatedItem.getItemStack().getType().toString().endsWith("SHULKER_BOX") &&
                getInventoryLocation().getBlock().getType().toString().endsWith("SHULKER_BOX"))
            return;

        (side.equals(ShopItemSide.PRODUCT) ? product : cost).set(index, updatedItem);

        if (save2disk) saveShop();
        updateSign();
    }

    /**
     * Checks if the shop can trade the desired items on the specified side
     *
     * @param side         Side to be checked, Checks both sides if biTrade
     * @param desiredItems Items to check for
     */
    public boolean isMissingSideItems(ShopItemSide side, List<ItemStack> desiredItems) {
        if (desiredItems == null || desiredItems.isEmpty())
            return false; //If we aren't looking for anything then just return

        List<Boolean> found = new ArrayList<>(desiredItems.size());
        List<ShopItemStack> referenceList = new ArrayList<>(getSideList(side)); //Will always check against the specified side
        if (shopType.isBiTrade())
            referenceList.addAll(getSideList(side.getReverse())); //Add in opposite side if shop is BiTrade

        for (ItemStack item : desiredItems) {
            boolean foundItem = referenceList.stream().anyMatch(shopItemStack -> shopItemStack.isSimilar(item));
            TradeShop.getPlugin().getDebugger().log(" --- _D_I_ --- " + side.name() + "\n" + item.toString() + "\n" + Arrays.toString(referenceList.stream().map(ShopItemStack::toString).toArray(String[]::new)) + "\n" + foundItem, DebugLevels.FIND_COMMAND);
            found.add(desiredItems.indexOf(item), //Get index of item being checked to set appropriate boolean in found list.
                    foundItem); //Returns true if any items are similar
        }

        return found.contains(false);
    }

    //------------------------------------------------------------------------------------------------------------------
    //endregion
}