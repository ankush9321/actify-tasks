package com.finledger.service;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.finledger.entity.*;
import com.finledger.entity.LedgerEntry.Side;
import com.finledger.repository.LedgerRepository;

@Service
public class LedgerService {
    private final LedgerRepository ledger;
    public LedgerService(LedgerRepository ledger) { this.ledger = ledger; }

    @Transactional(propagation = Propagation.MANDATORY)
    public void add(FinancialTransaction tx, String debitAccount, String creditAccount, BigDecimal amount) {
        ledger.save(new LedgerEntry(tx, debitAccount, Side.DEBIT, amount));
        ledger.save(new LedgerEntry(tx, creditAccount, Side.CREDIT, amount));
    }

    @Transactional(readOnly = true)
    public List<LedgerEntry> forTransaction(Long transactionId) {
        List<LedgerEntry> entries = ledger.findByTransactionIdOrderByIdAsc(transactionId);
        BigDecimal debits = BigDecimal.ZERO, credits = BigDecimal.ZERO;
        for (LedgerEntry e : entries) {
            if (e.getSide() == Side.DEBIT) debits = debits.add(e.getAmount());
            else credits = credits.add(e.getAmount());
        }
        if (debits.compareTo(credits) != 0) throw new IllegalStateException("Ledger is not balanced");
        return entries;
    }
}
