package com.miljuanat.skillpulse.model;

import jakarta.persistence.*;
import java.time.Instant;

/** Materialised mastery of one skill by one student; recomputed after every submission. */
@Entity
@Table(name = "competency_mastery",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "skill_id"}))
public class CompetencyMastery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id")
    private Skill skill;

    /** Percentage 0..100. */
    @Column(nullable = false)
    private double masteryScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MasteryLevel level = MasteryLevel.NOVICE;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public CompetencyMastery() {}

    public CompetencyMastery(Student student, Skill skill) {
        this.student = student;
        this.skill = skill;
    }

    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public Skill getSkill() { return skill; }
    public double getMasteryScore() { return masteryScore; }
    public void setMasteryScore(double masteryScore) { this.masteryScore = masteryScore; }
    public MasteryLevel getLevel() { return level; }
    public void setLevel(MasteryLevel level) { this.level = level; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
