package org.shanerx.tradeshop.data.storage.sqlite;

import org.shanerx.tradeshop.data.storage.PlayerConfiguration;
import org.shanerx.tradeshop.player.PlayerSetting;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SQLitePlayerConfiguration implements PlayerConfiguration {

    private UUID uuid;
    private static DatabaseManager database;

    public SQLitePlayerConfiguration(UUID uuid) {
        this.uuid = uuid;
        try {
            createTableIfNotExists();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(PlayerSetting playerSetting) {
        if (!playerSetting.getUuid().equals(uuid)) {
            throw new IllegalArgumentException("uuid of playerSetting does not match uuid field.");
        }

        String sql = "UPDATE players SET" +
                " showInvolvedStatus = " + (playerSetting.showInvolvedStatus() ? 1 : 0) + "," +
                " adminEnabled = " + (playerSetting.isAdminEnabled() ? 1 : 0) + "," +
                " multi = " + playerSetting.getMulti() + " " +
                "WHERE uuid = '" + uuid.toString() + "';";
        String sql2 = "DELETE FROM players_owned_shops WHERE uuid = '" + uuid.toString() + "';";
        String sql3 = "DELETE FROM players_staff_shops WHERE uuid = '" + uuid.toString() + "';";
        try {
            DatabaseManager.getSqlite(true).prepareStatement(sql).executeUpdate();
            DatabaseManager.getSqlite(false).prepareStatement(sql2).executeUpdate();
            DatabaseManager.getSqlite(false).prepareStatement(sql3).executeUpdate();

            for (String ownedShop : playerSetting.getOwnedShops()) {
                DatabaseManager.getSqlite(false)
                        .prepareStatement("INSERT INTO players_owned_shops (uuid, shop)"
                                + " VALUES ('" + uuid.toString() + "', '" + ownedShop + "');")
                        .executeUpdate();
            }

            for (String staffShop : playerSetting.getStaffShops()) {
                DatabaseManager.getSqlite(false)
                        .prepareStatement("INSERT INTO players_staff_shops (uuid, shop)"
                                + " VALUES ('" + uuid.toString() + "', '" + staffShop + "');")
                        .executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PlayerSetting load() {
        String sql = "SELECT * FROM players WHERE uuid = '" + uuid.toString() + "';";
        String sql2 = "SELECT * FROM players_owned_shops WHERE uuid = '" + uuid.toString() + "';";
        String sql3 = "SELECT * FROM players_staff_shops WHERE uuid = '" + uuid.toString() + "';";

        ResultSet res, res2, res3;

        try {
            res = DatabaseManager.getSqlite(true).prepareStatement(sql).executeQuery();
            res2 = DatabaseManager.getSqlite(false).prepareStatement(sql2).executeQuery();
            res3 = DatabaseManager.getSqlite(false).prepareStatement(sql3).executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            int len = DatabaseManager.resultsLegth(res);
            if (len == 0 || !res.next()) return null;
            else if (len > 1)
                throw new IllegalStateException("SQLite  Database cannot have more than one entry per player. This is likely a bug...");

            PlayerSetting playerSetting = new PlayerSetting(uuid);
            playerSetting.setMulti(res.getInt("multi"));
            playerSetting.setAdminEnabled(res.getBoolean("adminEnabled"));
            playerSetting.setShowInvolvedStatus(res.getBoolean("showInvolvedStatus"));

            while (res2.next()) {
                playerSetting.getOwnedShops().add(res2.getString("shop"));
            }

            while (res3.next()) {
                playerSetting.getStaffShops().add(res3.getString("shop"));
            }

            return playerSetting;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void remove() {
        String sql = "DELETE FROM players WHERE uuid = '" + uuid.toString() + "';";
        String sql2 = "DELETE FROM players_owned_shops WHERE uuid = '" + uuid.toString() + "';";
        String sql3 = "DELETE FROM players_staff_shops WHERE uuid = '" + uuid.toString() + "';";

        try {
            DatabaseManager.getSqlite(true).prepareStatement(sql).execute();
            DatabaseManager.getSqlite(false).prepareStatement(sql2).execute();
            DatabaseManager.getSqlite(false).prepareStatement(sql3).execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS players " +
                "(uuid VARCHAR(255) not NULL, " +
                " showInvolvedStatus INTEGER, " +
                " adminEnabled INTEGER, " +
                " multi INTEGER, " +
                " PRIMARY KEY ( uuid ));";
        DatabaseManager.getSqlite(true).prepareStatement(sql).execute();

        sql = "CREATE TABLE IF NOT EXISTS players_owned_shops " +
                "(uuid VARCHAR(255) not NULL, " +
                " shop VARCHAR(255));";
        DatabaseManager.getSqlite(false).prepareStatement(sql).execute();

        sql = "CREATE TABLE IF NOT EXISTS players_staff_shops " +
                "(uuid VARCHAR(255) not NULL, " +
                " shop VARCHAR(255));";
        DatabaseManager.getSqlite(false).prepareStatement(sql).execute();
    }
}
