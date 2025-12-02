package com.NorthrnLights.demo.service;

import com.NorthrnLights.demo.domain.Question;
import com.NorthrnLights.demo.domain.Teacher;
import com.NorthrnLights.demo.dto.QuestionDTO;
import com.NorthrnLights.demo.repository.QuestionRepository;
import com.NorthrnLights.demo.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class QuestionService {


    private final QuestionRepository questionRepository;
    private final TeacherRepository teacherRepository;

    public Question create(QuestionDTO questionDTO) {
        if (questionDTO.getTitle().trim().isEmpty() || questionDTO.getDescription().trim().isEmpty()) {
            log.error("Title can't be void");
            throw  new ResponseStatusException(HttpStatus.NO_CONTENT,"Title can't be void");  // Lança exceção para indicar erro
        }

        Question question = new Question();
        question.setTitle(questionDTO.getTitle());
        question.setDescription(questionDTO.getDescription());
        question.setCreateAt(LocalDateTime.now());
        System.out.println(question.getCreateAt());

        MultipartFile file = questionDTO.getImageFile();

        if (file != null && !file.isEmpty()){
            try {
                String folder = System.getProperty("user.dir") + "/uploads/questions/";
                String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
                String uniqueFilename = System.currentTimeMillis() + "_" + originalFilename;

                Path uploadPath = Paths.get(folder);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(uniqueFilename);
                file.transferTo(filePath.toFile());

                question.setImagePath(folder + uniqueFilename);
            }catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save image", e);
            }
        }

        if (questionDTO.getTeacher() != null) {
            Long teacherId = questionDTO.getTeacher().getId();
            String username = questionDTO.getTeacher().getUserName();

            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Teacher not found with ID: " + teacherId));


            question.setTeacher(teacher);
        }

        return questionRepository.save(question);
    }
    public Question updateQuestion(QuestionDTO questionDTO){
        Question question = questionRepository.findById(questionDTO.getId())
                .orElseThrow(() ->  new ResponseStatusException(HttpStatus.NOT_FOUND,"Question not found"));

        if (questionDTO.getTitle().trim().isEmpty() || questionDTO.getDescription().trim().isEmpty()) {
            log.error("Title and Description can't be void");
            throw  new ResponseStatusException(HttpStatus.NO_CONTENT,"Title and Description can't be void");  // Lança exceção para indicar erro
        }

        question.setTitle(questionDTO.getTitle());
        question.setDescription(questionDTO.getDescription());
        question.setUpdateAt(LocalDateTime.now());

        MultipartFile file = questionDTO.getImageFile();
        if (file != null && !file.isEmpty()) {
            try {
                String folder = "uploads/questions/";
                String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
                String uniqueFilename = System.currentTimeMillis() + "_" + originalFilename;

                Path uploadPath = Paths.get(folder);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(uniqueFilename);
                file.transferTo(filePath.toFile());

                question.setImagePath(folder + uniqueFilename);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save image", e);
            }
        }

        return questionRepository.save(question);
    }

    public List<Question> findAll() {

        return questionRepository.findAll();
    }

    public Question findById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() ->  new ResponseStatusException(HttpStatus.NOT_FOUND,"Question not found"));
    }

    public List<Question> filterQuestions(String title, String description, LocalDateTime startDate, LocalDateTime endDate) {
        boolean hasFilters = (title != null && !title.isBlank()) || (description != null && !description.isBlank()) || (startDate != null && endDate != null);

        if (title != null && description != null) {
            return questionRepository.findByTitleContainingIgnoreCaseAndDescriptionContainingIgnoreCase(title, description);
        } else if (title != null) {
            return questionRepository.findByTitleContainingIgnoreCase(title);
        } else if (description != null) {
            return questionRepository.findByDescriptionContainingIgnoreCase(description);
        } else if (startDate != null && endDate != null) {
            return questionRepository.findByCreateAtBetween(startDate, endDate);
        } else if (!hasFilters) {
            return questionRepository.findAll();  // só retorna tudo se NÃO tiver filtro
        } else {
            return List.of(); // filtro(s) passados mas não tratado acima, retorna vazio
        }
    }


    public ResponseEntity<String> deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            log.warn("Question with ID {} not found for deletion", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Question not found");
        }

        questionRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
