package com.connectbeleza.connectbeleza.service;

import com.connectbeleza.connectbeleza.domain.entity.*;
import com.connectbeleza.connectbeleza.domain.enums.StatusAgendamento;
import com.connectbeleza.connectbeleza.dto.request.AgendaRequest;
import com.connectbeleza.connectbeleza.dto.request.GerenciarPerfilProfissionalRequest;
import com.connectbeleza.connectbeleza.dto.request.ServicoRequest;
import com.connectbeleza.connectbeleza.dto.response.*;
import com.connectbeleza.connectbeleza.exception.RecursoNaoEncontradoException;
import com.connectbeleza.connectbeleza.exception.RegraDeNegocioException;
import com.connectbeleza.connectbeleza.repository.*;
import com.connectbeleza.connectbeleza.util.PaginacaoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GerenciarPerfilProfissionalService {

    private final ProfissionalRepository profissionalRepository;
    private final ServicoRepository servicoRepository;
    private final AgendaRepository agendaRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final ProfissionalService profissionalService;
    private final PaginacaoUtil paginacaoUtil;

    // ─── GERENCIAR PERFIL ────────────────────────────────────────────────────

    /**
     * Caso de uso: GERENCIAR PERFIL (profissional)
     */
    @Transactional
    public ProfissionalResponse atualizarPerfil(UUID usuarioId,
                                                GerenciarPerfilProfissionalRequest request) {
        Profissional profissional = profissionalRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Perfil profissional não encontrado para este usuário."));

        profissional.setBio(request.bio());
        profissional.setAnosExperiencia(request.anosExperiencia());
        profissional.setEspecialidades(request.especialidades());
        profissional.setCertificacoes(request.certificacoes());
        profissional.setUrlPortfolio(request.urlPortfolio());
        profissional.setLocalizacao(request.localizacao());

        if (request.latitude() != null)
            profissional.setLatitude(BigDecimal.valueOf(request.latitude()));
        if (request.longitude() != null)
            profissional.setLongitude(BigDecimal.valueOf(request.longitude()));

        return profissionalService.toResponse(profissionalRepository.save(profissional));
    }

    // ─── OFERECER SERVIÇOS ────────────────────────────────────────────────────

    /**
     * Caso de uso: OFERECER SERVIÇOS (profissional)
     * Include → DEFINIR PREÇO
     */
    @Transactional
    public ServicoResponse criarServico(UUID usuarioId, ServicoRequest request) {
        Profissional profissional = profissionalRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Perfil profissional não encontrado."));

        Servico servico = Servico.builder()
                .profissional(profissional)
                .nome(request.nome())
                .descricao(request.descricao())
                .categoria(request.categoria())
                .preco(request.preco())          // include → definir preço
                .duracaoMinutos(request.duracaoMinutos())
                .build();

        return toServicoResponse(servicoRepository.save(servico));
    }

    /**
     * Caso de uso: DEFINIR PREÇO — atualiza preço de um serviço existente
     */
    @Transactional
    public ServicoResponse atualizarPreco(UUID usuarioId, UUID servicoId, BigDecimal novoPreco) {
        Servico servico = buscarServicoDoProprietario(usuarioId, servicoId);

        if (novoPreco == null || novoPreco.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Preço deve ser maior que zero.");
        }

        servico.setPreco(novoPreco);
        return toServicoResponse(servicoRepository.save(servico));
    }

    @Transactional
    public ServicoResponse atualizarServico(UUID usuarioId, UUID servicoId, ServicoRequest request) {
        Servico servico = buscarServicoDoProprietario(usuarioId, servicoId);
        servico.setNome(request.nome());
        servico.setDescricao(request.descricao());
        servico.setCategoria(request.categoria());
        servico.setPreco(request.preco());
        servico.setDuracaoMinutos(request.duracaoMinutos());
        return toServicoResponse(servicoRepository.save(servico));
    }

    @Transactional
    public void desativarServico(UUID usuarioId, UUID servicoId) {
        Servico servico = buscarServicoDoProprietario(usuarioId, servicoId);
        servico.setAtivo(false);
        servicoRepository.save(servico);
    }

    // ─── DEFINIR AGENDA ───────────────────────────────────────────────────────

    /**
     * Caso de uso: DEFINIR AGENDA (include de oferecer serviços)
     * Define disponibilidade semanal por dia da semana.
     */
    @Transactional
    public AgendaResponse definirAgenda(UUID usuarioId, AgendaRequest request) {
        Profissional profissional = profissionalRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Perfil profissional não encontrado."));

        if (request.horaInicio().isAfter(request.horaFim()) ||
                request.horaInicio().equals(request.horaFim())) {
            throw new RegraDeNegocioException(
                    "Hora de início deve ser anterior à hora de fim.");
        }

        // Upsert por dia da semana
        Agenda agenda = agendaRepository
                .findByProfissionalIdAndDiaSemana(profissional.getId(), request.diaSemana())
                .orElse(Agenda.builder().profissional(profissional)
                        .diaSemana(request.diaSemana()).build());

        agenda.setHoraInicio(request.horaInicio());
        agenda.setHoraFim(request.horaFim());
        agenda.setAtivo(true);

        return toAgendaResponse(agendaRepository.save(agenda));
    }

    @Transactional(readOnly = true)
    public List<AgendaResponse> listarAgenda(UUID usuarioId) {
        Profissional profissional = profissionalRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Perfil profissional não encontrado."));
        return agendaRepository.findByProfissionalIdAndAtivoTrue(profissional.getId())
                .stream().map(this::toAgendaResponse).toList();
    }

    // ─── MÉTRICAS ─────────────────────────────────────────────────────────────

    /**
     * Caso de uso: VISUALIZAR MÉTRICAS DE DESEMPENHO (profissional)
     */
    @Transactional(readOnly = true)
    public MetricasProfissionalResponse obterMetricas(UUID usuarioId) {
        Profissional profissional = profissionalRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Perfil profissional não encontrado."));

        UUID profId = profissional.getId();

        long totalAgend = agendamentoRepository.findByServicosProfissionalIdOrderByDataHoraAgendadaDesc(
                profId, paginacaoUtil.build(0, Integer.MAX_VALUE)).getTotalElements();

        long concluidos = agendamentoRepository.findByProfissionalAndStatusAndPeriodo(
                profId, StatusAgendamento.CONCLUIDO,
                java.time.LocalDateTime.now().minusYears(10),
                java.time.LocalDateTime.now()).size();

        long cancelados = agendamentoRepository.findByProfissionalAndStatusAndPeriodo(
                profId, StatusAgendamento.CANCELADO,
                java.time.LocalDateTime.now().minusYears(10),
                java.time.LocalDateTime.now()).size();

        return new MetricasProfissionalResponse(
                profId,
                profissional.getUsuario().getNome(),
                totalAgend,
                concluidos,
                cancelados
        );
    }

    // ─── AGENDAMENTOS DO PROFISSIONAL ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<AgendamentoResponse> listarAgendamentos(UUID usuarioId, int page, int size) {
        Profissional profissional = profissionalRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Perfil profissional não encontrado."));
        return agendamentoRepository
                .findByServicosProfissionalIdOrderByDataHoraAgendadaDesc(
                        profissional.getId(), paginacaoUtil.build(page, size))
                .map(a -> new AgendamentoResponse(
                        a.getId(), a.getCliente().getId(), a.getCliente().getNome(),
                        toServicoResponse(a.getServico()),
                        a.getDataHoraAgendada(), a.getDataHoraAnterior(),
                        a.getStatus(), a.getMotivoCancelamento(),
                        a.getObservacoes(), null, a.getCriadoEm()));
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private Servico buscarServicoDoProprietario(UUID usuarioId, UUID servicoId) {
        Servico servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço", servicoId));
        Profissional profissional = profissionalRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Perfil profissional não encontrado."));
        if (!servico.getProfissional().getId().equals(profissional.getId())) {
            throw new RegraDeNegocioException("Este serviço não pertence ao seu perfil.");
        }
        return servico;
    }

    public ServicoResponse toServicoResponse(Servico s) {
        return new ServicoResponse(s.getId(), s.getProfissional().getId(),
                s.getProfissional().getUsuario().getNome(), s.getNome(), s.getDescricao(),
                s.getCategoria(), s.getPreco(), s.getDuracaoMinutos(), s.getAtivo(), s.getCriadoEm());
    }

    public AgendaResponse toAgendaResponse(Agenda a) {
        return new AgendaResponse(a.getId(), a.getDiaSemana(),
                a.getHoraInicio(), a.getHoraFim(), a.getAtivo());
    }
}