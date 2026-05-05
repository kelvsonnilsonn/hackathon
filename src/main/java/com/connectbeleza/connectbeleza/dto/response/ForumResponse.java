package com.connectbeleza.connectbeleza.dto.response;

import com.connectbeleza.connectbeleza.domain.enums.CategoriaEstetica;

import java.util.UUID;

public record ForumResponse(
        UUID id,
        String nome,
        String descricao,
        CategoriaEstetica categoria,
        Boolean ativo
) {}

