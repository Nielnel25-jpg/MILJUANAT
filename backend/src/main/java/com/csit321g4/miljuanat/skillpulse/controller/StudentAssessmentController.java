package com.csit321g4.miljuanat.skillpulse.controller;

import com.csit321g4.miljuanat.skillpulse.dto.SubmissionDtos.StudentAssessmentSummary;
import com.csit321g4.miljuanat.skillpulse.dto.SubmissionDtos.StudentAssessmentView;
import com.csit321g4.miljuanat.skillpulse.dto.SubmissionDtos.SubmissionResult;
import com.csit321g4.miljuanat.skillpulse.dto.SubmissionDtos.SubmitRequest;
import com.csit321g4.miljuanat.skillpulse.service.SubmissionService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** STUDENT only. */
@RestController
@RequestMapping("/api/student/assessments")
public class StudentAssessmentController {

    private final SubmissionService service;

    public StudentAssessmentController(SubmissionService service) { this.service = service; }

    /** Published assessments that are currently open, flagged with whether the student already submitted. */
    @GetMapping
    public List<StudentAssessmentSummary> available(Principal p) { return service.listAvailable(p.getName()); }

    /** Questions to answer (no answer key). */
    @GetMapping("/{id}")
    public StudentAssessmentView open(@PathVariable Long id, Principal p) { return service.open(id, p.getName()); }

    /** Submit answers: auto-grades, stores responses, recomputes skill mastery, returns result + updated mastery. */
    @PostMapping("/{id}/submit")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionResult submit(@PathVariable Long id, @Valid @RequestBody SubmitRequest req, Principal p) {
        return service.submit(id, p.getName(), req);
    }

    /** Review feedback for a completed assessment. */
    @GetMapping("/{id}/result")
    public SubmissionResult result(@PathVariable Long id, Principal p) { return service.result(id, p.getName()); }
}
