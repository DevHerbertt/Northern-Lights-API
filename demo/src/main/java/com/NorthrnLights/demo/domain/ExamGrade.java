package com.NorthrnLights.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "exam_id"}), 
       indexes = @Index(columnList = "student_id"))
public class ExamGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "points_obtained", nullable = false)
    private Double pointsObtained; // Pontos obtidos pelo aluno

    @Column(name = "total_points", nullable = false)
    private Double totalPoints; // Total de pontos da prova

    @Enumerated(EnumType.STRING)
    private Grade grade; // Classificação calculada (A+, B+, etc.)

    @Column(columnDefinition = "TEXT")
    private String feedback; // Feedback do professor

    @ManyToOne
    @JoinColumn(name = "student_id")
    @JsonIgnoreProperties({"answers", "weeklyGrades", "examGrades"})
    private Student student;

    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = true)
    @JsonIgnoreProperties({"questions", "teacher", "examGrades"})
    private Exam exam; // Pode ser null para notas de prova gerais (sem prova específica)

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    @JsonIgnoreProperties({"questions", "meet", "createAt", "updateAt"})
    private Teacher teacher;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Método para calcular porcentagem
    public Double getPercentage() {
        if (totalPoints == null || totalPoints == 0) {
            return 0.0;
        }
        return (pointsObtained / totalPoints) * 100.0;
    }
}

