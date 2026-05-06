package com.connectbeleza.connectbeleza.repository;

import com.connectbeleza.connectbeleza.domain.entity.Contratacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ContratacaoRepository extends JpaRepository<Contratacao, UUID> {
}
