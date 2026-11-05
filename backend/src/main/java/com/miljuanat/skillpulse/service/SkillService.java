package com.miljuanat.skillpulse.service;

import com.miljuanat.skillpulse.dto.SkillDtos.SkillNode;
import com.miljuanat.skillpulse.dto.SkillDtos.SkillRequest;
import com.miljuanat.skillpulse.dto.SkillDtos.SkillResponse;
import com.miljuanat.skillpulse.exception.ApiException;
import com.miljuanat.skillpulse.model.Skill;
import com.miljuanat.skillpulse.repository.CompetencyMasteryRepository;
import com.miljuanat.skillpulse.repository.QuestionSkillMappingRepository;
import com.miljuanat.skillpulse.repository.SkillRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SkillService {

    private final SkillRepository skills;
    private final QuestionSkillMappingRepository mappings;
    private final CompetencyMasteryRepository masteries;

    public SkillService(SkillRepository skills, QuestionSkillMappingRepository mappings,
                        CompetencyMasteryRepository masteries) {
        this.skills = skills;
        this.mappings = mappings;
        this.masteries = masteries;
    }

    public SkillResponse create(SkillRequest req) {
        if (skills.existsByNameIgnoreCase(req.name())) throw ApiException.conflict("Skill name already exists");
        Skill skill = new Skill();
        skill.setName(req.name().trim());
        skill.setDescription(req.description());
        skill.setParent(resolveParent(req.parentId()));
        return toResponse(skills.save(skill));
    }

    public SkillResponse update(Long id, SkillRequest req) {
        Skill skill = find(id);
        if (!skill.getName().equalsIgnoreCase(req.name()) && skills.existsByNameIgnoreCase(req.name())) {
            throw ApiException.conflict("Skill name already exists");
        }
        Skill newParent = resolveParent(req.parentId());
        // Reject cycles: walk up from the new parent; we must not meet this skill.
        for (Skill p = newParent; p != null; p = p.getParent()) {
            if (p.getId().equals(id)) throw ApiException.badRequest("A skill cannot be its own ancestor");
        }
        skill.setName(req.name().trim());
        skill.setDescription(req.description());
        skill.setParent(newParent);
        return toResponse(skill);
    }

    public void delete(Long id) {
        Skill skill = find(id);
        if (skills.existsByParentId(id)) throw ApiException.conflict("Skill has child skills; delete or move them first");
        if (mappings.existsBySkillId(id) || masteries.existsBySkillId(id)) {
            throw ApiException.conflict("Skill is used by questions or mastery records and cannot be deleted");
        }
        skills.delete(skill);
    }

    @Transactional(readOnly = true)
    public SkillResponse get(Long id) { return toResponse(find(id)); }

    @Transactional(readOnly = true)
    public List<SkillResponse> list() {
        return skills.findAll().stream().sorted(Comparator.comparing(Skill::getName)).map(this::toResponse).toList();
    }

    /** Whole taxonomy as a nested tree (roots first). */
    @Transactional(readOnly = true)
    public List<SkillNode> tree() {
        List<Skill> all = skills.findAll();
        Map<Long, List<Skill>> byParent = new HashMap<>();
        List<Skill> roots = new ArrayList<>();
        for (Skill s : all) {
            if (s.getParent() == null) roots.add(s);
            else byParent.computeIfAbsent(s.getParent().getId(), k -> new ArrayList<>()).add(s);
        }
        return roots.stream().sorted(Comparator.comparing(Skill::getName)).map(r -> node(r, byParent)).toList();
    }

    private SkillNode node(Skill s, Map<Long, List<Skill>> byParent) {
        List<SkillNode> kids = byParent.getOrDefault(s.getId(), List.of()).stream()
                .sorted(Comparator.comparing(Skill::getName)).map(c -> node(c, byParent)).toList();
        return new SkillNode(s.getId(), s.getName(), s.getDescription(), kids);
    }

    private Skill resolveParent(Long parentId) {
        return parentId == null ? null : find(parentId);
    }

    private Skill find(Long id) {
        return skills.findById(id).orElseThrow(() -> ApiException.notFound("Skill " + id + " not found"));
    }

    private SkillResponse toResponse(Skill s) {
        return new SkillResponse(s.getId(), s.getName(), s.getDescription(),
                s.getParent() == null ? null : s.getParent().getId());
    }
}
