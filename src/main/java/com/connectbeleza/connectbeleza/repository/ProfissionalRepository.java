package com.connectbeleza.connectbeleza.repository;

import com.connectbeleza.connectbeleza.domain.entity.Profissional;
import com.connectbeleza.connectbeleza.domain.enums.CategoriaPsicologica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfissionalRepository extends JpaRepository<Profissional, UUID> {

    Optional<Profissional> findByUsuarioId(UUID usuarioId);

    @Query("""
            SELECT p FROM Profissional p
            WHERE p.usuario.ativo = true
              AND p.verificado = true
              AND (:categoria IS NULL OR :categoria MEMBER OF p.especialidades)
              AND (:nome IS NULL OR LOWER(p.usuario.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
            """)
    Page<Profissional> buscarPorFiltros(
            @Param("categoria") CategoriaPsicologica categoria,
            @Param("nome") String nome,
            Pageable pageable);

    @Query("""
            SELECT p FROM Profissional p
            WHERE p.usuario.ativo = true
              AND p.verificado = true
              AND (:categoria IS NULL OR :categoria MEMBER OF p.especialidades)
              AND (6371 * acos(
                    cos(radians(:lat)) * cos(radians(p.latitude)) *
                    cos(radians(p.longitude) - radians(:lng)) +
                    sin(radians(:lat)) * sin(radians(p.latitude))
                  )) <= :raioKm
            """)
    Page<Profissional> buscarPorLocalizacao(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("raioKm") Double raioKm,
            @Param("categoria") CategoriaPsicologica categoria,
            Pageable pageable);
}