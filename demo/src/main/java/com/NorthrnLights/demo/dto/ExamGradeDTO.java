package com.NorthrnLights.demo.dto;

import com.NorthrnLights.demo.domain.Grade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamGradeDTO {
    private Long id;
    private Double pointsObtained;
    private Double totalPoints;
    private Grade grade;
    private String feedback;
    private Long studentId;
    private Long examId;
    private Long teacherId;
}






