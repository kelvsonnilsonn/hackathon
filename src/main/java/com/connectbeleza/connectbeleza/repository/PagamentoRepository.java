package com.connectbeleza.connectbeleza.repository;

import com.connectbeleza.connectbeleza.domain.entity.Pagamento;
import com.connectbeleza.connectbeleza.domain.enums.StatusPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, UUID> {

    Optional<Pagamento> findByAgendamentoId(UUID agendamentoId);

    boolean existsByAgendamentoIdAndStatus(UUID agendamentoId, StatusPagamento status);
}