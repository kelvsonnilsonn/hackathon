package com.connectbeleza.connectbeleza.service;

import com.connectbeleza.connectbeleza.domain.entity.Diario;
import com.connectbeleza.connectbeleza.domain.entity.Usuario;
import com.connectbeleza.connectbeleza.dto.response.DiarioResponse;
import com.connectbeleza.connectbeleza.repository.DiarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DiarioService {

    private static final String DIARIO_NAO_ENCONTRADO = "Diário não encontrado: ";

    private final DiarioRepository diarioRepository;

    @Transactional
    public DiarioResponse criar(Usuario autor, String conteudo) {
        Diario diario = Diario.builder()
                .autor(autor)
                .conteudo(conteudo)
                .build();

        return toResponse(diarioRepository.save(diario));
    }

    @Transactional(readOnly = true)
    public List<DiarioResponse> listar() {
        return diarioRepository.findAll()
                .stream()
                .map(DiarioService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DiarioResponse buscarPorId(UUID id) {
        return toResponse(buscarOuLancar(id));
    }

    @Transactional
    public DiarioResponse atualizar(UUID id, String novoConteudo) {
        Diario diario = buscarOuLancar(id);
        diario.setConteudo(novoConteudo);
        return toResponse(diarioRepository.save(diario));
    }

    @Transactional
    public void deletar(UUID id) {
        if (!diarioRepository.existsById(id)) {
            throw new EntityNotFoundException(DIARIO_NAO_ENCONTRADO + id);
        }
        diarioRepository.deleteById(id);
    }

    @Transactional
    public void alternarPrivacidade(UUID id) {
        Diario diario = buscarOuLancar(id);
        diario.mudarPrivacidade();
        diarioRepository.save(diario);
    }

    // ---------- helpers ----------

    private Diario buscarOuLancar(UUID id) {
        return diarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(DIARIO_NAO_ENCONTRADO + id));
    }

    private static DiarioResponse toResponse(Diario diario) {
        return new DiarioResponse(
                diario.getId(),
                diario.getConteudo(),
                diario.getCriadoEm()
        );
    }
}