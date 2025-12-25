package com.NorthrnLights.demo.service;

import com.NorthrnLights.demo.domain.Exam;
import com.NorthrnLights.demo.domain.Question;
import com.NorthrnLights.demo.domain.Teacher;
import com.NorthrnLights.demo.dto.ExamDTO;
import com.NorthrnLights.demo.repository.ExamRepository;
import com.NorthrnLights.demo.repository.QuestionRepository;
import com.NorthrnLights.demo.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final TeacherRepository teacherRepository;

    @Transactional
    public Exam createExam(ExamDTO dto, Authentication authentication) {
        Teacher teacher = (Teacher) authentication.getPrincipal();
        
        Exam exam = Exam.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .teacher(teacher)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .durationMinutes(dto.getDurationMinutes())
                .totalScore(dto.getTotalScore())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();

        exam = examRepository.save(exam);

        // Associar questões à prova
        if (dto.getQuestionIds() != null && !dto.getQuestionIds().isEmpty()) {
            List<Question> questions = questionRepository.findAllById(dto.getQuestionIds());
            for (Question question : questions) {
                question.setExam(exam);
            }
            questionRepository.saveAll(questions);
        }

        return exam;
    }

    public List<Exam> findAll() {
        return examRepository.findAll();
    }

    public Exam findById(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prova não encontrada"));
    }

    public List<Exam> findByTeacher(Authentication authentication) {
        Teacher teacher = (Teacher) authentication.getPrincipal();
        return examRepository.findByTeacherId(teacher.getId());
    }

    @Transactional
    public Exam updateExam(Long id, ExamDTO dto, Authentication authentication) {
        Exam exam = findById(id);
        Teacher teacher = (Teacher) authentication.getPrincipal();

        if (!exam.getTeacher().getId().equals(teacher.getId())) {
            throw new RuntimeException("Você não tem permissão para editar esta prova");
        }

        exam.setTitle(dto.getTitle());
        exam.setDescription(dto.getDescription());
        exam.setStartDate(dto.getStartDate());
        exam.setEndDate(dto.getEndDate());
        exam.setDurationMinutes(dto.getDurationMinutes());
        exam.setTotalScore(dto.getTotalScore());
        if (dto.getIsActive() != null) {
            exam.setIsActive(dto.getIsActive());
        }

        // Atualizar questões associadas
        if (dto.getQuestionIds() != null) {
            // Remover associação de questões antigas
            List<Question> oldQuestions = questionRepository.findAll()
                    .stream()
                    .filter(q -> exam.equals(q.getExam()))
                    .toList();
            for (Question q : oldQuestions) {
                q.setExam(null);
            }

            // Adicionar novas questões
            if (!dto.getQuestionIds().isEmpty()) {
                List<Question> questions = questionRepository.findAllById(dto.getQuestionIds());
                for (Question question : questions) {
                    question.setExam(exam);
                }
                questionRepository.saveAll(questions);
            }
        }

        return examRepository.save(exam);
    }

    @Transactional
    public void deleteExam(Long id, Authentication authentication) {
        Exam exam = findById(id);
        Teacher teacher = (Teacher) authentication.getPrincipal();

        if (!exam.getTeacher().getId().equals(teacher.getId())) {
            throw new RuntimeException("Você não tem permissão para excluir esta prova");
        }

        // Remover associação de questões
        List<Question> questions = questionRepository.findAll()
                .stream()
                .filter(q -> exam.equals(q.getExam()))
                .toList();
        for (Question q : questions) {
            q.setExam(null);
        }
        questionRepository.saveAll(questions);

        examRepository.delete(exam);
    }
}



