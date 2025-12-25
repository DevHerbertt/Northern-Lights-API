package com.NorthrnLights.demo.service;

import com.NorthrnLights.demo.domain.Answer;
import com.NorthrnLights.demo.domain.Question;
import com.NorthrnLights.demo.domain.Student;
import com.NorthrnLights.demo.repository.AnswerRepository;
import com.NorthrnLights.demo.repository.QuestionRepository;
import com.NorthrnLights.demo.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final StudentRepository studentRepository;

    // Cria uma nova resposta
    public Answer createAnswer(String text, Long questionId, Long studentId, MultipartFile imageFile) throws IOException {
        log.info("🔍 DEBUG AnswerService: Criando resposta - questionId: {}, studentId: {}", questionId, studentId);
        
        // Busca a questão e o estudante
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> {
                    log.error("❌ Questão não encontrada: {}", questionId);
                    return new IllegalArgumentException("Question not found");
                });

        log.info("🔍 DEBUG AnswerService: Buscando estudante com ID: {}", studentId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> {
                    log.error("❌ Estudante não encontrado com ID: {}", studentId);
                    // Tentar listar todos os estudantes para debug
                    List<Student> allStudents = studentRepository.findAll();
                    log.error("❌ Estudantes disponíveis no banco: {}", 
                            allStudents.stream().map(s -> s.getId() + ":" + s.getEmail()).toList());
                    return new IllegalArgumentException("Student not found with ID: " + studentId);
                });
        
        log.info("✅ DEBUG AnswerService: Estudante encontrado - ID: {}, Email: {}", student.getId(), student.getEmail());

        // Verifica se a questão expirou
        if (question.getExpiresAt() != null && question.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            log.warn("⚠️ Tentativa de responder questão expirada. Question ID: {}, ExpiresAt: {}", 
                    question.getId(), question.getExpiresAt());
            throw new IllegalArgumentException("Esta questão já expirou e não pode mais ser respondida. Data de expiração: " + 
                    question.getExpiresAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
        
        // Verifica se o estudante já respondeu essa pergunta
        boolean studentHasAnswered = answerRepository.existsByQuestionIdAndStudentId(questionId, studentId);
        if (studentHasAnswered) {
            throw new IllegalArgumentException("Student has already answered this question");
        }

        // Criação da nova resposta
        Answer answer = new Answer();
        answer.setText(text);
        answer.setQuestion(question);
        answer.setStudent(student);
        answer.setCreatedAt(LocalDateTime.now()); // Definir data de criação explicitamente

        // Se houver imagem, salvar no sistema de arquivos
        if (imageFile != null && !imageFile.isEmpty()) {
            String projectDir = System.getProperty("user.dir");
            String folder = "uploads" + java.io.File.separator + "answers" + java.io.File.separator;
            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path path = Paths.get(projectDir, folder, fileName);
            Files.createDirectories(path.getParent());  // Cria o diretório, se não existir
            Files.copy(imageFile.getInputStream(), path); // Salva o arquivo no caminho
            // Retornar caminho relativo para servir via FileController
            answer.setImagePath("/uploads/answers/" + fileName);
            log.info("✅ Imagem de resposta salva: {}", path.toAbsolutePath());
        }

        return answerRepository.save(answer);  // Salva a resposta no banco de dados
    }

    // Atualizar uma resposta existente
    public Answer updateAnswer(Long answerId, String text, MultipartFile imageFile) throws IOException {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found"));

        answer.setText(text); // Atualiza o texto

        // Se houver imagem, salva no sistema de arquivos
        if (imageFile != null && !imageFile.isEmpty()) {
            String projectDir = System.getProperty("user.dir");
            String folder = "uploads" + java.io.File.separator + "answers" + java.io.File.separator;
            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path path = Paths.get(projectDir, folder, fileName);
            Files.createDirectories(path.getParent());  // Cria o diretório, se não existir
            Files.copy(imageFile.getInputStream(), path); // Salva o arquivo no caminho
            // Retornar caminho relativo para servir via FileController
            answer.setImagePath("/uploads/answers/" + fileName);
            log.info("✅ Imagem de resposta atualizada: {}", path.toAbsolutePath());
        }

        return answerRepository.save(answer); // Salva a resposta atualizada
    }

    // Buscar todas as respostas de uma pergunta específica
    public List<Answer> findAnswersByQuestionId(Long questionId) {
        return answerRepository.findByQuestionId(questionId);
    }

    // Buscar uma resposta específica pelo ID
    public Answer findAnswerById(Long id) {
        return answerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found"));
    }

    // Buscar todas as respostas
    public List<Answer> findAll() {
        return answerRepository.findAll();
    }
    
    // Obter quantidade de respostas
    public int getQuantity() {
        return answerRepository.findAll().size();
    }

    // Deletar uma resposta
    public void deleteAnswer(Long id) {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found"));

        answerRepository.delete(answer);
    }

    // Buscar estudante por ID (para validação)
    public Student findStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + id));
    }

    // Buscar respostas do estudante agrupadas por dia
    public List<Answer> findAnswersByStudentId(Long studentId) {
        return answerRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }
}
