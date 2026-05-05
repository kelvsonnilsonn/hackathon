package com.connectbeleza.connectbeleza.dto.response;

import com.connectbeleza.connectbeleza.domain.enums.CategoriaEstetica;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProdutoResponse(
        UUID id,
        UUID empresaId,
        String nomeEmpresa,
        String nome,
        String descricao,
        CategoriaEstetica categoria,
        BigDecimal preco,
        String urlImagem,
        String urlCompra,
        Boolean ativo,
        Boolean patrocinado,
        LocalDateTime criadoEm
) {}