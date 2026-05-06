package com.connectbeleza.connectbeleza.service;

import com.connectbeleza.connectbeleza.domain.entity.Profissional;
import com.connectbeleza.connectbeleza.dto.request.BuscarProfissionalRequest;
import com.connectbeleza.connectbeleza.dto.response.ProfissionalResponse;
import com.connectbeleza.connectbeleza.exception.RecursoNaoEncontradoException;
import com.connectbeleza.connectbeleza.repository.ProfissionalRepository;
import com.connectbeleza.connectbeleza.util.PaginacaoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfissionalService {

    private final ProfissionalRepository profissionalRepository;
    private final UsuarioService usuarioService;
    private final PaginacaoUtil paginacaoUtil;

    /**
     * Caso de uso: buscar profissional — inclui filtrar por especialidade/categoria.
     * Suporta busca por localização (lat/lng + raio) ou por nome/categoria.
     */
    @Transactional(readOnly = true)
    public Page<ProfissionalResponse> buscar(BuscarProfissionalRequest filtro, int page, int size) {
        Pageable pageable = paginacaoUtil.build(page, size, "notaMedia", "desc");

        boolean buscaGeo = filtro.latitude() != null
                && filtro.longitude() != null
                && filtro.raioKm() != null;

        if (buscaGeo) {
            return profissionalRepository.buscarPorLocalizacao(
                    filtro.latitude(), filtro.longitude(),
                    filtro.raioKm(), filtro.categoria(), pageable
            ).map(this::toResponse);
        }

        return profissionalRepository
                .buscarPorFiltros(filtro.categoria(), filtro.nome(), pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ProfissionalResponse buscarPorId(UUID id) {
        return toResponse(buscarEntidadePorId(id));
    }

    @Transactional(readOnly = true)
    public Profissional buscarEntidadePorId(UUID id) {
        return profissionalRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional", id));
    }

    @Transactional(readOnly = true)
    public Profissional buscarEntidadePorUsuarioId(UUID usuarioId) {
        return profissionalRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Perfil profissional não encontrado para o usuário: " + usuarioId));
    }

    /**
     * Recalcula nota média do profissional após nova avaliação.
     * Chamado pelo AvaliacaoService após persistir avaliação.
     */
    @Transactional
    public void atualizarNotaMedia(UUID profissionalId) {
        Profissional profissional = buscarEntidadePorId(profissionalId);
        profissionalRepository.save(profissional);
    }

    public ProfissionalResponse toResponse(Profissional p) {
        return new ProfissionalResponse(
                p.getId(),
                p.getUsuario().getId(),
                p.getUsuario().getNome(),
                p.getUsuario().getEmail(),
                p.getBio(),
                p.getAnosExperiencia(),
                p.getEspecialidades(),
                p.getCertificacoes(),
                p.getUrlPortfolio(),
                p.getVerificado(),
                p.getLocalizacao()
        );
    }
}