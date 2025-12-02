package com.NorthrnLights.demo.controller;

import com.NorthrnLights.demo.domain.Answer;
import com.NorthrnLights.demo.service.AnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/answers")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    // Criar uma resposta
    @PostMapping
    public ResponseEntity<Answer> createAnswer(
            @RequestParam("text") String text,           // O texto da resposta
            @RequestParam("questionId") Long questionId, // O ID da questão associada
            @RequestParam("studentId") Long studentId,   // O ID do estudante que está respondendo
            @RequestParam("imageFile") MultipartFile imageFile) throws IOException { // O arquivo de imagem

        // Chama o serviço para criar a resposta
        Answer answer = answerService.createAnswer(text, questionId, studentId, imageFile);
        return ResponseEntity.ok(answer);
    }

    // Buscar uma resposta específica pelo ID
    @GetMapping("/{id}")
    public ResponseEntity<Answer> getAnswer(@PathVariable Long id) {
        return ResponseEntity.ok(answerService.findAnswerById(id));
    }
    @GetMapping("/quantity")
    public ResponseEntity<Integer> getQuantityAnswer() {
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
}
