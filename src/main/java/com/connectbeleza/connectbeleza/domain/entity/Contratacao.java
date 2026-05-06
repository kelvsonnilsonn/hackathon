package com.connectbeleza.connectbeleza.domain.entity;

import com.connectbeleza.connectbeleza.domain.enums.CategoriaPsicologica;
import com.connectbeleza.connectbeleza.domain.enums.StatusContratacao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Contratacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="psicologo_id")
    private Profissional psicologo;

    @Enumerated(EnumType.STRING)
    private StatusContratacao status;

    @Column(name="vencimento_contrato")
    private LocalDateTime vencimento;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name="atualizado_em")
    private LocalDateTime atualizadoEm;

    public Contratacao(Usuario usuario, Profissional profissional){
        this.usuario = usuario;
        this.psicologo = profissional;
        this.vencimento = LocalDateTime.now().plusMonths(1);
    }

    private void renovarContratacao(){
        if(status == StatusContratacao.ATRASADO){
            System.out.println("...");
        }
        this.vencimento = LocalDateTime.now().plusMonths(1);
    }

    @Scheduled(fixedRate = 60000 * 1440)
    private boolean checarContrato(){
        this.status = StatusContratacao.ATRASADO;
        return vencimento.isAfter(LocalDateTime.now());
    }

}
