package com.connectbeleza.connectbeleza.controller;

import com.connectbeleza.connectbeleza.domain.enums.CategoriaEstetica;
import com.connectbeleza.connectbeleza.dto.request.RespostaRequest;
import com.connectbeleza.connectbeleza.dto.request.TopicoRequest;
import com.connectbeleza.connectbeleza.dto.response.ForumResponse;
import com.connectbeleza.connectbeleza.dto.response.RespostaResponse;
import com.connectbeleza.connectbeleza.dto.response.TopicoResponse;
import com.connectbeleza.connectbeleza.service.ForumService;
import com.connectbeleza.connectbeleza.service.RespostaService;
import com.connectbeleza.connectbeleza.service.TopicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Casos de uso:
 * - ACESSAR FÓRUM  (include → criar conta)
 * - CRIAR TÓPICO   (include → acessar fórum → criar conta)
 * - PARTICIPAR DE TÓPICO (include → acessar fórum)
 */
@RestController
@RequestMapping("/forums")
@RequiredArgsConstructor
@Tag(name = "Fóruns & Tópicos", description = "Comunidade por categoria de estética")
public class ForumController {

    private final ForumService forumService;
    private final TopicoService topicoService;
    private final RespostaService respostaService;

    // ─── FÓRUM ────────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Acessar fórum — lista todos os fóruns por categoria")
    public ResponseEntity<List<ForumResponse>> listarForums() {
        return ResponseEntity.ok(forumService.listarTodos());
    }

    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Acessar fórum por categoria de estética")
    public ResponseEntity<ForumResponse> forumPorCategoria(@PathVariable CategoriaEstetica categoria) {
        return ResponseEntity.ok(forumService.buscarPorCategoria(categoria));
    }

    @GetMapping("/{forumId}")
    @Operation(summary = "Detalhe de um fórum")
    public ResponseEntity<ForumResponse> buscarForum(@PathVariable UUID forumId) {
        return ResponseEntity.ok(forumService.buscarPorId(forumId));
    }

    // ─── TÓPICOS ──────────────────────────────────────────────────────────────

    @GetMapping("/{forumId}/topicos")
    @Operation(summary = "Participar de tópico — lista tópicos do fórum, com busca por termo")
    public ResponseEntity<Page<TopicoResponse>> listarTopicos(
            @PathVariable UUID forumId,
            @RequestParam(required = false) String termo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(topicoService.listarPorForum(forumId, termo, page, size));
    }

    @PostMapping("/{forumId}/topicos")
    @Operation(summary = "Criar tópico — include → acessar fórum → criar conta (JWT)")
    public ResponseEntity<TopicoResponse> criarTopico(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID forumId,
            @Valid @RequestBody TopicoRequest request) {
        UUID autorId = UUID.fromString(userDetails.getUsername());
        // garante que o forumId da URL bate com o do body
        TopicoRequest req = new TopicoRequest(forumId, request.titulo(), request.conteudo());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(topicoService.criarTopico(autorId, req));
    }

    @GetMapping("/topicos/{topicoId}")
    @Operation(summary = "Detalhe de um tópico")
    public ResponseEntity<TopicoResponse> buscarTopico(@PathVariable UUID topicoId) {
        return ResponseEntity.ok(topicoService.buscarPorId(topicoId));
    }

    // ─── RESPOSTAS ────────────────────────────────────────────────────────────

    @GetMapping("/topicos/{topicoId}/respostas")
    @Operation(summary = "Listar respostas de um tópico")
    public ResponseEntity<Page<RespostaResponse>> listarRespostas(
            @PathVariable UUID topicoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(respostaService.listarPorTopico(topicoId, page, size));
    }

    @PostMapping("/topicos/{topicoId}/respostas")
    @Operation(summary = "Participar de tópico — responder — include → acessar fórum (JWT)")
    public ResponseEntity<RespostaResponse> responder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID topicoId,
            @Valid @RequestBody RespostaRequest request) {
        UUID autorId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(respostaService.responder(autorId, topicoId, request));
    }
}