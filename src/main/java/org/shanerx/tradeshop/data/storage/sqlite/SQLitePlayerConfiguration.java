package org.shanerx.tradeshop.data.storage.sqlite;

import org.shanerx.tradeshop.data.storage.PlayerConfiguration;
import org.shanerx.tradeshop.player.PlayerSetting;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SQLitePlayerConfiguration implements PlayerConfiguration {

    private final UUID uuid;
    private final DatabaseManager sqlite;

    public SQLitePlayerConfiguration(UUID uuid) {
        this.uuid = uuid;
        this.sqlite = DatabaseManager.getSqlite();

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

        remove();

        try (Connection conn = sqlite.setupConnection(true)) {
            String sql = "INSERT INTO players (uuid, showInvolvedStatus, adminEnabled, multi) VALUES " +
                    "('" + uuid.toString() + "', " + (playerSetting.showInvolvedStatus() ? 1 : 0) + ", " + (playerSetting.isAdminEnabled() ? 1 : 0) + ", " + playerSetting.getMulti() + ");";
            sqlite.prepareStatement(conn, sql).executeUpdate();

            for (String ownedShop : playerSetting.getOwnedShops()) {
                sqlite.prepareStatement(conn,
                                "INSERT INTO players_owned_shops (uuid, shop)"
                                        + " VALUES ('" + uuid.toString() + "', '" + ownedShop + "');")
                        .executeUpdate();
            }

            for (String staffShop : playerSetting.getStaffShops()) {
                sqlite.prepareStatement(conn,
                                "INSERT INTO players_staff_shops (uuid, shop)"
                                    + " VALUES ('" + uuid.toString() + "', '" + staffShop + "');")
                        .executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PlayerSetting load() {
        try (Connection conn = sqlite.setupConnection(true)) {
            String sql = "SELECT * FROM players WHERE uuid = '" + uuid.toString() + "';";
            String sql2 = "SELECT * FROM players_owned_shops WHERE uuid = '" + uuid.toString() + "';";
            String sql3 = "SELECT * FROM players_staff_shops WHERE uuid = '" + uuid.toString() + "';";

            ResultSet res = sqlite.prepareStatement(conn, sql).executeQuery();
            ResultSet res2 = sqlite.prepareStatement(conn, sql2).executeQuery();
            ResultSet res3 = sqlite.prepareStatement(conn, sql3).executeQuery();

            if (!res.next()) return null;

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
        try (Connection conn = sqlite.setupConnection(true)) {
            String sql = "DELETE FROM players WHERE uuid = '" + uuid.toString() + "';";
            String sql2 = "DELETE FROM players_owned_shops WHERE uuid = '" + uuid.toString() + "';";
            String sql3 = "DELETE FROM players_staff_shops WHERE uuid = '" + uuid.toString() + "';";

            sqlite.prepareStatement(conn, sql).execute();
            sqlite.prepareStatement(conn, sql2).execute();
            sqlite.prepareStatement(conn, sql3).execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createTableIfNotExists() throws SQLException {
        try (Connection conn = sqlite.setupConnection(true)) {
            String sql = "CREATE TABLE IF NOT EXISTS players " +
                    "(uuid TEXT not NULL, " +
                    " showInvolvedStatus INTEGER, " +
                    " adminEnabled INTEGER, " +
                    " multi INTEGER, " +
                    " PRIMARY KEY ( uuid ));";
            sqlite.prepareStatement(conn, sql).execute();

            sql = "CREATE TABLE IF NOT EXISTS players_owned_shops " +
                    "(uuid TEXT not NULL, " +
                    " shop TEXT);";
            sqlite.prepareStatement(conn, sql).execute();

            sql = "CREATE TABLE IF NOT EXISTS players_staff_shops " +
                    "(uuid TEXT not NULL, " +
                    " shop TEXT);";
            sqlite.prepareStatement(conn, sql).execute();

        } catch (SQLException e) {
            throw e;
        }
    }
}
