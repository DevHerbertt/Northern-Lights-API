package com.NorthrnLights.demo.dto;

import com.NorthrnLights.demo.domain.LevelEnglish;
import jakarta.persistence.Column;
import lombok.Data;

@Data
public class AuthRegister {
    private String email;
    private String password;
    private String userName;

    @Column(nullable = false)
    private Integer age;
    private LevelEnglish levelEnglish;
}
