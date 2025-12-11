package com.itau.seguro.app.domain.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoStatus {
    private StatusSolicitacao status;
    private LocalDateTime dataHora;
}
