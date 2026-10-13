package com.finledger.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.finledger.entity.AuditLog;
import com.finledger.repository.AuditRepository;

@Service
public class AuditService {
    private final AuditRepository repository;
    public AuditService(AuditRepository repository) { this.repository = repository; }
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(String actor, String action, String details) {
        repository.save(new AuditLog(actor, action, details));
    }
}
