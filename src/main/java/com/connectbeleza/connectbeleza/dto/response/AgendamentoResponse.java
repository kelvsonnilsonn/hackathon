package com.connectbeleza.connectbeleza.dto.response;

import com.connectbeleza.connectbeleza.domain.enums.StatusAgendamento;

import java.time.LocalDateTime;
import java.util.UUID;

public record AgendamentoResponse(
        UUID id,
        UUID clienteId,
        String nomeCliente,
        ServicoResponse servico,
        LocalDateTime dataHoraAgendada,
        LocalDateTime dataHoraAnterior,
        StatusAgendamento status,
        String motivoCancelamento,
        String observacoes,
        PagamentoResponse pagamento,
        LocalDateTime criadoEm
) {}
