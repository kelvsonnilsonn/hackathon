package com.connectbeleza.connectbeleza.dto.response;

import com.connectbeleza.connectbeleza.domain.enums.TipoLembrete;

import java.time.LocalDateTime;
import java.util.UUID;

public record LembreteResponse(
        UUID id,
        TipoLembrete tipo,
        String mensagem,
        String horaEnvio,
        Boolean ativo,
        LocalDateTime criadoEm
) {}