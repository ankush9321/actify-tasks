package com.finledger.service;

import java.math.BigDecimal;
import java.util.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.finledger.entity.*;
import com.finledger.repository.*;

@Service
public class AccountService {
    private final AccountRepository accounts;
    private final UserRepository users;
    private final AuditService audit;

    public AccountService(AccountRepository accounts, UserRepository users, AuditService audit) {
        this.accounts = accounts; this.users = users; this.audit = audit;
    }

    @Transactional
    public Account create(String email) {
        AppUser owner = users.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Account a = new Account();
        a.setOwner(owner);
        a.setAccountNumber("FL" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
        a.setBalance(new BigDecimal("0.00"));
        Account saved = accounts.save(a);
        audit.record(email, "ACCOUNT_CREATED", "Account id=" + saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Account> mine(String email) { return accounts.findByOwnerEmail(email); }

    @Transactional(readOnly = true)
    public Account getAuthorized(Long id, String email, boolean adminOrAuditor) {
        Account a = accounts.findById(id).orElseThrow(() -> new IllegalArgumentException("Account not found"));
        if (!adminOrAuditor && !a.getOwner().getEmail().equals(email))
            throw new org.springframework.security.access.AccessDeniedException("Not your account");
        return a;
    }
}
