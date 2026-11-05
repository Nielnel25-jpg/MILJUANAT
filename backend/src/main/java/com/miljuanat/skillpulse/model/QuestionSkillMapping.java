package com.miljuanat.skillpulse.model;

import jakarta.persistence.*;

/** Join entity: a question measures a skill with a given weight. */
@Entity
@Table(name = "question_skill_mappings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"question_id", "skill_id"}))
public class QuestionSkillMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id")
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @Column(nullable = false)
    private double weight = 1.0;

    public QuestionSkillMapping() {}

    public QuestionSkillMapping(Skill skill, double weight) {
        this.skill = skill;
        this.weight = weight;
    }

    public Long getId() { return id; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public Skill getSkill() { return skill; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
}
