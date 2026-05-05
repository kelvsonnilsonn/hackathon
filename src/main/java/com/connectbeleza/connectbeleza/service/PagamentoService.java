package com.connectbeleza.connectbeleza.service;

import com.connectbeleza.connectbeleza.domain.entity.Agendamento;
import com.connectbeleza.connectbeleza.domain.entity.Pagamento;
import com.connectbeleza.connectbeleza.domain.enums.StatusPagamento;
import com.connectbeleza.connectbeleza.dto.response.PagamentoResponse;
import com.connectbeleza.connectbeleza.exception.RegraDeNegocioException;
import com.connectbeleza.connectbeleza.repository.PagamentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;

    /**
     * Caso de uso: realizar pagamento (include de contratar serviço).
     * Cria o registro de pagamento e chama integração externa (simulada).
     */
    @Transactional
    public Pagamento realizarPagamento(Agendamento agendamento, String metodoPagamento) {
        if (pagamentoRepository.existsByAgendamentoIdAndStatus(
                agendamento.getId(), StatusPagamento.APROVADO)) {
            throw new RegraDeNegocioException("Pagamento já realizado para este agendamento.");
        }

        Pagamento pagamento = Pagamento.builder()
                .agendamento(agendamento)
                .valor(agendamento.getServico().getPreco())
                .metodoPagamento(metodoPagamento)
                .status(StatusPagamento.PROCESSANDO)
                .build();

        pagamento = pagamentoRepository.save(pagamento);

        // Simula integração com gateway de pagamento
        boolean aprovado = processarGateway(pagamento);

        if (aprovado) {
            pagamento.setStatus(StatusPagamento.APROVADO);
            pagamento.setPagoEm(LocalDateTime.now());
            log.info("Pagamento aprovado para agendamento {}", agendamento.getId());
        } else {
            pagamento.setStatus(StatusPagamento.RECUSADO);
            throw new RegraDeNegocioException("Pagamento recusado. Verifique os dados e tente novamente.");
        }

        return pagamentoRepository.save(pagamento);
    }

    @Transactional
    public void estornarPagamento(UUID agendamentoId) {
        pagamentoRepository.findByAgendamentoId(agendamentoId).ifPresent(p -> {
            if (p.getStatus() == StatusPagamento.APROVADO) {
                p.setStatus(StatusPagamento.ESTORNADO);
                pagamentoRepository.save(p);
                log.info("Estorno realizado para agendamento {}", agendamentoId);
            }
        });
    }

    /**
     * Stub de integração com gateway externo (ex: Stripe, Mercado Pago).
     * Em produção, substituir pela chamada real à API do gateway.
     */
    private boolean processarGateway(Pagamento pagamento) {
        log.info("Processando pagamento {} via {}", pagamento.getId(), pagamento.getMetodoPagamento());
        return true; // simula aprovação
    }

    public PagamentoResponse toResponse(Pagamento p) {
        if (p == null) return null;
        return new PagamentoResponse(
                p.getId(), p.getValor(), p.getStatus(),
                p.getMetodoPagamento(), p.getCriadoEm(), p.getPagoEm()
        );
    }
}