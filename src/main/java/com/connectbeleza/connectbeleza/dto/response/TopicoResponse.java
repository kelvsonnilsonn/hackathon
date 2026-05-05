package com.connectbeleza.connectbeleza.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record TopicoResponse(
        UUID id,
        UUID forumId,
        String nomeForumCategoria,
        UUID autorId,
        String nomeAutor,
        String titulo,
        String conteudo,
        Boolean fixado,
        Boolean fechado,
        Integer totalRespostas,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {}
