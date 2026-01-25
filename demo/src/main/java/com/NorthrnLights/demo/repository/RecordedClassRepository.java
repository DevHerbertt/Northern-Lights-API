package com.NorthrnLights.demo.repository;

import com.NorthrnLights.demo.domain.RecordedClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecordedClassRepository extends JpaRepository<RecordedClass, Long> {
    
    List<RecordedClass> findByTeacherIdOrderByClassDateDesc(Long teacherId);
    
    List<RecordedClass> findAllByOrderByClassDateDesc();
}






