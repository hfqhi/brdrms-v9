package com.barangay.system.service;

import com.barangay.system.model.Registration;
import com.barangay.system.model.Resident;
import com.barangay.system.repo.RegistrationRepo;
import com.barangay.system.repo.ResidentRepo;
import com.barangay.system.util.StringUtil;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class RegistrationService extends BaseService {

    private final RegistrationRepo regRepo;
    private final ResidentRepo     residentRepo;

    public RegistrationService(RegistrationRepo regRepo, ResidentRepo residentRepo) {
        this.regRepo      = regRepo;
        this.residentRepo = residentRepo;
    }

    public String submit(String username, String password, String fullName,
                         String contact, String address) {
        if (StringUtil.isBlank(username)) return "ERROR: Username cannot be empty.";
        if (StringUtil.isBlank(password)) return "ERROR: Password cannot be empty.";
        if (password.length() < 6)        return "ERROR: Password must be at least 6 characters.";
        if (StringUtil.isBlank(fullName)) return "ERROR: Full name cannot be empty.";
        if (StringUtil.isBlank(contact))  return "ERROR: Contact number cannot be empty.";
        if (StringUtil.isBlank(address))  return "ERROR: Address cannot be empty.";
        try {
            if (residentRepo.usernameExists(username.trim()))
                return "ERROR: Username '" + username.trim() + "' is already taken.";
            if (regRepo.pendingUsernameExists(username.trim()))
                return "ERROR: A pending registration with that username already exists.";
            String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));
            Registration reg = new Registration(username.trim(), hash,
                    fullName.trim(), contact.trim(), address.trim());
            return regRepo.save(reg)
                ? "SUCCESS: Registration submitted. Please wait for admin approval."
                : "ERROR: Registration failed.";
        } catch (SQLException e) { return "ERROR: " + e.getMessage(); }
    }

    public String approve(int regId, int adminId) {
        try {
            Registration reg = regRepo.findById(regId);
            if (reg == null)      return "ERROR: Registration not found.";
            if (!reg.isPending()) return "ERROR: Registration is no longer pending.";
            if (residentRepo.usernameExists(reg.getUsername()))
                return "ERROR: Username already exists in residents.";
            Resident r = new Resident(reg.getUsername(), reg.getPassword(),
                    reg.getFullName(), reg.getContactNumber(), reg.getAddress());
            residentRepo.save(r);
            regRepo.updateStatus(regId, Registration.APPROVED,
                    new Timestamp(System.currentTimeMillis()), adminId);
            return "SUCCESS: Approved. Resident account created for '" + reg.getFullName() + "'.";
        } catch (SQLException e) { return "ERROR: " + e.getMessage(); }
    }

    public String reject(int regId, int adminId) {
        try {
            Registration reg = regRepo.findById(regId);
            if (reg == null)      return "ERROR: Registration not found.";
            if (!reg.isPending()) return "ERROR: Registration is no longer pending.";
            regRepo.updateStatus(regId, Registration.REJECTED,
                    new Timestamp(System.currentTimeMillis()), adminId);
            return "SUCCESS: Registration rejected.";
        } catch (SQLException e) { return "ERROR: " + e.getMessage(); }
    }

    public List<Registration> getPending() { return safeList(() -> regRepo.findPending()); }

    public Registration getById(int id) {
        try { return regRepo.findById(id); } catch (SQLException e) { return null; }
    }
}