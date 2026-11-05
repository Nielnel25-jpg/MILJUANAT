package com.miljuanat.skillpulse.dto;

import com.miljuanat.skillpulse.model.AssessmentStatus;
import com.miljuanat.skillpulse.model.AssessmentType;
import com.miljuanat.skillpulse.model.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.List;

public final class AssessmentDtos {
    private AssessmentDtos() {}

    public record AssessmentRequest(
            @NotBlank String title,
            String description,
            @NotNull AssessmentType type,
            Instant availableFrom,
            Instant dueDate,
            @Min(1) Integer timeLimitMinutes) {}

    public record AssessmentResponse(
            Long id, String title, String description, AssessmentType type, AssessmentStatus status,
            Instant availableFrom, Instant dueDate, Integer timeLimitMinutes,
            Long instructorId, int questionCount, Instant createdAt) {}

    /** Question-to-skill mapping as sent by the instructor. */
    public record SkillWeight(@NotNull Long skillId, @Positive double weight) {}

    public record QuestionRequest(
            @NotBlank String text,
            @NotNull QuestionType type,
            List<String> choices,
            @NotBlank String correctAnswer,
            @Positive Double points,
            @NotEmpty @Valid List<SkillWeight> skills) {}

    public record SkillWeightView(Long skillId, String skillName, double weight) {}

    /** Instructor view of a question (includes the answer key). */
    public record QuestionResponse(
            Long id, String text, QuestionType type, List<String> choices, String correctAnswer,
            double points, List<SkillWeightView> skills) {}
}
