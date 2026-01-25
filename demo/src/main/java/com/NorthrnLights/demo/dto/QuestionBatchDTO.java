package com.NorthrnLights.demo.dto;

import com.NorthrnLights.demo.domain.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para criação de questões em lote via JSON.
 * Aceita imagens como base64 String ao invés de MultipartFile.
 */
@Data
public class QuestionBatchDTO {
    
    @NotBlank(message = "Título é obrigatório")
    private String title;
    
    private String description;
    
    private String portugueseTranslation; // Tradução/ajuda em português
    private Boolean hasHelp; // Indica se tem ajuda disponível
    
    @NotNull(message = "Tipo de questão é obrigatório")
    private QuestionType questionType;
    
    @NotNull(message = "Indicador de múltipla escolha é obrigatório")
    private Boolean multipleChoice;
    
    /**
     * Imagem em formato base64 (opcional).
     * Deve incluir o prefixo data:image/[tipo];base64, se presente.
     * Exemplo: "data:image/png;base64,iVBORw0KGgoAAAANS..."
     */
    private String imageBase64;
    
    /**
     * ID do professor que está criando a questão.
     * Obrigatório para associar a questão ao professor correto.
     */
    @NotNull(message = "ID do professor é obrigatório")
    private Long teacherId;
    
    /**
     * Lista de opções (obrigatória se multipleChoice = true).
     */
    @Valid
    private List<OptionDTO> options;
    
    /**
     * Data de expiração para correção automática (opcional).
     * Se definida, todas as questões criadas no mesmo dia terão esta mesma data.
     */
    private LocalDateTime expiresAt;
    
    /**
     * Data em que a questão ficará visível para os alunos (opcional).
     * Se não definida, a questão ficará visível imediatamente.
     */
    private LocalDateTime visibleAt;
}


