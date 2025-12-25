package com.NorthrnLights.demo.util;

import com.NorthrnLights.demo.domain.Answer;
import com.NorthrnLights.demo.domain.Correction;
import com.NorthrnLights.demo.domain.Grade;
import com.NorthrnLights.demo.domain.Question;
import com.NorthrnLights.demo.domain.QuestionOption;
import com.NorthrnLights.demo.repository.AnswerRepository;
import com.NorthrnLights.demo.repository.CorrectionRepository;
import com.NorthrnLights.demo.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutoCorrectionScheduler {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final CorrectionRepository correctionRepository;

    @Scheduled(fixedRate = 60000) // Executa a cada 1 minuto
    @Transactional
    public void autoCorrectMultipleChoiceAnswers() {
        LocalDateTime now = LocalDateTime.now();
        log.info("🔍 Verificando questões de múltipla escolha expiradas para correção automática... (Hora atual: {})", now);

        // Buscar questões de múltipla escolha que expiraram (usar método que carrega opções)
        List<Question> allQuestions = questionRepository.findAllWithOptions();
        List<Question> expiredQuestions = allQuestions.stream()
                .filter(q -> q.isMultipleChoice() 
                        && q.getExpiresAt() != null 
                        && q.getExpiresAt().isBefore(now))
                .toList();
        
        log.info("📊 Total de questões encontradas: {}, Questões expiradas: {}", allQuestions.size(), expiredQuestions.size());

        if (expiredQuestions.isEmpty()) {
            log.info("✅ Nenhuma questão de múltipla escolha expirada encontrada.");
            return;
        }

        int correctedCount = 0;

        for (Question question : expiredQuestions) {
            // Buscar todas as respostas para esta questão que ainda não têm correção
            List<Answer> answers = answerRepository.findByQuestionId(question.getId());
            
            for (Answer answer : answers) {
                // Verificar se já existe correção para esta resposta
                List<Correction> existingCorrections = correctionRepository.findByAnswerId(answer.getId());
                if (!existingCorrections.isEmpty()) {
                    continue; // Já foi corrigida
                }

                // Corrigir automaticamente
                boolean isCorrect = checkMultipleChoiceAnswer(answer, question);
                log.info("🔍 DEBUG: Resposta ID {} - isCorrect: {}", answer.getId(), isCorrect);
                log.info("🔍 DEBUG: Texto da resposta: '{}'", answer.getText());
                log.info("🔍 DEBUG: Questão ID: {}, Opções corretas: {}", 
                        question.getId(), 
                        question.getOptions().stream()
                            .filter(QuestionOption::isCorrect)
                            .map(opt -> question.getOptions().indexOf(opt) + " (" + opt.getText() + ")")
                            .toList());
                
                // ATENÇÃO: Se isCorrect é true, a resposta está CORRETA, então atribui Grade.A
                // Se isCorrect é false, a resposta está INCORRETA, então atribui Grade.F
                Grade grade = isCorrect ? Grade.A : Grade.F;
                log.info("🔍 DEBUG FINAL: isCorrect={}, Grade atribuída={}", isCorrect, grade);
                String feedback = isCorrect 
                    ? "Resposta correta! Parabéns!" 
                    : "Resposta incorreta. Revise o conteúdo.";
                
                log.info("🔍 DEBUG: Nota atribuída: {} (isCorrect={})", grade, isCorrect);

                Correction correction = Correction.builder()
                        .grade(grade)
                        .feedback(feedback)
                        .answer(answer)
                        .teacher(question.getTeacher()) // Usar o professor da questão
                        .build();

                correctionRepository.save(correction);
                correctedCount++;
                log.info("✅ Resposta ID {} corrigida automaticamente. Nota: {}", answer.getId(), grade);
            }
        }

        log.info("✅ Total de respostas corrigidas automaticamente: {}", correctedCount);
    }

    /**
     * Verifica se a resposta de múltipla escolha está correta
     */
    private boolean checkMultipleChoiceAnswer(Answer answer, Question question) {
        if (question.getOptions() == null || question.getOptions().isEmpty()) {
            log.warn("⚠️ Questão {} não tem opções", question.getId());
            return false;
        }

        // A resposta do aluno vem no formato "A) texto da opção"
        String answerText = answer.getText() != null ? answer.getText().trim() : "";
        log.debug("🔍 Verificando resposta: '{}' para questão ID: {}", answerText, question.getId());
        
        // Encontrar a opção correta
        QuestionOption correctOption = null;
        int correctIndex = -1;
        for (int i = 0; i < question.getOptions().size(); i++) {
            QuestionOption option = question.getOptions().get(i);
            if (option.isCorrect()) {
                correctOption = option;
                correctIndex = i;
                break;
            }
        }

        if (correctOption == null) {
            log.warn("⚠️ Questão {} não tem opção correta definida", question.getId());
            return false;
        }

        log.debug("✅ Opção correta encontrada: índice {}, texto: '{}'", correctIndex, correctOption.getText());
        
        // Verificar se a resposta do aluno corresponde à opção correta
        // A resposta vem no formato "A) texto da opção"
        String cleanAnswerText = answerText.trim();
        
        // Verificar se começa com letra seguida de parêntese (formato "A) texto")
        if (cleanAnswerText.matches("^[A-Z]\\)\\s*.*")) {
            // Extrair a letra (A, B, C, D, E, etc)
            char answerLetter = cleanAnswerText.toUpperCase().charAt(0);
            int answerIndex = answerLetter - 'A';
            
            log.debug("📝 Resposta no formato 'LETRA) texto'. Letra: {}, Índice calculado: {}", answerLetter, answerIndex);
            
            // Verificar se o índice está dentro dos limites
            if (answerIndex >= 0 && answerIndex < question.getOptions().size()) {
                QuestionOption selectedOption = question.getOptions().get(answerIndex);
                
                // Log detalhado para debug
                log.info("🔍 DEBUG: Opção selecionada - Índice: {}, Letra: {}, Texto: '{}', isCorrect: {}", 
                        answerIndex, answerLetter, selectedOption.getText(), selectedOption.isCorrect());
                log.info("🔍 DEBUG: Opção correta esperada - Índice: {}, Texto: '{}'", 
                        correctIndex, correctOption.getText());
                
                // A lógica correta: verificar se o índice selecionado corresponde ao índice da opção correta
                // Esta é a forma mais confiável de verificar
                boolean isCorrect = (answerIndex == correctIndex);
                
                log.info("🔍 DEBUG: answerIndex (selecionado) = {}, correctIndex (esperado) = {}", answerIndex, correctIndex);
                log.info("🔍 DEBUG: selectedOption.isCorrect() = {}", selectedOption.isCorrect());
                log.info("✅ Resposta verificada: Índice {} (letra {}), Opção correta no índice {}, Resultado: {}", 
                        answerIndex, answerLetter, correctIndex, isCorrect);
                
                // Verificação adicional: se os índices não coincidem, verificar se a opção selecionada está marcada como correta
                // (pode haver múltiplas opções corretas ou ordem diferente)
                if (!isCorrect && selectedOption.isCorrect()) {
                    log.warn("⚠️ Índices não coincidem, mas opção selecionada está marcada como correta. Considerando correta.");
                    return true;
                }
                
                return isCorrect;
            } else {
                log.warn("⚠️ Índice {} fora dos limites (total de opções: {})", answerIndex, question.getOptions().size());
                return false;
            }
        } else if (cleanAnswerText.matches("^[A-Z]$")) {
            // Apenas a letra (A, B, C, D, E)
            char answerLetter = cleanAnswerText.toUpperCase().charAt(0);
            int answerIndex = answerLetter - 'A';
            
            log.debug("📝 Resposta apenas com letra: {}, Índice calculado: {}", answerLetter, answerIndex);
            
            if (answerIndex >= 0 && answerIndex < question.getOptions().size()) {
                QuestionOption selectedOption = question.getOptions().get(answerIndex);
                
                // Log detalhado para debug
                log.info("🔍 DEBUG: Opção selecionada (apenas letra) - Índice: {}, Letra: {}, Texto: '{}', isCorrect: {}", 
                        answerIndex, answerLetter, selectedOption.getText(), selectedOption.isCorrect());
                log.info("🔍 DEBUG: Opção correta esperada - Índice: {}, Texto: '{}'", 
                        correctIndex, correctOption.getText());
                
                // A lógica correta: verificar se o índice selecionado corresponde ao índice da opção correta
                boolean isCorrect = (answerIndex == correctIndex);
                
                log.info("🔍 DEBUG: answerIndex (selecionado) = {}, correctIndex (esperado) = {}", answerIndex, correctIndex);
                log.info("🔍 DEBUG: selectedOption.isCorrect() = {}", selectedOption.isCorrect());
                log.info("✅ Resposta verificada (apenas letra): Índice {} (letra {}), Opção correta no índice {}, Resultado: {}", 
                        answerIndex, answerLetter, correctIndex, isCorrect);
                
                // Verificação adicional: se os índices não coincidem, verificar se a opção selecionada está marcada como correta
                if (!isCorrect && selectedOption.isCorrect()) {
                    log.warn("⚠️ Índices não coincidem, mas opção selecionada está marcada como correta. Considerando correta.");
                    return true;
                }
                
                return isCorrect;
            } else {
                log.warn("⚠️ Índice {} fora dos limites", answerIndex);
                return false;
            }
        } else {
            // Tentar comparar diretamente com o texto da opção correta
            String correctOptionText = correctOption.getText().trim();
            boolean isCorrect = correctOptionText.equalsIgnoreCase(cleanAnswerText);
            log.info("✅ Comparação direta de texto. Resposta: '{}', Correta: '{}', Resultado: {}", 
                    cleanAnswerText, correctOptionText, isCorrect);
            return isCorrect;
        }
    }
}

