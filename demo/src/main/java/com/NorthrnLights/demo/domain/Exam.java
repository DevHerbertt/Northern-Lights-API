package com.NorthrnLights.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "exams")
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "teacher_id")
    @JsonIgnoreProperties({"questions", "meet", "createAt", "updateAt"})
    private Teacher teacher;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"exam", "teacher", "options"})
    private List<Question> questions;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer durationMinutes; // Duração em minutos
    private Integer totalScore; // Pontuação total da prova
    private Boolean isActive; // Se a prova está ativa/disponível

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

