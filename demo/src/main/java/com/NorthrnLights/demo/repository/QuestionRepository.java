package com.NorthrnLights.demo.repository;

import com.NorthrnLights.demo.domain.Question;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    // Filtrar por título (usando como exemplo "contains" para buscar algo no título)
    List<Question> findByTitleContainingIgnoreCase(String title);

    // Filtrar por descrição - Query customizada para trabalhar com CLOB/TEXT
    @Query(value = "SELECT * FROM question WHERE LOWER(description) LIKE LOWER(CONCAT('%', :description, '%'))", nativeQuery = true)
    List<Question> findByDescriptionContainingIgnoreCase(@Param("description") String description);

    // Filtrar por título e descrição - Query customizada para trabalhar com CLOB/TEXT
    @Query(value = "SELECT * FROM question WHERE LOWER(title) LIKE LOWER(CONCAT('%', :title, '%')) AND LOWER(description) LIKE LOWER(CONCAT('%', :description, '%'))", nativeQuery = true)
    List<Question> findByTitleContainingIgnoreCaseAndDescriptionContainingIgnoreCase(@Param("title") String title, @Param("description") String description);

    // Outras consultas, como por data de criação ou atualização
    List<Question> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Buscar questão com opções (eager fetch)
    @EntityGraph(attributePaths = {"options"})
    @Query("SELECT q FROM Question q WHERE q.id = :id")
    Optional<Question> findByIdWithOptions(@Param("id") Long id);

    // Buscar todas as questões com opções
    @EntityGraph(attributePaths = {"options"})
    @Query("SELECT q FROM Question q")
    List<Question> findAllWithOptions();
    
    // Buscar questões por professor e intervalo de data
    List<Question> findByTeacherAndCreatedAtBetween(com.NorthrnLights.demo.domain.Teacher teacher, LocalDateTime start, LocalDateTime end);

}