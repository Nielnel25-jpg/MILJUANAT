package com.miljuanat.skillpulse.repository;

import com.miljuanat.skillpulse.model.QuestionSkillMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionSkillMappingRepository extends JpaRepository<QuestionSkillMapping, Long> {
    boolean existsBySkillId(Long skillId);
}
