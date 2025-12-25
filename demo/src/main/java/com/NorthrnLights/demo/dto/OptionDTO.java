package com.NorthrnLights.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OptionDTO {
    private Long id;
    
    @NotBlank(message = "Texto da opção é obrigatório")
    private String text;
    
    private boolean correct;

    public OptionDTO() {}

    public OptionDTO(String text, boolean correct) {
        this.text = text;
        this.correct = correct;
    }
}