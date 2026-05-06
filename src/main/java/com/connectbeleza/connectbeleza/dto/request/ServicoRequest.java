package com.connectbeleza.connectbeleza.dto.request;

import com.connectbeleza.connectbeleza.domain.enums.CategoriaPsicologica;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ServicoRequest(
        @NotBlank(message = "Nome é obrigatório") @Size(max = 120) String nome,
        @Size(max = 500) String descricao,
        @NotNull CategoriaPsicologica categoria,
        @NotNull @DecimalMin("0.01") BigDecimal preco,
        @NotNull @Min(15) Integer duracaoMinutos
) {}