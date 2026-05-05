package com.connectbeleza.connectbeleza.repository;

import com.connectbeleza.connectbeleza.domain.entity.Produto;
import com.connectbeleza.connectbeleza.domain.enums.CategoriaEstetica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, UUID> {

    Page<Produto> findByEmpresaIdAndAtivoTrue(UUID empresaId, Pageable pageable);

    Page<Produto> findByCategoriaAndAtivoTrue(CategoriaEstetica categoria, Pageable pageable);

    Page<Produto> findByAtivoTrueAndPatrocinadoTrue(Pageable pageable);
}