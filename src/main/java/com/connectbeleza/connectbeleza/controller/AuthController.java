package com.connectbeleza.connectbeleza.controller;

import com.connectbeleza.connectbeleza.dto.request.CriarContaRequest;
import com.connectbeleza.connectbeleza.dto.request.LoginRequest;
import com.connectbeleza.connectbeleza.dto.response.AuthResponse;
import com.connectbeleza.connectbeleza.dto.response.UsuarioResponse;
import com.connectbeleza.connectbeleza.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Caso de uso: CRIAR CONTA
 * Endpoint público — não requer autenticação.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Cadastro e login de usuários")
public class AuthController {

    private final UsuarioService usuarioService;

    @PostMapping("/cadastro")
    @Operation(summary = "Criar conta — caso de uso principal do fluxo de registro")
    public ResponseEntity<UsuarioResponse> criarConta(@Valid @RequestBody CriarContaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.criarConta(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login — retorna token JWT para acesso aos demais endpoints")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(usuarioService.login(request));
    }
}