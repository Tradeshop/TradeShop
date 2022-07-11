package org.shanerx.tradeshop.data.storage.sqlite;

import org.bukkit.Location;
import org.shanerx.tradeshop.data.storage.ShopConfiguration;
import org.shanerx.tradeshop.item.ShopItemStack;
import org.shanerx.tradeshop.player.ShopRole;
import org.shanerx.tradeshop.player.ShopUser;
import org.shanerx.tradeshop.shop.Shop;
import org.shanerx.tradeshop.shop.ShopSettingKeys;
import org.shanerx.tradeshop.shop.ShopType;
import org.shanerx.tradeshop.shoplocation.ShopChunk;
import org.shanerx.tradeshop.shoplocation.ShopLocation;
import org.shanerx.tradeshop.utils.objects.Tuple;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SQLiteShopConfiguration implements ShopConfiguration {

    private final ShopChunk chunk;
    private final String chunkStr;
    private final DatabaseManager sqlite;

    public SQLiteShopConfiguration(ShopChunk chunk) {
        this.chunk = chunk;
        this.chunkStr = chunk.serialize();
        this.sqlite = DatabaseManager.getSqlite();

        try {
            createTableIfNotExists();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(Shop shop) {
        try (Connection conn = sqlite.setupConnection(true)) {
            Location chestLoc = shop.getChestAsSC().getChest().getLocation();
            remove(shop.getShopLocationAsSL()); // this should NOT be moved above the try !

            String sql = "INSERT INTO shops (owner_uuid, sign_loc_serialized, chunk_serialized, type, " +
                    " status, setting_hopper_import, setting_hopper_export, setting_no_cost, available_trades, " +
                    " sign_world_name, sign_x, sign_y, sign_z, chest_world_name, chest_x, chest_y, chest_z) " +
                    " VALUES " +
                    String.format("('%s', '%s', '%s', '%s', '%s', %d, %d, %d, %d, '%s', %d, %d, %d, '%s', %d, %d, %d);",
                            shop.getOwner().getUUID().toString(), shop.getShopLocationAsSL().serialize(), chunkStr, shop.getShopType().name(),
                            shop.getStatus().toString(), shop.getShopSetting(ShopSettingKeys.HOPPER_IMPORT).asBoolean() ? 1 : 0,
                            shop.getShopSetting(ShopSettingKeys.HOPPER_EXPORT).asBoolean() ? 1 : 0, shop.getShopSetting(ShopSettingKeys.NO_COST).asBoolean() ? 1 : 0,
                            shop.getAvailableTrades(), shop.getShopLocation().getWorld().getName(),
                            shop.getShopLocation().getBlockX(), shop.getShopLocation().getBlockY(), shop.getShopLocation().getBlockZ(),
                            chestLoc.getWorld().getName(), chestLoc.getBlockX(), chestLoc.getBlockY(), chestLoc.getBlockZ());

            sqlite.prepareStatement(conn, sql).execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(ShopLocation loc) {
        try (Connection conn = sqlite.setupConnection(true)) {
            String sql = "DELETE FROM shops WHERE sign_loc_serialized = '" + loc.serialize() + "';";
            sqlite.prepareStatement(conn, sql).execute();

            String sql2 = "DELETE FROM shop_products WHERE sign_loc_serialized = '" + loc.serialize() + "';";
            sqlite.prepareStatement(conn, sql2).execute();

            String sql3 = "DELETE FROM shop_costs WHERE sign_loc_serialized = '" + loc.serialize() + "';";
            sqlite.prepareStatement(conn, sql3).execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Shop load(ShopLocation loc) {
        String locStr = loc.serialize();

        try (Connection conn = sqlite.setupConnection(true)) {
            ResultSet res = sqlite.prepareStatement(conn,
                            "SELECT * FROM shops WHERE sign_loc_serialized = '" + locStr + "';")
                    .executeQuery();

            if (!res.next()) return null;

            Tuple<Location, Location> locations = new Tuple<Location, Location>(ShopLocation.deserialize(res.getString("sign_loc_serialized")).getLocation(),
                                                                    ShopLocation.deserialize(res.getString("sign_loc_serialized")).getLocation());
            Set<UUID> members = new HashSet<>();
            Set<UUID> managers = new HashSet<>();

            List<ShopItemStack> products = new ArrayList<>();
            List<ShopItemStack> costs = new ArrayList<>();

            String sql2 = "SELECT * FROM shop_products WHERE sign_loc_serialized = '" + locStr + "';";
            ResultSet res2 = sqlite.prepareStatement(conn, sql2).executeQuery();
            while (res2.next()) {
                products.add(ShopItemStack.deserialize(res2.getString("product")));
            }

            String sql3 = "SELECT * FROM shop_costs WHERE sign_loc_serialized = '" + locStr + "';";
            ResultSet res3 = sqlite.prepareStatement(conn, sql3).executeQuery();
            while (res3.next()) {
                costs.add(ShopItemStack.deserialize(res3.getString("cost")));
            }

            String sql4 = "SELECT * FROM shop_managers WHERE sign_loc_serialized = '" + locStr + "';";
            ResultSet res4 = sqlite.prepareStatement(conn, sql4).executeQuery();
            while (res4.next()) {
                managers.add(UUID.fromString(res4.getString("uuid")));
            }

            String sql5 = "SELECT * FROM shop_members WHERE sign_loc_serialized = '" + locStr + "';";
            ResultSet res5 = sqlite.prepareStatement(conn, sql5).executeQuery();
            while (res5.next()) {
                managers.add(UUID.fromString(res5.getString("uuid")));
            }

            Shop shop = new Shop(locations,
                            ShopType.valueOf(res.getString("type")),
                            new ShopUser(UUID.fromString(res.getString("owner_uuid")), ShopRole.OWNER),
                            new Tuple<>(managers, members),
                            products, costs);

            if (res.next()) throw new IllegalStateException("Database contains more than one entry with the shop loc '" + locStr + "'");

            return shop;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int size() {
        String sql = "SELECT * FROM shops WHERE chunk_serialized = '" + chunkStr + "';";
        try (Connection conn = sqlite.setupConnection(true)) {
            ResultSet res = sqlite.prepareStatement(conn, sql).executeQuery();
            while (res.next()); // empty body is intentional
            return res.getRow();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createTableIfNotExists() throws SQLException {
        try(Connection conn = sqlite.setupConnection(true)) {
            String sql = "CREATE TABLE IF NOT EXISTS shops " +
                    "(owner_uuid TEXT not NULL, " +
                    " sign_loc_serialized TEXT not NULL, " +
                    " chunk_serialized TEXT not NULL, " +
                    " type TEXT not NULL, " +
                    " status TEXT not NULL, " +

                    " setting_hopper_import INTEGER, " +
                    " setting_hopper_export INTEGER, " +
                    " setting_no_cost INTEGER, " +

                    " available_trades INTEGER, " +

                    " sign_world_name TEXT not NULL, " +
                    " sign_x INTEGER, " +
                    " sign_y INTEGER, " +
                    " sign_z INTEGER, " +

                    " chest_world_name TEXT not NULL, " +
                    " chest_x INTEGER, " +
                    " chest_y INTEGER, " +
                    " chest_z INTEGER, " +

                    " PRIMARY KEY ( sign_loc_serialized ));";
            sqlite.prepareStatement(conn, sql).execute();

            String sql2 = "CREATE TABLE IF NOT EXISTS shop_products " +
                    "(sign_loc_serialized TEXT not NULL, " +
                    " product TEXT not NULL, " +
                    " PRIMARY KEY ( sign_loc_serialized ));";
            sqlite.prepareStatement(conn, sql2).execute();

            String sql3 = "CREATE TABLE IF NOT EXISTS shop_costs " +
                    "(sign_loc_serialized TEXT not NULL, " +
                    " cost TEXT not NULL, " +
                    " PRIMARY KEY ( sign_loc_serialized ));";
            sqlite.prepareStatement(conn, sql3).execute();

            String sql4 = "CREATE TABLE IF NOT EXISTS shop_managers " +
                    "(sign_loc_serialized TEXT not NULL, " +
                    " uuid TEXT not NULL, " +
                    " PRIMARY KEY ( sign_loc_serialized ));";
            sqlite.prepareStatement(conn, sql4).execute();

            String sql5 = "CREATE TABLE IF NOT EXISTS shop_members " +
                    "(sign_loc_serialized TEXT not NULL, " +
                    " uuid TEXT not NULL, " +
                    " PRIMARY KEY ( sign_loc_serialized ));";
            sqlite.prepareStatement(conn, sql5).execute();
        } catch (SQLException e) {
            throw e;
        }
    }
}
