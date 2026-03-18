package com.barangay.system.service;

import com.barangay.system.model.DocRequest;
import com.barangay.system.repo.DocRequestRepo;
import com.barangay.system.util.StringUtil;

import java.sql.SQLException;
import java.util.List;

public class DocRequestService extends BaseService {

    private final DocRequestRepo repo;

    public DocRequestService(DocRequestRepo repo) { this.repo = repo; }

    public String submit(int residentId, int docTypeId, String purpose) {
        if (docTypeId <= 0)                return "ERROR: Please select a document type.";
        if (StringUtil.isBlank(purpose))   return "ERROR: Purpose cannot be empty.";
        try {
            DocRequest req = new DocRequest(residentId, docTypeId, purpose.trim());
            return repo.save(req)
                ? "SUCCESS: Request submitted -- " + fmt(req.getId())
                : "ERROR: Failed to submit.";
        } catch (SQLException e) { return "ERROR: " + e.getMessage(); }
    }

    public String editPurpose(int id, int residentId, String purpose) {
        if (StringUtil.isBlank(purpose)) return "ERROR: Purpose cannot be empty.";
        try {
            DocRequest req = repo.findById(id);
            if (req == null)                       return "ERROR: Request not found.";
            if (req.getResidentId() != residentId) return "ERROR: Not your request.";
            if (!req.isPending())                  return "ERROR: Only PENDING requests can be edited.";
            return repo.updatePurpose(id, purpose.trim())
                ? "SUCCESS: Request updated." : "ERROR: Update failed.";
        } catch (SQLException e) { return "ERROR: " + e.getMessage(); }
    }

    public String process(int id, String status, String remarks, int adminId) {
        if (!DocRequest.APPROVED.equals(status) && !DocRequest.REJECTED.equals(status))
            return "ERROR: Invalid status.";
        try {
            DocRequest req = repo.findById(id);
            if (req == null)      return "ERROR: " + fmt(id) + " not found.";
            if (!req.isPending()) return "ERROR: " + fmt(id) + " is already " + req.getStatus() + ".";
            return repo.updateStatus(id, status, remarks, adminId)
                ? "SUCCESS: " + fmt(id) + " has been " + status + "."
                : "ERROR: Failed to update status.";
        } catch (SQLException e) { return "ERROR: " + e.getMessage(); }
    }

    public String cancel(int id, int residentId) {
        try {
            DocRequest req = repo.findById(id);
            if (req == null)                       return "ERROR: Not found.";
            if (req.getResidentId() != residentId) return "ERROR: Not your request.";
            if (!req.isPending())                  return "ERROR: Only PENDING requests can be cancelled.";
            return repo.archiveByResidentId(id, residentId)
                ? "SUCCESS: " + fmt(id) + " cancelled." : "ERROR: Cancel failed.";
        } catch (SQLException e) { return "ERROR: " + e.getMessage(); }
    }

    public String archive(int id) {
        try {
            if (repo.findById(id) == null) return "ERROR: Not found.";
            return repo.archiveById(id) ? "SUCCESS: Moved to archive." : "ERROR: Failed.";
        } catch (SQLException e) { return "ERROR: " + e.getMessage(); }
    }

    public String restore(int id) {
        try {
            return repo.restoreById(id)
                ? "SUCCESS: " + fmt(id) + " restored." : "ERROR: Failed.";
        } catch (SQLException e) { return "ERROR: " + e.getMessage(); }
    }

    public String remove(int id) {
        try {
            return repo.deleteById(id)
                ? "SUCCESS: " + fmt(id) + " permanently removed." : "ERROR: Failed.";
        } catch (SQLException e) { return "ERROR: " + e.getMessage(); }
    }

    public List<DocRequest> getAll()                  { return safeList(() -> repo.findAll()); }
    public List<DocRequest> getAllArchived()           { return safeList(() -> repo.findAllArchived()); }
    public List<DocRequest> getByResident(int id)     { return safeList(() -> repo.findByResidentId(id)); }
    public List<DocRequest> getByStatus(String s)     { return safeList(() -> repo.findByStatus(s)); }
    public List<DocRequest> getByResidentAndStatus(int rid, String s) {
        return safeList(() -> repo.findByResidentAndStatus(rid, s));
    }
    public List<DocRequest> search(String kw)         { return safeList(() -> repo.searchAll(kw)); }
    public List<DocRequest> searchForResident(String kw, int rid) {
        return safeList(() -> repo.searchByResident(kw, rid));
    }

    public DocRequest getById(int id) {
        try { return repo.findById(id); } catch (SQLException e) { return null; }
    }

    public DocRequest getByIdAny(int id) {
        try { return repo.findByIdAny(id); } catch (SQLException e) { return null; }
    }

    public static String fmt(int id) { return String.format("REQ-%04d", id); }
}