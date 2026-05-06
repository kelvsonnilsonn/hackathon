package com.connectbeleza.connectbeleza.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record MetricasProfissionalResponse(
        UUID profissionalId,
        String nome,
        long totalAgendamentos,
        long agendamentosConcluidos,
        long agendamentosCancelados
) {}