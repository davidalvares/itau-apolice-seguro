package com.itau.seguro.app.domain.service;

import com.itau.seguro.app.domain.model.CategoriaProduto;
import com.itau.seguro.app.domain.model.SolicitacaoApolice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicoValidacaoApoliceTest {

    private ServicoValidacaoApolice servico;

    @BeforeEach
    void setUp() {
        servico = new ServicoValidacaoApolice();
    }

    @Test
    @DisplayName("Deve retornar false se classificação de fraude for nula")
    void deveRetornarFalseSeClassificacaoFraudeNula() {
        assertFalse(servico.validar(new SolicitacaoApolice(), null));
    }

    @Test
    @DisplayName("Deve retornar false se classificação for desconhecida")
    void deveRetornarFalseSeClassificacaoDesconhecida() {
        assertFalse(servico.validar(new SolicitacaoApolice(), "DESCONHECIDO"));
    }

    @ParameterizedTest
    @CsvSource({
            "VIDA, 500000, true",
            "VIDA, 500000.01, false",
            "RESIDENCIAL, 500000, true",
            "RESIDENCIAL, 500001, false",
            "AUTO, 350000, true",
            "AUTO, 350001, false",
            "OUTROS, 255000, true",
            "OUTROS, 255001, false"
    })
    @DisplayName("Deve validar regras para perfil REGULAR")
    void deveValidarRegrasRegular(CategoriaProduto categoria, String valorStr, boolean resultadoEsperado) {
        SolicitacaoApolice solicitacao = SolicitacaoApolice.builder()
                .categoria(categoria)
                .valorSegurado(new BigDecimal(valorStr))
                .build();

        if (resultadoEsperado) {
            assertTrue(servico.validar(solicitacao, "REGULAR"));
        } else {
            assertFalse(servico.validar(solicitacao, "REGULAR"));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "AUTO, 250000, true",
            "AUTO, 250001, false",
            "RESIDENCIAL, 150000, true",
            "RESIDENCIAL, 150001, false",
            "VIDA, 125000, true",
            "VIDA, 125001, false"
    })
    @DisplayName("Deve validar regras para perfil HIGH_RISK")
    void deveValidarRegrasAltoRisco(CategoriaProduto categoria, String valorStr, boolean resultadoEsperado) {
        SolicitacaoApolice solicitacao = SolicitacaoApolice.builder()
                .categoria(categoria)
                .valorSegurado(new BigDecimal(valorStr))
                .build();

        if (resultadoEsperado) {
            assertTrue(servico.validar(solicitacao, "HIGH_RISK"));
        } else {
            assertFalse(servico.validar(solicitacao, "HIGH_RISK"));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "VIDA, 799999, true",
            "VIDA, 800000, false",
            "AUTO, 449999, true",
            "AUTO, 450000, false",
            "RESIDENCIAL, 449999, true",
            "RESIDENCIAL, 450000, false",
            "OUTROS, 375000, true",
            "OUTROS, 375001, false"
    })
    @DisplayName("Deve validar regras para perfil PREFERENTIAL")
    void deveValidarRegrasPreferencial(CategoriaProduto categoria, String valorStr, boolean resultadoEsperado) {
        SolicitacaoApolice solicitacao = SolicitacaoApolice.builder()
                .categoria(categoria)
                .valorSegurado(new BigDecimal(valorStr))
                .build();

        if (resultadoEsperado) {
            assertTrue(servico.validar(solicitacao, "PREFERENTIAL"));
        } else {
            assertFalse(servico.validar(solicitacao, "PREFERENTIAL"));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "VIDA, 200000, true",
            "VIDA, 200001, false",
            "AUTO, 75000, true",
            "AUTO, 75001, false",
            "OUTROS, 55000, true",
            "OUTROS, 55001, false"
    })
    @DisplayName("Deve validar regras para perfil NO_INFO")
    void deveValidarRegrasNoInfo(CategoriaProduto categoria, String valorStr, boolean resultadoEsperado) {
        SolicitacaoApolice solicitacao = SolicitacaoApolice.builder()
                .categoria(categoria)
                .valorSegurado(new BigDecimal(valorStr))
                .build();

        if (resultadoEsperado) {
            assertTrue(servico.validar(solicitacao, "NO_INFO"));
        } else {
            assertFalse(servico.validar(solicitacao, "NO_INFO"));
        }
    }
}
