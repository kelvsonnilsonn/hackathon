package com.connectbeleza.connectbeleza.service;

import com.connectbeleza.connectbeleza.domain.entity.*;
import com.connectbeleza.connectbeleza.domain.enums.StatusParceria;
import com.connectbeleza.connectbeleza.dto.request.ParceriaRequest;
import com.connectbeleza.connectbeleza.dto.request.ProdutoRequest;
import com.connectbeleza.connectbeleza.dto.response.*;
import com.connectbeleza.connectbeleza.exception.RecursoNaoEncontradoException;
import com.connectbeleza.connectbeleza.exception.RegraDeNegocioException;
import com.connectbeleza.connectbeleza.repository.*;
import com.connectbeleza.connectbeleza.util.PaginacaoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final ProdutoRepository produtoRepository;
    private final ParceriaRepository parceriaRepository;
    private final ProfissionalRepository profissionalRepository;
    private final PaginacaoUtil paginacaoUtil;

    // ─── PROMOVER PRODUTOS ────────────────────────────────────────────────────

    /**
     * Caso de uso: PROMOVER PRODUTOS (empresa)
     */
    @Transactional
    public ProdutoResponse criarProduto(UUID usuarioId, ProdutoRequest request) {
        Empresa empresa = buscarEmpresaPorUsuario(usuarioId);

        Produto produto = Produto.builder()
                .empresa(empresa)
                .nome(request.nome())
                .descricao(request.descricao())
                .categoria(request.categoria())
                .preco(request.preco())
                .urlImagem(request.urlImagem())
                .urlCompra(request.urlCompra())
                .patrocinado(request.patrocinado() != null && request.patrocinado())
                .build();

        return toProdutoResponse(produtoRepository.save(produto));
    }

    @Transactional
    public ProdutoResponse atualizarProduto(UUID usuarioId, UUID produtoId, ProdutoRequest request) {
        Produto produto = buscarProdutoDoProprietario(usuarioId, produtoId);
        produto.setNome(request.nome());
        produto.setDescricao(request.descricao());
        produto.setCategoria(request.categoria());
        produto.setPreco(request.preco());
        produto.setUrlImagem(request.urlImagem());
        produto.setUrlCompra(request.urlCompra());
        if (request.patrocinado() != null) produto.setPatrocinado(request.patrocinado());
        return toProdutoResponse(produtoRepository.save(produto));
    }

    @Transactional
    public void removerProduto(UUID usuarioId, UUID produtoId) {
        Produto produto = buscarProdutoDoProprietario(usuarioId, produtoId);
        produto.setAtivo(false);
        produtoRepository.save(produto);
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponse> listarProdutosDaEmpresa(UUID usuarioId, int page, int size) {
        Empresa empresa = buscarEmpresaPorUsuario(usuarioId);
        return produtoRepository
                .findByEmpresaIdAndAtivoTrue(empresa.getId(), paginacaoUtil.build(page, size))
                .map(this::toProdutoResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponse> listarPatrocinados(int page, int size) {
        return produtoRepository
                .findByAtivoTrueAndPatrocinadoTrue(paginacaoUtil.build(page, size))
                .map(this::toProdutoResponse);
    }

    // ─── REALIZAR PARCERIA COM PROFISSIONAL ───────────────────────────────────

    /**
     * Caso de uso: REALIZAR PARCERIA COM PROFISSIONAL (empresa)
     */
    @Transactional
    public ParceriaResponse solicitarParceria(UUID usuarioId, ParceriaRequest request) {
        Empresa empresa = buscarEmpresaPorUsuario(usuarioId);

        Profissional profissional = profissionalRepository.findById(request.profissionalId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Profissional", request.profissionalId()));

        boolean jaExiste = parceriaRepository.existsByEmpresaIdAndProfissionalIdAndStatus(
                empresa.getId(), profissional.getId(), StatusParceria.PENDENTE);
        if (jaExiste) {
            throw new RegraDeNegocioException(
                    "Já existe uma solicitação de parceria pendente com este profissional.");
        }

        Parceria parceria = Parceria.builder()
                .empresa(empresa)
                .profissional(profissional)
                .descricao(request.descricao())
                .status(StatusParceria.PENDENTE)
                .build();

        return toParceriaResponse(parceriaRepository.save(parceria));
    }

    @Transactional(readOnly = true)
    public Page<ParceriaResponse> listarParcerias(UUID usuarioId, int page, int size) {
        Empresa empresa = buscarEmpresaPorUsuario(usuarioId);
        return parceriaRepository
                .findByEmpresaIdOrderByCriadoEmDesc(empresa.getId(), paginacaoUtil.build(page, size))
                .map(this::toParceriaResponse);
    }

    // ─── GERENCIAR PARCERIAS (profissional responde) ─────────────────────────

    /**
     * Caso de uso: GERENCIAR PARCERIAS (profissional aceita ou recusa)
     */
    @Transactional
    public ParceriaResponse responderParceria(UUID usuarioProfId, UUID parceriaId,
                                              StatusParceria novoStatus) {
        Profissional profissional = profissionalRepository.findByUsuarioId(usuarioProfId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Perfil profissional não encontrado."));

        Parceria parceria = parceriaRepository.findById(parceriaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Parceria", parceriaId));

        if (!parceria.getProfissional().getId().equals(profissional.getId())) {
            throw new RegraDeNegocioException("Esta parceria não é direcionada a você.");
        }

        if (parceria.getStatus() != StatusParceria.PENDENTE) {
            throw new RegraDeNegocioException(
                    "Apenas parcerias pendentes podem ser respondidas.");
        }

        if (novoStatus != StatusParceria.ACEITA && novoStatus != StatusParceria.RECUSADA) {
            throw new RegraDeNegocioException("Status inválido. Use ACEITA ou RECUSADA.");
        }

        parceria.setStatus(novoStatus);
        return toParceriaResponse(parceriaRepository.save(parceria));
    }

    @Transactional(readOnly = true)
    public Page<ParceriaResponse> listarParceriasDoProfissional(UUID usuarioProfId,
                                                                int page, int size) {
        Profissional profissional = profissionalRepository.findByUsuarioId(usuarioProfId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Perfil profissional não encontrado."));
        return parceriaRepository
                .findByProfissionalIdOrderByCriadoEmDesc(
                        profissional.getId(), paginacaoUtil.build(page, size))
                .map(this::toParceriaResponse);
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private Empresa buscarEmpresaPorUsuario(UUID usuarioId) {
        return empresaRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Perfil de empresa não encontrado para este usuário."));
    }

    private Produto buscarProdutoDoProprietario(UUID usuarioId, UUID produtoId) {
        Empresa empresa = buscarEmpresaPorUsuario(usuarioId);
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", produtoId));
        if (!produto.getEmpresa().getId().equals(empresa.getId())) {
            throw new RegraDeNegocioException("Este produto não pertence à sua empresa.");
        }
        return produto;
    }

    public ProdutoResponse toProdutoResponse(Produto p) {
        return new ProdutoResponse(
                p.getId(), p.getEmpresa().getId(), p.getEmpresa().getRazaoSocial(),
                p.getNome(), p.getDescricao(), p.getCategoria(), p.getPreco(),
                p.getUrlImagem(), p.getUrlCompra(), p.getAtivo(), p.getPatrocinado(), p.getCriadoEm());
    }

    public ParceriaResponse toParceriaResponse(Parceria p) {
        return new ParceriaResponse(
                p.getId(), p.getEmpresa().getId(), p.getEmpresa().getRazaoSocial(),
                p.getProfissional().getId(), p.getProfissional().getUsuario().getNome(),
                p.getDescricao(), p.getStatus(), p.getCriadoEm(), p.getAtualizadoEm());
    }

    public EmpresaResponse toEmpresaResponse(Empresa e) {
        return new EmpresaResponse(
                e.getId(), e.getUsuario().getId(), e.getRazaoSocial(), e.getCnpj(),
                e.getDescricao(), e.getUrlSite(), e.getUrlLogo(), e.getLocalizacao(),
                e.getVerificada(), e.getCriadoEm());
    }
}