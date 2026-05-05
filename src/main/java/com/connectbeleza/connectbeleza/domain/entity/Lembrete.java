package com.connectbeleza.connectbeleza.domain.entity;

import com.connectbeleza.connectbeleza.domain.enums.TipoLembrete;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Caso de uso: RECEBER LEMBRETE (ator sistema)
 * Lembretes inteligentes: manhã, tarde, noite (beber água, rotina skincare etc.)
 */
@Entity
@Table(name = "lembretes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lembrete {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoLembrete tipo;

    @Column(nullable = false, length = 200)
    private String mensagem;

    @Column(name = "hora_envio", nullable = false)
    private String horaEnvio; // "08:00", "13:00", "21:00"

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;
}