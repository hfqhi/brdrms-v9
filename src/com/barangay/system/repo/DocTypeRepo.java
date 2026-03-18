package com.barangay.system.repo;

import com.barangay.system.database.DbConnection;
import com.barangay.system.model.DocType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DocTypeRepo extends BaseRepo {

    private static final String BASE =
        "SELECT id, type_name, description, fee, is_archived, updated_at FROM tbl_document_type ";

    public boolean save(DocType dt) throws SQLException {
        String sql = "INSERT INTO tbl_document_type (type_name, description, fee) VALUES (?,?,?)";
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dt.getTypeName());
            ps.setString(2, dt.getDescription());
            ps.setDouble(3, dt.getFee());
            if (ps.executeUpdate() > 0) {
                try (ResultSet k = ps.getGeneratedKeys()) {
                    if (k.next()) dt.setId(k.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    public List<DocType> findAll() throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(BASE + "ORDER BY id ASC")) {
            return buildList(ps);
        }
    }

    public List<DocType> findAllActive() throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                BASE + "WHERE is_archived = 0 ORDER BY id ASC")) {
            return buildList(ps);
        }
    }

    public DocType findById(int id) throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(BASE + "WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public boolean update(DocType dt) throws SQLException {
        String sql = "UPDATE tbl_document_type " +
                     "SET type_name=?, description=?, fee=?, updated_at=? WHERE id=?";
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dt.getTypeName());
            ps.setString(2, dt.getDescription());
            ps.setDouble(3, dt.getFee());
            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            ps.setInt(5, dt.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean archiveById(int id) throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE tbl_document_type SET is_archived=1, updated_at=? WHERE id=?")) {
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean restoreById(int id) throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE tbl_document_type SET is_archived=0, updated_at=? WHERE id=?")) {
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteById(int id) throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM tbl_document_type WHERE id=?")) {
            ps.setInt(1, id);
            boolean done = ps.executeUpdate() > 0;
            if (done) resetAutoIncrement("tbl_document_type");
            return done;
        }
    }

    private List<DocType> buildList(PreparedStatement ps) throws SQLException {
        List<DocType> list = new ArrayList<DocType>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private DocType mapRow(ResultSet rs) throws SQLException {
        return new DocType(
            rs.getInt("id"),
            rs.getString("type_name"),
            rs.getString("description"),
            rs.getDouble("fee"),
            rs.getInt("is_archived") == 1,
            rs.getTimestamp("updated_at")
        );
    }
}