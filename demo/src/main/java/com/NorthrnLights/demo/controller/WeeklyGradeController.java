package com.NorthrnLights.demo.controller;

import com.NorthrnLights.demo.domain.WeeklyGrade;
import com.NorthrnLights.demo.dto.WeeklyGradeDTO;
import com.NorthrnLights.demo.service.WeeklyGradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/weekly-grades")
@RequiredArgsConstructor
@Slf4j
public class WeeklyGradeController {

    private final WeeklyGradeService weeklyGradeService;

    @PostMapping
    public ResponseEntity<WeeklyGrade> createWeeklyGrade(
            @RequestBody WeeklyGradeDTO dto,
            Authentication authentication) {
        try {
            log.info("Recebida requisição para criar nota semanal para estudante ID: {}", dto.getStudentId());
            WeeklyGrade weeklyGrade = weeklyGradeService.createWeeklyGrade(dto, authentication);
            return ResponseEntity.status(HttpStatus.CREATED).body(weeklyGrade);
        } catch (Exception e) {
            log.error("Erro ao criar nota semanal", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<WeeklyGrade>> getStudentWeeklyGrades(@PathVariable Long studentId) {
        try {
            List<WeeklyGrade> grades = weeklyGradeService.getStudentWeeklyGrades(studentId);
            return ResponseEntity.ok(grades);
        } catch (Exception e) {
            log.error("Erro ao buscar notas semanais do estudante", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/student/{studentId}/current")
    public ResponseEntity<WeeklyGrade> getCurrentWeekGrade(@PathVariable Long studentId) {
        try {
            Optional<WeeklyGrade> grade = weeklyGradeService.getCurrentWeekGrade(studentId);
            return grade.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Erro ao buscar nota da semana atual", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/student/{studentId}/latest")
    public ResponseEntity<WeeklyGrade> getLatestGrade(@PathVariable Long studentId) {
        try {
            Optional<WeeklyGrade> grade = weeklyGradeService.getLatestGrade(studentId);
            return grade.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Erro ao buscar última nota", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}




