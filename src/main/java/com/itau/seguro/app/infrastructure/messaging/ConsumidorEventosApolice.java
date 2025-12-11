package com.itau.seguro.app.infrastructure.messaging;

import com.itau.seguro.app.application.service.SolicitacaoApoliceService;
import com.itau.seguro.app.infrastructure.messaging.dto.EventoPagamento;
import com.itau.seguro.app.infrastructure.messaging.dto.EventoSubscricao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConsumidorEventosApolice {

    private final SolicitacaoApoliceService service;

    @KafkaListener(topics = "payment-events", groupId = "apolice-group")
    public void consumirPagamento(EventoPagamento evento) {
        log.info("Recebido evento pagamento: {}", evento);
        boolean sucesso = "CONFIRMED".equalsIgnoreCase(evento.getStatus());
        service.processarConfirmacaoPagamento(evento.getIdApolice(), sucesso);
    }

    @KafkaListener(topics = "subscription-events", groupId = "apolice-group")
    public void consumirSubscricao(EventoSubscricao evento) {
        log.info("Recebido evento subscricao: {}", evento);
        boolean sucesso = "AUTHORIZED".equalsIgnoreCase(evento.getStatus());
        service.processarAutorizacaoSubscricao(evento.getIdApolice(), sucesso);
    }
}
