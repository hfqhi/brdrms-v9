package com.barangay.system.service;

import com.barangay.system.model.Resident;
import com.barangay.system.repo.ResidentRepo;
import com.barangay.system.util.StringUtil;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.List;

public class ResidentService extends BaseService {

    private final ResidentRepo repo;
    private String loginMsg = "";

    public ResidentService(ResidentRepo repo) { this.repo = repo; }

    public Resident login(String username, String password) {
        loginMsg = "ERROR: Invalid username or password.";
        if (StringUtil.isBlank(username) || StringUtil.isBlank(password)) return null;
        try {
            Resident r = repo.findByUsername(username.trim());
            if (r != null && BCrypt.checkpw(password, r.getPassword())) {
                loginMsg = "SUCCESS";
                return r;
            }
        } catch (SQLException e) {
            loginMsg = "ERROR: " + e.getMessage();
        }
        return null;
    }

    public String getLoginMsg() { return loginMsg; }

    public String updateProfile(int id, String fullName, String contact,
                                String address, String newPassword) {
        if (StringUtil.isBlank(fullName)) return "ERROR: Full name cannot be empty.";
        if (StringUtil.isBlank(contact))  return "ERROR: Contact number cannot be empty.";
        if (StringUtil.isBlank(address))  return "ERROR: Address cannot be empty.";
        if (!StringUtil.isBlank(newPassword) && newPassword.length() < 6)
            return "ERROR: Password must be at least 6 characters.";
        try {
            Resident r = repo.findById(id);
            if (r == null) return "ERROR: Resident not found.";
            r.setFullName(fullName.trim());
            r.setContactNumber(contact.trim());
            r.setAddress(address.trim());
            if (!StringUtil.isBlank(newPassword))
                r.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt(12)));
            return repo.update(r) ? "SUCCESS: Profile updated." : "ERROR: Update failed.";
        } catch (SQLException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public String archive(int id) {
        try {
            Resident r = repo.findById(id);
            if (r == null)      return "ERROR: Resident not found.";
            if (r.isArchived()) return "ERROR: Resident is already archived.";
            return repo.archiveById(id) ? "SUCCESS: Resident archived." : "ERROR: Archive failed.";
        } catch (SQLException e) { return "ERROR: " + e.getMessage(); }
    }

    public String restore(int id) {
        try {
            Resident r = repo.findById(id);
            if (r == null)       return "ERROR: Resident not found.";
            if (!r.isArchived()) return "ERROR: Resident is not archived.";
            return repo.restoreById(id) ? "SUCCESS: Resident restored." : "ERROR: Restore failed.";
        } catch (SQLException e) { return "ERROR: " + e.getMessage(); }
    }

    public String delete(int id) {
        try {
            if (repo.findById(id) == null) return "ERROR: Resident not found.";
            return repo.deleteById(id)
                ? "SUCCESS: Resident permanently deleted."
                : "ERROR: Delete failed.";
        } catch (SQLException e) { return "ERROR: " + e.getMessage(); }
    }

    public List<Resident> getAll()              { return safeList(() -> repo.findAll()); }
    public List<Resident> getAllArchived()       { return safeList(() -> repo.findAllArchived()); }
    public List<Resident> search(String kw)     { return safeList(() -> repo.search(kw)); }

    public Resident getById(int id) {
        try { return repo.findById(id); }
        catch (SQLException e) { return null; }
    }
}