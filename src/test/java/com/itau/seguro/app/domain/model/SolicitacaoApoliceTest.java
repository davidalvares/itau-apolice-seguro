package com.itau.seguro.app.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SolicitacaoApoliceTest {

    @Test
    @DisplayName("PrePersist deve inicializar dataCriacao e Status se nulos")
    void deveInicializarPrePersist() {
        SolicitacaoApolice apolice = new SolicitacaoApolice();

        apolice.prePersist();

        assertNotNull(apolice.getDataCriacao());
        assertEquals(StatusSolicitacao.RECEBIDO, apolice.getStatus());
        assertFalse(apolice.getHistorico().isEmpty());
        assertEquals(StatusSolicitacao.RECEBIDO, apolice.getHistorico().get(0).getStatus());
    }

    @Test
    @DisplayName("AtualizarStatus deve adicionar histórico e atualizar estado")
    void deveAtualizarStatusCorretamente() {
        SolicitacaoApolice apolice = new SolicitacaoApolice();
        apolice.prePersist(); // Inicializa histórico

        apolice.atualizarStatus(StatusSolicitacao.PENDENTE);

        assertEquals(StatusSolicitacao.PENDENTE, apolice.getStatus());
        assertEquals(2, apolice.getHistorico().size());
        assertEquals(StatusSolicitacao.PENDENTE, apolice.getHistorico().get(1).getStatus());
        assertNull(apolice.getDataFinalizacao());
    }

    @Test
    @DisplayName("Deve definir dataFinalizacao quando atingir estado terminal")
    void deveFinalizarEmEstadoTerminal() {
        SolicitacaoApolice apolice = new SolicitacaoApolice();
        apolice.prePersist();

        apolice.atualizarStatus(StatusSolicitacao.APROVADO);

        assertNotNull(apolice.getDataFinalizacao());
        assertEquals(StatusSolicitacao.APROVADO, apolice.getStatus());
    }

    @Test
    @DisplayName("Deve definir dataFinalizacao quando rejeitado")
    void deveFinalizarQuandoRejeitado() {
        SolicitacaoApolice apolice = new SolicitacaoApolice();
        apolice.prePersist();

        apolice.atualizarStatus(StatusSolicitacao.REJEITADO);

        assertNotNull(apolice.getDataFinalizacao());
    }
}
