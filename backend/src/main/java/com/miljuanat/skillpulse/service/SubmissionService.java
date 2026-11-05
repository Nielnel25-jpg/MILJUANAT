package com.miljuanat.skillpulse.service;

import com.miljuanat.skillpulse.dto.SubmissionDtos.AnswerRequest;
import com.miljuanat.skillpulse.dto.SubmissionDtos.QuestionResult;
import com.miljuanat.skillpulse.dto.SubmissionDtos.StudentAssessmentSummary;
import com.miljuanat.skillpulse.dto.SubmissionDtos.StudentAssessmentView;
import com.miljuanat.skillpulse.dto.SubmissionDtos.StudentQuestionView;
import com.miljuanat.skillpulse.dto.SubmissionDtos.SubmissionResult;
import com.miljuanat.skillpulse.dto.SubmissionDtos.SubmitRequest;
import com.miljuanat.skillpulse.exception.ApiException;
import com.miljuanat.skillpulse.model.Assessment;
import com.miljuanat.skillpulse.model.AssessmentStatus;
import com.miljuanat.skillpulse.model.Question;
import com.miljuanat.skillpulse.model.Student;
import com.miljuanat.skillpulse.model.StudentResponse;
import com.miljuanat.skillpulse.repository.AssessmentRepository;
import com.miljuanat.skillpulse.repository.StudentRepository;
import com.miljuanat.skillpulse.repository.StudentResponseRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Student side: list/open published assessments, submit answers (auto-graded), review results. */
@Service
@Transactional
public class SubmissionService {

    private final AssessmentRepository assessments;
    private final StudentRepository students;
    private final StudentResponseRepository responses;
    private final MasteryService masteryService;

    public SubmissionService(AssessmentRepository assessments, StudentRepository students,
                             StudentResponseRepository responses, MasteryService masteryService) {
        this.assessments = assessments;
        this.students = students;
        this.responses = responses;
        this.masteryService = masteryService;
    }

    @Transactional(readOnly = true)
    public List<StudentAssessmentSummary> listAvailable(String email) {
        Student student = student(email);
        Instant now = Instant.now();
        return assessments.findByStatusOrderByCreatedAtDesc(AssessmentStatus.PUBLISHED).stream()
                .filter(a -> isOpen(a, now))
                .map(a -> new StudentAssessmentSummary(a.getId(), a.getTitle(), a.getDescription(), a.getType(),
                        a.getAvailableFrom(), a.getDueDate(), a.getTimeLimitMinutes(),
                        responses.existsSubmission(student.getId(), a.getId())))
                .toList();
    }

    /** Questions for taking the assessment; the answer key and skill mapping are never exposed. */
    @Transactional(readOnly = true)
    public StudentAssessmentView open(Long assessmentId, String email) {
        Student student = student(email);
        Assessment a = find(assessmentId);
        if (!isOpen(a, Instant.now())) throw ApiException.forbidden("This assessment is not open");
        if (responses.existsSubmission(student.getId(), assessmentId)) {
            throw ApiException.conflict("You have already submitted this assessment");
        }
        List<StudentQuestionView> qs = a.getQuestions().stream()
                .map(q -> new StudentQuestionView(q.getId(), q.getText(), q.getType(), List.copyOf(q.getChoices()), q.getPoints()))
                .toList();
        return new StudentAssessmentView(a.getId(), a.getTitle(), a.getDescription(), a.getType(),
                a.getDueDate(), a.getTimeLimitMinutes(), qs);
    }

    /**
     * Grades every question (unanswered = wrong), stores one StudentResponse per question,
     * then triggers the mastery aggregation for this student.
     */
    public SubmissionResult submit(Long assessmentId, String email, SubmitRequest req) {
        Student student = student(email);
        Assessment a = find(assessmentId);
        if (!isOpen(a, Instant.now())) throw ApiException.forbidden("This assessment is not open for submissions");
        if (responses.existsSubmission(student.getId(), assessmentId)) {
            throw ApiException.conflict("You have already submitted this assessment");
        }

        Set<Long> validIds = a.getQuestions().stream().map(Question::getId).collect(Collectors.toSet());
        Map<Long, String> given = new HashMap<>();
        for (AnswerRequest ans : req.answers()) {
            if (!validIds.contains(ans.questionId())) {
                throw ApiException.badRequest("Question " + ans.questionId() + " is not part of this assessment");
            }
            if (given.putIfAbsent(ans.questionId(), ans.answer()) != null) {
                throw ApiException.badRequest("Question " + ans.questionId() + " answered more than once");
            }
        }

        Instant now = Instant.now();
        for (Question q : a.getQuestions()) {
            String answer = given.get(q.getId());
            boolean correct = answer != null && normalize(answer).equals(normalize(q.getCorrectAnswer()));
            StudentResponse r = new StudentResponse(student, q, answer, correct, correct ? q.getPoints() : 0.0);
            r.setSubmittedAt(now);
            responses.save(r);
        }
        responses.flush();                           // aggregation query must see the new rows
        masteryService.recomputeForStudent(student);

        return buildResult(a, student);
    }

    @Transactional(readOnly = true)
    public SubmissionResult result(Long assessmentId, String email) {
        Student student = student(email);
        Assessment a = find(assessmentId);
        if (!responses.existsSubmission(student.getId(), assessmentId)) {
            throw ApiException.notFound("You have not submitted this assessment");
        }
        return buildResult(a, student);
    }

    private SubmissionResult buildResult(Assessment a, Student student) {
        List<StudentResponse> rs = responses.findSubmission(student.getId(), a.getId());
        List<QuestionResult> details = rs.stream().map(r -> new QuestionResult(
                r.getQuestion().getId(), r.getQuestion().getText(), r.getAnswer(),
                r.getQuestion().getCorrectAnswer(), r.isCorrect(), r.getScore(), r.getQuestion().getPoints())).toList();
        double total = rs.stream().mapToDouble(StudentResponse::getScore).sum();
        double max = rs.stream().mapToDouble(r -> r.getQuestion().getPoints()).sum();
        double pct = max > 0 ? Math.round(total / max * 10000.0) / 100.0 : 0.0;
        Instant submittedAt = rs.isEmpty() ? null : rs.get(0).getSubmittedAt();
        return new SubmissionResult(a.getId(), a.getTitle(), total, max, pct, submittedAt, details,
                masteryService.studentMastery(student.getId()));
    }

    private boolean isOpen(Assessment a, Instant now) {
        return a.getStatus() == AssessmentStatus.PUBLISHED
                && (a.getAvailableFrom() == null || !now.isBefore(a.getAvailableFrom()))
                && (a.getDueDate() == null || !now.isAfter(a.getDueDate()));
    }

    private static String normalize(String s) { return s.trim().replaceAll("\\s+", " ").toLowerCase(); }

    private Student student(String email) {
        return students.findByEmail(email).orElseThrow(() -> ApiException.notFound("Student not found"));
    }

    private Assessment find(Long id) {
        return assessments.findById(id).orElseThrow(() -> ApiException.notFound("Assessment " + id + " not found"));
    }
}
