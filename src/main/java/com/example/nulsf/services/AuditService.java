package com.example.nulsf.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.nulsf.model.AuditLog;
import com.example.nulsf.repository.AuditLogRepository;

import java.util.List;

@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void log(String action, String performedBy, String targetType, String targetId, String details) {
        try {
            AuditLog log = new AuditLog(action, performedBy, targetType, targetId, details);
            auditLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("Could not record audit log: " + e.getMessage());
        }
    }

    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findTop100ByOrderByTimestampDesc();
    }
}
