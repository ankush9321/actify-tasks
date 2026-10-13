package com.finledger.entity;

import java.math.BigDecimal;
import java.time.Instant;
import javax.persistence.*;

@Entity
@Table(name = "financial_transactions", uniqueConstraints = @UniqueConstraint(name="uk_tx_idempotency", columnNames={"idempotencyKey"}))
public class FinancialTransaction {
    public enum Type { DEPOSIT, WITHDRAWAL, TRANSFER, REVERSAL, INTEREST }
    public enum Status { POSTED, REJECTED }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Type type;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Status status = Status.POSTED;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "source_account_id")
    private Account sourceAccount;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "destination_account_id")
    private Account destinationAccount;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal fee = BigDecimal.ZERO;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal gst = BigDecimal.ZERO;
    @Column(length = 500)
    private String description;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    @Column(unique = true, length = 100)
    private String idempotencyKey;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reverses_transaction_id", unique = true)
    private FinancialTransaction reversesTransaction;
    @Column(length = 180)
    private String createdBy;

    public Long getId() { return id; }
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Account getSourceAccount() { return sourceAccount; }
    public void setSourceAccount(Account sourceAccount) { this.sourceAccount = sourceAccount; }
    public Account getDestinationAccount() { return destinationAccount; }
    public void setDestinationAccount(Account destinationAccount) { this.destinationAccount = destinationAccount; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getFee() { return fee; }
    public void setFee(BigDecimal fee) { this.fee = fee; }
    public BigDecimal getGst() { return gst; }
    public void setGst(BigDecimal gst) { this.gst = gst; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getCreatedAt() { return createdAt; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public FinancialTransaction getReversesTransaction() { return reversesTransaction; }
    public void setReversesTransaction(FinancialTransaction reversesTransaction) { this.reversesTransaction = reversesTransaction; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
