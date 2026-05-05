package com.connectbeleza.connectbeleza.repository;

import com.connectbeleza.connectbeleza.domain.entity.Lembrete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LembreteRepository extends JpaRepository<Lembrete, UUID> {

    List<Lembrete> findByUsuarioIdAndAtivoTrue(UUID usuarioId);

    List<Lembrete> findByAtivoTrueAndHoraEnvio(String horaEnvio);
}