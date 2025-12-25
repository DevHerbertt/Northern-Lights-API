package com.NorthrnLights.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExamDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer durationMinutes;
    private Integer totalScore;
    private Boolean isActive;
    private List<Long> questionIds; // IDs das questões que fazem parte da prova
}



