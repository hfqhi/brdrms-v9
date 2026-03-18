package com.barangay.system.model;

import java.sql.Timestamp;

public class DocRequest {

    public static final String PENDING  = "PENDING";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";

    private int       id;
    private int       residentId;
    private String    residentName;   // joined from tbl_residents
    private int       docTypeId;
    private String    docTypeName;    // joined from tbl_document_type
    private double    docFee;         // joined from tbl_document_type
    private String    purpose;
    private String    status;
    private String    adminRemarks;
    private Integer   adminId;        // nullable
    private String    adminName;      // joined via LEFT JOIN (nullable)
    private boolean   archived;
    private Timestamp requestDate;
    private Timestamp processedDate;

    public DocRequest() {}

    public DocRequest(int residentId, int docTypeId, String purpose) {
        this.residentId = residentId;
        this.docTypeId  = docTypeId;
        this.purpose    = purpose;
        this.status     = PENDING;
        this.archived   = false;
    }

    public DocRequest(int id, int residentId, String residentName,
                      int docTypeId, String docTypeName, double docFee,
                      String purpose, String status, String adminRemarks,
                      Integer adminId, String adminName, boolean archived,
                      Timestamp requestDate, Timestamp processedDate) {
        this.id            = id;
        this.residentId    = residentId;
        this.residentName  = residentName;
        this.docTypeId     = docTypeId;
        this.docTypeName   = docTypeName;
        this.docFee        = docFee;
        this.purpose       = purpose;
        this.status        = status;
        this.adminRemarks  = adminRemarks;
        this.adminId       = adminId;
        this.adminName     = adminName;
        this.archived      = archived;
        this.requestDate   = requestDate;
        this.processedDate = processedDate;
    }

    public int       getId()            { return id; }
    public int       getResidentId()    { return residentId; }
    public String    getResidentName()  { return residentName; }
    public int       getDocTypeId()     { return docTypeId; }
    public String    getDocTypeName()   { return docTypeName; }
    public double    getDocFee()        { return docFee; }
    public String    getPurpose()       { return purpose; }
    public String    getStatus()        { return status; }
    public String    getAdminRemarks()  { return adminRemarks; }
    public Integer   getAdminId()       { return adminId; }
    public String    getAdminName()     { return adminName; }
    public boolean   isArchived()       { return archived; }
    public Timestamp getRequestDate()   { return requestDate; }
    public Timestamp getProcessedDate() { return processedDate; }

    public void setId(int id)           { this.id = id; }
    public void setPurpose(String p)    { this.purpose = p; }
    public void setArchived(boolean a)  { this.archived = a; }

    public boolean isPending() { return PENDING.equals(status); }
}