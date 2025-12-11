package com.itau.seguro.app.infrastructure.messaging.dto;

import com.itau.seguro.app.domain.model.StatusSolicitacao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoMudancaEstadoApolice {
    private UUID idApolice;
    private StatusSolicitacao status;
    private LocalDateTime dataHora;
}
