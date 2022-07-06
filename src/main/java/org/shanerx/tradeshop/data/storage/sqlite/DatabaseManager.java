package org.shanerx.tradeshop.data.storage.sqlite;

import org.bukkit.Bukkit;

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

    private Connection conn;

    /**
     * Creates instance of the DatabaseManager class.
     *
     * This does not initiate the connection; use {@ref setupConnection} for that.
     * @param path The .db path of the SQLite database file.
     */
    protected DatabaseManager(String path) {
        this.dbpath =  path;
        this.dburl = "jdbc:sqlite:" + path;
        this.dbfile = new File(dbpath);
    }

    /**
     * Sets up connection to the SQLite data file.
     * @param create Should the database file be created if non-existent?
     */
    private void setupConnection(boolean create) throws SQLException {
        if (!dbfile.exists() && !create) {
            throw new IllegalArgumentException("Database file is missing.");
        }
        else if (hasOpenConnection()) return;


        try {
            conn = DriverManager.getConnection(dburl);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Closes connection.
     *
     * What happens when destroying the connection while database transactions are still being carried out is
     * implementation defined and should hence be avoided.
     * @throws SQLException
     */
    protected void closeConnection() throws SQLException {
        if (hasOpenConnection()) {
            conn.close();
            conn = null;
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
    protected PreparedStatement prepareStatement(String query) throws SQLException {
        if (!hasOpenConnection()) {
            throw new IllegalStateException("No connection has been opened yet.");
        }

        return conn.prepareStatement(query, ResultSet.CONCUR_READ_ONLY);
    }

    /**
     * Returns true if the connection is currently open and valid.
     * @return whether it is open
     */
    protected boolean hasOpenConnection() {
        try {
            return conn != null && !conn.isClosed() && conn.isValid(0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static DatabaseManager sqlite;

    protected static DatabaseManager getSqlite() {
        return getSqlite(true);
    }

    protected static DatabaseManager getSqlite(boolean shouldOpen) {
        if (sqlite == null) {
            File dataDir = new File(Bukkit.getPluginManager().getPlugin("TradeShop").getDataFolder(), "Data");
            if (!dataDir.isDirectory()) dataDir.mkdirs();
            sqlite = new DatabaseManager(new File(dataDir, "database.db").getAbsolutePath());
        }
        if (!sqlite.hasOpenConnection() && shouldOpen) {
            try {
                sqlite.setupConnection(true);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return sqlite;
    }
}
