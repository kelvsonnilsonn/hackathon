package com.connectbeleza.connectbeleza.dto.request;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record AvaliacaoRequest(
        @NotNull(message = "ID do agendamento é obrigatório")
        UUID agendamentoId,

        @NotNull(message = "Nota é obrigatória")
        @Min(value = 1, message = "Nota mínima é 1")
        @Max(value = 5, message = "Nota máxima é 5")
        Integer nota,

        @Size(max = 600, message = "Comentário deve ter no máximo 600 caracteres")
        String comentario
) {}