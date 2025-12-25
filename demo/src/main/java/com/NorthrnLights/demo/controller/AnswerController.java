package com.NorthrnLights.demo.controller;

import com.NorthrnLights.demo.domain.Answer;
import com.NorthrnLights.demo.domain.Student;
import com.NorthrnLights.demo.service.AnswerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/answers")
@RequiredArgsConstructor
@Slf4j
public class AnswerController {

    private final AnswerService answerService;

    // Criar uma resposta
    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Answer> createAnswer(
            @RequestParam("text") String text,           // O texto da resposta
            @RequestParam("questionId") Long questionId, // O ID da questão associada
            @RequestParam("studentId") Long studentId,   // O ID do estudante que está respondendo
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Authentication authentication) throws IOException { // O arquivo de imagem (opcional)

        log.info("🔍 DEBUG AnswerController: Recebendo POST /answers");
        log.info("🔍 DEBUG AnswerController: Authentication: {}", authentication != null ? "present" : "null");
        log.info("🔍 DEBUG AnswerController: studentId recebido: {}", studentId);
        
        if (authentication == null || authentication.getPrincipal() == null) {
            log.error("❌ Authentication é null ou principal é null");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }
        
        Object principal = authentication.getPrincipal();
        log.info("🔍 DEBUG AnswerController: Principal type: {}", principal.getClass().getSimpleName());
        log.info("🔍 DEBUG AnswerController: Authorities: {}", authentication.getAuthorities());
        
        // Usar o Student do Authentication ao invés do studentId do frontend
        if (principal instanceof Student) {
            Student authenticatedStudent = (Student) principal;
            log.info("🔍 DEBUG AnswerController: Student ID do principal: {}", authenticatedStudent.getId());
            
            // Validar que o studentId fornecido corresponde ao estudante autenticado
            if (!authenticatedStudent.getId().equals(studentId)) {
                log.warn("⚠️ Tentativa de criar resposta com studentId diferente do autenticado. Autenticado: {}, Fornecido: {}", 
                        authenticatedStudent.getId(), studentId);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                        "Você só pode criar respostas para seu próprio perfil");
            }
            
            // Usar o ID do Student autenticado
            studentId = authenticatedStudent.getId();
        } else if (principal instanceof com.NorthrnLights.demo.domain.Teacher) {
            // Se o principal é Teacher, não pode criar respostas como estudante
            // Professores podem criar respostas para testes, mas precisam fornecer um studentId válido
            log.warn("⚠️ Principal é Teacher mas tentando criar resposta. Verificando studentId: {}", studentId);
            
            // Verificar se o studentId fornecido existe e é válido
            try {
                com.NorthrnLights.demo.domain.Student student = answerService.findStudentById(studentId);
                log.info("✅ Student encontrado - ID: {}, Email: {}", student.getId(), student.getEmail());
                // Manter o studentId fornecido para permitir que professores criem respostas para estudantes (testes)
            } catch (IllegalArgumentException e) {
                log.error("❌ Student não encontrado com ID: {}. Erro: {}", studentId, e.getMessage());
                // Se o studentId não existe, retornar erro mais claro
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Não é possível criar resposta: você está logado como professor, mas o ID de estudante fornecido (" + studentId + ") não existe. " +
                        "Faça login como estudante para responder questões ou forneça um ID de estudante válido.");
            }
        } else {
            log.error("❌ Principal não é um Student nem Teacher. Tipo: {}", principal.getClass().getSimpleName());
            log.error("❌ Principal details: {}", principal);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Apenas estudantes ou professores podem criar respostas");
        }

        // Chama o serviço para criar a resposta usando o ID correto do Student
        Answer answer = answerService.createAnswer(text, questionId, studentId, imageFile);
        log.info("✅ Resposta criada com sucesso: ID={}, Student ID={}", answer.getId(), studentId);
        return ResponseEntity.ok(answer);
    }

    // Buscar uma resposta específica pelo ID
    @GetMapping("/{id}")
    public ResponseEntity<Answer> getAnswer(@PathVariable Long id) {
        return ResponseEntity.ok(answerService.findAnswerById(id));
    }
    @GetMapping("/quantity")
    public ResponseEntity<Integer> getQuantityAnswer() {
        return ResponseEntity.ok(answerService.getQuantity());
    }

    // Buscar todas as respostas
    @GetMapping
    public ResponseEntity<List<Answer>> getAllAnswers() {
        return ResponseEntity.ok(answerService.findAll());
    }

    // Atualizar uma resposta
    @PutMapping
    public ResponseEntity<Answer> updateAnswer(
            @RequestParam("text") String text,
            @RequestParam("answerId") Long answerId,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) throws IOException {

        // Chama o serviço para atualizar a resposta
        Answer answer = answerService.updateAnswer(answerId, text, imageFile);
        return ResponseEntity.ok(answer);
    }

    // Deletar uma resposta
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnswer(@PathVariable Long id) {
        answerService.deleteAnswer(id);
        return ResponseEntity.noContent().build();
    }

    // Buscar todas as respostas de uma questão
    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<Answer>> getAnswersByQuestion(@PathVariable Long questionId) {
        return ResponseEntity.ok(answerService.findAnswersByQuestionId(questionId));
    }

    // Buscar respostas do estudante autenticado agrupadas por dia
    @GetMapping("/my-answers")
    public ResponseEntity<List<Answer>> getMyAnswers(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Student) {
            Student student = (Student) principal;
            List<Answer> answers = answerService.findAnswersByStudentId(student.getId());
            return ResponseEntity.ok(answers);
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
