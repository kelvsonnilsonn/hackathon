package com.connectbeleza.connectbeleza.controller;

import com.connectbeleza.connectbeleza.dto.request.AgendamentoRequest;
import com.connectbeleza.connectbeleza.dto.request.ContratacaoRequest;
import com.connectbeleza.connectbeleza.dto.response.AgendamentoResponse;
import com.connectbeleza.connectbeleza.dto.response.ContratacaoResponse;
import com.connectbeleza.connectbeleza.service.ContratacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/contratar")
@RequiredArgsConstructor
@Tag(name = "Agendamentos", description = "Contratação e cancelamento")
public class ContratacaoController {

    private final ContratacaoService contratacaoService;

    @PostMapping
    @Operation(summary = "Contratar serviço — inclui realizar pagamento automaticamente")
    public ResponseEntity<ContratacaoResponse> contratarServico(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ContratacaoRequest request) {
        UUID clienteId = extractId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contratacaoService.contratarServico(clienteId, request));
    }

    private UUID extractId(UserDetails userDetails) {
        return UUID.fromString(userDetails.getUsername());
    }
}
