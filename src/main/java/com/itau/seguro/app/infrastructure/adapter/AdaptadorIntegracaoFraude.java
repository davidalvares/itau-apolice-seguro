package com.itau.seguro.app.infrastructure.adapter;

import com.itau.seguro.app.domain.gateway.PortaIntegracaoFraude;
import com.itau.seguro.app.infrastructure.client.ClienteFraude;
import com.itau.seguro.app.infrastructure.client.dto.RespostaAnaliseFraude;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdaptadorIntegracaoFraude implements PortaIntegracaoFraude {

    private final ClienteFraude clienteFraude;

    @Override
    public RespostaAnaliseFraude analisarRisco(UUID idCliente, UUID idSolicitacao) {
        return clienteFraude.analisarRisco(idCliente, idSolicitacao);
    }
}
