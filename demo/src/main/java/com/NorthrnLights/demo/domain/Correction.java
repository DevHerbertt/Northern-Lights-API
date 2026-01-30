package com.NorthrnLights.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Correction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Grade grade;

    @Column(columnDefinition = "TEXT")
    private String feedback; // Texto da correção

    @ManyToOne
    @JoinColumn(name = "answer_id")
    @JsonIgnoreProperties({"corrections", "student", "question"})
    private Answer answer;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    @JsonIgnoreProperties({"questions", "meet", "createAt", "updateAt"})
    private Teacher teacher;
}
