package com.csit321g4.miljuanat.skillpulse.dto;

import com.csit321g4.miljuanat.skillpulse.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class UserDtos {
    private UserDtos() {}

    public record CreateUserRequest(
            @NotBlank String fullName,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, message = "must be at least 8 characters") String password,
            @NotNull Role role,
            String studentNumber,
            String department) {}

    /** All fields optional; only non-null ones are applied. */
    public record UpdateUserRequest(
            String fullName,
            @Email String email,
            @Size(min = 8, message = "must be at least 8 characters") String password,
            String studentNumber,
            String department) {}

    public record ChangeRoleRequest(@NotNull Role role) {}

    public record UserResponse(
            Long id, String fullName, String email, Role role, boolean active, Instant createdAt,
            String studentNumber, String department) {}
}
