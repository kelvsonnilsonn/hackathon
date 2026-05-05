package com.connectbeleza.connectbeleza.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record RemarcarRequest(
        @NotNull(message = "Nova data e hora são obrigatórias")
        @Future(message = "A data deve ser futura")
        LocalDateTime novaDataHora
) {}

