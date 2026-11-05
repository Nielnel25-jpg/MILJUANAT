package com.miljuanat.skillpulse.controller;

import com.miljuanat.skillpulse.dto.SkillDtos.SkillNode;
import com.miljuanat.skillpulse.dto.SkillDtos.SkillResponse;
import com.miljuanat.skillpulse.service.SkillService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only taxonomy access for any authenticated user (instructors need it to tag questions). */
@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) { this.skillService = skillService; }

    @GetMapping
    public List<SkillResponse> list() { return skillService.list(); }

    @GetMapping("/tree")
    public List<SkillNode> tree() { return skillService.tree(); }

    @GetMapping("/{id}")
    public SkillResponse get(@PathVariable Long id) { return skillService.get(id); }
}
