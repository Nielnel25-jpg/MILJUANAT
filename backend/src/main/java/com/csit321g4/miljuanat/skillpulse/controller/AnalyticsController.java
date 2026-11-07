package com.csit321g4.miljuanat.skillpulse.controller;

import com.csit321g4.miljuanat.skillpulse.dto.AnalyticsDtos.ClassSkillMasteryDto;
import com.csit321g4.miljuanat.skillpulse.dto.AnalyticsDtos.SkillMasteryDto;
import com.csit321g4.miljuanat.skillpulse.dto.UserDtos.UserResponse;
import com.csit321g4.miljuanat.skillpulse.exception.ApiException;
import com.csit321g4.miljuanat.skillpulse.model.Role;
import com.csit321g4.miljuanat.skillpulse.repository.StudentRepository;
import com.csit321g4.miljuanat.skillpulse.service.AssessmentService;
import com.csit321g4.miljuanat.skillpulse.service.MasteryService;
import com.csit321g4.miljuanat.skillpulse.service.UserService;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read endpoints feeding the React dashboards. */
@RestController
@RequestMapping("/api")
public class AnalyticsController {

    private final MasteryService masteryService;
    private final AssessmentService assessmentService;
    private final UserService userService;
    private final StudentRepository students;

    public AnalyticsController(MasteryService masteryService, AssessmentService assessmentService,
                               UserService userService, StudentRepository students) {
        this.masteryService = masteryService;
        this.assessmentService = assessmentService;
        this.userService = userService;
        this.students = students;
    }

    /** STUDENT: own per-skill mastery (feeds the radar/bar charts), weakest first. */
    @GetMapping("/student/analytics/mastery")
    public List<SkillMasteryDto> myMastery(Principal p) {
        Long id = students.findByEmail(p.getName()).orElseThrow(() -> ApiException.notFound("Student not found")).getId();
        return masteryService.studentMastery(id);
    }

    /** INSTRUCTOR: class-wide average mastery per skill, lowest first (common weak spots). */
    @GetMapping("/instructor/analytics/class/skills")
    public List<ClassSkillMasteryDto> classSkills() { return masteryService.classMasteryBySkill(); }

    /** INSTRUCTOR: same ranking for one of their own assessments. */
    @GetMapping("/instructor/analytics/assessments/{assessmentId}/skills")
    public List<ClassSkillMasteryDto> assessmentSkills(@PathVariable Long assessmentId, Principal p) {
        assessmentService.requireOwned(assessmentId, p.getName());
        return masteryService.assessmentMasteryBySkill(assessmentId);
    }

    /** INSTRUCTOR: list students (to pick one). */
    @GetMapping("/instructor/students")
    public List<UserResponse> studentList() { return userService.list(Role.STUDENT); }

    /** INSTRUCTOR: mastery progress of one student. */
    @GetMapping("/instructor/analytics/students/{studentId}/mastery")
    public List<SkillMasteryDto> studentMastery(@PathVariable Long studentId) {
        if (!students.existsById(studentId)) throw ApiException.notFound("Student " + studentId + " not found");
        return masteryService.studentMastery(studentId);
    }
}
