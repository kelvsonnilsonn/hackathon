package com.connectbeleza.connectbeleza.dto.request;

import com.connectbeleza.connectbeleza.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarContaRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 2, max = 120)
        String nome,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, message = "Senha deve ter ao menos 8 caracteres")
        String senha,

        String telefone,

        @NotNull(message = "Role é obrigatória")
        UserRole role
) {}