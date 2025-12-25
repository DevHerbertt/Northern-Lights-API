package com.NorthrnLights.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String portugueseTranslation; // Tradução/ajuda em português para o aluno
    private Boolean hasHelp; // Indica se a questão tem ajuda disponível

    private String imagePath;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    @JsonIgnoreProperties({"questions", "meet", "createAt", "updateAt"})
    private Teacher teacher;

    @ManyToOne
    @JoinColumn(name = "exam_id")
    @JsonIgnoreProperties({"questions", "teacher"})
    private Exam exam; // Prova à qual a questão pertence (null se não for de prova)

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<QuestionOption> options;

    private boolean multipleChoice;

    @Enumerated(EnumType.STRING)
    private QuestionType type;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // Data de expiração da questão (para correção automática)
    
    @Column(name = "visible_at")
    private LocalDateTime visibleAt; // Data em que a questão ficará visível para os alunos

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
