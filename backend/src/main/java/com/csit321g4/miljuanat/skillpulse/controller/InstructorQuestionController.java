package com.csit321g4.miljuanat.skillpulse.controller;

import com.csit321g4.miljuanat.skillpulse.dto.AssessmentDtos.QuestionRequest;
import com.csit321g4.miljuanat.skillpulse.dto.AssessmentDtos.QuestionResponse;
import com.csit321g4.miljuanat.skillpulse.service.QuestionService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Questions of an assessment, each carrying its skill mappings [{skillId, weight}]. Edits only while DRAFT. */
@RestController
@RequestMapping("/api/instructor/assessments/{assessmentId}/questions")
public class InstructorQuestionController {

    private final QuestionService service;

    public InstructorQuestionController(QuestionService service) { this.service = service; }

    @GetMapping
    public List<QuestionResponse> list(@PathVariable Long assessmentId, Principal p) {
        return service.list(assessmentId, p.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionResponse add(@PathVariable Long assessmentId, @Valid @RequestBody QuestionRequest req, Principal p) {
        return service.add(assessmentId, p.getName(), req);
    }

    @PutMapping("/{questionId}")
    public QuestionResponse update(@PathVariable Long assessmentId, @PathVariable Long questionId,
                                   @Valid @RequestBody QuestionRequest req, Principal p) {
        return service.update(assessmentId, questionId, p.getName(), req);
    }

    @DeleteMapping("/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long assessmentId, @PathVariable Long questionId, Principal p) {
        service.remove(assessmentId, questionId, p.getName());
    }
}
