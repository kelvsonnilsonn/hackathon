package com.connectbeleza.connectbeleza.service;

import com.connectbeleza.connectbeleza.domain.entity.Agendamento;
import com.connectbeleza.connectbeleza.domain.entity.Avaliacao;
import com.connectbeleza.connectbeleza.domain.entity.Usuario;
import com.connectbeleza.connectbeleza.domain.enums.StatusAgendamento;
import com.connectbeleza.connectbeleza.dto.request.AvaliacaoRequest;
import com.connectbeleza.connectbeleza.dto.response.AvaliacaoResponse;
import com.connectbeleza.connectbeleza.exception.RegraDeNegocioException;
import com.connectbeleza.connectbeleza.repository.AvaliacaoRepository;
import com.connectbeleza.connectbeleza.util.PaginacaoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final AgendamentoService agendamentoService;
    private final UsuarioService usuarioService;
    private final ProfissionalService profissionalService;
    private final PaginacaoUtil paginacaoUtil;

    /**
     * Caso de uso: AVALIAR PROFISSIONAL
     * Include → CONTRATAR SERVIÇO (somente agendamentos CONCLUIDOS podem ser avaliados)
     */
    @Transactional
    public AvaliacaoResponse avaliarProfissional(UUID clienteId, AvaliacaoRequest request) {
        Agendamento agendamento = agendamentoService.buscarEntidadePorId(request.agendamentoId());

        if (!agendamento.getCliente().getId().equals(clienteId)) {
            throw new RegraDeNegocioException("Somente o cliente do agendamento pode avaliar.");
        }

        if (agendamento.getStatus() != StatusAgendamento.CONCLUIDO) {
            throw new RegraDeNegocioException(
                    "Só é possível avaliar agendamentos com status CONCLUIDO.");
        }

        if (avaliacaoRepository.existsByAgendamentoId(request.agendamentoId())) {
            throw new RegraDeNegocioException("Este agendamento já foi avaliado.");
        }

        Usuario avaliador = usuarioService.buscarEntidadePorId(clienteId);
        var profissional = agendamento.getServico().getProfissional();

        Avaliacao avaliacao = Avaliacao.builder()
                .agendamento(agendamento)
                .avaliador(avaliador)
                .profissional(profissional)
                .nota(request.nota())
                .comentario(request.comentario())
                .build();

        avaliacao = avaliacaoRepository.save(avaliacao);

        // Recalcula nota média do profissional
        double media = avaliacaoRepository
                .calcularMediaPorProfissional(profissional.getId())
                .orElse((double) request.nota());
        long total = avaliacaoRepository.countByProfissionalId(profissional.getId());
        profissionalService.atualizarNotaMedia(profissional.getId(), media, total);

        return toResponse(avaliacao);
    }

    @Transactional(readOnly = true)
    public Page<AvaliacaoResponse> listarPorProfissional(UUID profissionalId, int page, int size) {
        profissionalService.buscarEntidadePorId(profissionalId);
        return avaliacaoRepository
                .findByProfissionalIdOrderByCriadoEmDesc(profissionalId, paginacaoUtil.build(page, size))
                .map(this::toResponse);
    }

    public AvaliacaoResponse toResponse(Avaliacao a) {
        return new AvaliacaoResponse(
                a.getId(),
                a.getAgendamento().getId(),
                a.getAvaliador().getId(),
                a.getAvaliador().getNome(),
                a.getProfissional().getId(),
                a.getNota(),
                a.getComentario(),
                a.getCriadoEm()
        );
    }
}