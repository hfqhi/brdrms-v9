package com.barangay.system.repo;

import com.barangay.system.database.DbConnection;
import com.barangay.system.model.Registration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class RegistrationRepo extends BaseRepo {

    private static final String BASE =
        "SELECT id, username, password, full_name, contact_number, " +
        "address, status, submitted_at, reviewed_at, admin_id FROM tbl_registration ";

    public boolean save(Registration reg) throws SQLException {
        String sql = "INSERT INTO tbl_registration " +
                     "(username, password, full_name, contact_number, address) VALUES (?,?,?,?,?)";
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, reg.getUsername());
            ps.setString(2, reg.getPassword());
            ps.setString(3, reg.getFullName());
            ps.setString(4, reg.getContactNumber());
            ps.setString(5, reg.getAddress());
            if (ps.executeUpdate() > 0) {
                try (ResultSet k = ps.getGeneratedKeys()) {
                    if (k.next()) reg.setId(k.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    public List<Registration> findPending() throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                BASE + "WHERE status='PENDING' ORDER BY submitted_at ASC")) {
            return buildList(ps);
        }
    }

    public Registration findById(int id) throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(BASE + "WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public boolean pendingUsernameExists(String username) throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM tbl_registration WHERE username=? AND status='PENDING'")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean updateStatus(int id, String status, Timestamp reviewedAt, Integer adminId)
            throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE tbl_registration SET status=?, reviewed_at=?, admin_id=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setTimestamp(2, reviewedAt);
            if (adminId != null) ps.setInt(3, adminId);
            else ps.setNull(3, java.sql.Types.INTEGER);
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        }
    }

    private List<Registration> buildList(PreparedStatement ps) throws SQLException {
        List<Registration> list = new ArrayList<Registration>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private Registration mapRow(ResultSet rs) throws SQLException {
        return new Registration(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("full_name"),
            rs.getString("contact_number"),
            rs.getString("address"),
            rs.getString("status"),
            rs.getTimestamp("submitted_at"),
            rs.getTimestamp("reviewed_at"),
            (Integer) rs.getObject("admin_id")
        );
    }
}