package com.barangay.system.repository;

import com.barangay.system.database.DatabaseConnection;
import com.barangay.system.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/*
 * DAO for the users table.
 * All methods use try-with-resources so every PreparedStatement
 * and ResultSet is closed automatically, even on exceptions.
 *
 * Every DELETE is followed by resetAutoIncrement() so deleted IDs
 * are reused by the next inserted row instead of skipping ahead.
 */
public class UserRepository {

    // ----------------------------------------------------------
    // INSERT - save a new user row
    // ----------------------------------------------------------
    public boolean save(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password, full_name, address, role) "
                + "VALUES (?, ?, ?, ?, ?)";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getAddress());
            ps.setString(5, user.getRole());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                // Retrieve the auto-generated primary key and set it on the object
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        user.setId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    // ----------------------------------------------------------
    // SELECT - find by username (used for login)
    // ----------------------------------------------------------
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password, full_name, address, role, created_at "
                + "FROM users WHERE username = ?";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    // ----------------------------------------------------------
    // SELECT - find by primary key
    // ----------------------------------------------------------
    public User findById(int id) throws SQLException {
        String sql = "SELECT id, username, password, full_name, address, role, created_at "
                + "FROM users WHERE id = ?";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    // ----------------------------------------------------------
    // SELECT - all users ordered by id ascending (admin view)
    // ----------------------------------------------------------
    public List<User> findAll() throws SQLException {
        String sql = "SELECT id, username, password, full_name, address, role, created_at "
                + "FROM users ORDER BY id ASC";

        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<User> users = new ArrayList<User>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        }
        return users;
    }

    // ----------------------------------------------------------
    // CHECK - returns true if the username is already taken
    // ----------------------------------------------------------
    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // ----------------------------------------------------------
    // UPDATE - change full_name, address, and password
    // ----------------------------------------------------------
    public boolean update(User user) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, address = ?, password = ? WHERE id = ?";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getAddress());
            ps.setString(3, user.getPassword());
            ps.setInt(4, user.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // ----------------------------------------------------------
    // DELETE - remove user row; cascades to document_requests.
    // Resets AUTO_INCREMENT afterward so the next inserted user
    // reuses the lowest available ID.
    // ----------------------------------------------------------
    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            boolean deleted = ps.executeUpdate() > 0;
            if (deleted) {
                resetAutoIncrement(conn);
            }
            return deleted;
        }
    }

    // ----------------------------------------------------------
    // Resets AUTO_INCREMENT to (current MAX id + 1).
    // MySQL treats value 1 as "use the next available slot"
    // when rows already exist, effectively filling any gaps.
    // ----------------------------------------------------------
    private void resetAutoIncrement(Connection conn) throws SQLException {
        String sql = "ALTER TABLE users AUTO_INCREMENT = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    // ----------------------------------------------------------
    // Maps the current ResultSet row into a User object
    // ----------------------------------------------------------
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFullName(rs.getString("full_name"));
        user.setAddress(rs.getString("address"));
        user.setRole(rs.getString("role"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }
}
