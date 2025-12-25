package com.NorthrnLights.demo.util;

import com.NorthrnLights.demo.domain.Grade;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CsvGradeParser {

    @Data
    public static class ParsedGrade {
        private String email;
        private String score; // Formato "X / Y" ou "X/Y"
        private String fullName;
        private String type; // "prova", "home", "homework"
        private List<String> answers; // Respostas das questões
        private String feedback; // Feedback do aluno (última coluna)
        
        // Campos calculados
        private Double pointsObtained;
        private Double totalPoints;
        private Grade grade;
    }

    /**
     * Parse CSV content and extract grades
     * @param inputStream InputStream do arquivo CSV
     * @param gradeType Tipo de nota: "prova", "home", "homework"
     * @return Lista de notas parseadas
     */
    public static List<ParsedGrade> parseCsv(InputStream inputStream, String gradeType) throws IOException {
        List<ParsedGrade> grades = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            String headerLine = reader.readLine();
            if (headerLine == null) {
                log.warn("CSV vazio ou sem cabeçalho");
                return grades;
            }
            
            // Parse header para identificar colunas
            String[] headers = parseCsvLine(headerLine);
            log.info("CSV Header: {} colunas encontradas", headers.length);
            
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    ParsedGrade grade = parseGradeLine(line, headers, gradeType);
                    if (grade != null) {
                        grades.add(grade);
                    }
                } catch (Exception e) {
                    log.error("Erro ao processar linha {}: {}", lineNumber, e.getMessage());
                }
            }
        }
        
        log.info("Total de {} notas parseadas do CSV", grades.size());
        return grades;
    }

    /**
     * Parse uma linha do CSV
     * Formato esperado: Timestamp, Score (X / Y), Full name, [respostas...], Feedback
     */
    private static ParsedGrade parseGradeLine(String line, String[] headers, String gradeType) {
        String[] values = parseCsvLine(line);
        
        if (values.length < 3) {
            log.warn("Linha com menos de 3 colunas, ignorando: {}", line);
            return null;
        }
        
        ParsedGrade grade = new ParsedGrade();
        
        // Primeira coluna: Timestamp (ignoramos, mas mantemos para referência)
        String timestamp = values[0].trim();
        
        // Segunda coluna: Nota (formato "X / Y" ou "X/Y")
        String scoreStr = values[1].trim();
        grade.setScore(scoreStr);
        
        // Parse da nota para obter pontos obtidos e total
        parseScore(scoreStr, grade);
        
        // Terceira coluna: Nome completo
        String fullName = values[2].trim();
        // Validar que o nome não está vazio e não é muito longo (provavelmente não é feedback)
        if (fullName.isEmpty() || fullName.length() > 100) {
            log.warn("Nome inválido na linha (muito longo ou vazio): '{}'", fullName);
            return null; // Ignorar linha inválida
        }
        grade.setFullName(fullName);
        
        // Email será null - vamos buscar apenas por nome
        grade.setEmail(null);
        
        // Tipo de nota
        grade.setType(gradeType != null ? gradeType.toLowerCase() : "homework");
        
        // Respostas das questões (colunas 3 até penúltima)
        List<String> answers = new ArrayList<>();
        for (int i = 3; i < values.length - 1; i++) {
            String answerValue = values[i].trim();
            // Ignorar valores vazios ou muito longos (provavelmente não são respostas válidas)
            if (!answerValue.isEmpty() && answerValue.length() < 500) {
                answers.add(answerValue);
            }
        }
        grade.setAnswers(answers);
        
        // Última coluna: Feedback
        if (values.length > 3) {
            String feedback = values[values.length - 1].trim();
            // Limitar tamanho do feedback para evitar problemas
            if (feedback.length() > 2000) {
                feedback = feedback.substring(0, 2000) + "...";
            }
            grade.setFeedback(feedback);
        }
        
        // Calcular grade baseado na porcentagem
        if (grade.getPointsObtained() != null && grade.getTotalPoints() != null && grade.getTotalPoints() > 0) {
            try {
                grade.setGrade(calculateGradeFromPoints(
                        grade.getPointsObtained(), 
                        grade.getTotalPoints()
                ));
            } catch (Exception e) {
                log.warn("Erro ao calcular grade para {}: {}", grade.getFullName(), e.getMessage());
            }
        }
        
        return grade;
    }

    /**
     * Parse score string "X / Y" ou "X/Y" para pontos obtidos e total
     */
    private static void parseScore(String scoreStr, ParsedGrade grade) {
        try {
            // Remover espaços e dividir por "/"
            String cleaned = scoreStr.replaceAll("\\s+", ""); // Remove todos os espaços
            String[] parts = cleaned.split("/");
            
            if (parts.length == 2) {
                grade.setPointsObtained(Double.parseDouble(parts[0].trim()));
                grade.setTotalPoints(Double.parseDouble(parts[1].trim()));
            } else {
                log.warn("Formato de nota inválido: {}", scoreStr);
            }
        } catch (Exception e) {
            log.error("Erro ao parsear nota '{}': {}", scoreStr, e.getMessage());
        }
    }

    /**
     * Parse CSV line handling quoted fields with commas
     */
    private static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentField = new StringBuilder();
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Escaped quote ("")
                    currentField.append('"');
                    i++; // Skip next quote
                } else {
                    // Toggle quote state
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // End of field
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        // Add last field
        fields.add(currentField.toString());
        
        return fields.toArray(new String[0]);
    }

    /**
     * Calcular grade baseado em pontos obtidos e total
     */
    private static Grade calculateGradeFromPoints(Double pointsObtained, Double totalPoints) {
        if (pointsObtained == null || totalPoints == null || totalPoints <= 0) {
            return Grade.F;
        }

        double percentage = (pointsObtained / totalPoints) * 100;

        if (percentage >= 95) return Grade.A_PLUS;
        if (percentage >= 90) return Grade.A;
        if (percentage >= 85) return Grade.A_MINUS;
        if (percentage >= 80) return Grade.B_PLUS;
        if (percentage >= 70) return Grade.B;
        if (percentage >= 65) return Grade.B_MINUS;
        if (percentage >= 60) return Grade.C_PLUS;
        if (percentage >= 50) return Grade.C;
        if (percentage >= 45) return Grade.C_MINUS;
        if (percentage >= 40) return Grade.D_PLUS;
        if (percentage >= 30) return Grade.D;
        return Grade.F;
    }
}

