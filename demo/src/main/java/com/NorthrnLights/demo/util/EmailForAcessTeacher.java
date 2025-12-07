package com.NorthrnLights.demo.util;

import com.NorthrnLights.demo.dto.TeacherDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Log4j2
@Service
public class EmailForAcessTeacher {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String remetente;

    public boolean sendEmail(TeacherDTO teacherDTO) {
        String email = teacherDTO.getEmail();
        if (email == null || email.trim().isEmpty() || !isValidEmail(email)) {
            log.error("E-mail inválido: {}", email);
            return false;
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
            return true;

        } catch (MessagingException e) {
            log.error("Erro ao enviar e-mail: {}", e.getMessage());
            return false;
        }
    }

    private String buildEmailContent(TeacherDTO teacherDTO) {
        String userName = teacherDTO.getUserName() != null ? teacherDTO.getUserName() : "Professor";
        String password = teacherDTO.getPassWord() != null ? teacherDTO.getPassWord() : "[senha não definida]";

        return String.format(
                "<html><body>" +
                        "<h2>Olá, %s!</h2>" +
                        "<p>Foi criado um professor para você no site <strong>NORTHERN LIGHTS</strong>.</p>" +
                        "<p><strong>Sua senha de acesso:</strong> %s</p>" +
                        "<p><em>Recomendamos que você mude esta senha após o primeiro login.</em></p>" +
                        "<p>Atenciosamente,<br>Equipe Northern Lights</p>" +
                        "</body></html>",
                userName, password
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
}
