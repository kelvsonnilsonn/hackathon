package com.connectbeleza.connectbeleza.dto.response;

import com.connectbeleza.connectbeleza.domain.enums.CategoriaEstetica;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ServicoResponse(
        UUID id,
        UUID profissionalId,
        String nomeProfissional,
        String nome,
        String descricao,
        CategoriaEstetica categoria,
        BigDecimal preco,
        Integer duracaoMinutos,
        Boolean ativo,
        LocalDateTime criadoEm
) {}