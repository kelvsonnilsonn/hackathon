package com.connectbeleza.connectbeleza.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Caso de uso: DEFINIR AGENDA (include de oferecer serviços)
 * Representa a disponibilidade semanal do profissional.
 */
@Entity
@Table(name = "agendas",
        uniqueConstraints = @UniqueConstraint(columnNames = {"profissional_id", "dia_semana"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agenda {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false)
    private DayOfWeek diaSemana;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;
}