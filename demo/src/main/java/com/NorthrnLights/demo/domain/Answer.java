package com.NorthrnLights.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnoreProperties({"answers", "createAt", "lastLogin", "levelEnglish", "status"})
    private Student student; // Student herda de User, então tem userName, email, id

    @ManyToOne
    @JoinColumn(name = "question_id")
    @JsonIgnoreProperties({"options", "teacher", "exam"})
    private Question question;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String text;

    @Column(name = "image_path")
    private String imagePath;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
