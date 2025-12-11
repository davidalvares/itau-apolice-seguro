package com.itau.seguro.app.domain.service;

import com.itau.seguro.app.domain.model.SolicitacaoApolice;
import com.itau.seguro.app.domain.model.CategoriaProduto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ServicoValidacaoApolice {

    public boolean validar(SolicitacaoApolice solicitacao, String classificacaoFraude) {
        if (classificacaoFraude == null)
            return false;

        return switch (classificacaoFraude) {
            case "REGULAR" -> validarRegular(solicitacao);
            case "HIGH_RISK" -> validarAltoRisco(solicitacao);
            case "PREFERENTIAL" -> validarPreferencial(solicitacao);
            case "NO_INFO", "SEM_INFORMACAO" -> validarSemInformacao(solicitacao);
            default -> false;
        };
    }

    private boolean validarRegular(SolicitacaoApolice solicitacao) {
        BigDecimal valor = solicitacao.getValorSegurado();
        CategoriaProduto cat = solicitacao.getCategoria();

        if (cat == CategoriaProduto.VIDA || cat == CategoriaProduto.RESIDENCIAL) {
            return valor.compareTo(new BigDecimal("500000")) <= 0;
        } else if (cat == CategoriaProduto.AUTO) {
            return valor.compareTo(new BigDecimal("350000")) <= 0;
        } else {
            return valor.compareTo(new BigDecimal("255000")) <= 0;
        }
    }

    private boolean validarAltoRisco(SolicitacaoApolice solicitacao) {
        BigDecimal valor = solicitacao.getValorSegurado();
        CategoriaProduto cat = solicitacao.getCategoria();

        if (cat == CategoriaProduto.AUTO) {
            return valor.compareTo(new BigDecimal("250000")) <= 0;
        } else if (cat == CategoriaProduto.RESIDENCIAL) {
            return valor.compareTo(new BigDecimal("150000")) <= 0;
        } else {
            return valor.compareTo(new BigDecimal("125000")) <= 0;
        }
    }

    private boolean validarPreferencial(SolicitacaoApolice solicitacao) {
        BigDecimal valor = solicitacao.getValorSegurado();
        CategoriaProduto cat = solicitacao.getCategoria();

        if (cat == CategoriaProduto.VIDA) {
            return valor.compareTo(new BigDecimal("800000")) < 0;
        } else if (cat == CategoriaProduto.AUTO || cat == CategoriaProduto.RESIDENCIAL) {
            return valor.compareTo(new BigDecimal("450000")) < 0;
        } else {
            return valor.compareTo(new BigDecimal("375000")) <= 0;
        }
    }

    private boolean validarSemInformacao(SolicitacaoApolice solicitacao) {
        BigDecimal valor = solicitacao.getValorSegurado();
        CategoriaProduto cat = solicitacao.getCategoria();

        if (cat == CategoriaProduto.VIDA || cat == CategoriaProduto.RESIDENCIAL) {
            return valor.compareTo(new BigDecimal("200000")) <= 0;
        } else if (cat == CategoriaProduto.AUTO) {
            return valor.compareTo(new BigDecimal("75000")) <= 0;
        } else {
            return valor.compareTo(new BigDecimal("55000")) <= 0;
        }
    }
}
