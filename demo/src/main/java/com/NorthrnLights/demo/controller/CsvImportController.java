package com.NorthrnLights.demo.controller;

import com.NorthrnLights.demo.service.CsvImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/csv/import")
@RequiredArgsConstructor
@Slf4j
public class CsvImportController {

    private final CsvImportService csvImportService;

    @PostMapping("/preview")
    public ResponseEntity<CsvImportService.PreviewResult> previewCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam("gradeType") String gradeType,
            @RequestParam(value = "examId", required = false) Long examId) {
        
        try {
            log.info("Recebida requisição para prévia do CSV. Tipo: {}, ExamId: {}", gradeType, examId);
            
            if (!file.getContentType().equals("text/csv") && 
                !file.getOriginalFilename().endsWith(".csv")) {
                return ResponseEntity.badRequest().build();
            }

            CsvImportService.PreviewResult result = csvImportService.previewCsvGrades(
                    file, gradeType, examId);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Erro ao gerar prévia do CSV", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/grades")
    public ResponseEntity<CsvImportService.ImportResult> importGrades(
            @RequestBody CsvImportRequest request,
            Authentication authentication) {
        
        try {
            log.info("Recebida requisição para importar notas do CSV. Tipo: {}, ExamId: {}, SendEmail: {}, SendToDashboard: {}", 
                    request.getGradeType(), request.getExamId(), request.isSendEmail(), request.isSendToDashboard());

            CsvImportService.ImportResult result = csvImportService.importGradesFromCsv(
                    request.getItems(), 
                    request.getGradeType(), 
                    request.getExamId(),
                    request.isSendEmail(),
                    request.isSendToDashboard(),
                    authentication);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Erro ao importar CSV", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @lombok.Data
    public static class CsvImportRequest {
        private java.util.List<CsvImportService.PreviewItem> items;
        private String gradeType;
        private Long examId;
        private boolean sendEmail;
        private boolean sendToDashboard;
    }
}

