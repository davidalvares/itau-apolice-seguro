package com.itau.seguro.app.application.service;

import com.itau.seguro.app.domain.gateway.PortaIntegracaoFraude;
import com.itau.seguro.app.domain.model.*;
import com.itau.seguro.app.domain.service.ServicoValidacaoApolice;
import com.itau.seguro.app.infrastructure.client.dto.RespostaAnaliseFraude;
import com.itau.seguro.app.infrastructure.controller.dto.CriarSolicitacaoDTO;
import com.itau.seguro.app.infrastructure.controller.dto.SolicitacaoResponseDTO;
import com.itau.seguro.app.infrastructure.messaging.ProdutorEventosApolice;
import com.itau.seguro.app.infrastructure.messaging.dto.EventoMudancaEstadoApolice;
import com.itau.seguro.app.infrastructure.persistence.SolicitacaoApoliceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitacaoApoliceServiceTest {

        @Mock
        private SolicitacaoApoliceRepository repositorio;

        @Mock
        private PortaIntegracaoFraude portaIntegracaoFraude;

        @Mock
        private ServicoValidacaoApolice servicoValidacao;

        @Mock
        private ProdutorEventosApolice produtorEventos;

        @InjectMocks
        private SolicitacaoApoliceService service;

        @Test
        @DisplayName("Deve criar solicitação com sucesso e status PENDENTE quando validado")
        void deveCriarSolicitacaoComSucesso() {
                // Arrange
                CriarSolicitacaoDTO dto = new CriarSolicitacaoDTO(
                                UUID.randomUUID(), "prod1", CategoriaProduto.AUTO, CanalVenda.MOBILE,
                                FormaPagamento.CARTAO_CREDITO,
                                BigDecimal.TEN, BigDecimal.valueOf(10000), Collections.emptyMap(),
                                Collections.emptyList());

                SolicitacaoApolice apoliceSalva = SolicitacaoApolice.builder()
                                .id(UUID.randomUUID())
                                .idCliente(dto.idCliente())
                                .status(StatusSolicitacao.RECEBIDO)
                                .build();

                when(repositorio.save(any(SolicitacaoApolice.class))).thenReturn(apoliceSalva);
                when(portaIntegracaoFraude.analisarRisco(any(), any())).thenReturn(new RespostaAnaliseFraude(
                                UUID.randomUUID(),
                                dto.idCliente(), LocalDateTime.now(), "REGULAR", Collections.emptyList()));
                when(servicoValidacao.validar(any(), eq("REGULAR"))).thenReturn(true);

                // Act
                SolicitacaoResponseDTO response = service.criar(dto);

                // Assert
                assertNotNull(response);
                assertEquals(apoliceSalva.getId(), response.id());

                // Verifica transições de status: RECEBIDO -> VALIDADO -> PENDENTE
                verify(repositorio, atLeast(2)).save(any(SolicitacaoApolice.class));
                verify(produtorEventos, atLeast(1)).publicarMudancaEstado(any(EventoMudancaEstadoApolice.class));
        }

        @Test
        @DisplayName("Deve rejeitar solicitação quando validação de fraude falha")
        void deveRejeitarSolicitacaoQuandoFraudeFalha() {
                CriarSolicitacaoDTO dto = new CriarSolicitacaoDTO(
                                UUID.randomUUID(), "prod1", CategoriaProduto.AUTO, CanalVenda.MOBILE,
                                FormaPagamento.CARTAO_CREDITO,
                                BigDecimal.TEN, BigDecimal.valueOf(10000), Collections.emptyMap(),
                                Collections.emptyList());

                SolicitacaoApolice apoliceSalva = SolicitacaoApolice.builder()
                                .id(UUID.randomUUID())
                                .status(StatusSolicitacao.RECEBIDO)
                                .build();

                when(repositorio.save(any(SolicitacaoApolice.class))).thenReturn(apoliceSalva);
                when(portaIntegracaoFraude.analisarRisco(any(), any())).thenReturn(new RespostaAnaliseFraude(
                                UUID.randomUUID(),
                                dto.idCliente(), LocalDateTime.now(), "HIGH_RISK", Collections.emptyList()));
                when(servicoValidacao.validar(any(), eq("HIGH_RISK"))).thenReturn(false);

                service.criar(dto);

                verify(repositorio, times(1)).save(argThat(a -> a.getStatus() == StatusSolicitacao.REJEITADO));
        }

        @Test
        @DisplayName("Deve processar confirmação de pagamento com sucesso")
        void deveProcessarConfirmacaoPagamento() {
                UUID idApolice = UUID.randomUUID();
                SolicitacaoApolice apolice = SolicitacaoApolice.builder()
                                .id(idApolice)
                                .status(StatusSolicitacao.PENDENTE)
                                .pago(false)
                                .build();

                when(repositorio.findById(idApolice)).thenReturn(Optional.of(apolice));

                service.processarConfirmacaoPagamento(idApolice, true);

                assertTrue(apolice.isPago());
                verify(repositorio).save(apolice);
        }

        @Test
        @DisplayName("Deve aprovar apólice quando paga e subscrita")
        void deveAprovarApoliceFull() {
                UUID idApolice = UUID.randomUUID();
                SolicitacaoApolice apolice = SolicitacaoApolice.builder()
                                .id(idApolice)
                                .status(StatusSolicitacao.PENDENTE)
                                .pago(true)
                                .subscrito(false)
                                .build();

                when(repositorio.findById(idApolice)).thenReturn(Optional.of(apolice));

                // Act - Simula evento de subscrição
                service.processarAutorizacaoSubscricao(idApolice, true);

                // Assert
                assertTrue(apolice.isSubscrito());
                assertEquals(StatusSolicitacao.APROVADO, apolice.getStatus());
                verify(repositorio, times(2)).save(apolice); // Salva subscrito, depois salva aprovado
        }
}
