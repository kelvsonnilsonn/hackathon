package com.connectbeleza.connectbeleza.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ParceriaRequest(
        @NotNull UUID profissionalId,
        @Size(max = 600) String descricao
) {}