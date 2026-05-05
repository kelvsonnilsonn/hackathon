package com.connectbeleza.connectbeleza.service;

import com.connectbeleza.connectbeleza.domain.entity.Agendamento;
import com.connectbeleza.connectbeleza.domain.entity.Servico;
import com.connectbeleza.connectbeleza.domain.entity.Usuario;
import com.connectbeleza.connectbeleza.domain.enums.StatusAgendamento;
import com.connectbeleza.connectbeleza.dto.request.AgendamentoRequest;
import com.connectbeleza.connectbeleza.dto.request.CancelarRequest;
import com.connectbeleza.connectbeleza.dto.request.RemarcarRequest;
import com.connectbeleza.connectbeleza.dto.response.AgendamentoResponse;
import com.connectbeleza.connectbeleza.exception.RecursoNaoEncontradoException;
import com.connectbeleza.connectbeleza.exception.RegraDeNegocioException;
import com.connectbeleza.connectbeleza.repository.AgendamentoRepository;
import com.connectbeleza.connectbeleza.util.PaginacaoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ServicoService servicoService;
    private final UsuarioService usuarioService;
    private final PagamentoService pagamentoService;
    private final PaginacaoUtil paginacaoUtil;

    /**
     * Caso de uso: CONTRATAR SERVIÇO
     * Include → REALIZAR PAGAMENTO
     * Include → CRIAR CONTA (garantido pelo filtro de segurança — usuário já autenticado)
     */
    @Transactional
    public AgendamentoResponse contratarServico(UUID clienteId, AgendamentoRequest request) {
        Usuario cliente = usuarioService.buscarEntidadePorId(clienteId);
        Servico servico = servicoService.buscarEntidadePorId(request.servicoId());

        if (!servico.getAtivo()) {
            throw new RegraDeNegocioException("O serviço selecionado não está disponível.");
        }

        LocalDateTime fim = request.dataHoraAgendada()
                .plusMinutes(servico.getDuracaoMinutos());

        boolean conflitoDeHorario = agendamentoRepository.existeConflitoDeHorario(
                servico.getProfissional().getId(),
                request.dataHoraAgendada(),
                fim
        );

        if (conflitoDeHorario) {
            throw new RegraDeNegocioException(
                    "Horário indisponível. O profissional já possui agendamento neste período.");
        }

        Agendamento agendamento = Agendamento.builder()
                .cliente(cliente)
                .servico(servico)
                .dataHoraAgendada(request.dataHoraAgendada())
                .observacoes(request.observacoes())
                .status(StatusAgendamento.PENDENTE)
                .build();

        agendamento = agendamentoRepository.save(agendamento);

        // Include: realizar pagamento
        var pagamento = pagamentoService.realizarPagamento(agendamento, request.metodoPagamento());

        agendamento.setStatus(StatusAgendamento.CONFIRMADO);
        agendamento.setPagamento(pagamento);
        agendamento = agendamentoRepository.save(agendamento);

        return toResponse(agendamento);
    }

    /**
     * Caso de uso: CANCELAR SERVIÇO — extend de contratar serviço.
     * Estorna pagamento quando elegível (política: até 24h antes).
     */
    @Transactional
    public AgendamentoResponse cancelarServico(UUID clienteId, UUID agendamentoId, CancelarRequest request) {
        Agendamento agendamento = buscarComValidacaoDeProprietario(agendamentoId, clienteId);

        if (agendamento.getStatus() == StatusAgendamento.CANCELADO) {
            throw new RegraDeNegocioException("Agendamento já está cancelado.");
        }

        if (agendamento.getStatus() == StatusAgendamento.CONCLUIDO) {
            throw new RegraDeNegocioException("Não é possível cancelar um serviço já concluído.");
        }

        boolean dentro24h = LocalDateTime.now()
                .plusHours(24)
                .isAfter(agendamento.getDataHoraAgendada());

        if (dentro24h) {
            throw new RegraDeNegocioException(
                    "Cancelamentos devem ser realizados com ao menos 24h de antecedência.");
        }

        agendamento.setStatus(StatusAgendamento.CANCELADO);
        agendamento.setMotivoCancelamento(request.motivoCancelamento());

        // Estorno automático do pagamento
        pagamentoService.estornarPagamento(agendamentoId);

        return toResponse(agendamentoRepository.save(agendamento));
    }

    /**
     * Caso de uso: REAGENDAR SERVIÇO — extend de contratar serviço.
     * Preserva histórico da data anterior.
     */
    @Transactional
    public AgendamentoResponse reagendarServico(UUID clienteId, UUID agendamentoId, RemarcarRequest request) {
        Agendamento agendamento = buscarComValidacaoDeProprietario(agendamentoId, clienteId);

        if (agendamento.getStatus() == StatusAgendamento.CANCELADO) {
            throw new RegraDeNegocioException("Não é possível reagendar um agendamento cancelado.");
        }

        if (agendamento.getStatus() == StatusAgendamento.CONCLUIDO) {
            throw new RegraDeNegocioException("Não é possível reagendar um serviço já concluído.");
        }

        LocalDateTime novoFim = request.novaDataHora()
                .plusMinutes(agendamento.getServico().getDuracaoMinutos());

        boolean conflito = agendamentoRepository.existeConflitoDeHorario(
                agendamento.getServico().getProfissional().getId(),
                request.novaDataHora(),
                novoFim
        );

        if (conflito) {
            throw new RegraDeNegocioException("Novo horário indisponível para o profissional.");
        }

        agendamento.setDataHoraAnterior(agendamento.getDataHoraAgendada());
        agendamento.setDataHoraAgendada(request.novaDataHora());
        agendamento.setStatus(StatusAgendamento.REAGENDADO);

        return toResponse(agendamentoRepository.save(agendamento));
    }

    @Transactional(readOnly = true)
    public Page<AgendamentoResponse> listarDoCliente(UUID clienteId, int page, int size) {
        return agendamentoRepository
                .findByClienteIdOrderByDataHoraAgendadaDesc(clienteId, paginacaoUtil.build(page, size))
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AgendamentoResponse buscarPorId(UUID id, UUID clienteId) {
        return toResponse(buscarComValidacaoDeProprietario(id, clienteId));
    }

    @Transactional(readOnly = true)
    public Agendamento buscarEntidadePorId(UUID id) {
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento", id));
    }

    private Agendamento buscarComValidacaoDeProprietario(UUID agendamentoId, UUID clienteId) {
        Agendamento agendamento = buscarEntidadePorId(agendamentoId);
        if (!agendamento.getCliente().getId().equals(clienteId)) {
            throw new RegraDeNegocioException("Agendamento não pertence a este cliente.");
        }
        return agendamento;
    }

    public AgendamentoResponse toResponse(Agendamento a) {
        return new AgendamentoResponse(
                a.getId(),
                a.getCliente().getId(),
                a.getCliente().getNome(),
                servicoService.toResponse(a.getServico()),
                a.getDataHoraAgendada(),
                a.getDataHoraAnterior(),
                a.getStatus(),
                a.getMotivoCancelamento(),
                a.getObservacoes(),
                pagamentoService.toResponse(a.getPagamento()),
                a.getCriadoEm()
        );
    }
}