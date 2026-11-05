package com.miljuanat.skillpulse.dto;

import com.miljuanat.skillpulse.model.MasteryLevel;
import java.time.Instant;

public final class AnalyticsDtos {
    private AnalyticsDtos() {}

    public record SkillMasteryDto(Long skillId, String skillName, double masteryScore, MasteryLevel level, Instant updatedAt) {}

    public record ClassSkillMasteryDto(Long skillId, String skillName, double averageMastery, MasteryLevel level, long studentCount) {}
}
