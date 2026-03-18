package com.barangay.system.model;

import java.sql.Timestamp;

public class Admin {

    private int       id;
    private String    username;
    private String    password;
    private String    fullName;
    private String    position;
    private Timestamp createdAt;

    public Admin() {}

    public Admin(String username, String password, String fullName, String position) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.position = position;
    }

    public Admin(int id, String username, String password,
                 String fullName, String position, Timestamp createdAt) {
        this.id        = id;
        this.username  = username;
        this.password  = password;
        this.fullName  = fullName;
        this.position  = position;
        this.createdAt = createdAt;
    }

    public int       getId()        { return id; }
    public String    getUsername()  { return username; }
    public String    getPassword()  { return password; }
    public String    getFullName()  { return fullName; }
    public String    getPosition()  { return position; }
    public Timestamp getCreatedAt() { return createdAt; }

    public void setId(int id)           { this.id = id; }
    public void setPassword(String p)   { this.password = p; }
    public void setFullName(String fn)  { this.fullName = fn; }
    public void setPosition(String pos) { this.position = pos; }
}