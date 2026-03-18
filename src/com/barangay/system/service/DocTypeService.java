package com.barangay.system.service;

import com.barangay.system.model.DocType;
import com.barangay.system.repo.DocTypeRepo;
import com.barangay.system.util.StringUtil;

import java.sql.SQLException;
import java.util.List;

public class DocTypeService extends BaseService {

    private final DocTypeRepo repo;

    public DocTypeService(DocTypeRepo repo) { this.repo = repo; }

    public String add(String typeName, String description, double fee) {
        if (StringUtil.isBlank(typeName)) return "ERROR: Type name cannot be empty.";
        if (fee < 0)                      return "ERROR: Fee cannot be negative.";
        try {
            return repo.save(new DocType(typeName.trim(), description.trim(), fee))
                ? "SUCCESS: Document type '" + typeName.trim() + "' added."
                : "ERROR: Failed to add.";
        } catch (SQLException e) { return "ERROR: " + e.getMessage(); }
    }

    public String edit(int id, String typeName, String description, double fee) {
        if (StringUtil.isBlank(typeName)) return "ERROR: Type name cannot be empty.";
        if (fee < 0)                      return "ERROR: Fee cannot be negative.";
        try {
            DocType dt = repo.findById(id);
            if (dt == null) return "ERROR: Not found.";
            dt.setTypeName(typeName.trim());
            dt.setDescription(description.trim());
            dt.setFee(fee);
            return repo.update(dt) ? "SUCCESS: Document type updated." : "ERROR: Update failed.";
        } catch (SQLException e) { return "ERROR: " + e.getMessage(); }
    }

    public String archive(int id) {
        try {
            DocType dt = repo.findById(id);
            if (dt == null)     return "ERROR: Not found.";
            if (dt.isArchived()) return "ERROR: Already archived.";
            return repo.archiveById(id) ? "SUCCESS: Archived." : "ERROR: Failed.";
        } catch (SQLException e) { return "ERROR: " + e.getMessage(); }
    }

    public String restore(int id) {
        try {
            DocType dt = repo.findById(id);
            if (dt == null)      return "ERROR: Not found.";
            if (!dt.isArchived()) return "ERROR: Not archived.";
            return repo.restoreById(id) ? "SUCCESS: Restored." : "ERROR: Failed.";
        } catch (SQLException e) { return "ERROR: " + e.getMessage(); }
    }

    public String remove(int id) {
        try {
            if (repo.findById(id) == null) return "ERROR: Not found.";
            return repo.deleteById(id) ? "SUCCESS: Document type removed." : "ERROR: Failed.";
        } catch (SQLException e) { return "ERROR: " + e.getMessage(); }
    }

    public List<DocType> getAll()       { return safeList(() -> repo.findAll()); }
    public List<DocType> getAllActive()  { return safeList(() -> repo.findAllActive()); }

    public DocType getById(int id) {
        try { return repo.findById(id); }
        catch (SQLException e) { return null; }
    }
}