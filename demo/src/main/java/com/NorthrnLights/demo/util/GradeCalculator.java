package com.NorthrnLights.demo.util;

import com.NorthrnLights.demo.domain.Grade;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GradeCalculator {

    /**
     * Calcula a classificação (Grade) baseada na porcentagem
     * @param percentage Porcentagem (0-100)
     * @return Grade correspondente
     */
    public static Grade calculateGradeFromPercentage(Double percentage) {
        if (percentage == null || percentage < 0) {
            return Grade.F;
        }

        if (percentage >= 95.0) return Grade.A_PLUS;
        if (percentage >= 90.0) return Grade.A;
        if (percentage >= 85.0) return Grade.A_MINUS;
        if (percentage >= 80.0) return Grade.B_PLUS;
        if (percentage >= 70.0) return Grade.B;
        if (percentage >= 65.0) return Grade.B_MINUS;
        if (percentage >= 60.0) return Grade.C_PLUS;
        if (percentage >= 50.0) return Grade.C;
        if (percentage >= 45.0) return Grade.C_MINUS;
        if (percentage >= 40.0) return Grade.D_PLUS;
        if (percentage >= 30.0) return Grade.D;
        return Grade.F;
    }

    /**
     * Calcula a classificação baseada em pontos obtidos e total
     * @param pointsObtained Pontos obtidos
     * @param totalPoints Total de pontos
     * @return Grade correspondente
     */
    public static Grade calculateGradeFromPoints(Double pointsObtained, Double totalPoints) {
        if (pointsObtained == null || totalPoints == null || totalPoints == 0) {
            return Grade.F;
        }
        
        Double percentage = (pointsObtained / totalPoints) * 100.0;
        return calculateGradeFromPercentage(percentage);
    }

    /**
     * Formata a nota no formato X/Y
     * @param pointsObtained Pontos obtidos
     * @param totalPoints Total de pontos
     * @return String formatada "X/Y"
     */
    public static String formatGradeAsFraction(Double pointsObtained, Double totalPoints) {
        if (pointsObtained == null || totalPoints == null) {
            return "0/0";
        }
        return String.format("%.1f/%.1f", pointsObtained, totalPoints);
    }

    /**
     * Formata a nota com classificação: X/Y (Grade)
     * @param pointsObtained Pontos obtidos
     * @param totalPoints Total de pontos
     * @param grade Classificação
     * @return String formatada "X/Y (Grade)"
     */
    public static String formatGradeWithClassification(Double pointsObtained, Double totalPoints, Grade grade) {
        String fraction = formatGradeAsFraction(pointsObtained, totalPoints);
        String gradeDisplay = grade != null ? grade.toString() : "N/A";
        return String.format("%s (%s)", fraction, gradeDisplay);
    }
}






