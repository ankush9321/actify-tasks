package com.finledger.entity;

import java.time.Instant;
import javax.persistence.*;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 180)
    private String actor;
    @Column(nullable = false, length = 100)
    private String action;
    @Column(length = 1000)
    private String details;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected AuditLog() {}
    public AuditLog(String actor, String action, String details) {
        this.actor = actor; this.action = action; this.details = details;
    }
    public Long getId() { return id; }
    public String getActor() { return actor; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
    public Instant getCreatedAt() { return createdAt; }
}
