package com.connectbeleza.connectbeleza.repository;

import com.connectbeleza.connectbeleza.domain.entity.Servico;
import com.connectbeleza.connectbeleza.domain.enums.CategoriaEstetica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, UUID> {

    Page<Servico> findByProfissionalIdAndAtivoTrue(UUID profissionalId, Pageable pageable);

    Page<Servico> findByCategoriaAndAtivoTrue(CategoriaEstetica categoria, Pageable pageable);
}