package com.NorthrnLights.demo.service;

import com.NorthrnLights.demo.domain.Question;
import com.NorthrnLights.demo.domain.QuestionOption;
import com.NorthrnLights.demo.domain.Teacher;
import com.NorthrnLights.demo.dto.OptionDTO;
import com.NorthrnLights.demo.dto.QuestionBatchDTO;
import com.NorthrnLights.demo.dto.QuestionDTO;
import com.NorthrnLights.demo.repository.QuestionRepository;
import com.NorthrnLights.demo.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class QuestionService {


    private final QuestionRepository questionRepository;
    private final TeacherRepository teacherRepository;

    // Usar variável de ambiente UPLOAD_DIR ou fallback para diretório do projeto
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    // Método para obter o diretório base de uploads
    private String getImageUploadDir() {
        // Se uploadDir for relativo, usar baseado no diretório do projeto
        // Se for absoluto, usar diretamente
        String baseDir;
        if (new File(uploadDir).isAbsolute()) {
            baseDir = uploadDir;
        } else {
            baseDir = System.getProperty("user.dir") + File.separator + uploadDir;
        }
        // Garantir que termina com separador
        if (!baseDir.endsWith(File.separator)) {
            baseDir += File.separator;
        }
        return baseDir;
    }

    /**
     * Criar várias questões em lote via JSON (batch).
     * Aceita imagens como base64 String.
     * 
     * @param questionBatchDTOs Lista de DTOs para criação em lote
     * @return Lista de questões criadas
     * @throws IOException Se houver erro ao salvar imagens
     * @throws ResponseStatusException Se houver erro de validação
     */
    public List<Question> createQuestionsBatch(List<QuestionBatchDTO> questionBatchDTOs) throws IOException {
        if (questionBatchDTOs == null || questionBatchDTOs.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lista de questões não pode estar vazia");
        }

        List<Question> questions = new ArrayList<>();

        for (QuestionBatchDTO dto : questionBatchDTOs) {
            // Validar dados obrigatórios
            if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Título é obrigatório para todas as questões");
            }

            if (dto.getTeacherId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "ID do professor é obrigatório para todas as questões");
            }

            // Buscar professor
            Teacher teacher = teacherRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Professor não encontrado com ID: " + dto.getTeacherId()));

            // Validar múltipla escolha
            if (dto.getMultipleChoice() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Indicador de múltipla escolha é obrigatório");
            }

            // Validar opções se for múltipla escolha
            if (Boolean.TRUE.equals(dto.getMultipleChoice())) {
                if (dto.getOptions() == null || dto.getOptions().isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Questões de múltipla escolha devem ter pelo menos uma opção");
                }
                
                // Validar que pelo menos uma opção está correta
                boolean hasCorrectOption = dto.getOptions().stream()
                    .anyMatch(OptionDTO::isCorrect);
                if (!hasCorrectOption) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Questões de múltipla escolha devem ter pelo menos uma opção correta");
                }
            }

            Question question = new Question();
            question.setTitle(dto.getTitle().trim());
            question.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
            question.setPortugueseTranslation(dto.getPortugueseTranslation() != null ? dto.getPortugueseTranslation().trim() : null);
            question.setHasHelp(dto.getHasHelp() != null ? dto.getHasHelp() : (dto.getPortugueseTranslation() != null && !dto.getPortugueseTranslation().trim().isEmpty()));
            question.setMultipleChoice(dto.getMultipleChoice());
            question.setType(dto.getQuestionType());
            question.setTeacher(teacher);
            
            // Definir data de expiração: se fornecida no DTO, usar; caso contrário, verificar se há data definida para hoje
            if (dto.getExpiresAt() != null) {
                question.setExpiresAt(dto.getExpiresAt());
            } else {
                // Verificar se há questões criadas hoje pelo mesmo professor com data de expiração
                LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
                LocalDateTime todayEnd = todayStart.plusDays(1);
                List<Question> todayQuestions = questionRepository.findByTeacherAndCreatedAtBetween(teacher, todayStart, todayEnd);
                if (!todayQuestions.isEmpty()) {
                    // Pegar a data de expiração da primeira questão criada hoje (se houver)
                    LocalDateTime existingExpiresAt = todayQuestions.stream()
                        .filter(q -> q.getExpiresAt() != null)
                        .map(Question::getExpiresAt)
                        .findFirst()
                        .orElse(null);
                    if (existingExpiresAt != null) {
                        question.setExpiresAt(existingExpiresAt);
                        log.info("Usando data de expiração existente do dia: {}", existingExpiresAt);
                    }
                }
            }
            
            // Definir data de visibilidade
            if (dto.getVisibleAt() != null) {
                question.setVisibleAt(dto.getVisibleAt());
                log.info("✅ Questão '{}' terá data de visibilidade: {}", question.getTitle(), dto.getVisibleAt());
            } else {
                log.info("ℹ️ Questão '{}' não tem data de visibilidade definida (ficará visível imediatamente)", question.getTitle());
            }

            // Salvar imagem se houver (base64)
            if (dto.getImageBase64() != null && !dto.getImageBase64().trim().isEmpty()) {
                try {
                    String path = saveImageFromBase64(dto.getImageBase64());
                    question.setImagePath(path);
                } catch (Exception e) {
                    log.error("Erro ao salvar imagem base64 para questão: {}", dto.getTitle(), e);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Erro ao processar imagem: " + e.getMessage());
                }
            }

            // Se for múltipla escolha, criar opções
            if (Boolean.TRUE.equals(dto.getMultipleChoice()) && dto.getOptions() != null) {
                List<QuestionOption> options = new ArrayList<>();
                for (OptionDTO optDTO : dto.getOptions()) {
                    if (optDTO.getText() == null || optDTO.getText().trim().isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                            "Texto da opção não pode estar vazio");
                    }
                    
                    QuestionOption option = new QuestionOption();
                    option.setText(optDTO.getText().trim());
                    option.setCorrect(optDTO.isCorrect());
                    option.setQuestion(question);
                    options.add(option);
                }
                question.setOptions(options);
            }

            questions.add(question);
        }

        log.info("Criando {} questões em lote", questions.size());
        return questionRepository.saveAll(questions);
    }

    // Criar várias questões (método original para compatibilidade)
    public List<Question> createQuestions(List<QuestionDTO> questionDTOs) throws IOException {
        List<Question> questions = new ArrayList<>();

        for (QuestionDTO dto : questionDTOs) {
            Question question = new Question();
            question.setTitle(dto.getTitle());
            question.setDescription(dto.getDescription());
            question.setPortugueseTranslation(dto.getPortugueseTranslation());
            question.setHasHelp(dto.getHasHelp() != null ? dto.getHasHelp() : (dto.getPortugueseTranslation() != null && !dto.getPortugueseTranslation().trim().isEmpty()));
            question.setMultipleChoice(dto.isMultipleChoice());
            question.setType(dto.getQuestionType());
            question.setTeacher(dto.getTeacher());
            
            // Definir data de expiração: se fornecida no DTO, usar; caso contrário, verificar se há data definida para hoje
            if (dto.getExpiresAt() != null) {
                question.setExpiresAt(dto.getExpiresAt());
            } else {
                // Verificar se há questões criadas hoje pelo mesmo professor com data de expiração
                Teacher teacher = dto.getTeacher();
                if (teacher != null) {
                    LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
                    LocalDateTime todayEnd = todayStart.plusDays(1);
                    List<Question> todayQuestions = questionRepository.findByTeacherAndCreatedAtBetween(teacher, todayStart, todayEnd);
                    if (!todayQuestions.isEmpty()) {
                        // Pegar a data de expiração da primeira questão criada hoje (se houver)
                        LocalDateTime existingExpiresAt = todayQuestions.stream()
                            .filter(q -> q.getExpiresAt() != null)
                            .map(Question::getExpiresAt)
                            .findFirst()
                            .orElse(null);
                        if (existingExpiresAt != null) {
                            question.setExpiresAt(existingExpiresAt);
                        }
                    }
                }
            }

            // Definir data de visibilidade
            if (dto.getVisibleAt() != null) {
                question.setVisibleAt(dto.getVisibleAt());
                log.info("✅ Questão '{}' terá data de visibilidade: {}", question.getTitle(), dto.getVisibleAt());
            } else {
                log.info("ℹ️ Questão '{}' não tem data de visibilidade definida (ficará visível imediatamente)", question.getTitle());
                // Se não fornecida, verificar se há data de visibilidade definida para questões do mesmo dia
                Teacher teacher = dto.getTeacher();
                if (teacher != null) {
                    LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
                    LocalDateTime todayEnd = todayStart.plusDays(1);
                    List<Question> todayQuestions = questionRepository.findByTeacherAndCreatedAtBetween(teacher, todayStart, todayEnd);
                    if (!todayQuestions.isEmpty()) {
                        LocalDateTime existingVisibleAt = todayQuestions.stream()
                            .filter(q -> q.getVisibleAt() != null)
                            .map(Question::getVisibleAt)
                            .findFirst()
                            .orElse(null);
                        if (existingVisibleAt != null) {
                            question.setVisibleAt(existingVisibleAt);
                            log.info("✅ Usando data de visibilidade existente do dia: {}", existingVisibleAt);
                        }
                    }
                }
            }

            // Salvar imagem se houver
            if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
                String path = saveImage(dto.getImageFile());
                question.setImagePath(path);
            }

            // Se for múltipla escolha, criar opções
            if (dto.isMultipleChoice() && dto.getOptions() != null) {
                List<QuestionOption> options = new ArrayList<>();
                for (OptionDTO optDTO : dto.getOptions()) {
                    QuestionOption option = new QuestionOption();
                    option.setText(optDTO.getText());
                    option.setCorrect(optDTO.isCorrect());
                    option.setQuestion(question);
                    options.add(option);
                }
                question.setOptions(options);
            }

            questions.add(question);
        }

        return questionRepository.saveAll(questions);
    }

    // Atualizar uma questão existente
    public Question updateQuestion(Long id, QuestionDTO dto) throws IOException {
        Question question = questionRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Questão não encontrada com ID: " + id));

        question.setTitle(dto.getTitle());
        question.setDescription(dto.getDescription());
        question.setPortugueseTranslation(dto.getPortugueseTranslation());
        question.setHasHelp(dto.getHasHelp() != null ? dto.getHasHelp() : (dto.getPortugueseTranslation() != null && !dto.getPortugueseTranslation().trim().isEmpty()));
        question.setMultipleChoice(dto.isMultipleChoice());
        question.setType(dto.getQuestionType());
        question.setTeacher(dto.getTeacher());
        
        // Atualizar datas de expiração e visibilidade
        if (dto.getExpiresAt() != null) {
            question.setExpiresAt(dto.getExpiresAt());
            log.info("✅ Atualizando data de expiração da questão {} para: {}", id, dto.getExpiresAt());
        }
        if (dto.getVisibleAt() != null) {
            question.setVisibleAt(dto.getVisibleAt());
            log.info("✅ Atualizando data de visibilidade da questão {} para: {}", id, dto.getVisibleAt());
        }

        // Atualizar imagem se houver
        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            String path = saveImage(dto.getImageFile());
            question.setImagePath(path);
        }

        // Atualizar opções para múltipla escolha
        if (dto.isMultipleChoice()) {
            question.getOptions().clear();
            if (dto.getOptions() != null) {
                List<QuestionOption> options = new ArrayList<>();
                for (OptionDTO optDTO : dto.getOptions()) {
                    QuestionOption option = new QuestionOption();
                    option.setText(optDTO.getText());
                    option.setCorrect(optDTO.isCorrect());
                    option.setQuestion(question);
                    options.add(option);
                }
                question.setOptions(options);
            }
        } else {
            // Se não for múltipla escolha, limpar opções
            question.getOptions().clear();
        }

        return questionRepository.save(question);
    }

    /**
     * Função auxiliar para salvar imagem no servidor a partir de MultipartFile.
     */
    private String saveImage(MultipartFile imageFile) throws IOException {
        log.info("🔍 DEBUG saveImage: Iniciando salvamento de imagem");
        log.info("🔍 DEBUG saveImage: Nome original: {}", imageFile.getOriginalFilename());
        log.info("🔍 DEBUG saveImage: Tamanho: {} bytes", imageFile.getSize());
        
        // Salvar em subdiretório específico para questões
        String subDir = "questions" + File.separator;
        String originalFilename = imageFile.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            originalFilename = "image.png";
        }
        String filename = System.currentTimeMillis() + "_" + originalFilename;
        
        // Criar diretório completo se não existir
        File uploadDirectory = new File(getImageUploadDir() + subDir);
        log.info("🔍 DEBUG saveImage: Diretório de upload: {}", uploadDirectory.getAbsolutePath());
        log.info("🔍 DEBUG saveImage: Diretório existe? {}", uploadDirectory.exists());
        
        if (!uploadDirectory.exists()) {
            boolean created = uploadDirectory.mkdirs();
            log.info("🔍 DEBUG saveImage: Tentativa de criar diretório: {}", created);
            if (!created && !uploadDirectory.exists()) {
                log.error("❌ Erro ao criar diretório: {}", uploadDirectory.getAbsolutePath());
                throw new IOException("Não foi possível criar o diretório: " + uploadDirectory.getAbsolutePath());
            }
            log.info("✅ Diretório criado: {}", uploadDirectory.getAbsolutePath());
        }
        
        File dest = new File(uploadDirectory, filename);
        
            // Verificar se o arquivo já existe e adicionar sufixo se necessário
            int counter = 1;
            String baseFilename = filename;
            while (dest.exists()) {
                int lastDot = baseFilename.lastIndexOf('.');
                if (lastDot > 0) {
                    String nameWithoutExt = baseFilename.substring(0, lastDot);
                    String ext = baseFilename.substring(lastDot);
                    filename = nameWithoutExt + "_" + counter + ext;
                } else {
                    filename = baseFilename + "_" + counter;
                }
                dest = new File(uploadDirectory, filename);
                counter++;
            }
        
        log.info("🔍 DEBUG saveImage: Salvando arquivo em: {}", dest.getAbsolutePath());
        imageFile.transferTo(dest);
        log.info("✅ Imagem salva com sucesso: {}", dest.getAbsolutePath());
        log.info("🔍 DEBUG saveImage: Arquivo existe após salvar? {}", dest.exists());
        log.info("🔍 DEBUG saveImage: Tamanho do arquivo salvo: {} bytes", dest.length());
        
        // Retornar caminho relativo para servir via FileController
        String relativePath = "/uploads/questions/" + filename;
        log.info("🔍 DEBUG saveImage: Caminho relativo retornado: {}", relativePath);
        return relativePath;
    }

    /**
     * Função auxiliar para salvar imagem no servidor a partir de base64.
     * Aceita base64 com ou sem prefixo data:image/[tipo];base64,
     * 
     * @param base64String String base64 da imagem (pode incluir prefixo data:image)
     * @return Caminho relativo da imagem salva
     * @throws IOException Se houver erro ao salvar o arquivo
     */
    private String saveImageFromBase64(String base64String) throws IOException {
        try {
            // Remover prefixo data:image/[tipo];base64, se presente
            String base64Data = base64String;
            String fileExtension = "png"; // padrão
            
            if (base64String.contains(",")) {
                String[] parts = base64String.split(",");
                if (parts.length == 2) {
                    String prefix = parts[0];
                    base64Data = parts[1];
                    
                    // Extrair extensão do tipo MIME
                    if (prefix.contains("image/")) {
                        String mimeType = prefix.substring(prefix.indexOf("image/") + 6);
                        if (mimeType.contains(";")) {
                            mimeType = mimeType.substring(0, mimeType.indexOf(";"));
                        }
                        fileExtension = mimeType.equals("jpeg") ? "jpg" : mimeType;
                    }
                }
            }

            // Decodificar base64
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            // Validar tamanho (máximo 12MB)
            if (imageBytes.length > 12 * 1024 * 1024) {
                throw new IOException("Imagem muito grande. Tamanho máximo: 12MB");
            }

            // Criar nome do arquivo
            String subDir = "questions" + File.separator;
            String filename = System.currentTimeMillis() + "_" + System.nanoTime() + "." + fileExtension;
            
            // Criar diretório completo se não existir
            File uploadDirectory = new File(getImageUploadDir() + subDir);
            if (!uploadDirectory.exists()) {
                boolean created = uploadDirectory.mkdirs();
                if (!created && !uploadDirectory.exists()) {
                    throw new IOException("Não foi possível criar o diretório: " + uploadDirectory.getAbsolutePath());
                }
                log.info("✅ Diretório criado: {}", uploadDirectory.getAbsolutePath());
            }
            
            File dest = new File(uploadDirectory, filename);
            
            // Verificar se o arquivo já existe e adicionar sufixo se necessário
            int counter = 1;
            String originalFilename = filename;
            while (dest.exists()) {
                String nameWithoutExt = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
                String ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
                filename = nameWithoutExt + "_" + counter + ext;
                dest = new File(uploadDirectory, filename);
                counter++;
            }

            // Salvar arquivo
            Files.write(dest.toPath(), imageBytes);

            log.info("✅ Imagem salva com sucesso: {}", dest.getAbsolutePath());
            // Retornar caminho relativo para servir via FileController
            return "/uploads/questions/" + filename;
        } catch (IllegalArgumentException e) {
            throw new IOException("String base64 inválida: " + e.getMessage(), e);
        }
    }

    public List<Question> findAll() {
        // Usar método que carrega opções junto
        List<Question> questions = questionRepository.findAllWithOptions();
        
        // Validar e limpar imagePath se o arquivo não existir
        for (Question q : questions) {
            if (q.getImagePath() != null && !q.getImagePath().trim().isEmpty()) {
                java.io.File imageFile = new java.io.File(q.getImagePath());
                if (!imageFile.exists()) {
                    log.warn("Imagem não encontrada para questão {}: {}", q.getId(), q.getImagePath());
                    q.setImagePath(null); // Limpar imagePath se arquivo não existir
                }
            }
        }
        
        // Filtrar questões que ainda não estão visíveis (apenas para estudantes)
        LocalDateTime now = LocalDateTime.now();
        List<Question> visibleQuestions = questions.stream()
            .filter(q -> {
                if (q.getVisibleAt() == null) {
                    return true; // Sem data de visibilidade = visível imediatamente
                }
                boolean isVisible = q.getVisibleAt().isBefore(now) || q.getVisibleAt().isEqual(now);
                if (!isVisible) {
                    log.debug("⏳ Questão {} ainda não está visível. VisibleAt: {}, Agora: {}", 
                            q.getId(), q.getVisibleAt(), now);
                }
                return isVisible;
            })
            .collect(java.util.stream.Collectors.toList());
        
        log.info("📊 Total de questões: {}, Questões visíveis: {}", questions.size(), visibleQuestions.size());
        return visibleQuestions;
    }

    public Question findById(Long id) {
        // Usar método que carrega opções junto
        Question question = questionRepository.findByIdWithOptions(id)
                .orElseThrow(() ->  new ResponseStatusException(HttpStatus.NOT_FOUND,"Question not found"));
        
        // Verificar se a questão está visível (apenas para estudantes, professores veem todas)
        // Nota: Esta verificação deve ser feita no controller baseado no role do usuário
        return question;
    }
    
    /**
     * Busca questões visíveis para estudantes (filtra por visibleAt)
     */
    public List<Question> findVisibleQuestions() {
        LocalDateTime now = LocalDateTime.now();
        List<Question> allQuestions = questionRepository.findAllWithOptions();
        return allQuestions.stream()
            .filter(q -> q.getVisibleAt() == null || q.getVisibleAt().isBefore(now) || q.getVisibleAt().isEqual(now))
            .collect(java.util.stream.Collectors.toList());
    }

    public List<Question> filterQuestions(String title, String description, LocalDateTime startDate, LocalDateTime endDate) {
        boolean hasFilters = (title != null && !title.isBlank()) || (description != null && !description.isBlank()) || (startDate != null && endDate != null);

        List<Question> questions;
        if (title != null && description != null) {
            questions = questionRepository.findByTitleContainingIgnoreCaseAndDescriptionContainingIgnoreCase(title, description);
        } else if (title != null) {
            questions = questionRepository.findByTitleContainingIgnoreCase(title);
        } else if (description != null) {
            questions = questionRepository.findByDescriptionContainingIgnoreCase(description);
        } else if (startDate != null && endDate != null) {
            questions = questionRepository.findByCreatedAtBetween(startDate, endDate);
        } else if (!hasFilters) {
            questions = questionRepository.findAllWithOptions();  // usar método que carrega opções
        } else {
            questions = List.of(); // filtro(s) passados mas não tratado acima, retorna vazio
        }
        
        // Para questões filtradas, carregar opções se necessário
        for (Question q : questions) {
            if (q.getOptions() == null || q.getOptions().isEmpty()) {
                // Recarregar questão com opções
                questionRepository.findByIdWithOptions(q.getId()).ifPresent(questionWithOptions -> {
                    q.setOptions(questionWithOptions.getOptions());
                });
            }
        }
        
        // Validar e limpar imagePath se o arquivo não existir
        for (Question q : questions) {
            if (q.getImagePath() != null && !q.getImagePath().trim().isEmpty()) {
                java.io.File imageFile = new java.io.File(q.getImagePath());
                if (!imageFile.exists()) {
                    log.warn("Imagem não encontrada para questão {}: {}", q.getId(), q.getImagePath());
                    q.setImagePath(null); // Limpar imagePath se arquivo não existir
                }
            }
        }
        
        // Filtrar questões que ainda não estão visíveis (apenas para estudantes)
        LocalDateTime now = LocalDateTime.now();
        return questions.stream()
            .filter(q -> q.getVisibleAt() == null || q.getVisibleAt().isBefore(now) || q.getVisibleAt().isEqual(now))
            .collect(java.util.stream.Collectors.toList());
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
