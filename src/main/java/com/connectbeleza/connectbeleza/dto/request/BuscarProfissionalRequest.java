package com.connectbeleza.connectbeleza.dto.request;

import com.connectbeleza.connectbeleza.domain.enums.CategoriaPsicologica;

public record BuscarProfissionalRequest(
        String nome,
        CategoriaPsicologica categoria,
        Double latitude,
        Double longitude,
        Double raioKm
) {}
