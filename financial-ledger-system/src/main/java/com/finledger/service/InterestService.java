package com.finledger.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.finledger.entity.*;
import com.finledger.repository.*;

@Service
public class InterestService {
    private static final BigDecimal MONTHLY_RATE = new BigDecimal("0.04").divide(new BigDecimal("12"), 12, RoundingMode.HALF_UP);
    private final AccountRepository accounts;
    private final InterestPostingRepository postings;
    private final TransactionRepository transactions;
    private final LedgerService ledger;
    private final AuditService audit;

    public InterestService(AccountRepository accounts, InterestPostingRepository postings,
            TransactionRepository transactions, LedgerService ledger, AuditService audit) {
        this.accounts=accounts; this.postings=postings; this.transactions=transactions; this.ledger=ledger; this.audit=audit;
    }

    @Scheduled(cron = "0 10 0 1 * *")
    public void scheduledRun() {
        postMonthlyInterest(YearMonth.now().minusMonths(1));
    }

    @Transactional
    public void postMonthlyInterest(YearMonth month) {
        LocalDate period = month.atEndOfMonth();
        List<Account> all = accounts.findAll();
        for (Account candidate : all) {
            Account a = accounts.findByIdForUpdate(candidate.getId()).orElse(null);
            if (a == null || !a.isActive() || postings.existsByAccountIdAndPeriod(a.getId(), period)) continue;
            BigDecimal interest = a.getBalance().multiply(MONTHLY_RATE).setScale(2, RoundingMode.HALF_UP);
            postings.save(new InterestPosting(a, period, interest));
            if (interest.signum() <= 0) continue;
            a.setBalance(a.getBalance().add(interest));
            FinancialTransaction tx = new FinancialTransaction();
            tx.setType(FinancialTransaction.Type.INTEREST);
            tx.setSourceAccount(null); tx.setDestinationAccount(a);
            tx.setAmount(interest); tx.setFee(new BigDecimal("0.00")); tx.setGst(new BigDecimal("0.00"));
            tx.setCreatedBy("SYSTEM");
            tx.setIdempotencyKey("INTEREST-" + a.getId() + "-" + period);
            tx = transactions.save(tx);
            ledger.add(tx, "INTEREST_EXPENSE", "CUSTOMER_DEPOSIT_LIABILITY", interest);
            audit.record("SYSTEM", "INTEREST_POSTED", "accountId=" + a.getId() + ", period=" + period + ", amount=" + interest);
        }
    }
}
