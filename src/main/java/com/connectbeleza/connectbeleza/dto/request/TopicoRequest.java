package com.connectbeleza.connectbeleza.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record TopicoRequest(
        @NotNull(message = "Fórum é obrigatório")
        UUID forumId,

        @NotBlank(message = "Título é obrigatório")
        @Size(min = 5, max = 200)
        String titulo,

        @NotBlank(message = "Conteúdo é obrigatório")
        @Size(min = 10)
        String conteudo
) {}