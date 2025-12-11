package com.itau.seguro.app.infrastructure.job;

import com.itau.seguro.app.domain.model.SolicitacaoApolice;
import com.itau.seguro.app.domain.model.StatusSolicitacao;
import com.itau.seguro.app.infrastructure.persistence.SolicitacaoApoliceRepository;
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
    @DisplayName("Deve processar apólices encontradas (validação de fluxo)")
    void deveProcessarApolices() throws NoSuchFieldException, IllegalAccessException {
        SolicitacaoApolice apolice = SolicitacaoApolice.builder()
                .id(UUID.randomUUID())
                .status(StatusSolicitacao.PENDENTE)
                .pago(false)
                .build();

        when(repository.findByStatus(StatusSolicitacao.PENDENTE)).thenReturn(List.of(apolice));

        // Inject deterministic Random to force execution (skip logic < 3)
        // We want nextInt(10) to be >= 3 to proceed.
        // And for payment (nextInt(10) < 8) to be true/false.
        Random mockRandom = mock(Random.class);

        // 1. Check if skipped (nextInt(10) < 3) -> return 5 (not skipped)
        // 2. Check payment success (nextInt(10) < 8) -> return 0 (success)
        when(mockRandom.nextInt(10)).thenReturn(5, 0);

        Field randomField = SimuladorEventosExternosJob.class.getDeclaredField("random");
        randomField.setAccessible(true);
        randomField.set(job, mockRandom);

        job.executar();

        verify(kafkaTemplate).send(eq("payment-events"), eq(apolice.getId().toString()), any());
    }
}
