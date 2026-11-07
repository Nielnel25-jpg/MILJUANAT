package com.csit321g4.miljuanat.skillpulse.service;

import com.csit321g4.miljuanat.skillpulse.dto.AssessmentDtos.QuestionRequest;
import com.csit321g4.miljuanat.skillpulse.dto.AssessmentDtos.QuestionResponse;
import com.csit321g4.miljuanat.skillpulse.dto.AssessmentDtos.SkillWeight;
import com.csit321g4.miljuanat.skillpulse.dto.AssessmentDtos.SkillWeightView;
import com.csit321g4.miljuanat.skillpulse.exception.ApiException;
import com.csit321g4.miljuanat.skillpulse.model.Assessment;
import com.csit321g4.miljuanat.skillpulse.model.AssessmentStatus;
import com.csit321g4.miljuanat.skillpulse.model.Question;
import com.csit321g4.miljuanat.skillpulse.model.QuestionSkillMapping;
import com.csit321g4.miljuanat.skillpulse.repository.SkillRepository;
import jakarta.persistence.EntityManager;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Question CRUD plus question-to-skill mapping (with weights). Questions are editable only while the assessment is DRAFT. */
@Service
@Transactional
public class QuestionService {

    private final AssessmentService assessmentService;
    private final SkillRepository skills;
    private final EntityManager em;

    public QuestionService(AssessmentService assessmentService, SkillRepository skills, EntityManager em) {
        this.assessmentService = assessmentService;
        this.skills = skills;
        this.em = em;
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> list(Long assessmentId, String email) {
        return assessmentService.requireOwned(assessmentId, email).getQuestions().stream().map(this::toResponse).toList();
    }

    public QuestionResponse add(Long assessmentId, String email, QuestionRequest req) {
        Assessment a = editable(assessmentId, email);
        Question q = new Question();
        apply(q, req);
        a.addQuestion(q);
        em.persist(q);   // cascades to the skill mappings
        em.flush();      // generates ids so the response carries them
        return toResponse(q);
    }

    public QuestionResponse update(Long assessmentId, Long questionId, String email, QuestionRequest req) {
        Assessment a = editable(assessmentId, email);
        Question q = find(a, questionId);
        apply(q, req);
        return toResponse(q);
    }

    public void remove(Long assessmentId, Long questionId, String email) {
        Assessment a = editable(assessmentId, email);
        a.removeQuestion(find(a, questionId));
    }

    private Assessment editable(Long assessmentId, String email) {
        Assessment a = assessmentService.requireOwned(assessmentId, email);
        if (a.getStatus() != AssessmentStatus.DRAFT) {
            throw ApiException.conflict("Questions can only be changed while the assessment is a draft");
        }
        return a;
    }

    private Question find(Assessment a, Long questionId) {
        return a.getQuestions().stream().filter(q -> q.getId().equals(questionId)).findFirst()
                .orElseThrow(() -> ApiException.notFound("Question " + questionId + " not found in this assessment"));
    }

    private void apply(Question q, QuestionRequest req) {
        q.setText(req.text());
        q.setType(req.type());
        q.setPoints(req.points() == null ? 1.0 : req.points());

        q.getChoices().clear();
        switch (req.type()) {
            case MULTIPLE_CHOICE -> {
                List<String> choices = req.choices() == null ? List.of() : req.choices().stream().map(String::trim).toList();
                if (choices.size() < 2 || choices.stream().anyMatch(String::isBlank)) {
                    throw ApiException.badRequest("Multiple choice questions need at least two non-blank choices");
                }
                if (choices.stream().noneMatch(c -> c.equalsIgnoreCase(req.correctAnswer().trim()))) {
                    throw ApiException.badRequest("correctAnswer must be one of the choices");
                }
                q.getChoices().addAll(choices);
                q.setCorrectAnswer(req.correctAnswer().trim());
            }
            case TRUE_FALSE -> {
                String ans = req.correctAnswer().trim();
                if (!ans.equalsIgnoreCase("true") && !ans.equalsIgnoreCase("false")) {
                    throw ApiException.badRequest("correctAnswer must be 'true' or 'false'");
                }
                q.getChoices().addAll(List.of("True", "False"));
                q.setCorrectAnswer(ans.equalsIgnoreCase("true") ? "True" : "False");
            }
            case SHORT_ANSWER -> q.setCorrectAnswer(req.correctAnswer().trim());
        }
        applyMappings(q, req.skills());
    }

    /** Syncs mappings in place (update weights, drop removed, add new) to respect the (question, skill) unique key. */
    private void applyMappings(Question q, List<SkillWeight> requested) {
        Set<Long> seen = new HashSet<>();
        for (SkillWeight sw : requested) {
            if (!seen.add(sw.skillId())) throw ApiException.badRequest("Skill " + sw.skillId() + " is listed twice");
        }
        Map<Long, QuestionSkillMapping> existing = q.getSkillMappings().stream()
                .collect(Collectors.toMap(m -> m.getSkill().getId(), Function.identity()));

        q.getSkillMappings().removeIf(m -> {
            boolean drop = !seen.contains(m.getSkill().getId());
            if (drop) m.setQuestion(null);
            return drop;
        });
        for (SkillWeight sw : requested) {
            QuestionSkillMapping m = existing.get(sw.skillId());
            if (m != null) {
                m.setWeight(sw.weight());
            } else {
                var skill = skills.findById(sw.skillId())
                        .orElseThrow(() -> ApiException.badRequest("Skill " + sw.skillId() + " does not exist"));
                q.addSkillMapping(new QuestionSkillMapping(skill, sw.weight()));
            }
        }
    }

    private QuestionResponse toResponse(Question q) {
        List<SkillWeightView> views = q.getSkillMappings().stream()
                .map(m -> new SkillWeightView(m.getSkill().getId(), m.getSkill().getName(), m.getWeight())).toList();
        return new QuestionResponse(q.getId(), q.getText(), q.getType(), List.copyOf(q.getChoices()),
                q.getCorrectAnswer(), q.getPoints(), views);
    }
}
