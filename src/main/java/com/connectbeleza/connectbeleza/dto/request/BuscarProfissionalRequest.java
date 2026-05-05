package com.connectbeleza.connectbeleza.dto.request;

import com.connectbeleza.connectbeleza.domain.enums.CategoriaEstetica;

public record BuscarProfissionalRequest(
        String nome,
        CategoriaEstetica categoria,
        Double latitude,
        Double longitude,
        Double raioKm
) {}
