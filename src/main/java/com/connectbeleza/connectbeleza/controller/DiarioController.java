package com.connectbeleza.connectbeleza.controller;

import com.connectbeleza.connectbeleza.domain.entity.Usuario;
import com.connectbeleza.connectbeleza.dto.request.CriarDiarioRequest;
import com.connectbeleza.connectbeleza.dto.response.DiarioResponse;
import com.connectbeleza.connectbeleza.service.DiarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/diarios")
@RequiredArgsConstructor
public class DiarioController {

    private final DiarioService diarioService;

    @PostMapping
    public ResponseEntity<DiarioResponse> criar(@Valid @RequestBody CriarDiarioRequest request) {
        Usuario autor = new Usuario();
        autor.setId(request.autorId()); // idealmente: buscar via UsuarioService

        DiarioResponse response = diarioService.criar(autor, request.conteudo());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<DiarioResponse>> listar() {
        return ResponseEntity.ok(diarioService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiarioResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(diarioService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiarioResponse> atualizar(
            @PathVariable UUID id,
            @RequestBody String novoConteudo
    ) {
        return ResponseEntity.ok(diarioService.atualizar(id, novoConteudo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        diarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/privacidade")
    public ResponseEntity<Void> alternarPrivacidade(@PathVariable UUID id) {
        diarioService.alternarPrivacidade(id);
        return ResponseEntity.noContent().build();
    }
}