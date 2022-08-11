package org.shanerx.tradeshop.data.storage.sqlite;

import org.shanerx.tradeshop.data.storage.PlayerConfiguration;
import org.shanerx.tradeshop.player.PlayerSetting;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
            PreparedStatement ps = sqlite.prepareStatement(conn, sql);
            ps.executeUpdate();
            ps.close();

            for (String ownedShop : playerSetting.getOwnedShops()) {
                ps = sqlite.prepareStatement(conn,
                                "INSERT INTO players_owned_shops (uuid, shop)"
                                        + " VALUES ('" + uuid.toString() + "', '" + ownedShop + "');");
                ps.executeUpdate();
                ps.close();
            }

            for (String staffShop : playerSetting.getStaffShops()) {
                ps = sqlite.prepareStatement(conn,
                                "INSERT INTO players_staff_shops (uuid, shop)"
                                    + " VALUES ('" + uuid.toString() + "', '" + staffShop + "');");
                ps.executeUpdate();
                ps.close();
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

            PreparedStatement ps = sqlite.prepareStatement(conn, sql);
            PreparedStatement ps2 = sqlite.prepareStatement(conn, sql2);
            PreparedStatement ps3 = sqlite.prepareStatement(conn, sql3);

            ResultSet res = ps.executeQuery();
            ResultSet res2 = ps2.executeQuery();
            ResultSet res3 = ps3.executeQuery();

            if (!res.next()) {
                ps.close();
                ps2.close();
                ps3.close();

                return null;
            }

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

            ps.close();
            ps2.close();
            ps3.close();

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

            PreparedStatement ps = sqlite.prepareStatement(conn, sql);
            PreparedStatement ps2 = sqlite.prepareStatement(conn, sql2);
            PreparedStatement ps3 = sqlite.prepareStatement(conn, sql3);

            ps.execute();
            ps2.execute();
            ps3.execute();

            ps.close();
            ps2.close();
            ps3.close();

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

            PreparedStatement ps = sqlite.prepareStatement(conn, sql);
            ps.execute();
            ps.close();

            sql = "CREATE TABLE IF NOT EXISTS players_owned_shops " +
                    "(uuid TEXT not NULL, " +
                    " shop TEXT);";

            ps = sqlite.prepareStatement(conn, sql);
            ps.execute();
            ps.close();

            sql = "CREATE TABLE IF NOT EXISTS players_staff_shops " +
                    "(uuid TEXT not NULL, " +
                    " shop TEXT);";

            ps = sqlite.prepareStatement(conn, sql);
            ps.execute();
            ps.close();
        }
    }
}
