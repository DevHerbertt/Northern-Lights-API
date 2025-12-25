package com.NorthrnLights.demo.controller;

import com.NorthrnLights.demo.domain.ExamGrade;
import com.NorthrnLights.demo.dto.ExamGradeDTO;
import com.NorthrnLights.demo.service.ExamGradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exam-grades")
@RequiredArgsConstructor
@Slf4j
public class ExamGradeController {

    private final ExamGradeService examGradeService;

    @PostMapping
    public ResponseEntity<ExamGrade> createExamGrade(
            @RequestBody ExamGradeDTO dto,
            Authentication authentication) {
        try {
            log.info("Recebida requisição para criar nota de prova para estudante ID: {}", dto.getStudentId());
            ExamGrade examGrade = examGradeService.createExamGrade(dto, authentication);
            return ResponseEntity.status(HttpStatus.CREATED).body(examGrade);
        } catch (Exception e) {
            log.error("Erro ao criar nota de prova", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<ExamGrade>> getStudentExamGrades(@PathVariable Long studentId) {
        try {
            List<ExamGrade> grades = examGradeService.getStudentExamGrades(studentId);
            return ResponseEntity.ok(grades);
        } catch (Exception e) {
            log.error("Erro ao buscar notas de prova do estudante", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/exam/{examId}")
    public ResponseEntity<List<ExamGrade>> getExamGrades(@PathVariable Long examId) {
        try {
            List<ExamGrade> grades = examGradeService.getExamGrades(examId);
            return ResponseEntity.ok(grades);
        } catch (Exception e) {
            log.error("Erro ao buscar notas da prova", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

