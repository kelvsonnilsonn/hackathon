package com.connectbeleza.connectbeleza.controller;

import com.connectbeleza.connectbeleza.dto.request.AvaliacaoRequest;
import com.connectbeleza.connectbeleza.dto.response.AvaliacaoResponse;
import com.connectbeleza.connectbeleza.service.AvaliacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Caso de uso: AVALIAR PROFISSIONAL (include → contratar serviço)
 */
@RestController
@RequestMapping("/avaliacoes")
@RequiredArgsConstructor
@Tag(name = "Avaliações", description = "Avaliação de profissionais após serviço concluído")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    @PostMapping
    @Operation(summary = "Avaliar profissional — somente após agendamento CONCLUIDO")
    public ResponseEntity<AvaliacaoResponse> avaliar(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AvaliacaoRequest request) {
        UUID clienteId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(avaliacaoService.avaliarProfissional(clienteId, request));
    }
}