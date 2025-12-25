package com.NorthrnLights.demo.service;

import com.NorthrnLights.demo.domain.RecordedClass;
import com.NorthrnLights.demo.domain.Teacher;
import com.NorthrnLights.demo.dto.RecordedClassDTO;
import com.NorthrnLights.demo.repository.RecordedClassRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordedClassService {

    private final RecordedClassRepository recordedClassRepository;

    public RecordedClass createRecordedClass(RecordedClassDTO dto, Authentication authentication) {
        log.info("Criando aula gravada: {}", dto.getTitle());

        Teacher teacher = getAuthenticatedTeacher(authentication);

        RecordedClass recordedClass = RecordedClass.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .videoUrl(dto.getVideoUrl())
                .classDate(dto.getClassDate())
                .teacher(teacher)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return recordedClassRepository.save(recordedClass);
    }

    public RecordedClass updateRecordedClass(Long id, RecordedClassDTO dto, Authentication authentication) {
        RecordedClass existing = recordedClassRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aula gravada não encontrada"));

        Teacher authenticatedTeacher = getAuthenticatedTeacher(authentication);
        if (!existing.getTeacher().getId().equals(authenticatedTeacher.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode editar suas próprias aulas gravadas");
        }

        if (dto.getTitle() != null) existing.setTitle(dto.getTitle());
        if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
        if (dto.getVideoUrl() != null) existing.setVideoUrl(dto.getVideoUrl());
        if (dto.getClassDate() != null) existing.setClassDate(dto.getClassDate());
        existing.setUpdatedAt(LocalDateTime.now());

        return recordedClassRepository.save(existing);
    }

    public void deleteRecordedClass(Long id, Authentication authentication) {
        RecordedClass existing = recordedClassRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aula gravada não encontrada"));

        Teacher authenticatedTeacher = getAuthenticatedTeacher(authentication);
        if (!existing.getTeacher().getId().equals(authenticatedTeacher.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode excluir suas próprias aulas gravadas");
        }

        recordedClassRepository.delete(existing);
    }

    public List<RecordedClass> getAllRecordedClasses() {
        return recordedClassRepository.findAllByOrderByClassDateDesc();
    }

    public List<RecordedClass> getTeacherRecordedClasses(Authentication authentication) {
        Teacher teacher = getAuthenticatedTeacher(authentication);
        return recordedClassRepository.findByTeacherIdOrderByClassDateDesc(teacher.getId());
    }

    public RecordedClass getRecordedClassById(Long id) {
        return recordedClassRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aula gravada não encontrada"));
    }

    private Teacher getAuthenticatedTeacher(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }

        if (!(authentication.getPrincipal() instanceof Teacher)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário autenticado não é um professor");
        }

        return (Teacher) authentication.getPrincipal();
    }
}

