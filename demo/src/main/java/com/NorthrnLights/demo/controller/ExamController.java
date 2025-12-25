package com.NorthrnLights.demo.controller;

import com.NorthrnLights.demo.domain.Exam;
import com.NorthrnLights.demo.dto.ExamDTO;
import com.NorthrnLights.demo.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @PostMapping
    public ResponseEntity<Exam> create(@RequestBody ExamDTO dto, Authentication authentication) {
        Exam exam = examService.createExam(dto, authentication);
        return ResponseEntity.ok(exam);
    }

    @GetMapping
    public ResponseEntity<List<Exam>> getAll() {
        return ResponseEntity.ok(examService.findAll());
    }

    @GetMapping("/my-exams")
    public ResponseEntity<List<Exam>> getMyExams(Authentication authentication) {
        return ResponseEntity.ok(examService.findByTeacher(authentication));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Exam> getById(@PathVariable Long id) {
        return ResponseEntity.ok(examService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Exam> update(@PathVariable Long id, @RequestBody ExamDTO dto, Authentication authentication) {
        Exam exam = examService.updateExam(id, dto, authentication);
        return ResponseEntity.ok(exam);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        examService.deleteExam(id, authentication);
        return ResponseEntity.noContent().build();
    }
}





