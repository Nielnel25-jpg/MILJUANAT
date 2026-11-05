package com.miljuanat.skillpulse.service;

import com.miljuanat.skillpulse.dto.UserDtos.CreateUserRequest;
import com.miljuanat.skillpulse.dto.UserDtos.UpdateUserRequest;
import com.miljuanat.skillpulse.dto.UserDtos.UserResponse;
import com.miljuanat.skillpulse.exception.ApiException;
import com.miljuanat.skillpulse.model.Admin;
import com.miljuanat.skillpulse.model.Instructor;
import com.miljuanat.skillpulse.model.Role;
import com.miljuanat.skillpulse.model.Student;
import com.miljuanat.skillpulse.model.User;
import com.miljuanat.skillpulse.repository.AssessmentRepository;
import com.miljuanat.skillpulse.repository.CompetencyMasteryRepository;
import com.miljuanat.skillpulse.repository.StudentResponseRepository;
import com.miljuanat.skillpulse.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final EntityManager em;
    private final StudentResponseRepository responses;
    private final CompetencyMasteryRepository masteries;
    private final AssessmentRepository assessments;

    public UserService(UserRepository users, PasswordEncoder encoder, EntityManager em,
                       StudentResponseRepository responses, CompetencyMasteryRepository masteries,
                       AssessmentRepository assessments) {
        this.users = users;
        this.encoder = encoder;
        this.em = em;
        this.responses = responses;
        this.masteries = masteries;
        this.assessments = assessments;
    }

    public UserResponse create(CreateUserRequest req) {
        if (users.existsByEmail(req.email())) throw ApiException.conflict("Email already in use");
        User user = switch (req.role()) {
            case STUDENT -> {
                Student s = new Student();
                s.setStudentNumber(req.studentNumber());
                yield s;
            }
            case INSTRUCTOR -> {
                Instructor i = new Instructor();
                i.setDepartment(req.department());
                yield i;
            }
            case ADMIN -> new Admin();
        };
        user.setFullName(req.fullName());
        user.setEmail(req.email());
        user.setPasswordHash(encoder.encode(req.password()));
        return toResponse(users.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list(Role role) {
        List<User> found = role == null ? users.findAll() : users.findByRoleOrderByFullNameAsc(role);
        return found.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        return toResponse(find(id));
    }

    public UserResponse update(Long id, UpdateUserRequest req) {
        User user = find(id);
        if (req.fullName() != null && !req.fullName().isBlank()) user.setFullName(req.fullName());
        if (req.email() != null && !req.email().equalsIgnoreCase(user.getEmail())) {
            if (users.existsByEmail(req.email())) throw ApiException.conflict("Email already in use");
            user.setEmail(req.email());
        }
        if (req.password() != null) user.setPasswordHash(encoder.encode(req.password()));
        if (user instanceof Student s && req.studentNumber() != null) s.setStudentNumber(req.studentNumber());
        if (user instanceof Instructor i && req.department() != null) i.setDepartment(req.department());
        return toResponse(user);
    }

    public UserResponse setActive(Long id, boolean active, String actingEmail) {
        User user = find(id);
        if (!active && user.getEmail().equalsIgnoreCase(actingEmail)) {
            throw ApiException.badRequest("You cannot deactivate your own account");
        }
        user.setActive(active);
        return toResponse(user);
    }

    /**
     * Changes a user's role. JPA cannot change an entity's subclass, so the subtype row is swapped with native SQL.
     * Refused (409) when the user already owns role-specific data (responses/mastery or assessments).
     */
    public UserResponse changeRole(Long id, Role newRole, String actingEmail) {
        User user = find(id);
        Role oldRole = user.getRole();
        if (oldRole == newRole) return toResponse(user);
        if (user.getEmail().equalsIgnoreCase(actingEmail)) {
            throw ApiException.badRequest("You cannot change your own role");
        }
        if (oldRole == Role.STUDENT && (responses.existsByStudentId(id) || masteries.existsByStudentId(id))) {
            throw ApiException.conflict("Student already has submissions or mastery records; role cannot be changed");
        }
        if (oldRole == Role.INSTRUCTOR && assessments.existsByInstructorId(id)) {
            throw ApiException.conflict("Instructor owns assessments; role cannot be changed");
        }
        em.flush();
        em.clear();
        em.createNativeQuery("delete from " + table(oldRole) + " where id = :id").setParameter("id", id).executeUpdate();
        em.createNativeQuery("update users set role = :role where id = :id")
                .setParameter("role", newRole.name()).setParameter("id", id).executeUpdate();
        em.createNativeQuery("insert into " + table(newRole) + " (id) values (:id)").setParameter("id", id).executeUpdate();
        em.clear();
        return toResponse(find(id));
    }

    private static String table(Role role) {
        return switch (role) {
            case STUDENT -> "students";
            case INSTRUCTOR -> "instructors";
            case ADMIN -> "admins";
        };
    }

    private User find(Long id) {
        return users.findById(id).orElseThrow(() -> ApiException.notFound("User " + id + " not found"));
    }

    private UserResponse toResponse(User u) {
        String studentNumber = u instanceof Student s ? s.getStudentNumber() : null;
        String department = u instanceof Instructor i ? i.getDepartment() : null;
        return new UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getRole(), u.isActive(),
                u.getCreatedAt(), studentNumber, department);
    }
}
