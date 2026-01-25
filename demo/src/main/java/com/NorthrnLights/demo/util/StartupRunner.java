package com.NorthrnLights.demo.util;

import com.NorthrnLights.demo.domain.Role;
import com.NorthrnLights.demo.domain.Teacher;
import com.NorthrnLights.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void run(String... args) {
        log.info("🚀 Iniciando StartupRunner...");

        // Criar diretórios de upload
        createUploadDirectories();

        // Criar Teacher padrão
        createDefaultTeacher();

        log.info("✅ StartupRunner concluído.");
    }

    private void createUploadDirectories() {
        try {
            // Determinar o diretório base
            String baseDir;
            if (new File(uploadDir).isAbsolute()) {
                baseDir = uploadDir;
            } else {
                baseDir = System.getProperty("user.dir") + File.separator + uploadDir;
            }

            // Criar subdiretórios necessários
            String[] subDirs = {
                "questions",
                "answers",
                "corrections",
                "exams",
                "users"
            };

            for (String subDir : subDirs) {
                Path dirPath = Paths.get(baseDir, subDir);
                if (!Files.exists(dirPath)) {
                    Files.createDirectories(dirPath);
                    log.info("✅ Diretório criado: {}", dirPath.toAbsolutePath());
                } else {
                    log.debug("📁 Diretório já existe: {}", dirPath.toAbsolutePath());
                }
            }

            log.info("✅ Todos os diretórios de upload verificados/criados com sucesso!");
        } catch (Exception e) {
            log.error("❌ Erro ao criar diretórios de upload: {}", e.getMessage(), e);
            // Não lançar exceção para não impedir a inicialização da aplicação
        }
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
