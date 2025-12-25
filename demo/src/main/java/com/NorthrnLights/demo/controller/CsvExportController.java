package com.NorthrnLights.demo.controller;

import com.NorthrnLights.demo.service.CsvExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/csv/export")
@RequiredArgsConstructor
@Slf4j
public class CsvExportController {

    private final CsvExportService csvExportService;

    @GetMapping("/exam/{examId}")
    public ResponseEntity<byte[]> exportExamGrades(@PathVariable Long examId, Authentication authentication) {
        try {
            byte[] csvData = csvExportService.exportExamGradesToCsv(examId, authentication);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
            headers.setContentDispositionFormData("attachment", "notas_prova_" + examId + ".csv");
            
            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Erro ao exportar notas de prova", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/weekly")
    public ResponseEntity<byte[]> exportWeeklyGrades(
            @RequestParam(value = "weekStartDate", required = false) LocalDate weekStartDate,
            Authentication authentication) {
        try {
            byte[] csvData = csvExportService.exportWeeklyGradesToCsv(weekStartDate, authentication);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
            String filename = weekStartDate != null ? 
                    "notas_semanais_" + weekStartDate + ".csv" : 
                    "notas_semanais_completo.csv";
            headers.setContentDispositionFormData("attachment", filename);
            
            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Erro ao exportar notas semanais", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

