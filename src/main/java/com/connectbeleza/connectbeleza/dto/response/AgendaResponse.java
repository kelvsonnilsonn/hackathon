package com.connectbeleza.connectbeleza.dto.response;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public record AgendaResponse(
        UUID id,
        DayOfWeek diaSemana,
        LocalTime horaInicio,
        LocalTime horaFim,
        Boolean ativo
) {}