package com.connectbeleza.connectbeleza.controller;

import com.connectbeleza.connectbeleza.dto.request.LembreteRequest;
import com.connectbeleza.connectbeleza.dto.response.LembreteResponse;
import com.connectbeleza.connectbeleza.service.LembreteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso: RECEBER LEMBRETE (ator Sistema)
 * O sistema dispara lembretes inteligentes (manhã/tarde/noite) via @Scheduled.
 * Este controller permite ao usuário configurar e visualizar seus lembretes.
 */
@RestController
@RequestMapping("/lembretes")
@RequiredArgsConstructor
@Tag(name = "Lembretes", description = "Lembretes inteligentes de autocuidado disparados pelo sistema")
public class LembreteController {

    private final LembreteService lembreteService;

    @GetMapping
    @Operation(summary = "Listar meus lembretes ativos")
    public ResponseEntity<List<LembreteResponse>> listar(
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(lembreteService.listarMeusLembretes(uid(ud)));
    }

    @PostMapping
    @Operation(summary = "Configurar lembrete personalizado — o sistema o dispara no horário definido")
    public ResponseEntity<LembreteResponse> configurar(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody LembreteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lembreteService.configurarLembrete(uid(ud), request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar lembrete")
    public ResponseEntity<Void> desativar(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable UUID id) {
        lembreteService.desativarLembrete(uid(ud), id);
        return ResponseEntity.noContent().build();
    }

    private UUID uid(UserDetails ud) {
        return UUID.fromString(ud.getUsername());
    }
}