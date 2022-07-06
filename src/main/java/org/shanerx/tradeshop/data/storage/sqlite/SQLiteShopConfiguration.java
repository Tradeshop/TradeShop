package org.shanerx.tradeshop.data.storage.sqlite;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
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

import javax.xml.crypto.Data;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SQLiteShopConfiguration implements ShopConfiguration {

    private final ShopChunk chunk;
    private final String chunkStr;

    public SQLiteShopConfiguration(ShopChunk chunk) {
        this.chunk = chunk;
        this.chunkStr = chunk.serialize();
        try {
            createTableIfNotExists();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(Shop shop) {
        Location chestLoc = shop.getChestAsSC().getChest().getLocation();
        remove(shop.getShopLocationAsSL());

        try {
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
            DatabaseManager.getSqlite(true).prepareStatement(sql).execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(ShopLocation loc) {
        try {
            String sql = "DELETE * FROM shops WHERE sign_loc_serialized ='" + loc.serialize() + "';";
            DatabaseManager.getSqlite(true).prepareStatement(sql).execute();

            String sql2 = "DELETE * FROM shop_products WHERE sign_loc_serialized = '" + loc.serialize() + "';";
            DatabaseManager.getSqlite(false).prepareStatement(sql2).execute();

            String sql3 = "DELETE * FROM shop_costs WHERE sign_loc_serialized = '" + loc.serialize() + "';";
            DatabaseManager.getSqlite(false).prepareStatement(sql3).execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Shop load(ShopLocation loc) {
        String locStr = loc.serialize();

        ResultSet res;
        try {
            res = DatabaseManager.getSqlite(true).prepareStatement("SELECT * FROM shops where sign_loc_serialized = '" + locStr + "';").executeQuery();

            if (!res.last()) return null;
            else if (res.getRow() > 1) throw new IllegalStateException("Database contains more than one entry with the shop loc '" + locStr + "'");

            res.beforeFirst();
            res.next();

            /**
             * Creates a Shop object
             *
             * @param locations Location of shop sign and chest as Tuple, left = Sign location, right = inventory location
             * @param owner     Owner of the shop as a ShopUser
             * @param shopType  Type of the shop as ShopType
             * @param items     Items to go into the shop as Tuple, left = Product, right = Cost
             * @param players   Users to be added to the shop as Tuple, left = Managers, right = Members
             */

            Tuple<Location, Location> locations = new Tuple<Location, Location>(ShopLocation.deserialize(res.getString("sign_loc_serialized")).getLocation(),
                                                                                ShopLocation.deserialize(res.getString("sign_loc_serialized")).getLocation());
            Set<UUID> members = new HashSet<>();
            Set<UUID> managers = new HashSet<>();

            List<ShopItemStack> products = new ArrayList<>();
            List<ShopItemStack> costs = new ArrayList<>();

            String sql2 = "SELECT * FROM shop_products WHERE sign_loc_serialized = '" + locStr + "';";
            ResultSet res2 = DatabaseManager.getSqlite(false).prepareStatement(sql2).executeQuery();
            while (res2.next()) {
                products.add(ShopItemStack.deserialize(res2.getString("product")));
            }

            String sql3 = "SELECT * FROM shop_costs WHERE sign_loc_serialized = '" + locStr + "';";
            ResultSet res3 = DatabaseManager.getSqlite(false).prepareStatement(sql3).executeQuery();
            while (res3.next()) {
                costs.add(ShopItemStack.deserialize(res3.getString("cost")));
            }

            String sql4 = "SELECT * FROM shop_managers WHERE sign_loc_serialized = '" + locStr + "';";
            ResultSet res4 = DatabaseManager.getSqlite(false).prepareStatement(sql4).executeQuery();
            while (res4.next()) {
                managers.add(UUID.fromString(res4.getString("uuid")));
            }

            String sql5 = "SELECT * FROM shop_members WHERE sign_loc_serialized = '" + locStr + "';";
            ResultSet res5 = DatabaseManager.getSqlite(false).prepareStatement(sql5).executeQuery();
            while (res5.next()) {
                managers.add(UUID.fromString(res5.getString("uuid")));
            }

            return new Shop(locations,
                            ShopType.valueOf(res.getString("type")),
                            new ShopUser(UUID.fromString(res.getString("owner_uuid")), ShopRole.OWNER),
                            new Tuple<>(managers, members),
                            products, costs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int size() {
        String sql = "SELECT * FROM shops WHERE chunk_serialized = '" + chunkStr + "';";
        try {
            ResultSet res = DatabaseManager.getSqlite(true).prepareStatement(sql).executeQuery();
            res.last();
            return res.getRow();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createTableIfNotExists() throws SQLException {
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

                " PRIMARY KEY ( shop_loc_serialized ));";
        DatabaseManager.getSqlite(true).prepareStatement(sql).execute();

        String sql2 = "CREATE TABLE IF NOT EXISTS shop_products " +
                "(sign_loc_serialized TEXT not NULL, " +
                " product TEXT not NULL, " +
                " PRIMARY KEY ( sign_loc_serialized ));";
        DatabaseManager.getSqlite(false).prepareStatement(sql2).execute();

        String sql3 = "CREATE TABLE IF NOT EXISTS shop_costs " +
                "(sign_loc_serialized TEXT not NULL, " +
                " cost TEXT not NULL, " +
                " PRIMARY KEY ( sign_loc_serialized ));";
        DatabaseManager.getSqlite(false).prepareStatement(sql3).execute();

        String sql4 = "CREATE TABLE IF NOT EXISTS shop_managers " +
                "(sign_loc_serialized TEXT not NULL, " +
                " uuid TEXT not NULL, " +
                " PRIMARY KEY ( sign_loc_serialized ));";
        DatabaseManager.getSqlite(false).prepareStatement(sql4).execute();

        String sql5 = "CREATE TABLE IF NOT EXISTS shop_members " +
                "(sign_loc_serialized TEXT not NULL, " +
                " uuid TEXT not NULL, " +
                " PRIMARY KEY ( sign_loc_serialized ));";
        DatabaseManager.getSqlite(false).prepareStatement(sql5).execute();
    }
}
