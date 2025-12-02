package com.NorthrnLights.demo.dto;

import com.NorthrnLights.demo.domain.Grade;
import lombok.Data;

@Data
public class CorrectionDTO {
    private Grade grade;
    private String feedback;
    private Long answerId;
}
