package com.csit321g4.miljuanat.skillpulse.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public final class SkillDtos {
    private SkillDtos() {}

    public record SkillRequest(@NotBlank String name, String description, Long parentId) {}

    public record SkillResponse(Long id, String name, String description, Long parentId) {}

    public record SkillNode(Long id, String name, String description, List<SkillNode> children) {}
}
