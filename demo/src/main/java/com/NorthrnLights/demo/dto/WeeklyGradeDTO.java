package com.NorthrnLights.demo.dto;

import com.NorthrnLights.demo.domain.Grade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyGradeDTO {
    private Long id;
    private Grade grade;
    private String feedback;
    private Long studentId;
    private Long teacherId;
    private LocalDate weekStartDate;
    private Double pointsObtained; // Pontos obtidos (opcional)
    private Double totalPoints; // Total de pontos (opcional)
}


