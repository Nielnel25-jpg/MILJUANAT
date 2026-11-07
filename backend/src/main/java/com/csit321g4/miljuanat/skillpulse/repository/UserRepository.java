package com.csit321g4.miljuanat.skillpulse.repository;

import com.csit321g4.miljuanat.skillpulse.model.Role;
import com.csit321g4.miljuanat.skillpulse.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByRole(Role role);
    List<User> findByRoleOrderByFullNameAsc(Role role);
}
