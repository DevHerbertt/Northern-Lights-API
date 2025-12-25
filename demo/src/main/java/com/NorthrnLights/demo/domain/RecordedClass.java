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
@Table(name = "recorded_classes")
public class RecordedClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // Título da aula gravada

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description; // Descrição opcional

    @Column(nullable = false)
    private String videoUrl; // URL do vídeo (YouTube, Vimeo, etc.)

    @Column(name = "class_date", nullable = false)
    private LocalDate classDate; // Data da aula

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    @JsonIgnoreProperties({"questions", "meet", "createAt", "updateAt"})
    private Teacher teacher;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}



