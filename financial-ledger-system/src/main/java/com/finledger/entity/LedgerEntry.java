package com.finledger.entity;

import java.math.BigDecimal;
import java.time.Instant;
import javax.persistence.*;

@Entity
@Table(name = "ledger_entries", indexes = @Index(name="idx_ledger_tx", columnList="transaction_id"))
public class LedgerEntry {
    public enum Side { DEBIT, CREDIT }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private FinancialTransaction transaction;
    @Column(nullable = false, length = 80)
    private String ledgerAccount;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10)
    private Side side;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public LedgerEntry() {}
    public LedgerEntry(FinancialTransaction transaction, String ledgerAccount, Side side, BigDecimal amount) {
        this.transaction = transaction; this.ledgerAccount = ledgerAccount; this.side = side; this.amount = amount;
    }
    public Long getId() { return id; }
    public FinancialTransaction getTransaction() { return transaction; }
    public String getLedgerAccount() { return ledgerAccount; }
    public Side getSide() { return side; }
    public BigDecimal getAmount() { return amount; }
    public Instant getCreatedAt() { return createdAt; }
}
