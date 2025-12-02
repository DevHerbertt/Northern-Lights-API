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
        // Busca a questão e o estudante
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

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

        // Se houver imagem, salvar no sistema de arquivos
        if (imageFile != null && !imageFile.isEmpty()) {
            String folder = "uploads/answers/";
            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path path = Paths.get(folder + fileName);
            Files.createDirectories(path.getParent());  // Cria o diretório, se não existir
            Files.copy(imageFile.getInputStream(), path); // Salva o arquivo no caminho
            answer.setImagePath(path.toString()); // Define o caminho da imagem no banco de dados
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
            String folder = "uploads/answers/";
            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path path = Paths.get(folder + fileName);
            Files.createDirectories(path.getParent());  // Cria o diretório, se não existir
            Files.copy(imageFile.getInputStream(), path); // Salva o arquivo no caminho
            answer.setImagePath(path.toString()); // Atualiza o caminho da imagem no banco
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

    public int findAll() {
        return answerRepository.findAll().size();
    }

    // Deletar uma resposta
    public void deleteAnswer(Long id) {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found"));

        answerRepository.delete(answer);
    }
}
