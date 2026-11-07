package com.csit321g4.miljuanat.skillpulse.repository;

import com.csit321g4.miljuanat.skillpulse.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByParentId(Long parentId);
}
