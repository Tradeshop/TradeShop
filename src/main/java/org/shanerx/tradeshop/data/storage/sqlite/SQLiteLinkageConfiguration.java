package org.shanerx.tradeshop.data.storage.sqlite;

import org.bukkit.World;
import org.shanerx.tradeshop.data.storage.LinkageConfiguration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SQLiteLinkageConfiguration implements LinkageConfiguration {

    private final String worldName;
    private Map<String, String> linkageData;
    private final DatabaseManager sqlite;

    public SQLiteLinkageConfiguration(World world) {
        this.worldName = world.getName();
        this.sqlite = DatabaseManager.getSqlite();

        try {
            createTableIfNotExists();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        load();
    }

    @Override
    public void save() {
        try (Connection conn = sqlite.setupConnection(true)) {
            String sql = "DELETE FROM shop_linkage WHERE world_name = '" + worldName + "';";

            sqlite.prepareStatement(conn, sql).executeUpdate();

            for (String chestData : linkageData.keySet()) {
                sqlite.prepareStatement(conn,
                                "INSERT INTO shop_linkage (chest_loc, sign_loc, world_name)"
                                + " VALUES ('" + chestData + "', '" + linkageData.get(chestData) + "', '" + worldName + "');")
                        .executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void load() {
        // stop accidental double loading (expensive!)
        if (linkageData != null) throw new UnsupportedOperationException("Cannot load twice (expensive operation)!");
        linkageData = new HashMap<>();

        try (Connection conn = sqlite.setupConnection(true)) {
            String sql = "SELECT * FROM shop_linkage WHERE world_name = '" + worldName + "';";
            ResultSet res = sqlite.prepareStatement(conn, sql).executeQuery();

            while (res.next()) {
                linkageData.put(res.getString("chest_loc"), res.getString("sign_loc"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, String> getLinkageData() {
        return linkageData;
    }

    private void createTableIfNotExists() throws SQLException {
        try (Connection conn = sqlite.setupConnection(true)) {
            String sql = "CREATE TABLE IF NOT EXISTS shop_linkage " +
                    "(chest_loc TEXT not NULL, " +
                    " sign_loc TEXT not NULL, " +
                    " world_name TEXT not NULL);";
            sqlite.prepareStatement(conn, sql).execute();
        } catch (SQLException e) {
            throw e;
        }
    }
}
