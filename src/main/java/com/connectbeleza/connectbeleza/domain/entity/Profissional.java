package com.connectbeleza.connectbeleza.domain.entity;

import com.connectbeleza.connectbeleza.domain.enums.CategoriaPsicologica;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "profissionais")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profissional {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(length = 500)
    private String bio;

    @Column(name = "anos_experiencia")
    private Integer anosExperiencia;

    @ElementCollection
    @CollectionTable(name = "profissional_especialidades",
            joinColumns = @JoinColumn(name = "profissional_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "especialidade")
    private List<CategoriaPsicologica> especialidades;

    @ElementCollection
    @CollectionTable(name = "profissional_certificacoes",
            joinColumns = @JoinColumn(name = "profissional_id"))
    @Column(name = "certificacao", length = 200)
    private List<String> certificacoes;

    @Column(name = "url_portfolio")
    private String urlPortfolio;

    @Column(name = "verificado", nullable = false)
    @Builder.Default
    private Boolean verificado = false;

    @Column(length = 200)
    private String localizacao;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;
}