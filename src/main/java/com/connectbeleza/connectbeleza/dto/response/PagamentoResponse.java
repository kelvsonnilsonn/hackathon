package com.connectbeleza.connectbeleza.dto.response;

import com.connectbeleza.connectbeleza.domain.enums.StatusPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PagamentoResponse(
        UUID id,
        BigDecimal valor,
        StatusPagamento status,
        String metodoPagamento,
        LocalDateTime criadoEm,
        LocalDateTime pagoEm
) {}