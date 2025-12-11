package com.itau.seguro.app.infrastructure.messaging;

import com.itau.seguro.app.application.service.SolicitacaoApoliceService;
import com.itau.seguro.app.infrastructure.messaging.dto.EventoPagamento;
import com.itau.seguro.app.infrastructure.messaging.dto.EventoSubscricao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConsumidorEventosApoliceTest {

    @Mock
    private SolicitacaoApoliceService service;

    @InjectMocks
    private ConsumidorEventosApolice consumidor;

    @Test
    @DisplayName("Deve processar evento de pagamento confirmado")
    void deveProcessarPagamentoConfirmado() {
        EventoPagamento evento = new EventoPagamento();
        evento.setIdApolice(UUID.randomUUID());
        evento.setStatus("CONFIRMED");

        consumidor.consumirPagamento(evento);

        verify(service).processarConfirmacaoPagamento(evento.getIdApolice(), true);
    }

    @Test
    @DisplayName("Deve processar evento de pagamento rejeitado")
    void deveProcessarPagamentoRejeitado() {
        EventoPagamento evento = new EventoPagamento();
        evento.setIdApolice(UUID.randomUUID());
        evento.setStatus("REJECTED");

        consumidor.consumirPagamento(evento);

        verify(service).processarConfirmacaoPagamento(evento.getIdApolice(), false);
    }

    @Test
    @DisplayName("Deve processar evento de subscrição autorizada")
    void deveProcessarSubscricaoAutorizada() {
        EventoSubscricao evento = new EventoSubscricao();
        evento.setIdApolice(UUID.randomUUID());
        evento.setStatus("AUTHORIZED");

        consumidor.consumirSubscricao(evento);

        verify(service).processarAutorizacaoSubscricao(evento.getIdApolice(), true);
    }

    @Test
    @DisplayName("Deve processar evento de subscrição negada")
    void deveProcessarSubscricaoNegada() {
        EventoSubscricao evento = new EventoSubscricao();
        evento.setIdApolice(UUID.randomUUID());
        evento.setStatus("DENIED");

        consumidor.consumirSubscricao(evento);

        verify(service).processarAutorizacaoSubscricao(evento.getIdApolice(), false);
    }
}
