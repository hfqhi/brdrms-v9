package com.barangay.system.service;

import com.barangay.system.model.History;
import com.barangay.system.repo.HistoryRepo;

import java.sql.SQLException;
import java.util.List;

public class HistoryService extends BaseService {

    private final HistoryRepo repo;

    public HistoryService(HistoryRepo repo) { this.repo = repo; }

    // Never throws — a log failure must not crash the application.
    public void log(int adminId, String adminName, String action,
                    int targetId, String targetType, String description) {
        try {
            repo.save(new History(adminId, adminName, action, targetId, targetType, description));
        } catch (SQLException e) {
            System.out.println("WARN: History log write failed - " + e.getMessage());
        }
    }

    public List<History> getAll()           { return safeList(() -> repo.findAll()); }
    public List<History> search(String kw)  { return safeList(() -> repo.search(kw)); }
    public List<History> getByType(String t){ return safeList(() -> repo.findByTargetType(t)); }
}   