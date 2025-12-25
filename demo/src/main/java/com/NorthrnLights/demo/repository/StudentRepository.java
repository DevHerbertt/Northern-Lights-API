package com.NorthrnLights.demo.repository;

import com.NorthrnLights.demo.domain.Student;
import com.NorthrnLights.demo.domain.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByStatusAndLastLoginBefore(Status status, LocalDateTime date);
    List<Student> findByStatusAndLastLoginAfter(Status status, LocalDateTime date);

    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.answers WHERE s.id = :id")
    Optional<Student> findByIdWithAnswers(@Param("id") Long id);

    Optional<Student> findByEmail(String email);
}