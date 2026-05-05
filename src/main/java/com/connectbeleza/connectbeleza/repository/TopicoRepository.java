package com.connectbeleza.connectbeleza.repository;

import com.connectbeleza.connectbeleza.domain.entity.Topico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TopicoRepository extends JpaRepository<Topico, UUID> {

    Page<Topico> findByForumIdOrderByFixadoDescCriadoEmDesc(UUID forumId, Pageable pageable);

    Page<Topico> findByAutorIdOrderByCriadoEmDesc(UUID autorId, Pageable pageable);

    @Query("""
            SELECT t FROM Topico t
            WHERE t.forum.id = :forumId
              AND LOWER(t.titulo) LIKE LOWER(CONCAT('%', :termo, '%'))
            ORDER BY t.criadoEm DESC
            """)
    Page<Topico> buscarPorTermo(
            @Param("forumId") UUID forumId,
            @Param("termo") String termo,
            Pageable pageable);
}