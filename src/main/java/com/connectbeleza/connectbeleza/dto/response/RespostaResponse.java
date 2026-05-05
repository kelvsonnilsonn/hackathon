package com.connectbeleza.connectbeleza.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record RespostaResponse(
        UUID id,
        UUID topicoId,
        UUID autorId,
        String nomeAutor,
        String conteudo,
        Integer totalCurtidas,
        LocalDateTime criadoEm
) {}