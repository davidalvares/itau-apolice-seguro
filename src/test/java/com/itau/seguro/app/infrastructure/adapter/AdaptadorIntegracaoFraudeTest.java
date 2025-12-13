package com.itau.seguro.app.infrastructure.adapter;

import com.itau.seguro.app.infrastructure.client.ClienteFraude;
import com.itau.seguro.app.infrastructure.client.dto.RespostaAnaliseFraude;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdaptadorIntegracaoFraudeTest {

    @Mock
    private ClienteFraude clienteFraude;

    @InjectMocks
    private AdaptadorIntegracaoFraude adaptador;

    @Test
    @DisplayName("Deve delegar análise de risco para cliente Feign")
    void deveDelegarAnaliseParaCliente() {
        UUID idCliente = UUID.randomUUID();
        UUID idSolicitacao = UUID.randomUUID();
        RespostaAnaliseFraude respostaEsperada = new RespostaAnaliseFraude(
                UUID.randomUUID(), idCliente, LocalDateTime.now(), "REGULAR", Collections.emptyList());

        when(clienteFraude.analisarRisco(idCliente, idSolicitacao)).thenReturn(respostaEsperada);

        RespostaAnaliseFraude resultado = adaptador.analisarRisco(idCliente, idSolicitacao);

        assertEquals(respostaEsperada, resultado);
        verify(clienteFraude).analisarRisco(idCliente, idSolicitacao);
    }

    @Test
    @DisplayName("Deve propagar exceção quando o cliente falhar")
    void devePropagarExcecaoQuandoClienteFalha() {
        UUID idCliente = UUID.randomUUID();
        UUID idSolicitacao = UUID.randomUUID();

        when(clienteFraude.analisarRisco(idCliente, idSolicitacao)).thenThrow(new RuntimeException("API indisponível"));

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> adaptador.analisarRisco(idCliente, idSolicitacao));
    }
}
