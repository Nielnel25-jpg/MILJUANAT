package com.csit321g4.miljuanat.skillpulse.controller;

import com.csit321g4.miljuanat.skillpulse.dto.UserDtos.ChangeRoleRequest;
import com.csit321g4.miljuanat.skillpulse.dto.UserDtos.CreateUserRequest;
import com.csit321g4.miljuanat.skillpulse.dto.UserDtos.UpdateUserRequest;
import com.csit321g4.miljuanat.skillpulse.dto.UserDtos.UserResponse;
import com.csit321g4.miljuanat.skillpulse.model.Role;
import com.csit321g4.miljuanat.skillpulse.service.UserService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** ADMIN only (enforced in SecurityConfig). */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) { this.userService = userService; }

    /** POST /api/admin/users - create a Student, Instructor or Admin account. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest req) { return userService.create(req); }

    /** GET /api/admin/users?role=STUDENT - list users, optionally by role. */
    @GetMapping
    public List<UserResponse> list(@RequestParam(required = false) Role role) { return userService.list(role); }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) { return userService.get(id); }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest req) {
        return userService.update(id, req);
    }

    /** PATCH /api/admin/users/{id}/role - assign/change role (409 if the user owns role-specific data). */
    @PatchMapping("/{id}/role")
    public UserResponse changeRole(@PathVariable Long id, @Valid @RequestBody ChangeRoleRequest req, Principal p) {
        return userService.changeRole(id, req.role(), p.getName());
    }

    /** DELETE = deactivate (soft delete) so historical responses stay intact. */
    @DeleteMapping("/{id}")
    public UserResponse deactivate(@PathVariable Long id, Principal p) {
        return userService.setActive(id, false, p.getName());
    }

    @PatchMapping("/{id}/activate")
    public UserResponse activate(@PathVariable Long id, Principal p) {
        return userService.setActive(id, true, p.getName());
    }
}
