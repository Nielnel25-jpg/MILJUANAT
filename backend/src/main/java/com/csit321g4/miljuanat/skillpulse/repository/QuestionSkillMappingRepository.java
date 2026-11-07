package com.csit321g4.miljuanat.skillpulse.repository;

import com.csit321g4.miljuanat.skillpulse.model.QuestionSkillMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionSkillMappingRepository extends JpaRepository<QuestionSkillMapping, Long> {
    boolean existsBySkillId(Long skillId);
}
