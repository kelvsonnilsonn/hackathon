package com.connectbeleza.connectbeleza.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record DiarioResponse(
        UUID id,
        String conteudo,
        LocalDateTime criadoEm
) {
}
