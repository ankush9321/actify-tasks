package com.finledger.service;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.finledger.entity.*;
import com.finledger.repository.*;

@Service
public class TransactionService {
    private final AccountRepository accounts;
    private final TransactionRepository transactions;
    private final UserRepository users;
    private final LedgerService ledger;
    private final AuditService audit;
    private final FraudService fraud;
    private final TransferCalculationService calculations;

    public TransactionService(AccountRepository accounts, TransactionRepository transactions,
            UserRepository users, LedgerService ledger, AuditService audit,
            FraudService fraud, TransferCalculationService calculations) {
        this.accounts=accounts; this.transactions=transactions; this.users=users;
        this.ledger=ledger; this.audit=audit; this.fraud=fraud; this.calculations=calculations;
    }

    @Transactional
    public FinancialTransaction deposit(Long accountId, BigDecimal amount, String email, String key) {
        if (existing(key) != null) return existing(key);
        Account a = lock(accountId);
        authorizeOwner(a, email);
        requireActive(a);
        validateAmount(amount);
        a.setBalance(a.getBalance().add(amount));
        FinancialTransaction tx = create(FinancialTransaction.Type.DEPOSIT, null, a, amount, BigDecimal.ZERO, BigDecimal.ZERO, email, key);
        ledger.add(tx, "CASH_CLEARING", "CUSTOMER_DEPOSIT_LIABILITY", amount);
        audit.record(email, "DEPOSIT", "transactionId=" + tx.getId() + ", accountId=" + accountId);
        return tx;
    }

    @Transactional
    public FinancialTransaction withdraw(Long accountId, BigDecimal amount, String email, String key) {
        FinancialTransaction previous = existing(key);
        if (previous != null) return previous;
        Account a = lock(accountId);
        authorizeOwner(a, email);
        requireActive(a);
        validateAmount(amount);
        requireFunds(a, amount);
        a.setBalance(a.getBalance().subtract(amount));
        FinancialTransaction tx = create(FinancialTransaction.Type.WITHDRAWAL, a, null, amount, BigDecimal.ZERO, BigDecimal.ZERO, email, key);
        ledger.add(tx, "CUSTOMER_DEPOSIT_LIABILITY", "CASH_CLEARING", amount);
        audit.record(email, "WITHDRAWAL", "transactionId=" + tx.getId() + ", accountId=" + accountId);
        fraud.checkAmount(amount, email);
        return tx;
    }

    @Transactional
    public FinancialTransaction transfer(Long sourceId, Long destinationId, BigDecimal amount, String email, String key) {
        FinancialTransaction previous = existing(key);
        if (previous != null) return previous;
        if (sourceId.equals(destinationId)) throw new IllegalArgumentException("Source and destination must differ");
        Long first = Math.min(sourceId, destinationId), second = Math.max(sourceId, destinationId);
        Account one = lock(first);
        Account two = lock(second);
        Account source = sourceId.equals(first) ? one : two;
        Account destination = destinationId.equals(first) ? one : two;
        authorizeOwner(source, email);
        requireActive(source); requireActive(destination);
        validateAmount(amount);
        TransferCalculationService.Amounts calc = calculations.calculate(amount);
        requireFunds(source, calc.totalDebit);
        source.setBalance(source.getBalance().subtract(calc.totalDebit));
        destination.setBalance(destination.getBalance().add(calc.amount));
        FinancialTransaction tx = create(FinancialTransaction.Type.TRANSFER, source, destination,
                calc.amount, calc.fee, calc.gst, email, key);
        ledger.add(tx, "CUSTOMER_DEPOSIT_LIABILITY", "CUSTOMER_DEPOSIT_LIABILITY", calc.amount);
        if (calc.fee.signum() > 0) ledger.add(tx, "CUSTOMER_DEPOSIT_LIABILITY", "TRANSFER_FEE_INCOME", calc.fee);
        if (calc.gst.signum() > 0) ledger.add(tx, "CUSTOMER_DEPOSIT_LIABILITY", "GST_PAYABLE", calc.gst);
        audit.record(email, "TRANSFER", "transactionId=" + tx.getId() + ", amount=" + calc.amount + ", fee=" + calc.fee + ", gst=" + calc.gst);
        fraud.checkAmount(amount, email);
        return tx;
    }

