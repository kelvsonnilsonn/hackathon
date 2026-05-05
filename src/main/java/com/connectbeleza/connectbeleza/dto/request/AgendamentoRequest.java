package com.connectbeleza.connectbeleza.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record AgendamentoRequest(
        @NotNull(message = "Serviço é obrigatório")
        UUID servicoId,

        @NotNull(message = "Data e hora são obrigatórias")
        @Future(message = "A data deve ser futura")
        LocalDateTime dataHoraAgendada,

        @NotBlank(message = "Método de pagamento é obrigatório")
        String metodoPagamento,

        String observacoes
) {}
