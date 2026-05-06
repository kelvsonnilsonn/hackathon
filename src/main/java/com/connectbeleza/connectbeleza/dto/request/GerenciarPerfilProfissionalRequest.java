package com.connectbeleza.connectbeleza.dto.request;

import com.connectbeleza.connectbeleza.domain.enums.CategoriaPsicologica;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record GerenciarPerfilProfissionalRequest(
        @NotBlank String bio,
        Integer anosExperiencia,
        @NotEmpty List<CategoriaPsicologica> especialidades,
        List<String> certificacoes,
        String urlPortfolio,
        String localizacao,
        Double latitude,
        Double longitude
) {}