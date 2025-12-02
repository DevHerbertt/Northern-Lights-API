package com.NorthrnLights.demo.dto;

import com.NorthrnLights.demo.domain.LevelEnglish;
import com.NorthrnLights.demo.domain.User;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentRegisterDTO  {
    private String userName;
    private String email;
    private String password;
    private Integer age;
    private LevelEnglish levelEnglish;
    private String classRoom;


}
