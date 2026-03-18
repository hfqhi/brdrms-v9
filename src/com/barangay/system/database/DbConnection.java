package com.barangay.system.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Singleton JDBC connection. Reconnects automatically if closed.
public class DbConnection {

    private static final String URL  = "jdbc:mysql://localhost:3306/db_brdrms_v1";
    private static final String USER = "root";
    private static final String PASS = "";

    private static DbConnection instance;
    private Connection conn;

    private DbConnection() throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver not found. Add mysql-connector-java to Libraries.", e);
        }
    }

    public static DbConnection getInstance() throws SQLException {
        if (instance == null || instance.conn.isClosed())
            instance = new DbConnection();
        return instance;
    }

    public Connection get() { return conn; }
}