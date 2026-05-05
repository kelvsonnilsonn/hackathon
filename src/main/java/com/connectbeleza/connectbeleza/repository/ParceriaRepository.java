package com.connectbeleza.connectbeleza.repository;

import com.connectbeleza.connectbeleza.domain.entity.Parceria;
import com.connectbeleza.connectbeleza.domain.enums.StatusParceria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ParceriaRepository extends JpaRepository<Parceria, UUID> {

    Page<Parceria> findByEmpresaIdOrderByCriadoEmDesc(UUID empresaId, Pageable pageable);

    Page<Parceria> findByProfissionalIdOrderByCriadoEmDesc(UUID profissionalId, Pageable pageable);

    Page<Parceria> findByProfissionalIdAndStatusOrderByCriadoEmDesc(
            UUID profissionalId, StatusParceria status, Pageable pageable);

    boolean existsByEmpresaIdAndProfissionalIdAndStatus(
            UUID empresaId, UUID profissionalId, StatusParceria status);
}