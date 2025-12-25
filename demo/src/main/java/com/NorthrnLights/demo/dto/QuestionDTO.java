package com.NorthrnLights.demo.dto;

import com.NorthrnLights.demo.domain.QuestionType;
import com.NorthrnLights.demo.domain.Teacher;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuestionDTO {
    private Long id;
    private String title;
    private String description;
    private String portugueseTranslation; // Tradução/ajuda em português
    private Boolean hasHelp; // Indica se tem ajuda disponível
    private boolean multipleChoice;
    private MultipartFile imageFile;
    private String imagePath;
    private Teacher teacher;
    private List<OptionDTO> options;
    private QuestionType questionType;
    private Long examId; // ID da prova à qual a questão pertence (null se não for de prova)
    private LocalDateTime expiresAt; // Data de expiração para correção automática
    private LocalDateTime visibleAt; // Data em que a questão ficará visível para os alunos

    // Construtor padrão
    public QuestionDTO() {}
}