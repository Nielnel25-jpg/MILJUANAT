package com.miljuanat.skillpulse.dto;

import com.miljuanat.skillpulse.model.AssessmentType;
import com.miljuanat.skillpulse.model.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public final class SubmissionDtos {
    private SubmissionDtos() {}

    public record StudentAssessmentSummary(
            Long id, String title, String description, AssessmentType type,
            Instant availableFrom, Instant dueDate, Integer timeLimitMinutes, boolean submitted) {}

    /** Student-facing question: no answer key, no skill mapping. */
    public record StudentQuestionView(Long id, String text, QuestionType type, List<String> choices, double points) {}

    public record StudentAssessmentView(
            Long id, String title, String description, AssessmentType type,
            Instant dueDate, Integer timeLimitMinutes, List<StudentQuestionView> questions) {}

    public record AnswerRequest(@NotNull Long questionId, String answer) {}

    public record SubmitRequest(@NotNull @Valid List<AnswerRequest> answers) {}

    public record QuestionResult(
            Long questionId, String text, String yourAnswer, String correctAnswer,
            boolean correct, double score, double points) {}

    public record SubmissionResult(
            Long assessmentId, String title, double totalScore, double maxScore, double percentage,
            Instant submittedAt, List<QuestionResult> questions,
            List<AnalyticsDtos.SkillMasteryDto> updatedMastery) {}
}
