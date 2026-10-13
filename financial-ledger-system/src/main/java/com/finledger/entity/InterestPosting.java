package com.finledger.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import javax.persistence.*;

@Entity
@Table(name = "interest_postings", uniqueConstraints = @UniqueConstraint(columnNames={"account_id", "period"}))
public class InterestPosting {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="account_id", nullable=false)
    private Account account;
    @Column(nullable=false)
    private LocalDate period;
    @Column(nullable=false, precision=19, scale=2)
    private BigDecimal amount;
    protected InterestPosting() {}
    public InterestPosting(Account account, LocalDate period, BigDecimal amount) {
        this.account=account; this.period=period; this.amount=amount;
    }
}
