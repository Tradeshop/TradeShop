package org.shanerx.tradeshop.data.storage.sqlite;

import org.bukkit.World;
import org.shanerx.tradeshop.data.storage.LinkageConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SQLiteLinkageConfiguration implements LinkageConfiguration {

    private final String worldName;
    private Map<String, String> linkageData;
    private final DatabaseManager sqlite;
    private final JobsDispatch dispatch;

    protected SQLiteLinkageConfiguration(World world) {
        this.worldName = world.getName();
        this.sqlite = DatabaseManager.getSqlite();
        this.dispatch = this.sqlite.getJobsDispatch();

        try {
            createTableIfNotExists();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        load();
    }

    @Override
    public void save() {
        try {
            Connection conn = sqlite.setupConnection(true);
            String sql = "DELETE FROM shop_linkage WHERE world_name = '" + worldName + "';";

            PreparedStatement ps = sqlite.prepareStatement(conn, sql);
            dispatch.enqueueJob(ps);

            for (String chestData : linkageData.keySet()) {
                ps = sqlite.prepareStatement(conn,
                                "INSERT INTO shop_linkage (chest_loc, sign_loc, world_name)"
                                + " VALUES ('" + chestData + "', '" + linkageData.get(chestData) + "', '" + worldName + "');");
                dispatch.enqueueJob(ps);
            }

            dispatch.runDispatcher();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void load() { // TODO: BROKEN
        // stop accidental double loading (expensive!)
        if (linkageData != null) throw new UnsupportedOperationException("Cannot load twice (expensive operation)!");
        linkageData = new HashMap<>();

        try {
            Connection conn = sqlite.setupConnection(true);

            String sql = "SELECT * FROM shop_linkage WHERE world_name = '" + worldName + "';";
            PreparedStatement ps = sqlite.prepareStatement(conn, sql);
            ResultSet res = ps.executeQuery();

            while (res.next()) {
                linkageData.put(res.getString("chest_loc"), res.getString("sign_loc"));
            }

            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, String> getLinkageData() {
        return linkageData;
    }

    private void createTableIfNotExists() throws SQLException {
        try {
            Connection conn = sqlite.setupConnection(true);

            String sql = "CREATE TABLE IF NOT EXISTS shop_linkage " +
                    "(chest_loc TEXT not NULL, " +
                    " sign_loc TEXT not NULL, " +
                    " world_name TEXT not NULL);";
            PreparedStatement ps = sqlite.prepareStatement(conn, sql);
            dispatch.enqueueJob(ps);
            dispatch.runDispatcher();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
