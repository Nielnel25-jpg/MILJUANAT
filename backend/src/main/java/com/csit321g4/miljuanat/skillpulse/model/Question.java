package com.csit321g4.miljuanat.skillpulse.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id")
    private Assessment assessment;

    @Column(name = "question_text", nullable = false, length = 2000)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType type;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "question_choices", joinColumns = @JoinColumn(name = "question_id"))
    @OrderColumn(name = "choice_order")
    @Column(name = "choice_text")
    private List<String> choices = new ArrayList<>();

    @Column(nullable = false)
    private String correctAnswer;

    @Column(nullable = false)
    private double points = 1.0;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionSkillMapping> skillMappings = new ArrayList<>();

    public void addSkillMapping(QuestionSkillMapping m) { skillMappings.add(m); m.setQuestion(this); }
    public void removeSkillMapping(QuestionSkillMapping m) { skillMappings.remove(m); m.setQuestion(null); }

    public Long getId() { return id; }
    public Assessment getAssessment() { return assessment; }
    public void setAssessment(Assessment assessment) { this.assessment = assessment; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public QuestionType getType() { return type; }
    public void setType(QuestionType type) { this.type = type; }
    public List<String> getChoices() { return choices; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public double getPoints() { return points; }
    public void setPoints(double points) { this.points = points; }
    public List<QuestionSkillMapping> getSkillMappings() { return skillMappings; }
}
