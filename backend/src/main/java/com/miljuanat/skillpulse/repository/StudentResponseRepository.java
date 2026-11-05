package com.miljuanat.skillpulse.repository;

import com.miljuanat.skillpulse.model.StudentResponse;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentResponseRepository extends JpaRepository<StudentResponse, Long> {

    boolean existsByStudentId(Long studentId);

    @Query("select count(r) > 0 from StudentResponse r where r.student.id = :studentId and r.question.assessment.id = :assessmentId")
    boolean existsSubmission(@Param("studentId") Long studentId, @Param("assessmentId") Long assessmentId);

    @Query("select count(r) > 0 from StudentResponse r where r.question.assessment.id = :assessmentId")
    boolean existsForAssessment(@Param("assessmentId") Long assessmentId);

    @Query("""
            select r from StudentResponse r join fetch r.question q
            where r.student.id = :studentId and q.assessment.id = :assessmentId
            order by q.id""")
    List<StudentResponse> findSubmission(@Param("studentId") Long studentId, @Param("assessmentId") Long assessmentId);

    /**
     * Mastery aggregation: for every skill a student has been tested on, sums
     * (score x mapping weight) vs. (question points x mapping weight) over all of the student's responses.
     * mastery% = earned / possible * 100.
     */
    @Query("""
            select m.skill.id as skillId,
                   sum(r.score * m.weight) as earned,
                   sum(q.points * m.weight) as possible
            from StudentResponse r
              join r.question q
              join q.skillMappings m
            where r.student.id = :studentId
            group by m.skill.id""")
    List<SkillAggregate> aggregateByStudent(@Param("studentId") Long studentId);

    /** Same weighted accuracy, but across every student's answers to one assessment, lowest skill first. */
    @Query("""
            select s.id as skillId,
                   s.name as skillName,
                   sum(r.score * m.weight) * 100.0 / sum(q.points * m.weight) as avgMastery,
                   count(distinct r.student.id) as studentCount
            from StudentResponse r
              join r.question q
              join q.skillMappings m
              join m.skill s
            where q.assessment.id = :assessmentId
            group by s.id, s.name
            order by avgMastery asc""")
    List<SkillAverage> aggregateByAssessment(@Param("assessmentId") Long assessmentId);
}
