package com.miljuanat.skillpulse.repository;

import com.miljuanat.skillpulse.model.CompetencyMastery;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompetencyMasteryRepository extends JpaRepository<CompetencyMastery, Long> {

    Optional<CompetencyMastery> findByStudentIdAndSkillId(Long studentId, Long skillId);

    boolean existsByStudentId(Long studentId);

    boolean existsBySkillId(Long skillId);

    @Query("select cm from CompetencyMastery cm join fetch cm.skill where cm.student.id = :studentId order by cm.masteryScore asc")
    List<CompetencyMastery> findByStudentWithSkill(@Param("studentId") Long studentId);

    /** Class-wide ranking: average mastery per skill across all students, weakest first. */
    @Query("""
            select s.id as skillId,
                   s.name as skillName,
                   avg(cm.masteryScore) as avgMastery,
                   count(cm) as studentCount
            from CompetencyMastery cm
              join cm.skill s
            group by s.id, s.name
            order by avgMastery asc""")
    List<SkillAverage> classAverages();
}
