package com.NorthrnLights.demo.service;

import com.NorthrnLights.demo.domain.*;
import com.NorthrnLights.demo.dto.WeeklyGradeDTO;
import com.NorthrnLights.demo.repository.*;
import com.NorthrnLights.demo.util.GradeCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklyGradeService {

    private final WeeklyGradeRepository weeklyGradeRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final EmailService emailService;

    public WeeklyGrade createWeeklyGrade(WeeklyGradeDTO dto, Authentication authentication) {
        log.info("Criando nota semanal para estudante ID: {}", dto.getStudentId());

        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudante não encontrado"));

        Teacher teacher = getAuthenticatedTeacher(authentication);

        // Calcular início da semana (segunda-feira)
        LocalDate weekStart = getWeekStartDate(dto.getWeekStartDate() != null ? dto.getWeekStartDate() : LocalDate.now());

        // Calcular grade se pontos foram fornecidos mas grade não
        Grade grade = dto.getGrade();
        if (grade == null && dto.getPointsObtained() != null && dto.getTotalPoints() != null) {
            grade = GradeCalculator.calculateGradeFromPoints(dto.getPointsObtained(), dto.getTotalPoints());
        }

        // Verificar se já existe nota para esta semana
        Optional<WeeklyGrade> existing = weeklyGradeRepository.findByStudentIdAndWeekStartDate(
                student.getId(), weekStart);
        
        WeeklyGrade weeklyGrade;
        if (existing.isPresent()) {
            // Atualizar nota existente
            weeklyGrade = existing.get();
            if (dto.getPointsObtained() != null) weeklyGrade.setPointsObtained(dto.getPointsObtained());
            if (dto.getTotalPoints() != null) weeklyGrade.setTotalPoints(dto.getTotalPoints());
            if (grade != null) weeklyGrade.setGrade(grade);
            if (dto.getFeedback() != null) weeklyGrade.setFeedback(dto.getFeedback());
            log.info("Atualizando nota semanal existente ID: {}", weeklyGrade.getId());
        } else {
            // Criar nova nota
            weeklyGrade = WeeklyGrade.builder()
                    .pointsObtained(dto.getPointsObtained())
                    .totalPoints(dto.getTotalPoints())
                    .grade(grade != null ? grade : dto.getGrade())
                    .feedback(dto.getFeedback())
                    .student(student)
                    .teacher(teacher)
                    .weekStartDate(weekStart)
                    .createdAt(LocalDateTime.now())
                    .build();
            log.info("Criando nova nota semanal");
        }

        WeeklyGrade saved = weeklyGradeRepository.save(weeklyGrade);

        // Enviar email de forma assíncrona
        sendGradeEmailAsync(student, saved);

        return saved;
    }

    @Async
    private void sendGradeEmailAsync(Student student, WeeklyGrade weeklyGrade) {
        try {
            String studentEmail = student.getEmail();
            if (studentEmail == null || studentEmail.trim().isEmpty()) {
                log.warn("Estudante {} não tem email cadastrado, não será enviado email", student.getId());
                return;
            }

            String studentName = student.getUserName() != null ? student.getUserName() : "Aluno";
            String weekInfo = formatWeek(weeklyGrade.getWeekStartDate());

            String gradeDisplay;
            if (weeklyGrade.getPointsObtained() != null && weeklyGrade.getTotalPoints() != null) {
                gradeDisplay = GradeCalculator.formatGradeWithClassification(
                        weeklyGrade.getPointsObtained(),
                        weeklyGrade.getTotalPoints(),
                        weeklyGrade.getGrade()
                );
            } else {
                gradeDisplay = formatGrade(weeklyGrade.getGrade());
            }

            String subject = String.format("Nota da Lição da Semana: %s", gradeDisplay);
            String htmlContent = buildGradeEmailContent(studentName, weeklyGrade, weekInfo);

            emailService.sendGradeEmail(studentEmail, subject, htmlContent);
            log.info("Email de nota enviado para estudante ID: {}", student.getId());

        } catch (Exception e) {
            log.error("Erro ao enviar email de nota para estudante ID: {}", student.getId(), e);
        }
    }

    private String buildGradeEmailContent(String studentName, WeeklyGrade weeklyGrade, String weekInfo) {
        String gradeDisplay;
        if (weeklyGrade.getPointsObtained() != null && weeklyGrade.getTotalPoints() != null) {
            gradeDisplay = GradeCalculator.formatGradeWithClassification(
                    weeklyGrade.getPointsObtained(),
                    weeklyGrade.getTotalPoints(),
                    weeklyGrade.getGrade()
            );
        } else {
            gradeDisplay = formatGrade(weeklyGrade.getGrade());
        }
        String feedback = weeklyGrade.getFeedback() != null ? weeklyGrade.getFeedback() : "";
        
        return buildGradeEmailContentHtml(studentName, gradeDisplay, feedback, weekInfo);
    }
    
    private String buildGradeEmailContentHtml(String studentName, String gradeDisplay, String feedback, String weekInfo) {
        return String.format(
            "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<meta charset='UTF-8'>" +
            "<style>" +
            "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
            ".header { background: linear-gradient(135deg, #14b8a6, #3b82f6); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
            ".content { background: #f8fafc; padding: 30px; border-radius: 0 0 10px 10px; }" +
            ".grade-box { background: white; padding: 30px; border-radius: 12px; margin: 20px 0; text-align: center; border: 3px solid #14b8a6; }" +
            ".grade-value { font-size: 48px; font-weight: bold; color: #14b8a6; margin: 10px 0; }" +
            ".week-info { background: rgba(59, 130, 246, 0.1); padding: 15px; border-radius: 8px; margin: 15px 0; border-left: 4px solid #3b82f6; }" +
            ".feedback-box { background: white; padding: 20px; border-radius: 8px; margin: 15px 0; border-left: 4px solid #8b5cf6; }" +
            ".footer { text-align: center; margin-top: 30px; color: #94a3b8; font-size: 0.9rem; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class='container'>" +
            "<div class='header'>" +
            "<h1>🌟 Northern Lights</h1>" +
            "<p>Sua Nota da Lição da Semana</p>" +
            "</div>" +
            "<div class='content'>" +
            "<h2>Olá, %s! 👋</h2>" +
            "<p>Seu professor avaliou seu desempenho da semana e atribuiu uma nota.</p>" +
            "<div class='grade-box'>" +
            "<p style='margin: 0; color: #64748b; font-size: 14px; text-transform: uppercase; letter-spacing: 1px;'>Nota da Lição da Semana</p>" +
            "<div class='grade-value'>%s</div>" +
            "</div>" +
            "<div class='week-info'>" +
            "<p style='margin: 0;'><strong>📅 Semana:</strong> %s</p>" +
            "</div>" +
            "%s" +
            "<p style='margin-top: 30px; padding: 15px; background: rgba(20, 184, 166, 0.1); border-radius: 8px; border-left: 4px solid #14b8a6;'>" +
            "<strong>💡 Continue assim!</strong> Seu esforço e dedicação são fundamentais para seu aprendizado." +
            "</p>" +
            "<p style='margin-top: 20px;'>Acesse sua área de estudante para ver mais detalhes sobre suas notas e correções.</p>" +
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
            weekInfo,
            feedback.isEmpty() ? "" : 
                "<div class='feedback-box'>" +
                "<h3 style='margin-top: 0; color: #8b5cf6;'>📝 Feedback do Professor:</h3>" +
                "<p>" + feedback + "</p>" +
                "</div>"
        );
    }

    private String formatGrade(Grade grade) {
        if (grade == null) return "N/A";
        return grade.toString(); // Usa o método toString() do enum que retorna A+, B+, etc.
    }

    private String formatWeek(LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        return String.format("%02d/%02d/%04d a %02d/%02d/%04d",
                weekStart.getDayOfMonth(), weekStart.getMonthValue(), weekStart.getYear(),
                weekEnd.getDayOfMonth(), weekEnd.getMonthValue(), weekEnd.getYear());
    }

    private LocalDate getWeekStartDate(LocalDate date) {
        // Retorna a segunda-feira da semana
        int daysToSubtract = date.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
        if (daysToSubtract < 0) {
            daysToSubtract += 7;
        }
        return date.minusDays(daysToSubtract);
    }

    public List<WeeklyGrade> getStudentWeeklyGrades(Long studentId) {
        return weeklyGradeRepository.findByStudentIdOrderByWeekStartDateDesc(studentId);
    }

    public Optional<WeeklyGrade> getCurrentWeekGrade(Long studentId) {
        LocalDate currentWeekStart = getWeekStartDate(LocalDate.now());
        return weeklyGradeRepository.findByStudentIdAndWeekStartDate(studentId, currentWeekStart);
    }

    public Optional<WeeklyGrade> getLatestGrade(Long studentId) {
        return weeklyGradeRepository.findFirstByStudentIdOrderByWeekStartDateDesc(studentId);
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


