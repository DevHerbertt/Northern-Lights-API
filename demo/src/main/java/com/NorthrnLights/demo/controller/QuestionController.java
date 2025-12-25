package com.NorthrnLights.demo.controller;

import com.NorthrnLights.demo.domain.Question;
import com.NorthrnLights.demo.dto.QuestionBatchDTO;
import com.NorthrnLights.demo.dto.QuestionDTO;
import com.NorthrnLights.demo.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
@Log4j2
public class QuestionController {

    private final QuestionService questionService;

    /**
     * Criar uma questão individual via FormData.
     * Aceita imagem como MultipartFile.
     */
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createQuestion(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "portugueseTranslation", required = false) String portugueseTranslation,
            @RequestParam(value = "hasHelp", required = false) String hasHelpStr,
            @RequestParam("questionType") String questionType,
            @RequestParam("multipleChoice") String multipleChoice,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "options", required = false) String optionsJson,
            @RequestParam(value = "expiresAt", required = false) String expiresAtStr,
            @RequestParam(value = "visibleAt", required = false) String visibleAtStr,
            Authentication authentication) {
        
        try {
            QuestionDTO dto = new QuestionDTO();
            dto.setTitle(title);
            dto.setDescription(description);
            dto.setPortugueseTranslation(portugueseTranslation);
            
            // Converter hasHelp de String para Boolean
            Boolean hasHelp = null;
            if (hasHelpStr != null && !hasHelpStr.trim().isEmpty()) {
                hasHelp = Boolean.parseBoolean(hasHelpStr);
            }
            dto.setHasHelp(hasHelp != null ? hasHelp : (portugueseTranslation != null && !portugueseTranslation.trim().isEmpty()));
            dto.setQuestionType(com.NorthrnLights.demo.domain.QuestionType.valueOf(questionType));
            dto.setMultipleChoice(Boolean.parseBoolean(multipleChoice));
            dto.setImageFile(imageFile);
            
            // Processar data de expiração
            if (expiresAtStr != null && !expiresAtStr.trim().isEmpty()) {
                try {
                    log.info("🔍 DEBUG: Recebendo expiresAtStr: '{}'", expiresAtStr);
                    // Converter de ISO string (com timezone) para LocalDateTime
                    java.time.Instant instant = java.time.Instant.parse(expiresAtStr);
                    java.time.LocalDateTime expiresAt = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault());
                    dto.setExpiresAt(expiresAt);
                    log.info("✅ DEBUG: Data de expiração parseada com sucesso: {}", expiresAt);
                } catch (Exception e) {
                    log.error("❌ Erro ao parsear data de expiração '{}': {}", expiresAtStr, e.getMessage(), e);
                }
            } else {
                log.info("ℹ️ DEBUG: expiresAtStr está vazio ou null");
            }
            
            // Processar data de visibilidade
            if (visibleAtStr != null && !visibleAtStr.trim().isEmpty()) {
                try {
                    log.info("🔍 DEBUG: Recebendo visibleAtStr: '{}'", visibleAtStr);
                    // Converter de ISO string (com timezone) para LocalDateTime
                    java.time.Instant instant = java.time.Instant.parse(visibleAtStr);
                    java.time.LocalDateTime visibleAt = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault());
                    dto.setVisibleAt(visibleAt);
                    log.info("✅ DEBUG: Data de visibilidade parseada com sucesso: {}", visibleAt);
                } catch (Exception e) {
                    log.error("❌ Erro ao parsear data de visibilidade '{}': {}", visibleAtStr, e.getMessage(), e);
                }
            } else {
                log.info("ℹ️ DEBUG: visibleAtStr está vazio ou null");
            }
            
            // Parse options if provided
            if (optionsJson != null && !optionsJson.trim().isEmpty()) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    List<com.NorthrnLights.demo.dto.OptionDTO> options = mapper.readValue(
                        optionsJson, 
                        new TypeReference<List<com.NorthrnLights.demo.dto.OptionDTO>>() {}
                    );
                    dto.setOptions(options);
                } catch (Exception e) {
                    log.warn("Erro ao parsear opções JSON: {}", e.getMessage());
                }
            }
            
            // Get teacher from authentication
            if (authentication == null || authentication.getPrincipal() == null) {
                log.error("Autenticação não encontrada");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuário não autenticado");
            }
            
            Object principal = authentication.getPrincipal();
            log.info("Principal type: {}", principal.getClass().getName());
            
            com.NorthrnLights.demo.domain.Teacher teacher;
            if (principal instanceof com.NorthrnLights.demo.domain.Teacher) {
                teacher = (com.NorthrnLights.demo.domain.Teacher) principal;
            } else {
                log.error("Principal não é um Teacher. Tipo: {}", principal.getClass().getName());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Apenas professores podem criar questões");
            }
            
            dto.setTeacher(teacher);
            
            List<QuestionDTO> dtos = List.of(dto);
            List<Question> savedQuestions = questionService.createQuestions(dtos);
            
            if (savedQuestions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao criar questão");
            }
            
            log.info("Questão criada com sucesso: {}", savedQuestions.get(0).getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedQuestions.get(0));
            
        } catch (Exception e) {
            log.error("Erro ao criar questão: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao criar questão: " + e.getMessage());
        }
    }

    /**
     * Criar várias questões em lote via JSON.
     * Aceita imagens como base64 String no campo imageBase64.
     * 
     * @param questionsBatchDTOs Lista de DTOs para criação em lote
     * @return Lista de questões criadas
     */
    @PostMapping(value = "/batch", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createQuestionsBatch(
            @Valid @RequestBody List<QuestionBatchDTO> questionsBatchDTOs) {

        try {
            log.info("Recebida requisição para criar {} questões em lote", questionsBatchDTOs.size());
            
            if (questionsBatchDTOs == null || questionsBatchDTOs.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lista de questões não pode estar vazia");
            }

            List<Question> savedQuestions = questionService.createQuestionsBatch(questionsBatchDTOs);
            
            log.info("{} questões criadas com sucesso", savedQuestions.size());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedQuestions);
            
        } catch (ResponseStatusException e) {
            log.error("Erro de validação ao criar questões em lote: {}", e.getReason(), e);
            return ResponseEntity.status(e.getStatusCode())
                .body("Erro: " + e.getReason());
                
        } catch (IOException e) {
            log.error("Erro ao salvar imagens das questões", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao processar imagens: " + e.getMessage());
                
        } catch (Exception e) {
            log.error("Erro inesperado ao criar questões em lote", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro interno do servidor: " + e.getMessage());
        }
    }


    // Atualizar questão existente
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateQuestion(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "portugueseTranslation", required = false) String portugueseTranslation,
            @RequestParam(value = "hasHelp", required = false) Boolean hasHelp,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "expiresAt", required = false) String expiresAtStr,
            @RequestParam(value = "visibleAt", required = false) String visibleAtStr,
            Authentication authentication) {

        try {
            QuestionDTO dto = new QuestionDTO();
            dto.setTitle(title);
            dto.setDescription(description);
            dto.setPortugueseTranslation(portugueseTranslation);
            dto.setHasHelp(hasHelp != null ? hasHelp : (portugueseTranslation != null && !portugueseTranslation.trim().isEmpty()));
            dto.setImageFile(imageFile);
            
            // Processar data de expiração
            if (expiresAtStr != null && !expiresAtStr.trim().isEmpty()) {
                try {
                    log.info("🔍 DEBUG: Recebendo expiresAtStr para atualização: '{}'", expiresAtStr);
                    java.time.Instant instant = java.time.Instant.parse(expiresAtStr);
                    java.time.LocalDateTime expiresAt = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault());
                    dto.setExpiresAt(expiresAt);
                    log.info("✅ DEBUG: Data de expiração parseada com sucesso: {}", expiresAt);
                } catch (Exception e) {
                    log.error("❌ Erro ao parsear data de expiração '{}': {}", expiresAtStr, e.getMessage(), e);
                }
            }
            
            // Processar data de visibilidade
            if (visibleAtStr != null && !visibleAtStr.trim().isEmpty()) {
                try {
                    log.info("🔍 DEBUG: Recebendo visibleAtStr para atualização: '{}'", visibleAtStr);
                    java.time.Instant instant = java.time.Instant.parse(visibleAtStr);
                    java.time.LocalDateTime visibleAt = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault());
                    dto.setVisibleAt(visibleAt);
                    log.info("✅ DEBUG: Data de visibilidade parseada com sucesso: {}", visibleAt);
                } catch (Exception e) {
                    log.error("❌ Erro ao parsear data de visibilidade '{}': {}", visibleAtStr, e.getMessage(), e);
                }
            }
            
            // Get teacher from authentication
            com.NorthrnLights.demo.domain.Teacher teacher = (com.NorthrnLights.demo.domain.Teacher) authentication.getPrincipal();
            dto.setTeacher(teacher);
            
            // Preserve existing question type and multiple choice
            Question existingQuestion = questionService.findById(id);
            dto.setQuestionType(existingQuestion.getType());
            dto.setMultipleChoice(existingQuestion.isMultipleChoice());
            
            Question updatedQuestion = questionService.updateQuestion(id, dto);
            log.info("Questão {} atualizada com sucesso", id);
            return ResponseEntity.ok(updatedQuestion);
        } catch (ResponseStatusException e) {
            log.error("Erro ao atualizar questão {}: {}", id, e.getReason(), e);
            return ResponseEntity.status(e.getStatusCode())
                .body("Erro: " + e.getReason());
        } catch (IOException e) {
            log.error("Erro ao atualizar imagem da questão {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao processar imagem: " + e.getMessage());
        } catch (Exception e) {
            log.error("Erro inesperado ao atualizar questão {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro interno do servidor: " + e.getMessage());
        }
    }



    @GetMapping("/quantity")
    public ResponseEntity<Long> getTotalQuestions() {
        return ResponseEntity.ok((long) questionService.findAll().size());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Question> getQuestionById(@PathVariable Long id, Authentication authentication) {
        Question question = questionService.findById(id);
        
        // Se o usuário for estudante, verificar se a questão está visível
        if (authentication != null && authentication.getPrincipal() instanceof com.NorthrnLights.demo.domain.Student) {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            if (question.getVisibleAt() != null && question.getVisibleAt().isAfter(now)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Esta questão ainda não está disponível. Ela ficará visível em: " + 
                    question.getVisibleAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            }
        }
        // Professores podem ver todas as questões
        
        return ResponseEntity.ok(question);
    }

    @GetMapping
    public ResponseEntity<List<Question>> getQuestions(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Authentication authentication
    ) {
        List<Question> questions = questionService.filterQuestions(title, description, startDate, endDate);
        
        // Se o usuário for estudante, filtrar questões não visíveis
        if (authentication != null && authentication.getPrincipal() instanceof com.NorthrnLights.demo.domain.Student) {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            questions = questions.stream()
                .filter(q -> q.getVisibleAt() == null || q.getVisibleAt().isBefore(now) || q.getVisibleAt().isEqual(now))
                .collect(java.util.stream.Collectors.toList());
        }
        // Professores veem todas as questões (sem filtro de visibilidade)
        
        return ResponseEntity.ok(questions);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteQuestion(@PathVariable Long id) {
        return questionService.deleteQuestion(id);
    }
}
