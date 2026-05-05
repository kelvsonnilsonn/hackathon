package com.connectbeleza.connectbeleza.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RespostaRequest(
        @NotBlank(message = "Conteúdo é obrigatório")
        @Size(min = 2, max = 2000)
        String conteudo
) {}
