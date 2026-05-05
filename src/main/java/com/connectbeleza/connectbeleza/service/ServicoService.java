package com.connectbeleza.connectbeleza.service;

import com.connectbeleza.connectbeleza.domain.entity.Servico;
import com.connectbeleza.connectbeleza.dto.response.ServicoResponse;
import com.connectbeleza.connectbeleza.exception.RecursoNaoEncontradoException;
import com.connectbeleza.connectbeleza.repository.ServicoRepository;
import com.connectbeleza.connectbeleza.util.PaginacaoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final ProfissionalService profissionalService;
    private final PaginacaoUtil paginacaoUtil;

    @Transactional(readOnly = true)
    public Page<ServicoResponse> listarPorProfissional(UUID profissionalId, int page, int size) {
        profissionalService.buscarEntidadePorId(profissionalId); // valida existência
        return servicoRepository
                .findByProfissionalIdAndAtivoTrue(profissionalId, paginacaoUtil.build(page, size))
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ServicoResponse buscarPorId(UUID id) {
        return toResponse(buscarEntidadePorId(id));
    }

    @Transactional(readOnly = true)
    public Servico buscarEntidadePorId(UUID id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço", id));
    }

    public ServicoResponse toResponse(Servico s) {
        return new ServicoResponse(
                s.getId(),
                s.getProfissional().getId(),
                s.getProfissional().getUsuario().getNome(),
                s.getNome(),
                s.getDescricao(),
                s.getCategoria(),
                s.getPreco(),
                s.getDuracaoMinutos(),
                s.getAtivo(),
                s.getCriadoEm()
        );
    }
}