package com.barangay.system.repository;

import com.barangay.system.database.DatabaseConnection;
import com.barangay.system.model.DocumentRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/*
 * DAO for the document_requests table.
 *
 * All SELECT queries JOIN with the users table so callers receive
 * the requester's username and full name without an extra query.
 *
 * Every DELETE is followed by resetAutoIncrement() so deleted IDs
 * are reused by the next inserted request.
 */
public class DocumentRequestRepository {

    // Shared SELECT fragment reused by all read methods
    private static final String SELECT_WITH_USER =
        "SELECT dr.id, dr.user_id, u.username, u.full_name, dr.document_type, "
      + "       dr.purpose, dr.status, dr.admin_remarks, dr.request_date, dr.processed_date "
      + "FROM document_requests dr "
      + "INNER JOIN users u ON dr.user_id = u.id ";

    // ----------------------------------------------------------
    // INSERT - save a new PENDING request
    // ----------------------------------------------------------

    public boolean save(DocumentRequest request) throws SQLException {
        String sql = "INSERT INTO document_requests (user_id, document_type, purpose, status) "
                   + "VALUES (?, ?, ?, ?)";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, request.getUserId());
            ps.setString(2, request.getDocumentType());
            ps.setString(3, request.getPurpose());
            ps.setString(4, DocumentRequest.STATUS_PENDING);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) { request.setId(keys.getInt(1)); }
                }
                return true;
            }
            return false;
        }
    }

    // ----------------------------------------------------------
    // SELECT - single request by primary key
    // ----------------------------------------------------------

    public DocumentRequest findById(int id) throws SQLException {
        String sql = SELECT_WITH_USER + "WHERE dr.id = ?";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { return mapRow(rs); }
            }
        }
        return null;
    }

    // ----------------------------------------------------------
    // SELECT - all requests in the system (admin view)
    // ----------------------------------------------------------

    public List<DocumentRequest> findAll() throws SQLException {
        String sql = SELECT_WITH_USER + "ORDER BY dr.id ASC";
        return executeListQuery(sql, null, -1);
    }

    // ----------------------------------------------------------
    // SELECT - all requests belonging to one user
    // ----------------------------------------------------------

    public List<DocumentRequest> findByUserId(int userId) throws SQLException {
        String sql = SELECT_WITH_USER
                   + "WHERE dr.user_id = ? ORDER BY dr.id ASC";
        return executeListQuery(sql, null, userId);
    }

    // ----------------------------------------------------------
    // SELECT - all requests with a given status
    // ----------------------------------------------------------

    public List<DocumentRequest> findByStatus(String status) throws SQLException {
        String sql = SELECT_WITH_USER
                   + "WHERE dr.status = ? ORDER BY dr.id ASC";
        return executeListQuery(sql, status, -1);
    }

    // ----------------------------------------------------------
    // SELECT - requests filtered by both owner and status
    // ----------------------------------------------------------

    public List<DocumentRequest> findByUserIdAndStatus(int userId, String status)
            throws SQLException {

        String sql = SELECT_WITH_USER
                   + "WHERE dr.user_id = ? AND dr.status = ? ORDER BY dr.id ASC";

        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<DocumentRequest> list = new ArrayList<DocumentRequest>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) { list.add(mapRow(rs)); }
            }
        }
        return list;
    }

    // ----------------------------------------------------------
    // UPDATE - change the purpose of a PENDING request
    // ----------------------------------------------------------

    public boolean updatePurpose(DocumentRequest request) throws SQLException {
        String sql = "UPDATE document_requests SET purpose = ? "
                   + "WHERE id = ? AND status = 'PENDING'";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, request.getPurpose());
            ps.setInt(2, request.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // ----------------------------------------------------------
    // UPDATE - set status, remarks, and processed_date (admin)
    // ----------------------------------------------------------

    public boolean updateStatus(int id, String newStatus, String adminRemarks)
            throws SQLException {

        String sql = "UPDATE document_requests "
                   + "SET status = ?, admin_remarks = ?, processed_date = ? "
                   + "WHERE id = ?";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, adminRemarks);
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ----------------------------------------------------------
    // DELETE - resident cancels their own PENDING request.
    // Resets AUTO_INCREMENT so the deleted ID is reused.
    // ----------------------------------------------------------

    public boolean deleteByIdAndUserId(int id, int userId) throws SQLException {
        String sql = "DELETE FROM document_requests "
                   + "WHERE id = ? AND user_id = ? AND status = 'PENDING'";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            boolean deleted = ps.executeUpdate() > 0;
            if (deleted) { resetAutoIncrement(conn); }
            return deleted;
        }
    }

    // ----------------------------------------------------------
    // DELETE - admin removes any request by id.
    // Resets AUTO_INCREMENT so the deleted ID is reused.
    // ----------------------------------------------------------

    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM document_requests WHERE id = ?";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            boolean deleted = ps.executeUpdate() > 0;
            if (deleted) { resetAutoIncrement(conn); }
            return deleted;
        }
    }

    // ----------------------------------------------------------
    // Resets AUTO_INCREMENT to (current MAX id + 1).
    // Value of 1 makes MySQL pick the next available slot,
    // effectively filling gaps left by deleted rows.
    // ----------------------------------------------------------

    private void resetAutoIncrement(Connection conn) throws SQLException {
        String sql = "ALTER TABLE document_requests AUTO_INCREMENT = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    // ----------------------------------------------------------
    // Shared helper for SELECT queries returning a list.
    // strParam used when filtering by String (e.g. status).
    // intParam used when filtering by int (e.g. userId); -1 = skip.
    // ----------------------------------------------------------

    private List<DocumentRequest> executeListQuery(
            String sql, String strParam, int intParam) throws SQLException {

        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<DocumentRequest> list = new ArrayList<DocumentRequest>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (strParam != null) {
                ps.setString(1, strParam);
            } else if (intParam > -1) {
                ps.setInt(1, intParam);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) { list.add(mapRow(rs)); }
            }
        }
        return list;
    }

    // ----------------------------------------------------------
    // Maps the current ResultSet row into a DocumentRequest
    // ----------------------------------------------------------

    private DocumentRequest mapRow(ResultSet rs) throws SQLException {
        DocumentRequest dr = new DocumentRequest();
        dr.setId(rs.getInt("id"));
        dr.setUserId(rs.getInt("user_id"));
        dr.setUsername(rs.getString("username"));
        dr.setFullName(rs.getString("full_name"));
        dr.setDocumentType(rs.getString("document_type"));
        dr.setPurpose(rs.getString("purpose"));
        dr.setStatus(rs.getString("status"));
        dr.setAdminRemarks(rs.getString("admin_remarks"));
        dr.setRequestDate(rs.getTimestamp("request_date"));
        dr.setProcessedDate(rs.getTimestamp("processed_date"));
        return dr;
    }
}