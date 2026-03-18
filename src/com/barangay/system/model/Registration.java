package com.barangay.system.model;

import java.sql.Timestamp;

public class Registration {

    public static final String PENDING  = "PENDING";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";

    private int       id;
    private String    username;
    private String    password;        // BCrypt hash
    private String    fullName;
    private String    contactNumber;
    private String    address;
    private String    status;
    private Timestamp submittedAt;
    private Timestamp reviewedAt;
    private Integer   adminId;

    public Registration() {}

    public Registration(String username, String password, String fullName,
                        String contactNumber, String address) {
        this.username      = username;
        this.password      = password;
        this.fullName      = fullName;
        this.contactNumber = contactNumber;
        this.address       = address;
        this.status        = PENDING;
    }

    public Registration(int id, String username, String password,
                        String fullName, String contactNumber, String address,
                        String status, Timestamp submittedAt,
                        Timestamp reviewedAt, Integer adminId) {
        this.id            = id;
        this.username      = username;
        this.password      = password;
        this.fullName      = fullName;
        this.contactNumber = contactNumber;
        this.address       = address;
        this.status        = status;
        this.submittedAt   = submittedAt;
        this.reviewedAt    = reviewedAt;
        this.adminId       = adminId;
    }

    public int       getId()            { return id; }
    public String    getUsername()      { return username; }
    public String    getPassword()      { return password; }
    public String    getFullName()      { return fullName; }
    public String    getContactNumber() { return contactNumber; }
    public String    getAddress()       { return address; }
    public String    getStatus()        { return status; }
    public Timestamp getSubmittedAt()   { return submittedAt; }
    public Timestamp getReviewedAt()    { return reviewedAt; }
    public Integer   getAdminId()       { return adminId; }

    public void setId(int id)               { this.id = id; }
    public void setStatus(String s)         { this.status = s; }
    public void setReviewedAt(Timestamp t)  { this.reviewedAt = t; }
    public void setAdminId(Integer aid)     { this.adminId = aid; }

    public boolean isPending() { return PENDING.equals(status); }
}