package com.connectbeleza.connectbeleza.dto.request;

import com.connectbeleza.connectbeleza.domain.enums.TipoLembrete;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record LembreteRequest(
        @NotNull TipoLembrete tipo,
        @NotBlank @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$",
                message = "Formato de hora inválido. Use HH:mm") String horaEnvio,
        @NotBlank String mensagem
) {}