package com.connectbeleza.connectbeleza.service;

import com.connectbeleza.connectbeleza.domain.entity.Resposta;
import com.connectbeleza.connectbeleza.domain.entity.Topico;
import com.connectbeleza.connectbeleza.domain.entity.Usuario;
import com.connectbeleza.connectbeleza.dto.request.RespostaRequest;
import com.connectbeleza.connectbeleza.dto.response.RespostaResponse;
import com.connectbeleza.connectbeleza.exception.RegraDeNegocioException;
import com.connectbeleza.connectbeleza.repository.RespostaRepository;
import com.connectbeleza.connectbeleza.util.PaginacaoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RespostaService {

    private final RespostaRepository respostaRepository;
    private final TopicoService topicoService;
    private final UsuarioService usuarioService;
    private final PaginacaoUtil paginacaoUtil;

    /**
     * Caso de uso: PARTICIPAR DE TÓPICO — responder um tópico existente.
     * Include → ACESSAR FÓRUM → Include → CRIAR CONTA (autenticação via JWT)
     */
    @Transactional
    public RespostaResponse responder(UUID autorId, UUID topicoId, RespostaRequest request) {
        Topico topico = topicoService.buscarEntidadePorId(topicoId);

        if (topico.getFechado()) {
            throw new RegraDeNegocioException("Este tópico está fechado para novas respostas.");
        }

        Usuario autor = usuarioService.buscarEntidadePorId(autorId);

        Resposta resposta = Resposta.builder()
                .topico(topico)
                .autor(autor)
                .conteudo(request.conteudo())
                .build();

        resposta = respostaRepository.save(resposta);

        // Atualiza contador de respostas no tópico
        topicoService.incrementarRespostas(topicoId);

        return toResponse(resposta);
    }

    @Transactional(readOnly = true)
    public Page<RespostaResponse> listarPorTopico(UUID topicoId, int page, int size) {
        topicoService.buscarEntidadePorId(topicoId);
        return respostaRepository
                .findByTopicoIdOrderByCriadoEmAsc(topicoId, paginacaoUtil.build(page, size))
                .map(this::toResponse);
    }

    public RespostaResponse toResponse(Resposta r) {
        return new RespostaResponse(
                r.getId(),
                r.getTopico().getId(),
                r.getAutor().getId(),
                r.getAutor().getNome(),
                r.getConteudo(),
                r.getTotalCurtidas(),
                r.getCriadoEm()
        );
    }
}