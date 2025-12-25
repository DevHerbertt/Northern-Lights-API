package com.NorthrnLights.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "week_start_date"}))
public class WeeklyGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "points_obtained")
    private Double pointsObtained; // Pontos obtidos pelo aluno (opcional, pode ser null para notas antigas)

    @Column(name = "total_points")
    private Double totalPoints; // Total de pontos da atividade semanal (opcional)

    @Enumerated(EnumType.STRING)
    private Grade grade;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String feedback; // Feedback geral da semana

    @ManyToOne
    @JoinColumn(name = "student_id")
    @JsonIgnoreProperties({"answers", "weeklyGrades"})
    private Student student;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    @JsonIgnoreProperties({"questions", "meet", "createAt", "updateAt"})
    private Teacher teacher;

    @Column(name = "week_start_date")
    private LocalDate weekStartDate; // Data de início da semana (segunda-feira)

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Método para calcular porcentagem
    public Double getPercentage() {
        if (totalPoints == null || totalPoints == 0) {
            return null; // Retorna null se não tiver pontos definidos
        }
        if (pointsObtained == null) {
            return null;
        }
        return (pointsObtained / totalPoints) * 100.0;
    }
}


