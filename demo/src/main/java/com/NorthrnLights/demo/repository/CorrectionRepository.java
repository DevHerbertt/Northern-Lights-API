package com.NorthrnLights.demo.repository;

import com.NorthrnLights.demo.domain.Correction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CorrectionRepository extends JpaRepository<Correction, Long> {
    List<Correction> findByTeacherId(Long teacherId);
    List<Correction> findByAnswerId(Long answerId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Correction c WHERE c.answer.id = :answerId")
    void deleteByAnswerId(@Param("answerId") Long answerId);
}