package com.itau.seguro.app.infrastructure.messaging;

import com.itau.seguro.app.domain.model.StatusSolicitacao;
import com.itau.seguro.app.infrastructure.messaging.dto.EventoMudancaEstadoApolice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProdutorEventosApoliceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private ProdutorEventosApolice produtor;

    @Test
    @DisplayName("Deve publicar evento de mudança de estado")
    void devePublicarEvento() {
        EventoMudancaEstadoApolice evento = new EventoMudancaEstadoApolice(
                UUID.randomUUID(),
                StatusSolicitacao.APROVADO,
                LocalDateTime.now());

        produtor.publicarMudancaEstado(evento);

        verify(kafkaTemplate).send(eq("policy-events"), eq(evento.getIdApolice().toString()), eq(evento));
    }
}
