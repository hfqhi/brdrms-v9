package com.barangay.system.repo;

import com.barangay.system.database.DbConnection;
import com.barangay.system.model.History;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// Append-only audit log — no UPDATE or DELETE operations.
public class HistoryRepo extends BaseRepo {

    private static final String BASE =
        "SELECT id, admin_id, admin_name, action, target_id, target_type, description, acted_at " +
        "FROM tbl_history ";

    public boolean save(History h) throws SQLException {
        String sql = "INSERT INTO tbl_history " +
                     "(admin_id, admin_name, action, target_id, target_type, description) VALUES (?,?,?,?,?,?)";
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (h.getAdminId() != null) ps.setInt(1, h.getAdminId());
            else ps.setNull(1, java.sql.Types.INTEGER);
            ps.setString(2, h.getAdminName());
            ps.setString(3, h.getAction());
            ps.setInt(4, h.getTargetId());
            ps.setString(5, h.getTargetType());
            ps.setString(6, h.getDescription());
            if (ps.executeUpdate() > 0) {
                try (ResultSet k = ps.getGeneratedKeys()) {
                    if (k.next()) h.setId(k.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    // Chronological order (oldest first).
    public List<History> findAll() throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        List<History> list = new ArrayList<History>();
        try (PreparedStatement ps = conn.prepareStatement(BASE + "ORDER BY acted_at ASC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // Keyword search across admin_name, action, and description — chronological.
    public List<History> search(String keyword) throws SQLException {
        String p = "%" + keyword + "%";
        Connection conn = DbConnection.getInstance().get();
        List<History> list = new ArrayList<History>();
        try (PreparedStatement ps = conn.prepareStatement(
                BASE + "WHERE admin_name LIKE ? OR action LIKE ? OR description LIKE ? " +
                "ORDER BY acted_at ASC")) {
            ps.setString(1, p); ps.setString(2, p); ps.setString(3, p);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // Filter by target_type (REQUEST, DOCTYPE, REGISTRATION, RESIDENT).
    public List<History> findByTargetType(String targetType) throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        List<History> list = new ArrayList<History>();
        try (PreparedStatement ps = conn.prepareStatement(
                BASE + "WHERE target_type=? ORDER BY acted_at ASC")) {
            ps.setString(1, targetType);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    private History mapRow(ResultSet rs) throws SQLException {
        return new History(
            rs.getInt("id"),
            (Integer) rs.getObject("admin_id"),
            rs.getString("admin_name"),
            rs.getString("action"),
            rs.getInt("target_id"),
            rs.getString("target_type"),
            rs.getString("description"),
            rs.getTimestamp("acted_at")
        );
    }
}