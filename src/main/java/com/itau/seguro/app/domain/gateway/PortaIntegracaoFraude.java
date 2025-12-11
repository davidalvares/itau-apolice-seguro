package com.itau.seguro.app.domain.gateway;

import com.itau.seguro.app.infrastructure.client.dto.RespostaAnaliseFraude;

import java.util.UUID;

public interface PortaIntegracaoFraude {
    RespostaAnaliseFraude analisarRisco(UUID idCliente, UUID idSolicitacao);
}
