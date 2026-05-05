package com.connectbeleza.connectbeleza.domain.entity;

import com.connectbeleza.connectbeleza.domain.enums.StatusParceria;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Caso de uso: REALIZAR PARCERIA COM PROFISSIONAL (empresa)
 *              GERENCIAR PARCERIAS (profissional)
 */
@Entity
@Table(name = "parcerias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parceria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    @Column(length = 600)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatusParceria status = StatusParceria.PENDENTE;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;
}