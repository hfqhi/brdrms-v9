package com.barangay.system.model;

import java.sql.Timestamp;

/*
 * POJO representing a row in the users table.
 *
 * Roles:
 *   USER  - a barangay resident who can submit document requests
 *   ADMIN - a barangay staff member who manages all requests
 */
public class User {

    // Role constants
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    // Fields matching the users table columns
    private int id;
    private String username;
    private String password;
    private String fullName;
    private String address;
    private String role;
    private Timestamp createdAt;

    // Default no-arg constructor
    public User() {
    }

    // Constructor used when creating a new account
    public User(String username, String password, String fullName,
            String address, String role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.address = address;
        this.role = role;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String u) {
        this.username = u;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String p) {
        this.password = p;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fn) {
        this.fullName = fn;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String a) {
        this.address = a;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp t) {
        this.createdAt = t;
    }

    // Returns true if this user holds the admin role
    public boolean isAdmin() {
        return ROLE_ADMIN.equals(this.role);
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username
                + "', fullName='" + fullName + "', role='" + role + "'}";
    }
}
