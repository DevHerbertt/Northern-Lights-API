package com.NorthrnLights.demo.controller;

import com.NorthrnLights.demo.dto.MeetEmailDTO;
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
        log.info("=== RECEBIDA REQUISIÇÃO: Enviar e-mail de teste ===");
        log.info("DEBUG - Email de destino: {}", email);
        log.info("DEBUG - Timestamp: {}", System.currentTimeMillis());

        try {
            boolean result = emailService.sendTestEmail(email);
            log.info("DEBUG - Resultado do envio: {}", result ? "SUCESSO" : "FALHA");

            if (result) {
                log.info("✅ Resposta: E-mail de teste enviado com sucesso!");
                return ResponseEntity.ok("E-mail de teste enviado com sucesso!");
            } else {
                log.error("❌ Resposta: Falha ao enviar e-mail de teste.");
                return ResponseEntity.badRequest().body("Falha ao enviar e-mail de teste. Verifique os logs para mais detalhes.");
            }
        } catch (Exception e) {
            log.error("❌ Exceção inesperada no controller ao enviar email de teste: {}", e.getMessage());
            log.error("DEBUG - Stack trace:", e);
            return ResponseEntity.internalServerError().body("Erro interno ao processar requisição de envio de email.");
        }
    }

    /**
     * Envia e-mail com informações da sala de aula (meet)
     */
    @PostMapping("/send-meet")
    public ResponseEntity<?> sendMeetEmail(@RequestBody MeetEmailDTO meetEmailDTO) {
        log.info("Recebida solicitação para enviar e-mail de aula para {}", meetEmailDTO.getEmail());

        CompletableFuture<Boolean> result = emailService.sendMeetEmail(meetEmailDTO);

        return ResponseEntity.ok("O e-mail está sendo enviado!");
    }
}
