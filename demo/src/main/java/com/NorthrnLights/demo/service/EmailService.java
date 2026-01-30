package com.NorthrnLights.demo.service;

import com.NorthrnLights.demo.dto.MeetEmailDTO;
import com.NorthrnLights.demo.dto.TeacherDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired(required = false)
    private SendGridEmailService sendGridEmailService;

    @Value("${spring.mail.username:}")
    private String remetente;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.port:}")
    private String mailPort;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${email.provider:smtp}")
    private String emailProvider; // smtp ou sendgrid

    @PostConstruct
    public void init() {
        log.info("=== INICIALIZANDO EmailService ===");
        log.info("DEBUG - Remetente configurado: {}", remetente != null && !remetente.isEmpty() ? remetente : "NÃO CONFIGURADO");
        log.info("DEBUG - Mail Host: {}", mailHost != null && !mailHost.isEmpty() ? mailHost : "NÃO CONFIGURADO");
        log.info("DEBUG - Mail Port: {}", mailPort != null && !mailPort.isEmpty() ? mailPort : "NÃO CONFIGURADO");
        
        // Verificar senha sem expor o valor
        if (mailPassword != null && !mailPassword.trim().isEmpty()) {
            log.info("DEBUG - Mail Password configurado: SIM");
            log.info("DEBUG - Mail Password tem espaços: {}", mailPassword.contains(" ") ? "SIM" : "NÃO");
            log.info("DEBUG - Mail Password tamanho: {} caracteres", mailPassword.length());
        } else {
            log.error("DEBUG - Mail Password configurado: NÃO CONFIGURADO");
        }
        
        log.info("DEBUG - JavaMailSender injetado: {}", javaMailSender != null ? "SIM" : "NÃO");
        
        if (javaMailSender == null) {
            log.error("ERRO CRÍTICO: JavaMailSender não foi injetado! Verifique se spring-boot-starter-mail está no classpath.");
        }
        
        if (remetente == null || remetente.trim().isEmpty()) {
            log.error("ERRO CRÍTICO: Remetente não configurado! Verifique MAIL_USERNAME");
        }
        
        if (mailPassword == null || mailPassword.trim().isEmpty()) {
            log.error("ERRO CRÍTICO: Senha de email não configurada! Verifique MAIL_PASSWORD");
        }
        
        // Verificar SendGrid
        if (sendGridEmailService != null && sendGridEmailService.isConfigured()) {
            log.info("✅ SendGrid está configurado e disponível como alternativa!");
        } else {
            log.info("ℹ️ SendGrid não está configurado (opcional)");
        }
        
        // Verificar se todas as configurações estão presentes
        boolean smtpConfigOk = (remetente != null && !remetente.trim().isEmpty()) &&
                          (mailPassword != null && !mailPassword.trim().isEmpty()) &&
                          (mailHost != null && !mailHost.trim().isEmpty()) &&
                          (mailPort != null && !mailPort.trim().isEmpty()) &&
                          (javaMailSender != null);
        
        boolean sendGridConfigOk = sendGridEmailService != null && sendGridEmailService.isConfigured();
        
        log.info("DEBUG - Email Provider configurado: {}", emailProvider);
        
        if (smtpConfigOk) {
            log.info("✅ Configurações SMTP estão presentes!");
        } else {
            log.warn("⚠️ Configurações SMTP estão faltando!");
        }
        
        if (sendGridConfigOk) {
            log.info("✅ SendGrid está configurado!");
        }
        
        if (!smtpConfigOk && !sendGridConfigOk) {
            log.error("❌ NENHUMA configuração de email está presente! Configure SMTP ou SendGrid.");
        }
        
        log.info("=== EmailService inicializado ===");
    }

    @Async
    public CompletableFuture<Boolean> sendEmailCreat(TeacherDTO teacherDTO) {
        log.debug("=== INÍCIO sendEmailCreat ===");
        String email = teacherDTO.getEmail();
        log.debug("DEBUG - Email recebido: {}", email);
        
        String assunto = "Acesso à sua conta como Professor - Northern Lights";
        String htmlContent = buildEmailContent(teacherDTO);
        
        return sendEmailGeneric(email, assunto, htmlContent);
    }

    @Async
    public CompletableFuture<Boolean> sendEmailUpdate(TeacherDTO teacherDTO) {
        log.debug("=== INÍCIO sendEmailUpdate ===");
        String email = teacherDTO.getEmail();
        log.debug("DEBUG - Email recebido: {}", email);
        
        String assunto = "Alterações na sua conta - Northern Lights";
        String htmlContent = buildEmailContent(teacherDTO);
        
        return sendEmailGeneric(email, assunto, htmlContent);
    }

    private String buildEmailContent(TeacherDTO teacherDTO) {
        String userName = teacherDTO.getUserName() != null ? teacherDTO.getUserName() : "Professor";
        String password = teacherDTO.getPassWord() != null ? teacherDTO.getPassWord() : "[senha não definida]";

        return String.format(
                "<html><body>" +
                        "<h2>Olá,Tudo bem ? %s!</h2>" +
                        "<p>O administrador fez alterações em sua conta na <strong>NORTHERN LIGHTS</strong> as %s.</p>" +
                        "<p><strong>Sua senha foi reiniciada:</strong> %s</p>" +
                        "<p><em>Recomendamos que você mude esta senha após o primeiro login.</em></p>" +
                        "<p><em>Link para acesso: </em></p>" +
                        "<p>Atenciosamente,<br>Equipe Northern Lights</p>" +
                        "</body></html>",
                userName,LocalDateTime.now(), password
        );
    }





    private boolean isValidEmail(String email) {
        // Validação simples de e-mail (padrão básico)
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    // Método para envio de e-mail de teste
    public boolean sendTestEmail(String testEmail) {
        log.debug("=== INÍCIO sendTestEmail ===");
        log.debug("DEBUG - Email de teste recebido: {}", testEmail);
        
        if (testEmail == null || testEmail.trim().isEmpty() || !isValidEmail(testEmail)) {
            log.error("E-mail de teste inválido: {}", testEmail);
            return false;
        }
        
        String assunto = "Teste de E-mail - Northern Lights";
        String texto = "Este é um e-mail de teste. Se você recebeu isso, o serviço de e-mail está funcionando!";
        
        try {
            CompletableFuture<Boolean> future = sendEmailGeneric(testEmail, assunto, texto);
            return future.get();
        } catch (Exception e) {
            log.error("❌ Erro ao enviar email de teste: {}", e.getMessage());
            log.error("DEBUG - Stack trace completo:", e);
            return false;
        }
    }

    @Async
    public CompletableFuture<Boolean> sendMeetEmail(MeetEmailDTO meetEmailDTO) {
        log.debug("=== INÍCIO sendMeetEmail ===");
        String email = meetEmailDTO.getEmail();
        log.debug("DEBUG - Email recebido: {}", email);
        
        String assunto = "Nova Aula Disponível - Northern Lights";
        String htmlContent = buildMeetEmailContent(meetEmailDTO);
        
        return sendEmailGeneric(email, assunto, htmlContent);
    }

    private String buildMeetEmailContent(MeetEmailDTO dto) {
        String userName = dto.getUserName() != null ? dto.getUserName() : "Aluno";
        String meetTitle = dto.getMeetTitle() != null ? dto.getMeetTitle() : "Nova Aula";
        String meetDescription = dto.getMeetDescription() != null ? dto.getMeetDescription() : "";
        String meetLink = dto.getMeetLink() != null ? dto.getMeetLink() : "";
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
        String startDate = dto.getMeetStartDate() != null ? 
            dto.getMeetStartDate().format(formatter) : "Data não informada";
        String endDate = dto.getMeetEndDate() != null ? 
            dto.getMeetEndDate().format(formatter) : "Data não informada";

        return String.format(
            "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<meta charset='UTF-8'>" +
            "<style>" +
            "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
            ".header { background: linear-gradient(135deg, #3b82f6, #8b5cf6); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
            ".content { background: #f8fafc; padding: 30px; border-radius: 0 0 10px 10px; }" +
            ".button { display: inline-block; background: #3b82f6; color: white; padding: 15px 30px; text-decoration: none; border-radius: 8px; margin: 20px 0; font-weight: bold; }" +
            ".button:hover { background: #2563eb; }" +
            ".info-box { background: white; padding: 20px; border-radius: 8px; margin: 15px 0; border-left: 4px solid #3b82f6; }" +
            ".footer { text-align: center; margin-top: 30px; color: #94a3b8; font-size: 0.9rem; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class='container'>" +
            "<div class='header'>" +
            "<h1>🌟 Northern Lights</h1>" +
            "<p>Nova Aula Disponível!</p>" +
            "</div>" +
            "<div class='content'>" +
            "<h2>Olá, %s! 👋</h2>" +
            "<p>Uma nova sala de aula foi criada e está disponível para você participar!</p>" +
            "<div class='info-box'>" +
            "<h3 style='margin-top: 0; color: #3b82f6;'>%s</h3>" +
            "%s" +
            "<p><strong>📅 Data e Hora de Início:</strong> %s</p>" +
            "<p><strong>📅 Data e Hora de Término:</strong> %s</p>" +
            "</div>" +
            "%s" +
            "<p style='margin-top: 30px; padding: 15px; background: rgba(59, 130, 246, 0.1); border-radius: 8px; border-left: 4px solid #3b82f6;'>" +
            "<strong>💡 Lembrete:</strong> Certifique-se de estar presente no horário agendado. " +
            "Após o término da aula, apenas a gravação estará disponível." +
            "</p>" +
            "<p style='margin-top: 20px;'>Não perca esta oportunidade de aprendizado!</p>" +
            "<p>Atenciosamente,<br><strong>Equipe Northern Lights</strong></p>" +
            "</div>" +
            "<div class='footer'>" +
            "<p>Este é um e-mail automático, por favor não responda.</p>" +
            "</div>" +
            "</div>" +
            "</body>" +
            "</html>",
            userName,
            meetTitle,
            meetDescription.isEmpty() ? "" : "<p>" + meetDescription + "</p>",
            startDate,
            endDate,
            meetLink.isEmpty() ? "" : 
                "<div style='text-align: center;'>" +
                "<a href='" + meetLink + "' class='button' target='_blank'>" +
                "🎥 Acessar Aula no Google Meet" +
                "</a>" +
                "</div>"
        );
    }

    @Async
    public CompletableFuture<Boolean> sendGradeEmail(String email, String subject, String htmlContent) {
        log.debug("=== INÍCIO sendGradeEmail ===");
        log.debug("DEBUG - Email recebido: {}", email);
        log.debug("DEBUG - Assunto recebido: {}", subject);
        
        return sendEmailGeneric(email, subject, htmlContent);
    }

    /**
     * Método auxiliar genérico para enviar emails com fallback automático
     * Tenta SMTP primeiro, depois SendGrid se SMTP falhar ou se SendGrid estiver configurado como provider principal
     */
    private CompletableFuture<Boolean> sendEmailGeneric(String toEmail, String subject, String htmlContent) {
        if (toEmail == null || toEmail.trim().isEmpty() || !isValidEmail(toEmail)) {
            log.error("E-mail inválido: {}", toEmail);
            return CompletableFuture.completedFuture(false);
        }

        // Se SendGrid está configurado como provider principal, usar diretamente
        if ("sendgrid".equalsIgnoreCase(emailProvider) && sendGridEmailService != null && sendGridEmailService.isConfigured()) {
            log.info("📧 Usando SendGrid como provider principal para: {}", toEmail);
            boolean success = sendGridEmailService.sendEmail(toEmail, subject, htmlContent);
            return CompletableFuture.completedFuture(success);
        }

        // Tentar SMTP primeiro
        if (javaMailSender != null && remetente != null && !remetente.trim().isEmpty()) {
            try {
                log.debug("DEBUG - Tentando enviar via SMTP...");
                MimeMessage mimeMessage = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
                helper.setFrom(remetente);
                helper.setTo(toEmail.trim());
                helper.setSubject(subject);
                helper.setText(htmlContent, true);

                log.info("DEBUG - Tentando enviar email para: {}", toEmail);
                javaMailSender.send(mimeMessage);
                log.info("✅ E-mail enviado com sucesso via SMTP para: {}", toEmail);
                return CompletableFuture.completedFuture(true);

            } catch (Exception e) {
                log.warn("⚠️ Falha ao enviar via SMTP para {}: {}", toEmail, e.getMessage());
                
                // Fallback para SendGrid se SMTP falhar
                if (sendGridEmailService != null && sendGridEmailService.isConfigured()) {
                    log.info("🔄 Tentando enviar via SendGrid (fallback) para: {}", toEmail);
                    boolean success = sendGridEmailService.sendEmail(toEmail, subject, htmlContent);
                    if (success) {
                        log.info("✅ E-mail enviado com sucesso via SendGrid (fallback) para: {}", toEmail);
                    }
                    return CompletableFuture.completedFuture(success);
                } else {
                    log.error("❌ Erro ao enviar e-mail para {}: {}", toEmail, e.getMessage());
                    log.error("DEBUG - Stack trace completo:", e);
                    return CompletableFuture.completedFuture(false);
                }
            }
        }

        // Se SMTP não está configurado, tentar SendGrid
        if (sendGridEmailService != null && sendGridEmailService.isConfigured()) {
            log.info("📧 SMTP não configurado, usando SendGrid para: {}", toEmail);
            boolean success = sendGridEmailService.sendEmail(toEmail, subject, htmlContent);
            return CompletableFuture.completedFuture(success);
        }

        log.error("❌ Nenhum método de envio de email configurado!");
        return CompletableFuture.completedFuture(false);
    }
}
