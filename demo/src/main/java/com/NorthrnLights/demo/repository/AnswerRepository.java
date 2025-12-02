package com.NorthrnLights.demo.repository;

import com.NorthrnLights.demo.domain.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    boolean existsByQuestionIdAndStudentId(Long questionId, Long studentId);
    List<Answer> findByQuestionId(Long questionId);
}