package com.connectbeleza.connectbeleza.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "empresas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "razao_social", nullable = false, length = 200)
    private String razaoSocial;

    @Column(nullable = false, unique = true, length = 20)
    private String cnpj;

    @Column(length = 500)
    private String descricao;

    @Column(name = "url_site")
    private String urlSite;

    @Column(name = "url_logo")
    private String urlLogo;

    @Column(length = 300)
    private String localizacao;

    @Column(nullable = false)
    @Builder.Default
    private Boolean verificada = false;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;
}