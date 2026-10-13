package com.finledger.service;

import java.util.Collections;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.finledger.entity.*;
import com.finledger.repository.UserRepository;
import com.finledger.security.JwtService;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuditService audit;

    public AuthService(UserRepository users, PasswordEncoder encoder,
            AuthenticationManager authenticationManager, JwtService jwtService, AuditService audit) {
        this.users = users; this.encoder = encoder; this.authenticationManager = authenticationManager;
        this.jwtService = jwtService; this.audit = audit;
    }

    @Transactional
    public String register(String fullName, String email, String password) {
        String normalized = email.trim().toLowerCase();
        if (users.existsByEmail(normalized)) throw new IllegalArgumentException("Email already registered");
        AppUser user = new AppUser();
        user.setFullName(fullName.trim());
        user.setEmail(normalized);
        user.setPassword(encoder.encode(password));
        user.setRoles(Collections.singleton(Role.CUSTOMER));
        users.save(user);
        audit.record(normalized, "REGISTER", "Customer registered");
        return jwtService.generate(normalized);
    }

    public String login(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                email.trim().toLowerCase(), password));
        return jwtService.generate(email.trim().toLowerCase());
    }
}
