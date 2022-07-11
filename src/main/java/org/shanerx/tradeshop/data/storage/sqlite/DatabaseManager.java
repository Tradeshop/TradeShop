package org.shanerx.tradeshop.data.storage.sqlite;

import org.bukkit.Bukkit;
import org.shanerx.tradeshop.TradeShop;
import org.shanerx.tradeshop.utils.debug.DebugLevels;

import java.io.File;
import java.sql.*;

/**
 * This class talks to the SQLITE file through JDBC.
 * **No direct calls to JDBC should ever be made outside of this class!!!**
 *
 * Please remember to use this class responsively and NOT execute SQL queries which have not been prepared in
 * order to avoid SQL Injection vulnerabilities.
 */
public class DatabaseManager {

    private String dbpath;
    private String dburl;
    private File dbfile;
    private TradeShop plugin;

    /**
     * Creates instance of the DatabaseManager class.
     *
     * This does not initiate the connection; use {@ref setupConnection} for that.
     * @param path The .db path of the SQLite database file.
     */
    protected DatabaseManager(String path) {
        if (sqlite != null) throw new UnsupportedOperationException("Multiple initializations of DatabaseManager singleton.");

        this.dbpath =  path;
        this.dburl = "jdbc:sqlite:" + path;
        this.dbfile = new File(dbpath);

        this.plugin = (TradeShop) Bukkit.getPluginManager().getPlugin("TradeShop");

        sqlite = this;
    }

    /**
     * Sets up connection to the SQLite data file.
     * @param create Should the database file be created if non-existent?
     */
    protected Connection setupConnection(boolean create) throws SQLException {
        if (!dbfile.exists() && !create) throw new IllegalArgumentException("Database file is missing.");

        try {
            return DriverManager.getConnection(dburl);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a {@ref PreparedStatement} object and returns it for execution.
     *
     * Please do not execute queries yourself. Always run SQL statements through this method for security against
     * SQL Injections!
     * @param query The string containing the SQL query (or queries).
     * @return The prepared statement.
     * @throws SQLException
     */
    protected PreparedStatement prepareStatement(Connection conn, String query) throws SQLException {
        plugin.getDebugger().log("Issuing SQL Statement: [" + query + "]", DebugLevels.SQLITE);
        if (!conn.isValid(0)) {
            throw new IllegalStateException("No connection has been opened yet.");
        }

        return conn.prepareStatement(query);
    }

    private static DatabaseManager sqlite;

    protected static DatabaseManager getSqlite() {
        if (sqlite == null) {
            File dataDir = new File(Bukkit.getPluginManager().getPlugin("TradeShop").getDataFolder(), "Data");
            if (!dataDir.isDirectory()) dataDir.mkdirs();
            sqlite = new DatabaseManager(new File(dataDir, "database.db").getAbsolutePath());
        }
        return sqlite;
    }
}
