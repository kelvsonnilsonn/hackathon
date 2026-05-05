package com.connectbeleza.connectbeleza.repository;

import com.connectbeleza.connectbeleza.domain.entity.Avaliacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, UUID> {

    boolean existsByAgendamentoId(UUID agendamentoId);

    Page<Avaliacao> findByProfissionalIdOrderByCriadoEmDesc(UUID profissionalId, Pageable pageable);

    @Query("SELECT AVG(a.nota) FROM Avaliacao a WHERE a.profissional.id = :profissionalId")
    Optional<Double> calcularMediaPorProfissional(@Param("profissionalId") UUID profissionalId);

    long countByProfissionalId(UUID profissionalId);
}