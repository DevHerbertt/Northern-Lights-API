package com.NorthrnLights.demo.repository;

import com.NorthrnLights.demo.domain.Correction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CorrectionRepository extends JpaRepository<Correction, Long> {
    List<Correction> findByTeacherId(Long teacherId);
    List<Correction> findByAnswerId(Long answerId);
}