package com.connectbeleza.connectbeleza.service;

import com.connectbeleza.connectbeleza.domain.entity.Agendamento;
import com.connectbeleza.connectbeleza.domain.entity.Contratacao;
import com.connectbeleza.connectbeleza.domain.entity.Profissional;
import com.connectbeleza.connectbeleza.domain.entity.Usuario;
import com.connectbeleza.connectbeleza.dto.request.ContratacaoRequest;
import com.connectbeleza.connectbeleza.dto.response.AgendamentoResponse;
import com.connectbeleza.connectbeleza.dto.response.ContratacaoResponse;
import com.connectbeleza.connectbeleza.repository.ContratacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContratacaoService {

    private final ContratacaoRepository contratacaoRepository;
    private final ProfissionalService profissionalService;
    private final UsuarioService usuarioService;

    @Transactional
    public ContratacaoResponse contratarServico(UUID clienteId, ContratacaoRequest request) {
        Usuario cliente = usuarioService.buscarEntidadePorId(clienteId);
        Profissional profissional = profissionalService.buscarEntidadePorId(request.profissionalId());
        Contratacao contrato = new Contratacao(cliente, profissional);

        contrato = contratacaoRepository.save(contrato);
        return toResponse(contrato);
    }

    public ContratacaoResponse toResponse(Contratacao c) {
        return new ContratacaoResponse(
                c.getId(),
                c.getUsuario().getId(),
                c.getPsicologo().getId(),
                c.getStatus(),
                c.getCriadoEm()
        );
    }
}
