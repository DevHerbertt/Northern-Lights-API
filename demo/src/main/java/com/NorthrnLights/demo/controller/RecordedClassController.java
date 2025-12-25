package com.NorthrnLights.demo.controller;

import com.NorthrnLights.demo.domain.RecordedClass;
import com.NorthrnLights.demo.dto.RecordedClassDTO;
import com.NorthrnLights.demo.service.RecordedClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recorded-classes")
@RequiredArgsConstructor
@Slf4j
public class RecordedClassController {

    private final RecordedClassService recordedClassService;

    @PostMapping
    public ResponseEntity<RecordedClass> createRecordedClass(
            @RequestBody RecordedClassDTO dto,
            Authentication authentication) {
        try {
            RecordedClass recordedClass = recordedClassService.createRecordedClass(dto, authentication);
            return ResponseEntity.status(HttpStatus.CREATED).body(recordedClass);
        } catch (Exception e) {
            log.error("Erro ao criar aula gravada", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecordedClass> updateRecordedClass(
            @PathVariable Long id,
            @RequestBody RecordedClassDTO dto,
            Authentication authentication) {
        try {
            RecordedClass recordedClass = recordedClassService.updateRecordedClass(id, dto, authentication);
            return ResponseEntity.ok(recordedClass);
        } catch (Exception e) {
            log.error("Erro ao atualizar aula gravada", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecordedClass(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            recordedClassService.deleteRecordedClass(id, authentication);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Erro ao excluir aula gravada", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<RecordedClass>> getAllRecordedClasses() {
        try {
            List<RecordedClass> classes = recordedClassService.getAllRecordedClasses();
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            log.error("Erro ao buscar aulas gravadas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/teacher")
    public ResponseEntity<List<RecordedClass>> getTeacherRecordedClasses(Authentication authentication) {
        try {
            List<RecordedClass> classes = recordedClassService.getTeacherRecordedClasses(authentication);
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            log.error("Erro ao buscar aulas gravadas do professor", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecordedClass> getRecordedClassById(@PathVariable Long id) {
        try {
            RecordedClass recordedClass = recordedClassService.getRecordedClassById(id);
            return ResponseEntity.ok(recordedClass);
        } catch (Exception e) {
            log.error("Erro ao buscar aula gravada", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

