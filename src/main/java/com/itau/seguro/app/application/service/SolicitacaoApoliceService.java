package com.itau.seguro.app.application.service;

import com.itau.seguro.app.domain.gateway.PortaIntegracaoFraude;
import com.itau.seguro.app.domain.model.SolicitacaoApolice;
import com.itau.seguro.app.domain.model.StatusSolicitacao;
import com.itau.seguro.app.domain.service.ServicoValidacaoApolice;
import com.itau.seguro.app.infrastructure.client.dto.RespostaAnaliseFraude;
import com.itau.seguro.app.infrastructure.controller.dto.CriarSolicitacaoDTO;
import com.itau.seguro.app.infrastructure.controller.dto.SolicitacaoResponseDTO;
import com.itau.seguro.app.infrastructure.messaging.ProdutorEventosApolice;
import com.itau.seguro.app.infrastructure.messaging.dto.EventoMudancaEstadoApolice;
import com.itau.seguro.app.infrastructure.persistence.SolicitacaoApoliceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SolicitacaoApoliceService {

    private final SolicitacaoApoliceRepository repositorio;
    private final PortaIntegracaoFraude portaIntegracaoFraude;
    private final ServicoValidacaoApolice servicoValidacao;
    private final ProdutorEventosApolice produtorEventos;

    @Transactional
    public SolicitacaoResponseDTO criar(CriarSolicitacaoDTO dto) {
        SolicitacaoApolice entidade = SolicitacaoApolice.builder()
                .idCliente(dto.idCliente())
                .idProduto(dto.idProduto())
                .categoria(dto.categoria())
                .canalVenda(dto.canalVenda())
                .formaPagamento(dto.formaPagamento())
                .valorPremioMensalTotal(dto.valorPremioMensalTotal())
                .valorSegurado(dto.valorSegurado())
                .coberturas(dto.coberturas())
                .assistencias(dto.assistencias())
                .build();

        // Salvar Estado Inicial (RECEBIDO via PrePersist)
        entidade = repositorio.save(entidade);
        publicarEvento(entidade);

        // Processar Validação (Sincrono por enquanto)
        processarValidacao(entidade);

        return mapearParaDTO(entidade);
    }

    public SolicitacaoResponseDTO buscarPorId(UUID id) {
        return repositorio.findById(id)
                .map(this::mapearParaDTO)
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada"));
    }

    public java.util.List<SolicitacaoResponseDTO> buscarPorIdCliente(UUID idCliente) {
        return repositorio.findByIdCliente(idCliente).stream()
                .map(this::mapearParaDTO)
                .toList();
    }

    @Transactional
    public void cancelar(UUID id) {
        SolicitacaoApolice entidade = repositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada"));

        if (entidade.getStatus() == StatusSolicitacao.APROVADO || entidade.getStatus() == StatusSolicitacao.REJEITADO) {
            throw new RuntimeException("Não é possível cancelar uma solicitação finalizada");
        }

        entidade.atualizarStatus(StatusSolicitacao.CANCELADO);
        repositorio.save(entidade);
        publicarEvento(entidade);
    }

    @Transactional
    public void processarConfirmacaoPagamento(UUID idApolice, boolean sucesso) {
        SolicitacaoApolice entidade = repositorio.findById(idApolice)
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada"));

        if (!sucesso) {
            atualizarStatus(entidade, StatusSolicitacao.REJEITADO);
            return;
        }

        entidade.setPago(true);
        repositorio.save(entidade);
        verificarAprovacao(entidade);
    }

    @Transactional
    public void processarAutorizacaoSubscricao(UUID idApolice, boolean sucesso) {
        SolicitacaoApolice entidade = repositorio.findById(idApolice)
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada"));

        if (!sucesso) {
            atualizarStatus(entidade, StatusSolicitacao.REJEITADO);
            return;
        }

        entidade.setSubscrito(true);
        repositorio.save(entidade);
        verificarAprovacao(entidade);
    }

    private void verificarAprovacao(SolicitacaoApolice entidade) {
        if (entidade.isPago() && entidade.isSubscrito() && entidade.getStatus() != StatusSolicitacao.REJEITADO) {
            atualizarStatus(entidade, StatusSolicitacao.APROVADO);
        }
    }

    private void processarValidacao(SolicitacaoApolice entidade) {
        try {
            RespostaAnaliseFraude respostaFraude = portaIntegracaoFraude.analisarRisco(entidade.getIdCliente(),
                    entidade.getId());
            boolean valido = servicoValidacao.validar(entidade, respostaFraude.classification());

            if (valido) {
                atualizarStatus(entidade, StatusSolicitacao.VALIDADO);
                atualizarStatus(entidade, StatusSolicitacao.PENDENTE);
            } else {
                atualizarStatus(entidade, StatusSolicitacao.REJEITADO);
            }
        } catch (Exception e) {
            log.error("Erro durante validação - Integração indisponível ou falha", e);
            atualizarStatus(entidade, StatusSolicitacao.EM_ANALISE_MANUAL);
        }
    }

    private void atualizarStatus(SolicitacaoApolice entidade, StatusSolicitacao status) {
        entidade.atualizarStatus(status);
        repositorio.save(entidade);
        publicarEvento(entidade);
    }

    private void publicarEvento(SolicitacaoApolice entidade) {
        produtorEventos.publicarMudancaEstado(new EventoMudancaEstadoApolice(
                entidade.getId(),
                entidade.getStatus(),
                LocalDateTime.now()));
    }

    private SolicitacaoResponseDTO mapearParaDTO(SolicitacaoApolice entidade) {
        return new SolicitacaoResponseDTO(
                entidade.getId(),
                entidade.getIdCliente(),
                entidade.getIdProduto(),
                entidade.getCategoria(),
                entidade.getCanalVenda(),
                entidade.getFormaPagamento(),
                entidade.getStatus(),
                entidade.getDataCriacao(),
                entidade.getDataFinalizacao(),
                entidade.getValorPremioMensalTotal(),
                entidade.getValorSegurado(),
                entidade.getCoberturas(),
                entidade.getAssistencias(),
                entidade.getHistorico());
    }
}
