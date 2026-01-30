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
                String userDir = System.getProperty("user.dir");
                baseDir = userDir + File.separator + uploadDir;
                
                // Verificar se podemos escrever no diretório
                File testDir = new File(baseDir);
                if (!testDir.exists()) {
                    File parentDir = testDir.getParentFile();
                    if (parentDir != null && !parentDir.canWrite()) {
                        // Se não puder escrever, usar /tmp como fallback
                        log.warn("⚠️ Não é possível escrever em {}. Usando /tmp como fallback.", baseDir);
                        baseDir = "/tmp" + File.separator + uploadDir;
                    }
                } else if (!testDir.canWrite()) {
                    log.warn("⚠️ Não é possível escrever em {}. Usando /tmp como fallback.", baseDir);
                    baseDir = "/tmp" + File.separator + uploadDir;
                }
            }

            log.info("📁 Diretório base de uploads: {}", baseDir);

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
                try {
                    if (!Files.exists(dirPath)) {
                        Files.createDirectories(dirPath);
                        log.info("✅ Diretório criado: {}", dirPath.toAbsolutePath());
                        
                        // Verificar se realmente foi criado e tem permissões
                        if (Files.exists(dirPath) && Files.isWritable(dirPath)) {
                            log.info("✅ Permissões verificadas para: {}", dirPath.toAbsolutePath());
                        } else {
                            log.error("❌ Diretório criado mas sem permissões de escrita: {}", dirPath.toAbsolutePath());
                        }
                    } else {
                        if (Files.isWritable(dirPath)) {
                            log.debug("📁 Diretório já existe e tem permissões: {}", dirPath.toAbsolutePath());
                        } else {
                            log.warn("⚠️ Diretório existe mas sem permissões de escrita: {}", dirPath.toAbsolutePath());
                        }
                    }
                } catch (Exception e) {
                    log.error("❌ Erro ao criar diretório {}: {}", subDir, e.getMessage(), e);
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
            
            // Verificar se o usuário já existe antes de criar
            if (userRepository.findByEmail(teacherEmail).isPresent()) {
                log.info("ℹ️ Teacher padrão já existe (email: {}). Pulando criação.", teacherEmail);
                return;
            }
            
            log.info("🔨 Criando Teacher padrão...");
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
            
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Se já existe (violação de constraint única), apenas logar
            log.info("ℹ️ Teacher padrão já existe no banco de dados. Pulando criação.");
        } catch (Exception e) {
            log.error("❌ Erro ao criar Teacher padrão: {}", e.getMessage(), e);
        }
    }
}
