package com.connectbeleza.connectbeleza.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record AvaliacaoResponse(
        UUID id,
        UUID agendamentoId,
        UUID avaliadorId,
        String nomeAvaliador,
        UUID profissionalId,
        Integer nota,
        String comentario,
        LocalDateTime criadoEm
) {}
