package com.finledger.entity;

import java.time.Instant;
import javax.persistence.*;

@Entity
@Table(name = "fraud_alerts")
public class FraudAlert {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 30)
    private String ruleCode;
    @Column(nullable = false, length = 500)
    private String details;
    @Column(nullable = false, length = 180)
    private String actor;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
    @Column(nullable = false)
    private boolean reviewed = false;
    protected FraudAlert() {}
    public FraudAlert(String ruleCode, String details, String actor) {
        this.ruleCode = ruleCode; this.details = details; this.actor = actor;
    }
    public Long getId() { return id; }
    public String getRuleCode() { return ruleCode; }
    public String getDetails() { return details; }
    public String getActor() { return actor; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isReviewed() { return reviewed; }
    public void setReviewed(boolean reviewed) { this.reviewed = reviewed; }
}
