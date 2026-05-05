package com.connectbeleza.connectbeleza.dto.response;

import com.connectbeleza.connectbeleza.domain.enums.CategoriaEstetica;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProfissionalResponse(
        UUID id,
        UUID usuarioId,
        String nome,
        String email,
        String bio,
        Integer anosExperiencia,
        List<CategoriaEstetica> especialidades,
        List<String> certificacoes,
        String urlPortfolio,
        BigDecimal notaMedia,
        Integer totalAvaliacoes,
        Boolean verificado,
        String localizacao
) {}