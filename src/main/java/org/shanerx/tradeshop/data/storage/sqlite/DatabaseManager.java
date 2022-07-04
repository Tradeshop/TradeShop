package org.shanerx.tradeshop.data.storage.sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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

    private Connection conn = null;

    /**
     * Creates instance of the DatabaseManager class.
     *
     * This does not initiate the connection; use {@ref setupConnection} for that.
     * @param path The .db path of the SQLite database file.
     */
    public DatabaseManager(String path) {
        this.dbpath =  path;
        this.dburl = "jdbc:sqlite:" + path;
        this.dbfile = new File(dbpath);
    }

    /**
     * Sets up connection to the SQLite data file.
     * @param create Should the database file be created if non-existent?
     */
    public void setupConnection(boolean create) {
        if (conn != null) {
            throw new IllegalStateException("A connection has already been opened.");
        }
        else if (!dbfile.exists() && create) {
            throw new IllegalArgumentException("Database file is missing.");
        }

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
    public void closeConnection() throws SQLException {
        if (conn != null) {
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
    public PreparedStatement prepareStatement(String query) throws SQLException {
        if (conn == null) {
            throw new IllegalStateException("No connection has been opened yet.");
        }

        return conn.prepareStatement(query);
    }
}
