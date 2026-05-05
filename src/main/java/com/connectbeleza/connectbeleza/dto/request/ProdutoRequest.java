package com.connectbeleza.connectbeleza.dto.request;

import com.connectbeleza.connectbeleza.domain.enums.CategoriaEstetica;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProdutoRequest(
        @NotBlank @Size(max = 150) String nome,
        @Size(max = 600) String descricao,
        @NotNull CategoriaEstetica categoria,
        BigDecimal preco,
        String urlImagem,
        String urlCompra,
        Boolean patrocinado
) {}