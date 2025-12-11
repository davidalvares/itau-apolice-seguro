package com.itau.seguro.app.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitacaoApolice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID idCliente;

    @Column(nullable = false)
    private String idProduto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaProduto categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CanalVenda canalVenda;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormaPagamento formaPagamento;

    @Column(nullable = false)
    private BigDecimal valorPremioMensalTotal;

    @Column(nullable = false)
    private BigDecimal valorSegurado;

    @ElementCollection
    @CollectionTable(name = "coberturas_apolice", joinColumns = @JoinColumn(name = "id_apolice"))
    @MapKeyColumn(name = "nome_cobertura")
    @Column(name = "valor_cobertura")
    private Map<String, BigDecimal> coberturas;

    @ElementCollection
    @CollectionTable(name = "assistencias_apolice", joinColumns = @JoinColumn(name = "id_apolice"))
    @Column(name = "nome_assistencia")
    private List<String> assistencias;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSolicitacao status;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    private LocalDateTime dataFinalizacao;

    @ElementCollection
    @CollectionTable(name = "historico_apolice", joinColumns = @JoinColumn(name = "id_apolice"))
    private List<HistoricoStatus> historico = new ArrayList<>();

    private boolean pago;
    private boolean subscrito;

    @PrePersist
    public void prePersist() {
        if (this.dataCriacao == null) {
            this.dataCriacao = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = StatusSolicitacao.RECEBIDO;
        }
        adicionarHistorico(this.status);
    }

    public void atualizarStatus(StatusSolicitacao novoStatus) {
        this.status = novoStatus;
        adicionarHistorico(novoStatus);
        if (isEstadoTerminal(novoStatus)) {
            this.dataFinalizacao = LocalDateTime.now();
        }
    }

    private void adicionarHistorico(StatusSolicitacao status) {
        if (this.historico == null) {
            this.historico = new ArrayList<>();
        }
        this.historico.add(new HistoricoStatus(status, LocalDateTime.now()));
    }

    private boolean isEstadoTerminal(StatusSolicitacao status) {
        return status == StatusSolicitacao.APROVADO || status == StatusSolicitacao.REJEITADO
                || status == StatusSolicitacao.CANCELADO;
    }
}
