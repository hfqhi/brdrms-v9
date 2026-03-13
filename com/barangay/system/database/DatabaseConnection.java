package com.barangay.system.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/*
 * Singleton class that manages the single JDBC connection to MySQL.
 *
 * Usage:
 *   Connection conn = DatabaseConnection.getInstance().getConnection();
 */
public class DatabaseConnection {

    // JDBC URL - points to barangay_db on localhost port 3306
    private static final String URL = "jdbc:mysql://localhost:3306/barangay_db";

    // Default XAMPP MySQL username
    private static final String DB_USER = "root";

    // Default XAMPP MySQL password (empty by default)
    private static final String DB_PASS = "";

    // The single shared instance
    private static DatabaseConnection instance;

    // The active JDBC connection held by this singleton
    private Connection connection;

    // ----------------------------------------------------------
    // Private constructor - only getInstance() may create one
    // ----------------------------------------------------------
    private DatabaseConnection() throws SQLException {
        try {
            // Load the MySQL JDBC driver (required for Java 6 / NetBeans 8.2)
            Class.forName("com.mysql.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, DB_USER, DB_PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "MySQL JDBC Driver not found. "
                    + "Add mysql-connector-java to project libraries.", e);
        }
    }

    // ----------------------------------------------------------
    // Returns the singleton, creating it if the connection is
    // null or has been closed
    // ----------------------------------------------------------
    public static DatabaseConnection getInstance() throws SQLException {
        if (instance == null || instance.connection.isClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    // ----------------------------------------------------------
    // Returns the raw Connection for use in repository classes
    // ----------------------------------------------------------
    public Connection getConnection() {
        return connection;
    }
}
