package com.miljuanat.skillpulse.controller;

import com.miljuanat.skillpulse.dto.AssessmentDtos.AssessmentRequest;
import com.miljuanat.skillpulse.dto.AssessmentDtos.AssessmentResponse;
import com.miljuanat.skillpulse.service.AssessmentService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** INSTRUCTOR only; every call is scoped to the caller's own assessments. */
@RestController
@RequestMapping("/api/instructor/assessments")
public class InstructorAssessmentController {

    private final AssessmentService service;

    public InstructorAssessmentController(AssessmentService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssessmentResponse create(@Valid @RequestBody AssessmentRequest req, Principal p) {
        return service.create(p.getName(), req);
    }

    @GetMapping
    public List<AssessmentResponse> list(Principal p) { return service.listOwned(p.getName()); }

    @GetMapping("/{id}")
    public AssessmentResponse get(@PathVariable Long id, Principal p) { return service.get(id, p.getName()); }

    @PutMapping("/{id}")
    public AssessmentResponse update(@PathVariable Long id, @Valid @RequestBody AssessmentRequest req, Principal p) {
        return service.update(id, p.getName(), req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Principal p) { service.delete(id, p.getName()); }

    /** DRAFT -> PUBLISHED (needs >=1 question, all mapped to skills). */
    @PostMapping("/{id}/publish")
    public AssessmentResponse publish(@PathVariable Long id, Principal p) { return service.publish(id, p.getName()); }

    /** PUBLISHED -> CLOSED. */
    @PostMapping("/{id}/close")
    public AssessmentResponse close(@PathVariable Long id, Principal p) { return service.close(id, p.getName()); }
}
