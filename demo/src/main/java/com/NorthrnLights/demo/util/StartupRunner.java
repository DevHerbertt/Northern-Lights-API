package com.NorthrnLights.demo.util;

import com.NorthrnLights.demo.domain.Role;
import com.NorthrnLights.demo.domain.Teacher;
import com.NorthrnLights.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(2) // Executar após DatabaseMigrationRunner
public class StartupRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WeeklyGradeRepository weeklyGradeRepository;
    private final CorrectionRepository correctionRepository;
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;
    private final MeetRepository meetRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        log.info("🚀 Iniciando StartupRunner...");

        // Criar Teacher padrão
        createDefaultTeacher();

        log.info("✅ StartupRunner concluído.");
    }

    private void createDefaultTeacher() {
        try {
            String teacherEmail = "teacher@school.com";
            String teacherPassword = "senhaForte123";
            String teacherName = "TEACHER DEFAULT";
            
            Teacher teacher = Teacher.builder()
                    .email(teacherEmail)
                    .password(passwordEncoder.encode(teacherPassword))
                    .userName(teacherName)
                    .age(27)
                    .role(Role.TEACHER)
                    .build();
            
            userRepository.save(teacher);
            
            log.info("✅ Teacher padrão criado com sucesso!");
            log.info("📧 Email: {}", teacherEmail);
            log.info("🔑 Senha: {}", teacherPassword);
            log.info("👤 Nome: {}", teacherName);
            
        } catch (Exception e) {
            log.error("❌ Erro ao criar Teacher padrão: {}", e.getMessage(), e);
        }
    }
}
