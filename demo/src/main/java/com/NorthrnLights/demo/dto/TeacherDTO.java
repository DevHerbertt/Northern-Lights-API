package com.NorthrnLights.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeacherDTO {
 private Long id;
 private String userName;
 private String email;

 private LocalDateTime createAt;
 private LocalDateTime updateAt;

 private String passWord;

 private int age;
 private String ClassRoom;

}
