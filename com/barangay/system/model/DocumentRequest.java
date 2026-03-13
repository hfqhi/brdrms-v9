package com.barangay.system.model;

import java.sql.Timestamp;

/*
 * POJO representing a row in the document_requests table.
 *
 * Document types are defined in DOCUMENT_TYPES[].
 * Status values match the ENUM column: PENDING, APPROVED, REJECTED.
 */
public class DocumentRequest {

    // Status constants matching the DB ENUM column
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    // Available document types (index = menu choice minus 1)
    public static final String[] DOCUMENT_TYPES = {
        "Barangay Clearance",
        "Certificate of Residency",
        "Certificate of Indigency",
        "Business Permit Endorsement",
        "Certificate of Good Moral Character"
    };

    // Fields matching the document_requests table columns
    private int id;
    private int userId;
    private String username;       // joined from users - display only
    private String fullName;       // joined from users - display only
    private String documentType;
    private String purpose;
    private String status;
    private String adminRemarks;
    private Timestamp requestDate;
    private Timestamp processedDate;

    // Default no-arg constructor
    public DocumentRequest() {
    }

    // Constructor used when submitting a new request
    public DocumentRequest(int userId, String documentType, String purpose) {
        this.userId = userId;
        this.documentType = documentType;
        this.purpose = purpose;
        this.status = STATUS_PENDING;
    }

    // ----------------------------------------------------------
    // Getters and Setters
    // ----------------------------------------------------------
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int uid) {
        this.userId = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String u) {
        this.username = u;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fn) {
        this.fullName = fn;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String dt) {
        this.documentType = dt;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String p) {
        this.purpose = p;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminRemarks() {
        return adminRemarks;
    }

    public void setAdminRemarks(String r) {
        this.adminRemarks = r;
    }

    public Timestamp getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Timestamp t) {
        this.requestDate = t;
    }

    public Timestamp getProcessedDate() {
        return processedDate;
    }

    public void setProcessedDate(Timestamp t) {
        this.processedDate = t;
    }

    @Override
    public String toString() {
        return "DocumentRequest{id=" + id + ", userId=" + userId
                + ", documentType='" + documentType + "', status='" + status + "'}";
    }
}
