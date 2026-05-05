package com.connectbeleza.connectbeleza.repository;

import com.connectbeleza.connectbeleza.domain.entity.Agenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgendaRepository extends JpaRepository<Agenda, UUID> {

    List<Agenda> findByProfissionalIdAndAtivoTrue(UUID profissionalId);

    Optional<Agenda> findByProfissionalIdAndDiaSemana(UUID profissionalId, DayOfWeek diaSemana);

    boolean existsByProfissionalIdAndDiaSemana(UUID profissionalId, DayOfWeek diaSemana);
}