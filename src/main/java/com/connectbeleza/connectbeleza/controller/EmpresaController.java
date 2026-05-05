package com.connectbeleza.connectbeleza.controller;

import com.connectbeleza.connectbeleza.dto.request.ParceriaRequest;
import com.connectbeleza.connectbeleza.dto.request.ProdutoRequest;
import com.connectbeleza.connectbeleza.dto.response.ParceriaResponse;
import com.connectbeleza.connectbeleza.dto.response.ProdutoResponse;
import com.connectbeleza.connectbeleza.service.EmpresaService;
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
 * Casos de uso da EMPRESA:
 * - PROMOVER PRODUTOS
 * - REALIZAR PARCERIA COM PROFISSIONAL
 */
@RestController
@RequestMapping("/empresa")
@RequiredArgsConstructor
@Tag(name = "Empresa", description = "Promoção de produtos e parcerias com profissionais")
public class EmpresaController {

    private final EmpresaService empresaService;

    // ─── PROMOVER PRODUTOS ────────────────────────────────────────────────────

    @GetMapping("/produtos")
    @Operation(summary = "Listar meus produtos")
    public ResponseEntity<Page<ProdutoResponse>> listarProdutos(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(empresaService.listarProdutosDaEmpresa(uid(ud), page, size));
    }

    @PostMapping("/produtos")
    @Operation(summary = "Promover produto — cadastra produto da empresa na plataforma")
    public ResponseEntity<ProdutoResponse> criarProduto(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody ProdutoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(empresaService.criarProduto(uid(ud), request));
    }

    @PutMapping("/produtos/{produtoId}")
    @Operation(summary = "Atualizar produto")
    public ResponseEntity<ProdutoResponse> atualizarProduto(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable UUID produtoId,
            @Valid @RequestBody ProdutoRequest request) {
        return ResponseEntity.ok(empresaService.atualizarProduto(uid(ud), produtoId, request));
    }

    @DeleteMapping("/produtos/{produtoId}")
    @Operation(summary = "Remover produto da vitrine")
    public ResponseEntity<Void> removerProduto(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable UUID produtoId) {
        empresaService.removerProduto(uid(ud), produtoId);
        return ResponseEntity.noContent().build();
    }

    // ─── REALIZAR PARCERIA COM PROFISSIONAL ───────────────────────────────────

    @GetMapping("/parcerias")
    @Operation(summary = "Listar parcerias solicitadas pela empresa")
    public ResponseEntity<Page<ParceriaResponse>> listarParcerias(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(empresaService.listarParcerias(uid(ud), page, size));
    }

    @PostMapping("/parcerias")
    @Operation(summary = "Realizar parceria com profissional — envia solicitação")
    public ResponseEntity<ParceriaResponse> solicitarParceria(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody ParceriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(empresaService.solicitarParceria(uid(ud), request));
    }

    private UUID uid(UserDetails ud) {
        return UUID.fromString(ud.getUsername());
    }
}