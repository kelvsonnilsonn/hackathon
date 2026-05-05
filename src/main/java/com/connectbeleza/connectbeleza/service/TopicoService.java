package com.connectbeleza.connectbeleza.service;

import com.connectbeleza.connectbeleza.domain.entity.Forum;
import com.connectbeleza.connectbeleza.domain.entity.Topico;
import com.connectbeleza.connectbeleza.domain.entity.Usuario;
import com.connectbeleza.connectbeleza.dto.request.TopicoRequest;
import com.connectbeleza.connectbeleza.dto.response.TopicoResponse;
import com.connectbeleza.connectbeleza.exception.RecursoNaoEncontradoException;
import com.connectbeleza.connectbeleza.exception.RegraDeNegocioException;
import com.connectbeleza.connectbeleza.repository.TopicoRepository;
import com.connectbeleza.connectbeleza.util.PaginacaoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TopicoService {

    private final TopicoRepository topicoRepository;
    private final ForumService forumService;
    private final UsuarioService usuarioService;
    private final PaginacaoUtil paginacaoUtil;

    /**
     * Caso de uso: CRIAR TÓPICO
     * Include → ACESSAR FÓRUM → Include → CRIAR CONTA (autenticação via JWT)
     */
    @Transactional
    public TopicoResponse criarTopico(UUID autorId, TopicoRequest request) {
        Forum forum = forumService.buscarEntidadePorId(request.forumId());

        if (!forum.getAtivo()) {
            throw new RegraDeNegocioException("Este fórum está fechado para novas publicações.");
        }

        Usuario autor = usuarioService.buscarEntidadePorId(autorId);

        Topico topico = Topico.builder()
                .forum(forum)
                .autor(autor)
                .titulo(request.titulo())
                .conteudo(request.conteudo())
                .build();

        return toResponse(topicoRepository.save(topico));
    }

    /**
     * Caso de uso: PARTICIPAR DE TÓPICO — lista os tópicos de um fórum
     * Include → ACESSAR FÓRUM
     */
    @Transactional(readOnly = true)
    public Page<TopicoResponse> listarPorForum(UUID forumId, String termo, int page, int size) {
        forumService.buscarEntidadePorId(forumId); // valida existência do fórum

        if (termo != null && !termo.isBlank()) {
            return topicoRepository
                    .buscarPorTermo(forumId, termo, paginacaoUtil.build(page, size))
                    .map(this::toResponse);
        }

        return topicoRepository
                .findByForumIdOrderByFixadoDescCriadoEmDesc(forumId, paginacaoUtil.build(page, size))
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public TopicoResponse buscarPorId(UUID id) {
        return toResponse(buscarEntidadePorId(id));
    }

    @Transactional(readOnly = true)
    public Topico buscarEntidadePorId(UUID id) {
        return topicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tópico", id));
    }

    @Transactional
    public void incrementarRespostas(UUID topicoId) {
        Topico topico = buscarEntidadePorId(topicoId);
        topico.setTotalRespostas(topico.getTotalRespostas() + 1);
        topicoRepository.save(topico);
    }

    public TopicoResponse toResponse(Topico t) {
        return new TopicoResponse(
                t.getId(),
                t.getForum().getId(),
                t.getForum().getCategoria().name(),
                t.getAutor().getId(),
                t.getAutor().getNome(),
                t.getTitulo(),
                t.getConteudo(),
                t.getFixado(),
                t.getFechado(),
                t.getTotalRespostas(),
                t.getCriadoEm(),
                t.getAtualizadoEm()
        );
    }
}