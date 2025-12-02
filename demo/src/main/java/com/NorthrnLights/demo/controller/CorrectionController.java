package com.NorthrnLights.demo.controller;

import com.NorthrnLights.demo.domain.Correction;
import com.NorthrnLights.demo.dto.CorrectionDTO;
import com.NorthrnLights.demo.service.CorrectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/corrections")
@RequiredArgsConstructor
public class CorrectionController {

    private final CorrectionService correctionService;

    @PostMapping
    public ResponseEntity<Correction> create(@RequestBody CorrectionDTO dto, Authentication authentication) {
        // O teacher é obtido da autenticação, não do DTO
        Correction correction = correctionService.createCorrection(dto, authentication);
        return ResponseEntity.ok(correction);
    }

    @GetMapping
    public ResponseEntity<List<Correction>> getAll() {
        return ResponseEntity.ok(correctionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Correction> getById(@PathVariable Long id) {
        return ResponseEntity.ok(correctionService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Correction> update(@PathVariable Long id, @RequestBody CorrectionDTO dto, Authentication authentication) {
        Correction correction = correctionService.updateCorrection(id, dto, authentication);
        return ResponseEntity.ok(correction);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        correctionService.deleteCorrection(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-corrections")
    public ResponseEntity<List<Correction>> getMyCorrections(Authentication authentication) {
        // Endpoint para o teacher autenticado ver suas próprias correções
        List<Correction> corrections = correctionService.findByAuthenticatedTeacher(authentication);
        return ResponseEntity.ok(corrections);
    }

    @GetMapping("/answer/{answerId}")
    public ResponseEntity<List<Correction>> getByAnswer(@PathVariable Long answerId) {
        return ResponseEntity.ok(correctionService.findByAnswerId(answerId));
    }

    // Removido o endpoint /teacher/{teacherId} por questões de segurança
    // Ou mantenha com validação de permissões se necessário
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<Correction>> getByTeacher(@PathVariable Long teacherId, Authentication authentication) {
        // Só permite acesso se o teacherId for do usuário autenticado
        List<Correction> corrections = correctionService.findByTeacherId(teacherId, authentication);
        return ResponseEntity.ok(corrections);
    }
}