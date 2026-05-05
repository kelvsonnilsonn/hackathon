package com.connectbeleza.connectbeleza.controller;

import com.connectbeleza.connectbeleza.domain.enums.CategoriaEstetica;
import com.connectbeleza.connectbeleza.dto.request.BuscarProfissionalRequest;
import com.connectbeleza.connectbeleza.dto.response.AvaliacaoResponse;
import com.connectbeleza.connectbeleza.dto.response.ProfissionalResponse;
import com.connectbeleza.connectbeleza.dto.response.ServicoResponse;
import com.connectbeleza.connectbeleza.service.AvaliacaoService;
import com.connectbeleza.connectbeleza.service.ProfissionalService;
import com.connectbeleza.connectbeleza.service.ServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Caso de uso: BUSCAR PROFISSIONAL — include → FILTRAR POR ESPECIALIDADE/CATEGORIA
 */
@RestController
@RequestMapping("/profissionais")
@RequiredArgsConstructor
@Tag(name = "Profissionais", description = "Busca e filtro de profissionais de beleza")
public class ProfissionalController {

    private final ProfissionalService profissionalService;
    private final ServicoService servicoService;
    private final AvaliacaoService avaliacaoService;

    @GetMapping
    @Operation(summary = "Buscar profissionais — suporta filtro por categoria, nome e localização")
    public ResponseEntity<Page<ProfissionalResponse>> buscar(
            @RequestParam(required = false) CategoriaEstetica categoria,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false, defaultValue = "10.0") Double raioKm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        BuscarProfissionalRequest filtro = new BuscarProfissionalRequest(
                nome, categoria, latitude, longitude, raioKm);

        return ResponseEntity.ok(profissionalService.buscar(filtro, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhes de um profissional")
    public ResponseEntity<ProfissionalResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(profissionalService.buscarPorId(id));
    }

    @GetMapping("/{id}/servicos")
    @Operation(summary = "Serviços oferecidos pelo profissional")
    public ResponseEntity<Page<ServicoResponse>> listarServicos(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(servicoService.listarPorProfissional(id, page, size));
    }

    @GetMapping("/{id}/avaliacoes")
    @Operation(summary = "Avaliações recebidas pelo profissional")
    public ResponseEntity<Page<AvaliacaoResponse>> listarAvaliacoes(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(avaliacaoService.listarPorProfissional(id, page, size));
    }
}