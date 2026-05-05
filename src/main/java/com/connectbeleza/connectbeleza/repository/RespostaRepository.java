package com.connectbeleza.connectbeleza.repository;

import com.connectbeleza.connectbeleza.domain.entity.Resposta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RespostaRepository extends JpaRepository<Resposta, UUID> {

    Page<Resposta> findByTopicoIdOrderByCriadoEmAsc(UUID topicoId, Pageable pageable);

    long countByTopicoId(UUID topicoId);
}