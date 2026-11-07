package com.csit321g4.miljuanat.skillpulse.model;

import jakarta.persistence.*;
import java.time.Instant;

/** One graded answer by one student to one question (one attempt per student per question). */
@Entity
@Table(name = "student_responses",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "question_id"}))
public class StudentResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(length = 2000)
    private String answer;

    @Column(nullable = false)
    private boolean correct;

    @Column(nullable = false)
    private double score;

    @Column(nullable = false)
    private Instant submittedAt = Instant.now();

    public StudentResponse() {}

    public StudentResponse(Student student, Question question, String answer, boolean correct, double score) {
        this.student = student;
        this.question = question;
        this.answer = answer;
        this.correct = correct;
        this.score = score;
    }

    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public Question getQuestion() { return question; }
    public String getAnswer() { return answer; }
    public boolean isCorrect() { return correct; }
    public double getScore() { return score; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
}
