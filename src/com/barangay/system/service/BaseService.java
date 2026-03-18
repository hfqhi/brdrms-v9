package com.barangay.system.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Shared utilities for all service classes.
public abstract class BaseService {

    // Wraps any SQL list call and returns an empty list on failure.
    protected interface SqlSupplier<T> {
        List<T> get() throws SQLException;
    }

    protected <T> List<T> safeList(SqlSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (SQLException e) {
            System.out.println("WARN: " + e.getMessage());
            return new ArrayList<T>();
        }
    }
}