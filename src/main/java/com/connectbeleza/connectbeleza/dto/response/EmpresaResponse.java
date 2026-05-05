package com.connectbeleza.connectbeleza.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record EmpresaResponse(
        UUID id,
        UUID usuarioId,
        String razaoSocial,
        String cnpj,
        String descricao,
        String urlSite,
        String urlLogo,
        String localizacao,
        Boolean verificada,
        LocalDateTime criadoEm
) {}