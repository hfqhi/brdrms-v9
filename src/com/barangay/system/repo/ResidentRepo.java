package com.barangay.system.repo;

import com.barangay.system.database.DbConnection;
import com.barangay.system.model.Resident;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ResidentRepo extends BaseRepo {

    private static final String BASE =
        "SELECT id, username, password, full_name, contact_number, " +
        "address, is_archived, created_at, updated_at FROM tbl_residents ";

    // CREATE — called when a registration is approved.
    public boolean save(Resident r) throws SQLException {
        String sql = "INSERT INTO tbl_residents " +
                     "(username, password, full_name, contact_number, address) VALUES (?,?,?,?,?)";
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getUsername());
            ps.setString(2, r.getPassword());
            ps.setString(3, r.getFullName());
            ps.setString(4, r.getContactNumber());
            ps.setString(5, r.getAddress());
            if (ps.executeUpdate() > 0) {
                try (ResultSet k = ps.getGeneratedKeys()) {
                    if (k.next()) r.setId(k.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    // READ — active residents only (login + default list).
    public Resident findByUsername(String username) throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                BASE + "WHERE username = ? AND is_archived = 0")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public Resident findById(int id) throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(BASE + "WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<Resident> findAll() throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                BASE + "WHERE is_archived = 0 ORDER BY id ASC")) {
            return buildList(ps);
        }
    }

    // READ — archived residents (admin archive view).
    public List<Resident> findAllArchived() throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                BASE + "WHERE is_archived = 1 ORDER BY id ASC")) {
            return buildList(ps);
        }
    }

    public List<Resident> search(String keyword) throws SQLException {
        String p = "%" + keyword + "%";
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                BASE + "WHERE is_archived = 0 AND " +
                "(full_name LIKE ? OR username LIKE ? OR contact_number LIKE ?) ORDER BY id ASC")) {
            ps.setString(1, p); ps.setString(2, p); ps.setString(3, p);
            return buildList(ps);
        }
    }

    public boolean usernameExists(String username) throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM tbl_residents WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // UPDATE — profile edit + sets updated_at to NOW().
    public boolean update(Resident r) throws SQLException {
        String sql = "UPDATE tbl_residents " +
                     "SET full_name=?, contact_number=?, address=?, password=?, updated_at=? WHERE id=?";
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getFullName());
            ps.setString(2, r.getContactNumber());
            ps.setString(3, r.getAddress());
            ps.setString(4, r.getPassword());
            ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            ps.setInt(6, r.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // ARCHIVE — soft delete.
    public boolean archiveById(int id) throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE tbl_residents SET is_archived=1, updated_at=? WHERE id=?")) {
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    // RESTORE — un-archive.
    public boolean restoreById(int id) throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE tbl_residents SET is_archived=0, updated_at=? WHERE id=?")) {
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    // DELETE — hard delete + AUTO_INCREMENT reset.
    public boolean deleteById(int id) throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM tbl_residents WHERE id=?")) {
            ps.setInt(1, id);
            boolean done = ps.executeUpdate() > 0;
            if (done) resetAutoIncrement("tbl_residents");
            return done;
        }
    }

    private List<Resident> buildList(PreparedStatement ps) throws SQLException {
        List<Resident> list = new ArrayList<Resident>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private Resident mapRow(ResultSet rs) throws SQLException {
        return new Resident(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("full_name"),
            rs.getString("contact_number"),
            rs.getString("address"),
            rs.getInt("is_archived") == 1,
            rs.getTimestamp("created_at"),
            rs.getTimestamp("updated_at")
        );
    }
}