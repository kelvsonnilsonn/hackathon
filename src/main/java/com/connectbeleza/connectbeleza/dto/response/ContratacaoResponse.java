package com.connectbeleza.connectbeleza.dto.response;

import com.connectbeleza.connectbeleza.domain.enums.StatusContratacao;

import java.time.LocalDateTime;
import java.util.UUID;

public record ContratacaoResponse(
        UUID id,
        UUID clienteId,
        UUID psicologoId,
        StatusContratacao status,
        LocalDateTime criadoEm
) {}