package com.NorthrnLights.demo.dto;

import com.NorthrnLights.demo.domain.LevelEnglish;
import com.NorthrnLights.demo.domain.Status;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class StudentProfileDTO {
    private String name;
    private String email;
    private LevelEnglish levelEnglish;
    private Status status;
    private LocalDateTime lastLogin;
}
