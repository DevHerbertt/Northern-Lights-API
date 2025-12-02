package com.NorthrnLights.demo.domain;

import com.NorthrnLights.demo.domain.Answer;
import com.NorthrnLights.demo.domain.Teacher;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private String description;

    @Column(name = "image_path")
    private String imagePath; // Caminho para o arquivo salvo no servidor

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    @JsonBackReference("teacher-questions") // Mesmo nome da referência
    private Teacher teacher;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    @JsonManagedReference("question-answers") // Nome único para esta referência
    private List<Answer> answers = new ArrayList<>();

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;
}
