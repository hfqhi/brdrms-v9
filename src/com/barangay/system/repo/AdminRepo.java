package com.barangay.system.repo;

import com.barangay.system.database.DbConnection;
import com.barangay.system.model.Admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AdminRepo extends BaseRepo {

    private static final String BASE =
        "SELECT id, username, password, full_name, position, created_at FROM tbl_admin ";

    // CREATE — used by Setup.java.
    public boolean save(Admin admin) throws SQLException {
        String sql = "INSERT INTO tbl_admin (username, password, full_name, position) VALUES (?,?,?,?)";
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, admin.getUsername());
            ps.setString(2, admin.getPassword());
            ps.setString(3, admin.getFullName());
            ps.setString(4, admin.getPosition());
            if (ps.executeUpdate() > 0) {
                try (ResultSet k = ps.getGeneratedKeys()) {
                    if (k.next()) admin.setId(k.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    public Admin findByUsername(String username) throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(BASE + "WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public Admin findById(int id) throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(BASE + "WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public boolean usernameExists(String username) throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM tbl_admin WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // UPDATE — password change only.
    public boolean updatePassword(int id, String hashedPassword) throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE tbl_admin SET password = ? WHERE id = ?")) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Admin mapRow(ResultSet rs) throws SQLException {
        return new Admin(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("full_name"),
            rs.getString("position"),
            rs.getTimestamp("created_at")
        );
    }
}