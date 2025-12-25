package com.NorthrnLights.demo.service;

import com.NorthrnLights.demo.domain.*;
import com.NorthrnLights.demo.repository.*;
import com.NorthrnLights.demo.util.GradeCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvExportService {

    private final ExamGradeRepository examGradeRepository;
    private final WeeklyGradeRepository weeklyGradeRepository;
    private final StudentRepository studentRepository;
    private final ExamRepository examRepository;

    public byte[] exportExamGradesToCsv(Long examId, Authentication authentication) throws IOException {
        List<ExamGrade> grades = examGradeRepository.findByExamId(examId);
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Prova não encontrada"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            // Header
            writer.println("Nome do Aluno,Nota Total,Classificação");

            // Data rows
            for (ExamGrade grade : grades) {
                String studentName = grade.getStudent().getUserName() != null ? 
                        grade.getStudent().getUserName() : 
                        grade.getStudent().getEmail();
                String gradeDisplay = GradeCalculator.formatGradeWithClassification(
                        grade.getPointsObtained(),
                        grade.getTotalPoints(),
                        grade.getGrade()
                );
                
                writer.printf("%s,%s,%s%n",
                        escapeCsvField(studentName),
                        gradeDisplay,
                        grade.getGrade() != null ? grade.getGrade().toString() : "N/A"
                );
            }
        }

        return baos.toByteArray();
    }

    public byte[] exportWeeklyGradesToCsv(LocalDate weekStartDate, Authentication authentication) throws IOException {
        List<WeeklyGrade> grades = weeklyGradeRepository.findAll();
        
        // Filtrar por semana se fornecido
        if (weekStartDate != null) {
            grades = grades.stream()
                    .filter(g -> g.getWeekStartDate().equals(weekStartDate))
                    .toList();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            // Header
            writer.println("Nome do Aluno,Nota Total,Classificação,Semana");

            // Data rows
            for (WeeklyGrade grade : grades) {
                String studentName = grade.getStudent().getUserName() != null ? 
                        grade.getStudent().getUserName() : 
                        grade.getStudent().getEmail();
                
                String gradeDisplay;
                if (grade.getPointsObtained() != null && grade.getTotalPoints() != null) {
                    gradeDisplay = GradeCalculator.formatGradeWithClassification(
                            grade.getPointsObtained(),
                            grade.getTotalPoints(),
                            grade.getGrade()
                    );
                } else {
                    gradeDisplay = grade.getGrade() != null ? grade.getGrade().toString() : "N/A";
                }
                
                String weekInfo = grade.getWeekStartDate() != null ? 
                        grade.getWeekStartDate().toString() : "N/A";
                
                writer.printf("%s,%s,%s,%s%n",
                        escapeCsvField(studentName),
                        gradeDisplay,
                        grade.getGrade() != null ? grade.getGrade().toString() : "N/A",
                        weekInfo
                );
            }
        }

        return baos.toByteArray();
    }

    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        // Se contém vírgula, aspas ou quebra de linha, precisa ser envolvido em aspas
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            // Escapar aspas duplicando-as
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}

