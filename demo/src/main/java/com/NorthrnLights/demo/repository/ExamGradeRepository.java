package com.NorthrnLights.demo.repository;

import com.NorthrnLights.demo.domain.ExamGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamGradeRepository extends JpaRepository<ExamGrade, Long> {
    
    Optional<ExamGrade> findByStudentIdAndExamId(Long studentId, Long examId);
    
    @Query("SELECT eg FROM ExamGrade eg LEFT JOIN FETCH eg.exam WHERE eg.student.id = :studentId ORDER BY eg.createdAt DESC")
    List<ExamGrade> findByStudentIdOrderByCreatedAtDesc(@Param("studentId") Long studentId);
    
    List<ExamGrade> findByExamId(Long examId);

    List<ExamGrade> findByTeacherId(Long teacherId);
    
    // Buscar notas gerais (sem examId) para um estudante
    List<ExamGrade> findByStudentIdAndExamIsNullOrderByCreatedAtDesc(Long studentId);
}

