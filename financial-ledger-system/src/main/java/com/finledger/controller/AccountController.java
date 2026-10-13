package com.finledger.controller;

import java.security.Principal;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.finledger.entity.Account;
import com.finledger.service.AccountService;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService service;
    public AccountController(AccountService service) { this.service = service; }

    @PostMapping
    public Map<String, Object> create(Principal principal) { return view(service.create(principal.getName())); }

    @GetMapping("/mine")
    public List<Map<String, Object>> mine(Principal principal) {
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        for (Account a : service.mine(principal.getName())) out.add(view(a));
        return out;
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id, Principal principal) {
        boolean elevated = hasRole("ROLE_ADMIN") || hasRole("ROLE_AUDITOR");
        return view(service.getAuthorized(id, principal.getName(), elevated));
    }

    private boolean hasRole(String role) {
        return org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().stream().anyMatch(a -> role.equals(a.getAuthority()));
    }
    private Map<String, Object> view(Account a) {
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        m.put("id", a.getId()); 
        m.put("accountNumber", a.getAccountNumber());
        m.put("ownerEmail", a.getOwner().getEmail()); 
        m.put("balance", a.getBalance());
        m.put("currency", a.getCurrency()); 
        m.put("active", a.isActive());
        return m;
    }
}
