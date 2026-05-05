package com.connectbeleza.connectbeleza.dto.response;

import com.connectbeleza.connectbeleza.domain.enums.StatusParceria;

import java.time.LocalDateTime;
import java.util.UUID;

public record ParceriaResponse(
        UUID id,
        UUID empresaId,
        String nomeEmpresa,
        UUID profissionalId,
        String nomeProfissional,
        String descricao,
        StatusParceria status,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {}