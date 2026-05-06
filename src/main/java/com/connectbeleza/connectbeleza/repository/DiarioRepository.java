package com.connectbeleza.connectbeleza.repository;

import com.connectbeleza.connectbeleza.domain.entity.Diario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DiarioRepository extends JpaRepository<Diario, UUID> {
}
