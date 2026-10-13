package com.finledger.controller;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;
import com.finledger.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }

    public static class RegisterRequest {
        @NotBlank @Size(max=100) public String fullName;
        @NotBlank @Email @Size(max=180) public String email;
        @NotBlank @Size(min=8, max=72) public String password;
    }
    public static class LoginRequest {
        @NotBlank @Email public String email;
        @NotBlank public String password;
    }
    @PostMapping("/register")
    public Map<String, String> register(@Valid @RequestBody RegisterRequest request) {
        return Collections.singletonMap("token", auth.register(request.fullName, request.email, request.password));
    }
    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody LoginRequest request) {
        return Collections.singletonMap("token", auth.login(request.email, request.password));
    }
}
