package com.itau.seguro.app.infrastructure.job;

import com.itau.seguro.app.domain.model.SolicitacaoApolice;
import com.itau.seguro.app.domain.model.StatusSolicitacao;
import com.itau.seguro.app.infrastructure.persistence.SolicitacaoApoliceRepository;
import com.itau.seguro.app.infrastructure.messaging.dto.EventoPagamento;
import com.itau.seguro.app.infrastructure.messaging.dto.EventoSubscricao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimuladorEventosExternosJobTest {

    @Mock
    private SolicitacaoApoliceRepository repository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private SimuladorEventosExternosJob job;

    @Test
    @DisplayName("Deve buscar apólices pendentes sem erro")
    void deveBuscarApolicesPendentes() {
        when(repository.findByStatus(StatusSolicitacao.PENDENTE)).thenReturn(Collections.emptyList());

        job.executar();

        verify(repository).findByStatus(StatusSolicitacao.PENDENTE);
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    @DisplayName("Deve simular pagamento com sucesso")
    void deveSimularPagamentoSucesso() throws NoSuchFieldException, IllegalAccessException {
        SolicitacaoApolice apolice = SolicitacaoApolice.builder()
                .id(UUID.randomUUID())
                .status(StatusSolicitacao.PENDENTE)
                .pago(false)
                .build();

        when(repository.findByStatus(StatusSolicitacao.PENDENTE)).thenReturn(List.of(apolice));

        Random mockRandom = mock(Random.class);

        when(mockRandom.nextInt(10)).thenReturn(5, 5);

        Field randomField = SimuladorEventosExternosJob.class.getDeclaredField("random");
        randomField.setAccessible(true);
        randomField.set(job, mockRandom);

        job.executar();

        verify(kafkaTemplate).send(eq("payment-events"), eq(apolice.getId().toString()), any(EventoPagamento.class));
    }

    @Test
    @DisplayName("Deve simular pagamento rejeitado")
    void deveSimularPagamentoRejeitado() throws NoSuchFieldException, IllegalAccessException {
        SolicitacaoApolice apolice = SolicitacaoApolice.builder()
                .id(UUID.randomUUID())
                .status(StatusSolicitacao.PENDENTE)
                .pago(false)
                .build();

        when(repository.findByStatus(StatusSolicitacao.PENDENTE)).thenReturn(List.of(apolice));

        Random mockRandom = mock(Random.class);

        when(mockRandom.nextInt(10)).thenReturn(5, 9);

        Field randomField = SimuladorEventosExternosJob.class.getDeclaredField("random");
        randomField.setAccessible(true);
        randomField.set(job, mockRandom);

        job.executar();

        verify(kafkaTemplate).send(eq("payment-events"), eq(apolice.getId().toString()), argThat(
                evt -> evt instanceof EventoPagamento && "REJECTED".equals(((EventoPagamento) evt).getStatus())));
    }

    @Test
    @DisplayName("Deve simular subscrição quando já pago")
    void deveSimularSubscricao() throws NoSuchFieldException, IllegalAccessException {
        SolicitacaoApolice apolice = SolicitacaoApolice.builder()
                .id(UUID.randomUUID())
                .status(StatusSolicitacao.PENDENTE)
                .pago(true)
                .subscrito(false)
                .build();

        when(repository.findByStatus(StatusSolicitacao.PENDENTE)).thenReturn(List.of(apolice));

        Random mockRandom = mock(Random.class);

        when(mockRandom.nextInt(10)).thenReturn(5, 5);

        Field randomField = SimuladorEventosExternosJob.class.getDeclaredField("random");
        randomField.setAccessible(true);
        randomField.set(job, mockRandom);

        job.executar();

        verify(kafkaTemplate).send(eq("subscription-events"), eq(apolice.getId().toString()),
                any(EventoSubscricao.class));
    }
}
