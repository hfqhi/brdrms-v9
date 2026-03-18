package com.barangay.system.repo;

import com.barangay.system.database.DbConnection;
import com.barangay.system.model.DocRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DocRequestRepo extends BaseRepo {

    private static final String BASE =
        "SELECT dr.id, dr.resident_id, r.full_name AS res_name, " +
        "       dr.doc_type_id, dt.type_name, dt.fee, " +
        "       dr.purpose, dr.status, dr.admin_remarks, " +
        "       dr.admin_id, a.full_name AS adm_name, " +
        "       dr.is_archived, dr.request_date, dr.processed_date " +
        "FROM tbl_document_request dr " +
        "INNER JOIN tbl_residents r      ON dr.resident_id  = r.id " +
        "INNER JOIN tbl_document_type dt ON dr.doc_type_id  = dt.id " +
        "LEFT  JOIN tbl_admin a          ON dr.admin_id     = a.id ";

    public boolean save(DocRequest req) throws SQLException {
        String sql = "INSERT INTO tbl_document_request " +
                     "(resident_id, doc_type_id, purpose, status) VALUES (?,?,?,'PENDING')";
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, req.getResidentId());
            ps.setInt(2, req.getDocTypeId());
            ps.setString(3, req.getPurpose());
            if (ps.executeUpdate() > 0) {
                try (ResultSet k = ps.getGeneratedKeys()) {
                    if (k.next()) req.setId(k.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    public DocRequest findById(int id) throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                BASE + "WHERE dr.id=? AND dr.is_archived=0")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public DocRequest findByIdAny(int id) throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(BASE + "WHERE dr.id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<DocRequest> findAll() throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                BASE + "WHERE dr.is_archived=0 ORDER BY dr.id ASC")) {
            return buildList(ps);
        }
    }

    public List<DocRequest> findAllArchived() throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                BASE + "WHERE dr.is_archived=1 ORDER BY dr.id ASC")) {
            return buildList(ps);
        }
    }

    public List<DocRequest> findByResidentId(int residentId) throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                BASE + "WHERE dr.resident_id=? AND dr.is_archived=0 ORDER BY dr.id ASC")) {
            ps.setInt(1, residentId);
            return buildList(ps);
        }
    }

    public List<DocRequest> findByStatus(String status) throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                BASE + "WHERE dr.status=? AND dr.is_archived=0 ORDER BY dr.id ASC")) {
            ps.setString(1, status);
            return buildList(ps);
        }
    }

    public List<DocRequest> findByResidentAndStatus(int residentId, String status) throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                BASE + "WHERE dr.resident_id=? AND dr.status=? AND dr.is_archived=0 ORDER BY dr.id ASC")) {
            ps.setInt(1, residentId); ps.setString(2, status);
            return buildList(ps);
        }
    }

    public List<DocRequest> searchAll(String keyword) throws SQLException {
        String p = "%" + keyword + "%";
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                BASE + "WHERE dr.is_archived=0 AND " +
                "(r.full_name LIKE ? OR dt.type_name LIKE ? OR dr.purpose LIKE ?) ORDER BY dr.id ASC")) {
            ps.setString(1, p); ps.setString(2, p); ps.setString(3, p);
            return buildList(ps);
        }
    }

    public List<DocRequest> searchByResident(String keyword, int residentId) throws SQLException {
        String p = "%" + keyword + "%";
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                BASE + "WHERE dr.resident_id=? AND dr.is_archived=0 AND " +
                "(dt.type_name LIKE ? OR dr.purpose LIKE ?) ORDER BY dr.id ASC")) {
            ps.setInt(1, residentId); ps.setString(2, p); ps.setString(3, p);
            return buildList(ps);
        }
    }

    public boolean updatePurpose(int id, String purpose) throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE tbl_document_request SET purpose=? WHERE id=? AND status='PENDING' AND is_archived=0")) {
            ps.setString(1, purpose); ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int id, String status, String remarks, int adminId) throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE tbl_document_request " +
                "SET status=?, admin_remarks=?, admin_id=?, processed_date=? WHERE id=? AND is_archived=0")) {
            ps.setString(1, status);
            ps.setString(2, remarks == null ? "" : remarks.trim());
            ps.setInt(3, adminId);
            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            ps.setInt(5, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean archiveByResidentId(int id, int residentId) throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE tbl_document_request SET is_archived=1 " +
                "WHERE id=? AND resident_id=? AND status='PENDING' AND is_archived=0")) {
            ps.setInt(1, id); ps.setInt(2, residentId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean archiveById(int id) throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE tbl_document_request SET is_archived=1 WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean restoreById(int id) throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE tbl_document_request SET is_archived=0 WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteById(int id) throws SQLException {
        Connection conn = DbConnection.getInstance().get();
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM tbl_document_request WHERE id=?")) {
            ps.setInt(1, id);
            boolean done = ps.executeUpdate() > 0;
            if (done) resetAutoIncrement("tbl_document_request");
            return done;
        }
    }

    private List<DocRequest> buildList(PreparedStatement ps) throws SQLException {
        List<DocRequest> list = new ArrayList<DocRequest>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private DocRequest mapRow(ResultSet rs) throws SQLException {
        return new DocRequest(
            rs.getInt("id"),
            rs.getInt("resident_id"),
            rs.getString("res_name"),
            rs.getInt("doc_type_id"),
            rs.getString("type_name"),
            rs.getDouble("fee"),
            rs.getString("purpose"),
            rs.getString("status"),
            rs.getString("admin_remarks"),
            (Integer) rs.getObject("admin_id"),
            rs.getString("adm_name"),
            rs.getInt("is_archived") == 1,
            rs.getTimestamp("request_date"),
            rs.getTimestamp("processed_date")
        );
    }
}