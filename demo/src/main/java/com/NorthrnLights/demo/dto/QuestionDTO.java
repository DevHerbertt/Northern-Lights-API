package com.NorthrnLights.demo.dto;

import com.NorthrnLights.demo.domain.Teacher;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
public class QuestionDTO {
    private Long id;
    private String title;
    private String description;

    // Para envio de imagem
    private MultipartFile imageFile;

    // Para retorno da imagem salva
    private String imagePath;

    private Teacher teacher;
    

}