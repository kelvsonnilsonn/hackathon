package com.connectbeleza.connectbeleza.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CriarDiarioRequest(
        @NotNull UUID autorId,
        @NotBlank String conteudo
) {}