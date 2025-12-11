package com.itau.seguro.app.infrastructure.controller.dto;

import com.itau.seguro.app.domain.model.FormaPagamento;
import com.itau.seguro.app.domain.model.CategoriaProduto;
import com.itau.seguro.app.domain.model.CanalVenda;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CriarSolicitacaoDTO(
        UUID idCliente,
        String idProduto,
        CategoriaProduto categoria,
        CanalVenda canalVenda,
        FormaPagamento formaPagamento,
        BigDecimal valorPremioMensalTotal,
        BigDecimal valorSegurado,
        Map<String, BigDecimal> coberturas,
        List<String> assistencias) {
}
