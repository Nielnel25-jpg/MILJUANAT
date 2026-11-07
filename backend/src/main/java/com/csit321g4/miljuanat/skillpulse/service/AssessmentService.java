package com.csit321g4.miljuanat.skillpulse.service;

import com.csit321g4.miljuanat.skillpulse.dto.AssessmentDtos.AssessmentRequest;
import com.csit321g4.miljuanat.skillpulse.dto.AssessmentDtos.AssessmentResponse;
import com.csit321g4.miljuanat.skillpulse.exception.ApiException;
import com.csit321g4.miljuanat.skillpulse.model.Assessment;
import com.csit321g4.miljuanat.skillpulse.model.AssessmentStatus;
import com.csit321g4.miljuanat.skillpulse.model.Instructor;
import com.csit321g4.miljuanat.skillpulse.model.Question;
import com.csit321g4.miljuanat.skillpulse.repository.AssessmentRepository;
import com.csit321g4.miljuanat.skillpulse.repository.InstructorRepository;
import com.csit321g4.miljuanat.skillpulse.repository.StudentResponseRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Instructor-side assessment lifecycle: DRAFT -> PUBLISHED -> CLOSED. Instructors only touch their own assessments. */
@Service
@Transactional
public class AssessmentService {

    private final AssessmentRepository assessments;
    private final InstructorRepository instructors;
    private final StudentResponseRepository responses;

    public AssessmentService(AssessmentRepository assessments, InstructorRepository instructors,
                             StudentResponseRepository responses) {
        this.assessments = assessments;
        this.instructors = instructors;
        this.responses = responses;
    }

    public AssessmentResponse create(String instructorEmail, AssessmentRequest req) {
        Instructor instructor = instructors.findByEmail(instructorEmail)
                .orElseThrow(() -> ApiException.notFound("Instructor not found"));
        Assessment a = new Assessment();
        a.setInstructor(instructor);
        apply(a, req);
        return toResponse(assessments.save(a));
    }

    @Transactional(readOnly = true)
    public List<AssessmentResponse> listOwned(String instructorEmail) {
        Instructor instructor = instructors.findByEmail(instructorEmail)
                .orElseThrow(() -> ApiException.notFound("Instructor not found"));
        return assessments.findByInstructorIdOrderByCreatedAtDesc(instructor.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AssessmentResponse get(Long id, String instructorEmail) {
        return toResponse(requireOwned(id, instructorEmail));
    }

    public AssessmentResponse update(Long id, String instructorEmail, AssessmentRequest req) {
        Assessment a = requireOwned(id, instructorEmail);
        if (a.getStatus() == AssessmentStatus.CLOSED) throw ApiException.conflict("Closed assessments cannot be edited");
        apply(a, req);
        return toResponse(a);
    }

    public void delete(Long id, String instructorEmail) {
        Assessment a = requireOwned(id, instructorEmail);
        if (responses.existsForAssessment(id)) {
            throw ApiException.conflict("Students have already submitted; close the assessment instead of deleting it");
        }
        assessments.delete(a);
    }

    /** Publishing requires at least one question and every question mapped to at least one skill. */
    public AssessmentResponse publish(Long id, String instructorEmail) {
        Assessment a = requireOwned(id, instructorEmail);
        if (a.getStatus() != AssessmentStatus.DRAFT) throw ApiException.conflict("Only draft assessments can be published");
        if (a.getQuestions().isEmpty()) throw ApiException.badRequest("Add at least one question before publishing");
        for (Question q : a.getQuestions()) {
            if (q.getSkillMappings().isEmpty()) {
                throw ApiException.badRequest("Question " + q.getId() + " is not mapped to any skill");
            }
        }
        a.setStatus(AssessmentStatus.PUBLISHED);
        return toResponse(a);
    }

    public AssessmentResponse close(Long id, String instructorEmail) {
        Assessment a = requireOwned(id, instructorEmail);
        if (a.getStatus() != AssessmentStatus.PUBLISHED) throw ApiException.conflict("Only published assessments can be closed");
        a.setStatus(AssessmentStatus.CLOSED);
        return toResponse(a);
    }

    /** Shared ownership check, also used by QuestionService and analytics. */
    @Transactional(readOnly = true)
    public Assessment requireOwned(Long id, String instructorEmail) {
        Assessment a = assessments.findById(id)
                .orElseThrow(() -> ApiException.notFound("Assessment " + id + " not found"));
        if (!a.getInstructor().getEmail().equalsIgnoreCase(instructorEmail)) {
            throw ApiException.forbidden("This assessment belongs to another instructor");
        }
        return a;
    }

    private void apply(Assessment a, AssessmentRequest req) {
        if (req.availableFrom() != null && req.dueDate() != null && req.dueDate().isBefore(req.availableFrom())) {
            throw ApiException.badRequest("dueDate must not be before availableFrom");
        }
        a.setTitle(req.title());
        a.setDescription(req.description());
        a.setType(req.type());
        a.setAvailableFrom(req.availableFrom());
        a.setDueDate(req.dueDate());
        a.setTimeLimitMinutes(req.timeLimitMinutes());
    }

    private AssessmentResponse toResponse(Assessment a) {
        return new AssessmentResponse(a.getId(), a.getTitle(), a.getDescription(), a.getType(), a.getStatus(),
                a.getAvailableFrom(), a.getDueDate(), a.getTimeLimitMinutes(),
                a.getInstructor().getId(), a.getQuestions().size(), a.getCreatedAt());
    }
}
