package com.csit321g4.miljuanat.skillpulse.service;

import com.csit321g4.miljuanat.skillpulse.config.MasteryProperties;
import com.csit321g4.miljuanat.skillpulse.dto.AnalyticsDtos.ClassSkillMasteryDto;
import com.csit321g4.miljuanat.skillpulse.dto.AnalyticsDtos.SkillMasteryDto;
import com.csit321g4.miljuanat.skillpulse.model.CompetencyMastery;
import com.csit321g4.miljuanat.skillpulse.model.MasteryLevel;
import com.csit321g4.miljuanat.skillpulse.model.Skill;
import com.csit321g4.miljuanat.skillpulse.model.Student;
import com.csit321g4.miljuanat.skillpulse.repository.CompetencyMasteryRepository;
import com.csit321g4.miljuanat.skillpulse.repository.SkillAggregate;
import com.csit321g4.miljuanat.skillpulse.repository.SkillAverage;
import com.csit321g4.miljuanat.skillpulse.repository.SkillRepository;
import com.csit321g4.miljuanat.skillpulse.repository.StudentResponseRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Analytics engine. Mastery of a skill for a student:
 *   sum(score * weight) / sum(points * weight) * 100
 * over all of the student's responses to questions mapped to that skill. The sums are computed by the
 * database (StudentResponseRepository.aggregateByStudent) and stored in competency_mastery.
 */
@Service
@Transactional
public class MasteryService {

    private final StudentResponseRepository responses;
    private final CompetencyMasteryRepository masteries;
    private final SkillRepository skills;
    private final MasteryProperties thresholds;

    public MasteryService(StudentResponseRepository responses, CompetencyMasteryRepository masteries,
                          SkillRepository skills, MasteryProperties thresholds) {
        this.responses = responses;
        this.masteries = masteries;
        this.skills = skills;
        this.thresholds = thresholds;
    }

    /** Re-aggregates and upserts the student's mastery for every skill they have been tested on. */
    public void recomputeForStudent(Student student) {
        Instant now = Instant.now();
        for (SkillAggregate agg : responses.aggregateByStudent(student.getId())) {
            double possible = agg.getPossible() == null ? 0 : agg.getPossible();
            double earned = agg.getEarned() == null ? 0 : agg.getEarned();
            double pct = possible > 0 ? round(earned / possible * 100.0) : 0.0;

            CompetencyMastery cm = masteries.findByStudentIdAndSkillId(student.getId(), agg.getSkillId())
                    .orElseGet(() -> {
                        Skill skill = skills.getReferenceById(agg.getSkillId());
                        return new CompetencyMastery(student, skill);
                    });
            cm.setMasteryScore(pct);
            cm.setLevel(levelFor(pct));
            cm.setUpdatedAt(now);
            masteries.save(cm);
        }
    }

    @Transactional(readOnly = true)
    public List<SkillMasteryDto> studentMastery(Long studentId) {
        return masteries.findByStudentWithSkill(studentId).stream()
                .map(cm -> new SkillMasteryDto(cm.getSkill().getId(), cm.getSkill().getName(),
                        cm.getMasteryScore(), cm.getLevel(), cm.getUpdatedAt()))
                .toList();
    }

    /** Class-wide ranking, lowest average mastery first (curriculum weak spots). */
    @Transactional(readOnly = true)
    public List<ClassSkillMasteryDto> classMasteryBySkill() {
        return masteries.classAverages().stream().map(this::toClassDto).toList();
    }

    /** Same ranking restricted to the answers given to one assessment. */
    @Transactional(readOnly = true)
    public List<ClassSkillMasteryDto> assessmentMasteryBySkill(Long assessmentId) {
        return responses.aggregateByAssessment(assessmentId).stream().map(this::toClassDto).toList();
    }

    public MasteryLevel levelFor(double pct) {
        if (pct >= thresholds.masteredMin()) return MasteryLevel.MASTERED;
        if (pct >= thresholds.proficientMin()) return MasteryLevel.PROFICIENT;
        if (pct >= thresholds.developingMin()) return MasteryLevel.DEVELOPING;
        return MasteryLevel.NOVICE;
    }

    private ClassSkillMasteryDto toClassDto(SkillAverage a) {
        double avg = a.getAvgMastery() == null ? 0 : round(a.getAvgMastery());
        return new ClassSkillMasteryDto(a.getSkillId(), a.getSkillName(), avg, levelFor(avg), a.getStudentCount());
    }

    private static double round(double v) { return Math.round(v * 100.0) / 100.0; }
}
