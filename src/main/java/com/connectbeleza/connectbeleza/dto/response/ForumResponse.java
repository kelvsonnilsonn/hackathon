package com.connectbeleza.connectbeleza.dto.response;

import com.connectbeleza.connectbeleza.domain.enums.CategoriaPsicologica;

import java.util.UUID;

public record ForumResponse(
        UUID id,
        String nome,
        String descricao,
        CategoriaPsicologica categoria,
        Boolean ativo
) {}

