package com.barangay.system.service;

import com.barangay.system.model.Admin;
import com.barangay.system.repo.AdminRepo;
import com.barangay.system.util.StringUtil;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class AdminService extends BaseService {

    private final AdminRepo repo;
    private String loginMsg = "";

    public AdminService(AdminRepo repo) { this.repo = repo; }

    public Admin login(String username, String password) {
        loginMsg = "ERROR: Invalid username or password.";
        if (StringUtil.isBlank(username) || StringUtil.isBlank(password)) return null;
        try {
            Admin a = repo.findByUsername(username.trim());
            if (a != null && BCrypt.checkpw(password, a.getPassword())) {
                loginMsg = "SUCCESS";
                return a;
            }
        } catch (SQLException e) {
            loginMsg = "ERROR: " + e.getMessage();
        }
        return null;
    }

    public String getLoginMsg() { return loginMsg; }

    // Changes admin password — verifies current password first.
    public String changePassword(int id, String currentPassword, String newPassword) {
        if (StringUtil.isBlank(newPassword))     return "ERROR: New password cannot be empty.";
        if (newPassword.length() < 6)             return "ERROR: Password must be at least 6 characters.";
        if (newPassword.equals(currentPassword))  return "ERROR: New password must differ from current.";
        try {
            Admin a = repo.findById(id);
            if (a == null) return "ERROR: Admin account not found.";
            if (!BCrypt.checkpw(currentPassword, a.getPassword()))
                return "ERROR: Current password is incorrect.";
            String hash = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
            return repo.updatePassword(id, hash)
                ? "SUCCESS: Password changed successfully."
                : "ERROR: Password update failed.";
        } catch (SQLException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public Admin getById(int id) {
        try { return repo.findById(id); }
        catch (SQLException e) { return null; }
    }
}