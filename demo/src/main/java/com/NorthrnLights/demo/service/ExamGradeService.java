package com.NorthrnLights.demo.service;

import com.NorthrnLights.demo.domain.*;
import com.NorthrnLights.demo.dto.ExamGradeDTO;
import com.NorthrnLights.demo.repository.*;
import com.NorthrnLights.demo.util.GradeCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamGradeService {

    private final ExamGradeRepository examGradeRepository;
    private final StudentRepository studentRepository;
    private final ExamRepository examRepository;
    private final TeacherRepository teacherRepository;
    private final EmailService emailService;

    @Transactional
    public ExamGrade createExamGrade(ExamGradeDTO dto, Authentication authentication) {
        log.info("Criando nota de prova para estudante ID: {}, Prova ID: {}", dto.getStudentId(), dto.getExamId());

        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudante não encontrado"));

        Teacher teacher = getAuthenticatedTeacher(authentication);

        // Exam pode ser null - se não fornecido, criar nota de prova geral
        Exam exam = null;
        if (dto.getExamId() != null) {
            exam = examRepository.findById(dto.getExamId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prova não encontrada"));
        }

        // Calcular grade se não fornecido
        Grade grade = dto.getGrade();
        if (grade == null && dto.getPointsObtained() != null && dto.getTotalPoints() != null) {
            grade = GradeCalculator.calculateGradeFromPoints(dto.getPointsObtained(), dto.getTotalPoints());
        }

        // Verificar se já existe nota para esta prova (ou nota geral se examId for null)
        Optional<ExamGrade> existing = Optional.empty();
        if (exam != null) {
            existing = examGradeRepository.findByStudentIdAndExamId(student.getId(), exam.getId());
        } else {
            // Para notas gerais sem prova específica, buscar a mais recente do estudante sem examId
            List<ExamGrade> studentGrades = examGradeRepository.findByStudentIdOrderByCreatedAtDesc(student.getId());
            existing = studentGrades.stream()
                    .filter(eg -> eg.getExam() == null)
                    .findFirst();
        }
        
        ExamGrade examGrade;
        if (existing.isPresent()) {
            // Atualizar nota existente
            examGrade = existing.get();
            examGrade.setPointsObtained(dto.getPointsObtained());
            examGrade.setTotalPoints(dto.getTotalPoints());
            examGrade.setGrade(grade);
            examGrade.setFeedback(dto.getFeedback());
            if (exam != null) {
                examGrade.setExam(exam);
            }
            log.info("Atualizando nota de prova existente ID: {}", examGrade.getId());
        } else {
            // Criar nova nota
            examGrade = ExamGrade.builder()
                    .pointsObtained(dto.getPointsObtained())
                    .totalPoints(dto.getTotalPoints())
                    .grade(grade)
                    .feedback(dto.getFeedback())
                    .student(student)
                    .exam(exam) // Pode ser null
                    .teacher(teacher)
                    .createdAt(LocalDateTime.now())
                    .build();
            log.info("Criando nova nota de prova (examId: {})", exam != null ? exam.getId() : "null");
        }

        ExamGrade saved = examGradeRepository.save(examGrade);

        // Enviar email de forma assíncrona
        sendExamGradeEmailAsync(student, saved, exam);

        return saved;
    }

    @Async
    private void sendExamGradeEmailAsync(Student student, ExamGrade examGrade, Exam exam) {
        try {
            String studentEmail = student.getEmail();
            if (studentEmail == null || studentEmail.trim().isEmpty()) {
                log.warn("Estudante {} não tem email cadastrado, não será enviado email", student.getId());
                return;
            }

            String studentName = student.getUserName() != null ? student.getUserName() : "Aluno";
            String gradeDisplay = GradeCalculator.formatGradeWithClassification(
                    examGrade.getPointsObtained(),
                    examGrade.getTotalPoints(),
                    examGrade.getGrade()
            );
            String feedback = examGrade.getFeedback() != null ? examGrade.getFeedback() : "";
            String examTitle = exam != null && exam.getTitle() != null ? exam.getTitle() : "Prova Geral";

            String subject = String.format("Nota da Prova: %s", gradeDisplay);
            String htmlContent = buildExamGradeEmailContent(studentName, gradeDisplay, examTitle, feedback);

            emailService.sendGradeEmail(studentEmail, subject, htmlContent);
            log.info("Email de nota de prova enviado para estudante ID: {}", student.getId());

        } catch (Exception e) {
            log.error("Erro ao enviar email de nota de prova para estudante ID: {}", student.getId(), e);
        }
    }

    private String buildExamGradeEmailContent(String studentName, String gradeDisplay, String examTitle, String feedback) {
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
            ".grade-box { background: white; padding: 30px; border-radius: 12px; margin: 20px 0; text-align: center; border: 3px solid #3b82f6; }" +
            ".grade-value { font-size: 48px; font-weight: 800; background: linear-gradient(135deg, #3b82f6, #8b5cf6); background-clip: text; -webkit-background-clip: text; -webkit-text-fill-color: transparent; margin: 10px 0; }" +
            ".exam-info { background: rgba(59, 130, 246, 0.1); padding: 15px; border-radius: 8px; margin: 15px 0; border-left: 4px solid #3b82f6; }" +
            ".feedback-box { background: rgba(139, 92, 246, 0.1); padding: 20px; border-radius: 8px; margin: 15px 0; border-left: 4px solid #8b5cf6; }" +
            ".footer { text-align: center; margin-top: 30px; color: #94a3b8; font-size: 0.9rem; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class='container'>" +
            "<div class='header'>" +
            "<h1>🌟 Northern Lights</h1>" +
            "<p>Sua Nota da Prova</p>" +
            "</div>" +
            "<div class='content'>" +
            "<h2>Olá, %s! 👋</h2>" +
            "<p>Seu professor avaliou sua prova e atribuiu uma nota.</p>" +
            "<div class='grade-box'>" +
            "<p style='margin: 0; color: #64748b; font-size: 14px; text-transform: uppercase; letter-spacing: 1px;'>Nota da Prova</p>" +
            "<div class='grade-value'>%s</div>" +
            "</div>" +
            "<div class='exam-info'>" +
            "<p style='margin: 0;'><strong><i class='fas fa-book'></i> Prova:</strong> %s</p>" +
            "</div>" +
            "%s" +
            "<p style='margin-top: 30px; padding: 15px; background: rgba(59, 130, 246, 0.1); border-radius: 8px; border-left: 4px solid #3b82f6;'>" +
            "<strong>💡 Continue estudando!</strong> Seu esforço e dedicação são fundamentais para seu aprendizado." +
            "</p>" +
            "<p style='margin-top: 20px;'>Acesse sua área de estudante para ver mais detalhes sobre suas notas.</p>" +
            "<p>Atenciosamente,<br><strong>Equipe Northern Lights</strong></p>" +
            "</div>" +
            "<div class='footer'>" +
            "<p>Este é um e-mail automático, por favor não responda.</p>" +
            "</div>" +
            "</div>" +
            "</body>" +
            "</html>",
            studentName,
            gradeDisplay,
            examTitle,
            feedback.isEmpty() ? "" : 
                "<div class='feedback-box'>" +
                "<h3 style='margin-top: 0; color: #8b5cf6;'>📝 Feedback do Professor:</h3>" +
                "<p>" + feedback + "</p>" +
                "</div>"
        );
    }

    public List<ExamGrade> getStudentExamGrades(Long studentId) {
        return examGradeRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    public List<ExamGrade> getExamGrades(Long examId) {
        return examGradeRepository.findByExamId(examId);
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

