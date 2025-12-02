package com.NorthrnLights.demo.dto;


import com.NorthrnLights.demo.domain.Student;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
public class AnswerDTO {
    private Long id;
    private String text;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private Student student;
    private Long questionId;
    private MultipartFile imageFile;

}