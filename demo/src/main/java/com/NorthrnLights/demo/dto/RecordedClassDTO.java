package com.NorthrnLights.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordedClassDTO {
    private Long id;
    private String title;
    private String description;
    private String videoUrl;
    private LocalDate classDate;
    private Long teacherId;
}






