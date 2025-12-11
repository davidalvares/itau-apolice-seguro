package com.itau.seguro.app.infrastructure.controller.dto;

import com.itau.seguro.app.domain.model.FormaPagamento;
import com.itau.seguro.app.domain.model.CategoriaProduto;
import com.itau.seguro.app.domain.model.StatusSolicitacao;
import com.itau.seguro.app.domain.model.CanalVenda;
import com.itau.seguro.app.domain.model.HistoricoStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SolicitacaoResponseDTO(
        UUID id,
        UUID idCliente,
        String idProduto,
        CategoriaProduto categoria,
        CanalVenda canalVenda,
        FormaPagamento formaPagamento,
        StatusSolicitacao status,
        LocalDateTime dataCriacao,
        LocalDateTime dataFinalizacao,
        BigDecimal valorPremioMensalTotal,
        BigDecimal valorSegurado,
        Map<String, BigDecimal> coberturas,
        List<String> assistencias,
        List<HistoricoStatus> historico) {
}