    @Transactional
    public FinancialTransaction reverse(Long transactionId, String email, boolean admin) {
        FinancialTransaction original = transactions.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
        if (original.getType() == FinancialTransaction.Type.REVERSAL || original.getType() == FinancialTransaction.Type.INTEREST)
            throw new IllegalArgumentException("This transaction type cannot be reversed using this endpoint");
        if (!admin && (original.getSourceAccount() == null ||
                !original.getSourceAccount().getOwner().getEmail().equals(email)))
            throw new org.springframework.security.access.AccessDeniedException("Only the source owner or admin can reverse");
        if (transactions.findAll().stream().anyMatch(t -> t.getReversesTransaction() != null &&
                t.getReversesTransaction().getId().equals(transactionId)))
            throw new IllegalArgumentException("Transaction already reversed");

        Account source = original.getSourceAccount() == null ? null : lock(original.getSourceAccount().getId());
        Account destination = original.getDestinationAccount() == null ? null : lock(original.getDestinationAccount().getId());
        if (original.getType() == FinancialTransaction.Type.DEPOSIT) {
            requireFunds(destination, original.getAmount());
            destination.setBalance(destination.getBalance().subtract(original.getAmount()));
        } else if (original.getType() == FinancialTransaction.Type.WITHDRAWAL) {
            source.setBalance(source.getBalance().add(original.getAmount()));
        } else if (original.getType() == FinancialTransaction.Type.TRANSFER) {
            BigDecimal total = original.getAmount().add(original.getFee()).add(original.getGst());
            requireFunds(destination, original.getAmount());
            destination.setBalance(destination.getBalance().subtract(original.getAmount()));
            source.setBalance(source.getBalance().add(original.getAmount()));
            if (source.getBalance().compareTo(total) < 0)
                throw new IllegalArgumentException("Source account cannot recover fee and GST for full reversal");
            source.setBalance(source.getBalance().subtract(original.getFee().add(original.getGst())));
        }
        FinancialTransaction reversal = create(FinancialTransaction.Type.REVERSAL,
                destination, source, original.getAmount(), original.getFee(), original.getGst(), email,
                "REV-" + transactionId);
        reversal.setReversesTransaction(original);
        if (source != null && destination != null) {
            ledger.add(reversal, "CUSTOMER_DEPOSIT_LIABILITY", "CUSTOMER_DEPOSIT_LIABILITY", original.getAmount());
        } else if (source == null) {
            ledger.add(reversal, "CUSTOMER_DEPOSIT_LIABILITY", "CASH_CLEARING", original.getAmount());
        } else {
            ledger.add(reversal, "CASH_CLEARING", "CUSTOMER_DEPOSIT_LIABILITY", original.getAmount());
        }
        audit.record(email, "REVERSAL", "originalTransactionId=" + transactionId);
        return reversal;
    }

    private FinancialTransaction existing(String key) {
        if (key == null || key.trim().isEmpty()) throw new IllegalArgumentException("Idempotency-Key header is required");
        return transactions.findByIdempotencyKey(key).orElse(null);
    }
    private Account lock(Long id) {
        return accounts.findByIdForUpdate(id).orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
    }
    private void authorizeOwner(Account a, String email) {
        if (!a.getOwner().getEmail().equals(email)) throw new org.springframework.security.access.AccessDeniedException("Not your account");
    }
    private void requireActive(Account a) {
        if (!a.isActive()) throw new IllegalArgumentException("Account is inactive");
    }
    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || amount.scale() > 2)
            throw new IllegalArgumentException("Amount must be positive with at most two decimal places");
    }
    private void requireFunds(Account a, BigDecimal amount) {
        if (a == null || a.getBalance().compareTo(amount) < 0) throw new IllegalArgumentException("Insufficient funds");
    }
    private FinancialTransaction create(FinancialTransaction.Type type, Account source, Account destination,
            BigDecimal amount, BigDecimal fee, BigDecimal gst, String email, String key) {
        FinancialTransaction tx = new FinancialTransaction();
        tx.setType(type); tx.setSourceAccount(source); tx.setDestinationAccount(destination);
        tx.setAmount(amount.setScale(2)); tx.setFee(fee.setScale(2)); tx.setGst(gst.setScale(2));
        tx.setCreatedBy(email); tx.setIdempotencyKey(key);
        return transactions.save(tx);
    }
}
