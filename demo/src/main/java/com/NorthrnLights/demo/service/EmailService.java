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

    @Value("${spring.mail.username}")
    private String remetente;

    @Async
    public CompletableFuture<Boolean> sendEmailCreat(TeacherDTO teacherDTO) {
        String email = teacherDTO.getEmail();
        if (email == null || email.trim().isEmpty() || !isValidEmail(email)) {
            log.error("E-mail inválido: {}", email);
            return CompletableFuture.completedFuture(false);
        }

        try {
            // Criar e configurar o MimeMessage
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            // Remetente
            helper.setFrom(remetente);

            // Destinatário
            helper.setTo(email.trim());

            // Assunto
            helper.setSubject("Acesso à sua conta como Professor - Northern Lights");

            // Corpo HTML
            String htmlContent = buildEmailContent(teacherDTO);
            helper.setText(htmlContent, true);

            // Enviar
            javaMailSender.send(mimeMessage);

            log.info("E-mail enviado com sucesso para: {}", email);
            return CompletableFuture.completedFuture(true);

        } catch (MessagingException e) {
            log.error("Erro ao enviar e-mail: {}", e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    @Async
    public CompletableFuture<Boolean> sendEmailUpdate(TeacherDTO teacherDTO) {
        String email = teacherDTO.getEmail();
        if (email == null || email.trim().isEmpty() || !isValidEmail(email)) {
            log.error("E-mail inválido: {}", email);
            return CompletableFuture.completedFuture(false);
        }

        try {
            // Criar e configurar o MimeMessage
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            // Remetente
            helper.setFrom(remetente);

            // Destinatário
            helper.setTo(email.trim());

            // Assunto
            helper.setSubject("Alterações na sua conta - Northern Lights");

            // Corpo HTML
            String htmlContent = buildEmailContent(teacherDTO);
            helper.setText(htmlContent, true);

            // Enviar
            javaMailSender.send(mimeMessage);

            log.info("E-mail enviado com sucesso para: {}", email);
            return CompletableFuture.completedFuture(true);

        } catch (MessagingException e) {
            log.error("Erro ao enviar e-mail: {}", e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
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
        if (testEmail == null || testEmail.trim().isEmpty() || !isValidEmail(testEmail)) {
            log.error("E-mail de teste inválido: {}", testEmail);
            return false;
        }

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            helper.setFrom(remetente);
            helper.setTo(testEmail.trim());
            helper.setSubject("Teste de E-mail - Northern Lights");
            helper.setText("Este é um e-mail de teste. Se você recebeu isso, o serviço de e-mail está funcionando!", false);

            javaMailSender.send(mimeMessage);
            log.info("E-mail de teste enviado com sucesso!");
            return true;

        } catch (Exception e) {
            log.error("Erro no envio do e-mail de teste: {}", e.getMessage());
            return false;
        }
    }

    @Async
    public CompletableFuture<Boolean> sendMeetEmail(MeetEmailDTO meetEmailDTO) {
        String email = meetEmailDTO.getEmail();
        if (email == null || email.trim().isEmpty() || !isValidEmail(email)) {
            log.error("E-mail inválido: {}", email);
            return CompletableFuture.completedFuture(false);
        }

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            helper.setFrom(remetente);
            helper.setTo(email.trim());
            helper.setSubject("Nova Aula Disponível - Northern Lights");

            String htmlContent = buildMeetEmailContent(meetEmailDTO);
            helper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);
            log.info("E-mail de aula enviado com sucesso para: {}", email);
            return CompletableFuture.completedFuture(true);

        } catch (MessagingException e) {
            log.error("Erro ao enviar e-mail de aula: {}", e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
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
        if (email == null || email.trim().isEmpty() || !isValidEmail(email)) {
            log.error("E-mail inválido: {}", email);
            return CompletableFuture.completedFuture(false);
        }

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            helper.setFrom(remetente);
            helper.setTo(email.trim());
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);
            log.info("E-mail de nota enviado com sucesso para: {}", email);
            return CompletableFuture.completedFuture(true);

        } catch (MessagingException e) {
            log.error("Erro ao enviar e-mail de nota: {}", e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }
}
