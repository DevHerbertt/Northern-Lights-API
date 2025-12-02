package com.NorthrnLights.demo.service;

import com.NorthrnLights.demo.domain.Answer;
import com.NorthrnLights.demo.domain.Correction;
import com.NorthrnLights.demo.domain.Teacher;
import com.NorthrnLights.demo.dto.CorrectionDTO;
import com.NorthrnLights.demo.repository.AnswerRepository;
import com.NorthrnLights.demo.repository.CorrectionRepository;
import com.NorthrnLights.demo.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CorrectionService {

    private final CorrectionRepository correctionRepository;
    private final AnswerRepository answerRepository;
    private final TeacherRepository teacherRepository;

    public Correction createCorrection(CorrectionDTO dto, Authentication authentication) {
        log.info("Creating correction for Answer ID: {}", dto.getAnswerId());

        Answer answer = answerRepository.findById(dto.getAnswerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Answer not found"));

        Teacher teacher = getAuthenticatedTeacher(authentication);

        Correction correction = Correction.builder()
                .grade(dto.getGrade())
                .feedback(dto.getFeedback())
                .answer(answer)
                .teacher(teacher)
                .build();

        return correctionRepository.save(correction);
    }

    public List<Correction> findAll() {
        return correctionRepository.findAll();
    }

    public Correction findById(Long id) {
        return correctionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Correction not found"));
    }

    public Correction updateCorrection(Long id, CorrectionDTO dto, Authentication authentication) {
        Correction existing = findById(id);

        Teacher authenticatedTeacher = getAuthenticatedTeacher(authentication);
        if (!existing.getTeacher().getId().equals(authenticatedTeacher.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own corrections");
        }

        if (dto.getGrade() != null) {
            existing.setGrade(dto.getGrade());
        }
        if (dto.getFeedback() != null) {
            existing.setFeedback(dto.getFeedback());
        }

        return correctionRepository.save(existing);
    }

    public void deleteCorrection(Long id, Authentication authentication) {
        Correction correction = findById(id);

        Teacher authenticatedTeacher = getAuthenticatedTeacher(authentication);
        if (!correction.getTeacher().getId().equals(authenticatedTeacher.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own corrections");
        }

        correctionRepository.deleteById(id);
    }

    public List<Correction> findByTeacherId(Long teacherId, Authentication authentication) {
        Teacher authenticatedTeacher = getAuthenticatedTeacher(authentication);
        if (!authenticatedTeacher.getId().equals(teacherId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only access your own corrections");
        }

        return correctionRepository.findByTeacherId(teacherId);
    }

    public List<Correction> findByAuthenticatedTeacher(Authentication authentication) {
        Teacher teacher = getAuthenticatedTeacher(authentication);
        return correctionRepository.findByTeacherId(teacher.getId());
    }

    public List<Correction> findByAnswerId(Long answerId) {
        return correctionRepository.findByAnswerId(answerId);
    }

    private Teacher getAuthenticatedTeacher(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        String email = authentication.getName();
        log.debug("Looking for teacher with email: {}", email);

        return teacherRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Authenticated user is not a teacher"));
    }
}