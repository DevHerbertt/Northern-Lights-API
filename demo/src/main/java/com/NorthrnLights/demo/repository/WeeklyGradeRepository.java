package com.NorthrnLights.demo.repository;

import com.NorthrnLights.demo.domain.WeeklyGrade;
import com.NorthrnLights.demo.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeeklyGradeRepository extends JpaRepository<WeeklyGrade, Long> {
    
    Optional<WeeklyGrade> findByStudentIdAndWeekStartDate(Long studentId, LocalDate weekStartDate);
    
    List<WeeklyGrade> findByStudentIdOrderByWeekStartDateDesc(Long studentId);
    
    List<WeeklyGrade> findByStudentIdAndWeekStartDateBetween(Long studentId, LocalDate start, LocalDate end);
    
    Optional<WeeklyGrade> findFirstByStudentIdOrderByWeekStartDateDesc(Long studentId);
    
    // Deletar todas as notas semanais de um estudante
    void deleteByStudentId(Long studentId);
}




