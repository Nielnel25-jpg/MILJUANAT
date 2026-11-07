package com.csit321g4.miljuanat.skillpulse.repository;

import com.csit321g4.miljuanat.skillpulse.model.Assessment;
import com.csit321g4.miljuanat.skillpulse.model.AssessmentStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    List<Assessment> findByInstructorIdOrderByCreatedAtDesc(Long instructorId);
    List<Assessment> findByStatusOrderByCreatedAtDesc(AssessmentStatus status);
    boolean existsByInstructorId(Long instructorId);
}
