package com.miljuanat.skillpulse.repository;

import com.miljuanat.skillpulse.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByParentId(Long parentId);
}
