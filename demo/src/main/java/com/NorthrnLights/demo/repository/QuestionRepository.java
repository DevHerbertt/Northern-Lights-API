package com.NorthrnLights.demo.repository;

import com.NorthrnLights.demo.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    // Filtrar por título (usando como exemplo "contains" para buscar algo no título)
    List<Question> findByTitleContainingIgnoreCase(String title);

    // Filtrar por descrição
    List<Question> findByDescriptionContainingIgnoreCase(String description);

    // Filtrar por título e descrição
    List<Question> findByTitleContainingIgnoreCaseAndDescriptionContainingIgnoreCase(String title, String description);

    // Outras consultas, como por data de criação ou atualização
    List<Question> findByCreateAtBetween(LocalDateTime startDate, LocalDateTime endDate);

}