package com.barangay.system.model;

import java.sql.Timestamp;

public class DocType {

    private int       id;
    private String    typeName;
    private String    description;
    private double    fee;
    private boolean   archived;
    private Timestamp updatedAt;

    public DocType() {}

    public DocType(String typeName, String description, double fee) {
        this.typeName    = typeName;
        this.description = description;
        this.fee         = fee;
        this.archived    = false;
    }

    public DocType(int id, String typeName, String description,
                   double fee, boolean archived, Timestamp updatedAt) {
        this.id          = id;
        this.typeName    = typeName;
        this.description = description;
        this.fee         = fee;
        this.archived    = archived;
        this.updatedAt   = updatedAt;
    }

    public int       getId()          { return id; }
    public String    getTypeName()    { return typeName; }
    public String    getDescription() { return description; }
    public double    getFee()         { return fee; }
    public boolean   isArchived()     { return archived; }
    public Timestamp getUpdatedAt()   { return updatedAt; }

    public void setId(int id)                 { this.id = id; }
    public void setTypeName(String n)         { this.typeName = n; }
    public void setDescription(String d)      { this.description = d; }
    public void setFee(double fee)            { this.fee = fee; }
    public void setArchived(boolean archived) { this.archived = archived; }
}