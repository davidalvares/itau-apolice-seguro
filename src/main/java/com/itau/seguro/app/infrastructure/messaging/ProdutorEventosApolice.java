package com.itau.seguro.app.infrastructure.messaging;

import com.itau.seguro.app.infrastructure.messaging.dto.EventoMudancaEstadoApolice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProdutorEventosApolice {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPICO = "policy-events"; // Manter tópico ou traduzir? Manter para consistencia com
                                                          // consumers externos, mas no código é TOPICO

    public void publicarMudancaEstado(EventoMudancaEstadoApolice evento) {
        log.info("Publicando evento: {}", evento);
        kafkaTemplate.send(TOPICO, evento.getIdApolice().toString(), evento);
    }
}
