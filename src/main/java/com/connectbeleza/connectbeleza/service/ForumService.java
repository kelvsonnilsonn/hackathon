package com.connectbeleza.connectbeleza.service;

import com.connectbeleza.connectbeleza.domain.entity.Forum;
import com.connectbeleza.connectbeleza.domain.enums.CategoriaPsicologica;
import com.connectbeleza.connectbeleza.dto.response.ForumResponse;
import com.connectbeleza.connectbeleza.exception.RecursoNaoEncontradoException;
import com.connectbeleza.connectbeleza.repository.ForumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ForumService {

    private final ForumRepository forumRepository;

    /**
     * Caso de uso: ACESSAR FÓRUM
     * Cada categoria de estética possui seu próprio fórum.
     */
    @Transactional(readOnly = true)
    public List<ForumResponse> listarTodos() {
        return forumRepository.findAll().stream()
                .filter(Forum::getAtivo)
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ForumResponse buscarPorCategoria(CategoriaPsicologica categoria) {
        return forumRepository.findByCategoria(categoria)
                .filter(Forum::getAtivo)
                .map(this::toResponse)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Fórum para categoria " + categoria + " não encontrado"));
    }

    @Transactional(readOnly = true)
    public Forum buscarEntidadePorId(UUID id) {
        return forumRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fórum", id));
    }

    @Transactional(readOnly = true)
    public ForumResponse buscarPorId(UUID id) {
        return toResponse(buscarEntidadePorId(id));
    }

    public ForumResponse toResponse(Forum f) {
        return new ForumResponse(f.getId(), f.getNome(), f.getDescricao(), f.getCategoria(), f.getAtivo());
    }
}