package com.barangay.system.model;

import java.sql.Timestamp;

public class Resident {

    private int       id;
    private String    username;
    private String    password;
    private String    fullName;
    private String    contactNumber;
    private String    address;
    private boolean   archived;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Resident() {}

    // Used when approving a registration — password is already BCrypt hash.
    public Resident(String username, String password, String fullName,
                    String contactNumber, String address) {
        this.username      = username;
        this.password      = password;
        this.fullName      = fullName;
        this.contactNumber = contactNumber;
        this.address       = address;
        this.archived      = false;
    }

    public Resident(int id, String username, String password, String fullName,
                    String contactNumber, String address,
                    boolean archived, Timestamp createdAt, Timestamp updatedAt) {
        this.id            = id;
        this.username      = username;
        this.password      = password;
        this.fullName      = fullName;
        this.contactNumber = contactNumber;
        this.address       = address;
        this.archived      = archived;
        this.createdAt     = createdAt;
        this.updatedAt     = updatedAt;
    }

    public int       getId()            { return id; }
    public String    getUsername()      { return username; }
    public String    getPassword()      { return password; }
    public String    getFullName()      { return fullName; }
    public String    getContactNumber() { return contactNumber; }
    public String    getAddress()       { return address; }
    public boolean   isArchived()       { return archived; }
    public Timestamp getCreatedAt()     { return createdAt; }
    public Timestamp getUpdatedAt()     { return updatedAt; }

    public void setId(int id)                 { this.id = id; }
    public void setPassword(String p)         { this.password = p; }
    public void setFullName(String fn)        { this.fullName = fn; }
    public void setContactNumber(String cn)   { this.contactNumber = cn; }
    public void setAddress(String a)          { this.address = a; }
    public void setArchived(boolean archived) { this.archived = archived; }
}