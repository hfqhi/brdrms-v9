package com.barangay.system.model;

import java.sql.Timestamp;

public class History {

    private int       id;
    private Integer   adminId;      // nullable
    private String    adminName;    // denormalized — survives admin deletion
    private String    action;
    private int       targetId;
    private String    targetType;
    private String    description;
    private Timestamp actedAt;

    public History() {}

    public History(Integer adminId, String adminName, String action,
                   int targetId, String targetType, String description) {
        this.adminId     = adminId;
        this.adminName   = adminName;
        this.action      = action;
        this.targetId    = targetId;
        this.targetType  = targetType;
        this.description = description;
    }

    public History(int id, Integer adminId, String adminName, String action,
                   int targetId, String targetType, String description,
                   Timestamp actedAt) {
        this.id          = id;
        this.adminId     = adminId;
        this.adminName   = adminName;
        this.action      = action;
        this.targetId    = targetId;
        this.targetType  = targetType;
        this.description = description;
        this.actedAt     = actedAt;
    }

    public int       getId()          { return id; }
    public Integer   getAdminId()     { return adminId; }
    public String    getAdminName()   { return adminName; }
    public String    getAction()      { return action; }
    public int       getTargetId()    { return targetId; }
    public String    getTargetType()  { return targetType; }
    public String    getDescription() { return description; }
    public Timestamp getActedAt()     { return actedAt; }

    public void setId(int id) { this.id = id; }
}