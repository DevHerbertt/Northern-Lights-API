package com.NorthrnLights.demo.controller;

import com.NorthrnLights.demo.dto.TeacherDTO;
import com.NorthrnLights.demo.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
@Log4j2
public class EmailController {

    private final EmailService emailService;

    /**
     * Envia e-mail real com dados do professor
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendEmail(@RequestBody TeacherDTO teacherDTO) {
        log.info("Recebida solicitação para enviar e-mail para {}", teacherDTO.getEmail());

        CompletableFuture<Boolean> result = emailService.sendEmailCreat(teacherDTO);

        return ResponseEntity.ok("O e-mail está sendo enviado!");
    }

    /**
     * Envia um e-mail simples de teste
     */
    @PostMapping("/test")
    public ResponseEntity<?> sendTestEmail(@RequestParam String email) {
        log.info("Enviando e-mail de teste para {}", email);

        boolean result = emailService.sendTestEmail(email);

        if (result) {
            return ResponseEntity.ok("E-mail de teste enviado com sucesso!");
        } else {
            return ResponseEntity.badRequest().body("Falha ao enviar e-mail de teste.");
        }
    }
}
