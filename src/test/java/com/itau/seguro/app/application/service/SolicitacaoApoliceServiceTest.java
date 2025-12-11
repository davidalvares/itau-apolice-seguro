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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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
                UUID idCliente = UUID.randomUUID();
                CriarSolicitacaoDTO dto = new CriarSolicitacaoDTO(
                                idCliente, "prod1", CategoriaProduto.AUTO, CanalVenda.MOBILE,
                                FormaPagamento.CARTAO_CREDITO,
                                BigDecimal.TEN, BigDecimal.valueOf(10000), Collections.emptyMap(),
                                Collections.emptyList());

                SolicitacaoApolice apoliceSalva = SolicitacaoApolice.builder()
                                .id(UUID.randomUUID())
                                .idCliente(dto.idCliente())
                                .status(StatusSolicitacao.RECEBIDO)
                                .build();

                // Mocking save to return the same instance to simulate JPA behavior loosely but
                // we capture the state changes
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

                // Verify final state
                assertEquals(StatusSolicitacao.PENDENTE, apoliceSalva.getStatus());

                // Verify interactions
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

                assertEquals(StatusSolicitacao.REJEITADO, apoliceSalva.getStatus());
                verify(repositorio, atLeast(2)).save(any(SolicitacaoApolice.class));
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
        @DisplayName("Deve rejeitar apólice se pagamento falhar")
        void deveRejeitarPagamento() {
                UUID idApolice = UUID.randomUUID();
                SolicitacaoApolice apolice = SolicitacaoApolice.builder()
                                .id(idApolice)
                                .status(StatusSolicitacao.PENDENTE)
                                .pago(false)
                                .build();

                when(repositorio.findById(idApolice)).thenReturn(Optional.of(apolice));

                service.processarConfirmacaoPagamento(idApolice, false);

                assertFalse(apolice.isPago());
                assertEquals(StatusSolicitacao.REJEITADO, apolice.getStatus());
                verify(repositorio).save(apolice);
                verify(produtorEventos)
                                .publicarMudancaEstado(argThat(e -> e.getStatus() == StatusSolicitacao.REJEITADO));
        }

        @Test
        @DisplayName("Deve aprovar apólice quando paga e então subscrita")
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
                // Deve publicar evento de APROVADO
                verify(produtorEventos, atLeastOnce())
                                .publicarMudancaEstado(argThat(e -> e.getStatus() == StatusSolicitacao.APROVADO));
        }

        @Test
        @DisplayName("Deve cancelar apólice pendente com sucesso")
        void deveCancelarApolicePendente() {
                UUID idApolice = UUID.randomUUID();
                SolicitacaoApolice apolice = SolicitacaoApolice.builder()
                                .id(idApolice)
                                .status(StatusSolicitacao.PENDENTE)
                                .build();

                when(repositorio.findById(idApolice)).thenReturn(Optional.of(apolice));

                service.cancelar(idApolice);

                assertEquals(StatusSolicitacao.CANCELADO, apolice.getStatus());
                verify(repositorio).save(apolice);
                verify(produtorEventos)
                                .publicarMudancaEstado(argThat(e -> e.getStatus() == StatusSolicitacao.CANCELADO));
        }

        @Test
        @DisplayName("Deve falhar ao cancelar apólice já finalizada")
        void deveFalharCancelarApoliceFinalizada() {
                UUID idApolice = UUID.randomUUID();
                SolicitacaoApolice apolice = SolicitacaoApolice.builder()
                                .id(idApolice)
                                .status(StatusSolicitacao.APROVADO)
                                .build();

                when(repositorio.findById(idApolice)).thenReturn(Optional.of(apolice));

                assertThrows(RuntimeException.class, () -> service.cancelar(idApolice));
                verify(repositorio, never()).save(any());
        }

        @Test
        @DisplayName("Deve buscar apólices por cliente")
        void deveBuscarPorCliente() {
                UUID idCliente = UUID.randomUUID();
                SolicitacaoApolice apolice = SolicitacaoApolice.builder()
                                .id(UUID.randomUUID())
                                .idCliente(idCliente)
                                .status(StatusSolicitacao.APROVADO)
                                .build();

                when(repositorio.findByIdCliente(idCliente)).thenReturn(List.of(apolice));

                List<SolicitacaoResponseDTO> resultado = service.buscarPorIdCliente(idCliente);

                assertFalse(resultado.isEmpty());
                assertEquals(1, resultado.size());
                assertEquals(apolice.getId(), resultado.get(0).id());
        }
}
