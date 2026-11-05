package com.miljuanat.skillpulse.repository;

import com.miljuanat.skillpulse.model.Instructor;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {
    Optional<Instructor> findByEmail(String email);
}
