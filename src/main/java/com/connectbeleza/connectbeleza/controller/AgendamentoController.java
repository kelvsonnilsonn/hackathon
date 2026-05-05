package com.connectbeleza.connectbeleza.controller;

import com.connectbeleza.connectbeleza.dto.request.AgendamentoRequest;
import com.connectbeleza.connectbeleza.dto.request.CancelarRequest;
import com.connectbeleza.connectbeleza.dto.request.RemarcarRequest;
import com.connectbeleza.connectbeleza.dto.response.AgendamentoResponse;
import com.connectbeleza.connectbeleza.service.AgendamentoService;
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

import java.util.UUID;

/**
 * Casos de uso:
 * - CONTRATAR SERVIÇO (include → realizar pagamento → criar conta)
 * - CANCELAR SERVIÇO  (extend → contratar serviço)
 * - REAGENDAR SERVIÇO (extend → contratar serviço)
 */
@RestController
@RequestMapping("/agendamentos")
@RequiredArgsConstructor
@Tag(name = "Agendamentos", description = "Contratação, cancelamento e reagendamento de serviços")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    @PostMapping
    @Operation(summary = "Contratar serviço — inclui realizar pagamento automaticamente")
    public ResponseEntity<AgendamentoResponse> contratarServico(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AgendamentoRequest request) {
        UUID clienteId = extractId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(agendamentoService.contratarServico(clienteId, request));
    }

    @GetMapping
    @Operation(summary = "Listar meus agendamentos")
    public ResponseEntity<Page<AgendamentoResponse>> listar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID clienteId = extractId(userDetails);
        return ResponseEntity.ok(agendamentoService.listarDoCliente(clienteId, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhe de um agendamento")
    public ResponseEntity<AgendamentoResponse> buscarPorId(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        UUID clienteId = extractId(userDetails);
        return ResponseEntity.ok(agendamentoService.buscarPorId(id, clienteId));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar serviço — extend de contratar serviço, gera estorno automático")
    public ResponseEntity<AgendamentoResponse> cancelar(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @RequestBody(required = false) CancelarRequest request) {
        UUID clienteId = extractId(userDetails);
        return ResponseEntity.ok(
                agendamentoService.cancelarServico(clienteId, id,
                        request != null ? request : new CancelarRequest(null)));
    }

    @PatchMapping("/{id}/reagendar")
    @Operation(summary = "Reagendar serviço — extend de contratar serviço, preserva data anterior")
    public ResponseEntity<AgendamentoResponse> reagendar(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody RemarcarRequest request) {
        UUID clienteId = extractId(userDetails);
        return ResponseEntity.ok(agendamentoService.reagendarServico(clienteId, id, request));
    }

    private UUID extractId(UserDetails userDetails) {
        return UUID.fromString(userDetails.getUsername());
    }
}