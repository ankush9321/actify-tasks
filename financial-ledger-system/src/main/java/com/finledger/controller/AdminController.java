package com.finledger.controller;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.finledger.entity.AuditLog;
import com.finledger.entity.FraudAlert;
import com.finledger.repository.AuditRepository;
import com.finledger.repository.FraudAlertRepository;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
public class AdminController {
    private final AuditRepository audits;
    private final FraudAlertRepository alerts;
    public AdminController(AuditRepository audits, FraudAlertRepository alerts) {
        this.audits=audits; this.alerts=alerts;
    }
    
    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    public List<AuditLog> audits() { return audits.findAll(); }
    
    @GetMapping("/fraud-alerts")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    public List<FraudAlert> alerts() { return alerts.findAll(); }
    
    @PostMapping("/fraud-alerts/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public String review(@PathVariable Long id) {
        FraudAlert alert = alerts.findById(id).orElseThrow(() -> new IllegalArgumentException("Alert not found"));
        alert.setReviewed(true); alerts.save(alert); return "Alert reviewed";
    }
}
