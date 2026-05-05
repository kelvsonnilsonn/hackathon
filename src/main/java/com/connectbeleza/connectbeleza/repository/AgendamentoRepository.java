package com.connectbeleza.connectbeleza.repository;

import com.connectbeleza.connectbeleza.domain.entity.Agendamento;
import com.connectbeleza.connectbeleza.domain.enums.StatusAgendamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, UUID> {

    Page<Agendamento> findByClienteIdOrderByDataHoraAgendadaDesc(UUID clienteId, Pageable pageable);

    @Query("""
            SELECT a FROM Agendamento a
            WHERE a.servico.profissional.id = :profissionalId
              AND a.status = :status
              AND a.dataHoraAgendada BETWEEN :inicio AND :fim
            """)
    List<Agendamento> findByProfissionalAndStatusAndPeriodo(
            @Param("profissionalId") UUID profissionalId,
            @Param("status") StatusAgendamento status,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);

    boolean existsByClienteIdAndServicoIdAndStatus(
            UUID clienteId, UUID servicoId, StatusAgendamento status);

    @Query("""
            SELECT COUNT(a) > 0 FROM Agendamento a
            WHERE a.servico.profissional.id = :profissionalId
              AND a.status NOT IN ('CANCELADO')
              AND a.dataHoraAgendada < :fim
              AND :inicio < a.dataHoraAgendada
            """)
    boolean existeConflitoDeHorario(
            @Param("profissionalId") UUID profissionalId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);

    default Page<Agendamento> findByServicosProfissionalIdOrderByDataHoraAgendadaDesc(UUID id, Pageable pageable) {
        return findByServicosProfissionalIdOrderByDataHoraAgendadaDesc(id, pageable);
    }
}