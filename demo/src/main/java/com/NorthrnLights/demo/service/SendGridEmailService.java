package com.NorthrnLights.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serviço de email usando SendGrid API REST (porta 443, não bloqueada pelo Render)
 * Alternativa ao SMTP que funciona no Render
 */
@Log4j2
@Service
public class SendGridEmailService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${sendgrid.api.key:}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email:}")
    private String sendGridFromEmail;

    @Value("${sendgrid.from.name:Northern Lights}")
    private String sendGridFromName;

    /**
     * Verifica se SendGrid está configurado
     */
    public boolean isConfigured() {
        return sendGridApiKey != null && !sendGridApiKey.trim().isEmpty() &&
               sendGridFromEmail != null && !sendGridFromEmail.trim().isEmpty();
    }

    /**
     * Envia email usando SendGrid API
     * @param toEmail Email do destinatário
     * @param subject Assunto do email
     * @param htmlContent Conteúdo HTML do email
     * @return true se enviado com sucesso, false caso contrário
     */
    public boolean sendEmail(String toEmail, String subject, String htmlContent) {
        log.debug("=== INÍCIO SendGrid sendEmail ===");
        log.debug("DEBUG - Email destinatário: {}", toEmail);
        log.debug("DEBUG - Assunto: {}", subject);
        
        if (!isConfigured()) {
            log.warn("⚠️ SendGrid não está configurado. Configure SENDGRID_API_KEY e SENDGRID_FROM_EMAIL");
            return false;
        }

        try {
            String url = "https://api.sendgrid.com/v3/mail/send";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(sendGridApiKey);
            
            Map<String, Object> emailData = new HashMap<>();
            
            // From
            Map<String, String> from = new HashMap<>();
            from.put("email", sendGridFromEmail);
            from.put("name", sendGridFromName);
            emailData.put("from", from);
            
            // Personalizations (To)
            Map<String, Object> personalization = new HashMap<>();
            List<Map<String, String>> toList = new ArrayList<>();
            Map<String, String> to = new HashMap<>();
            to.put("email", toEmail);
            toList.add(to);
            personalization.put("to", toList);
            emailData.put("personalizations", List.of(personalization));
            
            // Subject
            emailData.put("subject", subject);
            
            // Content
            List<Map<String, String>> contentList = new ArrayList<>();
            Map<String, String> content = new HashMap<>();
            content.put("type", "text/html");
            content.put("value", htmlContent);
            contentList.add(content);
            emailData.put("content", contentList);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(emailData, headers);
            
            log.info("DEBUG - Enviando email via SendGrid API para: {}", toEmail);
            log.debug("DEBUG - SendGrid From: {} ({})", sendGridFromEmail, sendGridFromName);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Email enviado com sucesso via SendGrid para: {}", toEmail);
                return true;
            } else {
                log.error("❌ SendGrid retornou status {}: {}", response.getStatusCode(), response.getBody());
                return false;
            }
            
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("❌ Erro HTTP ao enviar email via SendGrid para {}: Status={}, Body={}", 
                toEmail, e.getStatusCode(), e.getResponseBodyAsString());
            log.error("DEBUG - Stack trace completo:", e);
            return false;
        } catch (Exception e) {
            log.error("❌ Erro ao enviar email via SendGrid para {}: {}", toEmail, e.getMessage());
            log.error("DEBUG - Stack trace completo:", e);
            return false;
        }
    }
}

