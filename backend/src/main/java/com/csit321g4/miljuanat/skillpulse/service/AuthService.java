package com.csit321g4.miljuanat.skillpulse.service;

import com.csit321g4.miljuanat.skillpulse.dto.AuthDtos.AuthResponse;
import com.csit321g4.miljuanat.skillpulse.dto.AuthDtos.LoginRequest;
import com.csit321g4.miljuanat.skillpulse.exception.ApiException;
import com.csit321g4.miljuanat.skillpulse.model.User;
import com.csit321g4.miljuanat.skillpulse.repository.UserRepository;
import com.csit321g4.miljuanat.skillpulse.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository users;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, UserRepository users, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.users = users;
        this.jwtService = jwtService;
    }

    /** Invalid credentials / deactivated accounts raise AuthenticationException -> 401 via GlobalExceptionHandler. */
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        return toResponse(findByEmail(request.email()));
    }

    public AuthResponse me(String email) {
        return toResponse(findByEmail(email));
    }

    private User findByEmail(String email) {
        return users.findByEmail(email).orElseThrow(() -> ApiException.notFound("User not found"));
    }

    private AuthResponse toResponse(User u) {
        return new AuthResponse(jwtService.generateToken(u.getEmail(), u.getRole()), "Bearer",
                u.getId(), u.getFullName(), u.getEmail(), u.getRole());
    }
}
