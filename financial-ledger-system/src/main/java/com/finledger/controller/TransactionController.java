package com.finledger.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.finledger.entity.FinancialTransaction;
import com.finledger.repository.TransactionRepository;
import com.finledger.service.TransactionService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService service;
    private final TransactionRepository transactions;
    public TransactionController(TransactionService service, TransactionRepository transactions) {
        this.service=service; this.transactions=transactions;
    }

    public static class MoneyRequest {
        @NotNull @DecimalMin(value="0.01") @Digits(integer=17, fraction=2) public BigDecimal amount;
        @NotNull public Long accountId;
    }
    public static class TransferRequest {
        @NotNull @DecimalMin(value="0.01") @Digits(integer=17, fraction=2) public BigDecimal amount;
        @NotNull public Long sourceAccountId;
        @NotNull public Long destinationAccountId;
    }

    @PostMapping("/deposit")
    public Map<String,Object> deposit(@Valid @RequestBody MoneyRequest r, Principal p,
            @RequestHeader("Idempotency-Key") String key) {
        return view(service.deposit(r.accountId, r.amount, p.getName(), key));
    }
    @PostMapping("/withdraw")
    public Map<String,Object> withdraw(@Valid @RequestBody MoneyRequest r, Principal p,
            @RequestHeader("Idempotency-Key") String key) {
        return view(service.withdraw(r.accountId, r.amount, p.getName(), key));
    }
    @PostMapping("/transfer")
    public Map<String,Object> transfer(@Valid @RequestBody TransferRequest r, Principal p,
            @RequestHeader("Idempotency-Key") String key) {
        return view(service.transfer(r.sourceAccountId, r.destinationAccountId, r.amount, p.getName(), key));
    }
    @PostMapping("/{id}/reverse")
    public Map<String,Object> reverse(@PathVariable Long id, Principal p) {
        boolean admin = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        return view(service.reverse(id, p.getName(), admin));
    }
    @GetMapping("/{id}")
    public Map<String,Object> get(@PathVariable Long id, Principal p) {
        FinancialTransaction tx = transactions.findById(id).orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
        boolean adminOrAuditor = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().stream().anyMatch(a ->
                    "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_AUDITOR".equals(a.getAuthority()));
        boolean source = tx.getSourceAccount() != null && tx.getSourceAccount().getOwner().getEmail().equals(p.getName());
        boolean dest = tx.getDestinationAccount() != null && tx.getDestinationAccount().getOwner().getEmail().equals(p.getName());
        if (!adminOrAuditor && !source && !dest) throw new org.springframework.security.access.AccessDeniedException("Not allowed");
        return view(tx);
    }
    private Map<String,Object> view(FinancialTransaction t) {
        Map<String,Object> m = new LinkedHashMap<String,Object>();
        m.put("id",t.getId()); m.put("type",t.getType()); m.put("status",t.getStatus());
        m.put("sourceAccountId",t.getSourceAccount()==null?null:t.getSourceAccount().getId());
        m.put("destinationAccountId",t.getDestinationAccount()==null?null:t.getDestinationAccount().getId());
        m.put("amount",t.getAmount()); m.put("fee",t.getFee()); m.put("gst",t.getGst());
        m.put("createdAt",t.getCreatedAt()); m.put("createdBy",t.getCreatedBy());
        return m;
    }
}
