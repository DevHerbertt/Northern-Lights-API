package com.NorthrnLights.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class TeacherDTO {
 private Long id;
 private String userName;
 private String email;


 private String passWord;

 private int age;
 private String ClassRoom;

}
