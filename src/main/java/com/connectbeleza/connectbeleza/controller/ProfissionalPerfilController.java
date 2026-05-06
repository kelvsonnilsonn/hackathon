package com.connectbeleza.connectbeleza.controller;

import com.connectbeleza.connectbeleza.dto.request.AgendaRequest;
import com.connectbeleza.connectbeleza.dto.request.GerenciarPerfilProfissionalRequest;
import com.connectbeleza.connectbeleza.dto.request.ServicoRequest;
import com.connectbeleza.connectbeleza.dto.response.*;
import com.connectbeleza.connectbeleza.service.GerenciarPerfilProfissionalService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Casos de uso do PROFISSIONAL:
 * - GERENCIAR PERFIL
 * - OFERECER SERVIÇOS (include → DEFINIR PREÇO)
 * - DEFINIR AGENDA    (include de oferecer serviços)
 * - PARTICIPAR DA COMUNIDADE → encaminhado ao ForumController
 * - GERENCIAR PARCERIAS
 * - VISUALIZAR MÉTRICAS
 */
@RestController
@RequestMapping("/profissional/perfil")
@RequiredArgsConstructor
@Tag(name = "Profissional — Perfil & Serviços", description = "Gestão do perfil profissional")
public class ProfissionalPerfilController {

    private final GerenciarPerfilProfissionalService perfilService;

    // ─── GERENCIAR PERFIL ────────────────────────────────────────────────────

    @PutMapping
    @Operation(summary = "Gerenciar perfil — atualiza bio, especialidades, certificações e localização")
    public ResponseEntity<ProfissionalResponse> atualizarPerfil(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody GerenciarPerfilProfissionalRequest request) {
        return ResponseEntity.ok(perfilService.atualizarPerfil(uid(ud), request));
    }

    @GetMapping("/metricas")
    @Operation(summary = "Visualizar métricas de desempenho")
    public ResponseEntity<MetricasProfissionalResponse> metricas(
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(perfilService.obterMetricas(uid(ud)));
    }

    // ─── OFERECER SERVIÇOS ────────────────────────────────────────────────────

    @PostMapping("/servicos")
    @Operation(summary = "Oferecer serviço — include → definir preço (preço no body)")
    public ResponseEntity<ServicoResponse> criarServico(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody ServicoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(perfilService.criarServico(uid(ud), request));
    }

    @PutMapping("/servicos/{servicoId}")
    @Operation(summary = "Atualizar serviço")
    public ResponseEntity<ServicoResponse> atualizarServico(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable UUID servicoId,
            @Valid @RequestBody ServicoRequest request) {
        return ResponseEntity.ok(perfilService.atualizarServico(uid(ud), servicoId, request));
    }

    @PatchMapping("/servicos/{servicoId}/preco")
    @Operation(summary = "Definir preço — atualiza preço de serviço existente")
    public ResponseEntity<ServicoResponse> definirPreco(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable UUID servicoId,
            @RequestParam BigDecimal valor) {
        return ResponseEntity.ok(perfilService.atualizarPreco(uid(ud), servicoId, valor));
    }

    @DeleteMapping("/servicos/{servicoId}")
    @Operation(summary = "Desativar serviço")
    public ResponseEntity<Void> desativarServico(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable UUID servicoId) {
        perfilService.desativarServico(uid(ud), servicoId);
        return ResponseEntity.noContent().build();
    }

    // ─── DEFINIR AGENDA ───────────────────────────────────────────────────────

    @GetMapping("/agenda")
    @Operation(summary = "Listar disponibilidade semanal")
    public ResponseEntity<List<AgendaResponse>> listarAgenda(
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(perfilService.listarAgenda(uid(ud)));
    }

    @PostMapping("/agenda")
    @Operation(summary = "Definir agenda — configura horário disponível por dia da semana")
    public ResponseEntity<AgendaResponse> definirAgenda(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody AgendaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(perfilService.definirAgenda(uid(ud), request));
    }

    // ─── AGENDAMENTOS RECEBIDOS ───────────────────────────────────────────────

    @GetMapping("/agendamentos")
    @Operation(summary = "Ver agendamentos recebidos dos clientes")
    public ResponseEntity<Page<AgendamentoResponse>> agendamentosRecebidos(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(perfilService.listarAgendamentos(uid(ud), page, size));
    }

    private UUID uid(UserDetails ud) {
        return UUID.fromString(ud.getUsername());
    }
}