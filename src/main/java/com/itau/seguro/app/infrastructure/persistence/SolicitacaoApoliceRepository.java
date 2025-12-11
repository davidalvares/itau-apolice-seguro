package com.itau.seguro.app.infrastructure.persistence;

import com.itau.seguro.app.domain.model.SolicitacaoApolice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SolicitacaoApoliceRepository extends JpaRepository<SolicitacaoApolice, UUID> {
    List<SolicitacaoApolice> findByIdCliente(UUID idCliente);

    List<SolicitacaoApolice> findByStatus(com.itau.seguro.app.domain.model.StatusSolicitacao status);
}
