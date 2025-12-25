package com.NorthrnLights.demo.controller;

import com.NorthrnLights.demo.domain.Meet;
import com.NorthrnLights.demo.dto.MeetDTO;
import com.NorthrnLights.demo.service.MeetServiceImpl;
import com.NorthrnLights.demo.service.EmailService;
import com.NorthrnLights.demo.service.StudentService;
import com.NorthrnLights.demo.domain.Student;
import com.NorthrnLights.demo.dto.MeetEmailDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CompletableFuture;

// MeetController.java - ATUALIZADO
@RestController
@RequestMapping("/meets")
@RequiredArgsConstructor
@Slf4j
public class MeetController {

    private final MeetServiceImpl meetService;
    private final EmailService emailService;
    private final StudentService studentService;

    @PostMapping
    public ResponseEntity<Meet> create(@RequestBody MeetDTO dto, Authentication authentication) {
        log.info("📝 Criando nova sala de aula - Link: {}", dto.getLinkOfMeet());
        log.info("📝 dateTimeStart recebido: {}", dto.getDateTimeStart());
        log.info("📝 dateTimeEnd recebido: {}", dto.getDateTimeEnd());
        
        Meet meet = meetService.create(dto);
        log.info("✅ Sala de aula criada com sucesso - ID: {}", meet.getId());
        
        // Enviar email automaticamente para todos os alunos
        try {
            List<Student> students = studentService.findAll();
            log.info("📧 Enviando emails de notificação para {} alunos sobre a nova sala de aula", students.size());
            
            if (students.isEmpty()) {
                log.warn("⚠️ Nenhum aluno cadastrado para enviar email");
            } else {
                for (Student student : students) {
                    try {
                        MeetEmailDTO emailDTO = new MeetEmailDTO();
                        emailDTO.setEmail(student.getEmail());
                        emailDTO.setUserName(student.getUserName() != null ? student.getUserName() : 
                            (student.getEmail() != null ? student.getEmail().split("@")[0] : "Aluno"));
                        emailDTO.setMeetTitle("Nova Aula Disponível - Northern Lights");
                        emailDTO.setMeetDescription("Uma nova sala de aula foi criada e está disponível para você! Acesse o link abaixo para participar.");
                        emailDTO.setMeetLink(meet.getLinkOfMeet() != null ? meet.getLinkOfMeet() : meet.getLinkRecordClass());
                        emailDTO.setMeetStartDate(meet.getDateTimeStart());
                        emailDTO.setMeetEndDate(meet.getDateTimeEnd());
                        
                        CompletableFuture<Boolean> emailResult = emailService.sendMeetEmail(emailDTO);
                        emailResult.thenAccept(success -> {
                            if (success) {
                                log.info("✅ Email enviado com sucesso para: {}", student.getEmail());
                            } else {
                                log.warn("⚠️ Falha ao enviar email para: {}", student.getEmail());
                            }
                        });
                    } catch (Exception e) {
                        log.error("❌ Erro ao preparar email para {}: {}", student.getEmail(), e.getMessage());
                    }
                }
                
                log.info("✅ Processo de envio de emails iniciado para {} alunos", students.size());
            }
        } catch (Exception e) {
            log.error("❌ Erro ao enviar emails automaticamente: {}", e.getMessage(), e);
            // Não falhar a criação do meet se o email falhar
        }
        
        return ResponseEntity.ok(meet);
    }

    @GetMapping
    public ResponseEntity<List<Meet>> findWithFilters(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        return ResponseEntity.ok(meetService.findWithFilters(id, startDate, endDate));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Meet> getById(@PathVariable Long id) {
        return ResponseEntity.of(meetService.getAllMeets()
                .stream()
                .filter(m -> m.getId().equals(id))
                .findFirst());
    }

    @PutMapping("/{id}/presentCount")
    public ResponseEntity<Meet> updatePresentCount(@PathVariable Long id, @RequestParam int newCount) {
        return ResponseEntity.ok(meetService.updatePresentCount(id, newCount));
    }

    @GetMapping("/quantity")
    public ResponseEntity<Integer> getQuantityMeets() {
        return ResponseEntity.ok(meetService.getQuantityMeets());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Meet> updateMeet(@PathVariable Long id, @RequestBody MeetDTO dto) {
        return ResponseEntity.ok(meetService.updateMeet(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        meetService.deleteMeet(id);
        return ResponseEntity.noContent().build();
    }
}