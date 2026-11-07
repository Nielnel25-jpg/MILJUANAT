package com.csit321g4.miljuanat.skillpulse.controller;

import com.csit321g4.miljuanat.skillpulse.dto.AuthDtos.AuthResponse;
import com.csit321g4.miljuanat.skillpulse.dto.AuthDtos.LoginRequest;
import com.csit321g4.miljuanat.skillpulse.service.AuthService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    /** POST /api/auth/login  -> JWT + user info (401 on bad credentials). */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /** GET /api/auth/me -> current user (issues a fresh token). */
    @GetMapping("/me")
    public AuthResponse me(Principal principal) {
        return authService.me(principal.getName());
    }
}
