package com.connectbeleza.connectbeleza.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ContratacaoRequest(
        @NotNull(message = "Profissional é obrigatório")
        UUID profissionalId,

        @NotBlank(message = "Método de pagamento é obrigatório")
        String metodoPagamento

) {}
