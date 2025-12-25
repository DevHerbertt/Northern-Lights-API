package com.NorthrnLights.demo.service;

import com.NorthrnLights.demo.domain.*;
import com.NorthrnLights.demo.dto.ExamGradeDTO;
import com.NorthrnLights.demo.dto.WeeklyGradeDTO;
import com.NorthrnLights.demo.repository.*;
import com.NorthrnLights.demo.util.CsvGradeParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvImportService {

    private final StudentRepository studentRepository;
    private final ExamRepository examRepository;
    private final ExamGradeService examGradeService;
    private final WeeklyGradeService weeklyGradeService;

    /**
     * Parse CSV e retorna prévia sem salvar
     */
    public PreviewResult previewCsvGrades(MultipartFile file, String gradeType, Long examId) {
        log.info("Gerando prévia do CSV. Tipo: {}, ExamId: {}", gradeType, examId);

        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arquivo CSV vazio");
        }

        try (InputStream inputStream = file.getInputStream()) {
            List<CsvGradeParser.ParsedGrade> parsedGrades = CsvGradeParser.parseCsv(inputStream, gradeType);
            
            List<PreviewItem> previewItems = new java.util.ArrayList<>();
            List<String> errors = new java.util.ArrayList<>();

            for (CsvGradeParser.ParsedGrade parsed : parsedGrades) {
                try {
                    // Tentar encontrar estudante por email ou nome
                    Student student = findStudent(parsed.getEmail(), parsed.getFullName());
                    
                    PreviewItem item = new PreviewItem();
                    item.setEmail(parsed.getEmail());
                    item.setFullName(parsed.getFullName());
                    item.setScore(parsed.getScore());
                    item.setPointsObtained(parsed.getPointsObtained());
                    item.setTotalPoints(parsed.getTotalPoints());
                    item.setGrade(parsed.getGrade());
                    item.setFeedback(parsed.getFeedback());
                    
                    if (student != null) {
                        item.setStudentId(student.getId());
                        item.setStudentName(student.getUserName() != null ? student.getUserName() : student.getEmail());
                        item.setFound(true);
                        log.info("Estudante encontrado: {} -> {}", parsed.getFullName(), item.getStudentName());
                    } else {
                        item.setFound(false);
                        String errorMsg = parsed.getEmail() != null && !parsed.getEmail().isEmpty() 
                            ? String.format("Estudante não encontrado com email: %s ou nome: %s", parsed.getEmail(), parsed.getFullName())
                            : String.format("Estudante não encontrado com nome: %s", parsed.getFullName());
                        item.setErrorMessage(errorMsg);
                        log.warn("Estudante não encontrado: email={}, nome={}", parsed.getEmail(), parsed.getFullName());
                    }
                    
                    previewItems.add(item);
                    
                } catch (Exception e) {
                    PreviewItem item = new PreviewItem();
                    item.setEmail(parsed.getEmail());
                    item.setFullName(parsed.getFullName());
                    item.setScore(parsed.getScore());
                    item.setFound(false);
                    item.setErrorMessage("Erro ao processar: " + e.getMessage());
                    previewItems.add(item);
                    errors.add(String.format("Erro ao processar %s (%s): %s", 
                            parsed.getFullName(), parsed.getEmail(), e.getMessage()));
                }
            }

            return new PreviewResult(previewItems, errors);

        } catch (IOException e) {
            log.error("Erro ao ler arquivo CSV", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao processar arquivo CSV");
        }
    }

    /**
     * Importa e salva as notas do CSV
     */
    @Transactional
    public ImportResult importGradesFromCsv(List<PreviewItem> items, String gradeType, Long examId, 
                                           boolean sendEmail, boolean sendToDashboard, Authentication authentication) {
        log.info("Importando notas do CSV. Tipo: {}, ExamId: {}, SendEmail: {}, SendToDashboard: {}", 
                gradeType, examId, sendEmail, sendToDashboard);

        int successCount = 0;
        int errorCount = 0;
        List<String> errors = new java.util.ArrayList<>();

        for (PreviewItem item : items) {
            if (!item.isFound() || item.getStudentId() == null) {
                errorCount++;
                errors.add(String.format("Estudante não encontrado: %s (%s)", item.getFullName(), item.getEmail()));
                continue;
            }

            try {
                if ("prova".equalsIgnoreCase(gradeType) || "exam".equalsIgnoreCase(gradeType)) {
                    // Nota de prova é geral, não precisa de examId
                    importExamGrade(item, null, authentication, sendEmail);
                } else {
                    importWeeklyGrade(item, authentication, sendEmail);
                }
                successCount++;
            } catch (Exception e) {
                errorCount++;
                String errorMsg = String.format("Erro ao importar nota para %s (%s): %s", 
                        item.getFullName(), item.getEmail(), e.getMessage());
                errors.add(errorMsg);
                log.error(errorMsg, e);
            }
        }

        return new ImportResult(successCount, errorCount, errors);
    }

    private Student findStudent(String email, String fullName) {
        // Primeiro tentar por email (se fornecido)
        if (email != null && !email.trim().isEmpty()) {
            return studentRepository.findByEmail(email.trim()).orElse(null);
        }
        
        // Buscar por nome completo, primeiro nome ou segundo nome
        if (fullName != null && !fullName.trim().isEmpty()) {
            String normalizedFullName = fullName.trim();
            List<Student> students = studentRepository.findAll();
            
            // Normalizar nome do CSV (remover espaços extras, converter para minúsculas para comparação)
            String csvNameLower = normalizedFullName.toLowerCase().trim();
            String[] csvNameParts = csvNameLower.split("\\s+");
            
            for (Student student : students) {
                if (student.getUserName() == null || student.getUserName().trim().isEmpty()) {
                    continue;
                }
                
                String studentName = student.getUserName().trim();
                String studentNameLower = studentName.toLowerCase();
                String[] studentNameParts = studentNameLower.split("\\s+");
                
                // 1. Comparação exata (case-insensitive)
                if (studentNameLower.equals(csvNameLower)) {
                    log.debug("Match exato encontrado: '{}' -> '{}'", normalizedFullName, studentName);
                    return student;
                }
                
                // 2. Comparar primeiro nome
                if (csvNameParts.length > 0 && studentNameParts.length > 0) {
                    String csvFirstName = csvNameParts[0];
                    String studentFirstName = studentNameParts[0];
                    
                    if (csvFirstName.equals(studentFirstName)) {
                        // Se ambos têm segundo nome, comparar também
                        if (csvNameParts.length > 1 && studentNameParts.length > 1) {
                            String csvSecondName = csvNameParts[1];
                            String studentSecondName = studentNameParts[1];
                            
                            if (csvSecondName.equals(studentSecondName)) {
                                log.debug("Match por primeiro e segundo nome: '{}' -> '{}'", normalizedFullName, studentName);
                                return student;
                            }
                        } else {
                            // Apenas primeiro nome bateu - aceitar se o CSV tem só um nome
                            if (csvNameParts.length == 1) {
                                log.debug("Match por primeiro nome: '{}' -> '{}'", normalizedFullName, studentName);
                                return student;
                            }
                        }
                    }
                }
                
                // 3. Comparar segundo nome do CSV com primeiro nome do estudante
                if (csvNameParts.length > 1 && studentNameParts.length > 0) {
                    String csvSecondName = csvNameParts[1];
                    String studentFirstName = studentNameParts[0];
                    
                    if (csvSecondName.equals(studentFirstName)) {
                        log.debug("Match: segundo nome do CSV com primeiro nome do estudante: '{}' -> '{}'", normalizedFullName, studentName);
                        return student;
                    }
                }
                
                // 4. Verificar se o nome do CSV contém o nome do estudante ou vice-versa (match parcial)
                if (csvNameLower.contains(studentNameLower) || studentNameLower.contains(csvNameLower)) {
                    // Verificar se pelo menos o primeiro nome está presente
                    if (csvNameParts.length > 0 && studentNameParts.length > 0) {
                        if (csvNameParts[0].equals(studentNameParts[0])) {
                            log.debug("Match parcial por primeiro nome: '{}' -> '{}'", normalizedFullName, studentName);
                            return student;
                        }
                    }
                }
            }
        }
        
        return null;
    }

    private void importExamGrade(PreviewItem item, Long examId, Authentication authentication, boolean sendEmail) {
        // examId pode ser null - nota de prova geral
        Student student = studentRepository.findById(item.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Estudante não encontrado com ID: " + item.getStudentId()));

        ExamGradeDTO dto = ExamGradeDTO.builder()
                .studentId(student.getId())
                .examId(examId) // Pode ser null para notas gerais
                .pointsObtained(item.getPointsObtained()) // Manter como Double
                .totalPoints(item.getTotalPoints()) // Manter como Double
                .grade(item.getGrade())
                .feedback(item.getFeedback())
                .build();

        examGradeService.createExamGrade(dto, authentication);
        // sendEmail é controlado pelo ExamGradeService internamente
    }

    private void importWeeklyGrade(PreviewItem item, Authentication authentication, boolean sendEmail) {
        Student student = studentRepository.findById(item.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Estudante não encontrado com ID: " + item.getStudentId()));

        // Calcular início da semana (segunda-feira)
        LocalDate today = LocalDate.now();
        int daysToSubtract = today.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
        if (daysToSubtract < 0) {
            daysToSubtract += 7;
        }
        LocalDate weekStart = today.minusDays(daysToSubtract);

        WeeklyGradeDTO dto = WeeklyGradeDTO.builder()
                .studentId(student.getId())
                .pointsObtained(item.getPointsObtained()) // Manter como Double
                .totalPoints(item.getTotalPoints()) // Manter como Double
                .grade(item.getGrade())
                .feedback(item.getFeedback())
                .weekStartDate(weekStart) // Usar início da semana atual
                .build();

        weeklyGradeService.createWeeklyGrade(dto, authentication);
        // sendEmail é controlado pelo WeeklyGradeService internamente
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ImportResult {
        private int successCount;
        private int errorCount;
        private List<String> errors;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PreviewItem {
        private String email;
        private String fullName;
        private String score;
        private Double pointsObtained;
        private Double totalPoints;
        private com.NorthrnLights.demo.domain.Grade grade;
        private String feedback;
        private Long studentId;
        private String studentName;
        private boolean found;
        private String errorMessage;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class PreviewResult {
        private List<PreviewItem> items;
        private List<String> errors;
    }
}

