package com.connectbeleza.connectbeleza.repository;

import com.connectbeleza.connectbeleza.domain.entity.Forum;
import com.connectbeleza.connectbeleza.domain.enums.CategoriaEstetica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ForumRepository extends JpaRepository<Forum, UUID> {

    Optional<Forum> findByCategoria(CategoriaEstetica categoria);

    boolean existsByCategoria(CategoriaEstetica categoria);
}