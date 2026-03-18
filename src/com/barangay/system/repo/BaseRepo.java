package com.barangay.system.repo;

import com.barangay.system.database.DbConnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;

// Shared utilities for all repo classes.
public abstract class BaseRepo {

    // Resets AUTO_INCREMENT after a hard delete so freed IDs are reused.
    protected void resetAutoIncrement(String table) throws SQLException {
        try (PreparedStatement ps = DbConnection.getInstance().get()
                .prepareStatement("ALTER TABLE " + table + " AUTO_INCREMENT = 1")) {
            ps.executeUpdate();
        }
    }
}