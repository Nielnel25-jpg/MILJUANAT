package com.miljuanat.skillpulse.controller;

import com.miljuanat.skillpulse.dto.SkillDtos.SkillRequest;
import com.miljuanat.skillpulse.dto.SkillDtos.SkillResponse;
import com.miljuanat.skillpulse.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Skill taxonomy management - ADMIN only. Reading lives in SkillController (any authenticated user). */
@RestController
@RequestMapping("/api/admin/skills")
public class AdminSkillController {

    private final SkillService skillService;

    public AdminSkillController(SkillService skillService) { this.skillService = skillService; }

    /** POST - create a skill; parentId makes it a child (hierarchy). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SkillResponse create(@Valid @RequestBody SkillRequest req) { return skillService.create(req); }

    /** PUT - rename / move (cycles rejected). */
    @PutMapping("/{id}")
    public SkillResponse update(@PathVariable Long id, @Valid @RequestBody SkillRequest req) {
        return skillService.update(id, req);
    }

    /** DELETE - 409 if the skill has children, mapped questions or mastery records. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { skillService.delete(id); }
}
