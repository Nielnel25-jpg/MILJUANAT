package com.miljuanat.skillpulse.dto;

import com.miljuanat.skillpulse.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public final class AuthDtos {
    private AuthDtos() {}

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}

    public record AuthResponse(String token, String tokenType, Long userId, String fullName, String email, Role role) {}
}
