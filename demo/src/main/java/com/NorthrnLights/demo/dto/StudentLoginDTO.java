package com.NorthrnLights.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class StudentLoginDTO {
    private String email;
    private String password;
}
